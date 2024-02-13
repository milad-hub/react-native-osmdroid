package com.osmdroid;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.osmdroid.overlays.InterceptDoubleTapOverlay;
import com.osmdroid.overlays.InterceptScrollOverlay;
import com.osmdroid.utils.LatLngBoundsUtils;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class OsmMapView extends MapView implements MapView.OnFirstLayoutListener {

    private static final int DEFAULT_PADDING = 50;

    private BoundingBox boundsToMove;
    private boolean isMonitoringRegion = false;
    private boolean isTouchDown = false;
    private boolean handlePanDrag = false;
    private boolean moveOnMarkerPress = false;
    private boolean initialRegionSet = false;

    private final List<OsmMapFeature> features = new ArrayList<>();
    private final Map<Marker, OsmMapMarker> markerMap = new HashMap<>();
    private final Map<Polyline, OsmMapPolyline> polylineMap = new HashMap<>();
    private final Map<Polygon, OsmMapPolygon> polygonMap = new HashMap<>();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetectorCompat gestureDetector;
    private final OsmMapManager manager;
    private final ThemedReactContext context;
    private final EventDispatcher eventDispatcher;

    public OsmMapView(ThemedReactContext reactContext,
                      ReactApplicationContext appContext,
                      OsmMapManager manager) {
        super(reactContext.getApplicationContext());
        this.manager = manager;
        this.context = reactContext;
        this.addOnFirstLayoutListener(this);
        this.getController().setZoom(10.0);

        final OsmMapView view = this;
        scaleDetector =
                new ScaleGestureDetector(reactContext,
                        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public boolean onScaleBegin(ScaleGestureDetector detector) {
                                view.startMonitoringRegion();
                                return true; // stop recording this gesture. let mapview handle it.
                            }
                        });

        gestureDetector =
                new GestureDetectorCompat(reactContext, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        view.startMonitoringRegion();
                        return false;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                            float distanceY) {
                        if (handlePanDrag) {
                            onPanDrag(e2);
                        }
                        view.startMonitoringRegion();
                        return false;
                    }
                });

        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        this.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        this.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
    }

    /*
     * Keep a cache of map listeners because the original list is destroyed
     * when view detached from window.
     */
    protected List<MapListener> mListnersCache = new ArrayList<>();

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        mListners.addAll(mListnersCache);
        mListnersCache.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        mListnersCache.addAll(mListners);
        mListners.clear();

        super.onDetachedFromWindow();
    }

    boolean m_isDestroying = false;

    public void doDestroy() {
        m_isDestroying = true;
        onDetach();
    }

    @Override
    public void onDetach() {
        if (m_isDestroying) {
            new Exception("not an exception").printStackTrace();
            super.onDetach();
        }
    }

    @Override
    public void onFirstLayout(View view, int i, int i1, int i2, int i3) {
        manager.pushEvent(context, this, "onMapReady", new WritableNativeMap());

        this.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                OsmMapView mapView = OsmMapView.this;
                BoundingBox bounds = mapView.getBoundingBox();
                IGeoPoint center = mapView.getMapCenter();
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new OsmRegionChangeEvent(getId(), bounds, center, isTouchDown));
                mapView.stopMonitoringRegion();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                OsmMapView mapView = OsmMapView.this;
                BoundingBox bounds = mapView.getBoundingBox();
                IGeoPoint center = mapView.getMapCenter();
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new OsmRegionChangeEvent(getId(), bounds, center, isTouchDown));
                mapView.stopMonitoringRegion();
                return true;
            }
        }, 100));

        MapEventsOverlay OverlayEvents = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                WritableMap event = makeClickEventData(p);
                event.putString("action", "press");
                manager.pushEvent(context, OsmMapView.this, "onPress", event);
                InfoWindow.closeAllInfoWindowsOn(OsmMapView.this);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                WritableMap event = makeClickEventData(p);
                event.putString("action", "long-press");
                manager.pushEvent(context, OsmMapView.this, "onLongPress", event);
                return false;
            }
        });
        this.getOverlays().add(OverlayEvents);

        // listen for polygon click
        this.getOverlays().add(new Overlay() {
            @Override
            public void draw(Canvas canvas, MapView mapView, boolean b) {
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                for (Polygon polygon : polygonMap.keySet()) {
                    if (polygon.contains(e)) {
                        WritableMap event = makeClickEventData(polygon.getPoints().get(0));
                        event.putString("action", "polygon-press");
                        manager.pushEvent(context, polygonMap.get(polygon), "onPress", event);
                        return true;
                    }
                }
                return super.onSingleTapConfirmed(e, mapView);
            }
        });
    }

    public void setInitialRegion(ReadableMap initialRegion) {
        if (!initialRegionSet && initialRegion != null) {
            setRegion(initialRegion);
            initialRegionSet = true;
        }
    }

    public void setRegion(ReadableMap region) {
        if (region == null) return;

        Double lng = region.getDouble("longitude");
        Double lat = region.getDouble("latitude");
        Double lngDelta = region.getDouble("longitudeDelta");
        Double latDelta = region.getDouble("latitudeDelta");
        BoundingBox bounds = new BoundingBox(
                lat + latDelta / 2, lng + lngDelta / 2,
                lat - latDelta / 2, lng - lngDelta / 2
        );
        if (super.getHeight() <= 0 || super.getWidth() <= 0) {
            // in this case, our map has not been laid out yet, so we save the bounds in a local
            // variable, and make a guess of zoomLevel 10. Not to worry, though: as soon as layout
            // occurs, we will move the camera to the saved bounds. Note that if we tried to move
            // to the bounds now, it would trigger an exception.
            this.getController().setZoom(10.0f);
            this.getController().setCenter(bounds.getCenterWithDateLine());
            boundsToMove = bounds;
        } else {
            int width = getWidth();
            int height = getHeight();
            double zoom = getTileSystem().getBoundingBoxZoom(bounds, width, height);
            this.getController().setZoom(zoom);
            this.getController().setCenter(bounds.getCenterWithDateLine());
            boundsToMove = null;
        }
    }

    public void setMoveOnMarkerPress(boolean moveOnPress) {
        this.moveOnMarkerPress = moveOnPress;
    }

    public void setHandlePanDrag(boolean handlePanDrag) {
        this.handlePanDrag = handlePanDrag;
    }

    public void addFeature(View child, int index) {
        // Our desired API is to pass up annotations/overlays as children to the mapview component.
        // This is where we intercept them and do the appropriate underlying mapview action.
        if (child instanceof OsmMapMarker) {
            OsmMapMarker annotation = (OsmMapMarker) child;
            annotation.addToMap(this);
            annotation.setOnCalloutPressListener(onCalloutPressListener);
            features.add(index, annotation);
            Marker marker = (Marker) annotation.getFeature();
            markerMap.put(marker, annotation);
            marker.setOnMarkerClickListener(onMarkerClickListener);
            marker.setOnMarkerDragListener(onMarkerDragListener);
        } else if (child instanceof OsmMapPolyline) {
            OsmMapPolyline polylineView = (OsmMapPolyline) child;
            polylineView.addToMap(this);
            features.add(index, polylineView);
            Polyline polyline = (Polyline) polylineView.getFeature();
            polylineMap.put(polyline, polylineView);
            polyline.setOnClickListener(onPolylineClickListener);
        } else if (child instanceof OsmMapPolygon) {
            OsmMapPolygon polygonView = (OsmMapPolygon) child;
            polygonView.addToMap(this);
            features.add(index, polygonView);
            Polygon polygon = (Polygon) polygonView.getFeature();
            polygonMap.put(polygon, polygonView);
            polygon.setOnClickListener(onPolygonClickListener);
        } else if (child instanceof OsmMapUrlTile) {
            OsmMapUrlTile urlTileView = (OsmMapUrlTile) child;
            urlTileView.addToMap(this);
            features.add(index, urlTileView);
        } else if (child instanceof OsmMapFileTile) {
            OsmMapFileTile filetileView = (OsmMapFileTile) child;
            filetileView.addToMap(this);
            features.add(index, filetileView);
        } else if (child instanceof OsmMapCircle) {
            OsmMapCircle circleView = (OsmMapCircle) child;
            circleView.addToMap(this);
            features.add(index, circleView);
        } /* else {
            ViewGroup children = (ViewGroup) child;
            for (int i = 0; i < children.getChildCount(); i++) {
                addFeature(children.getChildAt(i), index);
            }
        }*/
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        manager.invalidateNode(this);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        manager.invalidateNode(this);
    }

    public int getFeatureCount() {
        return features.size();
    }

    public View getFeatureAt(int index) {
        return features.get(index);
    }

    public void removeFeatureAt(int index) {
        OsmMapFeature feature = features.remove(index);
        if (feature instanceof OsmMapMarker) {
            //noinspection SuspiciousMethodCalls
            markerMap.remove(feature.getFeature());
        }
        feature.removeFromMap(this);
    }

    public WritableMap makeClickEventData(IGeoPoint point) {
        WritableMap event = new WritableNativeMap();

        WritableMap coordinate = new WritableNativeMap();
        coordinate.putDouble("latitude", point.getLatitude());
        coordinate.putDouble("longitude", point.getLongitude());
        event.putMap("coordinate", coordinate);

        Projection projection = this.getProjection();
        Point screenPoint = projection.toPixels(point, null);

        WritableMap position = new WritableNativeMap();
        position.putDouble("x", screenPoint.x);
        position.putDouble("y", screenPoint.y);
        event.putMap("position", position);

        return event;
    }

    public void animateToCamera(ReadableMap camera, int duration) {
        double zoom = getZoomLevelDouble();
        if (camera.hasKey("zoom")) {
            zoom = (float)camera.getDouble("zoom");
        }
        Float bearing = getMapOrientation();
        if (camera.hasKey("heading")) {
            bearing = (float)camera.getDouble("heading");
        }
        // not supported!
        // if (camera.hasKey("pitch")) {
        //     builder.tilt((float)camera.getDouble("pitch"));
        // }
        IGeoPoint center = getMapCenter();
        if (camera.hasKey("center")) {
            ReadableMap latLng = camera.getMap("center");
            Double lat = latLng.getDouble("latitude");
            Double lng = latLng.getDouble("longitude");
            center = new GeoPoint(lat, lng);
        }

        if (duration > 0) {
            this.getController().animateTo(center, zoom, (long) duration, bearing);
        } else {
            this.getController().setZoom(zoom);
            this.getController().setCenter(center);
            this.setMapOrientation(bearing);
        }
    }

    public void updateExtraData(Object extraData) {
        // if boundsToMove is not null, we now have the MapView's width/height, so we can apply
        // a proper camera move
        if (boundsToMove != null) {
            @SuppressWarnings("unchecked")
            HashMap<String, Float> data = (HashMap<String, Float>) extraData;
            int width = data.get("width") == null ? 0 : data.get("width").intValue();
            int height = data.get("height") == null ? 0 : data.get("height").intValue();

            //fix for https://github.com/airbnb/react-native-maps/issues/245,
            //it's not guaranteed the passed-in height and width would be greater than 0.
            if (width > 0 && height > 0) {
                double zoom = getTileSystem().getBoundingBoxZoom(boundsToMove, width, height);
                this.getController().setZoom(zoom);
                this.getController().setCenter(boundsToMove.getCenterWithDateLine());
                boundsToMove = null;
            }
        }
    }

    public void animateToRegion(BoundingBox bounds, int duration) {
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            startMonitoringRegion();

            this.zoomToBoundingBox(bounds, true, 0, getMaxZoomLevel(), (long) duration);
        }
    }

    public void animateToBearing(float bearing, int duration) {
        startMonitoringRegion();
        this.getController().animateTo(getMapCenter(),getZoomLevelDouble(), (long) duration, bearing);
    }

    public void animateToCoordinate(IGeoPoint coordinate, int duration) {
        startMonitoringRegion();
        this.getController().animateTo(coordinate, getZoomLevelDouble(), (long) duration);
    }

    private void fitBoundingBox(BoundingBox bounds, int padding, boolean animated) {
        if (animated) {
            startMonitoringRegion();
        }
        this.zoomToBoundingBox(bounds, animated, padding, getMaxZoomLevel(), null);
    }

    public void fitToElements(boolean animated) {

        List<GeoPoint> points = new ArrayList<>();
        for (OsmMapFeature feature : features) {
            if (feature instanceof OsmMapMarker) {
                Marker marker = (Marker) feature.getFeature();
                points.add(marker.getPosition());
            }
            // TODO(lmr): may want to include shapes / etc.
        }
        if (points.size() > 0) {
            BoundingBox bounds = BoundingBox.fromGeoPoints(points);
            fitBoundingBox(bounds, DEFAULT_PADDING, animated);
        }
    }

    public void fitToSuppliedMarkers(ReadableArray markerIDsArray,
                                     ReadableMap edgePadding,
                                     boolean animated) {
        String[] markerIDs = new String[markerIDsArray.size()];
        for (int i = 0; i < markerIDsArray.size(); i++) {
            markerIDs[i] = markerIDsArray.getString(i);
        }

        List<String> markerIDList = Arrays.asList(markerIDs);

        List<GeoPoint> points = new ArrayList<>();
        for (OsmMapFeature feature : features) {
            if (feature instanceof OsmMapMarker) {
                String identifier = ((OsmMapMarker) feature).getIdentifier();
                Marker marker = (Marker) feature.getFeature();
                if (markerIDList.contains(identifier)) {
                    points.add(marker.getPosition());
                }
            }
        }

        if (points.size() > 0) {
            int padding = DEFAULT_PADDING;
            if (edgePadding != null) {
                int left = edgePadding.getInt("left");
                int top = edgePadding.getInt("top");
                int right = edgePadding.getInt("right");
                int bottom = edgePadding.getInt("bottom");
                padding = (left + top + right + bottom)/4;
            }

            BoundingBox bounds = BoundingBox.fromGeoPoints(points);
            fitBoundingBox(bounds, padding, animated);
        }
    }

    public void fitToCoordinates(ReadableArray coordinatesArray, ReadableMap edgePadding,
                                 boolean animated) {

        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i < coordinatesArray.size(); i++) {
            ReadableMap latLng = coordinatesArray.getMap(i);
            Double lat = latLng.getDouble("latitude");
            Double lng = latLng.getDouble("longitude");
            points.add(new GeoPoint(lat, lng));
        }

        if (points.size() > 0) {
            int padding = DEFAULT_PADDING;
            if (edgePadding != null) {
                int left = edgePadding.getInt("left");
                int top = edgePadding.getInt("top");
                int right = edgePadding.getInt("right");
                int bottom = edgePadding.getInt("bottom");
                padding = (left + top + right + bottom)/4;
            }

            BoundingBox bounds = BoundingBox.fromGeoPoints(points);
            fitBoundingBox(bounds, padding, animated);
        }
    }

  public double[][] getMapBoundaries() {
    OsmMapView mapView = OsmMapView.this;
    BoundingBox bounds = mapView.getBoundingBox();

    return new double[][] {
      {bounds.getLonEast(), bounds.getLatNorth()},
      {bounds.getLonWest(), bounds.getLatSouth()}
    };
  }
  public IGeoPoint getCameraPosition() {
    OsmMapView mapView = OsmMapView.this;
    IGeoPoint pos = mapView.getMapCenter();
    return pos;
  }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);

        int action = ev.getActionMasked();

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                this.getParent().requestDisallowInterceptTouchEvent(true);
                isTouchDown = true;
                break;
            case (MotionEvent.ACTION_MOVE):
                startMonitoringRegion();
                break;
            case (MotionEvent.ACTION_UP):
                // Clear this regardless, since isScrollGesturesEnabled() may have been updated
                this.getParent().requestDisallowInterceptTouchEvent(false);
                isTouchDown = false;
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    // Timer Implementation

    public void startMonitoringRegion() {
        if (isMonitoringRegion) return;
        timerHandler.postDelayed(timerRunnable, 100);
        isMonitoringRegion = true;
    }

    public void stopMonitoringRegion() {
        if (!isMonitoringRegion) return;
        timerHandler.removeCallbacks(timerRunnable);
        isMonitoringRegion = false;
    }

    private BoundingBox lastBoundsEmitted;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            if (OsmMapView.this.isLayoutOccurred()) {
                BoundingBox bounds = OsmMapView.this.getBoundingBox();

                if ((bounds != null) &&
                        (lastBoundsEmitted == null ||
                                LatLngBoundsUtils.BoundsAreDifferent(bounds, lastBoundsEmitted))) {
                    IGeoPoint center = OsmMapView.this.getMapCenter();
                    lastBoundsEmitted = bounds;
                    eventDispatcher.dispatchEvent(new OsmRegionChangeEvent(getId(), bounds, center, true));
                }
            }

            timerHandler.postDelayed(this, 100);
        }
    };

    private OsmMapMarker.OnCalloutPressListener onCalloutPressListener = new OsmMapMarker.OnCalloutPressListener() {
        @Override
        public void OnCalloutPress(OsmMapMarker markerView) {
            Marker marker = (Marker) markerView.getFeature();

            WritableMap event;
            event = makeClickEventData(marker.getPosition());
            event.putString("action", "callout-press");
            manager.pushEvent(context, OsmMapView.this, "onCalloutPress", event);

            event = makeClickEventData(marker.getPosition());
            event.putString("action", "callout-press");
            manager.pushEvent(context, markerView, "onCalloutPress", event);

            event = makeClickEventData(marker.getPosition());
            event.putString("action", "callout-press");
            OsmMapCallout infoWindow = markerView.getCalloutView();
            if (infoWindow != null) manager.pushEvent(context, infoWindow, "onPress", event);
        }
    };

    private Marker.OnMarkerClickListener onMarkerClickListener = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            WritableMap event;
            OsmMapMarker airMapMarker = markerMap.get(marker);

            event = makeClickEventData(marker.getPosition());
            event.putString("action", "marker-press");
            event.putString("id", airMapMarker.getIdentifier());
            manager.pushEvent(context, OsmMapView.this, "onMarkerPress", event);

            event = makeClickEventData(marker.getPosition());
            event.putString("action", "marker-press");
            event.putString("id", airMapMarker.getIdentifier());
            manager.pushEvent(context, markerMap.get(marker), "onPress", event);

            marker.showInfoWindow();
            if (moveOnMarkerPress) {
                mapView.getController().animateTo(marker.getPosition());
            }

            return true;
        }
    };

    private Marker.OnMarkerDragListener onMarkerDragListener = new Marker.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
            WritableMap event = makeClickEventData(marker.getPosition());
            manager.pushEvent(context, OsmMapView.this, "onMarkerDragStart", event);

            OsmMapMarker markerView = markerMap.get(marker);
            event = makeClickEventData(marker.getPosition());
            manager.pushEvent(context, markerView, "onDragStart", event);
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            WritableMap event = makeClickEventData(marker.getPosition());
            manager.pushEvent(context, OsmMapView.this, "onMarkerDrag", event);

            OsmMapMarker markerView = markerMap.get(marker);
            event = makeClickEventData(marker.getPosition());
            manager.pushEvent(context, markerView, "onDrag", event);
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            WritableMap event = makeClickEventData(marker.getPosition());
            manager.pushEvent(context, OsmMapView.this, "onMarkerDragEnd", event);

            OsmMapMarker markerView = markerMap.get(marker);
            event = makeClickEventData(marker.getPosition());
            manager.pushEvent(context, markerView, "onDragEnd", event);
        }
    };

    private Polyline.OnClickListener onPolylineClickListener = new Polyline.OnClickListener() {
        @Override
        public boolean onClick(Polyline polyline, MapView mapView, GeoPoint geoPoint) {
            WritableMap event = makeClickEventData(polyline.getPoints().get(0));
            event.putString("action", "polyline-press");
            manager.pushEvent(context, polylineMap.get(polyline), "onPress", event);
            return true;
        }
    };

    private Polygon.OnClickListener onPolygonClickListener = new Polygon.OnClickListener() {
        @Override
        public boolean onClick(Polygon polygon, MapView mapView, GeoPoint geoPoint) {
            WritableMap event = makeClickEventData(polygon.getPoints().get(0));
            event.putString("action", "polygon-press");
            manager.pushEvent(context, polygonMap.get(polygon), "onPress", event);
            return true;
        }
    };

    public void onPanDrag(MotionEvent ev) {
        IGeoPoint coords = this.getProjection().fromPixels((int) ev.getX(), (int) ev.getY());
        WritableMap event = makeClickEventData(coords);
        manager.pushEvent(context, this, "onPanDrag", event);
    }

    private InterceptDoubleTapOverlay interceptDoubleTapOverlay;
    private InterceptScrollOverlay interceptScrollOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;

    public void setScrollEnabled(boolean scrollEnabled) {
        if (interceptScrollOverlay == null) {
            interceptScrollOverlay = new InterceptScrollOverlay();
            getOverlayManager().add(interceptScrollOverlay);
        }
        interceptScrollOverlay.setEnabled(!scrollEnabled);
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        if (interceptDoubleTapOverlay == null) {
            interceptDoubleTapOverlay = new InterceptDoubleTapOverlay();
            getOverlayManager().add(interceptDoubleTapOverlay);
        }
        interceptDoubleTapOverlay.setEnabled(!zoomEnabled);
        setMultiTouchControls(zoomEnabled);
    }

    public void setRotateEnabled(boolean rotateEnabled) {
        if (mRotationGestureOverlay == null) {
            mRotationGestureOverlay = new RotationGestureOverlay(this);
            getOverlays().add(this.mRotationGestureOverlay);
        }
        mRotationGestureOverlay.setEnabled(rotateEnabled);
    }
}
