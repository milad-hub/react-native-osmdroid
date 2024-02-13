package com.osmdroid;

import android.preference.PreferenceManager;
import android.view.View;

import com.osmdroid.utils.SizeReportingShadowNode;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.Map;

import javax.annotation.Nullable;

public class OsmMapManager extends ViewGroupManager<OsmMapView> {

  private static final String REACT_CLASS = "OsmMap";
  private static final int ANIMATE_TO_REGION = 1;
  private static final int ANIMATE_TO_COORDINATE = 2;
  private static final int ANIMATE_TO_VIEWING_ANGLE = 3;
  private static final int ANIMATE_TO_BEARING = 4;
  private static final int FIT_TO_ELEMENTS = 5;
  private static final int FIT_TO_SUPPLIED_MARKERS = 6;
  private static final int FIT_TO_COORDINATES = 7;

  private static final int SET_CAMERA = 11;
  private static final int ANIMATE_CAMERA = 12;

  private final ReactApplicationContext appContext;

  public OsmMapManager(ReactApplicationContext context) {
    this.appContext = context;

    final String packageName = context.getApplicationContext().getApplicationInfo().packageName;
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
    Configuration.getInstance().setUserAgentValue(packageName);
    Configuration.getInstance().setTileDownloadThreads((short) 12);
  }

