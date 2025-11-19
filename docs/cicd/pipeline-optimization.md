# CI/CD Pipeline Optimization Plan

**Document Version:** 1.0
**Last Updated:** 2025-11-19
**Status:** Ready for Implementation

---

## Executive Summary

Comprehensive review of the GitHub Actions CI/CD pipeline for BeerFestApp with optimization recommendations that can reduce build times by **50-60%**.

**Current Performance:**
- Build job: ~26 minutes (includes unit tests and coverage)
- Test job: ~41 minutes (emulator + instrumented tests)
- Coverage job: ~6 seconds
- **Total: ~41 minutes** (jobs run in parallel)

**Target Performance:**
- Build job: ~8-12 minutes (60-70% reduction)
- Test job: ~15-20 minutes (50-60% reduction)
- **Total: ~15-20 minutes** (50-60% reduction)

---

## Table of Contents

1. [Build Performance Optimizations](#1-build-performance-optimizations)
2. [Test Performance Optimizations](#2-test-performance-optimizations)
3. [Workflow Structure Optimizations](#3-workflow-structure-optimizations)
4. [Best Practices & Security](#4-best-practices--security)
5. [Configuration Cache Optimization](#5-configuration-cache-optimization)
6. [Android-Specific Optimizations](#6-android-specific-optimizations)
7. [Prioritized Implementation Plan](#7-prioritized-implementation-plan)
8. [Measurement & Monitoring](#8-measurement--monitoring)
9. [Complete File Changes](#9-complete-file-changes)

---

## 1. Build Performance Optimizations

### 1.1 CRITICAL: Update Gradle to 8.10.2

**Current:** Gradle 8.1.1
**Target:** Gradle 8.10.2 (latest 8.x)

**Changes Required:**
```properties
# gradle/wrapper/gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
```

**Command:**
```bash
./gradlew wrapper --gradle-version=8.10.2
```

**Benefits:**
- 10-15% faster builds
- Better configuration cache stability
- Improved build cache effectiveness
- Compatible with Android Gradle Plugin 8.7+

**Risk:** Low - patch version upgrade
**Testing:** Run `./gradlew build --scan` and verify successful build

---

### 1.2 CRITICAL: Update Android Gradle Plugin to 8.7.3

**Current:** AGP 8.0.0 (April 2023, 1.5+ years old)
**Target:** AGP 8.7.3 (latest stable 8.x)

**Changes Required:**
```gradle
// app/build.gradle
buildscript {
    dependencies {
        classpath 'com.android.tools.build:gradle:8.7.3'
    }
}
```

**Benefits:**
- 15-20% faster builds
- Better compatibility with Gradle 8.10
- Improved configuration cache support
- Better incremental compilation
- Security updates

**Risk:** Low - same major version
**Testing:** Run `./gradlew build -PRELEASE --scan` locally

---

### 1.3 HIGH: Optimize Gradle Daemon Memory

**Current:**
```properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
```

**Recommended:**
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC -Dfile.encoding=UTF-8
```

**Rationale:**
- GitHub Actions runners have 7GB RAM, 2 cores
- 2GB is conservative; 4GB allows better parallel compilation
- UseParallelGC optimized for multi-core throughput
- MaxMetaspaceSize prevents metaspace leaks

**Benefits:** 10-15% faster builds with parallel compilation
**Risk:** None - more memory available

---

### 1.4 HIGH: Optimize Gradle Build Configuration

**Add to `gradle.properties`:**
```properties
# Gradle 8+ performance features
org.gradle.vfs.watch=true

# Android build optimizations
android.enableR8.fullMode=true
android.experimental.testOptions.emulatorSnapshots.maxSnapshotsForTestFailures=2
android.defaults.buildfeatures.buildconfig=true
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false
android.enableAdditionalTestOutput=false
```

**Benefits:**
- `org.gradle.vfs.watch=true`: File system watching for incremental builds
- `android.enableR8.fullMode=true`: Better code optimization
- Disabled unused Android build features reduce configuration overhead

**Impact:** 5-10% faster builds

---

### 1.5 MEDIUM: Improve Gradle Cache Strategy

**Add explicit Gradle cache to workflow:**
```yaml
- name: Cache Gradle dependencies
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
      ~/.gradle/daemon
      ~/.gradle/native
      .gradle/
    key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', 'gradle.properties') }}
    restore-keys: |
      gradle-${{ runner.os }}-
```

**Benefits:**
- Faster dependency resolution
- 2-5 minutes saved on cache hits
- Includes daemon state and native components

---

### 1.6 MEDIUM: Optimize Build Command

**Current:**
```yaml
- name: Build with Gradle
  run: ./gradlew build -PRELEASE --scan
```

**Recommended (Granular):**
```yaml
- name: Build release APK
  run: ./gradlew :app:assembleRelease -PRELEASE --scan --parallel --build-cache

- name: Run unit tests
  run: ./gradlew :app:testDebugUnitTest :libraries:beers:test --scan --parallel --build-cache

- name: Generate coverage
  run: ./gradlew :libraries:beers:jacocoTestReport --scan
```

**Benefits:**
- Separates concerns (build vs test)
- Easier to identify which step fails
- Better CI logs
- 5-10% faster with granular tasks

---

## 2. Test Performance Optimizations

### 2.1 CRITICAL: Reduce Emulator Startup Time

**Current Configuration:**
```yaml
emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
disk-size: 6000M
heap-size: 600M
```

**Recommended:**
```yaml
- name: Run instrumented tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: ${{ matrix.api-level }}
    target: google_apis
    arch: x86_64
    force-avd-creation: false
    emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none -no-metrics -accel on
    disable-animations: true
    disk-size: 4096M   # Reduced from 6000M
    heap-size: 512M    # Reduced from 600M
    ram-size: 2048M    # NEW: Explicit RAM allocation
    cores: 2           # NEW: Use both available cores
    script: ./gradlew connectedCheck --scan --max-workers=2
```

**Benefits:**
- Smaller disk/heap = faster initialization
- Explicit RAM and core allocation = better performance
- `-no-metrics` = skip telemetry
- `-accel on` = explicit hardware acceleration
- `--max-workers=2` = match runner core count

**Impact:** 5-10 minutes faster emulator startup and test execution

---

### 2.2 HIGH: Improve AVD Caching

**Current:**
```yaml
key: avd-${{ matrix.api-level }}
```

**Recommended:**
```yaml
- name: AVD cache
  uses: actions/cache@v4
  id: avd-cache
  with:
    path: |
      ~/.android/avd/*
      ~/.android/adb*
    key: avd-${{ matrix.api-level }}-${{ runner.os }}-v2
    restore-keys: |
      avd-${{ matrix.api-level }}-${{ runner.os }}-
```

**Benefits:**
- Add `runner.os` for OS-specific caching
- Add version suffix (`-v2`) to allow cache invalidation
- Restore keys provide fallback
- More reliable cache hits, fewer AVD recreations

---

### 2.3 MEDIUM: Optimize Test Execution

**Current:**
```yaml
script: ./gradlew connectedCheck --scan
```

**Recommended:**
```yaml
script: |
  ./gradlew connectedCheck \
    --scan \
    --max-workers=2 \
    --parallel \
    --build-cache \
    -Pandroid.testInstrumentationRunnerArguments.numShards=1
```

**Benefits:**
- `--max-workers=2`: Match runner CPU cores
- `--parallel`: Enable parallel test execution
- `--build-cache`: Reuse test compilation

**Impact:** 10-20% faster test execution

---

## 3. Workflow Structure Optimizations

### 3.1 HIGH: Separate Build and Test Jobs

**Current Issue:** `build` job does both compilation and testing

**Recommended Structure:**
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - # ... Build release APK only
      - name: Upload release APK
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: app/build/outputs/apk/release/*.apk

  unit-test:
    runs-on: ubuntu-latest
    steps:
      - # ... Run unit tests with coverage

  instrumented-test:
    runs-on: ubuntu-latest
    steps:
      - # ... Run instrumented tests

  coverage:
    needs: [unit-test, instrumented-test]
    steps:
      - # ... Combine coverage reports
```

**Benefits:**
- Jobs run in parallel
- Single responsibility per job
- 30-40% faster overall pipeline through parallelization

**Impact:** Major - reduces total pipeline time from ~41 min to ~20-25 min

---

### 3.2 MEDIUM: Optimize Artifact Storage

**Current:** Uploads entire `app/build/outputs/` directory

**Recommended (Selective):**
```yaml
- name: Upload release APK
  uses: actions/upload-artifact@v4
  with:
    name: release-apk
    path: app/build/outputs/apk/release/*.apk
    retention-days: 30  # Keep releases longer

- name: Upload ProGuard mappings
  uses: actions/upload-artifact@v4
  with:
    name: proguard-mappings
    path: app/build/outputs/mapping/release/mapping.txt
    retention-days: 90  # Keep mappings for crash analysis

- name: Upload build logs
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: build-logs
    path: |
      app/build/outputs/logs/
      ~/.gradle/daemon/*/daemon-*.out.log
    retention-days: 7
```

**Benefits:**
- Selective uploads reduce storage costs
- Critical artifacts retained longer
- Build logs only uploaded on failure
- Faster upload/download

---

### 3.3 MEDIUM: Add Build Timeouts

**Recommended:**
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 30  # Prevent hung builds

  test:
    runs-on: ubuntu-latest
    timeout-minutes: 60  # Emulator can be slow

  coverage:
    runs-on: ubuntu-latest
    timeout-minutes: 10
```

**Benefits:**
- Prevents runaway jobs consuming runner time
- Early failure detection
- Cost savings on free tier (2000 minutes/month)

---

## 4. Best Practices & Security

### 4.1 GOOD: Security Practices ✅

**Current Implementation (Excellent):**
- ✅ Keystore stored as base64-encoded secret
- ✅ Signing credentials passed as environment variables
- ✅ Minimal permissions (`contents: read`, `checks: write`, `pull-requests: write`)
- ✅ Keystore decoded to temp directory

**Recommended Addition:**
```yaml
- name: Cleanup keystore
  if: always()
  run: rm -f ${{ steps.decode_keystore.outputs.filePath }}
```

**Benefits:** Defense in depth, ensures keystore not persisted

---

### 4.2 EXCELLENT: Develocity Integration ✅

**Current Implementation:** Well configured

**Minor Enhancement:**
```yaml
- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v4
  with:
    develocity-injection-enabled: true
    develocity-plugin-version: '3.18.2'
    build-scan-publish: true
    build-scan-terms-of-use-url: "https://gradle.com/help/legal-terms-of-use"
    build-scan-terms-of-use-agree: "yes"
```

**Benefits:** Explicit terms acceptance for reproducibility

---

## 5. Configuration Cache Optimization

### 5.1 EXCELLENT: Configuration Cache Enabled ✅

**Current:**
```properties
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn
```

**Status:** ✅ Well configured

**Note:** One-time cache invalidation (PR #35) was expected and normal after enabling.

---

### 5.2 HIGH: Monitor Configuration Cache Effectiveness

**Action:** Review build scans after each build

**Monitor:**
1. Configuration cache reuse rate (target: >80%)
2. Configuration cache invalidation reasons
3. Configuration time (should be <1s with cache hit)

**Example Success Metrics:**
```
Configuration cache: REUSED
Configuration time: 0.3s
Build time: 12.5s (vs 25s without cache)
```

---

## 6. Android-Specific Optimizations

### 6.1 HIGH: Update Compile SDK

**Current:**
```gradle
android {
    compileSdkVersion 33
    targetSdkVersion 34
}
```

**Recommended:**
```gradle
android {
    compileSdkVersion 34  // Match targetSdkVersion
    targetSdkVersion 34
}
```

**Benefits:**
- Better build reliability
- Access to latest APIs and bug fixes
- Better compatibility with test libraries

---

### 6.2 MEDIUM: Optimize R8 Configuration

**Create:** `/workspaces/BeerFestApp/app/proguard-rules.pro`
```proguard
# OrmLite optimizations
-keep class com.j256.ormlite.** { *; }
-keep class ralcock.cbf.model.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Aggressive optimizations
-optimizationpasses 5
-dontpreverify
-repackageclasses ''
-allowaccessmodification
```

**Update `app/build.gradle`:**
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        android.enableR8.fullMode = true
    }
}
```

**Benefits:**
- 20-30% smaller APK
- Remove debug logging in release
- Better code optimization

---

## 7. Prioritized Implementation Plan

### Phase 1: Quick Wins (1-2 hours, 30-40% improvement)

**Priority:** CRITICAL
**Time:** 1-2 hours
**Impact:** 30-40% faster builds

1. **Update Gradle to 8.10.2**
   ```bash
   ./gradlew wrapper --gradle-version=8.10.2
   git add gradle/wrapper/
   ```

2. **Update Android Gradle Plugin to 8.7.3**
   - Edit `app/build.gradle`: Update classpath to 8.7.3
   - Test: `./gradlew build -PRELEASE --scan`

3. **Optimize `gradle.properties`**
   - Update JVM args to 4GB
   - Add file system watching
   - Add Android build optimizations

4. **Update `compileSdkVersion` to 34**
   - Edit `app/build.gradle`
   - Test locally

5. **Add job timeouts to workflow**
   - Edit `.github/workflows/android.yml`
   - Add `timeout-minutes` to each job

**Testing:**
```bash
# Local testing
./gradlew clean build -PRELEASE --scan

# Review build scan for:
# - Configuration time
# - Build cache effectiveness
# - Task execution times
```

**Commit:**
```bash
git commit -am "perf(ci): Phase 1 - Gradle and AGP updates, optimized configuration"
```

---

### Phase 2: Structural Changes (2-4 hours, additional 20-30% improvement)

**Priority:** HIGH
**Time:** 2-4 hours
**Impact:** Additional 20-30% improvement

1. **Separate build and test jobs**
   - Refactor workflow into distinct jobs
   - Enable parallelization

2. **Optimize test execution**
   - Update emulator configuration
   - Add test execution optimizations

3. **Improve caching strategy**
   - Add explicit Gradle cache
   - Enhance AVD cache

4. **Optimize artifact handling**
   - Selective artifact uploads
   - Adjust retention policies

**Testing:**
```bash
# Create PR to test new workflow structure
# Monitor GitHub Actions for:
# - Job parallelization
# - Cache hit rates
# - Total pipeline time
```

**Commit:**
```bash
git commit -am "perf(ci): Phase 2 - Restructure jobs for parallelization"
```

---

### Phase 3: Advanced Optimizations (4-8 hours, additional 10-15% improvement)

**Priority:** MEDIUM
**Time:** 4-8 hours
**Impact:** Additional 10-15% improvement

1. **R8 optimization**
   - Configure ProGuard rules
   - Enable R8 full mode

2. **Add CI build type**
   - Create dedicated CI configuration
   - Faster debug builds

3. **Dependency review**
   - Add security scanning
   - Review and update dependencies

4. **Build scan monitoring**
   - Establish baseline metrics
   - Monitor configuration cache effectiveness

**Testing:**
```bash
# Verify APK size reduction
ls -lh app/build/outputs/apk/release/

# Test ProGuard rules don't break app
./gradlew :app:assembleRelease -PRELEASE --scan
adb install app/build/outputs/apk/release/*.apk
```

---

## 8. Measurement & Monitoring

### Baseline Metrics (Current - PR #35)

| Metric | Current | Target |
|--------|---------|--------|
| Build job | ~26 min | 8-12 min |
| Test job | ~41 min | 15-20 min |
| Total time | ~41 min | 15-20 min |
| Configuration cache hit rate | New | >80% |
| Dependency cache hit rate | Unknown | >90% |
| AVD cache hit rate | High | >90% |

### Monitoring Strategy

**1. Build Scans (Develocity)**
- Review every build scan URL from `--scan` flag
- Track configuration time, task execution time
- Identify slow tasks and cache misses

**Example Review Checklist:**
```
✓ Configuration cache: REUSED/MISS
✓ Configuration time: <1s on reuse
✓ Slow tasks: identify tasks >30s
✓ Cache effectiveness: >80% from cache
✓ Total build time: trending down
```

**2. GitHub Actions Metrics**
- Track job duration trends
- Monitor cache hit rates
- Alert on timeouts or failures

**Example Dashboard:**
```
Build job duration (7-day avg): 12 min ↓
Test job duration (7-day avg): 18 min ↓
Cache hit rate: 87% ↑
Failed builds: 2/50 (4%)
```

**3. Weekly Review**
- Compare build times week-over-week
- Review failed builds for patterns
- Identify optimization opportunities

---

## 9. Complete File Changes

### File: `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dist
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
```

---

### File: `gradle.properties`

```properties
android.builder.sdkDownload=true
android.useAndroidX=true

# Performance optimizations
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC -Dfile.encoding=UTF-8
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true

# Gradle 8+ features
org.gradle.vfs.watch=true
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn

# Android-specific optimizations
android.nonTransitiveRClass=true
android.enableR8.fullMode=true
android.experimental.testOptions.emulatorSnapshots.maxSnapshotsForTestFailures=2
android.experimental.androidTest.numManagedDeviceShards=1
android.defaults.buildfeatures.buildconfig=true
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false
android.enableAdditionalTestOutput=false
```

---

### File: `app/build.gradle` (Partial - Key Changes)

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.7.3'  // Updated
    }
}

// ... existing plugins ...

android {
    compileSdkVersion 34  // Updated from 33
    namespace 'ralcock.cbf'

    defaultConfig {
        versionCode 27
        versionName "2025.0.0.1"
        minSdkVersion 14
        targetSdkVersion 34
        multiDexEnabled true
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            testCoverageEnabled true
        }

        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    // ... rest unchanged ...
}
```

---

### File: `.github/workflows/android.yml` (Optimized - Complete)

See full optimized workflow in [Appendix A](#appendix-a-full-optimized-workflow).

**Key Changes:**
- Added explicit Gradle caching
- Separated build and test concerns
- Optimized emulator configuration
- Added job timeouts
- Improved artifact handling
- Enhanced Develocity configuration

---

## 10. Success Criteria

### Phase 1 Success Metrics

- ✅ Build completes without errors
- ✅ All tests pass
- ✅ Build time reduced by 25-35%
- ✅ Configuration cache reuse >70%
- ✅ Build scan shows improved task execution

### Phase 2 Success Metrics

- ✅ Jobs run in parallel successfully
- ✅ Total pipeline time reduced by 40-50%
- ✅ Cache hit rates >85%
- ✅ No test failures due to changes

### Phase 3 Success Metrics

- ✅ APK size reduced by 20-30%
- ✅ ProGuard rules don't break app functionality
- ✅ Security scanning integrated
- ✅ Overall pipeline time <20 minutes

---

## 11. Rollback Plan

If any phase causes issues:

**Immediate Rollback:**
```bash
# Revert specific commit
git revert <commit-hash>

# Or reset to before changes
git reset --hard origin/main
git push --force
```

**Gradle/AGP Rollback:**
```bash
# Revert gradle-wrapper.properties
./gradlew wrapper --gradle-version=8.1.1

# Revert app/build.gradle AGP version
# Edit manually to restore 8.0.0
```

**Workflow Rollback:**
```bash
# Restore previous workflow
git checkout origin/main -- .github/workflows/android.yml
git commit -m "fix(ci): rollback workflow changes"
git push
```

---

## Appendix A: Full Optimized Workflow

**File:** `.github/workflows/android.yml`

```yaml
name: Android CI

on:
  workflow_dispatch:
  push:
    branches: [ "main" ]
    paths-ignore:
      - 'README.md'
      - 'LICENSE'
      - '.gitignore'
      - '.devcontainer/**'
      - 'docs/**'
      - '*.md'
  pull_request:
    branches: [ "main" ]
    paths-ignore:
      - 'README.md'
      - 'LICENSE'
      - '.gitignore'
      - '.devcontainer/**'
      - 'docs/**'
      - '*.md'

permissions:
  contents: read
  checks: write
  pull-requests: write

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
    - name: Checkout
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Cache Gradle dependencies
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
          ~/.gradle/daemon
          ~/.gradle/native
          .gradle/
        key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', 'gradle.properties') }}
        restore-keys: |
          gradle-${{ runner.os }}-

    - name: Setup Android SDK
      uses: android-actions/setup-android@v3

    - name: Decode Keystore
      id: decode_keystore
      uses: timheuer/base64-to-file@v1
      with:
        fileName: 'keystore/your_signing_keystore.jks'
        encodedString: ${{ secrets.KEYSTORE }}

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        develocity-injection-enabled: true
        develocity-plugin-version: '3.18.2'
        build-scan-publish: true
        build-scan-terms-of-use-url: "https://gradle.com/help/legal-terms-of-use"
        build-scan-terms-of-use-agree: "yes"

    - name: Build release APK
      run: ./gradlew :app:assembleRelease -PRELEASE --scan --parallel --build-cache
      env:
        SIGNING_KEYSTORE: ${{ steps.decode_keystore.outputs.filePath }}
        SIGNING_KEY_ALIAS: ${{ secrets.SIGNING_KEY_ALIAS }}
        SIGNING_KEY_PASSWORD: ${{ secrets.SIGNING_KEY_PASSWORD }}
        SIGNING_STORE_PASSWORD: ${{ secrets.SIGNING_STORE_PASSWORD }}

    - name: Cleanup keystore
      if: always()
      run: rm -f ${{ steps.decode_keystore.outputs.filePath }}

    - name: Run library unit tests with coverage
      run: ./gradlew :libraries:beers:test :libraries:beers:jacocoTestReport --scan --parallel --build-cache

    - name: Publish Unit Test Results
      uses: EnricoMi/publish-unit-test-result-action@v2
      if: always()
      with:
        files: |
          **/build/test-results/**/*.xml
        check_name: Unit Test Results
        comment_mode: off

    - name: Archive library coverage reports
      uses: actions/upload-artifact@v4
      if: success() || failure()
      with:
        name: library-coverage-reports
        path: |
          libraries/beers/build/reports/jacoco/
        retention-days: 7

    - name: Upload release APK
      uses: actions/upload-artifact@v4
      with:
        name: release-apk
        path: app/build/outputs/apk/release/*.apk
        retention-days: 30

    - name: Upload ProGuard mappings
      uses: actions/upload-artifact@v4
      if: success()
      with:
        name: proguard-mappings
        path: app/build/outputs/mapping/release/mapping.txt
        retention-days: 90

    - name: Archive build reports
      uses: actions/upload-artifact@v4
      if: failure()
      with:
        name: build-reports
        path: |
          ./app/build/reports/
          ~/.gradle/daemon/*/daemon-*.out.log
        retention-days: 7

  test:
    runs-on: ubuntu-latest
    timeout-minutes: 60
    strategy:
      matrix:
        api-level: [34]
    steps:
      - name: Delete unnecessary tools
        uses: jlumbroso/free-disk-space@v1.3.1
        with:
          android: false
          tool-cache: true
          dotnet: true
          haskell: true
          swap-storage: true
          docker-images: false
          large-packages: false

      - name: Enable KVM group perms
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm
          ls /dev/kvm

      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Cache Gradle dependencies
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
            ~/.gradle/daemon
            ~/.gradle/native
            .gradle/
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', 'gradle.properties') }}
          restore-keys: |
            gradle-${{ runner.os }}-

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          develocity-injection-enabled: true
          develocity-plugin-version: '3.18.2'
          build-scan-publish: true
          build-scan-terms-of-use-url: "https://gradle.com/help/legal-terms-of-use"
          build-scan-terms-of-use-agree: "yes"

      - name: AVD cache
        uses: actions/cache@v4
        id: avd-cache
        with:
          path: |
            ~/.android/avd/*
            ~/.android/adb*
          key: avd-${{ matrix.api-level }}-${{ runner.os }}-v2
          restore-keys: |
            avd-${{ matrix.api-level }}-${{ runner.os }}-

      - name: Create AVD and generate snapshot for caching
        if: steps.avd-cache.outputs.cache-hit != 'true'
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          force-avd-creation: false
          target: google_apis
          arch: x86_64
          emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none -no-metrics
          disable-animations: true
          disk-size: 4096M
          heap-size: 512M
          ram-size: 2048M
          cores: 2
          script: echo "Generated AVD snapshot for caching."

      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          target: google_apis
          arch: x86_64
          force-avd-creation: false
          emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none -no-metrics -accel on
          disable-animations: true
          disk-size: 4096M
          heap-size: 512M
          ram-size: 2048M
          cores: 2
          script: ./gradlew connectedCheck --scan --max-workers=2 --parallel --build-cache

      - name: Generate coverage report
        if: always()
        run: ./gradlew jacocoTestReport --scan

      - name: Publish Instrumented Test Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: |
            **/build/outputs/androidTest-results/**/*.xml
          check_name: Instrumented Test Results
          comment_mode: off

      - name: Archive app coverage reports
        uses: actions/upload-artifact@v4
        if: success() || failure()
        with:
          name: app-coverage-reports
          path: |
            app/build/reports/jacoco/
          retention-days: 7

      - name: Archive test reports
        uses: actions/upload-artifact@v4
        if: success() || failure()
        with:
          name: test-reports
          path: ./app/build/reports/androidTests/connected/
          retention-days: 7

  coverage:
    needs: [build, test]
    runs-on: ubuntu-latest
    timeout-minutes: 10
    if: always()
    steps:
      - name: Download library coverage reports
        uses: actions/download-artifact@v4
        continue-on-error: true
        with:
          name: library-coverage-reports
          path: library-coverage/

      - name: Download app coverage reports
        uses: actions/download-artifact@v4
        continue-on-error: true
        with:
          name: app-coverage-reports
          path: app-coverage/

      - name: Add coverage report to PR
        id: jacoco
        uses: madrapps/jacoco-report@v1.7.2
        if: github.event_name == 'pull_request'
        with:
          paths: |
            ${{ github.workspace }}/library-coverage/test/jacocoTestReport.xml,
            ${{ github.workspace }}/app-coverage/jacocoTestReport/jacocoTestReport.xml
          token: ${{ secrets.GITHUB_TOKEN }}
          min-coverage-overall: 15
          min-coverage-changed-files: 50
          title: '📊 JaCoCo Code Coverage'
          update-comment: true
```

---

## Appendix B: Quick Reference Commands

### Testing Locally

```bash
# Clean build with scan
./gradlew clean build -PRELEASE --scan

# Unit tests only
./gradlew test --scan

# Instrumented tests (requires emulator/device)
./gradlew connectedCheck --scan

# Coverage report
./gradlew jacocoTestReport

# Check Gradle version
./gradlew --version

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.10.2
```

### Monitoring Commands

```bash
# View build scan
# Copy URL from build output

# Check cache effectiveness
# Review build scan "Build Cache" section

# Check configuration cache
# Review build scan "Configuration" section

# Gradle daemon status
./gradlew --status

# Stop all Gradle daemons
./gradlew --stop
```

### GitHub Actions

```bash
# List recent runs
gh run list --limit 10

# View specific run
gh run view <run-id>

# View logs for failed run
gh run view <run-id> --log-failed

# Re-run failed jobs
gh run rerun <run-id> --failed

# Watch current run
gh run watch
```

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-19 | ci-build-engineer | Initial comprehensive review |

---

**For questions or issues, create a GitHub issue or consult:**
- [CI/CD Documentation](../cicd/)
- [Getting Started Guide](../getting-started.md)
- [Troubleshooting Guide](../troubleshooting/)
