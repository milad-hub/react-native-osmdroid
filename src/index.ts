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
 * Facilitates caching of map tiles in a specified local directory for offline use on Android devices. This utility
 * enhances offline map functionality and map tile loading performance by storing downloaded map tiles on the device's
 * local storage. Using cached tiles minimizes network requests for previously accessed areas, ensuring map availability
 * without an internet connection and speeding up map rendering.
 *
 * `TileCacher.cacheTilesFromDirectory` requires a filesystem path to the target cache directory. Optionally, a `showProgressToast`
 * boolean can be provided to display toast messages indicating caching progress as a percentage. This feedback is useful for
 * user experience, providing real-time updates on the caching process.
 *
 * Example cache directory path: '/storage/emulated/0/map/tiles'. The path should point to a directory on external storage
 * where the app has write permissions. Tiles must be organized in a "/{z}/{x}/{y}.png" structure within the cache directory,
 * where `{z}`, `{x}`, and `{y}` represent the zoom level and tile coordinates, respectively. This organization enables
 * efficient tile retrieval and display by the map component.
 *
 * Developers should monitor and manage the cache directory's size to avoid excessive storage consumption. Implementing
 * a cache management strategy, such as periodic cleanup or size limitation, is recommended. The provided TileDatabaseHelper
 * class example showcases one method of managing cached tiles, but developers may employ any suitable cache size control technique.
 *
 * Usage example with progress toast:
 * TileCacher.cacheTilesFromDirectory('/storage/emulated/0/map/tiles', { showProgressToast: true });
 *
 * Note: This functionality is Android-specific. Ensure the app has the required permissions to access external storage,
 * including runtime permissions necessary on Android.
 */
export { TileCacher };
