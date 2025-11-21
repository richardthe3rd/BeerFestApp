# Test Migration Plan: Legacy to Modern Framework

## Status: In Progress (66% Complete)

**Priority:** Medium
**Effort:** 2-3 hours remaining
**Impact:** Improved maintainability, better tooling, modern test framework

---

## Overview

**Test Migration Progress: 66% Complete (5 of 6 legacy tests migrated)**

The BeerFestApp test suite has been significantly expanded:
- ✅ **5 legacy tests migrated** to modern AndroidJUnit4 framework
- ✅ **6 new E2E tests added** covering major UI interactions
- ✅ **3 new unit tests added** (BeerSearcher, AppPreferences coverage expansion, etc.)
- ⏳ **2 legacy tests remaining** in `app/tests/src/` (DAO tests)
- ✅ **LifecycleTest replaced** with modern ActivityScenario-based test

**Current state (as of 2025-11-21):**
- ✅ **15 modern test classes** in `app/src/androidTest/` (up from 11)
- ✅ Comprehensive E2E test coverage for UI interactions
- ✅ Comprehensive UI architecture documentation
- ⏳ 2 legacy DAO tests still to migrate
- ❌ Tests still use deprecated `AndroidTestCase` framework (2 remaining)

**Recent Additions:**
- ✅ `CamBeerFestApplicationLifecycleTest` (2025-11-18) - Modern ActivityScenario-based lifecycle testing
- ✅ `BeerListInteractionTest` (2025-11-18) - Beer list display and navigation
- ✅ `StarRatingInteractionTest` (2025-11-18) - Star rating functionality
- ✅ `FavoritesInteractionTest` (2025-11-18) - Bookmark/wishlist functionality
- ✅ `SortingAndFilteringTest` (2025-11-18) - Sort and filter dialogs
- ✅ `SearchFunctionalityTest` (2025-11-18) - Search/filter functionality
- ✅ `BeerSearcherTest` (2025-11-21) - Web search functionality (18 tests)
- ✅ `/docs/ui-architecture.md` (2025-11-18) - Comprehensive UI documentation for modernization

**Goal:** Complete migration of remaining 2 legacy tests and expand E2E test coverage.

---

## Current Test Inventory

### Legacy Tests (app/tests/src/)

| Test File | Lines | Test Methods | Status | Dependencies |
|-----------|-------|--------------|--------|--------------|
| ~~`LifecycleTest.java`~~ | 45 | 1 | ❌ **DELETED** | ActivityUnitTestCase (removed - see below) |
| ~~`CamBeerFestApplicationTest.java`~~ | 9 | 0 | ❌ **DELETED** | Empty test, deprecated framework |
| ~~`actions/BeerSharerTest.java`~~ | 84 | 3 | ✅ **MIGRATED** | Moved to modern framework |
| ~~`actions/BeerExporterTest.java`~~ | 382 | 20 | ✅ **MIGRATED** | Moved to modern framework |
| ~~`model/JsonBeerListTest.java`~~ | 78 | 2 | ✅ **MIGRATED** | Moved to modern framework + resources |
| ~~`model/BeerListTest.java`~~ | 134 | 3 | ✅ **MIGRATED** | Moved to modern framework |
| `model/dao/BeersImplTest.java` | ? | 2 | ✅ Running | AndroidTestCase, OrmLite |
| `model/dao/BreweriesImplTest.java` | ? | 3 | ✅ Running | AndroidTestCase, OrmLite |

**Total:** 2 test classes running (4 migrated, 2 deleted)

### Modern Tests (app/src/androidTest/)

#### Unit and Integration Tests

| Test File | Lines | Test Methods | Framework |
|-----------|-------|--------------|-----------|
| `CamBeerFestApplicationInstrumentedTest.java` | 28 | 1 | AndroidJUnit4, Espresso |
| `actions/BeerSharerTest.java` | 108 | 3 | AndroidJUnit4 |
| `actions/BeerSearcherTest.java` | 379 | 18 | AndroidJUnit4 |
| `actions/BeerExporterTest.java` | 453 | 20 | AndroidJUnit4 |
| `AppPreferencesTest.java` | ~200 | ~15 | AndroidJUnit4 |
| `model/JsonBeerListTest.java` | 84 | 2 | AndroidJUnit4 |
| `model/BeerListTest.java` | 144 | 3 | AndroidJUnit4, EasyMock |
| `model/dao/BeersImplTest.java` | ? | 2 | AndroidJUnit4 |
| `model/dao/BreweriesImplTest.java` | ? | 3 | AndroidJUnit4 |

#### End-to-End Tests (Added 2025-11-18)

| Test File | Test Methods | Coverage Area |
|-----------|--------------|---------------|
| `CamBeerFestApplicationLifecycleTest.java` | 4 | Activity lifecycle, recreation, state transitions |
| `BeerListInteractionTest.java` | 6 | List display, clicking beers, navigation |
| `StarRatingInteractionTest.java` | 4 | Rating bar interaction, persistence |
| `FavoritesInteractionTest.java` | 5 | Bookmarking beers, wishlist functionality |
| `SortingAndFilteringTest.java` | 7 | Sort dialog, filter by style, preferences |
| `SearchFunctionalityTest.java` | 10 | Search/filter beers, search persistence |

