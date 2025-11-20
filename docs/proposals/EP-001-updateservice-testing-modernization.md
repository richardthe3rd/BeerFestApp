# EP-001: UpdateService Testing and Modernization

**Status:** Proposed (Revision 2.0.0)
**Author:** Development Team
**Created:** 2025-11-20
**Updated:** 2025-11-20 (Revised after review feedback)
**Target Version:** 2025.1.0.0 (cbf2025 maintenance release)

---

## Abstract

Establish comprehensive test coverage for UpdateService and UpdateTask (currently 0% tested) to enable safe refactoring away from deprecated Android APIs (LocalBroadcastManager, AsyncTask) to modern alternatives.

---

## Motivation

### Current Problems

1. **Zero Test Coverage** - UpdateService and UpdateTask are completely untested (0% coverage)
   - **Risk:** No safety net for refactoring or bug fixes
   - **Impact:** High - this is the critical beer list download and update system
   - **Evidence:** See `docs/testing/test-coverage-analysis.md` sections 6.1, 6.2

2. **Deprecated APIs** - Using two deprecated Android components:
   - **LocalBroadcastManager** - Deprecated in AndroidX 1.1.0 (2019)
     - Used for: Progress updates and result notifications to UI
     - Replacement options: LiveData, Callbacks, StateFlow
   - **AsyncTask** - Deprecated in API 30 (Android 11, 2020)
     - Used for: Background beer list download and database updates
     - Replacement options: Coroutines, RxJava, ExecutorService, WorkManager

3. **Maintenance Risk** - Changes to update logic are risky without tests
   - Recent stale data issues (see `docs/troubleshooting/stale-data.md`)
   - No automated verification of update flow
   - Manual testing is time-consuming and error-prone

4. **Annual Update Pain** - Update logic critical for annual festival transitions
   - DB_VERSION increments must trigger updates correctly
   - MD5 comparison logic must work reliably
   - Rating preservation must not fail

### Why Now?

- **Deprecation warnings** increasing with newer Android SDKs
- **Future Android versions** may remove deprecated APIs entirely
- **Technical debt** accumulating - better to address proactively
- **Recent CI/CD improvements** provide infrastructure for robust testing

---

## Proposal

### Overview

A **4-phase, 4-5 week initiative** to:
1. **Introduce unit testing** to BeerFestApp (first-ever use of `app/src/test/` directory)
2. Build comprehensive test coverage (Phase 1-3)
3. Create test infrastructure and utilities (Phase 4)
4. Execute safe refactoring with test safety net (Future phase)

**Note:** This EP introduces **JVM unit tests** (fast, no emulator) alongside existing instrumented tests, establishing a new testing pattern for the project.

### Goals

**Primary Goals:**
- Achieve **80-85% test coverage** on UpdateService, UpdateTask, and update integration flow
- Enable **safe refactoring** away from deprecated APIs
- **Prevent regressions** in critical update functionality
- Establish **testing patterns** for future service development

**Secondary Goals:**
- Improve code quality through testability improvements
- Document update flow behavior via tests
- Reduce manual testing burden
- Increase development velocity (faster, safer changes)

---

## Key Innovation: Introducing Unit Tests to BeerFestApp

### Current State

**BeerFestApp currently has ONLY instrumented tests:**
- All tests located in `app/src/androidTest/` directory
- Tests run on Android emulator or physical device
- Slower execution (2-5 minutes including emulator startup)
- Requires emulator/device for every test run
- Higher CI cost (emulator time)

**The `app/src/test/` directory does not exist** - no JVM unit tests

### Proposed Change

**Introduce JVM unit tests for the first time:**
- Create new `app/src/test/java/ralcock/cbf/` directory structure
- Use Robolectric 4.13 to mock Android framework
- Use MockWebServer 4.12.0 for network mocking
- Use Mockito 5.14.2 for dependency mocking
- Tests run on JVM (no emulator/device needed)
- **Fast execution: seconds instead of minutes**

### Benefits

| Aspect | Instrumented Tests (Existing) | Unit Tests (NEW) |
|--------|-------------------------------|------------------|
| **Execution Environment** | Android emulator/device | JVM (host machine) |
| **Speed** | 2-5 minutes | 10-30 seconds |
| **Setup Required** | Emulator startup | None |
| **CI Cost** | Higher (emulator) | Lower (JVM only) |
| **Debugging** | Slower, device logs | Faster, IDE debugger |
| **Best For** | UI interactions, full integration | Business logic, network, database |
| **Flakiness** | Higher (device/emulator issues) | Lower (deterministic) |

**Strategy:** Use **both** - unit tests for fast feedback on logic, instrumented tests for UI verification.

### New Directory Structure

```
app/
├── src/
│   ├── main/java/ralcock/cbf/              # Production code
│   │   ├── service/
│   │   │   ├── UpdateService.java
│   │   │   └── UpdateTask.java
│   │   └── ...
│   │
│   ├── test/java/ralcock/cbf/              # NEW: JVM unit tests
│   │   ├── service/
│   │   │   ├── UpdateTaskTest.java         # 33 tests
│   │   │   ├── UpdateServiceTest.java      # 18 tests
│   │   │   └── UpdateTaskTestHelper.java
│   │   └── testutil/
│   │       ├── TestDataFactory.java
│   │       └── MockAppPreferences.java
│   │
│   └── androidTest/java/ralcock/cbf/       # EXISTING: Instrumented tests
│       ├── service/
│       │   ├── UpdateServiceIntegrationTest.java  # 15 tests
│       │   └── UpdateServiceIdlingResource.java
│       └── ... (existing tests)
```

