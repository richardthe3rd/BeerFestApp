[![Android CI](https://github.com/richardthe3rd/BeerFestApp/actions/workflows/android.yml/badge.svg)](https://github.com/richardthe3rd/BeerFestApp/actions/workflows/android.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Android API 14+](https://img.shields.io/badge/Android-API%2014%2B-green.svg)]()
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)]()

# Cambridge Beer Festival Android App

A native Android app for discovering and tracking beers at the Cambridge Beer Festival. Features real-time beer data, brewery information, and festival details.

## Quick Start

```bash
# Build
./gradlew build

# Run tests
./gradlew test                    # Unit tests
./gradlew connectedCheck          # Instrumented tests (requires device)

# Install on device/emulator
./gradlew installDebug
```

## Tech Stack

- **Language**: Java 17
- **Build**: Gradle 8.0.0
- **Android SDK**: API 14+ (min), 34 (target), 33 (compile)
- **Database**: OrmLite 5.0
- **UI**: Material Design 1.8.0
- **Testing**: Espresso 3.3.0, JUnit 4.13.2

## Documentation

👉 **[See CLAUDE.md](CLAUDE.md)** for complete developer guide including:

- **[Getting Started](docs/getting-started.md)** – Setup and first contribution
- **[Annual Updates](docs/annual-updates/)** – Update for new festival year (most common task)
- **[Troubleshooting](docs/troubleshooting/)** – Debug crashes, stale data, and issues
- **[Features](docs/features/)** – Proposed improvements and roadmap
- **[Full Reference](CLAUDE-full.md.backup)** – Comprehensive documentation (2900+ lines)

## Project Structure

```
app/                     # Main Android application
├── src/main/java/       # Application code
├── src/main/res/        # Resources, layouts, strings
├── src/androidTest/     # Instrumented tests
└── src/test/            # Unit tests
libraries/beers/         # Shared domain models
docs/                    # Developer guides
.github/workflows/       # CI/CD pipelines
```

## License

Apache License 2.0 – See [LICENSE](LICENSE)

**Maintainer**: [Richard the Third](https://github.com/richardthe3rd)
