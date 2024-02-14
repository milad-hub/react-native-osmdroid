# react-native-osmdroid [![npm version](https://img.shields.io/npm/v/@milad445/react-native-osmdroid.svg?style=flat)](https://www.npmjs.com/package/@milad445/react-native-osmdroid) [![Downloads](https://img.shields.io/npm/dm/@milad445/react-native-osmdroid.svg)](http://www.npmtrends.com/@milad445/react-native-osmdroid)

React Native Open Street Maps components for Android.
This is unofficial React Native wrapper for Osmdroid SDK.
Many thanks to [fqborges](https://github.com/fqborges) for his [library](https://github.com/fqborges/react-native-maps-osmdroid), which is the basis of this library!
## Installation

```sh
npm install @milad445/react-native-osmdroid
```

## Tile Caching with TileCacher

The `TileCacher` feature enables efficient offline map functionality by caching downloaded map tiles on the device's local storage. This allows for improved performance in map tile loading, reducing network requests for previously visited areas and ensuring maps remain accessible without an internet connection.

### How it Works

The `TileCacher` utilizes the `cacheTilesFromDirectory` method to specify the local directory path where map tiles will be cached for offline use. This feature is currently available only on Android.

```js
import { TileCacher } from '@milad445/react-native-osmdroid';

TileCacher.cacheTilesFromDirectory('/storage/emulated/0/map/tiles', { showProgressToast: true });
```

### Directory Structure
The cached tiles must be stored following the "/{z}/{x}/{y}.png" subdirectory pattern, where {z} is the zoom level, and {x} and {y} are the tile coordinates. This structure allows the map component to efficiently locate and load the appropriate tiles based on the current map view.

### Managing Cache Size
It's crucial to manage the cache directory's size to prevent excessive storage usage. Developers are advised to implement a strategy for cache cleanup or limiting the cache size.


## Manifest

In most cases, you will have to set the following authorizations in your AndroidManifest.xml:

```sh
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"  />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

On Android 13 or higher, the `android.permission.READ_MEDIA_IMAGES` permission and `android:requestLegacyExternalStorage="true"` attribute in your AndroidManifest.xml are required to access external storage for tile caching.

```sh
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
```

Add the `android:requestLegacyExternalStorage="true"` attribute to your application tag if targeting Android 10 (API level 29) or higher.

If you are only using parts of the library, you can adjust the permissions accordingly.

Online tile provider

```sh
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"  />
```

Offline tile provider and storing tiles

```sh
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Location provider

```sh
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

Android 6.0+ devices require you have to check for "dangerous" permissions at runtime.
osmdroid requires the following dangerous permissions:
WRITE_EXTERNAL_STORAGE and ACCESS_COARSE_LOCATION/ACCESS_FINE_LOCATION.
See [OpenStreetMapViewer's implementation](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/MainActivity.java#L83) or [Google Documentation on Permissions](https://developer.android.com/training/permissions/requesting)

### Note
This feature is specific to Android. Ensure your app has the necessary permissions to read from and write to the specified directory, particularly on Android, where runtime permissions are required for accessing external storage.

### Usage Example
```js
import React from 'react';
import { Button, View } from 'react-native';
import MapView, { UrlTile, TileCacher } from '@milad445/react-native-osmdroid';

export default function App() {
  const cacheTiles = () => {
    TileCacher.cacheTilesFromDirectory('/storage/emulated/0/map/tiles', { showProgressToast: true });
  };

  return (
    <View style={{ flex: 1 }}>
      <Button title="Cache Tiles" onPress={cacheTiles} />
      <MapView style={{ flex: 1 }}>
        <UrlTile urlTemplate="" />
      </MapView>
    </View>
  );
}
```
Note: Caching process duration is proportional to the number of tiles. Large datasets may take longer to cache.

## Component API

[`<MapView />` Component API](docs/mapview.md)

[`<Marker />` Component API](docs/marker.md)

[`<Callout />` Component API](docs/callout.md)

[`<Polygon />` Component API](docs/polygon.md)

[`<Polyline />` Component API](docs/polyline.md)

[`<Circle />` Component API](docs/circle.md)

[`<Overlay />` Component API](docs/overlay.md)

[`<Heatmap />` Component API](docs/heatmap.md)

[`<Geojson />` Component API](docs/geojson.md)

[`<URLTile />` and `<WMSTile />` Component API](docs/tiles.md)

## Usage

```js
import MapView from '@milad445/react-native-osmdroid';

// ...
<MapView
  initialRegion={{
    latitude: 37.78825,
    longitude: -122.4324,
    latitudeDelta: 0.0922,
    longitudeDelta: 0.0421,
  }}
/>;
```

## Examples
### MapView
<img src="./images/mapView.png" alt="mapView" width="300" height="500"/>

### Polyline
<img src="./images/polyline.png" alt="polyline" width="300" height="500"/>

### Marker
<img src="./images/marker.png" alt="marker" width="300" height="500"/>

### Polygon
<img src="./images/polygon.png" alt="polygon" width="300" height="500"/>

### Circle
<img src="./images/circle.png" alt="circle" width="300" height="500"/>

### UrlTile
<img src="./images/urlTile.png" alt="urlTile" width="300" height="500"/>

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
