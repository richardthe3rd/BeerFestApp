# AGP Upgrade Plan

**Created:** 2025-11-21
**Status:** Planned

## Current State

| Component | Current Version |
|-----------|-----------------|
| AGP | 8.0.0 |
| Gradle | 8.14.3 |
| compileSdkVersion | 33 |
| targetSdkVersion | 34 |
| minSdkVersion | 14 |
| JDK | 17 (Temurin) |
| Build syntax | Legacy `buildscript` block |

## Target State

| Component | Target Version |
|-----------|----------------|
| AGP | 8.7.x |
| Gradle | 8.14.3 (no change) |
| compileSdkVersion | 34 |
| targetSdkVersion | 34 (no change) |
| minSdkVersion | 21 (optional) |
| JDK | 17 (no change) |
| Build syntax | Plugins DSL |

## Compatibility Matrix

| Requirement | Current | AGP 8.7.x Requires | Status |
|-------------|---------|-------------------|--------|
| Gradle | 8.14.3 | 8.9+ | Compatible |
| JDK | 17 | 17+ | Compatible |
| compileSdkVersion | 33 | 34+ | Upgrade needed |
| minSdkVersion | 14 | No minimum (21+ recommended) | Optional upgrade |

## Upgrade Steps

### Step 1: Migrate to Plugins DSL

**Risk:** Low
**Rationale:** Syntax-only change with no functional impact. Isolating this change makes debugging easier if issues arise.

**Changes required:**

1. Update `settings.gradle`:
   ```groovy
   pluginManagement {
       repositories {
           google()
           mavenCentral()
           gradlePluginPortal()
       }
   }

   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           google()
           mavenCentral()
       }
   }

   include ':app', ':libraries:beers'
   ```

2. Update `app/build.gradle`:
   - Remove `buildscript` block
   - Replace `apply plugin` with `plugins` block
   ```groovy
   plugins {
       id 'com.android.application' version '8.0.0'
       id 'jacoco'
   }
   ```

3. Update `build.gradle` (root):
   - Remove `subprojects.repositories` block (moved to settings.gradle)

4. Update `libraries/beers/build.gradle`:
   - Migrate to plugins DSL syntax

**Commit:** `refactor: migrate to Gradle plugins DSL`

---

### Step 2: Upgrade compileSdk to 34

**Risk:** Low
**Rationale:** Required for AGP 8.7.x. Separating this change allows catching SDK-related deprecations independently from AGP changes.

**Changes required:**

1. Update `app/build.gradle`:
   ```groovy
   android {
       compileSdkVersion 34  // was 33
   }
   ```

**Verification:**
- Run `./gradlew build` and check for deprecation warnings
- Run `./gradlew connectedCheck` if device available

**Commit:** `chore: upgrade compileSdk to 34`

---

### Step 3: Upgrade AGP to 8.7.x

**Risk:** Medium
**Rationale:** With plugins DSL and compileSdk prerequisites in place, the AGP upgrade should be cleaner.

**Changes required:**

1. Update AGP version in `app/build.gradle` (or `settings.gradle` if using plugins DSL):
   ```groovy
   plugins {
       id 'com.android.application' version '8.7.3'  // was 8.0.0
   }
   ```

2. Address deprecations:
   - `testCoverageEnabled` → `enableAndroidTestCoverage` / `enableUnitTestCoverage`
   - Review and fix any other deprecation warnings

**Verification:**
- Run `./gradlew build --warning-mode all`
- Run full test suite
- Verify APK builds correctly

**Commit:** `chore: upgrade AGP to 8.7.x`

---

### Step 4: Raise minSdk to 21 (Optional)

**Risk:** Low
**Rationale:** API 14 (Ice Cream Sandwich) has <0.1% market share. Raising to 21 (Lollipop) enables better compatibility with modern libraries and removes legacy code paths.

**Changes required:**

1. Update `app/build.gradle`:
   ```groovy
   android {
       defaultConfig {
           minSdkVersion 21  // was 14
       }
   }
   ```

2. Remove `multiDexEnabled true` (not needed with minSdk 21+)

**Benefits:**
- Native multidex support (no library needed)
- Better ART runtime support
- Simplified ProGuard/R8 configuration
- Access to newer APIs without version checks

**Commit:** `chore: raise minSdk to 21`

---

## Commit Strategy

Each step should be a separate commit to maintain clear history and enable easy rollback:

```
1. refactor: migrate to Gradle plugins DSL
2. chore: upgrade compileSdk to 34
3. chore: upgrade AGP to 8.7.x
4. chore: raise minSdk to 21
```

## Rollback Plan

If issues arise at any step:

1. Revert the specific commit: `git revert <commit-hash>`
2. Investigate the issue in isolation
3. Fix and re-attempt the upgrade

## Testing Checklist

- [ ] `./gradlew clean build` succeeds
- [ ] `./gradlew test` passes
- [ ] `./gradlew connectedCheck` passes (if device available)
- [ ] Release APK builds: `./gradlew assembleRelease -PRELEASE`
- [ ] APK installs and runs correctly on test device
- [ ] CI pipeline passes

## References

- [AGP Release Notes](https://developer.android.com/build/releases/gradle-plugin)
- [Gradle Compatibility Matrix](https://developer.android.com/build/releases/gradle-plugin#updating-gradle)
- [Migrate to Plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block)
