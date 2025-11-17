# Test Migration Plan: Legacy to Modern Framework

## Status: Planned

**Priority:** Medium
**Effort:** 2-3 hours
**Impact:** Improved maintainability, better tooling, modern test framework

---

## Overview

The BeerFestApp currently has **8 instrumented tests**:
- **7 legacy tests** in `app/tests/src/` (ANT-based, AndroidTestCase framework)
- **1 modern test** in `app/src/androidTest/` (Gradle-based, AndroidJUnit4 framework)

**Current state (as of Nov 2025):**
- ✅ Quick fix implemented: Gradle configured to run legacy tests via sourceSets
- ❌ Tests still use deprecated `AndroidTestCase` framework (pre-2013)
- ❌ Tests not in standard Gradle location

**Goal:** Migrate all tests to modern AndroidJUnit4 framework in standard location.

---

## Current Test Inventory

### Legacy Tests (app/tests/src/)

| Test File | Lines | Test Methods | Dependencies |
|-----------|-------|--------------|--------------|
| `LifecycleTest.java` | ? | ? | AndroidTestCase |
| `CamBeerFestApplicationTest.java` | ? | ? | AndroidTestCase |
| `actions/BeerSharerTest.java` | 84 | 3 | AndroidTestCase, Resources |
| `model/JsonBeerListTest.java` | 78 | 2 | AndroidTestCase, JSON resources |
| `model/BeerListTest.java` | ? | ? | AndroidTestCase |
| `model/dao/BeersImplTest.java` | ? | ? | AndroidTestCase, OrmLite |
| `model/dao/BreweriesImplTest.java` | ? | ? | AndroidTestCase, OrmLite |

**Total:** 7 test classes

### Modern Tests (app/src/androidTest/)

| Test File | Lines | Test Methods | Framework |
|-----------|-------|--------------|-----------|
| `CamBeerFestApplicationInstrumentedTest.java` | 28 | 1 | AndroidJUnit4, Espresso |

**Total:** 1 test class

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
2. `LifecycleTest.java` (likely simple state tests)
3. `BeerSharerTest.java` (Intent tests, medium complexity)
4. `JsonBeerListTest.java` (requires resource migration)
5. `BeerListTest.java` (unknown complexity)
6. `CamBeerFestApplicationTest.java` (may overlap with instrumented test)
7. `BeersImplTest.java` (DAO/database tests, complex)
8. `BreweriesImplTest.java` (DAO/database tests, complex)

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
| Phase 2: Migrate 7 tests | 2-3 hours | ~15-25 min per test |
| Phase 3: Cleanup | 15 min | Delete old files, update docs |
| **Total** | **2.5-3.5 hours** | Can be done incrementally |

---

## Success Criteria

- [ ] All 8 tests migrated to `app/src/androidTest/`
- [ ] All tests use AndroidJUnit4 framework
- [ ] All tests pass in CI (`./gradlew connectedCheck`)
- [ ] `app/tests/` directory deleted
- [ ] `sourceSets` configuration removed from `app/build.gradle`
- [ ] Documentation updated

---

## References

- [Android Testing Guide](https://developer.android.com/training/testing)
- [AndroidX Test Library](https://developer.android.com/training/testing/instrumented-tests)
- [Migrating from AndroidTestCase](https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/test-setup)

---

**Last Updated:** 2025-11-17
**Status:** Phase 1 complete, Phase 2 pending
