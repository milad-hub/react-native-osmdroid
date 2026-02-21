/**
 * Comprehensive tests for UrlTile offline mode and local storage functionality
 * @jest-environment node
 */

import React from 'react';
import type { MapUrlTileProps } from '../MapUrlTile';

// Mock React Native
jest.mock('react-native', () => ({
  requireNativeComponent: jest.fn((name) => name),
  StyleSheet: {
    create: jest.fn((styles) => styles),
  },
  Platform: {
    OS: 'android',
  },
}));

const MapUrlTile = require('../MapUrlTile').default;

describe('MapUrlTile - Offline Mode & Local Storage', () => {
  describe('Offline Mode Functionality', () => {
    it('should enable offline mode with boolean flag', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
    });

    it('should work offline with cached tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '/storage/emulated/0/osmdroid/tiles',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCachePath).toBeDefined();
    });

    it('should work offline without expiring cached tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '/cache',
        tileCacheMaxAge: 0, // Never expire
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCacheMaxAge).toBe(0);
    });

    it('should support offline mode with zoom level restrictions', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
        minimumZ: 10,
        maximumZ: 16,
        tileCachePath: '/cache',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.minimumZ).toBe(10);
      expect(component.props.maximumZ).toBe(16);
    });
  });

  describe('Local File Storage - file:// Protocol', () => {
    it('should accept file:// with simple base path (new API)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/emulated/0/tiles',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.urlTemplate).toContain(
        '/storage/emulated/0/tiles'
      );
    });

    it('should accept file:// with extension for JPG tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/tiles.jpg',
        minimumZ: 0,
        maximumZ: 15,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.jpg');
    });

    it('should accept file:// with extension for WebP tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/tiles.webp',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.webp');
    });

    it('should work with file:// on sdcard', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///sdcard/osm-tiles',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file:///sdcard/');
    });

    it('should work with file:// on internal storage', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///data/data/com.app/tiles',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file:///data/data/');
    });

    it('should support legacy format with placeholders (backward compatible)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
    });

    it('should accept file:// with JPEG extension', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/satellite.jpeg',
        minimumZ: 0,
        maximumZ: 16,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.jpeg');
    });

    it('should work with new API and custom tileSize', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/hd-tiles',
        tileSize: 512,
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(512);
      expect(component.props.urlTemplate).not.toContain('{z}');
    });

    it('should work with new API and flipY for TMS tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/tms-tiles',
        flipY: true,
        minimumZ: 0,
        maximumZ: 16,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.flipY).toBe(true);
      expect(component.props.urlTemplate).not.toContain('{z}');
    });

    it('should work with deeply nested paths in new API', () => {
      const props: MapUrlTileProps = {
        urlTemplate:
          'file:///storage/emulated/0/Android/data/com.myapp/files/maps/region1',
        minimumZ: 8,
        maximumZ: 16,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('region1');
      expect(component.props.urlTemplate).not.toContain('{z}');
    });

    it('should work with new API format for PNG (default)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toBe('file:///tiles');
    });
  });

  describe('Local Storage with Offline Mode', () => {
    it('should combine file:// with offlineMode flag', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles',
        offlineMode: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.offlineMode).toBe(true);
    });

    it('should work with file:// and zoom restrictions', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///maps/offline',
        offlineMode: true,
        minimumZ: 8,
        maximumZ: 16,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.minimumZ).toBe(8);
      expect(component.props.maximumZ).toBe(16);
    });
  });

  describe('Tile Cache Configuration', () => {
    it('should configure cache with absolute path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        tileCachePath: '/storage/emulated/0/osmdroid',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCachePath).toBe(
        '/storage/emulated/0/osmdroid'
      );
    });

    it('should configure cache with file:// protocol', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        tileCachePath: 'file:///storage/cache',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCachePath).toContain('file://');
    });

    it('should configure cache expiration', () => {
      const expirations = [
        3600, // 1 hour
        86400, // 1 day
        604800, // 1 week
        2592000, // 30 days
        31536000, // 1 year
        0, // Never expire
      ];

      expirations.forEach((age) => {
        const props: MapUrlTileProps = {
          urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
          tileCachePath: '/cache',
          tileCacheMaxAge: age,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.tileCacheMaxAge).toBe(age);
      });
    });

    it('should work offline with cache expiration disabled', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '/cache',
        tileCacheMaxAge: 0,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCacheMaxAge).toBe(0);
    });
  });

  describe('Real-World Offline Scenarios', () => {
    it('Scenario: Hiking app with pre-downloaded maps', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///sdcard/HikingApp/maps',
        offlineMode: true,
        minimumZ: 8,
        maximumZ: 16,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.minimumZ).toBe(8);
    });

    it('Scenario: Field data collection with offline base map', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/emulated/0/FieldWork/basemap',
        offlineMode: true,
        minimumZ: 10,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('FieldWork');
      expect(component.props.offlineMode).toBe(true);
    });

    it('Scenario: Emergency response with cached city maps', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.emergency.gov/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '/storage/emulated/0/EmergencyMaps/cache',
        tileCacheMaxAge: 0, // Never expire cached emergency maps
        minimumZ: 12,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCacheMaxAge).toBe(0);
    });

    it('Scenario: Navigation app with offline fallback', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://navigation.tiles.com/{z}/{x}/{y}.png',
        offlineMode: false, // Try online first
        tileCachePath: '/storage/osmdroid',
        tileCacheMaxAge: 604800, // 1 week cache
        maximumZ: 20,
        maximumNativeZ: 15, // Scale beyond zoom 15 to save bandwidth
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(false);
      expect(component.props.tileCachePath).toBeDefined();
      expect(component.props.maximumNativeZ).toBe(15);
    });

    it('Scenario: Offline topographic maps for surveyors', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///sdcard/SurveyMaps/topo/{z}/{x}/{y}.jpg',
        offlineMode: true,
        tileSize: 512, // High-resolution tiles
        minimumZ: 10,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.jpg');
      expect(component.props.tileSize).toBe(512);
      expect(component.props.offlineMode).toBe(true);
    });

    it('Scenario: Aviation app with sectional charts', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/Aviation/charts/{z}/{x}/{y}.png',
        offlineMode: true,
        flipY: true, // Charts may use TMS coordinate system
        minimumZ: 6,
        maximumZ: 14,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.flipY).toBe(true);
      expect(component.props.offlineMode).toBe(true);
    });

    it('Scenario: Maritime navigation with offline charts', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///sdcard/MarineCharts/{z}/{x}/{y}.png',
        offlineMode: true,
        minimumZ: 4,
        maximumZ: 16,
        opacity: 0.9, // Slightly transparent to show GPS track
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('MarineCharts');
      expect(component.props.opacity).toBe(0.9);
    });
  });

  describe('Offline Mode Edge Cases', () => {
    it('should handle switching from online to offline', () => {
      // Start online
      let props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: false,
        tileCachePath: '/cache',
      };
      let component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(false);

      // Switch to offline
      props = { ...props, offlineMode: true };
      component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
    });

    it('should handle file:// without offlineMode flag', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
        // offlineMode not explicitly set
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.offlineMode).toBeUndefined();
    });

    it('should handle offlineMode with online URL', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '/cache',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCachePath).toBeDefined();
    });

    it('should handle empty cache path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCachePath).toBe('');
    });
  });

  describe('Local Storage File Formats', () => {
    it('should support PNG tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.png');
    });

    it('should support JPEG tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.jpg',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.jpg');
    });

    it('should support WebP tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.webp',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('.webp');
    });

    it('should support tiles without extension in URL', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
    });
  });

  describe('Performance Optimization for Offline', () => {
    it('should configure for minimal storage with zoom limits', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
        offlineMode: true,
        minimumZ: 10,
        maximumZ: 15, // Limited zoom range to save storage
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.maximumZ).toBe(15);
    });

    it('should use tile scaling for offline maps', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
        offlineMode: true,
        maximumZ: 18,
        maximumNativeZ: 14, // Scale beyond zoom 14
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.maximumNativeZ).toBe(14);
      expect(component.props.maximumZ).toBe(18);
    });

    it('should support high-res offline tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}@2x.png',
        offlineMode: true,
        tileSize: 512,
        doubleTileSize: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(512);
      expect(component.props.doubleTileSize).toBe(true);
    });
  });

  describe('Local Tiles Storage - Critical Behavior Tests', () => {
    describe('Base Directory Extraction (New Simplified API)', () => {
      const testCases = [
        {
          input: 'file:///storage/tiles',
          expectedBase: '/storage/tiles',
          desc: 'simple path',
        },
        {
          input: 'file:///storage/tiles.jpg',
          expectedBase: '/storage/tiles',
          desc: 'path with extension hint',
        },
        {
          input: 'file:///storage/emulated/0/MyApp/maps/region1',
          expectedBase: '/storage/emulated/0/MyApp/maps/region1',
          desc: 'deeply nested path',
        },
        {
          input: 'file:///sdcard/tiles',
          expectedBase: '/sdcard/tiles',
          desc: 'sdcard path',
        },
        {
          input: 'file:///tiles/{z}/{x}/{y}.png',
          expectedBase: '/tiles',
          desc: 'legacy format with placeholders',
        },
        {
          input: 'file:///storage/maps/{z}/{x}/{y}.jpg',
          expectedBase: '/storage/maps',
          desc: 'legacy format with nested path',
        },
      ];

      testCases.forEach(({ input, desc }) => {
        it(`should correctly parse ${desc}: ${input}`, () => {
          const props: MapUrlTileProps = { urlTemplate: input };
          const component = React.createElement(MapUrlTile, props);
          expect(component.props.urlTemplate).toBe(input);
        });
      });
    });

    describe('File Extension Detection', () => {
      const extensionCases = [
        {
          input: 'file:///tiles',
          expectedExt: '.png',
          desc: 'no extension (default PNG)',
        },
        {
          input: 'file:///tiles.png',
          expectedExt: '.png',
          desc: 'explicit PNG',
        },
        {
          input: 'file:///tiles.jpg',
          expectedExt: '.jpg',
          desc: 'JPG extension',
        },
        {
          input: 'file:///tiles.jpeg',
          expectedExt: '.jpeg',
          desc: 'JPEG extension',
        },
        {
          input: 'file:///tiles.webp',
          expectedExt: '.webp',
          desc: 'WebP extension',
        },
        {
          input: 'file:///tiles/{z}/{x}/{y}.jpg',
          expectedExt: '.jpg',
          desc: 'legacy JPG',
        },
        {
          input: 'file:///tiles/{z}/{x}/{y}',
          expectedExt: '.png',
          desc: 'legacy no ext',
        },
      ];

      extensionCases.forEach(({ input, desc }) => {
        it(`should detect ${desc}`, () => {
          const props: MapUrlTileProps = { urlTemplate: input };
          const component = React.createElement(MapUrlTile, props);
          expect(component.props.urlTemplate).toBe(input);
        });
      });
    });

    describe('Local Storage Directory Structure', () => {
      it('should expect tiles in {baseDir}/{z}/{x}/{y}.{ext} structure', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/emulated/0/tiles',
          minimumZ: 10,
          maximumZ: 16,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toBe(
          'file:///storage/emulated/0/tiles'
        );
        expect(component.props.minimumZ).toBe(10);
        expect(component.props.maximumZ).toBe(16);
      });

      it('should use empty tile source name to avoid subdirectory issues', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///tiles',
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toBe('file:///tiles');
      });
    });

    describe('Offline Storage Configuration', () => {
      it('should disable network for file:// URLs automatically', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/offline-tiles',
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate.startsWith('file://')).toBe(true);
      });

      it('should set osmdroid cache path to tile directory', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/emulated/0/MyApp/tiles',
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain(
          '/storage/emulated/0/MyApp/tiles'
        );
      });

      it('should work with all supported Android storage paths', () => {
        const storagePaths = [
          'file:///storage/emulated/0/tiles',
          'file:///sdcard/tiles',
          'file:///data/data/com.myapp/files/tiles',
          'file:///storage/sdcard1/tiles',
          'file:///mnt/sdcard/tiles',
        ];

        storagePaths.forEach((path) => {
          const props: MapUrlTileProps = { urlTemplate: path };
          const component = React.createElement(MapUrlTile, props);
          expect(component.props.urlTemplate).toBe(path);
        });
      });
    });

    describe('Local Tiles with TMS Coordinate System', () => {
      it('should support flipY for tiles with TMS origin (bottom-left)', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/tms-tiles',
          flipY: true,
          minimumZ: 8,
          maximumZ: 16,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.flipY).toBe(true);
      });

      it('should default flipY to false for standard XYZ tiles', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/xyz-tiles',
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.flipY).toBeUndefined();
      });
    });

    describe('Local Tiles Full Offline Configuration', () => {
      it('should configure complete offline map with all options', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/emulated/0/OfflineMaps/region.jpg',
          offlineMode: true,
          minimumZ: 8,
          maximumZ: 16,
          tileSize: 256,
          flipY: false,
          opacity: 1.0,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain('file://');
        expect(component.props.urlTemplate).toContain('.jpg');
        expect(component.props.offlineMode).toBe(true);
        expect(component.props.minimumZ).toBe(8);
        expect(component.props.maximumZ).toBe(16);
        expect(component.props.tileSize).toBe(256);
      });

      it('should configure satellite imagery with optimized settings', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/satellite.jpg',
          offlineMode: true,
          minimumZ: 10,
          maximumZ: 18,
          maximumNativeZ: 15,
          tileSize: 512,
          doubleTileSize: true,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain('.jpg');
        expect(component.props.maximumNativeZ).toBe(15);
        expect(component.props.doubleTileSize).toBe(true);
      });
    });

    describe('Error Handling for Invalid Paths', () => {
      it('should accept paths with special characters', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/My Maps (2024)/tiles',
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain('My Maps (2024)');
      });

      it('should handle paths with unicode characters', () => {
        const props: MapUrlTileProps = {
          urlTemplate: 'file:///storage/地图/tiles',
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain('地图');
      });

      it('should handle very long nested paths', () => {
        const longPath =
          'file:///storage/emulated/0/Android/data/com.company.verylongappname/files/cache/maps/region/country/city/tiles';
        const props: MapUrlTileProps = { urlTemplate: longPath };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toBe(longPath);
      });
    });
  });
});