### New Dependencies

**Add to `app/build.gradle`:**

```gradle
dependencies {
    // ... existing dependencies ...

    // JVM Unit Test Dependencies (NEW)
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.robolectric:robolectric:4.13'
    testImplementation 'org.mockito:mockito-core:5.14.2'
    testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
    testImplementation 'androidx.test:core:1.5.0'
    testImplementation 'androidx.test.ext:junit:1.1.5'

    // Existing instrumented test dependencies (KEEP)
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'org.easymock:easymock:3.6'
}
```

**Why these versions:**
- **Robolectric 4.13** - Latest stable, compatible with AGP 8.7.3 and compileSdkVersion 33
- **Mockito 5.14.2** - Latest stable 5.x (requires Java 8+, which we have)
- **MockWebServer 4.12.0** - Part of OkHttp 4.x (stable, well-documented, consistent with potential future use)
- **JUnit 4.13.2** - Latest JUnit 4 (project uses JUnit 4, not JUnit 5)

**Why Mockito over EasyMock:**
- Better support for AsyncTask and callback mocking
- More active maintenance and community support
- Cleaner syntax for stubbing and verification
- Existing tests use EasyMock, but new tests will use Mockito (gradual migration)

### Gradle Configuration

**Add to `app/build.gradle`:**

```gradle
android {
    // ... existing config ...

    testOptions {
        unitTests {
            includeAndroidResources = true  // Required for Robolectric
            returnDefaultValues = true
        }
    }
}
```

### Test Configuration Strategy

**Problem:** Tests need to override `beer_list_url` to point to MockWebServer

**Solution:** Build flavor with BuildConfig override

```gradle
// app/build.gradle
android {
    flavorDimensions "environment"
    productFlavors {
        production {
            dimension "environment"
            buildConfigField "String", "BEER_LIST_URL",
                "\"https://www.camra.org.uk/cbf2025.json\""
        }
        test {
            dimension "environment"
            buildConfigField "String", "BEER_LIST_URL",
                "\"http://localhost:8080/beers.json\""  // MockWebServer
        }
    }
}
```

**Usage in tests:**
```java
@Test
public void testDownload() throws Exception {
    fMockServer.start(8080);  // Use port from BuildConfig
    fMockServer.enqueue(new MockResponse().setBody(TestDataFactory.createValidBeerJSON(10)));

    // UpdateTask uses BuildConfig.BEER_LIST_URL in test flavor
    UpdateTask.Result result = executeUpdateTask();

    assertTrue(result instanceof UpdateTask.UpdateResult);
}
```

**Alternative (if build flavors are too complex):** Dependency injection via constructor parameter.

### Alternative Considered: Refactor to Library

**Option:** Move UpdateTask/UpdateService to `libraries/beers/` module for easier unit testing

**Pros:**
- Library module has no Android dependencies (easier to test)
- Cleaner separation of concerns
- Could be reused in other apps

**Cons:**
- **UpdateService requires Android Context, NotificationManager** - inherently Android-specific
- **UpdateTask tightly coupled to BeerDatabaseHelper** - uses OrmLite (Android)
- **Significant refactoring required before testing** - defeats "test first" approach
- **Larger scope, longer timeline** - 2-3 additional weeks
- **High risk** - no safety net during refactoring

**Decision:** **Rejected** - Keep UpdateService/UpdateTask in `app/` module. Use Robolectric to mock Android dependencies. Refactoring to library can be considered in a future EP if needed.

---

## Detailed Design

### Database Testing Setup

**Challenge:** BeerDatabaseHelper extends OrmLiteSqliteOpenHelper, requires Android Context

**Solution:** Use Robolectric's RuntimeEnvironment to provide Context, create in-memory database

**Complete Setup Pattern:**

```java
@RunWith(RobolectricTestRunner.class)
public class UpdateTaskTest {
    private MockWebServer fMockServer;
    private BeerDatabaseHelper fDbHelper;
    private UpdateTask fUpdateTask;

    @Before
    public void setUp() throws IOException {
        // Start MockWebServer
        fMockServer = new MockWebServer();
        fMockServer.start();

        // Create in-memory database
        final Context context = RuntimeEnvironment.getApplication();
        fDbHelper = new BeerDatabaseHelper(
            context,
            null,  // null = in-memory database
            null,  // no factory
            1      // version (reset for each test)
        );
    }

    @After
    public void tearDown() throws Exception {
        // Shutdown MockWebServer
        fMockServer.shutdown();

        // Close database connection
        if (fDbHelper != null) {
            fDbHelper.close();
        }
    }

    @Test
    public void testExample() {
        // Database is fresh for each test
        assertEquals(0, fDbHelper.getBeers().countAll());
    }
}
```

