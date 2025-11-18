# Code Quality & Formatting

**Status:** ✅ Active
**Last Updated:** 2025-11-18
**Applies to:** All Java code in app/ and libraries/

---

## Overview

BeerFestApp uses automated code formatting and linting to maintain consistent code quality:

- **Spotless with google-java-format** - Automatic code formatting (AOSP style)
- **Android Lint** - Static analysis for Android-specific issues
- **Pre-commit hooks** - Automatic checks before committing
- **GitHub Actions** - CI/CD enforcement

---

## Tools

### 1. Spotless Formatter

**What it does:**
- Enforces Google Java Style (AOSP variant with 4-space indent)
- Removes unused imports
- Fixes import order (static imports first)
- Trims trailing whitespace
- Ensures files end with newline

**Configuration:**
- `app/build.gradle:11-13, 145-155`
- `libraries/beers/build.gradle:10-12, 71-81`

### 2. Android Lint

**What it checks:**
- API level compatibility (`NewApi`, `InlinedApi`)
- Resource usage (`UnusedResources`, `UnusedIds`)
- Internationalization (`SetTextI18n`, `HardcodedText`, `DefaultLocale`)
- Date/time formatting (`SimpleDateFormat`)
- Accessibility (`ContentDescription`)

**Configuration:**
- `app/build.gradle:53-65`
- Set to `abortOnError = true` for strict enforcement

---

## Usage

### Quick Reference

```bash
# Check formatting (4-5 seconds)
./gradlew spotlessCheck

# Auto-fix formatting issues
./gradlew spotlessApply

# Run lint checks
./gradlew lintDebug

# Run all pre-commit checks
./gradlew precommit
```

### Daily Workflow

#### Before Committing

The pre-commit hook automatically runs when you commit:

```bash
git commit -m "Your commit message"
```

If checks fail:
```bash
# Fix formatting automatically
./gradlew spotlessApply

# Fix lint issues manually (check output)
./gradlew lintDebug

# Retry commit
git commit -m "Your commit message"
```

#### Manual Checks

Run checks manually anytime:

```bash
# Fast pre-commit checks (15-20 seconds)
./gradlew precommit

# Individual checks
./gradlew spotlessCheck    # Formatting only
./gradlew lintDebug        # Lint only
```

---

## Pre-commit Hook

### What It Does

Located at `.git/hooks/pre-commit`, it intelligently runs checks based on what's being committed:

**Smart Detection:**
- Only runs if `.java` files are in the commit
- Skips instantly for documentation, config, or other non-Java changes

**When Java files are detected:**
1. **Spotless formatting check** (~5 seconds)
2. **Android Lint** (~10-15 seconds, optional with `SKIP_LINT=1`)

If either fails, the commit is blocked with helpful error messages.

### Installation

The hook is created automatically when you clone the repository. If it's missing:

```bash
# Create the hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
set -e

# Check if any Java files are being committed
JAVA_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep '\.java$' || true)

if [ -z "$JAVA_FILES" ]; then
    echo "⚡ No Java files in commit - skipping code quality checks"
    exit 0
fi

echo "🔍 Running pre-commit checks for Java files..."
echo ""

# Fast formatter check (< 5 seconds)
echo "→ Checking code formatting with Spotless..."
if ./gradlew spotlessCheck --quiet --console=plain --no-daemon 2>&1 | grep -v "Configuration.*was resolved"; then
    echo "✓ Code formatting passed"
else
    echo ""
    echo "❌ Code formatting failed!"
    echo ""
    echo "Run './gradlew spotlessApply' to auto-fix formatting issues."
    exit 1
fi

echo ""

# Android Lint (can be slow - skip with SKIP_LINT=1)
if [ "${SKIP_LINT}" != "1" ]; then
    echo "→ Running Android Lint (set SKIP_LINT=1 to skip)..."
    if ./gradlew lintDebug --quiet --console=plain --no-daemon 2>&1 | grep -v "Configuration.*was resolved"; then
        echo "✓ Android Lint passed"
    else
        echo ""
        echo "❌ Android Lint failed!"
        echo ""
        echo "Fix lint issues before committing."
        exit 1
    fi
else
    echo "⚠️  Skipping Android Lint (SKIP_LINT=1)"
    echo "   (Lint will still run in CI)"
fi

echo ""
echo "✅ Pre-commit checks passed!"
echo ""
echo "💡 Tip: For faster commits, use 'SKIP_LINT=1 git commit'"
echo ""
EOF

# Make it executable
chmod +x .git/hooks/pre-commit
```

