# Getting Started with BeerFestApp Development

Welcome! This guide will help you set up your development environment and make your first contribution.

---

## Quick Links

- **Most Common Task:** [Annual Festival Updates](annual-updates/)
- **Fixing Bugs:** [Troubleshooting Guide](troubleshooting/)
- **Understanding the Code:** [Architecture](architecture/)
- **Proposing Features:** [Feature Proposals](features/)

---

## Prerequisites

Before you begin, make sure you have:

- [ ] **JDK 17** (Temurin distribution recommended)
- [ ] **Android SDK** (API 14-34)
- [ ] **Git** installed
- [ ] **Android Studio** or **IntelliJ IDEA** (optional but recommended)

---

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/richardthe3rd/BeerFestApp.git
cd BeerFestApp
```

### 2. Build the Project

```bash
# Using Gradle wrapper (recommended)
./gradlew build

# If build fails, try cleaning first
./gradlew clean build
```

**Expected output:**
```
BUILD SUCCESSFUL in 30s
```

### 3. Run Tests

```bash
# Unit tests (fast)
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedCheck
```

### 4. Install on Device

```bash
# Debug build
./gradlew installDebug

# Or via Android Studio: Run > Run 'app'
```

---

## Dev Container (Optional)

For a consistent environment, use the included dev container:

### Prerequisites
- Docker installed
- VS Code with Remote-Containers extension

### Usage

1. Open project in VS Code
2. Click "Reopen in Container" when prompted
3. Container includes:
   - Java 17
   - Android SDK
   - Gradle
   - All dependencies

---

## Project Structure

```
BeerFestApp/
├── app/                    # Main Android app
│   ├── src/main/          # Production code
│   ├── src/androidTest/   # Instrumented tests
│   └── tests/             # Unit tests
├── libraries/beers/        # Reusable domain models
├── docs/                   # Documentation (you are here!)
└── scripts/                # Automation scripts
```

**Key directories to know:**
- `app/src/main/java/ralcock/cbf/` - App code
- `app/src/main/res/` - Android resources
- `libraries/beers/src/main/java/ralcock/cbf/model/` - Data models

---

## Making Your First Contribution

### Option 1: Fix a Known Issue

1. Browse [Troubleshooting Guide](troubleshooting/)
2. Pick an issue (sharing bugs are a good start)
3. Create a branch: `git checkout -b fix-share-button`
4. Make changes
5. Test: `./gradlew test`
6. Commit: `git commit -am "Fix share button chooser"`
7. Push and create PR

### Option 2: Update for New Festival

Perfect first contribution! Follow the [Annual Updates Guide](annual-updates/).

**Time required:** 15 minutes
**Files to modify:** 3
**Difficulty:** Easy

### Option 3: Improve Documentation

Found something unclear? Fix it!

1. Edit the relevant `.md` file in `docs/`
2. Commit and create PR
3. Help future contributors!

---

## Development Workflow

### Recommended Workflow

```bash
# 1. Create feature branch
git checkout -b feature/my-feature

# 2. Make changes
# (edit code)

# 3. Test locally
./gradlew clean test

# 4. Commit
git add .
git commit -m "Add feature: description"

# 5. Push
git push origin feature/my-feature

# 6. Create Pull Request on GitHub
```

### Branch Naming

- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `cbf2026` - Annual festival updates

### Commit Messages

```bash
# Good
git commit -m "Fix share button to show all apps"
git commit -m "cbf2026"
git commit -m "Add dark mode support"

# Less good (too vague)
git commit -m "Fixed bug"
git commit -m "Updates"
```

---

## Testing

### Running Tests

```bash
# All unit tests
./gradlew test

# Specific test class
./gradlew test --tests BeersImplTest

# Instrumented tests (requires device/emulator)
./gradlew connectedCheck

# With coverage (library only)
cd libraries/beers
./gradlew jacocoTestReport
```

### Writing Tests

**Unit test example:**
```java
@Test
public void testBeerName() {
    Beer beer = new BeerBuilder()
        .withName("Test IPA")
        .build();

    assertThat(beer.getName(), is("Test IPA"));
}
```

**Instrumented test example:**
```java
@Test
public void testAppLaunches() {
    onView(withId(R.id.beerList))
        .check(matches(isDisplayed()));
}
```

---

## Coding Standards

### Java Style

```java
// Fields: camelCase with 'f' prefix
private Beers fBeers;

// Constants: UPPER_SNAKE_CASE
private static final int DB_VERSION = 32;

// Methods: camelCase
public Beers getBeers() { }

// Parameters: final
public void onCreate(final SQLiteDatabase db) { }
```

**See full conventions:** [Coding Conventions](../CLAUDE.md#coding-conventions)

### Build Files

```gradle
// Use tabs in Gradle files
dependencies {
	implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

---

## Common Commands

```bash
# Build
./gradlew build

# Test
./gradlew test

# Install debug
./gradlew installDebug

# Clean
./gradlew clean

# List tasks
./gradlew tasks

# Check dependencies
./gradlew dependencies
```

---

## Troubleshooting Setup

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Check Java version
java -version
# Should be: openjdk 17.x.x

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.0.0
```

### Tests Fail

```bash
# For unit tests: check test reports
cat app/build/reports/tests/index.html

# For instrumented tests: check device connection
adb devices

# Start emulator
emulator -avd Pixel_5_API_34
```

### Can't Install App

```bash
# Uninstall old version
adb uninstall ralcock.cbf

# Reinstall
./gradlew installDebug

# Check logs
adb logcat | grep ralcock.cbf
```

---

## Getting Help

### Documentation

1. **This guide** - Getting started basics
2. **[Annual Updates](annual-updates/)** - Most common task
3. **[Troubleshooting](troubleshooting/)** - Debug issues
4. **[Main Documentation](../CLAUDE.md)** - Comprehensive reference

### Community

1. **GitHub Issues** - https://github.com/richardthe3rd/BeerFestApp/issues
2. **Search existing issues** before creating new ones
3. **Provide details:**
   - Android version
   - Steps to reproduce
   - Logcat output

---

## What to Work On

### Good First Issues

- [ ] Fix share button to show all apps ([sharing-bugs.md](troubleshooting/sharing-bugs.md))
- [ ] Add missing null checks in UI code
- [ ] Improve error messages
- [ ] Update documentation

### Medium Difficulty

- [ ] Add dark mode support
- [ ] Implement cider/mead filtering
- [ ] Add more unit tests

### Advanced

- [ ] Implement dynamic festival loading
- [ ] Material Design 3 upgrade
- [ ] Replace AsyncTask with WorkManager

**See:** [Feature Proposals](features/) for detailed plans

---

## Next Steps

1. **Complete setup** (build, test, install)
2. **Read** [Architecture Overview](architecture/) to understand the code
3. **Pick a task** from above
4. **Make your first commit!**

**Most important:** Don't hesitate to ask questions via GitHub Issues!

---

**Welcome to the team! 🍺**
