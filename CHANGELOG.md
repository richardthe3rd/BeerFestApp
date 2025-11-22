# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2025.11.0] - 2025-11-22

### Added
- Tablet device testing (pixel_tablet) in CI matrix
- Multi-API level testing (API 29, 31, 34) for broader device coverage
- Comprehensive BeerSearcher unit tests
- AppPreferences test coverage
- Dialog verification in instrumented tests

### Changed
- Upgraded Gradle to 8.14.3
- Upgraded setup-gradle GitHub Action from v4 to v5
- Migrated UI components to modern Material Design
- Separated release signing into dedicated CI job for better security
- Improved CI pipeline consistency and reliability

### Fixed
- Window focus timeout issue on pixel_tablet (Android 14)
- ListView interaction pattern in instrumented tests
- Instrumented tests now actually verify behavior

[Unreleased]: https://github.com/richardthe3rd/BeerFestApp/compare/v2025.11.0...HEAD
[2025.11.0]: https://github.com/richardthe3rd/BeerFestApp/releases/tag/v2025.11.0