  void invalidateNode(final OsmMapView view) {
    // to force refresh when adding/updating a callout
    appContext.runOnNativeModulesQueueThread(new Runnable() {
      @Override
      public void run() {
        // TODO: very hacky way of forcing layout, discover how to replace (or avoid) this.
        int width = view.getWidth();
        int height = view.getHeight();
        UIManagerModule uiManagerModule = appContext.getNativeModule(UIManagerModule.class);
        uiManagerModule.updateNodeSize(view.getId(), width, height + 1);
        uiManagerModule.updateNodeSize(view.getId(), width, height);
      }
    });
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  protected OsmMapView createViewInstance(ThemedReactContext context) {
    OsmMapView view = new OsmMapView(context, this.appContext, this);
    view.setZoomEnabled(true);
    view.setRotateEnabled(true);
    view.setScrollEnabled(true);
    return view;
  }

  private void emitMapError(ThemedReactContext context, String message, String type) {
    WritableMap error = Arguments.createMap();
    error.putString("message", message);
    error.putString("type", type);

    context
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("onError", error);
  }

  @ReactProp(name = "region")
  public void setRegion(OsmMapView view, ReadableMap region) {
    view.setRegion(region);
  }

  @ReactProp(name = "initialRegion")
  public void setInitialRegion(OsmMapView view, ReadableMap initialRegion) {
    view.setInitialRegion(initialRegion);
  }

  @ReactProp(name = "tileDownloadThreads", defaultFloat = 12)
  public void setTileDownloadThreads(OsmMapView view, float tileDownloadThread) {
    Configuration.getInstance().setTileDownloadThreads((short) tileDownloadThread);
  }

  // This is a private prop to improve performance of panDrag by disabling it when the callback
  // is not set
  @ReactProp(name = "handlePanDrag", defaultBoolean = false)
  public void setHandlePanDrag(OsmMapView view, boolean handlePanDrag) {
    view.setHandlePanDrag(handlePanDrag);
  }

  @ReactProp(name = "scrollEnabled", defaultBoolean = false)
  public void setScrollEnabled(OsmMapView view, boolean scrollEnabled) {
    view.setScrollEnabled(scrollEnabled);
  }

  @ReactProp(name = "zoomEnabled", defaultBoolean = false)
  public void setZoomEnabled(OsmMapView view, boolean zoomEnabled) {
    view.setZoomEnabled(zoomEnabled);
  }

  @ReactProp(name = "rotateEnabled", defaultBoolean = false)
  public void setRotateEnabled(OsmMapView view, boolean rotateEnabled) {
    view.setRotateEnabled(rotateEnabled);
  }

  @ReactProp(name = "moveOnMarkerPress", defaultBoolean = false)
  public void setMoveOnMarkerPress(OsmMapView view, boolean moveOnPress) {
    view.setMoveOnMarkerPress(moveOnPress);
  }

  @ReactProp(name = "minZoomLevel")
  public void setMinZoomLevel(OsmMapView view, float minZoomLevel) {
    view.setMinZoomLevel((double) minZoomLevel);
  }

  @ReactProp(name = "maxZoomLevel")
  public void setMaxZoomLevel(OsmMapView view, float maxZoomLevel) {
    view.setMaxZoomLevel((double) maxZoomLevel);
  }


  @Override
  public void receiveCommand(OsmMapView view, String commandId, @Nullable ReadableArray args) {
    Integer duration;
    Double lat;
    Double lng;
    Double lngDelta;
    Double latDelta;
    float bearing;
    ReadableMap region;
    ReadableMap camera;

    switch (commandId) {
      case "setCamera":
        camera = args.getMap(0);
        view.animateToCamera(camera, 0);
        break;

      case "animateCamera":
        camera = args.getMap(0);
        duration = args.getInt(1);
        view.animateToCamera(camera, duration);
        break;

      case "animateToRegion":
        region = args.getMap(0);
        duration = args.getInt(1);
        lng = region.getDouble("longitude");
        lat = region.getDouble("latitude");
        lngDelta = region.getDouble("longitudeDelta");
        latDelta = region.getDouble("latitudeDelta");
        BoundingBox bounds = new BoundingBox(
          lat + latDelta / 2, lng + lngDelta / 2, // northeast
          lat - latDelta / 2, lng - lngDelta / 2 // southwest
        );
        view.animateToRegion(bounds, duration);
        break;

      case "animateToCoordinate":
        region = args.getMap(0);
        duration = args.getInt(1);
        lng = region.getDouble("longitude");
        lat = region.getDouble("latitude");
        view.animateToCoordinate(new GeoPoint(lat, lng), duration);
        break;


      case "animateToBearing":
        bearing = (float) args.getDouble(0);
        duration = args.getInt(1);
        view.animateToBearing(bearing, duration);
        break;

      case "fitToElements":
        view.fitToElements(args.getBoolean(0));
        break;

      case "fitToSuppliedMarkers":
        view.fitToSuppliedMarkers(args.getArray(0), args.getMap(1), args.getBoolean(2));
        break;
      case "fitToCoordinates":
        view.fitToCoordinates(args.getArray(0), args.getMap(1), args.getBoolean(2));
        break;
    }
  }

  @Override
  @Nullable
  public Map getExportedCustomDirectEventTypeConstants() {
    Map<String, Map<String, String>> map = MapBuilder.of(
      "onMapReady", MapBuilder.of("registrationName", "onMapReady"),
      "onPress", MapBuilder.of("registrationName", "onPress"),
      "onLongPress", MapBuilder.of("registrationName", "onLongPress"),
      "onMarkerPress", MapBuilder.of("registrationName", "onMarkerPress"),
      "onMarkerSelect", MapBuilder.of("registrationName", "onMarkerSelect"),
      "onMarkerDeselect", MapBuilder.of("registrationName", "onMarkerDeselect"),
      "onCalloutPress", MapBuilder.of("registrationName", "onCalloutPress")
    );

    map.putAll(MapBuilder.of(
      "onMarkerDragStart", MapBuilder.of("registrationName", "onMarkerDragStart"),
      "onMarkerDrag", MapBuilder.of("registrationName", "onMarkerDrag"),
      "onMarkerDragEnd", MapBuilder.of("registrationName", "onMarkerDragEnd"),
      "onPanDrag", MapBuilder.of("registrationName", "onPanDrag")
    ));

    return map;
  }

  @Override
  @Nullable
  public Map<String, Integer> getCommandsMap() {
    Map<String, Integer> map = MapBuilder.of(
      "setCamera", SET_CAMERA,
      "animateCamera", ANIMATE_CAMERA
    );
    map.putAll(MapBuilder.of(
      "animateToRegion", ANIMATE_TO_REGION,
      "animateToCoordinate", ANIMATE_TO_COORDINATE,
      "animateToViewingAngle", ANIMATE_TO_VIEWING_ANGLE,
      "animateToBearing", ANIMATE_TO_BEARING,
      "fitToElements", FIT_TO_ELEMENTS,
      "fitToSuppliedMarkers", FIT_TO_SUPPLIED_MARKERS,
      "fitToCoordinates", FIT_TO_COORDINATES
    ));
    return map;
  }

  @Override
  public LayoutShadowNode createShadowNodeInstance() {
    // A custom shadow node is needed in order to pass back the width/height of the map to the
    // view manager so that it can start applying camera moves with bounds.
    return new SizeReportingShadowNode();
  }

  @Override
  public void addView(OsmMapView parent, View child, int index) {
    parent.addFeature(child, index);
  }

  @Override
  public int getChildCount(OsmMapView view) {
    return view.getFeatureCount();
  }

  @Override
  public View getChildAt(OsmMapView view, int index) {
    return view.getFeatureAt(index);
  }

  @Override
  public void removeViewAt(OsmMapView parent, int index) {
    parent.removeFeatureAt(index);
  }

  @Override
  public void updateExtraData(OsmMapView view, Object extraData) {
    view.updateExtraData(extraData);
  }

  void pushEvent(ThemedReactContext context, View view, String name, WritableMap data) {
    context.getJSModule(RCTEventEmitter.class)
      .receiveEvent(view.getId(), name, data);
  }

  @Override
  public void onDropViewInstance(OsmMapView view) {
    super.onDropViewInstance(view);
    view.doDestroy();
  }

}
