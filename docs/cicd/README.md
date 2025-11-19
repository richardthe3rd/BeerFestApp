# CI/CD Documentation

**Last Updated:** 2025-11-19

This directory contains documentation for the BeerFestApp CI/CD pipeline, build process, and optimization strategies.

---

## Documents

### [Pipeline Optimization Plan](pipeline-optimization.md) ⭐

**Status:** Ready for Implementation
**Impact:** 50-60% build time reduction

Comprehensive analysis and recommendations for optimizing the GitHub Actions CI/CD pipeline.

**Quick Links:**
- [Phase 1: Quick Wins](pipeline-optimization.md#phase-1-quick-wins-1-2-hours-30-40-improvement) - 1-2 hours, 30-40% improvement
- [Complete File Changes](pipeline-optimization.md#9-complete-file-changes) - Ready-to-use configurations
- [Measurement & Monitoring](pipeline-optimization.md#8-measurement--monitoring) - Track progress

**Key Improvements:**
- Update Gradle to 8.10.2
- Update Android Gradle Plugin to 8.7.3
- Optimize Gradle configuration
- Separate build and test jobs
- Improve caching strategies
- Optimize emulator configuration

---

## Quick Reference

### Current Pipeline Performance (as of 2025-11-19)

| Job | Duration | Status |
|-----|----------|--------|
| Build | ~26 min | ✅ Passing |
| Test | ~41 min | ✅ Passing |
| Coverage | ~6 sec | ✅ Passing |
| **Total** | **~41 min** | ✅ All checks passing |

### Target Performance (After Optimizations)

| Job | Duration | Improvement |
|-----|----------|-------------|
| Build | 8-12 min | 60-70% faster |
| Test | 15-20 min | 50-60% faster |
| **Total** | **15-20 min** | **50-60% faster** |

---

## Workflow Files

### `.github/workflows/android.yml`

Main CI/CD pipeline that runs on:
- Push to `main` branch
- Pull requests to `main`
- Manual workflow dispatch

**Jobs:**
1. **build** - Compiles APK, runs unit tests, generates coverage
2. **test** - Runs instrumented tests on Android emulator (API 34)
3. **coverage** - Combines coverage reports and posts to PR

---

## Implementation Status

### ✅ Completed

- Configuration cache enabled
- Develocity/build scan integration
- Secure keystore handling
- CodeQL security scanning
- JaCoCo coverage reporting

### 🔄 In Progress

- None currently

### 📋 Planned (Phase 1)

- Update Gradle to 8.10.2
- Update Android Gradle Plugin to 8.7.3
- Optimize `gradle.properties`
- Update `compileSdkVersion` to 34
- Add job timeouts

### 📋 Planned (Phase 2)

- Separate build and test jobs
- Optimize test execution
- Improve caching strategy
- Optimize artifact handling

### 📋 Planned (Phase 3)

- R8 optimization
- Add CI build type
- Dependency review
- Build scan monitoring

---

## Related Documentation

- [Getting Started](../getting-started.md) - Initial setup
- [Annual Updates](../annual-updates/) - Festival updates
- [Troubleshooting](../troubleshooting/) - Common issues
- [Features](../features/) - Feature proposals

---

## Common Commands

### Local Development

```bash
# Build release APK
./gradlew :app:assembleRelease -PRELEASE --scan

# Run all tests
./gradlew test connectedCheck --scan

# Generate coverage report
./gradlew jacocoTestReport
```

### CI Monitoring

```bash
# List recent workflow runs
gh run list --limit 10

# View specific run
gh run view <run-id>

# Watch current run
gh run watch
```

---

## Key Metrics to Monitor

1. **Build Times**
   - Build job duration
   - Test job duration
   - Total pipeline duration

2. **Cache Effectiveness**
   - Configuration cache hit rate (target: >80%)
   - Dependency cache hit rate (target: >90%)
   - AVD cache hit rate (target: >90%)

3. **Build Scans**
   - Configuration time (<1s on cache hit)
   - Task execution time
   - Cache miss reasons

4. **Reliability**
   - Build success rate
   - Test pass rate
   - Flaky test count

---

## Support

For questions or issues:
1. Check [Pipeline Optimization Plan](pipeline-optimization.md)
2. Review [Troubleshooting Guide](../troubleshooting/)
3. Create a [GitHub Issue](https://github.com/richardthe3rd/BeerFestApp/issues)

---

**Document Version:** 1.0
**Last Reviewed:** 2025-11-19