**Key Points:**
- `null` database name creates in-memory SQLite database
- Each test gets fresh database (no pollution between tests)
- `RuntimeEnvironment.getApplication()` provides mock Android Context
- Always close database in `@After` to prevent resource leaks

---

### Phase 1: UpdateTask Unit Tests (Week 1)

**Priority:** 🔴 **CRITICAL**
**Effort:** 5 days
**Coverage Target:** 85%+ (realistic for complex async code)

#### Scope

Test all UpdateTask logic using:
- **MockWebServer 4.12.0** (OkHttp) for network mocking
- **Robolectric 4.13** for Android framework mocking
- **In-memory SQLite database** for database operations
- **Mockito 5.14.2** for dependency mocking

#### Key Test Categories

1. **Network & Download Tests** (6 tests)
   - Successful download and JSON parsing
   - Network timeout handling
   - HTTP error responses (404, 500)
   - Malformed URL handling
   - Partial download handling
   - SSL/TLS error handling

2. **MD5 Digest Tests** (4 tests)
   - Correct MD5 computation via DigestInputStream
   - MD5 string conversion (toMD5String)
   - MD5 match → skip update logic
   - MD5 mismatch → trigger update logic

3. **JSON Parsing Tests** (5 tests)
   - Valid JSON → successful parse
   - Malformed JSON → FailedUpdateResult
   - Empty JSON → NoUpdateRequiredResult
   - Missing required fields → graceful handling
   - Large JSON (1000+ beers) → performance

4. **Database Update Tests** (6 tests)
   - Clean update deletes old data
   - Incremental update preserves user ratings
   - updateFromFestivalOrCreate() upserts correctly
   - Transaction commits successfully
   - Transaction rollback on SQL error
   - Concurrent update handling

5. **Update Decision Tests** (5 tests)
   - updateDue() returns true when time expired
   - updateDue() returns true when beer count is 0
   - updateDue() returns false when not due
   - needsUpdate() compares MD5 correctly
   - Clean update bypasses due check

6. **Progress Reporting Tests** (3 tests)
   - Progress published for each beer processed
   - Progress values correct (count, total)
   - onProgressUpdate() called on UI thread

7. **Result Handling Tests** (4 tests)
   - UpdateResult contains correct count and digest
   - NoUpdateRequiredResult when MD5 matches
   - FailedUpdateResult on IOException
   - FailedUpdateResult on SQLException

**Total:** 33 unit tests

#### File Structure

```
app/src/test/java/ralcock/cbf/service/
├── UpdateTaskTest.java          (main test class, 33 tests)
└── UpdateTaskTestHelper.java    (test utilities)
```

#### Example Test Pattern (with Hungarian Notation)

```java
@RunWith(AndroidJUnit4.class)
public class UpdateTaskTest {
    private MockWebServer fMockServer;
    private BeerDatabaseHelper fDbHelper;
    private UpdateTask fUpdateTask;

    @Before
    public void setUp() throws IOException {
        fMockServer = new MockWebServer();
        fMockServer.start();

        // Use in-memory database (null name = in-memory)
        final Context context = RuntimeEnvironment.getApplication();
        fDbHelper = new BeerDatabaseHelper(context, null, null, 1);
    }

    @Test
    public void testSuccessfulDownloadAndUpdate() throws Exception {
        // Arrange
        final String validJson = TestDataFactory.createValidBeerJSON(10);
        fMockServer.enqueue(new MockResponse()
            .setBody(validJson)
            .setHeader("Content-Type", "application/json"));

        // Act
        final UpdateTask.Result result = executeUpdateTask(fMockServer.url("/beers.json"));

        // Assert
        assertTrue(result instanceof UpdateTask.UpdateResult);
        assertEquals(10, ((UpdateTask.UpdateResult) result).getCount());
        assertEquals(10, fDbHelper.getBeers().countAll());
    }

    @After
    public void tearDown() throws Exception {
        fMockServer.shutdown();
        if (fDbHelper != null) {
            fDbHelper.close();
        }
    }
}
```

---

### Phase 2: UpdateService Unit Tests (Week 2)

**Priority:** 🔴 **CRITICAL**
**Effort:** 4-5 days
**Coverage Target:** 80%+ (service lifecycle complexity)

#### Scope

Test UpdateService lifecycle, notification management, and broadcast sending using Robolectric.

#### Key Test Categories

1. **Service Lifecycle Tests** (4 tests)
   - onCreate() initializes LocalBroadcastManager
   - onStartCommand() creates notification
   - onStartCommand() passes cleanUpdate flag
   - onDestroy() cleanup

2. **Notification Tests** (6 tests)
   - Notification created with correct title and icon
   - Notification has PendingIntent for app launch
   - Notification progress bar updates correctly
   - Notification cancelled when count is 0
   - Notification updated to completion state
   - Notification channel properly configured

3. **Broadcast Tests** (5 tests)
   - Progress broadcast sent with correct action
   - Progress broadcast contains Progress extra
   - Result broadcast sent with correct action
   - Result broadcast contains Result extra
   - Broadcasts sent via LocalBroadcastManager

4. **Intent Handling Tests** (3 tests)
   - Default update intent (no extras)
   - Clean update intent (CLEAN_UPDATE=true)
   - Null intent handling (defensive)

**Total:** 18 unit tests

#### File Structure