### Performance Options

The hook has multiple levels for different speed needs:

**Instant (~0s) - Non-Java commits:**
```bash
# Commits with only docs, configs, etc.
git commit -m "docs: update README"
# Output: ⚡ No Java files in commit - skipping code quality checks
```

**Fast (~5-10s) - Skip lint:**
```bash
# When you need quick commits for Java changes
SKIP_LINT=1 git commit -m "feat: add new feature"
# Runs: Spotless check only
```

**Full (~15-20s) - All checks:**
```bash
# Normal commit with all validation
git commit -m "feat: add new feature"
# Runs: Spotless check + Android Lint
```

**Bypass (not recommended):**
```bash
# Skip all checks (CI will still run them)
git commit --no-verify -m "Your message"
```

**Warning:** Bypassing checks means CI will catch issues later!

---

## CI/CD Integration

### GitHub Actions Workflow

The `code-quality` job runs on every push and pull request:

**Location:** `.github/workflows/android.yml:32-61`

**Steps:**
1. Checkout code
2. Setup JDK 17
3. Run `spotlessCheck`
4. Run `lintDebug`
5. Upload lint report artifact

**Timing:**
- Runs **before** the build job (fail-fast)
- Takes ~20-30 seconds
- Caches Gradle dependencies for speed

### Job Dependencies

```
code-quality (20-30s)
    ↓
build (depends on code-quality)
    ↓
test (parallel with build)
    ↓
coverage (depends on build + test)
```

If `code-quality` fails, the entire pipeline stops immediately.

### Viewing Results

**In Pull Requests:**
- Check status appears in PR checks
- Click "Details" to see specific failures
- Lint report available as artifact

**In Actions Tab:**
- View detailed logs
- Download lint HTML report from artifacts

---

## Configuration Details

### Spotless Configuration

```gradle
spotless {
    java {
        target 'src/**/*.java'
        googleJavaFormat('1.19.2').aosp()  // 4-space indent
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        importOrder()  // Static imports first
    }
}
```

**AOSP Style:**
- 4-space indentation (not 2-space Google default)
- Matches existing codebase conventions
- Hungarian notation preserved (`fFieldName`)

### Lint Configuration

```gradle
lint {
    abortOnError true
    warningsAsErrors = false
    checkReleaseBuilds = true

    checkOnly 'NewApi', 'InlinedApi', 'UnusedResources',
              'SimpleDateFormat', 'DefaultLocale', 'SetTextI18n',
              'HardcodedText', 'UnusedIds', 'ContentDescription'

    disable 'IconMissingDensityFolder', 'GoogleAppIndexingWarning'
}
```

**Why `checkOnly`?**
- Runs only critical checks (fast)
- ~10-15 seconds instead of minutes
- Suitable for pre-commit hooks

---

## Common Issues

### Issue: Spotless Check Fails

**Symptoms:**
```
The following files had format violations:
  src/main/java/ralcock/cbf/MyClass.java
```

**Solution:**
```bash
./gradlew spotlessApply
git add .
git commit
```

### Issue: Lint Errors Block Commit

**Symptoms:**
```
❌ Android Lint failed!
Fix lint issues before committing.
```

**Solution:**
```bash
# View detailed lint report
./gradlew lintDebug
open app/build/reports/lint-results-debug.html

# Fix issues manually
# Commit after fixing
git commit
```

### Issue: Pre-commit Hook Too Slow

**Current timing:** ~15-20 seconds

**If you need faster commits:**
```bash
# Temporarily disable hook
git commit --no-verify

# Or reduce lint checks in app/build.gradle
```

### Issue: CI Fails But Local Passes

**Cause:** Gradle cache differences

**Solution:**
```bash
# Clean and retest locally
./gradlew clean
./gradlew precommit
```

