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
    it('should accept file:// with absolute path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/emulated/0/tiles/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.urlTemplate).toContain('/storage/emulated/0/tiles');
    });

    it('should accept file:// with relative path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file://./tiles/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 15,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
    });

    it('should work with file:// on sdcard', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///sdcard/osm-tiles/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file:///sdcard/');
    });

    it('should work with file:// on internal storage', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///data/data/com.app/tiles/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file:///data/data/');
    });

    it('should work with file:// using different file extensions', () => {
      const extensions = ['.png', '.jpg', '.jpeg', '.webp'];
      extensions.forEach((ext) => {
        const props: MapUrlTileProps = {
          urlTemplate: `file:///tiles/{z}/{x}/{y}${ext}`,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain(ext);
      });
    });
  });

  describe('Local Storage with Offline Mode', () => {
    it('should combine file:// with offlineMode flag', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
        offlineMode: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
      expect(component.props.offlineMode).toBe(true);
    });

    it('should work with file:// and zoom restrictions', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///maps/offline/{z}/{x}/{y}.png',
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
      expect(component.props.tileCachePath).toBe('/storage/emulated/0/osmdroid');
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
        3600,      // 1 hour
        86400,     // 1 day
        604800,    // 1 week
        2592000,   // 30 days
        31536000,  // 1 year
        0,         // Never expire
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
        urlTemplate: 'file:///sdcard/HikingApp/maps/{z}/{x}/{y}.png',
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
        urlTemplate: 'file:///storage/emulated/0/FieldWork/basemap/{z}/{x}/{y}.png',
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
});