**Total:** 15 test classes (8 unit/integration + 7 e2e)

---

## Migration Strategy

### Phase 1: Setup & Validation ✅ COMPLETE
- [x] Configure Gradle to run legacy tests (implemented)
- [x] Verify all 8 tests run in CI
- [x] Establish baseline (all tests should pass)

### Phase 2: Migrate Tests (Pending)

**For each test file:**

1. **Create new test file** in `app/src/androidTest/java/ralcock/cbf/`
2. **Update class structure:**
   ```java
   // OLD
   public class FooTest extends AndroidTestCase {
       public void testSomething() throws Exception { ... }
   }

   // NEW
   @RunWith(AndroidJUnit4.class)
   public class FooTest {
       @Test
       public void testSomething() { ... }
   }
   ```

3. **Replace Context access:**
   ```java
   // OLD
   Context ctx = getContext();

   // NEW (option 1 - for unit-style tests)
   @get:Rule
   val context = ApplicationProvider.getApplicationContext<Context>()

   // NEW (option 2 - for UI tests)
   @Rule
   public ActivityTestRule<CamBeerFestApplication> activityRule =
       new ActivityTestRule<>(CamBeerFestApplication.class);
   Context ctx = activityRule.getActivity();
   ```

4. **Update assertions:**
   ```java
   // OLD & NEW both work (JUnit 3 style compatible)
   assertEquals(expected, actual);
   assertTrue(condition);

   // NEW (modern option - Hamcrest)
   assertThat(actual, is(expected));
   ```

5. **Migrate resources:**
   - Move JSON test resources from `app/tests/src/.../resources/` to `app/src/androidTest/resources/`
   - Or keep in package structure under `app/src/androidTest/java/.../resources/`

6. **Verify test passes** in new location

7. **Delete old test** from `app/tests/src/`

### Phase 3: Cleanup
- [ ] Remove `app/tests/` directory entirely
- [ ] Remove `sourceSets` configuration from `app/build.gradle`
- [ ] Update documentation

---

## Migration Order (Recommended)

**Migrate in this order (simplest to most complex):**

1. ✅ `CamBeerFestApplicationInstrumentedTest.java` (already migrated)
2. ❌ ~~`LifecycleTest.java`~~ (**DELETED** - see replacement strategy below)
3. ✅ `BeerSharerTest.java` (Intent tests, medium complexity) **MIGRATED**
4. ✅ `BeerExporterTest.java` (CSV export tests, medium complexity) **MIGRATED**
5. ✅ `JsonBeerListTest.java` (requires resource migration) **MIGRATED**
6. ✅ `BeerListTest.java` (unknown complexity) **MIGRATED**
7. ❌ ~~`CamBeerFestApplicationTest.java`~~ (**DELETED** - empty test, deprecated framework)
8. `BeersImplTest.java` (DAO/database tests, complex) **← NEXT**
9. `BreweriesImplTest.java` (DAO/database tests, complex)

---

## Special Case: LifecycleTest Replacement

**Status:** Deleted (commit 3df0096)
**Reason:** `ActivityUnitTestCase` is deprecated and incompatible with modern Android testing

### What It Was Testing

The old `LifecycleTest` tested activity lifecycle state transitions:
- Starting activity
- Saving instance state
- Destroying activity
- Recreating from saved state

### Modern Replacement Strategy

**Replace with ActivityScenario** (AndroidX Test Library):

```java
package ralcock.cbf;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ActivityScenario.launch;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class CamBeerFestApplicationLifecycleTest {

    @Test
    public void testActivityRecreation() {
        // Launch activity
        try (ActivityScenario<CamBeerFestApplication> scenario =
                launch(CamBeerFestApplication.class)) {

            // Test activity is created
            scenario.onActivity(activity -> {
                assertNotNull(activity);
            });

            // Simulate configuration change (recreate activity)
            scenario.recreate();

            // Verify activity survives recreation
            scenario.onActivity(activity -> {
                assertNotNull(activity);
                // Add assertions to verify state was restored
            });
        }
    }

    @Test
    public void testActivityStateTransitions() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                launch(CamBeerFestApplication.class)) {

            // Move through lifecycle states
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.DESTROYED);

            // ActivityScenario handles the lifecycle automatically
        }
    }
}
```

**Key Differences:**
- `ActivityScenario` is modern, not deprecated
- Much simpler API - no manual lifecycle calls needed
- Better integration with AndroidX Test libraries
- Automatic cleanup with try-with-resources

**Migration Priority:** Medium-Low (after other tests are migrated)

**Effort:** 30-45 minutes

---

## Example Migration: BeerSharerTest

### Before (app/tests/src/ralcock/cbf/actions/BeerSharerTest.java)

