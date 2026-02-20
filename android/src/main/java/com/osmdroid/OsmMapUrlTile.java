package com.osmdroid;

import android.content.Context;
import android.util.Log;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;

public class OsmMapUrlTile extends OsmMapFeature {

    private static final String TAG = "OsmMapUrlTile";

    private String urlTemplate;
    private float minimumZ = 0.0f;
    private float maximumZ = 20.0f;
    private float maximumNativeZ = -1.0f;
    private boolean offlineMode = false;
    private String tileCachePath = null;
    private long tileCacheMaxAge = 0;
    private int tileSize = 256;
    private boolean doubleTileSize = false;
    private boolean flipY = false;
    private float opacity = 1.0f;

    private MapView mapView;
    private TilesOverlay tilesOverlay;
    private boolean isConfigured = false;
    private MapTileProviderBase customTileProvider;

    public OsmMapUrlTile(Context context) {
        super(context);
    }

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

    private void configureTileSource() {
        if (mapView == null || urlTemplate == null || urlTemplate.isEmpty()) {
            Log.w(TAG, "Cannot configure: mapView or urlTemplate is null");
            return;
        }

        if (isConfigured) {
            return;
        }

        try {
            int minZoom = (int) minimumZ;
            int maxZoom = (int) maximumZ;
            int maxNativeZoom = maximumNativeZ > 0 ? (int) maximumNativeZ : maxZoom;
            int effectiveTileSize = doubleTileSize ? 512 : tileSize;
            boolean isFileUrl = urlTemplate.startsWith("file://");

            if (tileCachePath != null && !tileCachePath.isEmpty()) {
                configureTileCache(tileCachePath);
            }

            if (isFileUrl) {
                configureLocalFileTiles(minZoom, maxZoom, effectiveTileSize);
            } else if (offlineMode) {
                configureOfflineMode(minZoom, maxZoom, maxNativeZoom, effectiveTileSize);
            } else {
                configureOnlineMode(minZoom, maxZoom, maxNativeZoom, effectiveTileSize);
            }

            if (opacity < 1.0f) {
                applyOpacity();
            }

            isConfigured = true;
            mapView.invalidate();
            Log.d(TAG, "Configured: " + urlTemplate + " (offline=" + offlineMode + ", file=" + isFileUrl + ")");

        } catch (Exception e) {
            Log.e(TAG, "Error configuring tile source", e);
        }
    }

    private void configureLocalFileTiles(int minZoom, int maxZoom, int effectiveTileSize) {
        String baseDir = extractBaseDirectory(urlTemplate);
        String extension = extractFileExtension(urlTemplate);

        File dir = new File(baseDir);
        if (!dir.exists()) {
            Log.w(TAG, "Local tiles directory does not exist: " + baseDir);
        } else {
            Log.d(TAG, "Local tiles directory: " + baseDir + " (extension: " + extension + ")");
        }

        // Configure osmdroid to use the local directory as its tile cache
        // This is critical - MapTileFilesystemProvider reads from osmdroid's configured cache path
        Configuration.getInstance().setOsmdroidBasePath(dir);
        Configuration.getInstance().setOsmdroidTileCache(dir);

        XYTileSource tileSource = createLocalTileSource(minZoom, maxZoom, effectiveTileSize, baseDir, extension);
        MapTileProviderBase provider = createFilesystemProvider(tileSource);

        mapView.setTileSource(tileSource);
        mapView.setTileProvider(provider);
        mapView.setUseDataConnection(false);
        customTileProvider = provider;
    }

    private void configureOfflineMode(int minZoom, int maxZoom, int maxNativeZoom, int effectiveTileSize) {
        OnlineTileSourceBase tileSource = createOnlineTileSource(minZoom, maxZoom, maxNativeZoom, effectiveTileSize);
        MapTileProviderBase provider = createFilesystemProvider(tileSource);

        mapView.setTileSource(tileSource);
        mapView.setTileProvider(provider);
        mapView.setUseDataConnection(false);
        customTileProvider = provider;
    }

    private void configureOnlineMode(int minZoom, int maxZoom, int maxNativeZoom, int effectiveTileSize) {
        OnlineTileSourceBase tileSource = createOnlineTileSource(minZoom, maxZoom, maxNativeZoom, effectiveTileSize);
        mapView.setTileSource(tileSource);
        mapView.setUseDataConnection(true);
    }