---

## Best Practices

### 1. Run Checks Early and Often

```bash
# Before starting work
./gradlew spotlessApply

# Before committing
./gradlew precommit
```

### 2. Auto-format in IDE

**Android Studio:**
1. Go to Settings → Editor → Code Style → Java
2. Set from... → Predefined Style → Android (AOSP)
3. Enable "Reformat code" in commit dialog

**Alternative:** Let Spotless handle it automatically

### 3. Fix Lint Issues Immediately

Don't let lint warnings accumulate:
- Check lint report regularly
- Fix issues as you write code
- Use `@SuppressLint` sparingly with justification

### 4. Commit Formatting Separately

If you're making large changes:

```bash
# Step 1: Format code
./gradlew spotlessApply
git add .
git commit -m "style: apply code formatting"

# Step 2: Make functional changes
# ... your changes ...
git commit -m "feat: your feature"
```

---

## Performance

### Timing Benchmarks

| Task | Duration | Frequency | Notes |
|------|----------|-----------|-------|
| Pre-commit (non-Java) | ~0s | Docs/config commits | Instant skip |
| Pre-commit (Java, skip lint) | 5-10s | With `SKIP_LINT=1` | Format only |
| Pre-commit (Java, full) | 15-20s | Normal Java commits | Format + lint |
| `spotlessCheck` | 4-5s | On-demand | Manual check |
| `spotlessApply` | 5s | On-demand | Auto-fix |
| `lintDebug` | 10-15s | On-demand | Manual check |
| `precommit` task | 15-20s | On-demand | Both checks |
| CI `code-quality` job | 20-30s | Every push | Full validation |

### Optimization Tips

1. **Smart detection:** Hook only runs for Java file changes (instant for docs)
2. **Skip lint:** Use `SKIP_LINT=1` for faster Java commits (5-10s vs 15-20s)
3. **Gradle optimizations:** `--no-daemon` and `--console=plain` reduce overhead
4. **Use Gradle daemon for manual runs:** Already enabled by default
5. **Cache dependencies:** Already configured in CI
6. **Run incrementally:** Spotless only checks changed files

---

## Maintenance

### Updating Spotless

When a new version is released:

```gradle
// In app/build.gradle and libraries/beers/build.gradle
plugins {
    id 'com.diffplug.spotless' version '6.XX.X'  // Update version
}
```

Run reformatting after update:
```bash
./gradlew spotlessApply
git add .
git commit -m "build: update Spotless to vX.XX.X"
```

### Updating google-java-format

```gradle
spotless {
    java {
        googleJavaFormat('1.XX.X').aosp()  // Update version
    }
}
```

### Modifying Lint Checks

To add/remove checks:

```gradle
lint {
    checkOnly 'ExistingCheck', 'NewCheck'  // Add checks
    disable 'NoisyCheck'                   // Disable checks
}
```

Test changes locally:
```bash
./gradlew lintDebug --rerun-tasks
```

---

## Troubleshooting

### Spotless Downloaded Wrong Version

```bash
# Clear Spotless cache
rm -rf ~/.gradle/caches/spotless
./gradlew spotlessCheck
```

### Lint Cache Stale

```bash
# Clear lint cache
./gradlew cleanBuildCache
./gradlew lintDebug
```

### Pre-commit Hook Not Working

```bash
# Check if executable
ls -la .git/hooks/pre-commit

# Make executable if needed
chmod +x .git/hooks/pre-commit

# Test manually
./.git/hooks/pre-commit
```

---

## Related Documentation

- [Getting Started](../getting-started.md) - Development environment setup
- [Annual Updates](../annual-updates/README.md) - Apply formatting during updates
- [GitHub Actions](.github/workflows/android.yml) - CI/CD workflow configuration

---

## References

- [Spotless Documentation](https://github.com/diffplug/spotless)
- [google-java-format](https://github.com/google/google-java-format)
- [Android Lint](https://developer.android.com/studio/write/lint)
- [AOSP Java Style Guide](https://source.android.com/setup/contribute/code-style)

---

**Last Updated:** 2025-11-18
**Version:** 1.0.0