```
app/src/test/java/ralcock/cbf/service/
└── UpdateServiceTest.java
```

#### Example Test Pattern

```java
@RunWith(AndroidJUnit4.class)
public class UpdateServiceTest {
    private ServiceController<UpdateService> controller;

    @Test
    public void testNotificationCreatedOnServiceStart() {
        // Arrange
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
            UpdateService.class);

        // Act
        controller = Robolectric.buildService(UpdateService.class, intent);
        controller.create().startCommand(0, 1);

        UpdateService service = controller.get();

        // Assert
        NotificationManager notificationManager =
            (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        ShadowNotificationManager shadowManager = shadowOf(notificationManager);

        assertEquals(1, shadowManager.size());

        Notification notification = shadowManager.getAllNotifications().get(0);
        assertEquals("Updating Beers", getNotificationTitle(notification));

        controller.destroy();
    }

    @Test
    public void testProgressBroadcastSentWithCorrectExtras() {
        // Arrange
        Application app = ApplicationProvider.getApplicationContext();
        ShadowApplication shadowApp = shadowOf(app);

        controller = Robolectric.buildService(UpdateService.class);
        controller.create().startCommand(0, 1);

        UpdateTask.Progress progress = new UpdateTask.Progress(5, 10);

        // Act
        // Trigger progress update (via UpdateTask callback)
        ShadowLooper.idleMainLooper();

        // Assert
        List<Intent> broadcasts = shadowApp.getBroadcastIntents();
        Intent progressBroadcast = findBroadcast(broadcasts,
            UpdateService.UPDATE_SERVICE_PROGRESS);

        assertNotNull(progressBroadcast);
        UpdateTask.Progress receivedProgress =
            (UpdateTask.Progress) progressBroadcast.getSerializableExtra(
                UpdateService.PROGRESS_EXTRA);
        assertEquals(5, receivedProgress.getCount());
        assertEquals(10, receivedProgress.getSize());
    }
}
```

---

### Phase 3: Integration Tests (Week 2, Days 4-5)

**Priority:** 🔴 **CRITICAL**
**Effort:** 2 days
**Coverage Target:** 80%+

#### Scope

Test end-to-end update flow from service start to UI refresh using instrumented tests.

#### Key Test Categories

1. **End-to-End Update Flow** (4 tests)
   - Successful update flow: trigger → download → DB update → UI refresh
   - Clean update flow: trigger → delete old data → download → UI refresh
   - No update needed flow: MD5 match → skip update → Toast shown
   - Failed update flow: network error → Toast shown

2. **Activity Lifecycle Integration** (3 tests)
   - Update completes while activity paused
   - Broadcast received after activity resumed
   - Receiver properly unregistered on pause

3. **UI Refresh Integration** (3 tests)
   - BeerListFragment refreshes after update
   - ListAdapter notified of data changes
   - Toast shown on failure

4. **Concurrent Update Tests** (2 tests)
   - Second update while first running is queued
   - Multiple rapid triggers handled gracefully

5. **Preference Integration** (3 tests)
   - MD5 saved to AppPreferences after success
   - Next update time saved correctly (now + 4 hours)
   - Preferences unchanged on failure

**Total:** 15 integration tests

#### File Structure

```
app/src/androidTest/java/ralcock/cbf/service/
├── UpdateServiceIntegrationTest.java
└── UpdateServiceIdlingResource.java  (Espresso IdlingResource)
```

#### Example Test Pattern

```java
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UpdateServiceIntegrationTest {
    @Rule
    public ActivityScenarioRule<CamBeerFestApplication> activityRule =
        new ActivityScenarioRule<>(CamBeerFestApplication.class);

    private MockWebServer mockServer;
    private UpdateServiceIdlingResource idlingResource;

    @Before
    public void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        // Override beer_list_url to point to mockServer
        // (via BuildConfig or test flavor)

        idlingResource = new UpdateServiceIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void testSuccessfulUpdateRefreshesUI() throws Exception {
        // Arrange
        String beerJson = TestDataFactory.createValidBeerJSON(5);
        mockServer.enqueue(new MockResponse()
            .setBody(beerJson)
            .setHeader("Content-Type", "application/json"));

        // Act - Trigger update via menu
        openActionBarOverflowOrOptionsMenu(
            ApplicationProvider.getApplicationContext());
        onView(withText("Refresh Database")).perform(click());

        // Wait for update to complete (IdlingResource handles this)

        // Assert - Verify UI updated
        onView(withId(R.id.beer_list_recycler_view))
            .check(matches(hasDescendant(withText("Beer 0"))));

        // Verify preferences updated
        String md5 = AppPreferences.getLastUpdateMD5(
            ApplicationProvider.getApplicationContext());
        assertNotNull(md5);
        assertNotEquals("", md5);

        // Verify request made correctly
        RecordedRequest request = mockServer.takeRequest();
        assertTrue(request.getPath().contains("/beers.json"));
    }

    @After
    public void tearDown() throws Exception {
        IdlingRegistry.getInstance().unregister(idlingResource);
        mockServer.shutdown();
    }
}
```

---

### Phase 4: Test Infrastructure (Week 3)

**Priority:** 🟡 **HIGH**
**Effort:** 5 days
**Coverage Target:** N/A (infrastructure)

