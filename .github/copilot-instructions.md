# GitHub Copilot Instructions for BeerFestApp

## Project Overview

**Cambridge Beer Festival Android App** - A native Android application for discovering and tracking beers at the Cambridge Beer Festival.

- **Language**: Java 17
- **Build System**: Gradle Wrapper 8.1.1 (Android Gradle Plugin 8.0.0)
- **Android SDK**: Min API 14, Target API 34, Compile API 34
- **Database**: OrmLite 5.0 for SQLite
- **UI**: Material Design 1.8.0
- **Repository**: https://github.com/richardthe3rd/BeerFestApp

## Architecture

This is a mature production Android app with MVC architecture:

```
app/src/main/java/ralcock/cbf/
├── CamBeerFestApplication.java    # Main Activity
├── actions/                       # Business logic layer
├── model/                         # Data access layer
│   ├── BeerDatabaseHelper.java   # OrmLite database helper
│   ├── Beer.java, Brewery.java   # Entity models
│   └── dao/                      # Data Access Objects
├── service/                       # Background services (UpdateTask)
└── view/                          # UI components

libraries/beers/                   # Reusable domain models
```

## Coding Conventions

### Java Coding Standards

**Field Naming (Hungarian Notation)**:
- Private fields use `f` prefix: `private Beers fBeers;`
- Constants use UPPER_SNAKE_CASE: `private static final int DB_VERSION = 32;`

**Method Parameters**:
- Always use `final` modifier: `public void onCreate(final SQLiteDatabase db) { }`

**Exception Handling**:
- Wrap `SQLException` in `RuntimeException`:
```java
try {
    TableUtils.createTable(conn, Beer.class);
} catch (SQLException e) {
    throw new RuntimeException(e);
}
```

**Critical Conventions**:
- DO NOT change Hungarian notation (fFieldName) - it's established convention
- DO NOT remove `final` from method parameters
- ALWAYS increment `DB_VERSION` when changing database schema or festival year

### Git Commit Messages

Use conventional commits with one commit per logical change:
- `feat: add new feature`
- `fix: resolve bug`
- `docs: update documentation`
- `refactor: restructure code`
- `test: add tests`
- `chore: maintenance tasks`

## Common Development Tasks

### Annual Festival Updates (Most Common)

**Files to modify** (exactly 3):
1. `app/build.gradle` - Increment `versionCode`, update `versionName` year
2. `app/src/main/res/values/festival.xml` - Update `festival_name`, `festival_hashtag`, `beer_list_url`
3. `app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java` - Increment `DB_VERSION`, update comment

**Critical**: All years must match across files!

**Automation script available**:
```bash
./scripts/update-festival-year.sh 2026
```

### Build and Test Commands

```bash
# Build
./gradlew build

# Test
./gradlew test                    # Unit tests
./gradlew connectedCheck          # Instrumented tests (requires device/emulator)

# Install
./gradlew installDebug

# Release build
./gradlew assembleRelease -PRELEASE --scan

# Clean
./gradlew clean
```

## Database Schema

**Current Version**: DB_VERSION = 32 (cbf2025)

**Entities**:
- `Beer` - Beer information with foreign key to Brewery
- `Brewery` - Brewery information

**Key Helper Methods**:
- `BeerDatabaseHelper.getBeers()` - Get Beer DAO
- `BeerDatabaseHelper.getBreweries()` - Get Brewery DAO
- `BeerDatabaseHelper.deleteAll()` - Clear all tables

**⚠️ Critical**: Always increment `DB_VERSION` when changing festivals or schema to avoid stale data!

## Common Mistakes to Avoid

1. ❌ Forgetting to increment `DB_VERSION` → Users see old festival data
2. ❌ Mismatched years across files → Inconsistent behavior
3. ❌ Wrong `versionCode` sequence → Google Play rejects update
4. ❌ Beer list URL doesn't exist yet → App can't download data
5. ❌ Changing Hungarian notation (fFieldName) → Breaks established convention
6. ❌ Removing `final` from parameters → Violates code style

## Testing

- **Unit Tests**: Located in `app/src/test/`
- **Instrumented Tests**: Located in `app/src/androidTest/`
- **Test Framework**: JUnit 4.13.2, Espresso 3.3.0

Run tests before committing changes:
```bash
./gradlew test && ./gradlew connectedCheck
```

## CI/CD

**GitHub Actions** (`.github/workflows/android.yml`):
- Triggers on push to `main` and pull requests
- Builds release APK with signing
- Runs instrumented tests on API 34 emulator
- Artifacts: build reports and release APKs

## Project-Specific Patterns

### Background Updates
- `UpdateTask` service downloads beer list JSON
- Festival data loaded from `app/src/main/res/values/festival.xml`
- Beer list URL format: `https://www.cambridgebeerfestival.com/cbf{YEAR}/beers.json`

### Data Flow
1. User launches app → `CamBeerFestApplication`
2. `UpdateTask` checks for new beer list
3. JSON parsed and stored via OrmLite DAOs
4. Views query `BeerDatabaseHelper` for display

## Documentation

- **CLAUDE.md**: Quick reference for AI assistants
- **docs/getting-started.md**: Setup guide for new developers
- **docs/annual-updates/**: Festival year update guide
- **docs/troubleshooting/**: Debug common issues
- **docs/features/**: Proposed improvements
- **CLAUDE-full.md.backup**: Comprehensive reference (2900+ lines)

## Key Resources

- Main documentation: See `CLAUDE.md` for overview
- Getting started: `docs/getting-started.md`
- Annual updates: `docs/annual-updates/`
- Troubleshooting: `docs/troubleshooting/`

## When Suggesting Code

1. Follow Hungarian notation for fields (fFieldName)
2. Use `final` for all method parameters
3. Wrap SQLExceptions in RuntimeException
4. Maintain existing code structure and patterns
5. Reference `CLAUDE.md` for detailed conventions
6. Check `docs/` for task-specific guides before suggesting changes
