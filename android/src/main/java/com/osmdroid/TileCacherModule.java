package com.osmdroid;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class TileCacherModule extends ReactContextBaseJavaModule {

    public TileCacherModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "TileCacher";
    }

    @ReactMethod
    public void cacheTilesFromDirectory(String directoryPath) {
        OsmMapTileCacher cacher = new OsmMapTileCacher(getReactApplicationContext());
        cacher.cacheTilesFromDirectory(directoryPath);
    }
}
