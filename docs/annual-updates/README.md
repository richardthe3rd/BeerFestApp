# Annual Festival Updates

**Time Required:** 15 minutes (manual) or 5 minutes (automated)
**Frequency:** Once per year
**Files Modified:** 3

This is the **most common development task** for BeerFestApp - updating the app for each year's Cambridge Beer Festival.

---

## Quick Start

### Option 1: Automated (Recommended)

```bash
./scripts/update-festival-year.sh 2026
git diff  # Review changes
./gradlew clean test  # Test
git commit -am "cbf2026"
```

[View automation scripts →](automation-scripts.md)

### Option 2: Manual

Follow these three steps:

1. **Update version** in `app/build.gradle` ([details](manual-process.md#step-1-update-buildgradle))
2. **Update festival config** in `app/src/main/res/values/festival.xml` ([details](manual-process.md#step-2-update-festivalxml))
3. **Increment database version** in `BeerDatabaseHelper.java` ([details](manual-process.md#step-3-increment-database-version))

[Full manual guide →](manual-process.md)

---

## Why These Updates Are Needed

Each year, the festival has:
- New beer list JSON URL (`cbf2025/beer.json` → `cbf2026/beer.json`)
- New hashtag for social sharing (`#cbf2025` → `#cbf2026`)
- New app version for Play Store
- Database needs clearing to remove old festival data

---

## Documentation

| Document | Purpose |
|----------|---------|
| [Manual Process](manual-process.md) | Step-by-step manual update guide |
| [Automation Scripts](automation-scripts.md) | Bash/Gradle automation options |
| [Testing Guide](testing-guide.md) | What to test before release |
| [Checklist](checklist.md) | Printable verification checklist |

---

## Future: Zero-Maintenance Updates

**Problem:** Manual updates every year are error-prone and require app releases.

**Solution:** [Dynamic Festival Loading](../features/dynamic-festivals.md)

With dynamic festivals, you would:
1. Add new festival to `festivals.json`
2. Upload to server
3. Apps worldwide automatically see the new festival

**No app release needed!**

[Read the full proposal →](../features/dynamic-festivals.md)

---

## Historical Pattern

Recent annual update commits:
- `ab75e5c` - cbf2025 (#18)
- `912ba56` - Update various things for cbf2024 (#16)
- `cff8443` - update resources for 2023 (#15)

**Commit Message Convention:**
- Short form: `cbfYYYY`
- PR form: `Update app for Cambridge Beer Festival YYYY (#XX)`

---

## Need Help?

- **Issues during update?** See [Troubleshooting](../troubleshooting/stale-data.md)
- **First time updating?** Start with [Manual Process](manual-process.md)
- **Questions?** Check [Main Documentation](../../CLAUDE.md)
