# CLAUDE.md - AI Assistant Guide for BeerFestApp

This document provides comprehensive guidance for AI assistants working with the Cambridge Beer Festival Android application codebase.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture and Structure](#architecture-and-structure)
3. [Tech Stack](#tech-stack)
4. [Development Setup](#development-setup)
5. [Build and Test](#build-and-test)
6. [Annual Festival Update Workflow](#annual-festival-update-workflow)
7. [Coding Conventions](#coding-conventions)
8. [CI/CD Pipeline](#cicd-pipeline)
9. [Key Files Reference](#key-files-reference)
10. [Common Development Tasks](#common-development-tasks)
11. [Database Schema](#database-schema)
12. [Testing Strategy](#testing-strategy)
13. [Release Process](#release-process)

---

## Project Overview

**Project Name:** BeerFestApp (Cambridge Beer Festival Android App)
**Purpose:** Native Android application for browsing beer lists at the annual Cambridge Beer Festival
**License:** BSD 3-Clause (2011-2012, Richard Alcock)
**Repository:** https://github.com/richardthe3rd/BeerFestApp

### Key Features
- Browse comprehensive beer lists with brewery information
- Filter beers by style
- Sort beers by various criteria (name, ABV, brewery, etc.)
- Bookmark favorite beers
- Search functionality
- Share beers on social media with festival hashtag
- Export beer lists
- Offline-first design with local SQLite database
- Annual data updates from remote JSON feed

### Project Maturity
- **Status:** Mature, production-ready application
- **Maintenance Pattern:** Annual updates for each year's festival (cbf2024, cbf2025, etc.)
- **Active Development:** Primarily maintenance-driven with periodic CI/CD improvements
- **Code Size:** ~3,500 lines of production code

---

## Architecture and Structure

### Overall Architecture Pattern
The application follows a **layered MVC-inspired architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│          View Layer                     │
│  (Fragments, Adapters, Dialogs)         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Actions/Business Logic            │
│  (BeerSearcher, BeerSharer, etc.)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Model/Data Layer                │
│   (OrmLite DAOs, Database Helper)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Domain Models (Library)           │
│    (Beer, Brewery, BeerList)            │
└─────────────────────────────────────────┘
```

### Directory Structure

```
BeerFestApp/
├── .github/workflows/          # CI/CD configuration
│   └── android.yml             # GitHub Actions pipeline
│
├── .devcontainer/              # Dev container setup
│   ├── Dockerfile              # Android SDK container
│   └── devcontainer.json       # VS Code dev container config
│
├── app/                        # Main Android application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ralcock/cbf/
│   │   │   │   ├── CamBeerFestApplication.java    # Main Activity
│   │   │   │   ├── AppPreferences.java            # Settings wrapper
│   │   │   │   ├── actions/                       # Business logic layer
│   │   │   │   │   ├── BeerSearcher.java
│   │   │   │   │   ├── BeerSharer.java
│   │   │   │   │   └── BeerExporter.java
│   │   │   │   ├── model/                         # Data access layer
│   │   │   │   │   ├── BeerAccessor.java
│   │   │   │   │   ├── BeerDatabaseHelper.java    # DB initialization
│   │   │   │   │   └── BeerDatabaseConfigUtil.java
│   │   │   │   ├── service/                       # Background services
│   │   │   │   │   ├── UpdateService.java         # Data update service
│   │   │   │   │   └── UpdateTask.java            # Async update task
│   │   │   │   ├── util/
│   │   │   │   │   └── ExceptionReporter.java
│   │   │   │   └── view/                          # UI components
│   │   │   │       ├── fragments/
│   │   │   │       │   ├── BeerListFragment.java
│   │   │   │       │   ├── BeerDetailsFragment.java
│   │   │   │       │   └── ...
│   │   │   │       ├── adapters/
│   │   │   │       │   ├── BeerListAdapter.java
│   │   │   │       │   └── BeerStyleListAdapter.java
│   │   │   │       └── dialogs/
│   │   │   │           ├── FilterByStyleDialogFragment.java
│   │   │   │           └── ...
│   │   │   ├── res/                               # Android resources
│   │   │   │   ├── layout/                        # 8 XML layouts
│   │   │   │   ├── drawable-*/                    # Multi-density assets
│   │   │   │   ├── menu/                          # Menu definitions
│   │   │   │   ├── raw/                           # Raw resources
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml                # String resources
│   │   │   │       └── festival.xml               # Festival config
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/                           # Instrumented tests
│   │   │   └── java/ralcock/cbf/
│   │   │       └── CamBeerFestApplicationInstrumentedTest.java
│   │   └── tests/                                 # Unit tests
│   │       └── src/ralcock/cbf/
│   │           ├── BeerListTest.java
│   │           ├── BeersImplTest.java
│   │           └── ... (10+ test classes)
│   ├── build.gradle                               # App build config
│   └── release.gradle                             # Release signing config
│
├── libraries/beers/            # Reusable domain model library
│   ├── src/
│   │   ├── main/java/ralcock/cbf/model/
│   │   │   ├── Beer.java                          # Beer entity
│   │   │   ├── BeerBuilder.java                   # Builder pattern
│   │   │   ├── Brewery.java                       # Brewery entity
│   │   │   ├── BreweryBuilder.java
│   │   │   ├── BeerList.java                      # Beer collection
│   │   │   ├── JsonBeerList.java                  # JSON parser
│   │   │   ├── SortOrder.java                     # Sort enum
│   │   │   ├── StarRating.java                    # Rating enum
│   │   │   ├── StatusToShow.java                  # Filter enum
│   │   │   └── dao/                               # Data Access Objects
│   │   │       ├── Beers.java                     # Beer DAO interface
│   │   │       ├── BeersImpl.java                 # Implementation
│   │   │       ├── Breweries.java                 # Brewery DAO
│   │   │       ├── BreweriesImpl.java
│   │   │       └── BeerAccessException.java
│   │   └── test/                                  # Library unit tests
│   └── build.gradle                               # Library build config
│
├── gradle/wrapper/             # Gradle wrapper files
├── build.gradle                # Root build configuration
├── settings.gradle             # Multi-module setup
├── gradle.properties           # Gradle properties
├── proguard.cfg                # Code obfuscation rules
├── example-beer-list.json      # Sample data for testing
├── README.md                   # Basic project info
├── UPDATING.md                 # Festival update instructions
├── privacy.md                  # Privacy policy
└── LICENSE                     # BSD 3-Clause license
```

### Package Organization

**Main App Package:** `ralcock.cbf`

| Package | Purpose | Key Classes |
|---------|---------|-------------|
| `ralcock.cbf` | Main activity and app config | `CamBeerFestApplication`, `AppPreferences` |
| `ralcock.cbf.actions` | Business logic and use cases | `BeerSearcher`, `BeerSharer`, `BeerExporter` |
| `ralcock.cbf.model` | Data access and persistence | `BeerDatabaseHelper`, `BeerAccessor` |
| `ralcock.cbf.service` | Background services | `UpdateService`, `UpdateTask` |
| `ralcock.cbf.view` | UI components | Fragments, Adapters, Dialogs |
| `ralcock.cbf.util` | Utilities | `ExceptionReporter` |

**Library Package:** `ralcock.cbf.model` (in libraries/beers)

Contains framework-agnostic domain models and DAOs that can be reused across different Android apps or even non-Android Java projects.

---

## Tech Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Language** | Java | Primary development language |
| **JDK** | 17 (Temurin) | Java Development Kit |
| **Build System** | Gradle 8.0.0 | Build automation |
| **Android Plugin** | 8.0.0 | Android build tooling |
| **Compile SDK** | 33 | Android SDK version for compilation |
| **Target SDK** | 34 | Target Android API level |
| **Min SDK** | 14 | Minimum Android API level (Android 4.0+) |

### Key Libraries

#### UI Framework
- **AndroidX AppCompat** - Backward-compatible support library
- **Google Material Design** (1.8.0) - Material Design components
- **LocalBroadcastManager** - Local event broadcasting

#### Data Persistence
- **OrmLite Core** (5.0) - Object-relational mapping framework
- **OrmLite Android** (5.0) - Android-specific ORM extensions
- **SQLite** - Local database (Android built-in)

#### Logging
- **SLF4J Android** (1.7.25) - Logging facade

#### Data Formats
- **org.json** (20160810) - JSON parsing library

### Testing Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **JUnit** | 4.11 | Unit testing framework |
| **Hamcrest** | 1.3 | Matcher library for assertions |
| **Espresso Core** | 3.3.0 | UI testing framework |
| **Espresso Contrib** | 3.3.0 | Additional Espresso utilities |
| **AndroidX Test Runner** | 1.3.0 | Test execution |
| **AndroidX Test Rules** | 1.3.0 | Test rules |
| **H2 Database** | 1.4.194 | In-memory DB for unit tests |
| **OrmLite JDBC** | 5.0 | JDBC support for testing |

---

## Development Setup

### Prerequisites
- **JDK 17** (Temurin distribution recommended)
- **Android SDK** with API levels 14-34
- **Gradle** 8.0.0 (or use wrapper: `./gradlew`)
- **Git** for version control

### Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/richardthe3rd/BeerFestApp.git
   cd BeerFestApp
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run tests:**
   ```bash
   ./gradlew test                    # Unit tests
   ./gradlew connectedCheck          # Instrumented tests (requires device/emulator)
   ```

4. **Install on device:**
   ```bash
   ./gradlew installDebug
   ```

### Dev Container Setup

The project includes a dev container configuration for consistent development environments:

**Location:** `.devcontainer/`

**Features:**
- Java 17 pre-installed
- Android SDK with command-line tools
- Gradle and Maven
- VS Code extensions: Java Pack, Docker, Gradle, ActionLint

**Usage:**
- Open in VS Code with Remote-Containers extension
- Select "Reopen in Container" when prompted

### Environment Configuration

**Gradle Properties** (`gradle.properties`):
```properties
android.builder.sdkDownload=true     # Auto-download SDK components
org.gradle.jvmargs=-Xmx1538M         # JVM memory allocation
android.useAndroidX=true             # Use AndroidX libraries
```

---

## Build and Test

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew build -PRELEASE --scan

# Clean build
./gradlew clean build

# Build with verbose output
./gradlew build --info

# Build with stack traces
./gradlew build --stacktrace
```

### Test Commands

```bash
# Run all unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedCheck

# Run specific test class
./gradlew test --tests BeersImplTest

# Run tests with code coverage (library module)
./gradlew jacocoTestReport

# Run all checks (lint, tests, etc.)
./gradlew check
```

### Build Configuration

**Debug Build:**
- No signing required
- Debugging enabled
- No code obfuscation
- Faster build times

**Release Build:**
- Requires `release.gradle` with signing configuration
- ProGuard/R8 obfuscation enabled (see `proguard.cfg`)
- Triggered with `-PRELEASE` flag
- Used in CI/CD for production builds

### Gradle Tasks Reference

```bash
# List all available tasks
./gradlew tasks

# List project dependencies
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates  # (if plugin configured)

# Generate build scan
./gradlew build --scan
```

---

## Annual Festival Update Workflow

**CRITICAL:** This is the most common development task. The app is updated annually for each Cambridge Beer Festival.

### Update Checklist

Follow these steps **in order** when updating for a new festival year:

#### 1. Update Application Version
**File:** `app/build.gradle`
**Lines:** 30-31

```gradle
defaultConfig {
    versionCode 27                    // INCREMENT by 1
    versionName "2025.0.0.1"          // UPDATE year (e.g., 2025 → 2026)
    minSdkVersion 14
    targetSdkVersion 34
    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
```

**Rules:**
- `versionCode`: Increment by 1 for each release (27 → 28 → 29...)
- `versionName`: Format is `YYYY.0.0.1` where YYYY is the festival year

#### 2. Update Festival Configuration
**File:** `app/src/main/res/values/festival.xml`

```xml
<resources>
    <string name="app_name">Cambridge Beer Festival</string>
    <string name="festival_name">Cambridge Beer Festival 2025</string>  <!-- UPDATE year -->
    <string name="festival_hashtag">cbf2025</string>                   <!-- UPDATE year -->
    <string name="festival_website_url">https://www.cambridgebeerfestival.com/</string>
    <string formatted="false" name="share_intent_subject">Drinking a %1$s at the %2$s</string>
    <string formatted="false" name="share_intent_text">Drinking %1$s %2$s</string>
    <string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>  <!-- UPDATE year -->
</resources>
```

**Update these strings:**
- `festival_name`: "Cambridge Beer Festival YYYY"
- `festival_hashtag`: "cbfYYYY"
- `beer_list_url`: Update year in URL path

#### 3. Increment Database Version
**File:** `app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java`
**Line:** 19

```java
public final class BeerDatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "BEERS";

    private static final int DB_VERSION = 32; // cbf2025  ← UPDATE this
```

**Rules:**
- Increment `DB_VERSION` by 1 (32 → 33 → 34...)
- Update comment to reflect new festival year
- **Important:** This forces database migration, clearing old festival data

#### 4. Test the Changes

```bash
# Build and run tests
./gradlew clean test

# Test on emulator
./gradlew connectedCheck

# Manual testing checklist:
# - App launches successfully
# - Beer list downloads from new URL
# - Database migration works (old data cleared)
# - Share functionality uses correct hashtag
# - Festival name displays correctly in UI
```

#### 5. Commit the Changes

```bash
# Stage the three modified files
git add app/build.gradle
git add app/src/main/res/values/festival.xml
git add app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java

# Commit with conventional message
git commit -m "cbf2026"

# Or more descriptive:
git commit -m "Update app for Cambridge Beer Festival 2026 (#XX)"
```

### Historical Pattern

Looking at recent commits, the update pattern follows:
- `ab75e5c` - cbf2025 (#18)
- `912ba56` - Update various things for cbf2024 (#16)
- `cff8443` - update resources for 2023 (#15)

**Commit Message Convention:**
- Short form: `cbfYYYY`
- PR form: `Update app for Cambridge Beer Festival YYYY (#XX)`

---

## Coding Conventions

### Java Style Guidelines

#### Naming Conventions

```java
// Classes: PascalCase
public class BeerDatabaseHelper { }

// Interfaces: PascalCase (no 'I' prefix)
public interface Beers { }

// Constants: UPPER_SNAKE_CASE
public static final String DATABASE_NAME = "BEERS";
private static final int DB_VERSION = 32;

// Fields: camelCase with 'f' prefix (Hungarian notation)
private Breweries fBreweries;
private Beers fBeers;

// Methods: camelCase
public Beers getBeers() { }

// Parameters: camelCase with descriptive names
public void onCreate(final SQLiteDatabase sqLiteDatabase,
                     final ConnectionSource connectionSource) { }
```

#### Field Naming Pattern

The codebase uses **Hungarian notation** for instance fields:

```java
private Breweries fBreweries;  // 'f' prefix = field
private Beers fBeers;
```

**Note:** While this is an older Java convention, it's used consistently throughout this codebase. Maintain this pattern for consistency.

#### Code Formatting

```java
// Indentation: 4 spaces (no tabs in source, tabs in build files)
public void onCreate(final SQLiteDatabase sqLiteDatabase,
                     final ConnectionSource connectionSource) {
    try {
        TableUtils.createTable(connectionSource, Beer.class);
        TableUtils.createTable(connectionSource, Brewery.class);
    } catch (SQLException sqlx) {
        throw new RuntimeException(sqlx);
    }
}

// Braces: Open on same line, close on new line
if (fBeers == null) {
    // ...
}

// Final parameters: Used extensively
public BeerDatabaseHelper(final Context context) {
    super(context, DATABASE_NAME, null, DB_VERSION, R.raw.ormlite_config);
}
```

#### Exception Handling

```java
// Catch specific exceptions, wrap in RuntimeException if appropriate
try {
    TableUtils.createTable(connectionSource, Beer.class);
} catch (SQLException sqlx) {
    throw new RuntimeException(sqlx);
}

// SQLException → RuntimeException is the standard pattern in this codebase
```

### Android-Specific Conventions

#### Activity/Fragment Naming
- Activities: `*Application.java` (e.g., `CamBeerFestApplication.java`)
- Fragments: `*Fragment.java` (e.g., `BeerListFragment.java`)
- Dialog Fragments: `*DialogFragment.java`
- Adapters: `*Adapter.java`

#### Resource Naming
```xml
<!-- Strings: lowercase with underscores -->
<string name="festival_name">Cambridge Beer Festival 2025</string>
<string name="beer_list_url">...</string>

<!-- IDs: component_description_type -->
<!-- Layouts: activity_*, fragment_*, dialog_*, list_item_* -->
```

### Build File Conventions

```gradle
// Gradle files use tabs for indentation (unlike Java source)
dependencies {
	implementation group: 'com.j256.ormlite', name: 'ormlite-core', version: '5.0'
}

// Group dependencies by type
dependencies {
    // Main dependencies
    implementation project(':libraries:beers')
    implementation group: 'com.j256.ormlite', name: 'ormlite-core', version: '5.0'

    // Test dependencies
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
```

### Compiler Settings

The project enforces strict compilation:

```gradle
tasks.withType(JavaCompile) {
    options.compilerArgs << '-Xlint:unchecked'
    options.deprecation = true
}
```

**Implications:**
- All unchecked warnings will be shown
- Deprecation warnings will be shown
- Aim to minimize warnings when making changes

### Lint Configuration

```gradle
lintOptions {
    abortOnError false  // Warnings don't fail the build
}
```

**Note:** While lint doesn't fail builds, address lint warnings when practical.

---

## CI/CD Pipeline

### GitHub Actions Workflow

**File:** `.github/workflows/android.yml`

The CI/CD pipeline runs on GitHub Actions with two jobs:

#### Job 1: Build

**Triggers:**
- Manual: `workflow_dispatch`
- Push to `main` branch (ignoring docs/config changes)
- Pull requests to `main` branch

**Steps:**
1. Checkout code
2. Set up JDK 17 (Temurin)
3. Setup Android SDK
4. Decode signing keystore from GitHub Secrets
5. Setup Gradle with caching
6. **Build with:** `./gradlew build -PRELEASE --scan`
7. Upload build reports (on success or failure)
8. Upload build artifacts (APKs)

**Secrets Required:**
- `KEYSTORE` - Base64-encoded signing keystore
- `SIGNING_KEY_ALIAS` - Key alias
- `SIGNING_KEY_PASSWORD` - Key password
- `SIGNING_STORE_PASSWORD` - Store password

**Artifacts Produced:**
- `build-reports` - Build reports
- `build-artifacts` - APK outputs in `app/build/outputs/`

#### Job 2: Test

**Runs on:** Android API level 34 emulator

**Steps:**
1. Free disk space (removes unnecessary tools)
2. Enable KVM permissions for emulation
3. Checkout code
4. Set up JDK 17 with Gradle cache
5. Setup Gradle
6. Cache AVD (Android Virtual Device)
7. Create AVD snapshot if not cached
8. **Run tests:** `./gradlew connectedCheck --scan`
9. Upload test reports (on success or failure)

**Optimizations:**
- AVD caching for faster test runs
- Disk space cleanup for emulator performance
- Gradle caching across jobs
- No snapshot saving during test runs (faster)

### Build Scans

The pipeline uses Gradle Build Scans (`--scan` flag):

**Purpose:**
- Detailed build performance analysis
- Dependency resolution insights
- Test execution details
- Published to Gradle Enterprise

**Configuration:** `build.gradle` (root)

```gradle
plugins {
    id "com.gradle.enterprise" version "3.9"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}
```

### CI/CD Best Practices for Development

When working on this codebase:

1. **Local testing before push:**
   ```bash
   ./gradlew clean build test
   ```

2. **Check CI status after pushing:**
   - Monitor GitHub Actions workflow
   - Review build scans if failures occur

3. **Failed builds:**
   - Download build reports from Actions artifacts
   - Check build scan links in workflow logs

4. **Pull requests:**
   - Ensure all CI checks pass
   - Review build artifacts if needed

---

## Key Files Reference

### Configuration Files

| File | Purpose | When to Edit |
|------|---------|--------------|
| `app/build.gradle` | App build configuration, dependencies, versioning | Version updates, dependency changes |
| `app/src/main/res/values/festival.xml` | Festival-specific configuration (name, URL, hashtag) | **Annual festival updates** |
| `app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java` | Database versioning and schema | **Annual festival updates**, schema changes |
| `build.gradle` (root) | Root build config, Gradle Enterprise | Repository changes, build scan config |
| `settings.gradle` | Multi-module project structure | Adding/removing modules |
| `gradle.properties` | Gradle JVM settings, AndroidX config | Memory issues, build optimization |
| `proguard.cfg` | Code obfuscation rules for release builds | Adding reflection-based libraries |
| `app/src/main/AndroidManifest.xml` | App manifest, permissions, activities | Permissions, services, activities |

### Documentation Files

| File | Purpose | Audience |
|------|---------|----------|
| `README.md` | Basic project information | Users, contributors |
| `UPDATING.md` | Festival update instructions | Developers |
| `CLAUDE.md` | AI assistant guide (this file) | AI assistants, future maintainers |
| `privacy.md` | Privacy policy | Users |
| `LICENSE` | BSD 3-Clause license | Legal |

### Source Files (Key Entry Points)

| File | Lines | Purpose |
|------|-------|---------|
| `app/src/main/java/ralcock/cbf/CamBeerFestApplication.java` | 362 | Main Activity, app entry point |
| `app/src/main/java/ralcock/cbf/AppPreferences.java` | 167 | SharedPreferences wrapper |
| `app/src/main/java/ralcock/cbf/service/UpdateService.java` | 7010 | Background data update service |
| `app/src/main/java/ralcock/cbf/service/UpdateTask.java` | 6700 | AsyncTask for updates |
| `libraries/beers/src/main/java/ralcock/cbf/model/Beer.java` | - | Core Beer domain model |
| `libraries/beers/src/main/java/ralcock/cbf/model/Brewery.java` | - | Core Brewery domain model |

### Test Files

| File | Purpose |
|------|---------|
| `app/tests/src/ralcock/cbf/BeersImplTest.java` | DAO layer testing |
| `app/tests/src/ralcock/cbf/BeerListTest.java` | Beer list functionality |
| `app/src/androidTest/.../CamBeerFestApplicationInstrumentedTest.java` | Instrumented UI tests |
| `libraries/beers/src/test/.../StarRatingTest.java` | Unit tests for domain models |

### Data Files

| File | Purpose |
|------|---------|
| `example-beer-list.json` | Sample beer list for testing (103KB) |
| `app/src/main/res/raw/ormlite_config` | OrmLite configuration (generated) |

---

## Common Development Tasks

### Task 1: Add a New Dependency

**Example:** Adding a new library

1. **Edit** `app/build.gradle`:
   ```gradle
   dependencies {
       implementation project(':libraries:beers')
       implementation group: 'com.j256.ormlite', name: 'ormlite-core', version: '5.0'
       // Add new dependency here
       implementation group: 'com.example', name: 'new-library', version: '1.0.0'
   }
   ```

2. **Sync Gradle:**
   ```bash
   ./gradlew build --refresh-dependencies
   ```

3. **Update ProGuard rules if needed** (`proguard.cfg`) for reflection-based libraries

### Task 2: Add a New Fragment

**Example:** Creating a new UI screen

1. **Create Fragment class:**
   ```
   app/src/main/java/ralcock/cbf/view/NewFeatureFragment.java
   ```

2. **Create layout:**
   ```
   app/src/main/res/layout/fragment_new_feature.xml
   ```

3. **Register in navigation** (if using fragment navigation)

4. **Update activity** to host the fragment

### Task 3: Modify Database Schema

**WARNING:** Database changes require careful migration handling

1. **Modify entity class** (e.g., `Beer.java` or `Brewery.java`)
   ```java
   @DatabaseField
   private String newField;  // Add new field
   ```

2. **Increment database version:**
   ```java
   // BeerDatabaseHelper.java
   private static final int DB_VERSION = 33; // cbf2025 → increment
   ```

3. **Update migration logic in `onUpgrade()`** if you need to preserve data:
   ```java
   @Override
   public void onUpgrade(final SQLiteDatabase sqLiteDatabase,
                         final ConnectionSource connectionSource,
                         int old_version, int new_version) {
       // Current implementation drops all tables
       // For data preservation, implement proper migration
   }
   ```

   **Note:** Current implementation drops all tables. This is acceptable for festival updates but consider data migration for user data.

4. **Test thoroughly:**
   ```bash
   ./gradlew connectedCheck
   ```

### Task 4: Update Android Target SDK

**Example:** Updating from API 34 to API 35

1. **Update** `app/build.gradle`:
   ```gradle
   android {
       compileSdkVersion 34  // Update this
       defaultConfig {
           targetSdkVersion 35  // Update this
       }
   }
   ```

2. **Update CI/CD** `.github/workflows/android.yml`:
   ```yaml
   strategy:
     matrix:
       api-level: [35]  # Update test API level
   ```

3. **Check for breaking changes:**
   - Review Android API changes for target SDK
   - Update deprecated API usage
   - Test permissions, behavior changes

4. **Test extensively:**
   ```bash
   ./gradlew connectedCheck
   ```

### Task 5: Fix Lint Warnings

```bash
# Run lint
./gradlew lint

# View lint report
# Located at: app/build/reports/lint-results.html

# Fix issues in code
# Common issues:
# - Missing translations
# - Unused resources
# - API version checks
# - Potential null pointer exceptions
```

### Task 6: Generate Release Build

```bash
# Ensure release.gradle exists with signing config
# Run release build
./gradlew assembleRelease -PRELEASE

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

### Task 7: Run Code Coverage

```bash
# For library module (has JaCoCo configured)
cd libraries/beers
./gradlew jacocoTestReport

# View report at:
# libraries/beers/build/reports/jacoco/test/html/index.html
```

---

## Database Schema

### ORM Framework: OrmLite

The app uses OrmLite for object-relational mapping with SQLite.

### Entities

#### Beer Entity

**Class:** `libraries/beers/src/main/java/ralcock/cbf/model/Beer.java`

**Key Fields:**
- `id` - Primary key
- `name` - Beer name
- `brewery` - Foreign key to Brewery
- `abv` - Alcohol by volume
- `style` - Beer style
- `description` - Beer description
- `starRating` - User rating (enum: UNRATED, ONE_STAR, ..., FIVE_STARS)
- `notes` - User notes
- `bookmarked` - Bookmark flag

**OrmLite Annotations:**
```java
@DatabaseTable(tableName = "beers")
public class Beer {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField(foreign = true)
    private Brewery brewery;

    @DatabaseField
    private String name;

    // ... other fields
}
```

#### Brewery Entity

**Class:** `libraries/beers/src/main/java/ralcock/cbf/model/Brewery.java`

**Key Fields:**
- `id` - Primary key
- `name` - Brewery name
- `location` - Brewery location
- `website` - Brewery website URL

### Database Helper

**Class:** `app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java`

**Key Methods:**
- `onCreate()` - Creates tables on first install
- `onUpgrade()` - Handles database migrations (currently drops and recreates)
- `getBeers()` - Returns Beer DAO
- `getBreweries()` - Returns Brewery DAO
- `deleteAll()` - Clears all tables

**Database Name:** `BEERS`
**Current Version:** `32` (as of cbf2025)

### Data Access Objects (DAOs)

#### Beers DAO

**Interface:** `libraries/beers/src/main/java/ralcock/cbf/model/dao/Beers.java`
**Implementation:** `libraries/beers/src/main/java/ralcock/cbf/model/dao/BeersImpl.java`

**Key Methods:**
- `getAll()` - Retrieve all beers
- `getById(int id)` - Retrieve specific beer
- `save(Beer beer)` - Save or update beer
- `delete(Beer beer)` - Delete beer
- `queryForMatching(Beer matchTemplate)` - Query by example

#### Breweries DAO

**Interface:** `libraries/beers/src/main/java/ralcock/cbf/model/dao/Breweries.java`
**Implementation:** `libraries/beers/src/main/java/ralcock/cbf/model/dao/BreweriesImpl.java`

### Data Loading

**Update Service:** `UpdateService.java` and `UpdateTask.java`

**Process:**
1. Download JSON from `beer_list_url` (configured in `festival.xml`)
2. Parse JSON into domain objects using `JsonBeerList`
3. Clear existing database (`deleteAll()`)
4. Populate with new data
5. Notify UI of completion

**JSON Format:** See `example-beer-list.json` for structure

---

## Testing Strategy

### Test Pyramid

```
         ┌─────────────────┐
         │  Instrumented   │  ← Few: UI/Integration tests
         │     Tests       │    (CamBeerFestApplicationInstrumentedTest)
         └─────────────────┘
              ▲
         ┌────┴────────────┐
         │   Unit Tests    │  ← Many: Fast, isolated tests
         │  (JUnit + H2)   │    (BeersImplTest, BeerListTest, etc.)
         └─────────────────┘
```

### Unit Tests

**Location:** `app/tests/src/ralcock/cbf/`

**Key Test Classes:**
- `BeersImplTest` - Tests Beer DAO with H2 in-memory database
- `BreweriesImplTest` - Tests Brewery DAO
- `BeerListTest` - Tests beer list functionality
- `JsonBeerListTest` - Tests JSON parsing
- `BeerSharerTest` - Tests share functionality
- `StarRatingTest` - Tests rating enum

**Testing Pattern:**
```java
public class BeersImplTest {
    private Beers beers;

    @Before
    public void setUp() throws Exception {
        // Setup H2 in-memory database
        // Initialize DAOs
    }

    @Test
    public void testSaveAndRetrieve() {
        // Arrange
        Beer beer = new BeerBuilder()
            .withName("Test Beer")
            .build();

        // Act
        beers.save(beer);
        Beer retrieved = beers.getById(beer.getId());

        // Assert
        assertThat(retrieved.getName(), is("Test Beer"));
    }
}
```

**Test Database:** H2 (not SQLite) for unit tests
- Faster than SQLite
- Pure Java, no native dependencies
- JDBC-compatible for OrmLite

### Instrumented Tests

**Location:** `app/src/androidTest/java/ralcock/cbf/`

**Test Runner:** `androidx.test.runner.AndroidJUnitRunner`

**Example:**
```java
@RunWith(AndroidJUnit4.class)
public class CamBeerFestApplicationInstrumentedTest {
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("ralcock.cbf", appContext.getPackageName());
    }
}
```

**Run instrumented tests:**
```bash
# Requires connected device or emulator
./gradlew connectedCheck
```

### Testing Best Practices

1. **Run unit tests frequently:**
   ```bash
   ./gradlew test
   ```

2. **Run instrumented tests before major commits:**
   ```bash
   ./gradlew connectedCheck
   ```

3. **Check test reports:**
   - Unit: `app/build/reports/tests/`
   - Instrumented: `app/build/reports/androidTests/`

4. **Write tests for new features:**
   - Unit tests for business logic
   - Instrumented tests for UI flows

5. **Maintain test coverage:**
   - Library module uses JaCoCo for coverage
   - Aim for high coverage on DAOs and business logic

---

## Release Process

### Release Build Configuration

**File:** `app/release.gradle`

This file is **not in version control** (gitignored) and contains signing configuration:

```gradle
android {
    signingConfigs {
        release {
            storeFile file(System.getenv("SIGNING_KEYSTORE"))
            storePassword System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias System.getenv("SIGNING_KEY_ALIAS")
            keyPassword System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), '../proguard.cfg'
        }
    }
}
```

### Local Release Build

**Prerequisites:**
1. Create `app/release.gradle` (see above)
2. Set environment variables:
   ```bash
   export SIGNING_KEYSTORE=/path/to/keystore.jks
   export SIGNING_STORE_PASSWORD=your_store_password
   export SIGNING_KEY_ALIAS=your_key_alias
   export SIGNING_KEY_PASSWORD=your_key_password
   ```

**Build:**
```bash
./gradlew assembleRelease -PRELEASE --scan
```

**Output:**
```
app/build/outputs/apk/release/app-release.apk
```

### CI/CD Release Build

**Triggered by:** Push to `main` branch or manual workflow dispatch

**Process:**
1. Keystore decoded from GitHub Secrets (`KEYSTORE`)
2. Signing credentials injected from secrets
3. Build executed: `./gradlew build -PRELEASE --scan`
4. APK uploaded as artifact

**Download release APK:**
1. Go to GitHub Actions workflow run
2. Download `build-artifacts`
3. Extract `app/build/outputs/apk/release/app-release.apk`

### ProGuard/R8 Obfuscation

**Configuration:** `proguard.cfg`

**Purpose:**
- Code shrinking (remove unused code)
- Code obfuscation (rename classes/methods)
- Code optimization

**Important:**
- Keep OrmLite classes (reflection-based)
- Keep Android framework callbacks
- Custom rules in `proguard.cfg`

**Testing obfuscated builds:**
```bash
./gradlew assembleRelease -PRELEASE
# Install and test on device
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Release Checklist

Before releasing a new version:

- [ ] Update version in `app/build.gradle` (versionCode and versionName)
- [ ] Update festival configuration in `festival.xml` (if new festival)
- [ ] Increment database version in `BeerDatabaseHelper.java` (if schema changed or new festival)
- [ ] Run all tests: `./gradlew test connectedCheck`
- [ ] Test release build locally
- [ ] Verify ProGuard doesn't break functionality
- [ ] Update `UPDATING.md` if process changed
- [ ] Create git tag: `git tag -a v2025.0.0.1 -m "Release for CBF 2025"`
- [ ] Push tag: `git push origin v2025.0.0.1`
- [ ] Monitor CI/CD build
- [ ] Download and test release APK from CI artifacts
- [ ] Publish to Google Play Store (if applicable)

---

## Additional Notes for AI Assistants

### Development Philosophy

This is a **mature, stable codebase** with a specific maintenance pattern:
- **Primary activity:** Annual festival updates
- **Secondary activity:** Android SDK/dependency updates
- **Tertiary activity:** Bug fixes and minor improvements

**When making changes:**
- Preserve existing patterns and conventions
- Maintain consistency with codebase style
- Avoid unnecessary refactoring
- Test thoroughly before committing

### Code Review Focus Areas

When reviewing or modifying code, pay attention to:

1. **Database version management:** Always increment when schema changes
2. **Resource updates:** Festival configuration must be consistent across files
3. **Backward compatibility:** Min SDK is 14 (very broad compatibility)
4. **ProGuard rules:** Keep rules for any reflection-based libraries
5. **Test coverage:** Maintain or improve test coverage
6. **Build configuration:** Changes to Gradle files should be minimal and justified

### Common Pitfalls

**Avoid:**
- Changing Hungarian notation (fFieldName) to standard Java conventions
- Removing "final" keywords from parameters
- Updating dependencies without testing (especially OrmLite)
- Modifying database schema without version increment
- Breaking backward compatibility with older Android versions
- Introducing new architectural patterns inconsistent with the codebase

### Helpful Context

**Project History:**
- Created around 2011-2012 (based on license)
- Updated annually since then
- Migrated from Travis CI to GitHub Actions
- Evolved from older Android practices to AndroidX

**Design Decisions:**
- OrmLite chosen for simplicity and stability
- Offline-first approach for festival environment (limited connectivity)
- Minimal external dependencies (reduces maintenance burden)
- Conservative update strategy (stability over latest features)

### When in Doubt

**Reference files:**
1. `UPDATING.md` - Annual update process
2. Recent commits - See historical patterns
3. This file (CLAUDE.md) - Comprehensive reference

**Ask questions about:**
- Breaking changes to existing functionality
- Major architectural changes
- New dependencies with significant impact
- Changes that affect all users

### Success Criteria

A successful contribution:
- Follows existing conventions
- Passes all tests (`./gradlew test connectedCheck`)
- Builds successfully (`./gradlew build`)
- Has clear commit messages
- Updates documentation if needed
- Doesn't introduce new warnings
- Maintains or improves code quality

---

## Document Maintenance

**Last Updated:** 2025-11-17
**Document Version:** 1.0.0
**Codebase Version:** 2025.0.0.1 (versionCode 27)
**Database Version:** 32 (cbf2025)

**Update this document when:**
- Major architectural changes occur
- Development workflow changes
- New tools or frameworks are adopted
- CI/CD pipeline is modified
- Build system is updated

**Document Owner:** Repository maintainers
**Feedback:** Create an issue or pull request with documentation improvements

---

## Quick Reference Card

### Essential Commands
```bash
# Build
./gradlew build

# Test
./gradlew test connectedCheck

# Release
./gradlew assembleRelease -PRELEASE --scan

# Clean
./gradlew clean
```

### Essential Files for Annual Updates
1. `app/build.gradle` (lines 30-31) - Version
2. `app/src/main/res/values/festival.xml` - Festival config
3. `app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java` (line 19) - DB version

### Essential Patterns
- Fields: `fFieldName`
- Constants: `CONSTANT_NAME`
- Parameters: `final Type parameterName`
- Exceptions: Wrap `SQLException` in `RuntimeException`

### Essential Commands
```bash
# Find all Gradle tasks
./gradlew tasks

# Check dependencies
./gradlew dependencies

# View project structure
./gradlew projects
```

---

**END OF CLAUDE.MD**
