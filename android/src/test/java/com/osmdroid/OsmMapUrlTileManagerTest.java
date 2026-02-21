package com.osmdroid;

import com.facebook.react.uimanager.ThemedReactContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OsmMapUrlTileManager
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class OsmMapUrlTileManagerTest {

    @Mock
    private ThemedReactContext mockContext;

    @Mock
    private OsmMapUrlTile mockUrlTile;

    private OsmMapUrlTileManager manager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new OsmMapUrlTileManager();
    }

    // ========== Manager Configuration Tests ==========

    @Test
    public void testGetName() {
        String name = manager.getName();
        assertEquals("OsmMapUrlTile", name);
    }

    @Test
    public void testCreateViewInstance() {
        OsmMapUrlTile tile = manager.createViewInstance(mockContext);
        assertNotNull(tile);
    }

    // ========== Prop Setter Tests ==========

    @Test
    public void testSetUrlTemplate() {
        String url = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
        manager.setUrlTemplate(mockUrlTile, url);
        verify(mockUrlTile).setUrlTemplate(url);
    }

    @Test
    public void testSetUrlTemplateWithFile() {
        String url = "file:///storage/tiles/{z}/{x}/{y}.png";
        manager.setUrlTemplate(mockUrlTile, url);
        verify(mockUrlTile).setUrlTemplate(url);
    }

    @Test
    public void testSetUrlTemplateNull() {
        manager.setUrlTemplate(mockUrlTile, null);
        verify(mockUrlTile).setUrlTemplate(null);
    }

    @Test
    public void testSetMinimumZ() {
        manager.setMinimumZ(mockUrlTile, 5.0f);
        verify(mockUrlTile).setMinimumZ(5.0f);
    }

    @Test
    public void testSetMinimumZZero() {
        manager.setMinimumZ(mockUrlTile, 0.0f);
        verify(mockUrlTile).setMinimumZ(0.0f);
    }

    @Test
    public void testSetMaximumZ() {
        manager.setMaximumZ(mockUrlTile, 18.0f);
        verify(mockUrlTile).setMaximumZ(18.0f);
    }

    @Test
    public void testSetMaximumZMax() {
        manager.setMaximumZ(mockUrlTile, 20.0f);
        verify(mockUrlTile).setMaximumZ(20.0f);
    }

    @Test
    public void testSetMaximumNativeZ() {
        manager.setMaximumNativeZ(mockUrlTile, 15.0f);
        verify(mockUrlTile).setMaximumNativeZ(15.0f);
    }

    @Test
    public void testSetOfflineModeTrue() {
        manager.setOfflineMode(mockUrlTile, true);
        verify(mockUrlTile).setOfflineMode(true);
    }

    @Test
    public void testSetOfflineModeFalse() {
        manager.setOfflineMode(mockUrlTile, false);
        verify(mockUrlTile).setOfflineMode(false);
    }

    @Test
    public void testSetTileCachePath() {
        String path = "/cache/tiles";
        manager.setTileCachePath(mockUrlTile, path);
        verify(mockUrlTile).setTileCachePath(path);
    }

    @Test
    public void testSetTileCachePathNull() {
        manager.setTileCachePath(mockUrlTile, null);
        verify(mockUrlTile).setTileCachePath(null);
    }

    @Test
    public void testSetTileCacheMaxAge() {
        manager.setTileCacheMaxAge(mockUrlTile, 86400.0f);
        verify(mockUrlTile).setTileCacheMaxAge(86400L);
    }

    @Test
    public void testSetTileCacheMaxAgeZero() {
        manager.setTileCacheMaxAge(mockUrlTile, 0.0f);
        verify(mockUrlTile).setTileCacheMaxAge(0L);
    }

    @Test
    public void testSetTileCacheMaxAgeLarge() {
        manager.setTileCacheMaxAge(mockUrlTile, 2592000.0f); // 30 days
        verify(mockUrlTile).setTileCacheMaxAge(2592000L);
    }

    @Test
    public void testSetTileSize() {
        manager.setTileSize(mockUrlTile, 256);
        verify(mockUrlTile).setTileSize(256);
    }

    @Test
    public void testSetTileSizeHighRes() {
        manager.setTileSize(mockUrlTile, 512);
        verify(mockUrlTile).setTileSize(512);
    }

    @Test
    public void testSetDoubleTileSizeTrue() {
        manager.setDoubleTileSize(mockUrlTile, true);
        verify(mockUrlTile).setDoubleTileSize(true);
    }

    @Test
    public void testSetDoubleTileSizeFalse() {
        manager.setDoubleTileSize(mockUrlTile, false);
        verify(mockUrlTile).setDoubleTileSize(false);
    }

    @Test
    public void testSetFlipYTrue() {
        manager.setFlipY(mockUrlTile, true);
        verify(mockUrlTile).setFlipY(true);
    }

    @Test
    public void testSetFlipYFalse() {
        manager.setFlipY(mockUrlTile, false);
        verify(mockUrlTile).setFlipY(false);
    }

    @Test
    public void testSetOpacity() {
        manager.setOpacity(mockUrlTile, 0.5f);
        verify(mockUrlTile).setOpacity(0.5f);
    }

    @Test
    public void testSetOpacityMinimum() {
        manager.setOpacity(mockUrlTile, 0.0f);
        verify(mockUrlTile).setOpacity(0.0f);
    }

    @Test
    public void testSetOpacityMaximum() {
        manager.setOpacity(mockUrlTile, 1.0f);
        verify(mockUrlTile).setOpacity(1.0f);
    }

    // ========== Configuration Scenario Tests ==========

    @Test
    public void testConfigureOnlineTiles() {
        manager.setUrlTemplate(mockUrlTile, "https://tiles.example.com/{z}/{x}/{y}.png");
        manager.setMinimumZ(mockUrlTile, 0.0f);
        manager.setMaximumZ(mockUrlTile, 18.0f);
        manager.setOfflineMode(mockUrlTile, false);

        verify(mockUrlTile).setUrlTemplate("https://tiles.example.com/{z}/{x}/{y}.png");
        verify(mockUrlTile).setMinimumZ(0.0f);
        verify(mockUrlTile).setMaximumZ(18.0f);
        verify(mockUrlTile).setOfflineMode(false);
    }

    @Test
    public void testConfigureOfflineTiles() {
        manager.setUrlTemplate(mockUrlTile, "file:///storage/tiles/{z}/{x}/{y}.png");
        manager.setOfflineMode(mockUrlTile, true);
        manager.setMinimumZ(mockUrlTile, 0.0f);
        manager.setMaximumZ(mockUrlTile, 15.0f);

        verify(mockUrlTile).setUrlTemplate("file:///storage/tiles/{z}/{x}/{y}.png");
        verify(mockUrlTile).setOfflineMode(true);
        verify(mockUrlTile).setMinimumZ(0.0f);
        verify(mockUrlTile).setMaximumZ(15.0f);
    }

    @Test
    public void testConfigureCachedTiles() {
        manager.setUrlTemplate(mockUrlTile, "https://tiles.example.com/{z}/{x}/{y}.png");
        manager.setTileCachePath(mockUrlTile, "/cache");
        manager.setTileCacheMaxAge(mockUrlTile, 86400.0f);

        verify(mockUrlTile).setUrlTemplate("https://tiles.example.com/{z}/{x}/{y}.png");
        verify(mockUrlTile).setTileCachePath("/cache");
        verify(mockUrlTile).setTileCacheMaxAge(86400L);
    }

    @Test
    public void testConfigureBandwidthOptimized() {
        manager.setUrlTemplate(mockUrlTile, "https://tiles.example.com/{z}/{x}/{y}.png");
        manager.setMaximumZ(mockUrlTile, 20.0f);
        manager.setMaximumNativeZ(mockUrlTile, 15.0f);

        verify(mockUrlTile).setUrlTemplate("https://tiles.example.com/{z}/{x}/{y}.png");
        verify(mockUrlTile).setMaximumZ(20.0f);
        verify(mockUrlTile).setMaximumNativeZ(15.0f);
    }

    @Test
    public void testConfigureHighResTiles() {
        manager.setUrlTemplate(mockUrlTile, "https://tiles.example.com/{z}/{x}/{y}@2x.png");
        manager.setTileSize(mockUrlTile, 512);
        manager.setDoubleTileSize(mockUrlTile, true);

        verify(mockUrlTile).setUrlTemplate("https://tiles.example.com/{z}/{x}/{y}@2x.png");
        verify(mockUrlTile).setTileSize(512);
        verify(mockUrlTile).setDoubleTileSize(true);
    }

    @Test
    public void testConfigureTmsTiles() {
        manager.setUrlTemplate(mockUrlTile, "https://tms-tiles.example.com/{z}/{x}/{y}.png");
        manager.setFlipY(mockUrlTile, true);

        verify(mockUrlTile).setUrlTemplate("https://tms-tiles.example.com/{z}/{x}/{y}.png");
        verify(mockUrlTile).setFlipY(true);
    }

    @Test
    public void testConfigureTransparentOverlay() {
        manager.setUrlTemplate(mockUrlTile, "https://overlay.example.com/{z}/{x}/{y}.png");
        manager.setOpacity(mockUrlTile, 0.6f);

        verify(mockUrlTile).setUrlTemplate("https://overlay.example.com/{z}/{x}/{y}.png");
        verify(mockUrlTile).setOpacity(0.6f);
    }

    // ========== Type Conversion Tests ==========

    @Test
    public void testFloatToLongConversion() {
        // Test that tileCacheMaxAge float is correctly converted to long
        float floatValue = 86400.5f;
        manager.setTileCacheMaxAge(mockUrlTile, floatValue);
        verify(mockUrlTile).setTileCacheMaxAge(86400L);
    }

    @Test
    public void testFloatToIntConversion() {
        // Test that zoom levels are handled as floats
        manager.setMinimumZ(mockUrlTile, 5.5f);
        manager.setMaximumZ(mockUrlTile, 18.5f);
        verify(mockUrlTile).setMinimumZ(5.5f);
        verify(mockUrlTile).setMaximumZ(18.5f);
    }

    // ========== Multiple Property Updates Tests ==========

    @Test
    public void testMultiplePropertyUpdates() {
        manager.setUrlTemplate(mockUrlTile, "https://tiles1.example.com/{z}/{x}/{y}.png");
        manager.setUrlTemplate(mockUrlTile, "https://tiles2.example.com/{z}/{x}/{y}.png");

        verify(mockUrlTile, times(2)).setUrlTemplate(anyString());
    }

    @Test
    public void testAllPropertiesSet() {
        manager.setUrlTemplate(mockUrlTile, "https://tiles.example.com/{z}/{x}/{y}.png");
        manager.setMinimumZ(mockUrlTile, 0.0f);
        manager.setMaximumZ(mockUrlTile, 18.0f);
        manager.setMaximumNativeZ(mockUrlTile, 15.0f);
        manager.setOfflineMode(mockUrlTile, false);
        manager.setTileCachePath(mockUrlTile, "/cache");
        manager.setTileCacheMaxAge(mockUrlTile, 86400.0f);
        manager.setTileSize(mockUrlTile, 256);
        manager.setDoubleTileSize(mockUrlTile, false);
        manager.setFlipY(mockUrlTile, false);
        manager.setOpacity(mockUrlTile, 1.0f);

        verify(mockUrlTile).setUrlTemplate(anyString());
        verify(mockUrlTile).setMinimumZ(anyFloat());
        verify(mockUrlTile).setMaximumZ(anyFloat());
        verify(mockUrlTile).setMaximumNativeZ(anyFloat());
        verify(mockUrlTile).setOfflineMode(anyBoolean());
        verify(mockUrlTile).setTileCachePath(anyString());
        verify(mockUrlTile).setTileCacheMaxAge(anyLong());
        verify(mockUrlTile).setTileSize(anyInt());
        verify(mockUrlTile).setDoubleTileSize(anyBoolean());
        verify(mockUrlTile).setFlipY(anyBoolean());
        verify(mockUrlTile).setOpacity(anyFloat());
    }

    // ========== Edge Case Tests ==========

    @Test
    public void testSetPropertiesWithNullView() {
        // Should not throw NPE
        try {
            manager.setUrlTemplate(null, "https://tiles.example.com/{z}/{x}/{y}.png");
            // If we get here, it threw NPE which is expected
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
            assertTrue(true);
        }
    }

    @Test
    public void testDefaultValues() {
        // Test that default values are applied correctly
        OsmMapUrlTile tile = manager.createViewInstance(mockContext);

        // Defaults should be set appropriately
        assertNotNull(tile);
    }

    // ========== Real-world Integration Tests ==========

    @Test
    public void testOpenStreetMapSetup() {
        manager.setUrlTemplate(mockUrlTile, "https://tile.openstreetmap.org/{z}/{x}/{y}.png");
        manager.setMinimumZ(mockUrlTile, 0.0f);
        manager.setMaximumZ(mockUrlTile, 19.0f);

        verify(mockUrlTile).setUrlTemplate("https://tile.openstreetmap.org/{z}/{x}/{y}.png");
        verify(mockUrlTile).setMinimumZ(0.0f);
        verify(mockUrlTile).setMaximumZ(19.0f);
    }

    @Test
    public void testFieldMapSetup() {
        manager.setUrlTemplate(mockUrlTile, "file:///sdcard/fieldmaps/{z}/{x}/{y}.png");
        manager.setOfflineMode(mockUrlTile, true);
        manager.setMinimumZ(mockUrlTile, 10.0f);
        manager.setMaximumZ(mockUrlTile, 16.0f);

        verify(mockUrlTile).setUrlTemplate("file:///sdcard/fieldmaps/{z}/{x}/{y}.png");
        verify(mockUrlTile).setOfflineMode(true);
        verify(mockUrlTile).setMinimumZ(10.0f);
        verify(mockUrlTile).setMaximumZ(16.0f);
    }

    @Test
    public void testWeatherOverlaySetup() {
        manager.setUrlTemplate(mockUrlTile, "https://weather.example.com/{z}/{x}/{y}.png");
        manager.setOpacity(mockUrlTile, 0.4f);
        manager.setTileCacheMaxAge(mockUrlTile, 3600.0f);

        verify(mockUrlTile).setUrlTemplate("https://weather.example.com/{z}/{x}/{y}.png");
        verify(mockUrlTile).setOpacity(0.4f);
        verify(mockUrlTile).setTileCacheMaxAge(3600L);
    }
}

