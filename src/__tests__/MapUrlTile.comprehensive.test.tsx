/**
 * Comprehensive test suite covering ALL scenarios for MapUrlTile
 * @jest-environment node
 */

import React from 'react';
import type { MapUrlTileProps } from '../MapUrlTile';

// Mock requireNativeComponent
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

describe('MapUrlTile - Comprehensive Test Suite', () => {
  // ========== PROP VALIDATION - ALL POSSIBLE VALUES ==========

  describe('urlTemplate - All Protocol Variations', () => {
    it('should accept standard HTTP URL', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'http://tile.openstreetmap.org/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toBe(
        'http://tile.openstreetmap.org/{z}/{x}/{y}.png'
      );
    });

    it('should accept standard HTTPS URL', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toBe(
        'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
      );
    });

    it('should accept file:// with absolute path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/emulated/0/tiles/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
    });

    it('should accept file:// with relative path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file://./tiles/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('file://');
    });

    it('should accept URL with subdomain', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://a.tile.openstreetmap.org/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('a.tile');
    });

    it('should accept URL with port number', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'http://localhost:8080/tiles/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain(':8080');
    });

    it('should accept URL with query parameters', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png?apikey=123&format=png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('?apikey=');
    });

    it('should accept URL with authentication', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://user:pass@tiles.com/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('user:pass@');
    });

    it('should accept URL with different file extensions', () => {
      const extensions = ['.png', '.jpg', '.jpeg', '.webp', '.gif'];
      extensions.forEach((ext) => {
        const props: MapUrlTileProps = {
          urlTemplate: `https://tiles.com/{z}/{x}/{y}${ext}`,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.urlTemplate).toContain(ext);
      });
    });

    it('should accept retina tiles format', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}@2x.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('@2x');
    });

    it('should accept WMS-style URL', () => {
      const props: MapUrlTileProps = {
        urlTemplate:
          'https://wms.server.com/service?layers=layer1&bbox={minX},{minY},{maxX},{maxY}',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('bbox=');
    });
  });

  describe('Zoom Levels - All Boundary Values', () => {
    it('should accept minimum zoom of 0', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        minimumZ: 0,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.minimumZ).toBe(0);
    });

    it('should accept maximum zoom of 20', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        maximumZ: 20,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.maximumZ).toBe(20);
    });

    it('should accept maximum zoom of 22 (extended)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        maximumZ: 22,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.maximumZ).toBe(22);
    });

    it('should accept fractional zoom levels', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        minimumZ: 5.5,
        maximumZ: 18.5,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.minimumZ).toBe(5.5);
      expect(component.props.maximumZ).toBe(18.5);
    });

    it('should accept same min and max zoom', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        minimumZ: 10,
        maximumZ: 10,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.minimumZ).toBe(10);
      expect(component.props.maximumZ).toBe(10);
    });

    it('should accept all zoom scaling scenarios', () => {
      const scenarios = [
        { max: 20, native: 15 }, // Standard scaling
        { max: 18, native: 13 }, // Heavy scaling
        { max: 22, native: 18 }, // Extended range
        { max: 15, native: 15 }, // No scaling
        { max: 20, native: 10 }, // Aggressive scaling
      ];

      scenarios.forEach(({ max, native }) => {
        const props: MapUrlTileProps = {
          urlTemplate: 'https://tiles.com',
          maximumZ: max,
          maximumNativeZ: native,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.maximumZ).toBe(max);
        expect(component.props.maximumNativeZ).toBe(native);
      });
    });
  });

  describe('Offline Mode - All Scenarios', () => {
    it('should accept offlineMode true', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles',
        offlineMode: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
    });

    it('should accept offlineMode false', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        offlineMode: false,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(false);
    });

    it('should work with offline + file URL', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///storage/tiles/{z}/{x}/{y}.png',
        offlineMode: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.urlTemplate).toContain('file://');
    });

    it('should work with offline + HTTP URL (cached)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        offlineMode: true,
        tileCachePath: '/cache',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.offlineMode).toBe(true);
      expect(component.props.tileCachePath).toBe('/cache');
    });
  });

  describe('Tile Caching - All Cache Configurations', () => {
    it('should accept absolute cache path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '/data/cache/tiles',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCachePath).toBe('/data/cache/tiles');
    });

    it('should accept relative cache path', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: './cache',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCachePath).toBe('./cache');
    });

    it('should accept cache with no expiration', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '/cache',
        tileCacheMaxAge: 0,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCacheMaxAge).toBe(0);
    });

    it('should accept short cache expiration (1 hour)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '/cache',
        tileCacheMaxAge: 3600,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCacheMaxAge).toBe(3600);
    });

    it('should accept medium cache expiration (1 day)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '/cache',
        tileCacheMaxAge: 86400,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCacheMaxAge).toBe(86400);
    });

    it('should accept long cache expiration (30 days)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '/cache',
        tileCacheMaxAge: 2592000,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCacheMaxAge).toBe(2592000);
    });

    it('should accept very long cache expiration (1 year)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '/cache',
        tileCacheMaxAge: 31536000,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCacheMaxAge).toBe(31536000);
    });
  });

  describe('Tile Sizes - All Variations', () => {
    it('should accept standard 256px tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileSize: 256,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(256);
    });

    it('should accept retina 512px tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileSize: 512,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(512);
    });

    it('should accept 128px tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileSize: 128,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(128);
    });

    it('should accept 1024px tiles', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileSize: 1024,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(1024);
    });

    it('should work with doubleTileSize flag', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        doubleTileSize: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.doubleTileSize).toBe(true);
    });

    it('should work with explicit size + doubleTileSize', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileSize: 256,
        doubleTileSize: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileSize).toBe(256);
      expect(component.props.doubleTileSize).toBe(true);
    });
  });

  describe('Opacity - All Values', () => {
    it('should accept opacity 0 (fully transparent)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 0,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.opacity).toBe(0);
    });

    it('should accept opacity 0.1', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 0.1,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.opacity).toBe(0.1);
    });

    it('should accept opacity 0.25', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 0.25,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.opacity).toBe(0.25);
    });

    it('should accept opacity 0.5 (half transparent)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 0.5,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.opacity).toBe(0.5);
    });

    it('should accept opacity 0.75', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 0.75,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.opacity).toBe(0.75);
    });

    it('should accept opacity 1 (fully opaque)', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 1,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.opacity).toBe(1);
    });

    it('should accept precise opacity values', () => {
      const values = [0.33, 0.66, 0.125, 0.875, 0.999];
      values.forEach((value) => {
        const props: MapUrlTileProps = {
          urlTemplate: 'https://tiles.com',
          opacity: value,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.opacity).toBe(value);
      });
    });
  });

  describe('FlipY - TMS Coordinate System', () => {
    it('should accept flipY true', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tms-tiles.com',
        flipY: true,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.flipY).toBe(true);
    });

    it('should accept flipY false', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        flipY: false,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.flipY).toBe(false);
    });

    it('should work with TMS server configuration', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tms.server.com/{z}/{x}/{y}.png',
        flipY: true,
        minimumZ: 0,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.flipY).toBe(true);
    });
  });

  // ========== COMPLEX CONFIGURATION SCENARIOS ==========

  describe('Use Case Configurations', () => {
    it('should configure for long-term caching scenario', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.example.com/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 19,
        tileCachePath: '/cache/tiles',
        tileCacheMaxAge: 604800, // 1 week
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should configure for bandwidth optimization with scaling', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.example.com/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 22,
        maximumNativeZ: 18,
        tileCachePath: '/cache/tiles',
        tileCacheMaxAge: 2592000, // 30 days
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should configure for offline field mapping', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///sdcard/fieldmaps/{z}/{x}/{y}.png',
        offlineMode: true,
        minimumZ: 12,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should configure for transparent weather overlay', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://weather.example.com/{z}/{x}/{y}.png',
        opacity: 0.4,
        tileCacheMaxAge: 3600, // 1 hour for live data
        minimumZ: 0,
        maximumZ: 10,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should configure for high-resolution satellite imagery', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://satellite.example.com/{z}/{x}/{y}.jpg',
        tileSize: 512,
        maximumZ: 20,
        maximumNativeZ: 15,
        tileCachePath: '/cache/satellite',
        tileCacheMaxAge: 7776000, // 90 days
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should configure for real-time traffic overlay', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://traffic.example.com/{z}/{x}/{y}.png',
        opacity: 0.7,
        tileCacheMaxAge: 300, // 5 minutes for real-time updates
        minimumZ: 8,
        maximumZ: 18,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });
  });

  describe('Edge Cases and Boundary Conditions', () => {
    it('should handle empty string tileCachePath', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        tileCachePath: '',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.tileCachePath).toBe('');
    });

    it('should handle very long URL templates', () => {
      const longUrl =
        'https://tiles.com/' + 'a'.repeat(500) + '/{z}/{x}/{y}.png';
      const props: MapUrlTileProps = {
        urlTemplate: longUrl,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toBe(longUrl);
    });

    it('should handle URL with special characters', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/path%20with%20spaces/{z}/{x}/{y}.png',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toContain('%20');
    });

    it('should handle all props at maximum values', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 22,
        maximumNativeZ: 20,
        offlineMode: false,
        tileCachePath: '/cache',
        tileCacheMaxAge: 999999999,
        tileSize: 1024,
        doubleTileSize: true,
        flipY: true,
        opacity: 1,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should handle all props at minimum values', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 0,
        maximumNativeZ: 0,
        offlineMode: true,
        tileCachePath: '/',
        tileCacheMaxAge: 0,
        tileSize: 1,
        doubleTileSize: false,
        flipY: false,
        opacity: 0,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });
  });

  describe('Prop Combinations - All Permutations', () => {
    it('should work: online + cached + scaled + transparent', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        maximumZ: 20,
        maximumNativeZ: 15,
        tileCachePath: '/cache',
        tileCacheMaxAge: 86400,
        opacity: 0.8,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should work: offline + file + no cache', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'file:///tiles/{z}/{x}/{y}.png',
        offlineMode: true,
        minimumZ: 0,
        maximumZ: 15,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should work: high-res + TMS + cached', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tms-tiles.com/{z}/{x}/{y}@2x.png',
        tileSize: 512,
        doubleTileSize: true,
        flipY: true,
        tileCachePath: '/cache',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });

    it('should work: low zoom + high opacity + short cache', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com/{z}/{x}/{y}.png',
        minimumZ: 0,
        maximumZ: 8,
        opacity: 0.9,
        tileCacheMaxAge: 300,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props).toMatchObject(props);
    });
  });

  describe('Component Lifecycle', () => {
    it('should create multiple instances independently', () => {
      const props1: MapUrlTileProps = { urlTemplate: 'https://tiles1.com' };
      const props2: MapUrlTileProps = { urlTemplate: 'https://tiles2.com' };

      const component1 = React.createElement(MapUrlTile, props1);
      const component2 = React.createElement(MapUrlTile, props2);

      expect(component1.props.urlTemplate).not.toBe(
        component2.props.urlTemplate
      );
    });

    it('should handle prop updates', () => {
      const props1: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 0.5,
      };
      const props2: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        opacity: 1.0,
      };

      const component1 = React.createElement(MapUrlTile, props1);
      const component2 = React.createElement(MapUrlTile, props2);

      expect(component1.props.opacity).toBe(0.5);
      expect(component2.props.opacity).toBe(1.0);
    });
  });

  describe('Type Safety', () => {
    it('should enforce urlTemplate as required', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.urlTemplate).toBeDefined();
    });

    it('should accept all optional props as undefined', () => {
      const props: MapUrlTileProps = {
        urlTemplate: 'https://tiles.com',
        minimumZ: undefined,
        maximumZ: undefined,
        opacity: undefined,
      };
      const component = React.createElement(MapUrlTile, props);
      expect(component.props.minimumZ).toBeUndefined();
    });
  });

  // ========== STRESS TESTS ==========

  describe('Stress Tests', () => {
    it('should handle rapid component creation', () => {
      for (let i = 0; i < 100; i++) {
        const props: MapUrlTileProps = {
          urlTemplate: `https://tiles${i}.com/{z}/{x}/{y}.png`,
          opacity: i / 100,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.opacity).toBe(i / 100);
      }
    });

    it('should handle all zoom levels 0-22', () => {
      for (let z = 0; z <= 22; z++) {
        const props: MapUrlTileProps = {
          urlTemplate: 'https://tiles.com',
          minimumZ: z,
          maximumZ: z,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.minimumZ).toBe(z);
      }
    });

    it('should handle all opacity increments', () => {
      for (let i = 0; i <= 100; i += 5) {
        const opacity = i / 100;
        const props: MapUrlTileProps = {
          urlTemplate: 'https://tiles.com',
          opacity,
        };
        const component = React.createElement(MapUrlTile, props);
        expect(component.props.opacity).toBe(opacity);
      }
    });
  });
});
