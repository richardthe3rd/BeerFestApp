# CLAUDE.md - AI Assistant Guide for BeerFestApp

**Quick Navigation:** [Getting Started](docs/getting-started.md) | [Annual Updates](docs/annual-updates/) | [Troubleshooting](docs/troubleshooting/) | [Features](docs/features/) | [Full Documentation](CLAUDE-full.md.backup)

---

## Project At a Glance

**Project:** Cambridge Beer Festival Android App
**Language:** Java (Android native)
**Build System:** Gradle 8.0.0
**Status:** Production, mature codebase with annual updates
**Repository:** https://github.com/richardthe3rd/BeerFestApp

### Pain Points & Solutions

| Problem | Solution |
|---------|----------|
| **Manual annual releases** (error-prone, 1-2 weeks) | [Dynamic Festival Loading](docs/features/dynamic-festivals.md) ⭐⭐⭐⭐⭐ |
| **User-reported crashes** | [Crash Debugging Guide](docs/troubleshooting/crashes.md) + [Testing Improvements](docs/features/README.md#testing-improvements) |
| **Stale beer lists** | [Troubleshooting Guide](docs/troubleshooting/stale-data.md) |
| **Only shows beer** (no cider/mead) | [Beverage Type Support](docs/features/README.md#cider-and-mead-support) |
| **Dated UI/UX** | [UI Modernization Plan](docs/features/README.md#ui-modernization) |

---

## Documentation Structure

### 📚 Core Guides

| Guide | Purpose | When to Use |
|-------|---------|-------------|
| **[Getting Started](docs/getting-started.md)** | Setup & first contribution | New developers |
| **[Annual Updates](docs/annual-updates/)** | Most common task | Every year (cbf2025 → cbf2026) |
| **[Troubleshooting](docs/troubleshooting/)** | Debug common issues | App crashes, stale data, bugs |
| **[Features](docs/features/)** | Proposed improvements | Planning new features |

### 🏗️ Technical References

| Reference | Content |
|-----------|---------|
| **[Architecture](docs/architecture/)** _(coming soon)_ | System design, MVC layers |
| **[Development](docs/development/)** _(coming soon)_ | Build, test, code conventions |
| **[CI/CD](docs/cicd/)** _(coming soon)_ | GitHub Actions, release process |
| **[API Reference](docs/api/)** _(coming soon)_ | Data models, DAOs, JSON format |

---

## Critical Information

### For Annual Festival Updates

**Most common development task** - updating for new festival year.

**Quick Start:**
```bash
./scripts/update-festival-year.sh 2026
git diff  # Review
./gradlew test  # Test
git commit -am "cbf2026"
```

**📖 Full Guide:** [Annual Updates](docs/annual-updates/)

**Files to modify:** 3
- `app/build.gradle` - Version
- `app/src/main/res/values/festival.xml` - Festival config
- `BeerDatabaseHelper.java` - Database version

**⚠️ Common Mistakes:**
- Forgetting to increment DB_VERSION → stale data
- Mismatched years across files
- Beer list URL doesn't exist yet

### For Bug Fixes

**User-Reported Issues:**

| Issue | Severity | Guide |
|-------|----------|-------|
| App crashes | 🔴 High | [Crash Debugging](docs/troubleshooting/crashes.md) |
| ANR (freezing) | 🔴 High | [ANR Guide](docs/troubleshooting/anr.md) |
| Stale beer list | 🔴 High | [Stale Data](docs/troubleshooting/stale-data.md) |
| Share button limited | 🟡 Medium | [Sharing Bugs](docs/troubleshooting/sharing-bugs.md) |
| Build failures | 🟡 Medium | [Build Issues](docs/troubleshooting/build-failures.md) |

**📖 Full Guide:** [Troubleshooting](docs/troubleshooting/)

### For New Features

**High-Priority Proposals:**

1. **[Dynamic Festival Loading](docs/features/dynamic-festivals.md)** ⭐⭐⭐⭐⭐
   - Eliminates annual app releases
   - Update via JSON instead of code changes
   - ROI: 2-3 week investment saves 1 week/year forever

2. **[Testing Improvements](docs/features/README.md#testing-improvements)**
   - Add comprehensive Espresso tests
   - Reduce production crashes
   - Effort: 2-3 weeks

3. **[UI Modernization](docs/features/README.md#ui-modernization)**
   - Material Design 3 upgrade
   - Dark mode support
   - Effort: 6-9 weeks (3 phases)

**📖 Full Guide:** [Feature Proposals](docs/features/)

---

## Repository Structure

```
BeerFestApp/
├── app/                    # Main Android application
│   ├── src/main/java/ralcock/cbf/
│   │   ├── CamBeerFestApplication.java    # Main Activity
│   │   ├── actions/                       # Business logic
│   │   ├── model/                         # Data access
│   │   ├── service/                       # Background services
│   │   └── view/                          # UI components
│   ├── src/main/res/
│   │   └── values/festival.xml            # Festival config
│   ├── src/androidTest/                   # Instrumented tests
│   └── tests/                             # Unit tests
├── libraries/beers/        # Reusable domain models
│   └── src/main/java/ralcock/cbf/model/
│       ├── Beer.java, Brewery.java        # Entities
│       └── dao/                           # Data Access Objects
├── docs/                   # Documentation (progressive disclosure)
│   ├── getting-started.md
│   ├── annual-updates/
│   ├── troubleshooting/
│   ├── features/
│   └── ...
├── .github/workflows/      # CI/CD (GitHub Actions)
└── scripts/                # Automation scripts
```

**Code size:** ~3,500 lines of production code (26 Java files)

---

## Essential Commands

```bash
# Build
./gradlew build

# Test
./gradlew test                    # Unit tests
./gradlew connectedCheck          # Instrumented tests (requires device)

# Install
./gradlew installDebug

# Release
./gradlew assembleRelease -PRELEASE --scan

# Clean
./gradlew clean

# List all tasks
./gradlew tasks
```

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Language** | Java | Primary language |
| **JDK** | 17 (Temurin) | Development kit |
| **Build** | Gradle 8.0.0 | Build automation |
| **Android SDK** | Compile: 33, Target: 34, Min: 14 | Android platform |
| **OrmLite** | 5.0 | Database ORM |
| **Material Design** | 1.8.0 | UI components |

**Full tech stack:** See [CLAUDE-full.md.backup](CLAUDE-full.md.backup#tech-stack)

---

## Coding Conventions (Quick Reference)

```java
// Fields: camelCase with 'f' prefix (Hungarian notation)
private Beers fBeers;

// Constants: UPPER_SNAKE_CASE
private static final int DB_VERSION = 32;

// Parameters: final
public void onCreate(final SQLiteDatabase db) { }

// Exception handling: Wrap SQLException in RuntimeException
try {
    TableUtils.createTable(conn, Beer.class);
} catch (SQLException e) {
    throw new RuntimeException(e);
}
```

**Full conventions:** See [CLAUDE-full.md.backup](CLAUDE-full.md.backup#coding-conventions)

---

## Database Quick Reference

**Entities:**
- `Beer` - Beer information with foreign key to Brewery
- `Brewery` - Brewery information

**Key Methods:**
- `BeerDatabaseHelper.getBeers()` - Get Beer DAO
- `BeerDatabaseHelper.getBreweries()` - Get Brewery DAO
- `BeerDatabaseHelper.deleteAll()` - Clear all tables

**Current DB Version:** 32 (cbf2025)

**⚠️ Critical:** Always increment `DB_VERSION` when changing festivals or schema!

---

## CI/CD Pipeline

**Platform:** GitHub Actions (`.github/workflows/android.yml`)

**Jobs:**
1. **build-release** - Builds unsigned release APK, runs unit tests
2. **instrumented-test** - Matrix testing on 4 emulator configurations (API 29/31/34, pixel_2/tablet)
3. **release** - Signs APK (main branch only, after tests pass)
4. **coverage** - Aggregates coverage reports for PRs

**Triggers:**
- Push to `main`
- Pull requests to `main`
- Manual workflow dispatch

**Artifacts:**
- `release-apk-unsigned` - Unsigned release APK
- `release-apk-signed` - Signed release APK (tag pushes only)
- `build-reports` - Build and lint reports
- `library-coverage-reports` - Unit test coverage
- `app-coverage-reports-api-*` - Instrumented test coverage

**Secrets Required:** KEYSTORE, SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD, SIGNING_STORE_PASSWORD
(Only accessed by `release` job on main branch)

---

## Annual Update Checklist

**3 Files to Update:**

- [ ] `app/build.gradle` - Increment versionCode, update versionName year
- [ ] `app/src/main/res/values/festival.xml` - Update festival_name, festival_hashtag, beer_list_url
- [ ] `BeerDatabaseHelper.java` - Increment DB_VERSION, update comment

**Verify:** All years match across all files!

**📖 Full Checklist:** [Printable Checklist](docs/annual-updates/checklist.md)

---

## Common Mistakes to Avoid

1. ❌ **Forgetting to increment DB_VERSION** → Users see old festival data
2. ❌ **Mismatched years** (version says 2026, URL says 2025) → Inconsistent behavior
3. ❌ **Wrong versionCode** → Google Play rejects update
4. ❌ **Beer list URL doesn't exist** → App can't download data
5. ❌ **Changing Hungarian notation** (fFieldName) → Breaks convention
6. ❌ **Removing "final" from parameters** → Breaks code style

---

## Getting Help

1. **Check documentation:** Browse [docs/](docs/) for your topic
2. **Search issues:** https://github.com/richardthe3rd/BeerFestApp/issues
3. **Ask questions:** Create new GitHub issue with details
4. **Full reference:** See [CLAUDE-full.md.backup](CLAUDE-full.md.backup) for comprehensive documentation

---

## Quick Links

- **[Getting Started](docs/getting-started.md)** - First steps for new contributors
- **[Annual Updates](docs/annual-updates/)** - Update for new festival (most common task)
- **[Troubleshooting](docs/troubleshooting/)** - Debug crashes, stale data, bugs
- **[Feature Proposals](docs/features/)** - Planned improvements
- **[Full Documentation](CLAUDE-full.md.backup)** - Complete reference (2900+ lines)

---

## Document Information

**Last Updated:** 2025-11-17
**Version:** 3.0.0 (Progressive Disclosure)
**Codebase Version:** 2025.0.0.1 (versionCode 27)
**Database Version:** 32 (cbf2025)

**Changelog:**
- **v3.0.0 (2025-11-17):** Refactored into progressive disclosure structure with docs/ subdirectories
- **v2.1.0 (2025-11-17):** Added dynamic festivals, UI modernization, sharing bugs
- **v2.0.0 (2025-11-17):** Added known issues, troubleshooting, automation
- **v1.0.0 (2025-11-17):** Initial comprehensive documentation

---

**📖 For detailed information, explore the [docs/](docs/) directory or see [full documentation](CLAUDE-full.md.backup).**
- use convential commits. With one commit per logical change