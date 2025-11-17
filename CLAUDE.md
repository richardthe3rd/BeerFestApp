# CLAUDE.md - AI Assistant Guide for BeerFestApp

This document provides comprehensive guidance for AI assistants working with the Cambridge Beer Festival Android application codebase.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Known Issues and Limitations](#known-issues-and-limitations)
3. [Architecture and Structure](#architecture-and-structure)
4. [Tech Stack](#tech-stack)
5. [Development Setup](#development-setup)
6. [Build and Test](#build-and-test)
7. [Annual Festival Update Workflow](#annual-festival-update-workflow)
8. [Coding Conventions](#coding-conventions)
9. [CI/CD Pipeline](#cicd-pipeline)
10. [Key Files Reference](#key-files-reference)
11. [Common Development Tasks](#common-development-tasks)
12. [Database Schema](#database-schema)
13. [Testing Strategy](#testing-strategy)
14. [Release Process](#release-process)
15. [Troubleshooting](#troubleshooting)

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

## Known Issues and Limitations

### Critical Pain Points

#### 1. Annual Update Complexity
**Problem:** Updating the app each year requires manual changes to multiple files:
- Version codes in `build.gradle`
- Festival year in `festival.xml` (multiple locations)
- Database version in `BeerDatabaseHelper.java`

**Impact:**
- Error-prone manual process
- Easy to miss a file or forget to increment DB version
- No automation to prevent mistakes

**Improvement Opportunities:**
- Create a script to automate annual updates (see [Common Development Tasks](#common-development-tasks))
- Add pre-commit hooks to validate version consistency
- Consider externalizing configuration to a single source of truth
- **RECOMMENDED: Dynamic festival loading** (see below for detailed design)

#### 2. Insufficient Testing
**Problem:** Testing coverage is inadequate for production use:
- Limited instrumented tests (only 1 basic test)
- No UI automation tests for critical flows
- No integration tests for data updates
- Manual testing required for each release

**Impact:**
- Crashes in production
- ANRs (Application Not Responding) reported by users
- Stale beer lists not detected before release

**User Reports:**
- "App crashes when opening beer details"
- "App becomes unresponsive during data download"
- "Beer list shows old data from last year"

**Improvement Opportunities:**
- Add comprehensive Espresso tests for all UI flows
- Add integration tests for UpdateService/UpdateTask
- Test network timeout scenarios
- Add automated tests for database migrations
- Implement crash reporting (Firebase Crashlytics, Sentry)
- Add proper loading states and error handling in UI

#### 3. Stale Beer List Issue
**Problem:** Users report seeing old festival data even after updates are released.

**Root Causes:**
- Database migration may fail silently
- Update service may not run automatically
- Network failures during download not handled properly
- No user feedback when updates fail

**Improvement Opportunities:**
- Add explicit version checking in UpdateService
- Show update status in UI (last updated timestamp)
- Implement retry logic with exponential backoff
- Add manual "Force Update" option in settings
- Log update failures for debugging

#### 4. Beverage Type Limitation
**Problem:** App only displays ales/beers, but festival also features cider and mead.

**User Feedback:**
- "Where are the ciders?"
- "Can't find mead section"
- "Only showing beer, not other festival drinks"

**Impact:**
- Incomplete festival coverage
- Reduced app utility for cider/mead enthusiasts
- Negative user reviews

**Technical Requirements for Fix:**
- Update data model to support beverage type field
- Modify UI to filter/group by beverage type (Beer, Cider, Mead)
- Update JSON feed parsing to include type
- Add navigation tabs or sections for each beverage type
- Coordinate with festival data provider to include type in JSON

**Files to Modify:**
- `libraries/beers/src/main/java/ralcock/cbf/model/Beer.java` - Add beverage type field
- `BeerDatabaseHelper.java` - Increment DB version for schema change
- UI fragments to support filtering/grouping
- JSON parser to handle new field

#### 5. Crash and ANR Reports
**Problem:** Users report crashes and "Application Not Responding" errors.

**Likely Causes:**
- Network operations on main thread
- Large dataset processing blocking UI
- Database operations on main thread
- Memory leaks in long-running activities

**Improvement Opportunities:**
- Audit all network calls (ensure background threads)
- Use WorkManager instead of deprecated AsyncTask
- Implement pagination for large beer lists
- Add proper cancellation for background tasks
- Use RecyclerView optimizations (DiffUtil)
- Implement ProGuard/R8 properly to catch issues early
- Add StrictMode in debug builds to detect violations

### Known Limitations

| Limitation | Impact | Workaround |
|------------|--------|------------|
| No offline beer list included | First run requires network | Could bundle previous year's data |
| No push notifications for updates | Users don't know when new data available | Add in-app update check on launch |
| No search suggestions | Search less discoverable | Add autocomplete or popular searches |
| No multi-language support | English only | Festival is UK-based, low priority |
| Min SDK 14 (old) | Maintenance burden for old APIs | Consider raising to SDK 21+ |
| No dark mode | User preference ignored | Add theme support |
| Dated UI/UX design | Looks outdated compared to modern apps | Material Design 3 refresh |

### UI/UX Modernization

**Current Status:** App uses older Material Design patterns (pre-2020 design language)

**Modern UI Improvements Needed:**

#### 1. Material Design 3 (Material You)
```gradle
// Upgrade to Material 3
implementation 'com.google.android.material:material:1.11.0'
```

**Key Changes:**
- **Dynamic color theming** - Adapts to user's wallpaper (Android 12+)
- **Updated components** - Filled buttons, outlined cards, elevated surfaces
- **New typography scale** - Better readability
- **Motion and transitions** - Smoother animations

**Example Updates:**
```xml
<!-- OLD: Material Design 2 -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.MaterialComponents.Button" />

<!-- NEW: Material Design 3 -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button.FilledTonal" />
```

#### 2. Dark Mode Support
**User Benefit:** Reduce eye strain, battery savings on OLED screens

**Implementation:**
```xml
<!-- res/values/themes.xml -->
<style name="Theme.BeerFest" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/beer_amber</item>
    <item name="colorOnPrimary">@color/white</item>
    <item name="android:statusBarColor">?attr/colorPrimaryVariant</item>
</style>

<!-- res/values-night/themes.xml -->
<style name="Theme.BeerFest" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/beer_amber_dark</item>
    <item name="colorSurface">@color/dark_surface</item>
</style>
```

#### 3. Modern Layout Patterns

**Replace:**
- ListView → RecyclerView with modern adapters
- FragmentTransaction → Navigation Component
- AsyncTask → Coroutines or WorkManager
- Manual state handling → ViewModel + LiveData

**Example: Beer List Modernization**
```kotlin
// Convert to Kotlin + modern architecture
class BeerListViewModel : ViewModel() {
    private val repository = BeerRepository()

    val beers: LiveData<List<Beer>> = repository.getBeers()

    val filteredBeers: LiveData<List<Beer>> = beers.map { list ->
        list.filter { it.matchesCurrentFilter() }
    }
}

class BeerListFragment : Fragment() {
    private val viewModel: BeerListViewModel by viewModels()
    private lateinit var binding: FragmentBeerListBinding

    override fun onCreateView(...): View {
        binding = FragmentBeerListBinding.inflate(inflater)

        // Modern list with DiffUtil for smooth updates
        val adapter = BeerListAdapter()
        binding.recyclerView.adapter = adapter

        viewModel.filteredBeers.observe(viewLifecycleOwner) { beers ->
            adapter.submitList(beers)
        }

        return binding.root
    }
}
```

#### 4. Enhanced Beer Card Design

**Current:** Basic list items
**Modern:** Rich cards with imagery and information hierarchy

```xml
<!-- Modern beer card with Material 3 -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardElevation="2dp"
    app:cardCornerRadius="16dp"
    app:strokeWidth="0dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:padding="16dp">

        <!-- Beer style icon/badge -->
        <com.google.android.material.chip.Chip
            android:id="@+id/styleChip"
            style="@style/Widget.Material3.Chip.Suggestion"
            android:text="IPA" />

        <!-- Beer name - prominent -->
        <TextView
            android:id="@+id/beerName"
            android:textAppearance="?attr/textAppearanceTitleMedium"
            android:textSize="18sp"
            android:textStyle="bold" />

        <!-- Brewery - secondary -->
        <TextView
            android:id="@+id/breweryName"
            android:textAppearance="?attr/textAppearanceBodyMedium"
            android:textColor="?attr/colorOnSurfaceVariant" />

        <!-- ABV with icon -->
        <LinearLayout>
            <ImageView
                android:src="@drawable/ic_alcohol"
                android:tint="?attr/colorPrimary" />
            <TextView
                android:id="@+id/abv"
                android:text="5.2% ABV" />
        </LinearLayout>

        <!-- Rating stars (interactive) -->
        <com.google.android.material.rating.RatingBar
            android:id="@+id/ratingBar"
            style="@style/Widget.Material3.RatingBar.Small" />

        <!-- Bookmark FAB (floating action button) -->
        <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
            android:id="@+id/bookmarkFab"
            app:icon="@drawable/ic_bookmark_border"
            android:text="Save" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</com.google.android.material.card.MaterialCardView>
```

#### 5. Modern Navigation

**Replace:** Custom fragments with Navigation Component

```gradle
// Add Navigation Component
implementation "androidx.navigation:navigation-fragment:2.7.6"
implementation "androidx.navigation:navigation-ui:2.7.6"
```

```xml
<!-- res/navigation/nav_graph.xml -->
<navigation>
    <fragment
        android:id="@+id/festivalSelectionFragment"
        android:name="ralcock.cbf.view.FestivalSelectionFragment">
        <action
            android:id="@+id/action_selectFestival"
            app:destination="@id/beerListFragment" />
    </fragment>

    <fragment
        android:id="@+id/beerListFragment"
        android:name="ralcock.cbf.view.BeerListFragment">
        <argument
            android:name="festivalId"
            app:argType="string" />
        <action
            android:id="@+id/action_viewBeerDetails"
            app:destination="@id/beerDetailsFragment" />
    </fragment>
</navigation>
```

#### 6. Visual Enhancements

**Add:**
- **Beer style color coding** - Different colors for IPA, Stout, Lager, etc.
- **ABV visualization** - Progress bar or gauge
- **Brewery logos** - If available in data feed
- **Search with suggestions** - Material SearchView with autocomplete
- **Smooth animations** - Shared element transitions between screens
- **Empty states** - Beautiful illustrations when no results
- **Loading states** - Skeleton screens instead of blank/spinner

**Example: ABV Gauge**
```xml
<com.google.android.material.progressindicator.CircularProgressIndicator
    android:id="@+id/abvGauge"
    android:layout_width="48dp"
    android:layout_height="48dp"
    app:indicatorColor="?attr/colorPrimary"
    app:trackColor="?attr/colorSurfaceVariant"
    app:trackThickness="4dp" />
```

#### 7. Accessibility Improvements

```xml
<!-- Add content descriptions -->
<ImageView
    android:contentDescription="@string/bookmark_beer" />

<!-- Increase touch targets -->
<Button
    android:minWidth="48dp"
    android:minHeight="48dp" />

<!-- Support screen readers -->
<TextView
    android:importantForAccessibility="yes"
    android:accessibilityTraversalBefore="@id/nextElement" />
```

#### 8. Bottom Navigation (if adding more features)

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    app:menu="@menu/bottom_nav_menu">

    <!-- Menu items -->
    <item
        android:id="@+id/navigation_festivals"
        android:icon="@drawable/ic_festival"
        android:title="@string/festivals" />

    <item
        android:id="@+id/navigation_beers"
        android:icon="@drawable/ic_beer"
        android:title="@string/beers" />

    <item
        android:id="@+id/navigation_bookmarks"
        android:icon="@drawable/ic_bookmark"
        android:title="@string/bookmarks" />

    <item
        android:id="@+id/navigation_settings"
        android:icon="@drawable/ic_settings"
        android:title="@string/settings" />
</com.google.android.material.bottomnavigation.BottomNavigationView>
```

#### 9. Splash Screen (Android 12+)

```xml
<!-- res/values/themes.xml -->
<style name="Theme.BeerFest.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">?attr/colorPrimary</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/app_icon</item>
    <item name="postSplashScreenTheme">@style/Theme.BeerFest</item>
</style>
```

#### 10. Typography Refresh

```xml
<!-- res/values/type.xml -->
<resources>
    <!-- Material 3 type scale -->
    <style name="TextAppearance.BeerFest.DisplayLarge" parent="TextAppearance.Material3.DisplayLarge">
        <item name="fontFamily">@font/inter_bold</item>
    </style>

    <style name="TextAppearance.BeerFest.TitleMedium" parent="TextAppearance.Material3.TitleMedium">
        <item name="fontFamily">@font/inter_medium</item>
    </style>

    <style name="TextAppearance.BeerFest.BodyLarge" parent="TextAppearance.Material3.BodyLarge">
        <item name="fontFamily">@font/inter_regular</item>
    </style>
</resources>
```

#### Implementation Priority

**Phase 1: Foundation (Quick Wins)**
1. Upgrade to Material 3 library
2. Add dark mode support
3. Update button and card styles
4. Improve beer list item design

**Phase 2: Architecture (Medium Effort)**
5. Migrate to ViewModels and LiveData
6. Replace AsyncTask with WorkManager
7. Add Navigation Component
8. Implement proper loading states

**Phase 3: Polish (Nice to Have)**
9. Custom animations and transitions
10. Brewery logos and beer style icons
11. Advanced filtering UI
12. Accessibility enhancements

#### Estimated Effort
- **Phase 1 (Quick wins):** 1-2 weeks
- **Phase 2 (Architecture):** 3-4 weeks
- **Phase 3 (Polish):** 2-3 weeks
- **Total:** 6-9 weeks for complete modernization

#### Design Resources
- [Material Design 3 Guidelines](https://m3.material.io/)
- [Android Design Guidelines](https://developer.android.com/design)
- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)

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

### Automation Suggestions

**PROBLEM:** The manual update process is error-prone and time-consuming.

**SOLUTION:** Create a script to automate the annual update process:

**Option 1: Bash Script** (`scripts/update-festival-year.sh`)

```bash
#!/bin/bash
# Usage: ./scripts/update-festival-year.sh 2026

set -e

YEAR=$1
if [ -z "$YEAR" ]; then
    echo "Usage: $0 <year>"
    echo "Example: $0 2026"
    exit 1
fi

echo "Updating BeerFestApp for CBF $YEAR..."

# 1. Update build.gradle version
BUILD_GRADLE="app/build.gradle"
CURRENT_VERSION_CODE=$(grep "versionCode" $BUILD_GRADLE | sed 's/[^0-9]*//g')
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

sed -i "s/versionCode $CURRENT_VERSION_CODE/versionCode $NEW_VERSION_CODE/" $BUILD_GRADLE
sed -i "s/versionName \"[0-9]\{4\}\.[0-9.]*\"/versionName \"$YEAR.0.0.1\"/" $BUILD_GRADLE

echo "✓ Updated $BUILD_GRADLE"

# 2. Update festival.xml
FESTIVAL_XML="app/src/main/res/values/festival.xml"
sed -i "s/Cambridge Beer Festival [0-9]\{4\}/Cambridge Beer Festival $YEAR/" $FESTIVAL_XML
sed -i "s/cbf[0-9]\{4\}/cbf$YEAR/g" $FESTIVAL_XML

echo "✓ Updated $FESTIVAL_XML"

# 3. Update BeerDatabaseHelper.java
DB_HELPER="app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java"
CURRENT_DB_VERSION=$(grep "DB_VERSION = " $DB_HELPER | sed 's/[^0-9]*//g' | head -1)
NEW_DB_VERSION=$((CURRENT_DB_VERSION + 1))

sed -i "s/DB_VERSION = $CURRENT_DB_VERSION; \/\/ cbf[0-9]\{4\}/DB_VERSION = $NEW_DB_VERSION; \/\/ cbf$YEAR/" $DB_HELPER

echo "✓ Updated $DB_HELPER"

echo ""
echo "Summary of changes:"
echo "  - Version code: $CURRENT_VERSION_CODE → $NEW_VERSION_CODE"
echo "  - Version name: → $YEAR.0.0.1"
echo "  - DB version: $CURRENT_DB_VERSION → $NEW_DB_VERSION"
echo "  - Festival year: → $YEAR"
echo ""
echo "Next steps:"
echo "  1. Review changes: git diff"
echo "  2. Test: ./gradlew clean test"
echo "  3. Commit: git commit -am 'cbf$YEAR'"
```

**Option 2: Gradle Task** (add to `app/build.gradle`)

```gradle
task updateFestivalYear {
    doLast {
        def year = project.findProperty('year')
        if (!year) {
            throw new GradleException("Usage: ./gradlew updateFestivalYear -Pyear=2026")
        }

        println "Updating for CBF ${year}..."

        // Update festival.xml
        def festivalXml = file('src/main/res/values/festival.xml')
        def content = festivalXml.text
        content = content.replaceAll(/Cambridge Beer Festival \d{4}/, "Cambridge Beer Festival ${year}")
        content = content.replaceAll(/cbf\d{4}/, "cbf${year}")
        festivalXml.text = content

        println "✓ Updated festival.xml"
        println "⚠ Remember to manually update:"
        println "  - versionCode and versionName in build.gradle"
        println "  - DB_VERSION in BeerDatabaseHelper.java"
    }
}
```

**Option 3: Pre-commit Hook** (`.git/hooks/pre-commit`)

```bash
#!/bin/bash
# Validate version consistency before commit

BUILD_GRADLE="app/build.gradle"
FESTIVAL_XML="app/src/main/res/values/festival.xml"
DB_HELPER="app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java"

# Extract years from different files
VERSION_YEAR=$(grep "versionName" $BUILD_GRADLE | sed 's/.*"\([0-9]\{4\}\).*/\1/')
FESTIVAL_YEAR=$(grep "festival_name" $FESTIVAL_XML | sed 's/.*Festival \([0-9]\{4\}\).*/\1/')
HASHTAG_YEAR=$(grep "festival_hashtag" $FESTIVAL_XML | sed 's/.*cbf\([0-9]\{4\}\).*/\1/')
URL_YEAR=$(grep "beer_list_url" $FESTIVAL_XML | sed 's/.*cbf\([0-9]\{4\}\).*/\1/')
DB_YEAR=$(grep "DB_VERSION.*cbf" $DB_HELPER | sed 's/.*cbf\([0-9]\{4\}\).*/\1/')

# Check consistency
if [ "$VERSION_YEAR" != "$FESTIVAL_YEAR" ] || \
   [ "$VERSION_YEAR" != "$HASHTAG_YEAR" ] || \
   [ "$VERSION_YEAR" != "$URL_YEAR" ] || \
   [ "$VERSION_YEAR" != "$DB_YEAR" ]; then
    echo "ERROR: Year mismatch detected!"
    echo "  build.gradle version:   $VERSION_YEAR"
    echo "  festival.xml name:      $FESTIVAL_YEAR"
    echo "  festival.xml hashtag:   $HASHTAG_YEAR"
    echo "  festival.xml URL:       $URL_YEAR"
    echo "  BeerDatabaseHelper:     $DB_YEAR"
    echo ""
    echo "All years must match. Please fix before committing."
    exit 1
fi

echo "✓ Version consistency check passed (CBF $VERSION_YEAR)"
```

### Future-Proof Solution: Dynamic Festival Loading

**VISION:** Eliminate annual app releases by loading festival configurations dynamically from a remote JSON resource.

#### Current vs. Proposed Architecture

**Current (Manual Release Each Year):**
```
User Downloads App → Hardcoded 2025 Config → Downloads cbf2025 beer list
                   → Next year: NEW APP RELEASE REQUIRED
```

**Proposed (Dynamic Multi-Festival):**
```
User Downloads App → Fetches festival list JSON → Shows available festivals
                   → User selects CBF 2025 → Downloads cbf2025 beer list
                   → User selects CBF 2024 → Shows cached 2024 data
                   → CBF 2026 added to JSON → Automatically appears in app!
```

#### Implementation Design

**1. Festival Catalog JSON** (hosted at centralized URL)

```json
{
  "festivals": [
    {
      "id": "cbf2026",
      "name": "Cambridge Beer Festival 2026",
      "year": 2026,
      "hashtag": "cbf2026",
      "website": "https://www.cambridgebeerfestival.com/",
      "beerListUrl": "https://data.cambridgebeerfestival.com/cbf2026/beer.json",
      "startDate": "2026-05-25",
      "endDate": "2026-05-30",
      "active": true
    },
    {
      "id": "cbf2025",
      "name": "Cambridge Beer Festival 2025",
      "year": 2025,
      "hashtag": "cbf2025",
      "website": "https://www.cambridgebeerfestival.com/",
      "beerListUrl": "https://data.cambridgebeerfestival.com/cbf2025/beer.json",
      "startDate": "2025-05-26",
      "endDate": "2025-05-31",
      "active": true
    },
    {
      "id": "cbf2024",
      "name": "Cambridge Beer Festival 2024",
      "year": 2024,
      "hashtag": "cbf2024",
      "website": "https://www.cambridgebeerfestival.com/",
      "beerListUrl": "https://data.cambridgebeerfestival.com/cbf2024/beer.json",
      "startDate": "2024-05-20",
      "endDate": "2024-05-25",
      "active": false,
      "archived": true
    }
  ],
  "catalogVersion": 3,
  "lastUpdated": "2026-01-15T10:00:00Z"
}
```

**Catalog URL:**
```java
// In festival.xml or AppPreferences
public static final String FESTIVAL_CATALOG_URL =
    "https://data.cambridgebeerfestival.com/festivals.json";
```

**2. Database Schema Changes**

```java
// Add Festival table
@DatabaseTable(tableName = "festivals")
public class Festival {
    @DatabaseField(id = true)
    private String id; // "cbf2026"

    @DatabaseField
    private String name;

    @DatabaseField
    private int year;

    @DatabaseField
    private String hashtag;

    @DatabaseField
    private String beerListUrl;

    @DatabaseField
    private String startDate;

    @DatabaseField
    private String endDate;

    @DatabaseField
    private boolean active;

    @DatabaseField
    private boolean archived;

    @DatabaseField
    private long lastDownloaded; // Timestamp
}

// Update Beer table to link to Festival
@DatabaseTable(tableName = "beers")
public class Beer {
    // ... existing fields ...

    @DatabaseField(foreign = true)
    private Festival festival; // Link beer to festival
}
```

**3. UI Changes**

```java
// New: Festival Selection Fragment
public class FestivalSelectionFragment extends Fragment {
    // Shows list of available festivals
    // - Current festival (highlighted)
    // - Past festivals (if data downloaded)
    // - Future festivals (if announced)

    private void loadFestivals() {
        // Fetch from FESTIVAL_CATALOG_URL
        // Display in RecyclerView
    }

    private void onFestivalSelected(Festival festival) {
        // Download beer list for selected festival
        // Switch database context to that festival
    }
}

// Updated: Main Activity
public class CamBeerFestApplication extends AppCompatActivity {
    private Festival currentFestival;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load festival catalog
        loadFestivalCatalog();

        // Determine current/default festival
        currentFestival = getCurrentFestival();

        // Load beer list for current festival
        loadBeersForFestival(currentFestival);
    }

    private Festival getCurrentFestival() {
        // 1. Check if user has manually selected a festival
        // 2. Otherwise, use most recent active festival
        // 3. Fall back to latest festival in catalog
    }
}
```

**4. Data Persistence Strategy**

```java
// Keep historical data from previous festivals
public class MultiFestivalDatabaseHelper extends OrmLiteSqliteOpenHelper {

    // Option A: Single database with festival FK (RECOMMENDED)
    public Beers getBeersForFestival(String festivalId) {
        return beers.queryForFestival(festivalId);
    }

    // Option B: Separate database per festival
    public BeerDatabaseHelper getHelperForFestival(String festivalId) {
        String dbName = "BEERS_" + festivalId; // BEERS_cbf2025
        return new BeerDatabaseHelper(context, dbName);
    }
}
```

**5. Migration Path**

```java
// Backward compatibility: migrate existing data
@Override
public void onUpgrade(SQLiteDatabase db, ConnectionSource conn,
                      int oldVersion, int newVersion) {
    if (oldVersion < 33) { // Adding festival support
        // 1. Create Festival table
        TableUtils.createTable(conn, Festival.class);

        // 2. Create a "cbf2025" festival entry for existing data
        Festival cbf2025 = new Festival();
        cbf2025.setId("cbf2025");
        cbf2025.setName("Cambridge Beer Festival 2025");
        // ... set other fields ...

        Festivals festivals = getDao(Festival.class);
        festivals.create(cbf2025);

        // 3. Add festival_id column to beers table
        db.execSQL("ALTER TABLE beers ADD COLUMN festival_id TEXT");

        // 4. Link all existing beers to cbf2025
        db.execSQL("UPDATE beers SET festival_id = 'cbf2025'");
    }
}
```

**6. Update Service Changes**

```java
// New: Festival Catalog Update Service
public class FestivalCatalogUpdateService extends IntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // 1. Download festivals.json
            String json = downloadCatalog(FESTIVAL_CATALOG_URL);

            // 2. Parse festival list
            FestivalCatalog catalog = parseCatalog(json);

            // 3. Save to database
            saveFestivals(catalog.getFestivals());

            // 4. Check if current festival needs update
            Festival current = getCurrentFestival();
            if (current.needsUpdate()) {
                // Download beer list for current festival
                downloadBeerList(current);
            }

            // 5. Notify UI
            broadcastCatalogUpdated();

        } catch (Exception e) {
            Log.e(TAG, "Failed to update festival catalog", e);
        }
    }
}

// Updated: Beer List Update Service
public class BeerListUpdateService extends IntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        String festivalId = intent.getStringExtra("festival_id");
        Festival festival = getFestival(festivalId);

        // Download beer list for specific festival
        downloadAndSaveBeers(festival);
    }
}
```

#### Benefits

**For Users:**
- ✅ No app updates required when new festival announced
- ✅ Browse historical festival data (past years)
- ✅ Compare beers across different years
- ✅ App automatically shows new festivals

**For Developers:**
- ✅ No annual app releases required
- ✅ Update festival data via JSON file only
- ✅ No Play Store review delays
- ✅ Instant updates worldwide
- ✅ A/B test different festivals
- ✅ Support multiple festivals simultaneously

**For Maintenance:**
- ✅ Eliminates manual version updates
- ✅ No database version increment needed
- ✅ Centralized configuration management
- ✅ Rollback capability (edit JSON)

#### Rollout Strategy

**Phase 1: Foundation (v2.0)**
- Add Festival model and table
- Migrate existing cbf2025 data to festival-aware schema
- Internal festival selector (hidden UI)
- Test with cbf2024 and cbf2025 data

**Phase 2: Dynamic Loading (v2.1)**
- Implement festival catalog download
- Add FestivalSelectionFragment UI
- Update UpdateService for multi-festival support
- Release with cbf2024 and cbf2025 pre-loaded

**Phase 3: Full Production (v2.2)**
- Remove hardcoded festival.xml (keep catalog URL only)
- Add automatic catalog refresh
- Historical data retention settings
- User can browse all past festivals

#### Annual Update Process (New)

**Before (Current):**
```bash
1. Update build.gradle version
2. Update festival.xml (3 places)
3. Update BeerDatabaseHelper DB_VERSION
4. Test, commit, build, release to Play Store
5. Wait for review (1-7 days)
6. Users update app
Total time: 1-2 weeks
```

**After (Dynamic):**
```bash
1. Add new entry to festivals.json
2. Upload to data.cambridgebeerfestival.com
3. App auto-fetches and shows new festival
Total time: 5 minutes, instant worldwide
```

#### Files to Modify

**New Files:**
- `libraries/beers/src/main/java/ralcock/cbf/model/Festival.java`
- `libraries/beers/src/main/java/ralcock/cbf/model/FestivalCatalog.java`
- `libraries/beers/src/main/java/ralcock/cbf/model/dao/Festivals.java`
- `app/src/main/java/ralcock/cbf/service/FestivalCatalogUpdateService.java`
- `app/src/main/java/ralcock/cbf/view/FestivalSelectionFragment.java`

**Modified Files:**
- `Beer.java` - Add festival foreign key
- `BeerDatabaseHelper.java` - Add Festival table, migration logic
- `CamBeerFestApplication.java` - Festival selection support
- `UpdateService.java` - Multi-festival awareness
- `festival.xml` - Keep only catalog URL

#### Risks and Mitigation

| Risk | Mitigation |
|------|------------|
| Catalog URL unreachable | Bundle festivals.json in app as fallback |
| Invalid JSON format | Validate with JSON schema, catch parse errors |
| Database migration failure | Extensive testing, backup/restore mechanism |
| Increased app complexity | Phased rollout, comprehensive documentation |
| User confusion | Clear UI, default to current festival |
| Increased database size | Add cleanup for very old festivals (>3 years) |

#### Estimated Effort

- Database schema changes: 2-3 days
- Festival catalog service: 2-3 days
- UI changes: 3-4 days
- Migration logic: 2-3 days
- Testing: 3-4 days
- **Total: 12-17 days** (2-3 weeks of development)

**ROI:** One-time 2-3 week investment eliminates all future annual release work (saves ~1 week/year forever)

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

## Troubleshooting

### User-Reported Issues

This section documents common issues reported by users and how to diagnose/fix them.

#### Issue 1: App Crashes on Launch or Beer Details

**Symptoms:**
- App crashes when opening
- Crashes when tapping on a beer to view details
- "Unfortunately, BeerFestApp has stopped"

**Possible Causes:**
1. **Null pointer exceptions** in data binding
2. **Database corruption** or migration failure
3. **Missing data** in Beer or Brewery objects
4. **ProGuard stripping** required classes in release builds

**Debugging Steps:**
```bash
# 1. Check logcat for stack traces
adb logcat | grep -i "exception\|error\|crash"

# 2. Run debug build to get full stack traces
./gradlew installDebug

# 3. Check database state
adb shell
run-as ralcock.cbf
cd databases
sqlite3 BEERS
.schema
SELECT COUNT(*) FROM beers;
SELECT COUNT(*) FROM breweries;
.quit
```

**Common Fixes:**
1. **Add null checks** in UI code:
   ```java
   if (beer != null && beer.getBrewery() != null) {
       breweryName.setText(beer.getBrewery().getName());
   }
   ```

2. **Fix ProGuard rules** in `proguard.cfg`:
   ```
   # Keep OrmLite models
   -keep class ralcock.cbf.model.** { *; }

   # Keep Builder classes
   -keep class **Builder { *; }
   ```

3. **Clear app data** if database corrupted:
   ```bash
   adb shell pm clear ralcock.cbf
   ```

#### Issue 2: ANR (Application Not Responding)

**Symptoms:**
- App freezes/hangs
- "App is not responding" dialog
- UI becomes unresponsive during beer list download

**Root Causes:**
- Network operations on main thread
- Large dataset processing blocking UI
- Database operations on main thread

**Diagnosis:**
```bash
# Pull ANR traces from device
adb pull /data/anr/traces.txt

# Enable StrictMode in debug builds (add to CamBeerFestApplication.onCreate)
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build());
}
```

**Files to Audit:**
- `UpdateService.java:7010` - Check for main thread operations
- `UpdateTask.java:6700` - Ensure proper AsyncTask usage
- `BeerListFragment.java` - Check data loading
- `BeerAccessor.java` - Database operations

**Solutions:**
1. **Move network to background:**
   ```java
   // BAD: Network on main thread
   URL url = new URL(beerListUrl);
   connection = url.openConnection();

   // GOOD: Use AsyncTask or WorkManager
   new UpdateTask().execute(beerListUrl);
   ```

2. **Use WorkManager** instead of deprecated AsyncTask:
   ```gradle
   // Add to app/build.gradle
   implementation 'androidx.work:work-runtime:2.8.1'
   ```

3. **Paginate large lists:**
   - Implement RecyclerView pagination
   - Load beers in batches of 50-100

#### Issue 3: Stale Beer List (Old Data Showing)

**Symptoms:**
- Beer list shows data from previous year
- "Why am I seeing 2024 beers when it's 2025?"
- Update button doesn't work

**Diagnosis:**
```bash
# Check app preferences for last update time
adb shell
run-as ralcock.cbf
cat shared_prefs/ralcock.cbf_preferences.xml

# Check current database version
adb shell
run-as ralcock.cbf
cd databases
sqlite3 BEERS
PRAGMA user_version;
.quit
```

**Root Causes:**
1. **Database version not incremented** - Migration didn't run
2. **Update service not triggered** - Background service disabled
3. **Network failure** - Download failed silently
4. **URL not updated** - Still pointing to old year

**Solutions:**
1. **Verify database version was incremented:**
   ```java
   // In BeerDatabaseHelper.java
   private static final int DB_VERSION = 33; // cbf2026 ← Must increment!
   ```

2. **Add explicit update check in UI:**
   ```java
   // Show last update time in settings
   SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
   long lastUpdate = prefs.getLong("last_beer_update", 0);
   if (lastUpdate > 0) {
       String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(lastUpdate));
       lastUpdateText.setText("Last updated: " + date);
   }
   ```

3. **Add manual "Force Update" button:**
   ```java
   forceUpdateButton.setOnClickListener(v -> {
       // Clear database
       BeerDatabaseHelper helper = getHelper();
       helper.deleteAll();

       // Trigger update
       Intent intent = new Intent(this, UpdateService.class);
       startService(intent);
   });
   ```

4. **Verify festival.xml was updated:**
   ```bash
   grep beer_list_url app/src/main/res/values/festival.xml
   # Should show: https://data.cambridgebeerfestival.com/cbf2026/beer.json
   ```

#### Issue 4: Missing Ciders and Mead

**Symptoms:**
- Users complain "Where are the ciders?"
- Only beers showing up
- Incomplete festival coverage

**Current Status:**
- **Known limitation** - App name is "BeerFestApp", data model only handles beer
- Festival includes cider and mead but app doesn't display them
- JSON feed may or may not include these beverage types

**Temporary Workarounds:**
1. **Check if JSON includes cider/mead:**
   ```bash
   curl https://data.cambridgebeerfestival.com/cbf2025/beer.json | jq '.beers[] | select(.style | contains("Cider"))'
   ```

2. **If data exists, it's displayed as "beer"** - no filtering by type

**Long-term Solution:**
See [Known Issues - Beverage Type Limitation](#4-beverage-type-limitation) for implementation plan.

#### Issue 5: Share Function Not Working / Incomplete

**Symptoms:**
- Share button doesn't work
- Wrong hashtag in shared posts
- Share text incomplete
- **BUG: Share widget from beer details view doesn't show all sharing options** (unlike long press on list)

**Diagnosis:**
```bash
# Check festival.xml configuration
grep festival_hashtag app/src/main/res/values/festival.xml
# Should output: <string name="festival_hashtag">cbf2025</string>

# Check BeerSharer implementation
grep -n "createChooser\|Intent.ACTION_SEND" app/src/main/java/ralcock/cbf/actions/BeerSharer.java
```

**Root Cause of Sharing Options Bug:**
The share intent from beer details view may not be using `Intent.createChooser()` properly, limiting available share targets.

**Common Fixes:**
1. **Update hashtag** in `festival.xml` for new year

2. **Check BeerSharer.java** for null checks:
   ```java
   if (beer == null || beer.getName() == null) {
       return; // Don't share incomplete data
   }
   ```

3. **Fix share intent to show all options:**
   ```java
   // INCORRECT: Limited sharing options
   Intent shareIntent = new Intent(Intent.ACTION_SEND);
   shareIntent.setType("text/plain");
   startActivity(shareIntent); // Missing chooser!

   // CORRECT: Shows all available share targets
   Intent shareIntent = new Intent(Intent.ACTION_SEND);
   shareIntent.setType("text/plain");
   shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
   shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);

   Intent chooser = Intent.createChooser(shareIntent, "Share beer via...");
   if (shareIntent.resolveActivity(getPackageManager()) != null) {
       startActivity(chooser);
   }
   ```

4. **Ensure consistency** between list long-press and details view share:
   ```java
   // Extract to common method in BeerSharer
   public Intent createShareIntent(Beer beer) {
       String shareText = formatShareText(beer);
       Intent intent = new Intent(Intent.ACTION_SEND);
       intent.setType("text/plain");
       intent.putExtra(Intent.EXTRA_TEXT, shareText);
       return Intent.createChooser(intent, "Share beer");
   }
   ```

**Files to Check:**
- `BeerSharer.java` - Share action implementation
- `BeerDetailsFragment.java` - Share button click handler
- `BeerListFragment.java` - Long-press share handler (working version)

#### Issue 6: Build Failures in CI/CD

**Symptoms:**
- GitHub Actions build fails
- "Keystore not found" error
- Tests timeout

**Solutions:**
1. **Keystore issues:**
   ```yaml
   # Verify secrets are set in GitHub repo settings:
   # - KEYSTORE (base64 encoded)
   # - SIGNING_KEY_ALIAS
   # - SIGNING_KEY_PASSWORD
   # - SIGNING_STORE_PASSWORD
   ```

2. **Test timeouts:**
   ```yaml
   # In .github/workflows/android.yml
   # Increase AVD disk size if needed:
   disk-size: 8000M  # Increase from 6000M
   ```

3. **Gradle build failures:**
   ```bash
   # Clear Gradle cache locally
   rm -rf ~/.gradle/caches
   ./gradlew clean build --refresh-dependencies
   ```

### Performance Optimization

**Slow beer list loading:**
1. Add RecyclerView.ViewHolder recycling
2. Use DiffUtil for list updates
3. Load images asynchronously (if added)
4. Cache beer styles for filtering

**High memory usage:**
1. Avoid loading entire dataset at once
2. Use cursor-based pagination
3. Release database connections properly
4. Clear bitmap caches

**Slow search:**
1. Add database indexes on search fields:
   ```java
   @DatabaseField(index = true)
   private String name;
   ```

2. Use FTS (Full-Text Search) table for better search performance

### Development Workflow Issues

**Can't run instrumented tests:**
```bash
# Start emulator first
emulator -avd Pixel_5_API_34 -no-snapshot-load

# Or create AVD
avdmanager create avd -n test_device -k "system-images;android-34;google_apis;x86_64"
```

**ProGuard breaks release build:**
```bash
# Test ProGuard locally
./gradlew assembleRelease -PRELEASE

# Check mapping file
cat app/build/outputs/mapping/release/mapping.txt

# Add keep rules for broken classes in proguard.cfg
```

**Gradle sync fails:**
```bash
# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.0.0

# Or download dependencies manually
./gradlew build --refresh-dependencies --offline
```

---

## Document Maintenance

**Last Updated:** 2025-11-17
**Document Version:** 2.0.0
**Codebase Version:** 2025.0.0.1 (versionCode 27)
**Database Version:** 32 (cbf2025)

**Changelog:**
- **v2.0.0 (2025-11-17):** Added Known Issues section, Troubleshooting guide, and automation suggestions for annual updates
- **v1.0.0 (2025-11-17):** Initial comprehensive documentation

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

## Documentation Structure Recommendations

**Current Status:** Single large CLAUDE.md file (2600+ lines)

**Problem:** As documentation grows, it becomes harder to navigate and maintain. Progressive disclosure is better for usability.

### Recommended Structure

**Proposed hierarchy for better progressive disclosure:**

```
docs/
├── README.md                          # Documentation hub (what you're reading)
├── getting-started.md                 # Quick start for new contributors
├── architecture/
│   ├── overview.md                    # High-level architecture
│   ├── database-schema.md             # Database design and ORM
│   ├── dependency-injection.md        # DI patterns
│   └── navigation-flow.md             # UI navigation
├── development/
│   ├── setup.md                       # Dev environment setup
│   ├── build-and-test.md              # Build commands and testing
│   ├── coding-conventions.md          # Code style guide
│   └── git-workflow.md                # Branching and commits
├── annual-updates/
│   ├── README.md                      # Annual update overview
│   ├── manual-process.md              # Step-by-step manual updates
│   ├── automation-scripts.md          # Automation tools
│   └── checklist.md                   # Verification checklist
├── troubleshooting/
│   ├── README.md                      # Common issues overview
│   ├── crashes.md                     # App crashes debugging
│   ├── anr.md                         # ANR issues
│   ├── stale-data.md                  # Beer list update problems
│   ├── build-failures.md              # CI/CD issues
│   └── sharing-bugs.md                # Share functionality bugs
├── features/
│   ├── dynamic-festivals.md           # Festival loading proposal
│   ├── cider-mead-support.md          # Beverage type expansion
│   ├── ui-modernization.md            # Material Design 3 upgrade
│   └── testing-improvements.md        # Test coverage plan
├── cicd/
│   ├── github-actions.md              # CI/CD pipeline docs
│   ├── release-process.md             # How to release
│   └── signing-keys.md                # Keystore management
└── api/
    ├── data-models.md                 # Beer, Brewery, Festival models
    ├── dao-layer.md                   # Data access objects
    └── json-format.md                 # Beer list JSON spec

CLAUDE.md (top level)                  # Overview + links to detailed docs
```

### Top-Level CLAUDE.md (Proposed Refactor)

**Purpose:** Entry point with high-level overview and links to detailed documentation

**Contents:**
```markdown
# CLAUDE.md - AI Assistant Guide

## Quick Links
- [Getting Started](docs/getting-started.md) - Setup and first contribution
- [Annual Updates](docs/annual-updates/README.md) - **Most common task**
- [Troubleshooting](docs/troubleshooting/README.md) - Debug common issues
- [Architecture](docs/architecture/overview.md) - System design
- [Feature Proposals](docs/features/) - Future enhancements

## At a Glance

**Project:** Cambridge Beer Festival Android App
**Language:** Java (Android)
**Status:** Production, annual updates
**Pain Points:** Manual yearly releases, crashes, stale data

## Critical Information

### For Annual Updates
See [Annual Updates Guide](docs/annual-updates/README.md)
- 3 files to modify
- ~15 minutes with automation script
- Zero-downtime with dynamic festivals (proposed)

### For Bug Fixes
See [Troubleshooting Guide](docs/troubleshooting/README.md)
- Known issues with solutions
- Debugging commands
- Root cause analysis

### For New Features
See [Feature Proposals](docs/features/)
- Dynamic festival loading (RECOMMENDED)
- Cider/mead support
- UI modernization

## Repository Structure
[Brief 10-line overview, link to architecture/overview.md]

## Essential Commands
[5 most common commands, link to development/build-and-test.md]

---
*For detailed information, browse the [docs/](docs/) directory*
```

### Migration Plan

**Phase 1: Create Structure (No Breaking Changes)**
```bash
# Create docs directory structure
mkdir -p docs/{architecture,development,annual-updates,troubleshooting,features,cicd,api}

# Extract sections from CLAUDE.md to individual files
# Keep CLAUDE.md as overview with links
```

**Phase 2: Refactor Content**
1. Split Known Issues → `troubleshooting/*.md`
2. Split Architecture → `architecture/*.md`
3. Split Annual Update → `annual-updates/*.md`
4. Split Features → `features/*.md`
5. Update CLAUDE.md to be hub with summaries + links

**Phase 3: Add Navigation**
- Add README.md in each subdirectory
- Cross-link related documents
- Add "breadcrumbs" navigation
- Generate table of contents

### Benefits

**Progressive Disclosure:**
- ✅ Readers start with overview
- ✅ Dive deeper as needed
- ✅ Less overwhelming
- ✅ Easier to find specific info

**Maintainability:**
- ✅ Smaller, focused files
- ✅ Easier to update individual sections
- ✅ Multiple people can edit different docs
- ✅ Git conflicts reduced

**Discoverability:**
- ✅ Clear categorization
- ✅ READMEs guide exploration
- ✅ Links between related topics
- ✅ Searchable by topic

### Example: Annual Updates Extraction

**From:** Large section in CLAUDE.md

**To:** Dedicated docs/annual-updates/ directory:

```
docs/annual-updates/
├── README.md                     # Overview + quick links
├── manual-process.md             # Step-by-step manual updates
│   ├── 1. Update build.gradle
│   ├── 2. Update festival.xml
│   ├── 3. Increment DB version
│   ├── 4. Test
│   └── 5. Commit
├── automation-scripts.md         # Bash/Gradle automation
│   ├── Option 1: Bash script
│   ├── Option 2: Gradle task
│   └── Option 3: Pre-commit hook
├── checklist.md                  # Printable checklist
└── testing-guide.md              # What to test before release
```

**docs/annual-updates/README.md:**
```markdown
# Annual Festival Updates

**Time Required:** 15 minutes (manual) or 5 minutes (automated)
**Frequency:** Once per year
**Files Modified:** 3

## Quick Start

Using automation (recommended):
```bash
./scripts/update-festival-year.sh 2026
```

Manual process:
1. [Update build.gradle](manual-process.md#1-update-buildgradle)
2. [Update festival.xml](manual-process.md#2-update-festivalxml)
3. [Increment DB version](manual-process.md#3-increment-db-version)

[Full manual process guide →](manual-process.md)

## Automation Options

- [Bash script](automation-scripts.md#bash-script)
- [Gradle task](automation-scripts.md#gradle-task)
- [Pre-commit hook](automation-scripts.md#pre-commit-hook)

## Future: Zero-Maintenance Updates

[Dynamic festival loading](../features/dynamic-festivals.md) eliminates
annual releases entirely. One-time 2-3 week investment, saves 1 week/year forever.
```

### Implementation

**Script to Auto-Generate Structure:**
```bash
#!/bin/bash
# scripts/generate-docs-structure.sh

# Create directory structure
mkdir -p docs/{architecture,development,annual-updates,troubleshooting,features,cicd,api}

# Extract sections from CLAUDE.md using sed/awk
# (Would need custom extraction logic)

# Generate README files
for dir in docs/*/; do
    cat > "$dir/README.md" <<EOF
# $(basename $dir)

[Auto-generated overview]
EOF
done

echo "Documentation structure created in docs/"
```

### Estimated Effort

- Directory structure creation: 30 minutes
- Content extraction/splitting: 4-6 hours
- README generation and linking: 2-3 hours
- Review and polish: 2-3 hours
- **Total: 1-2 days**

**Benefits:** Much easier to maintain long-term, better contributor experience

---

**END OF CLAUDE.MD**
