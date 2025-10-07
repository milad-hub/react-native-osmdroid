package com.osmdroid;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class OsmMapUrlTileManager extends ViewGroupManager<OsmMapUrlTile> {

  public OsmMapUrlTileManager() {
    super();
  }

  @Override
  public String getName() {
    return "OsmMapUrlTile";
  }

  @Override
  public OsmMapUrlTile createViewInstance(ThemedReactContext context) {
    return new OsmMapUrlTile(context);
  }

  @ReactProp(name = "urlTemplate")
  public void setUrlTemplate(OsmMapUrlTile view, String urlTemplate) {
    view.setUrlTemplate(urlTemplate);
  }

  @ReactProp(name = "minimumZ", defaultFloat = 0.0f)
  public void setMinimumZ(OsmMapUrlTile view, float minimumZ) {
    view.setMinimumZ(minimumZ);
  }

  @ReactProp(name = "maximumZ", defaultFloat = 100.0f)
  public void setMaximumZ(OsmMapUrlTile view, float maximumZ) {
    view.setMaximumZ(maximumZ);
  }

  @ReactProp(name = "maximumNativeZ")
  public void setMaximumNativeZ(OsmMapUrlTile view, float maximumNativeZ) {
    view.setMaximumNativeZ(maximumNativeZ);
  }

  @ReactProp(name = "offlineMode", defaultBoolean = false)
  public void setOfflineMode(OsmMapUrlTile view, boolean offlineMode) {
    view.setOfflineMode(offlineMode);
  }

  @ReactProp(name = "tileCachePath")
  public void setTileCachePath(OsmMapUrlTile view, String tileCachePath) {
    view.setTileCachePath(tileCachePath);
  }

  @ReactProp(name = "tileCacheMaxAge")
  public void setTileCacheMaxAge(OsmMapUrlTile view, float tileCacheMaxAge) {
    view.setTileCacheMaxAge((long) tileCacheMaxAge);
  }

  @ReactProp(name = "tileSize", defaultInt = 256)
  public void setTileSize(OsmMapUrlTile view, int tileSize) {
    view.setTileSize(tileSize);
  }

  @ReactProp(name = "doubleTileSize", defaultBoolean = false)
  public void setDoubleTileSize(OsmMapUrlTile view, boolean doubleTileSize) {
    view.setDoubleTileSize(doubleTileSize);
  }

  @ReactProp(name = "flipY", defaultBoolean = false)
  public void setFlipY(OsmMapUrlTile view, boolean flipY) {
    view.setFlipY(flipY);
  }

  @ReactProp(name = "opacity", defaultFloat = 1.0f)
  public void setOpacity(OsmMapUrlTile view, float opacity) {
    view.setOpacity(opacity);
  }

}
