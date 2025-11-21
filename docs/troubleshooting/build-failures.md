# Build Failures and CI/CD Troubleshooting

This guide covers common build failures in CI/CD pipelines and local development.

---

## Quick Navigation

- [Test Timeouts](#test-timeouts)
- [Gradle Build Failures](#gradle-build-failures)
- [Keystore Issues](#keystore-issues)
- [ProGuard Issues](#proguard-issues)
- [Understanding Test Device Names](#understanding-test-device-names)

---

## Test Timeouts

### Window Focus Timeout (Espresso Tests)

**Symptom:**
```
androidx.test.espresso.base.RootViewPicker$RootViewWithoutFocusException:
Waited for the root of the view hierarchy to have window focus and not
request layout for 10 seconds.
```

**Root Cause:**
Soft keyboard (IME) or system UI elements prevent the window from gaining focus during Espresso tests. This is especially common on:
- Tablet emulators (pixel_tablet profile)
- Android 14+ (API 34+)
- Tests that interact with text input fields

**Solution:**
The project uses multiple strategies to prevent this issue:

1. **Test Orchestrator** (`app/build.gradle`):
   ```gradle
   testOptions {
       execution 'ANDROIDX_TEST_ORCHESTRATOR'
       animationsDisabled = true
   }
   ```
   Runs each test in isolation to prevent state leakage.

2. **Emulator Configuration** (`.github/workflows/android.yml`):
   ```yaml
   disable-animations: true
   disable-spellchecker: true
   ```
   Disables animations and spellchecker that can interfere with tests.

3. **ADB Settings** (Applied before tests):
   ```bash
   adb shell settings put secure show_ime_with_hard_keyboard 0
   adb shell settings put global window_animation_scale 0.0
   adb shell settings put global transition_animation_scale 0.0
   adb shell settings put global animator_duration_scale 0.0
   ```
   Ensures soft keyboard stays hidden and animations are disabled.

**Local Testing:**
If you encounter window focus issues locally:
```bash
# Connect to running emulator
adb devices

# Disable animations
adb shell settings put global window_animation_scale 0.0
adb shell settings put global transition_animation_scale 0.0
adb shell settings put global animator_duration_scale 0.0

# Hide soft keyboard
adb shell settings put secure show_ime_with_hard_keyboard 0

# Run tests
./gradlew connectedCheck
```

**Related Issues:**
- #65 - Test failing on pixel_tablet Android 14

---

## Understanding Test Device Names

When viewing test results in CI, you'll see device names like:

```
Starting 126 tests on test(AVD) - 14
```

**Format Breakdown:**
- `test` = Default AVD name from ReactiveCircus/android-emulator-runner
- `(AVD)` = Android Virtual Device
- `14` = **Android version** (not API level!)

**Android Version to API Level Mapping:**

| Test Output | Android Version | API Level | Notes |
|-------------|----------------|-----------|-------|
| test(AVD) - 10 | Android 10 | API 29 | |
| test(AVD) - 12 | Android 12 | API 31 | |
| test(AVD) - 14 | Android 14 | API 34 | Current target SDK |

**Custom AVD Names:**
Starting from the fix for issue #65, AVDs are named based on the test matrix:
```
test-pixel_2-api29
test-pixel_2-api31
test-pixel_2-api34
test-pixel_tablet-api34
```

This makes it clearer which configuration is running.

**Example CI Output:**
```
========================================
Running tests on Android API 34 (pixel_tablet)
Device name: test-pixel_tablet-api34
========================================
```

---

## Gradle Build Failures

### Sandbox Environment Limitations

**Environment Check:**
```bash
# Check if running in sandbox
printenv | grep IS_SANDBOX
```

**If `IS_SANDBOX=yes`:**
- Network access is restricted
- Cannot download Gradle dependencies
- Cannot run builds or tests that require internet
- CI/CD will still work normally (not sandboxed)

**Workaround:**
Changes can be tested in CI/CD pipeline where network access is available.

### Gradle Sync Issues

**Symptom:**
```
Could not resolve all dependencies
```

**Solutions:**
```bash
# Clean build cache
./gradlew clean

# Refresh dependencies
./gradlew --refresh-dependencies

# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build
```

### Out of Memory Errors

**Symptom:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
Check `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m
```

Increase if needed:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Version Conflicts

**Symptom:**
```
Duplicate class found in modules
```

**Solution:**
Check dependency tree:
```bash
./gradlew :app:dependencies

# Filter for specific dependency
./gradlew :app:dependencies | grep androidx.test
```

---

## Keystore Issues

### Keystore Not Found

**Symptom:**
```
Keystore file not found for signing config 'release'
```

**In CI:**
Ensure GitHub secrets are configured:
- `KEYSTORE` (base64 encoded)
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`
- `SIGNING_STORE_PASSWORD`

**Locally:**
For local release builds, you need a keystore:
```bash
# Generate keystore (if needed)
keytool -genkey -v -keystore app/keystore/your_signing_keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias your_alias

# Build release (skip signing for local testing)
./gradlew assembleRelease
# OR with signing:
./gradlew assembleRelease -PRELEASE
```

**Debug Builds:**
Debug builds work without a keystore:
```bash
./gradlew assembleDebug
```

---

## ProGuard Issues

### ProGuard Breaks Release

**Symptom:**
Release build works but crashes at runtime with:
```
java.lang.NoSuchMethodException
java.lang.ClassNotFoundException
```

**Diagnosis:**
ProGuard is removing classes/methods it thinks are unused.

**Solution:**
Add keep rules to `proguard-rules.pro`:
```proguard
# Keep model classes used by OrmLite
-keep class ralcock.cbf.model.** { *; }
-keep @com.j256.ormlite.table.DatabaseTable class * { *; }

# Keep classes used by reflection
-keepclassmembers class * {
    @com.j256.ormlite.field.DatabaseField *;
}
```

**Testing ProGuard:**
```bash
# Build release
./gradlew assembleRelease -PRELEASE

# Install on device
adb install app/build/outputs/apk/release/app-release.apk

# Check for crashes
adb logcat | grep AndroidRuntime
```

---

## CI/CD Pipeline Failures

### GitHub Actions Cache Issues

**Symptom:**
Build succeeds locally but fails in CI.

**Solution:**
```yaml
# Clear cache by changing cache key in .github/workflows/android.yml
key: avd-${{ matrix.api-level }}-${{ matrix.profile }}-v2  # Increment version
```

Or manually clear caches via GitHub Actions UI:
Settings → Actions → Caches → Delete

### Emulator Boot Timeout

**Symptom:**
```
Timeout waiting for emulator to boot
```

**Solution:**
Increase timeout or disk size:
```yaml
disk-size: 6000M
heap-size: 600M
```

Check emulator logs:
```yaml
script: |
  adb logcat -d > emulator.log
  cat emulator.log
  ./gradlew connectedCheck
```

### Test Coverage Upload Fails

**Symptom:**
JaCoCo report generation or upload fails.

**Solution:**
```bash
# Verify coverage was generated
./gradlew jacocoTestReport

# Check reports exist
ls -la app/build/reports/jacoco/
ls -la libraries/beers/build/reports/jacoco/
```

---

## Prevention

**Best Practices:**
1. ✅ Always run `./gradlew clean` before troubleshooting
2. ✅ Test locally before pushing to CI
3. ✅ Check GitHub Actions logs for full stack traces
4. ✅ Keep dependencies up to date
5. ✅ Monitor CI/CD pipeline health

**Automated Checks:**
- CI runs on every PR
- Multiple API levels tested (29, 31, 34)
- Multiple device profiles (pixel_2, pixel_tablet)
- Test coverage tracked
- Build artifacts archived

---

## Related Documentation

- [Annual Updates](../annual-updates/) - Version management
- [Troubleshooting](README.md) - Other common issues
- [CI/CD Pipeline](../../.github/workflows/android.yml) - Full workflow
- [Testing](../development/testing.md) - Local testing setup

---

**Still having issues?** Check [GitHub Issues](https://github.com/richardthe3rd/BeerFestApp/issues) or create a new issue with:
- Full error message
- Build logs
- Steps to reproduce