#### Deliverables

1. **Test Data Factory** (`app/src/test/java/ralcock/cbf/testutil/TestDataFactory.java`)
   - createValidBeerJSON(int count)
   - createMalformedBeerJSON()
   - createEmptyBeerJSON()
   - createTestBeer(String name, float abv)
   - createBeerList(int count)

2. **Mock AppPreferences** (`app/src/test/java/ralcock/cbf/testutil/MockAppPreferences.java`)
   - Testable SharedPreferences wrapper
   - In-memory preference storage for tests

3. **Service Idling Resource** (`app/src/androidTest/java/ralcock/cbf/testutil/UpdateServiceIdlingResource.java`)
   - Espresso IdlingResource for service completion
   - Monitors UpdateService state

4. **Test Configuration**
   - gradle.properties settings for test optimization
   - test build flavor with mock URLs
   - Code coverage configuration

5. **Documentation**
   - Testing guide (`docs/testing/service-testing-guide.md`)
   - Test pattern examples
   - CI/CD integration instructions

#### Test Data Factory Example

```java
public class TestDataFactory {

    public static String createValidBeerJSON(int count) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"producers\": [");

        // Generate breweries
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"name\":\"Brewery %d\",\"location\":\"Location %d\"}", i, i));
        }

        json.append("],\"products\": [");

        // Generate beers
        for (int i = 0; i < count; i++) {
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"name\":\"Beer %d\",\"abv\":%.1f,\"producer\":\"Brewery %d\"}",
                i, 4.0 + (i % 10), i));
        }

        json.append("]}");
        return json.toString();
    }

    public static String createMalformedBeerJSON() {
        return "{invalid json}";
    }

    public static Beer createTestBeer(String name, float abv) {
        Beer beer = new Beer();
        beer.setName(name);
        beer.setAbv(abv);
        beer.setBrewery(createTestBrewery("Test Brewery"));
        return beer;
    }

    private static Brewery createTestBrewery(String name) {
        Brewery brewery = new Brewery();
        brewery.setName(name);
        brewery.setLocation("Test Location");
        return brewery;
    }
}
```

---

## Future Work: Refactoring Phase (Week 4+)

### Refactoring Options Evaluated

Once comprehensive test coverage is in place, evaluate three modernization approaches:

#### Option A: LiveData + ViewModel (Recommended)

**Pros:**
- Modern, lifecycle-aware
- Built-in support in AndroidX
- Reactive pattern fits well
- Good for UI updates

**Cons:**
- Requires ViewModel introduction
- More architectural changes

**Migration Path:**
```
UpdateService → UpdateRepository (LiveData) → ViewModel → Activity/Fragment
```

#### Option B: WorkManager

**Pros:**
- Designed for background work
- Survives process death
- Handles constraints (network, battery)
- Job scheduling built-in

**Cons:**
- Overkill for simple updates
- More complex setup
- May not fit "on-demand" update pattern

**Best For:** Future periodic background updates

#### Option C: Direct Callbacks

**Pros:**
- Simpler, less abstraction
- Easier migration from broadcasts
- No new dependencies

**Cons:**
- Manual lifecycle management
- Not as modern/reactive
- More boilerplate

**Migration Path:**
```
UpdateService → UpdateCallback interface → Activity implements callback
```

### Refactoring Checklist

**Pre-Refactoring:**
- [ ] All Phase 1-4 tests passing (66 tests)
- [ ] Code coverage ≥ 80% on update components
- [ ] Tests run successfully in CI
- [ ] Team review of test suite completed
- [ ] Refactoring approach agreed upon
- [ ] Branch created for refactoring work

**During Refactoring:**
- [ ] Tests remain passing at each step
- [ ] No test modifications needed (behavior preserved)
- [ ] Incremental commits (one component at a time)
- [ ] Code review after each major change

**Post-Refactoring:**
- [ ] All tests still passing
- [ ] New implementation tested
- [ ] LocalBroadcastManager removed
- [ ] AsyncTask removed
- [ ] UI refresh verified manually
- [ ] Notifications verified manually
- [ ] Performance comparable or better
- [ ] Update CLAUDE.md and architecture docs

---

## Test Execution Strategy

### Local Development

```bash
# Run unit tests (fast, < 30 seconds)
./gradlew testDebugUnitTest

# Run instrumented tests (slower, 2-3 minutes)
./gradlew connectedDebugAndroidTest

# Run all tests
./gradlew test connectedCheck

# Generate coverage report
./gradlew testDebugUnitTestCoverage
open app/build/reports/coverage/test/debug/index.html
```

### CI/CD Integration

**GitHub Actions Workflow:**
```yaml
test:
  runs-on: ubuntu-latest
  steps:
    - name: Run Unit Tests
      run: ./gradlew testDebugUnitTest

    - name: Run Instrumented Tests
      run: ./gradlew connectedDebugAndroidTest

    - name: Generate Coverage Report
      run: ./gradlew testDebugUnitTestCoverage

    - name: Upload Coverage
      uses: codecov/codecov-action@v3
      with:
        files: app/build/reports/coverage/test/debug/report.xml
```

