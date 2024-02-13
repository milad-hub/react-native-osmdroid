package com.osmdroid;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.osmdroid.OsmMapTileCacher;

public class TileCacherModule extends ReactContextBaseJavaModule {

    private final OsmMapTileCacher cacher;

    public TileCacherModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.cacher = new OsmMapTileCacher(reactContext);
    }

    @Override
    public String getName() {
        return "TileCacher";
    }

    @ReactMethod
    public void cacheTilesFromDirectory(String directoryPath) {
        cacher.cacheTilesFromDirectory(directoryPath);
    }
}