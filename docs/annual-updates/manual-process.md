# Manual Annual Update Process

This guide walks through the manual process of updating BeerFestApp for a new festival year.

**Before you start:** Make sure you're on a clean branch and have committed any pending work.

---

## Step 1: Update build.gradle

**File:** `app/build.gradle`
**Lines:** 30-31

### Changes Required

```gradle
defaultConfig {
    versionCode 27                    // INCREMENT by 1 (27 → 28)
    versionName "2025.0.0.1"          // UPDATE year (2025 → 2026)
    minSdkVersion 14
    targetSdkVersion 34
    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
```

### Rules

- **versionCode**: Increment by 1 for each release
  - Current: 27
  - Next: 28
  - Google Play Store uses this to determine which version is newer

- **versionName**: Update year in format `YYYY.0.0.1`
  - Current: `"2025.0.0.1"`
  - Next: `"2026.0.0.1"`
  - This is what users see in the app

### Example

```diff
defaultConfig {
-   versionCode 27
-   versionName "2025.0.0.1"
+   versionCode 28
+   versionName "2026.0.0.1"
    minSdkVersion 14
    targetSdkVersion 34
}
```

---

## Step 2: Update festival.xml

**File:** `app/src/main/res/values/festival.xml`

### Changes Required

Update **3 strings** with the new year:

```xml
<resources>
    <string name="app_name">Cambridge Beer Festival</string>
    <string name="festival_name">Cambridge Beer Festival 2025</string>  <!-- UPDATE YEAR -->
    <string name="festival_hashtag">cbf2025</string>                   <!-- UPDATE YEAR -->
    <string name="festival_website_url">https://www.cambridgebeerfestival.com/</string>
    <string formatted="false" name="share_intent_subject">Drinking a %1$s at the %2$s</string>
    <string formatted="false" name="share_intent_text">Drinking %1$s %2$s</string>
    <string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>  <!-- UPDATE YEAR -->
</resources>
```

### What to Update

1. **`festival_name`**: "Cambridge Beer Festival 2025" → "Cambridge Beer Festival 2026"
2. **`festival_hashtag`**: "cbf2025" → "cbf2026"
3. **`beer_list_url`**: `.../cbf2025/beer.json` → `.../cbf2026/beer.json`

### Example

```diff
-<string name="festival_name">Cambridge Beer Festival 2025</string>
+<string name="festival_name">Cambridge Beer Festival 2026</string>

-<string name="festival_hashtag">cbf2025</string>
+<string name="festival_hashtag">cbf2026</string>

-<string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>
+<string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2026/beer.json</string>
```

### Why This Matters

- **festival_name**: Displayed in the app UI
- **festival_hashtag**: Used when sharing beers on social media
- **beer_list_url**: Where the app downloads the beer list from

**⚠️ IMPORTANT:** The beer list URL must be correct and accessible, otherwise the app won't have any data!

---

## Step 3: Increment Database Version

**File:** `app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java`
**Line:** 19

### Changes Required

```java
public final class BeerDatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "BEERS";

    private static final int DB_VERSION = 32; // cbf2025  ← UPDATE THIS
```

### Rules

- **Increment** `DB_VERSION` by 1 (32 → 33)
- **Update comment** to reflect new festival year

### Example

```diff
-private static final int DB_VERSION = 32; // cbf2025
+private static final int DB_VERSION = 33; // cbf2026
```

### Why This Is Critical

**Database versioning triggers migration:**
- When users update the app, `onUpgrade()` is called
- Current implementation **drops all tables** and recreates them
- This clears out old festival data (cbf2025 beers)
- Fresh download of cbf2026 beers on first launch

**⚠️ WARNING:** If you forget to increment DB_VERSION:
- Old festival data will remain
- Users will see 2025 beers instead of 2026 beers
- This is the #1 cause of "stale beer list" bugs

---

## Step 4: Verify Changes

Before committing, verify all three files are updated correctly:

```bash
# Check version in build.gradle
grep -A 2 "defaultConfig" app/build.gradle | grep "version"

# Check festival year in festival.xml
grep -E "festival_name|festival_hashtag|beer_list_url" app/src/main/res/values/festival.xml

# Check database version
grep "DB_VERSION" app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java
```

**All years should match!** If you see:
- `versionName "2026.0.0.1"`
- `festival_name>Cambridge Beer Festival 2026`
- `cbf2026` (in hashtag and URL)
- `DB_VERSION = 33; // cbf2026`

✅ **You're good to proceed!**

---

## Step 5: Test

Run tests to catch any issues:

```bash
# Clean and run unit tests
./gradlew clean test

# Optional: Run instrumented tests (requires emulator)
./gradlew connectedCheck
```

**Manual testing checklist:**
- [ ] App launches successfully
- [ ] Beer list downloads from new URL
- [ ] Festival name shows "Cambridge Beer Festival 2026" in UI
- [ ] Share button uses "#cbf2026" hashtag
- [ ] Database migration works (old data cleared)

For detailed testing guidance, see [Testing Guide](testing-guide.md).

---

## Step 6: Commit

```bash
# Stage the three modified files
git add app/build.gradle
git add app/src/main/res/values/festival.xml
git add app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java

# Commit with conventional message
git commit -m "cbf2026"

# Or more descriptive:
git commit -m "Update app for Cambridge Beer Festival 2026"
```

---

## Step 7: Build and Release

See [CI/CD Release Process](../cicd/release-process.md) for detailed release instructions.

**Quick summary:**
1. Push to main branch (or create PR)
2. GitHub Actions builds release APK
3. Download APK from artifacts
4. Upload to Google Play Store

---

## Common Mistakes

### ❌ Forgetting to Increment DB Version
**Result:** Users see old festival data
**Fix:** Always increment DB_VERSION when changing festivals

### ❌ Mismatched Years
**Result:** Hashtag says 2026 but URL points to 2025
**Fix:** Use automation scripts or carefully verify all three files

### ❌ Wrong versionCode
**Result:** Google Play rejects update (version must increase)
**Fix:** Always increment versionCode by exactly 1

### ❌ URL Doesn't Exist Yet
**Result:** App can't download beer list
**Fix:** Coordinate with festival data team to ensure URL is ready

---

## Next Steps

- [Testing Guide](testing-guide.md) - Comprehensive testing before release
- [Checklist](checklist.md) - Print this for manual verification
- [Automation Scripts](automation-scripts.md) - Avoid manual process next year

---

**Need help?** See [Troubleshooting Guide](../troubleshooting/)