**Coverage Thresholds:**
- UpdateTask: 85% minimum
- UpdateService: 80% minimum
- Overall update flow: 80% minimum
- Fail build if coverage drops below thresholds

---

## Success Criteria

### Quantitative Metrics

| Metric | Current | Target | Measured By |
|--------|---------|--------|-------------|
| **UpdateTask Coverage** | 0% | 85%+ | JaCoCo report |
| **UpdateService Coverage** | 0% | 80%+ | JaCoCo report |
| **Integration Coverage** | 0% | 80%+ | JaCoCo report |
| **Test Count** | 0 | 66 | Test reports |
| **Test Execution Time** | N/A | < 5 min | CI logs |
| **Flaky Tests** | N/A | 0 | Test stability |

### Qualitative Criteria

**Phase 1-3 Complete When:**
- ✅ All 66 tests passing locally and in CI
- ✅ MockWebServer successfully mocking network calls
- ✅ Robolectric tests running without real devices
- ✅ Integration tests verifying UI refresh
- ✅ Code coverage reports generated automatically
- ✅ Test execution time under 5 minutes
- ✅ Zero flaky tests (100% reliable)

**Phase 4 Complete When:**
- ✅ Test utilities documented and reusable
- ✅ Testing guide written
- ✅ CI/CD configured with coverage gates
- ✅ Team trained on test patterns

**Refactoring Phase Complete When:**
- ✅ All deprecated APIs removed
- ✅ Modern alternatives implemented
- ✅ All existing tests still passing (no modifications)
- ✅ Manual testing confirms all features work
- ✅ Performance equal or better than before
- ✅ Documentation updated

---

## Risks and Mitigations

### Risk 1: Test Flakiness

**Risk:** Async tests (especially instrumented) may be flaky
**Probability:** Medium
**Impact:** High (slows development, reduces confidence)

**Mitigation:**
- Use Espresso IdlingResources for async operations
- Use Awaitility for waiting on conditions
- Avoid Thread.sleep() - use proper synchronization
- Run tests multiple times in CI to detect flakiness
- Fix flaky tests immediately (top priority)

### Risk 2: Test Maintenance Burden

**Risk:** Large test suite becomes maintenance burden
**Probability:** Low
**Impact:** Medium (slows feature development)

**Mitigation:**
- Build reusable test utilities (TestDataFactory)
- Follow DRY principle in tests
- Document test patterns clearly
- Review test design during code reviews
- Refactor tests alongside production code

### Risk 3: Incomplete Coverage

**Risk:** Tests miss critical edge cases
**Probability:** Medium
**Impact:** High (bugs in production)

**Mitigation:**
- Systematic test planning (this document)
- Code review with focus on test coverage
- Coverage reports with minimum thresholds
- Manual exploratory testing after automation
- Bug fixes include regression tests

### Risk 4: Refactoring Breaks Functionality

**Risk:** Moving away from broadcasts breaks update flow
**Probability:** Low (if tests are comprehensive)
**Impact:** Critical (app unusable)

**Mitigation:**
- **Tests are the safety net** - this is why we're doing this!
- Incremental refactoring (one component at a time)
- Feature flags for gradual rollout
- Beta testing phase before production release
- Rollback plan if issues discovered

### Risk 5: Timeline Slippage

**Risk:** 4-5 week timeline extends longer
**Probability:** Medium
**Impact:** Medium (delays other work)

**Mitigation:**
- Clear phase boundaries and deliverables
- Daily progress tracking
- Prioritize Phase 1 (most critical)
- Accept good-enough coverage (80%) vs perfect (100%)
- Defer refactoring phase if needed (tests still valuable)

---

## Alternatives Considered

### Alternative 1: Refactor Without Tests

**Description:** Just rewrite UpdateService/UpdateTask with modern APIs, rely on manual testing

**Pros:**
- Faster short-term (no test writing)
- Simpler (less code to write)

**Cons:**
- **Extremely risky** - update flow is critical
- **No safety net** - hard to catch regressions
- **Manual testing burden** - time-consuming, error-prone
- **Future changes risky** - no automated verification

**Verdict:** ❌ **Rejected** - Risk far too high for critical functionality

### Alternative 2: Integration Tests Only

**Description:** Skip unit tests, write only end-to-end integration tests

**Pros:**
- Tests real behavior
- Fewer tests to write
- Simpler test setup

**Cons:**
- **Slow execution** (instrumented tests on emulator)
- **Hard to debug** (failures in complex flow)
- **Poor coverage** of edge cases (network errors, etc.)
- **Flakier** (more moving parts)

**Verdict:** ❌ **Rejected** - Need fast unit tests for rapid feedback

### Alternative 3: Test After Refactoring

**Description:** Refactor first, then add tests to new implementation

**Pros:**
- Write tests for cleaner, modern code
- No "throwaway" tests for old code

**Cons:**
- **No safety net during refactoring** - very risky
- **Can't verify behavior preservation** - no baseline
- **Defeats the purpose** - tests should enable safe refactoring

**Verdict:** ❌ **Rejected** - Tests must come first to enable safe refactoring

### Alternative 4: Minimal Tests (Happy Path Only)

**Description:** Write only a few tests covering successful update path

**Pros:**
- Faster to implement
- Some safety net
- Better than nothing

