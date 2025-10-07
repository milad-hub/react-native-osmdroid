# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0-beta.0] - 2025-10-07

### ⚠️ BETA RELEASE - NOT PRODUCTION READY

This is a beta release with significant improvements to offline mode and local tile functionality. While comprehensive testing has been done (130 tests passing), this version should be tested thoroughly in development environments before production use.

### Added

- **Enhanced Offline Mode**: Properly implemented offline tile providers using osmdroid's native APIs
  - `MapTileFilesystemProvider` for reading cached tiles
  - `MapTileFileArchiveProvider` for .zip, .mbtiles, and .gemf tile archives
  - Automatic network disabling when offline mode is enabled
  
- **Local File Storage Support**: Full implementation of `file://` protocol
  - Read tiles directly from device filesystem
  - Support for absolute paths (`/storage/emulated/0/tiles`)
  - Support for SD card paths (`/sdcard/maps`)
  - Support for relative paths (`./tiles`)
  
- **Improved Tile Caching**:
  - Proper cache directory configuration via `tileCachePath` prop
  - Cache expiration control via `tileCacheMaxAge` prop
  - Automatic cache directory creation
  - Integration with osmdroid's native cache system

- **Network Management**:
  - Automatic network enable/disable based on `offlineMode` and `file://` URLs
  - Proper handling of online/offline mode switching
  
- **File Format Support**:
  - PNG tiles (standard)
  - JPEG/JPG tiles (for satellite imagery)
  - WebP tiles (modern compression)
  - Tile archives (.zip, .mbtiles, .gemf)

- **Coordinate System Support**:
  - `flipY` prop now works correctly with offline tiles
  - Full TMS (Tile Map Service) coordinate system support

### Fixed

- Fixed `tileCachePath` prop not being used - now properly configures osmdroid cache
- Fixed `offlineMode` not truly working offline - now disables network and uses proper providers
- Fixed `file://` URLs not reading from local filesystem - now uses proper file-based tile source
- Fixed tile loading issues in offline mode
- Fixed cache expiration not being respected

### Changed

- Complete rewrite of `OsmMapUrlTile.java` implementation (~435 lines)
  - Replaced incomplete implementation with proper offline tile providers
  - Added `createOfflineTileProvider()` method
  - Added `configureTileCache()` method
  - Added `FileBasedTileSource` for local tiles
  - Improved error handling and logging

### Testing

- Added 33 comprehensive offline-specific tests
- Added 66 exhaustive prop validation tests  
- Added 30 integration and workflow tests
- Total: 130 tests, all passing ✅

### Documentation

- Added beta warning in README.md
- Added offline mode usage examples
- Added local file storage examples
- Added hybrid online/offline example with network detection
- Added important notes for beta users

### Known Limitations

- Android only (iOS not yet supported)
- Requires proper storage permissions
- Some tile archive formats may have limited testing

### Migration Guide

For users upgrading from 1.0.x:

**No breaking changes** - all existing functionality remains compatible. New features are opt-in:

```jsx
// Old way (still works)
<UrlTile urlTemplate="https://tile.openstreetmap.org/{z}/{x}/{y}.png" />

// New: Enable offline mode
<UrlTile
  urlTemplate="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
  offlineMode={true}
  tileCachePath="/storage/cache"
  tileCacheMaxAge={604800}
/>

// New: Use local files
<UrlTile
  urlTemplate="file:///storage/maps/{z}/{x}/{y}.png"
  offlineMode={true}
/>
```

### Feedback

Please report any issues on [GitHub Issues](https://github.com/milad-hub/react-native-osmdroid/issues) with the `beta` label.

---

## [1.0.9] - Previous Stable Release

Last stable version before beta. See git history for previous changes.

