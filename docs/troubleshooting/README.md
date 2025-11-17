# Troubleshooting Guide

This section documents common issues reported by users and developers, along with diagnosis and solutions.

---

## Common User-Reported Issues

| Issue | Impact | Guide |
|-------|--------|-------|
| [App Crashes](crashes.md) | High - App unusable | Crash debugging guide |
| [ANR (Not Responding)](anr.md) | High - App freezes | ANR diagnosis and fixes |
| [Stale Beer List](stale-data.md) | High - Wrong festival data | Database update issues |
| [Share Function Broken](sharing-bugs.md) | Medium - Feature doesn't work | Share intent fixes |
| [Build Failures](build-failures.md) | Medium - Can't build app | CI/CD troubleshooting |

---

## Quick Diagnosis

### Is it a crash?
**Symptoms:** App closes unexpectedly, "App has stopped" dialog

**First steps:**
1. Check logcat: `adb logcat | grep -i exception`
2. Look for NullPointerException or database errors
3. See [Crash Debugging Guide](crashes.md)

### Is it an ANR?
**Symptoms:** App freezes, "App is not responding" dialog

**First steps:**
1. Check if network/database on main thread
2. Pull ANR traces: `adb pull /data/anr/traces.txt`
3. See [ANR Guide](anr.md)

### Is it stale data?
**Symptoms:** Old festival beers showing up

**First steps:**
1. Check database version was incremented
2. Verify beer list URL is correct
3. See [Stale Data Guide](stale-data.md)

### Is it a build issue?
**Symptoms:** Build fails locally or in CI

**First steps:**
1. Clean: `./gradlew clean`
2. Check Java version: `java -version` (should be 17)
3. See [Build Failures Guide](build-failures.md)

---

## Development Issues

| Issue | Guide |
|-------|-------|
| Can't run tests | [Testing Setup](../development/testing.md) |
| Gradle sync fails | [Build Failures](build-failures.md#gradle-sync) |
| ProGuard breaks release | [Build Failures](build-failures.md#proguard) |
| Can't connect to emulator | [Development Setup](../development/setup.md) |

---

## By Category

### Data Issues
- [Stale Beer List](stale-data.md) - Old festival data
- [Download Failures](stale-data.md#download-failures) - Can't fetch beer list
- [Database Corruption](crashes.md#database-issues) - DB errors

### UI Issues
- [Crashes on Beer Details](crashes.md#beer-details) - NullPointerException
- [Share Button Limited Options](sharing-bugs.md) - Missing Intent.createChooser
- [Freezing During Download](anr.md#network-operations) - Network on main thread

### Build Issues
- [CI/CD Failures](build-failures.md) - GitHub Actions errors
- [Keystore Not Found](build-failures.md#keystore) - Signing issues
- [Test Timeouts](build-failures.md#test-timeouts) - Emulator problems

---

## Debug Commands Reference

### Logcat
```bash
# View all logs
adb logcat

# Filter for exceptions
adb logcat | grep -i exception

# Filter for your app
adb logcat | grep ralcock.cbf

# Save to file
adb logcat > debug.log
```

### Database Inspection
```bash
# Connect to device shell
adb shell
run-as ralcock.cbf
cd databases

# Open SQLite
sqlite3 BEERS

# Check schema
.schema

# Check data
SELECT COUNT(*) FROM beers;
SELECT COUNT(*) FROM breweries;

# Check version
PRAGMA user_version;

.quit
```

### App State
```bash
# Clear app data
adb shell pm clear ralcock.cbf

# Uninstall
adb uninstall ralcock.cbf

# Check version
adb shell dumpsys package ralcock.cbf | grep versionName
```

### Network
```bash
# Test beer list URL
curl -I https://data.cambridgebeerfestival.com/cbf2025/beer.json

# Download and inspect
curl https://data.cambridgebeerfestival.com/cbf2025/beer.json | jq .
```

---

## When to Report a Bug

**Before reporting:**
1. Check this troubleshooting guide
2. Search existing GitHub issues
3. Try with a clean install

**Report if:**
- Issue persists after troubleshooting
- Affects multiple users
- You have clear reproduction steps

**Include:**
- Android version
- App version (from About screen)
- Steps to reproduce
- Logcat output
- Screenshots/video if relevant

---

## Getting Help

1. **Check documentation first**
   - [Annual Updates](../annual-updates/)
   - [Development Guide](../development/)
   - [Main Documentation](../../CLAUDE.md)

2. **Search existing issues**
   - https://github.com/richardthe3rd/BeerFestApp/issues

3. **Ask the community**
   - Create a new GitHub issue
   - Include debug information

---

## Prevention

**Avoid common issues:**
- ✅ Always increment DB_VERSION for new festivals
- ✅ Test on multiple Android versions
- ✅ Run all tests before releasing
- ✅ Use automation scripts for updates
- ✅ Monitor crashes in Play Console

**Long-term solutions:**
- [Improve Testing](../features/testing-improvements.md)
- [Dynamic Festival Loading](../features/dynamic-festivals.md)
- [UI Modernization](../features/ui-modernization.md)

---

**Need immediate help?** Start with the most relevant guide above or see [main documentation](../../CLAUDE.md).