```java
package ralcock.cbf.actions;

import android.content.Intent;
import android.test.AndroidTestCase;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;

public class BeerSharerTest extends AndroidTestCase {

    public void testExtraTextUnrated() throws Exception {
        Beer beer = aBeer()
                .called("TheBeerName")
                .from(aBrewery().called("TheBreweryName"))
                .build();

        String theHashTag = getContext().getResources().getString(R.string.festival_hashtag);

        BeerSharer sharer = new BeerSharer(getContext());
        Intent intent = sharer.makeShareIntent(beer);
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);

        assertTrue(extraText.contains("TheBeerName"));
        assertTrue(extraText.contains(theHashTag));
    }
}
```

### After (app/src/androidTest/java/ralcock/cbf/actions/BeerSharerTest.java)

```java
package ralcock.cbf.actions;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;

import static org.junit.Assert.*;
import static ralcock.cbf.model.BeerBuilder.aBeer;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

@RunWith(AndroidJUnit4.class)
public class BeerSharerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testExtraTextUnrated() {
        Beer beer = aBeer()
                .called("TheBeerName")
                .from(aBrewery().called("TheBreweryName"))
                .build();

        String theHashTag = context.getResources().getString(R.string.festival_hashtag);

        BeerSharer sharer = new BeerSharer(context);
        Intent intent = sharer.makeShareIntent(beer);
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);

        assertTrue(extraText.contains("TheBeerName"));
        assertTrue(extraText.contains(theHashTag));
    }
}
```

**Key changes:**
- `extends AndroidTestCase` → `@RunWith(AndroidJUnit4.class)`
- `public void testFoo() throws Exception` → `@Test public void testFoo()`
- `getContext()` → `ApplicationProvider.getApplicationContext()`
- Added `@Before setUp()` method for initialization
- Removed `throws Exception` (only throw if actually needed)

---

## Dependencies Needed

Already in `app/build.gradle`:
```gradle
androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
androidTestImplementation 'androidx.test:runner:1.3.0'
androidTestImplementation 'androidx.test:rules:1.3.0'
```

May need to add:
```gradle
androidTestImplementation 'androidx.test:core:1.4.0'
androidTestImplementation 'androidx.test.ext:junit:1.1.3'
```

---

## Special Considerations

### DAO/Database Tests
Tests like `BeersImplTest.java` and `BreweriesImplTest.java` interact with SQLite:
- May need database setup/teardown in `@Before`/`@After`
- Consider using in-memory database for tests
- May need to handle database migrations

### Resource Files
Tests like `JsonBeerListTest.java` load JSON resources:
- Resources currently at: `app/tests/src/ralcock/cbf/model/resources/*.json`
- Move to: `app/src/androidTest/resources/ralcock/cbf/model/` or similar
- Update resource loading code if needed

---

## Testing the Migration

After each test migration:

```bash
# Run specific test class
./gradlew connectedCheck --tests "ralcock.cbf.actions.BeerSharerTest"

# Run all androidTest tests
./gradlew connectedCheck

# Verify in CI
git push origin <branch>
# Check GitHub Actions workflow
```

---

## Rollback Plan

If migration causes issues:
1. Legacy tests remain in `app/tests/src/` until migration complete
2. Both old and new versions can coexist temporarily
3. Can revert Gradle configuration if needed

---

## Timeline Estimate

| Phase | Effort | Notes |
|-------|--------|-------|
| Phase 1: Setup ✅ | 30 min | Complete |
| Phase 2a: Migrate 6 legacy tests | 2-2.5 hours | ~15-25 min per test |
| Phase 2b: Create new LifecycleTest | 30-45 min | Using ActivityScenario |
| Phase 3: Cleanup | 15 min | Delete old files, update docs |
| **Total** | **3-3.5 hours** | Can be done incrementally |

**Note:** LifecycleTest is a new test (not a migration) since the old one was deleted.

---

## Success Criteria

- [ ] 7 legacy tests migrated to `app/src/androidTest/`
- [ ] New ActivityScenario-based lifecycle test created
- [ ] All 9 tests use modern frameworks (AndroidJUnit4, Espresso, ActivityScenario)
- [ ] All tests pass in CI (`./gradlew connectedCheck`)
- [ ] `app/tests/` directory deleted
- [ ] `sourceSets` configuration removed from `app/build.gradle`
- [ ] Documentation updated

**Current Progress:**
- ✅ 5/9 tests already modern (`CamBeerFestApplicationInstrumentedTest`, `BeerSharerTest`, `BeerExporterTest`, `JsonBeerListTest`, `BeerListTest`)
- ⏳ 3/9 tests still in legacy framework
- ❌ 1/9 tests deleted (LifecycleTest - needs ActivityScenario replacement)

**Progress: 56% complete (5/9 tests migrated)**

---

## References

- [Android Testing Guide](https://developer.android.com/training/testing)
- [AndroidX Test Library](https://developer.android.com/training/testing/instrumented-tests)
- [Migrating from AndroidTestCase](https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/test-setup)

---

**Last Updated:** 2025-11-21
**Status:** Phase 1 complete, Phase 2 in progress (5/9 tests migrated - 56%)
**Note:** Additional tests added beyond migration (BeerSearcherTest, AppPreferences expansion)
