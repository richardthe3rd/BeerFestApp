# CI/CD Lessons Learned

**Date:** 2025-11-22
**Context:** Attempted optimization of GitHub Actions build matrix

## Summary

We attempted to optimize the CI pipeline by eliminating redundant debug APK builds across the test matrix. The approach became overly complex and was simplified back to using standard Gradle tooling.

## What We Tried (And Why It Failed)

### Approach: Share Debug APK via Artifacts

**Idea:** Build debug APK once, upload as artifact, download in each matrix job.

**Implementation:**
1. `build-debug` job → builds APK → uploads artifact
2. `instrumented-test` matrix → downloads artifact → installs via `adb install`

**Problems encountered:**

| Problem | Issue |
|---------|-------|
| **`adb shell` exit codes** | Returns 0 even when tests fail; had to parse output |
| **No JUnit XML** | `adb shell am instrument` doesn't produce XML reports |
| **Third-party dependency** | Added Instrumentationpretty JAR for XML conversion |
| **Supply chain risk** | Downloading JAR from GitHub (had to pin to commit) |
| **Output parsing fragility** | Grepping for "FAILURES!!!" is unreliable |
| **`set -e` gotchas** | Shell error handling interacted badly with exit code capture |
| **Artifact path structure** | Easy to get wrong; undocumented implicit contracts |

**Estimated savings:** ~12-15 minutes
**Added complexity:** ~100 lines of fragile shell scripting

### Why Gradle Build Cache Doesn't Help

We initially thought Gradle's build cache would avoid redundant builds.

**Reality:**
- Each matrix job runs on a **separate GitHub Actions runner**
- Local build cache is **per-runner** (not shared)
- Remote build cache requires **Develocity infrastructure** (cost/setup)

The `setup-gradle` action caches **dependencies**, not build outputs.

## What Actually Works

### Simple Approach

```yaml
instrumented-test:
  script: |
    ./gradlew connectedCheck --scan --build-cache --parallel
```

**Benefits:**
- Proper JUnit XML output (built-in)
- Correct exit codes
- Coverage data handled automatically
- No third-party dependencies
- No output parsing
- Battle-tested by millions of Android projects

**Trade-off:** Each matrix job rebuilds debug APK (~3-5 min each)

### Valuable Separation: Release Signing

What **is** worth separating:

```yaml
build-release:    # Builds unsigned APK + unit tests
release:          # Signs APK + creates GitHub Release (tag pushes only)
```

**Why this works:**
- Signing secrets only accessed on tag pushes
- PRs verify release builds without accessing production keys
- Clear separation of concerns
- No complex artifact juggling (just one APK)

## Decision Framework

### When to Optimize CI

✅ **Do optimize when:**
- Build times exceed 20-30 minutes
- You have Develocity/remote build cache infrastructure
- The optimization uses standard, well-supported tooling
- ROI is clear (hours saved per week)

❌ **Don't optimize when:**
- Savings are marginal (~10-15 min)
- Requires custom scripting/parsing
- Adds third-party dependencies
- Increases maintenance burden
- You're "cargo culting" patterns from larger projects

### Complexity Cost Checklist

Before implementing a CI optimization, ask:

1. **Does this add third-party dependencies?** (supply chain risk)
2. **Does this require custom output parsing?** (fragility)
3. **Does this rely on undocumented behavior?** (breakage risk)
4. **Can a new team member understand this in 5 minutes?** (maintainability)
5. **What happens when this breaks at 5pm Friday?** (operational risk)

## Patterns to Avoid

### 1. Custom Test Execution

```bash
# DON'T: Roll your own test runner
adb shell am instrument -w ... | tee output.txt
if grep -q "FAILURES" output.txt; then exit 1; fi

# DO: Use Gradle
./gradlew connectedCheck
```

### 2. Exit Code Workarounds

```bash
# DON'T: Capture and re-check exit codes
set +e
command
EXIT_CODE=$?
set -e
if [ $EXIT_CODE -ne 0 ]; then ...

# DO: Let the command fail naturally
command  # Script exits on failure with set -e
```

### 3. Artifact-Based Build Sharing (Without Infrastructure)

```yaml
# DON'T: Complex artifact upload/download for build outputs
- upload-artifact: debug-apks
# ... later ...
- download-artifact: debug-apks
- run: adb install ./path/that/might/be/wrong/app.apk

# DO: Let each job build what it needs
- run: ./gradlew connectedCheck
```

### 4. Conditional Gradle Scripts

```groovy
// DON'T: Complex conditionals in build scripts
if (System.getenv("SIGNING_KEYSTORE") != null) {
    signingConfigs { ... }
}

// DO: Only include the script when needed
// In CI: Don't pass -PRELEASE for unsigned builds
// build.gradle:
if (project.hasProperty("RELEASE")) {
    apply from: "release.gradle"
}
```

## What We Kept

1. **Separate `release` job** - Signing secrets isolated to main branch
2. **Matrix testing** - 4 emulator configurations (API 29/31/34, pixel_2/tablet)
3. **Standard tooling** - `./gradlew connectedCheck`
4. **Clear job naming** - `build-release`, `instrumented-test`, `release`, `coverage`

## Final Architecture

```
build-release ──────────────────────────┐
  └─ assembleRelease + unit tests       │
                                        ├──> release (main only)
instrumented-test (4x matrix) ──────────┤    └─ sign APK
  └─ connectedCheck (standard Gradle)   │
                                        └──> coverage
```

**Total lines of workflow YAML:** ~350 (down from ~450 at peak complexity)
**Third-party dependencies for CI:** 0
**Custom shell parsing:** 0

## References

- [Gradle Build Cache docs](https://docs.gradle.org/current/userguide/build_cache.html)
- [Android Testing docs](https://developer.android.com/studio/test/command-line)
- [GitHub Actions caching](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows)
