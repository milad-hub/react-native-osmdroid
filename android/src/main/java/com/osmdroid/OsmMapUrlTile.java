package com.osmdroid;

import android.content.Context;
import android.util.Log;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;

/**
 * Enhanced UrlTile component with full offline support, local storage, and caching
 */
public class OsmMapUrlTile extends OsmMapFeature {

  private static final String TAG = "OsmMapUrlTile";

  // Props
  private String urlTemplate;
  private float minimumZ = 0.0f;
  private float maximumZ = 20.0f;
  private float maximumNativeZ = -1.0f; // -1 means use maximumZ
  private boolean offlineMode = false;
  private String tileCachePath = null;
  private long tileCacheMaxAge = 0; // 0 means no expiration
  private int tileSize = 256;
  private boolean doubleTileSize = false;
  private boolean flipY = false;
  private float opacity = 1.0f;

  // State
  private MapView mapView;
  private TilesOverlay tilesOverlay;
  private boolean isConfigured = false;
  private MapTileProviderBase customTileProvider;

  public OsmMapUrlTile(Context context) {
    super(context);
  }

  // Setters for all props
  public void setUrlTemplate(String urlTemplate) {
    this.urlTemplate = urlTemplate;
    this.isConfigured = false;
  }

  public void setMinimumZ(float minimumZ) {
    this.minimumZ = minimumZ;
    this.isConfigured = false;
  }

  public void setMaximumZ(float maximumZ) {
    this.maximumZ = maximumZ;
    this.isConfigured = false;
  }

  public void setMaximumNativeZ(float maximumNativeZ) {
    this.maximumNativeZ = maximumNativeZ;
    this.isConfigured = false;
  }

  public void setOfflineMode(boolean offlineMode) {
    this.offlineMode = offlineMode;
    this.isConfigured = false;
  }

  public void setTileCachePath(String tileCachePath) {
    this.tileCachePath = tileCachePath;
    this.isConfigured = false;
  }

  public void setTileCacheMaxAge(long tileCacheMaxAge) {
    this.tileCacheMaxAge = tileCacheMaxAge;
    this.isConfigured = false;
  }

  public void setTileSize(int tileSize) {
    this.tileSize = tileSize;
    this.isConfigured = false;
  }

  public void setDoubleTileSize(boolean doubleTileSize) {
    this.doubleTileSize = doubleTileSize;
    this.isConfigured = false;
  }

  public void setFlipY(boolean flipY) {
    this.flipY = flipY;
    this.isConfigured = false;
  }

  public void setOpacity(float opacity) {
    this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
    if (tilesOverlay != null) {
      tilesOverlay.setColorFilter(createColorFilter(this.opacity));
    }
  }

  @Override
  public Object getFeature() {
    return this;
  }

  @Override
  public void addToMap(MapView map) {
    this.mapView = map;
    configureTileSource();
  }

  @Override
  public void removeFromMap(MapView map) {
    if (tilesOverlay != null) {
      map.getOverlays().remove(tilesOverlay);
      tilesOverlay = null;
    }
    if (customTileProvider != null) {
      customTileProvider.detach();
      customTileProvider = null;
    }
    map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
    this.mapView = null;
  }

