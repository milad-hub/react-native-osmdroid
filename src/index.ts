import MapView, { MapViewProps } from './MapView';
export type { MapViewProps };
import Marker from './MapMarker';
import Polyline from './MapPolyline';
import Polygon from './MapPolygon';
export type { MapPolylineProps } from './MapPolyline';
export type { MapPolygonProps } from './MapPolygon';
export { default as Callout } from './MapCallout';
export type { MapCalloutProps } from './MapCallout';
export { default as Circle } from './MapCircle';
export type { MapCircleProps } from './MapCircle';
export { default as UrlTile } from './MapUrlTile';
export type { MapUrlTileProps } from './MapUrlTile';
export * from './MapView.types';
export * from './sharedTypes';
export default MapView;
export { Marker, Polyline, Polygon };
export { default as AnimatedRegion } from './AnimatedRegion';

import { NativeModules } from 'react-native';
const { TileCacher } = NativeModules;

/**
 * Specifies the path to the local directory where map tiles will be cached for offline use, available only on Android.
 * This feature enables the caching of downloaded map tiles on the device's local storage, allowing for
 * offline map functionality and improved performance for map tile loading on Android devices. The cached tiles reduce
 * network requests for previously visited areas and ensure maps are still accessible without an internet connection.
 *
 * Provide the ` TileCacher.cacheTilesFromDirectory` with a direct file system path to the desired cache directory on the device.
 * A typical path for Android might be '/storage/emulated/0/map/tiles', representing a directory
 * on the external storage where the app has write access.
 *
 * The tiles within the cache directory must follow the subdirectory pattern "/{z}/{x}/{y}.png", where
 * `{z}` is the zoom level, `{x}` and `{y}` are the tile coordinates. This structure allows the map component
 * to efficiently locate and load the appropriate tiles for display based on the current map view on Android devices.
 *
 * It's important to manage the cache directory's size to prevent excessive storage usage. Developers should
 * implement a strategy for cache cleanup or limiting the cache size. While the example TileDatabaseHelper
 * class demonstrates managing cached tiles in a database, any approach that monitors and controls the cache size
 * is advisable.
 *
 * Usage Example:
 * TileCacher.cacheTilesFromDirectory('/storage/emulated/0/map/tiles');
 *
 * Note: This prop is specific to Android. Ensure your app has the necessary permissions to read from and write to the specified directory,
 * particularly on Android, where runtime permissions are required for accessing external storage.
 */
export { TileCacher };