**Cons:**
- **Missing critical edge cases** (network errors, database failures)
- **False confidence** - tests pass but app has bugs
- **Not sufficient** for safe refactoring

**Verdict:** ❌ **Rejected** - Need comprehensive coverage for confidence

---

## Dependencies

### Technical Dependencies

- **MockWebServer 4.12.0** - OkHttp's testing library for network mocking
- **Robolectric 4.13** - Android framework mocking for unit tests
- **Mockito 5.14.2** - Mocking library for Java
- **Awaitility 4.2.0** - Async test utilities
- **AndroidX Test 1.5.0** - Instrumented testing framework
- **Espresso 3.5.0** - UI testing framework

### External Dependencies

- **CI/CD pipeline** - GitHub Actions (already in place)
- **Emulator** - Android API 34 for instrumented tests (already configured)
- **Code coverage** - JaCoCo (need to configure)

### Human Dependencies

- **Development team** - 1 developer, 4-5 weeks focused effort
- **Code reviewer** - For test design and implementation review
- **QA** - Manual testing after refactoring phase

---

## Assumptions

This EP assumes the following:

1. **CI Test Performance Issues Being Addressed Separately**
   - Current instrumented test hangs (30-40 min) documented in test-coverage-analysis.md
   - Assumed to be resolved independently (work in progress)
   - If not resolved by Phase 3, integration tests will be deferred

2. **Existing Infrastructure Available**
   - JaCoCo already configured (build.gradle line 78)
   - GitHub Actions CI/CD already set up
   - Android SDK and emulator already configured for instrumented tests

3. **Team Capacity**
   - 1 developer available for 4-5 weeks focused effort
   - Code reviewer available for timely reviews
   - No blocking dependencies on other work

4. **Technical Prerequisites**
   - JDK 17 (Temurin) available - ✅ already in use
   - Gradle 8.10.2 - ✅ already upgraded
   - AGP 8.7.3 - ✅ already upgraded

5. **Scope Boundaries**
   - Refactoring phase (replacing deprecated APIs) is SEPARATE
   - This EP covers testing only
   - Refactoring will be addressed in future EP/ADR once tests are in place

---

## Timeline and Milestones

**Total Duration: 4-5 weeks** (1 week buffer added for reviews, debugging, CI setup)

### Week 1: UpdateTask Unit Tests + Setup
- **Day 1:** Set up unit test infrastructure (gradle config, directory structure)
- **Days 2-3:** Network and download tests (MockWebServer setup)
- **Day 4:** MD5 digest, JSON parsing, and error handling tests
- **Day 5:** Database transaction and update decision tests

**Milestone:** 33 UpdateTask unit tests passing, 85%+ coverage

### Week 2: UpdateService Unit Tests
- **Days 1-2:** UpdateService lifecycle tests (Robolectric)
- **Days 3-4:** Notification management tests
- **Day 5:** Broadcast sending tests, code review

**Milestone:** 18 service tests passing, 80%+ coverage

### Week 3: Integration Tests + Infrastructure
- **Days 1-3:** Integration tests (Espresso + ActivityScenario)
  - **Conditional:** Only if CI test performance issues resolved
  - **Alternative:** Mock-based integration tests if instrumented tests still problematic
- **Days 4-5:** Test utilities (TestDataFactory, IdlingResource, MockAppPreferences)

**Milestone:** 15 integration tests passing (or mocked equivalents), test utilities complete

### Week 4: CI/CD + Documentation
- **Days 1-2:** CI/CD configuration (separate unit test job, coverage gates)
- **Day 3:** Documentation (testing guide, patterns, examples)
- **Days 4-5:** Code review, adjustments, buffer for issues

**Milestone:** Tests running in CI with coverage gates, documentation complete

### Week 5: Buffer + Refactoring Prep (if needed)
- **Days 1-2:** Address flaky tests, fix edge cases
- **Day 3:** Team review of test suite
- **Days 4-5:** Finalize refactoring approach (prepare for future EP/ADR)

**Milestone:** Ready to execute safe refactoring in future phase

---

## Resolved Questions

**All questions from Revision 1 have been resolved in Revision 2:**

1. **Q:** Should we test UpdateService with real AsyncTask or mock it?
   - **A:** ✅ Test with real AsyncTask in Phase 1-2, verify actual behavior. Mock only when necessary for isolation.

2. **Q:** How do we override beer_list_url for tests to point to MockWebServer?
   - **A:** ✅ **RESOLVED** - Use build flavor with BuildConfig override (see "Test Configuration Strategy" section)

3. **Q:** What happens to tests after we refactor away from AsyncTask?
   - **A:** ✅ Tests should still pass! That's the whole point - verify behavior preservation. Tests may need minor adjustments (mocking strategy) but behavior assertions remain unchanged.

4. **Q:** Should we test UpdateBeersProgressDialogFragment (currently dead code)?
   - **A:** ✅ No - remove dead code in separate cleanup task (not in scope for this EP)

5. **Q:** Do we need to test on multiple API levels?
   - **A:** ✅ Yes - Robolectric can simulate different SDK levels. Test on min SDK (14), target SDK (34) via `@Config(sdk = {14, 34})`

