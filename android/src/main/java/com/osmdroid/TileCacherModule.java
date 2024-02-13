package com.osmdroid;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class TileCacherModule extends ReactContextBaseJavaModule {

    private final TileDatabaseHelper cacher;

    public TileCacherModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.cacher = new TileDatabaseHelper(reactContext);
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