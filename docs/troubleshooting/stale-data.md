# Stale Beer List Troubleshooting

**Problem:** App shows beers from a previous festival year instead of the current one.

**Common User Complaints:**
- "Why am I seeing 2024 beers when it's 2025?"
- "The beer list hasn't updated"
- "Update button doesn't work"

---

## Quick Diagnosis

### Check What Year Is Showing

```bash
# Method 1: Check app UI
# Look at festival name in app header
# Should say "Cambridge Beer Festival 2025" (or current year)

# Method 2: Check database
adb shell
run-as ralcock.cbf
cd databases
sqlite3 BEERS
SELECT DISTINCT style FROM beers LIMIT 5;
# Look at the data - does it match current festival?
.quit
```

### Check Database Version

```bash
adb shell
run-as ralcock.cbf
cd databases
sqlite3 BEERS
PRAGMA user_version;
# Should match DB_VERSION in BeerDatabaseHelper.java
.quit
```

---

## Root Causes

### Cause 1: DB_VERSION Not Incremented

**Most common cause** of stale data.

**How it happens:**
1. Developer updates festival.xml for 2026
2. Forgets to increment DB_VERSION in BeerDatabaseHelper.java
3. App updates, but database version stays the same
4. `onUpgrade()` never called
5. Old 2025 data remains in database

**Diagnosis:**
```bash
# Check if DB_VERSION was incremented
grep "DB_VERSION" app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java

# Compare to festival year
grep "beer_list_url" app/src/main/res/values/festival.xml

# Years should match!
```

**Fix:**
```java
// In BeerDatabaseHelper.java
-private static final int DB_VERSION = 32; // cbf2025
+private static final int DB_VERSION = 33; // cbf2026
```

**Then:**
1. Rebuild app
2. Uninstall old version: `adb uninstall ralcock.cbf`
3. Install new version: `./gradlew installDebug`
4. Database will be recreated with new version

---

### Cause 2: Update Service Not Triggered

**How it happens:**
- UpdateService never runs
- Background service disabled by system
- Network failure during download
- User never opened app after update

**Diagnosis:**
```bash
# Check last update time in preferences
adb shell
run-as ralcock.cbf
cat shared_prefs/ralcock.cbf_preferences.xml | grep last_update
```

**Fix:**
Add manual "Force Update" button in UI:

```java
// In settings or menu
forceUpdateButton.setOnClickListener(v -> {
    // Clear database
    BeerDatabaseHelper helper = getHelper();
    helper.deleteAll();

    // Trigger update
    Intent intent = new Intent(this, UpdateService.class);
    startService(intent);

    Toast.makeText(this, "Downloading beer list...", Toast.LENGTH_SHORT).show();
});
```

---

### Cause 3: Wrong Beer List URL

**How it happens:**
- Festival XML still points to old year
- New year's JSON not published yet
- Typo in URL

**Diagnosis:**
```bash
# Check what URL the app is configured to use
grep "beer_list_url" app/src/main/res/values/festival.xml

# Test if URL exists
curl -I https://data.cambridgebeerfestival.com/cbf2026/beer.json
# Should return: HTTP/1.1 200 OK
# If 404: URL doesn't exist yet!
```

**Fix:**
```xml
<!-- In festival.xml -->
-<string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>
+<string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2026/beer.json</string>
```

**Coordinate with festival data team** to ensure JSON is published before app release!

---

### Cause 4: Network Failure

**How it happens:**
- Download fails silently
- No error shown to user
- Old data remains

**Diagnosis:**
```bash
# Check logcat for network errors
adb logcat | grep -i "UpdateService\|UpdateTask\|IOException"

# Common errors:
# - java.net.UnknownHostException (no internet)
# - java.net.SocketTimeoutException (timeout)
# - HTTP 404 (URL not found)
```

**Fix:**
Improve error handling in UpdateService:

```java
try {
    downloadBeerList(url);
} catch (IOException e) {
    Log.e(TAG, "Failed to download beer list", e);

    // Show error to user
    showNotification("Failed to update beer list. Tap to retry.");

    // Schedule retry
    scheduleRetry();
}
```

---

## Solutions

### Solution 1: Clear App Data (User Fix)

Tell users to clear app data:

```
Settings → Apps → Cambridge Beer Festival → Storage → Clear Data
```

Or via adb:
```bash
adb shell pm clear ralcock.cbf
```

**⚠️ Warning:** This deletes all user data (bookmarks, ratings, notes)!

---

### Solution 2: Force Database Upgrade (Developer Fix)

```java
// In BeerDatabaseHelper.java
@Override
public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);

    // Force upgrade even if version hasn't changed
    if (BuildConfig.DEBUG) {
        int currentVersion = db.getVersion();
        onUpgrade(db, getConnectionSource(), currentVersion, currentVersion + 1);
    }
}
```

---

### Solution 3: Add Update Status UI

Show users when data was last updated:

```java
// In settings or main screen
SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
long lastUpdate = prefs.getLong("last_beer_update", 0);

if (lastUpdate > 0) {
    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        .format(new Date(lastUpdate));
    lastUpdateText.setText("Last updated: " + date);
} else {
    lastUpdateText.setText("Never updated");
}
```

---

### Solution 4: Add Version Check

Check beer list version before displaying:

```java
// Add to Beer or BeerList model
public class BeerList {
    private String festivalYear; // "2026"

    public boolean isCurrentFestival() {
        String expectedYear = getString(R.string.festival_name)
            .replaceAll("[^0-9]", ""); // Extract year
        return festivalYear.equals(expectedYear);
    }
}

// In UI
if (!beerList.isCurrentFestival()) {
    showWarning("Beer list is out of date. Tap to update.");
}
```

---

## Prevention

### For Developers

1. **Always increment DB_VERSION** when updating festival year
   ```java
   private static final int DB_VERSION = 33; // cbf2026 ← Don't forget!
   ```

2. **Use automation scripts** to avoid mistakes
   ```bash
   ./scripts/update-festival-year.sh 2026
   ```

3. **Add pre-commit hook** for version consistency
   - See [Automation Scripts](../annual-updates/automation-scripts.md#option-3-pre-commit-hook)

4. **Test database migration**
   ```bash
   # Install old version
   adb install old-app.apk

   # Generate some data
   # (open app, download beers, add bookmarks)

   # Install new version
   adb install new-app.apk

   # Verify old data cleared, new data loads
   ```

### For Users

1. **Update app from Play Store** when available
2. **Open app after updating** to trigger data download
3. **Check festival year** in app header
4. **Report issues** if data seems wrong

---

## Long-Term Solution

**Problem:** Database version management is fragile and error-prone.

**Solution:** [Dynamic Festival Loading](../features/dynamic-festivals.md)

With dynamic festivals:
- ✅ No database version changes needed
- ✅ Multiple festivals can coexist in one database
- ✅ Users can browse historical data
- ✅ New festivals appear automatically (no app update!)

```java
// With dynamic festivals:
SELECT * FROM beers WHERE festival_id = 'cbf2026';  // Just works!
SELECT * FROM beers WHERE festival_id = 'cbf2025';  // Historical data!
```

---

## Related Issues

- [Database Schema](../api/data-models.md#database)
- [Annual Update Process](../annual-updates/manual-process.md#step-3-increment-database-version)
- [Update Service Code](../../app/src/main/java/ralcock/cbf/service/UpdateService.java)

---

**Back to:** [Troubleshooting](README.md)