    private void configureTileCache(String cachePath) {
        try {
            String path = cachePath.replace("file://", "");
            File cacheDir = new File(path);
            
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            Configuration.getInstance().setOsmdroidBasePath(cacheDir);
            Configuration.getInstance().setOsmdroidTileCache(cacheDir);

            if (tileCacheMaxAge > 0) {
                Configuration.getInstance().setExpirationOverrideDuration(tileCacheMaxAge * 1000);
            } else {
                clearExpirationOverrideDuration();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configuring cache", e);
        }
    }

    String extractBaseDirectory(String template) {
        String path = template.replace("file://", "");
        
        // Check if template contains placeholders (old format)
        int placeholderIndex = path.indexOf("{z}");
        if (placeholderIndex > 0) {
            path = path.substring(0, placeholderIndex);
        }
        
        // Remove trailing slash
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        // Remove file extension if path ends with one (e.g., .png, .jpg)
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash && lastDot > 0) {
            String ext = path.substring(lastDot).toLowerCase();
            if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".webp")) {
                path = path.substring(0, lastDot);
            }
        }
        
        return path;
    }

    String extractFileExtension(String template) {
        String sanitizedTemplate = stripQueryAndFragment(template);

        // Check for extension in template (e.g., file:///path/tiles.jpg or file:///path/{z}/{x}/{y}.jpg)
        int lastDot = sanitizedTemplate.lastIndexOf('.');
        int lastSlash = sanitizedTemplate.lastIndexOf('/');
        
        if (lastDot > lastSlash && lastDot > 0) {
            String ext = sanitizedTemplate.substring(lastDot).toLowerCase();
            // Check for placeholder after dot (means no extension specified)
            if (!ext.contains("{") && (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".webp"))) {
                return ext;
            }
        }
        
        return ".png";
    }

    String stripQueryAndFragment(String template) {
        int queryIndex = template.indexOf('?');
        int hashIndex = template.indexOf('#');

        int end = template.length();
        if (queryIndex >= 0) {
            end = Math.min(end, queryIndex);
        }
        if (hashIndex >= 0) {
            end = Math.min(end, hashIndex);
        }

        return template.substring(0, end);
    }

    private XYTileSource createLocalTileSource(int minZoom, int maxZoom, int tileSize, String baseDir, String extension) {
        // Use empty string as tile source name to avoid subdirectory lookup
        // osmdroid looks for tiles at: {cache_path}/{tile_source_name}/{z}/{x}/{y}.ext
        // With empty name, it looks at: {cache_path}/{z}/{x}/{y}.ext
        final String finalExtension = extension;
        return new XYTileSource("", minZoom, maxZoom, tileSize, extension, new String[]{}) {
            @Override
            public String getTileRelativeFilenameString(long pMapTileIndex) {
                int z = MapTileIndex.getZoom(pMapTileIndex);
                int x = MapTileIndex.getX(pMapTileIndex);
                int y = MapTileIndex.getY(pMapTileIndex);
                if (flipY) {
                    y = (1 << z) - 1 - y;
                }
                return z + "/" + x + "/" + y + finalExtension;
            }
        };
    }

    private OnlineTileSourceBase createOnlineTileSource(int minZoom, int maxZoom, int maxNativeZoom, int tileSize) {
        String extension = extractFileExtension(urlTemplate);

        return new OnlineTileSourceBase("CustomTiles", minZoom, maxZoom, tileSize, extension, new String[]{urlTemplate}) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                int z = MapTileIndex.getZoom(pMapTileIndex);
                int x = MapTileIndex.getX(pMapTileIndex);
                int y = MapTileIndex.getY(pMapTileIndex);

                int[] resolvedCoordinates =
                        resolveOnlineTileCoordinates(z, x, y, maxNativeZoom, flipY);
                int effectiveZ = resolvedCoordinates[0];
                int effectiveX = resolvedCoordinates[1];
                int effectiveY = resolvedCoordinates[2];

                return getBaseUrl()
                        .replace("{z}", String.valueOf(effectiveZ))
                        .replace("{x}", String.valueOf(effectiveX))
                        .replace("{y}", String.valueOf(effectiveY));
            }
        };
    }

    int[] resolveOnlineTileCoordinates(int z, int x, int y, int maxNativeZoom, boolean shouldFlipY) {
        int effectiveZ = z;
        int effectiveX = x;
        int effectiveY = y;

        if (maxNativeZoom >= 0 && z > maxNativeZoom) {
            int zoomDiff = z - maxNativeZoom;
            int scale = 1 << zoomDiff;
            effectiveX = x / scale;
            effectiveY = y / scale;
            effectiveZ = maxNativeZoom;
        }

        if (shouldFlipY) {
            effectiveY = (1 << effectiveZ) - 1 - effectiveY;
        }

        return new int[]{effectiveZ, effectiveX, effectiveY};
    }

    private void clearExpirationOverrideDuration() {
        try {
            Configuration.class
                    .getMethod("setExpirationOverrideDuration", Long.class)
                    .invoke(Configuration.getInstance(), new Object[]{null});
        } catch (Exception ignored) {
            Configuration.getInstance().setExpirationOverrideDuration(0L);
        }
    }

    private MapTileProviderBase createFilesystemProvider(XYTileSource tileSource) {
        Context context = getContext();
        ArrayList<MapTileModuleProviderBase> providers = new ArrayList<>();

        providers.add(new MapTileFilesystemProvider(
                new SimpleRegisterReceiver(context), tileSource));

        try {
            providers.add(new MapTileFileArchiveProvider(
                    new SimpleRegisterReceiver(context), tileSource));
        } catch (Exception e) {
            Log.d(TAG, "Archive provider not available");
        }

        return new MapTileProviderArray(tileSource, new SimpleRegisterReceiver(context),
                providers.toArray(new MapTileModuleProviderBase[0]));
    }

    private MapTileProviderBase createFilesystemProvider(OnlineTileSourceBase tileSource) {
        Context context = getContext();
        ArrayList<MapTileModuleProviderBase> providers = new ArrayList<>();

        providers.add(new MapTileFilesystemProvider(
                new SimpleRegisterReceiver(context), tileSource));

        return new MapTileProviderArray(tileSource, new SimpleRegisterReceiver(context),
                providers.toArray(new MapTileModuleProviderBase[0]));
    }

    private void applyOpacity() {
        tilesOverlay = mapView.getOverlayManager().getTilesOverlay();
        if (tilesOverlay != null) {
            tilesOverlay.setColorFilter(createColorFilter(opacity));
        }
    }

    private android.graphics.ColorFilter createColorFilter(float opacity) {
        int alpha = (int) (opacity * 255);
        return new android.graphics.PorterDuffColorFilter(
                android.graphics.Color.argb(alpha, 255, 255, 255),
                android.graphics.PorterDuff.Mode.DST_IN);
    }
}