  /**
   * Configure the appropriate tile source based on props
   */
  private void configureTileSource() {
    if (mapView == null || urlTemplate == null || urlTemplate.isEmpty()) {
      Log.w(TAG, "Cannot configure tile source: mapView or urlTemplate is null");
      return;
    }

    if (isConfigured) {
      return; // Already configured
    }

    try {
      // Determine effective zoom levels
      int minZoom = (int) minimumZ;
      int maxZoom = (int) maximumZ;
      int maxNativeZoom = maximumNativeZ > 0 ? (int) maximumNativeZ : maxZoom;

      // Adjust tile size if doubleTileSize is enabled
      int effectiveTileSize = doubleTileSize ? 512 : tileSize;

      // Check if this is a file:// URL
      boolean isFileUrl = urlTemplate.startsWith("file://");

      // Configure cache if specified
      if (tileCachePath != null && !tileCachePath.isEmpty()) {
        configureTileCache(tileCachePath);
      }

      ITileSource tileSource;
      MapTileProviderBase tileProvider = null;

      if (isFileUrl) {
        // Handle local file:// URLs
        Log.d(TAG, "Configuring local file tile source");
        tileSource = createFileTileSource(minZoom, maxZoom, effectiveTileSize);
        tileProvider = createOfflineTileProvider(tileSource, isFileUrl);
        
        // For file:// URLs, always disable network
        mapView.setUseDataConnection(false);
        
      } else if (offlineMode) {
        // Handle offline mode with cached tiles
        Log.d(TAG, "Configuring offline mode with cache");
        tileSource = createOnlineTileSource(minZoom, maxZoom, maxNativeZoom, effectiveTileSize);
        tileProvider = createOfflineTileProvider(tileSource, false);
        
        // Disable network in offline mode
        mapView.setUseDataConnection(false);
        
      } else {
        // Handle online mode
        Log.d(TAG, "Configuring online tile source");
        tileSource = createOnlineTileSource(minZoom, maxZoom, maxNativeZoom, effectiveTileSize);
        
        // Enable network for online mode
        mapView.setUseDataConnection(true);
      }

      // Apply tile source
      mapView.setTileSource(tileSource);

      // Apply custom tile provider if created
      if (tileProvider != null) {
        customTileProvider = tileProvider;
        mapView.setTileProvider(tileProvider);
      }

      // Apply opacity if needed
      if (opacity < 1.0f) {
        setupTilesOverlayWithOpacity();
      }

      isConfigured = true;
      mapView.invalidate();

      Log.d(TAG, String.format("Tile source configured: %s (offline=%b, fileUrl=%b, cache=%s, size=%d)",
          urlTemplate, offlineMode, isFileUrl, tileCachePath, effectiveTileSize));

    } catch (Exception e) {
      Log.e(TAG, "Error configuring tile source", e);
    }
  }

  /**
   * Configure tile cache directory
   */
  private void configureTileCache(String cachePath) {
    try {
      // Remove file:// prefix if present
      String path = cachePath.replace("file://", "");
      
      File cacheDir = new File(path);
      if (!cacheDir.exists()) {
        boolean created = cacheDir.mkdirs();
        Log.d(TAG, "Cache directory created: " + created + " at " + path);
      }

      // Configure osmdroid cache
      Configuration.getInstance().setOsmdroidBasePath(cacheDir);
      Configuration.getInstance().setOsmdroidTileCache(cacheDir);
      
      // Set cache expiration if specified
      if (tileCacheMaxAge > 0) {
        Configuration.getInstance().setExpirationOverrideDuration(tileCacheMaxAge * 1000); // Convert to milliseconds
      }

      Log.d(TAG, "Tile cache configured at: " + path);
    } catch (Exception e) {
      Log.e(TAG, "Error configuring tile cache", e);
    }
  }

  /**
   * Create tile provider for offline mode or local files
   */
  private MapTileProviderBase createOfflineTileProvider(ITileSource tileSource, boolean isFileUrl) {
    ArrayList<MapTileModuleProviderBase> providers = new ArrayList<>();
    Context context = getContext();

    if (isFileUrl) {
      // For file:// URLs, use filesystem provider
      String basePath = urlTemplate.replace("file://", "");
      
      // Create custom file-based tile source
      FileBasedTileSource fileBasedTileSource = new FileBasedTileSource(
          "LocalTiles",
          (int) minimumZ,
          (int) maximumZ,
          tileSize,
          ".png",
          new String[]{basePath}
      ) {
        @Override
        public String getTileRelativeFilenameString(long pMapTileIndex) {
          int zoom = MapTileIndex.getZoom(pMapTileIndex);
          int x = MapTileIndex.getX(pMapTileIndex);
          int y = MapTileIndex.getY(pMapTileIndex);
          
          if (flipY) {
            y = flipYCoordinate(y, zoom);
          }
          
          // Return path in format: z/x/y.png
          return String.format("%d/%d/%d.png", zoom, x, y);
        }
      };

      // Add filesystem provider
      MapTileFilesystemProvider filesystemProvider = new MapTileFilesystemProvider(
          new SimpleRegisterReceiver(context),
          fileBasedTileSource
      );
      providers.add(filesystemProvider);

      // Also try to add archive provider (for .zip, .mbtiles, etc.)
      try {
        MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
            new SimpleRegisterReceiver(context),
            fileBasedTileSource
        );
        providers.add(archiveProvider);
      } catch (Exception e) {
        Log.d(TAG, "Archive provider not available: " + e.getMessage());
      }

    } else {
      // For offline mode with cache, use filesystem provider with cache directory
      MapTileFilesystemProvider filesystemProvider = new MapTileFilesystemProvider(
          new SimpleRegisterReceiver(context),
          tileSource
      );
      providers.add(filesystemProvider);
    }

