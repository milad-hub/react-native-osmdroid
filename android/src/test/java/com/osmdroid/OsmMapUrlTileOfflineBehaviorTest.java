package com.osmdroid;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class OsmMapUrlTileOfflineBehaviorTest {

    private OsmMapUrlTile urlTile;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        urlTile = new OsmMapUrlTile(context);
    }

    @Test
    public void extractBaseDirectory_supportsNewDirectoryStylePath() {
        assertEquals(
                "/storage/emulated/0/MyMaps/tiles",
                urlTile.extractBaseDirectory("file:///storage/emulated/0/MyMaps/tiles")
        );
    }

    @Test
    public void extractBaseDirectory_removesExtensionHints() {
        assertEquals(
                "/storage/maps",
                urlTile.extractBaseDirectory("file:///storage/maps.jpg")
        );
    }

    @Test
    public void extractBaseDirectory_supportsLegacyPlaceholderPath() {
        assertEquals(
                "/storage/maps",
                urlTile.extractBaseDirectory("file:///storage/maps/{z}/{x}/{y}.png")
        );
    }

    @Test
    public void extractFileExtension_defaultsToPng() {
        assertEquals(".png", urlTile.extractFileExtension("file:///storage/maps"));
    }

    @Test
    public void extractFileExtension_supportsJpegWithQueryString() {
        assertEquals(
                ".jpeg",
                urlTile.extractFileExtension("https://tiles.example.com/{z}/{x}/{y}.jpeg?token=abc")
        );
    }

    @Test
    public void extractFileExtension_supportsWebpWithFragment() {
        assertEquals(
                ".webp",
                urlTile.extractFileExtension("file:///storage/tiles.webp#section")
        );
    }

    @Test
    public void resolveOnlineTileCoordinates_keepsOriginalWhenWithinNativeZoom() {
        assertArrayEquals(
                new int[]{12, 1331, 2047},
                urlTile.resolveOnlineTileCoordinates(12, 1331, 2047, 15, false)
        );
    }

    @Test
    public void resolveOnlineTileCoordinates_scalesCoordinatesWhenAboveNativeZoom() {
        assertArrayEquals(
                new int[]{15, 25000, 12500},
                urlTile.resolveOnlineTileCoordinates(18, 200000, 100000, 15, false)
        );
    }

    @Test
    public void resolveOnlineTileCoordinates_flipsYAfterScalingAtEffectiveZoom() {
        assertArrayEquals(
                new int[]{15, 1, 32765},
                urlTile.resolveOnlineTileCoordinates(18, 8, 16, 15, true)
        );
    }

    @Test
    public void resolveOnlineTileCoordinates_flipsYWithoutScalingWhenNotClamped() {
        assertArrayEquals(
                new int[]{3, 4, 5},
                urlTile.resolveOnlineTileCoordinates(3, 4, 2, 15, true)
        );
    }
}
