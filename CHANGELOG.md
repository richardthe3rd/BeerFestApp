# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2025.11.0] - 2025-11-22

### Added
- Tablet device testing (pixel_tablet) in CI matrix
- Multi-API level testing (API 29, 31, 34) for broader device coverage
- Comprehensive E2E test suite with UI architecture documentation
- BeerSearcher unit tests with full coverage
- AppPreferences test coverage
- Dialog verification in instrumented tests
- JaCoCo coverage reports in pull requests
- API documentation for multi-festival and multi-beverage support
- Progressive disclosure documentation structure (CLAUDE.md v3.0.0)

### Changed
- Upgraded Gradle to 8.14.3
- Upgraded setup-gradle GitHub Action from v4 to v5
- Migrated UI components to modern Material Design
- Separated release signing into dedicated CI job for better security
- Improved CI pipeline consistency and reliability
- Optimized CI build time from 13min to ~4-5min via caching
- Migrated DAO tests to modern AndroidJUnit4 framework
- Improved devcontainer setup with mise and developer extensions
- Improved README with badges and better structure

### Fixed
- Window focus timeout issue on pixel_tablet (Android 14)
- ListView interaction pattern in instrumented tests
- Instrumented tests now actually verify behavior
- Gradle configuration resolution performance issue
- Build warnings from deprecated API usage

[Unreleased]: https://github.com/richardthe3rd/BeerFestApp/compare/v2025.11.0...HEAD
[2025.11.0]: https://github.com/richardthe3rd/BeerFestApp/releases/tag/v2025.11.0