    // Create array provider
    MapTileProviderArray tileProviderArray = new MapTileProviderArray(
        tileSource,
        new SimpleRegisterReceiver(context),
        providers.toArray(new MapTileModuleProviderBase[0])
    );

    Log.d(TAG, "Offline tile provider created with " + providers.size() + " providers");
    return tileProviderArray;
  }

  /**
   * Create tile source for file:// URLs
   */
  private ITileSource createFileTileSource(int minZoom, int maxZoom, int tileSize) {
    // Remove file:// prefix
    String basePath = urlTemplate.replace("file://", "");

    return new XYTileSource(
        "FileTileSource",
        minZoom,
        maxZoom,
        tileSize,
        ".png",
        new String[]{basePath}
    ) {
      @Override
      public String getTileURLString(long pMapTileIndex) {
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        
        if (flipY) {
          y = flipYCoordinate(y, zoom);
        }

        // Build file path from template
        String path = basePath
            .replace("{z}", String.valueOf(zoom))
            .replace("{x}", String.valueOf(x))
            .replace("{y}", String.valueOf(y));

        // Ensure it starts with file://
        if (!path.startsWith("file://") && !path.startsWith("/")) {
          path = "file://" + path;
        } else if (path.startsWith("/")) {
          path = "file://" + path;
        }

        return path;
      }
    };
  }

  /**
   * Create tile source for online HTTP/HTTPS URLs
   */
  private ITileSource createOnlineTileSource(int minZoom, int maxZoom, int maxNativeZoom, int tileSize) {
    return new OnlineTileSourceBase(
        "CustomTileSource",
        minZoom,
        maxZoom,
        tileSize,
        ".png",
        new String[]{urlTemplate}
    ) {
      @Override
      public String getTileURLString(long pMapTileIndex) {
        // Use maxNativeZoom for tile scaling
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int adjustedZoom = Math.min(zoom, maxNativeZoom);
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        
        if (flipY) {
          y = flipYCoordinate(y, zoom);
        }

        String url = getBaseUrl()
            .replace("{z}", String.valueOf(adjustedZoom))
            .replace("{x}", String.valueOf(x))
            .replace("{y}", String.valueOf(y));

        return url;
      }
    };
  }

  /**
   * Setup tiles overlay with custom opacity
   */
  private void setupTilesOverlayWithOpacity() {
    if (tilesOverlay == null) {
      tilesOverlay = mapView.getOverlayManager().getTilesOverlay();
    }
    if (tilesOverlay != null) {
      tilesOverlay.setColorFilter(createColorFilter(opacity));
    }
  }

  /**
   * Create color filter for opacity
   */
  private android.graphics.ColorFilter createColorFilter(float opacity) {
    int alpha = (int) (opacity * 255);
    return new android.graphics.PorterDuffColorFilter(
        android.graphics.Color.argb(alpha, 255, 255, 255),
        android.graphics.PorterDuff.Mode.DST_IN
    );
  }

  /**
   * Flip Y coordinate for TMS tile systems
   */
  private int flipYCoordinate(int y, int zoom) {
    return (1 << zoom) - 1 - y;
  }
}
