# Versioning and Release Strategy

**Status:** Proposal
**Date:** 2025-11-22

## Versioning Scheme

### CalVer Format

```
vYYYY.MM.PATCH
```

| Component | Description | Example |
|-----------|-------------|---------|
| `YYYY` | Year | 2025 |
| `MM` | Month (zero-padded) | 11 |
| `PATCH` | Increment per release in that month | 0, 1, 2... |

**Examples:**
- `v2025.11.0` - First release, November 2025
- `v2025.11.1` - Second release, November 2025
- `v2025.12.0` - First release, December 2025

### Android Version Fields

| Field | Tagged Release | Non-Tagged Build |
|-------|----------------|------------------|
| `versionName` | `"2025.11.0"` | `"dev"` |
| `versionCode` | `20251100` | `1` |

**versionCode formula:** `YYYY * 10000 + MM * 100 + PATCH`

This allows:
- Up to 100 releases per month
- Always increasing (required by Play Store)
- Easily readable/debuggable

## Release Workflow

### Triggers

| Event | Jobs Run | Release Created | Signed APK |
|-------|----------|-----------------|------------|
| PR to main (internal) | build-release, instrumented-test, sign-for-testing, coverage | No | Yes |
| PR to main (fork) | build-release, instrumented-test, coverage | No | No (debug APK only) |
| Push to main | build-release, instrumented-test, sign-for-testing, coverage | No | Yes |
| Push tag `v*` | build-release, instrumented-test, release, coverage | Yes | Yes |
| workflow_dispatch | build-release, sign-for-testing | No | Yes |

### Release Process

```
1. Ensure main branch is stable (all tests passing)
2. Create and push tag:
   git tag v2025.11.0
   git push origin v2025.11.0
3. CI automatically:
   - Builds release APK
   - Runs all tests
   - Signs APK
   - Creates GitHub Release with APK attached
```

### Job Dependencies

```
build-release ──────────────────────────┐
  └─ assembleRelease + unit tests       │
                                        ├──> release (tag only)
instrumented-test (4x matrix) ──────────┤    ├─ sign APK
  └─ connectedCheck                     │    └─ create GitHub Release
                                        │
                                        └──> coverage
```

## Implementation Details

### Workflow Changes (android.yml)

**1. Add tag trigger:**
```yaml
on:
  push:
    branches: [ "main" ]
    tags:
      - 'v*'
  pull_request:
    branches: [ "main" ]
```

**2. Add version parsing to build-release job (outputs shared with release job):**
```yaml
build-release:
  outputs:
    version_name: ${{ steps.version.outputs.version_name }}
    version_code: ${{ steps.version.outputs.version_code }}
  steps:
    - name: Parse version from tag
      id: version
      run: |
        if [[ "${{ github.ref }}" == refs/tags/v* ]]; then
          TAG="${{ github.ref_name }}"
          VERSION="${TAG#v}"  # v2025.11.0 → 2025.11.0
          IFS='.' read -r YEAR MONTH PATCH <<< "$VERSION"
          VERSION_CODE=$((YEAR * 10000 + MONTH * 100 + PATCH))
          echo "version_name=$VERSION" >> $GITHUB_OUTPUT
          echo "version_code=$VERSION_CODE" >> $GITHUB_OUTPUT
        else
          echo "version_name=dev" >> $GITHUB_OUTPUT
          echo "version_code=1" >> $GITHUB_OUTPUT
        fi
```

**3. Release job uses outputs from build-release:**
```yaml
release:
  needs: [build-release, instrumented-test]
  if: startsWith(github.ref, 'refs/tags/v')
  steps:
    # ... existing signing steps ...

    - name: Create GitHub Release
      uses: softprops/action-gh-release@v1
      with:
        files: ./app-release-signed.apk
        name: "Cambridge Beer Festival ${{ needs.build-release.outputs.version_name }}"
        generate_release_notes: true
```

### Gradle Changes (app/build.gradle)

```groovy
android {
    defaultConfig {
        versionCode project.hasProperty('versionCode')
            ? project.versionCode.toInteger()
            : 1
        versionName project.hasProperty('versionName')
            ? project.versionName
            : "dev"
    }
}
```

### Build Command Changes

**Non-tagged builds (unchanged):**
```bash
./gradlew assembleRelease --build-cache --parallel
```

**Tagged builds:**
```bash
./gradlew assembleRelease \
  -PversionName=$VERSION_NAME \
  -PversionCode=$VERSION_CODE \
  --build-cache --parallel
```

## Validation (Proposal)

> **Status:** Not yet implemented. These validations are proposed for future enhancement.

### Tag Format Validation

The release job could validate tag format:
```bash
if [[ ! "$GITHUB_REF_NAME" =~ ^v[0-9]{4}\.[0-9]{2}\.[0-9]+$ ]]; then
  echo "::error::Invalid tag format. Expected vYYYY.MM.PATCH (e.g., v2025.11.0)"
  exit 1
fi
```

### versionCode Validation

Could ensure versionCode is higher than previous release:
```bash
LATEST_CODE=$(gh release view --json tagName -q '.tagName' | ... calculate ...)
if [ $VERSION_CODE -le $LATEST_CODE ]; then
  echo "::error::versionCode must be higher than previous release"
  exit 1
fi
```

## Rollback

### Failed Release

If a release has issues after publishing:
1. Do NOT delete the tag (breaks versionCode sequence)
2. Create a new patch release: `v2025.11.1`
3. Document the issue in release notes

### Hotfix Process

```bash
# From main (assuming main has the fix)
git tag v2025.11.1
git push origin v2025.11.1
```

## Sideload Testing

For testing builds on physical devices before release, CI provides multiple options:

### Artifacts Available

| Artifact | When Available | Signed | Use Case |
|----------|----------------|--------|----------|
| `debug-apk` | All builds | Yes (debug key) | Quick sideload testing |
| `release-apk-signed-test` | Internal PRs, main, workflow_dispatch | Yes (release key) | Production-like testing |
| `release-apk-unsigned` | All builds | No | Manual signing |

### How to Get a Sideloadable APK

**Option 1: Debug APK (always available)**
1. Go to the workflow run
2. Download `debug-apk` artifact
3. Install on device via `adb install` or file manager

**Option 2: Signed Release APK (internal builds)**
1. Go to the workflow run (must be internal PR or main branch)
2. Download `release-apk-signed-test` artifact
3. Install on device

**Option 3: Manual Trigger**
1. Go to Actions → Android CI → Run workflow
2. Check "Sign APK for sideload testing"
3. Download `release-apk-signed-test` after completion

### Fork PRs

For security reasons, PRs from forks cannot access signing secrets. Fork contributors can use the `debug-apk` artifact for testing.

## Security Considerations

- Signing secrets only accessed for internal repository events
- Tags can only be pushed by users with write access
- GitHub Release artifacts are immutable once published

## Migration

### Current State
- versionCode: 27
- versionName: "2025.0.0.1"

### First CalVer Release
- Tag: `v2025.11.0`
- versionCode: 20251100 (> 27 ✓)
- versionName: "2025.11.0"

No migration issues - new versionCode is significantly higher.