6. **Q:** Where should tests be located?
   - **A:** ✅ **RESOLVED** - Unit tests in NEW `app/src/test/` directory, instrumented tests in EXISTING `app/src/androidTest/`

7. **Q:** Which testing frameworks to use?
   - **A:** ✅ **RESOLVED** - Robolectric 4.13 + Mockito 5.14.2 + MockWebServer 4.12.0 (versions justified in Dependencies section)

---

## References

### Internal Documentation
- [Test Coverage Analysis](../testing/test-coverage-analysis.md)
- [Troubleshooting: Stale Data](../troubleshooting/stale-data.md)
- [CLAUDE.md - Pain Points](../../CLAUDE.md)
- [CI/CD Pipeline Optimization](../cicd/pipeline-optimization.md)

### External Resources
- [Robolectric Documentation](http://robolectric.org/)
- [MockWebServer Documentation](https://github.com/square/okhttp/tree/master/mockwebserver)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AndroidX Test Guide](https://developer.android.com/training/testing/instrumented-tests)
- [Espresso Testing Guide](https://developer.android.com/training/testing/espresso)

### Android API Deprecations
- [LocalBroadcastManager Deprecation](https://developer.android.com/reference/androidx/localbroadcastmanager/content/LocalBroadcastManager)
- [AsyncTask Deprecation](https://developer.android.com/reference/android/os/AsyncTask)

---

## Approval

**Decision Makers:**
- [ ] Lead Developer
- [ ] Product Owner
- [ ] Technical Lead

**Approval Date:** _Pending_

**Status After Approval:**
- Approved → Status becomes "Accepted", proceed to implementation
- Rejected → Status becomes "Rejected", document reasons
- Deferred → Status becomes "Deferred", revisit timeline

---

## Revision History

| Date | Version | Author | Changes |
|------|---------|--------|---------|
| 2025-11-20 | 1.0.0 | Dev Team | Initial proposal |
| 2025-11-20 | 2.0.0 | Dev Team | Major revision addressing review feedback |

### Changes in Revision 2.0.0

**Critical Issues Addressed:**

1. **✅ Test Location Resolved**
   - Added "Introducing Unit Tests to BeerFestApp" section
   - Documented that this creates FIRST-EVER `app/src/test/` directory
   - Explained JVM unit tests vs existing instrumented tests
   - Provided complete directory structure

2. **✅ Timeline Updated**
   - Changed from "3-4 weeks" to "4-5 weeks"
   - Added 1 week buffer for debugging, reviews, CI setup
   - Broke down Week 3 to be conditional on CI test fix
   - Added Week 5 buffer for unexpected issues

3. **✅ Code Conventions Fixed**
   - All code examples now use Hungarian notation (`fMockServer`, `fDbHelper`, `fUpdateTask`)
   - Added `final` modifiers to local variables where appropriate
   - Consistent with CLAUDE.md conventions

4. **✅ Database Testing Details Added**
   - New "Database Testing Setup" section
   - Complete `@Before/@After` example with Robolectric
   - Explains in-memory database creation (`null` name)
   - Shows proper cleanup to prevent resource leaks

5. **✅ MockWebServer URL Override Resolved**
   - New "Test Configuration Strategy" section
   - Concrete solution: BuildConfig with build flavors
   - Complete gradle configuration example
   - Alternative noted (dependency injection)

6. **✅ Dependencies Updated**
   - Robolectric: 4.11 (proposed) → 4.13 (latest stable)
   - Mockito: Corrected to 5.14.2 (Java 8 compatible)
   - MockWebServer: Corrected to 4.12.0 (stable OkHttp 4.x)
   - Justified each version choice

7. **✅ Assumptions Added**
   - New "Assumptions" section documents prerequisites
   - CI test performance issues being fixed separately
   - Existing infrastructure (JaCoCo, GitHub Actions)
   - Team capacity and scope boundaries

**Important Additions:**

8. **✅ Alternative Analysis**
   - Added "Alternative Considered: Refactor to Library"
   - Explained why keeping in app/ module (uses Robolectric instead)
   - Justified decision with pros/cons

9. **✅ Coverage Targets Adjusted**
   - UpdateTask: 90% → 85% (realistic for async code)
   - UpdateService: 85% → 80% (service lifecycle complexity)

10. **✅ Open Questions Resolved**
    - All 7 questions now have concrete answers
    - Renamed section to "Resolved Questions"

**Minor Improvements:**

11. Target version updated: 2026.0.0.0 → 2025.1.0.0 (cbf2025 maintenance)
12. File structure examples updated to show test counts
13. Total test count corrected: "50+ tests" → "66 tests" (33+18+15)
14. Added explanation: Why Mockito over EasyMock
15. Phase 2 effort updated: 3 days → 4-5 days (more realistic)

**Review Feedback Incorporated:**
- ✅ All 5 critical issues from enhancement-proposal-reviewer addressed
- ✅ All 6 important issues addressed
- ✅ Timeline feasibility concerns resolved
- ✅ Technical accuracy improved
- ✅ Project conventions followed

---

**Next Steps:**
1. Review this EP with team
2. Address open questions
3. Get approval from decision makers
4. Create tracking issues for each phase
5. Begin Phase 1: UpdateTask unit tests
