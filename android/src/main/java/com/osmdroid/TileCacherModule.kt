package com.osmdroid;

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap

class TileCacherModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "TileCacher"
    }

    @ReactMethod
    fun cacheTilesFromDirectory(directoryPath: String, options: ReadableMap?) {
        val showProgressToast = options?.getBoolean("showProgressToast") ?: false
        OsmMapTileCacher(reactApplicationContext).cacheTilesFromDirectory(directoryPath, showProgressToast)
    }
}