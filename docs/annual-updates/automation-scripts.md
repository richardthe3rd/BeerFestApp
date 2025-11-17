# Automation Scripts for Annual Updates

**Problem:** Manual updates are error-prone and tedious.

**Solution:** Automate with scripts!

This document provides three automation options for annual festival updates.

---

## Option 1: Bash Script (Recommended)

A complete bash script that updates all three files automatically.

### Installation

```bash
# Create scripts directory
mkdir -p scripts

# Create the script
cat > scripts/update-festival-year.sh << 'EOF'
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
EOF

# Make executable
chmod +x scripts/update-festival-year.sh
```

### Usage

```bash
# Update for 2026 festival
./scripts/update-festival-year.sh 2026

# Review changes
git diff

# Test
./gradlew clean test

# Commit
git commit -am "cbf2026"
```

### Advantages
- ✅ Updates all 3 files automatically
- ✅ Shows summary of changes
- ✅ Catches mistakes (e.g., missing year)
- ✅ No dependencies needed

---

## Option 2: Gradle Task

Add this task to `app/build.gradle` for Gradle-based automation.

### Installation

Add to `app/build.gradle`:

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

### Usage

```bash
# Update festival year
./gradlew updateFestivalYear -Pyear=2026

# Manually update version and DB
# (Edit build.gradle and BeerDatabaseHelper.java)

# Test and commit
./gradlew clean test
git commit -am "cbf2026"
```

### Advantages
- ✅ Integrates with existing Gradle workflow
- ✅ No external scripts needed
- ✅ Works on all platforms (Windows, Mac, Linux)

### Disadvantages
- ⚠️ Only updates festival.xml
- ⚠️ Still need to manually update version and DB

---

## Option 3: Pre-commit Hook

Validates version consistency to catch mistakes before committing.

### Installation

```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
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
EOF

# Make executable
chmod +x .git/hooks/pre-commit
```

### Usage

Works automatically! When you commit:

```bash
# Try to commit with mismatched years
git commit -am "cbf2026"

# If years don't match, commit is blocked:
# ERROR: Year mismatch detected!
#   build.gradle version:   2026
#   festival.xml name:      2025  ← Mismatch!
#   ...

# Fix the mismatch, then commit succeeds
```

### Advantages
- ✅ Catches mistakes automatically
- ✅ Prevents commits with mismatched years
- ✅ Works with any update method

### Disadvantages
- ⚠️ Doesn't do the updates (only validates)
- ⚠️ Hook needs to be installed per clone

---

## Recommended Workflow

**Best approach:** Combine Option 1 + Option 3

1. **Use bash script** to do the updates automatically
2. **Pre-commit hook** validates before commit
3. **Manual review** with `git diff` for safety

```bash
# 1. Run automation script
./scripts/update-festival-year.sh 2026

# 2. Review changes
git diff

# 3. Test
./gradlew clean test

# 4. Commit (pre-commit hook validates)
git commit -am "cbf2026"
# ✓ Version consistency check passed (CBF 2026)

# 5. Push
git push origin main
```

---

## Future: No Scripts Needed

With [Dynamic Festival Loading](../features/dynamic-festivals.md), you won't need any of these scripts:

**Instead of:**
```bash
./scripts/update-festival-year.sh 2026
# Edit files, test, commit, build, release...
```

**You would:**
```bash
# Just add to festivals.json
echo '{"id": "cbf2026", "year": 2026, ...}' >> festivals.json
# Upload to server
# Done! Apps worldwide see new festival
```

[Read more about dynamic festivals →](../features/dynamic-festivals.md)

---

## Troubleshooting

### Script Fails: "sed: command not found"
**Platform:** Windows Git Bash
**Solution:** Use Git Bash or WSL (Windows Subsystem for Linux)

### Script Makes Wrong Changes
**Solution:** Always review with `git diff` before committing

### Pre-commit Hook Not Running
**Solution:** Make sure it's executable: `chmod +x .git/hooks/pre-commit`

### Years Still Mismatch After Script
**Solution:** Check for typos in the script, or file paths have changed

---

**Back to:** [Annual Updates](README.md) | [Manual Process](manual-process.md)
