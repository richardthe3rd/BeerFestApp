# Phase 3 Quick Reference: Critical Integration Tests

**TL;DR:** Add 20 tests to enable safe refactoring of AsyncTask/LocalBroadcastManager

---

## Why We Need Phase 3

**Current Problem:**
```java
// What we test NOW (Phase 1-2):
UpdateTask task = new UpdateTask();
Result result = task.doInBackground(params);  // ❌ Direct call

// What we DON'T test:
task.execute(params);  // ❌ Never tested!
// - Does execute() work?
// - Does onProgressUpdate() get called?
// - Does onPostExecute() get called?
// - Are broadcasts sent?
// - Do notifications update?
```

**We can't safely refactor AsyncTask/LocalBroadcastManager without these tests!**

---

## The 20 Tests We Need

### CRITICAL (Must Have) - 15 tests

#### AsyncTask Lifecycle (5 tests)
1. ✅ Task execute() calls doInBackground()
2. ✅ onProgressUpdate() called during execution
3. ✅ onPostExecute() called after completion
4. ✅ Task cancellation works
5. ✅ Multiple tasks run sequentially

#### Broadcast Tests (5 tests)
6. ✅ Progress broadcast sent during update
7. ✅ Result broadcast sent on success
8. ✅ Result broadcast sent on failure
9. ✅ Multiple progress broadcasts sent
10. ✅ Broadcast data correctness

#### Notification Tests (5 tests)
11. ✅ Notification created on start
12. ✅ Notification updated with progress
13. ✅ Notification shows completion
14. ✅ Notification cancelled on no-update
15. ✅ Notification has correct content

### HIGH (Should Have) - 5 tests

#### Real Params Tests (4 tests) - **Requires UpdateService refactoring**
16. ⚠️ Real updateDue() checks AppPreferences time
17. ⚠️ Real updateDue() checks beer count = 0
18. ⚠️ Real needsUpdate() compares MD5
19. ⚠️ Real Params work end-to-end

#### End-to-End (2 tests) - **Requires MockWebServer**
20. ✅ Complete update flow success
21. ✅ Complete update flow with error

---

## Key Testing Patterns

### Pattern 1: AsyncTask Execution
```java
@Test
public void testAsyncTaskPattern() {
    // Create task with overridden callbacks
    UpdateTask task = new UpdateTask() {
        @Override
        protected void onProgressUpdate(Progress... values) {
            super.onProgressUpdate(values);
            progressCalled.set(true);  // Track callback
        }
    };

    // Execute (not direct call)
    task.execute(params);

    // Wait for async completion
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Verify callbacks invoked
    assertTrue("Callback should be called", progressCalled.get());
}
```

### Pattern 2: Broadcast Verification
```java
@Test
public void testBroadcastPattern() {
    // Register receiver BEFORE starting service
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastReceived.set(true);  // Track receipt
        }
    };
    LocalBroadcastManager.getInstance(context)
        .registerReceiver(receiver,
            new IntentFilter(UpdateService.UPDATE_SERVICE_PROGRESS));

    // Start service
    serviceController.startCommand(0, 1);

    // Wait for async
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Verify broadcast received
    assertTrue("Broadcast should be sent", broadcastReceived.get());

    // ALWAYS cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
}
```

### Pattern 3: Notification Inspection
```java
@Test
public void testNotificationPattern() {
    // Get notification manager shadow
    NotificationManager nm = (NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
    ShadowNotificationManager shadowNM = shadowOf(nm);

    // Start service
    serviceController.startCommand(0, 1);
    ShadowLooper.idleMainLooper();

    // Verify notification
    assertEquals("Should have 1 notification", 1, shadowNM.size());
    Notification notification = shadowNM.getNotification(0);
    assertNotNull("Notification should exist", notification);
}
```

---

## Required Refactoring

### 1. UpdateService URL Injection (HIGH PRIORITY)

**Problem:** Can't test with MockWebServer
```java
// Current - hardcoded URL
URL url = new URL(getString(R.string.beer_list_url));
```

**Solution:**
```java
// UpdateService.java - Add protected method
protected String getBeerListUrl() {
    return getString(R.string.beer_list_url);
}

// In Params.openStream()
URL url = new URL(getBeerListUrl());

// In tests - override
UpdateService testService = new UpdateService() {
    @Override
    protected String getBeerListUrl() {
        return mockServer.url("/beers.json").toString();
    }
};
```

### 2. UpdateService Params Extraction (MEDIUM PRIORITY)

**Problem:** Can't test real Params logic
```java
// Current - anonymous inner class
UpdateTask.Params p = new UpdateTask.Params() { ... };
```

**Solution:**
```java
// UpdateService.java - Extract to method
protected Params createParams(final boolean cleanUpdate) {
    return new Params() { ... };
}

// In tests
Params realParams = service.createParams(false);
assertTrue(realParams.updateDue());
```

---

## Timeline

| Day | Focus | Tests | Time |
|-----|-------|-------|------|
| **0** | UpdateService refactoring | - | 2-4h |
| **1-2** | AsyncTask lifecycle | 1-5 | 8-12h |
| **3-4** | Broadcast tests | 6-10 | 8-12h |
| **5** | Notification tests | 11-15 | 6-8h |
| **6** | Real Params tests | 16-19 | 6-8h |
| **7** | End-to-end tests | 20-21 | 4-6h |

**Total:** 34-50 hours (5-7 days)

**Can be reduced to 3-4 days by:**
- Deferring tests 16-19 (Real Params) to Phase 4
- Simplifying tests 20-21 (End-to-End)
- Focus on CRITICAL tests 1-15 only

---

## Success Criteria

### Minimum (3-4 days)
✅ Tests 1-10 (AsyncTask + Broadcasts) - **CRITICAL**
✅ Tests 11-15 (Notifications) - **HIGH**
✅ Can confidently refactor AsyncTask/LocalBroadcastManager

### Full (5-7 days)
✅ All 20 tests passing
✅ 85%+ coverage on UpdateTask/UpdateService
✅ Real Params logic tested (not just TestParams)
✅ End-to-end flow verified
✅ MockWebServer integrated

---

## Common Robolectric Gotchas

### Gotcha 1: Async Not Running
```java
// ❌ BAD - Test finishes before async completes
task.execute(params);
assertTrue(result);  // FAILS - task not done yet

// ✅ GOOD - Wait for completion
task.execute(params);
Robolectric.flushBackgroundThreadScheduler();  // Run background threads
ShadowLooper.idleMainLooper();                 // Process callbacks
assertTrue(result);  // PASSES - task completed
```

### Gotcha 2: Broadcast Not Received
```java
// ❌ BAD - Register after service starts
serviceController.startCommand(0, 1);
LocalBroadcastManager.getInstance(context).registerReceiver(...);  // Too late!

// ✅ GOOD - Register BEFORE starting service
LocalBroadcastManager.getInstance(context).registerReceiver(...);
serviceController.startCommand(0, 1);
```

### Gotcha 3: Notification Not Posted
```java
// ❌ BAD - Check immediately
serviceController.startCommand(0, 1);
assertEquals(1, shadowNM.size());  // FAILS - notification not posted yet

// ✅ GOOD - Wait for UI thread
serviceController.startCommand(0, 1);
ShadowLooper.idleMainLooper();  // Process notification posting
assertEquals(1, shadowNM.size());  // PASSES
```

### Gotcha 4: Service Lifecycle
```java
// ❌ BAD - Missing create()
ServiceController<UpdateService> controller =
    Robolectric.buildService(UpdateService.class, intent);
controller.startCommand(0, 1);  // FAILS - onCreate not called

// ✅ GOOD - Full lifecycle
ServiceController<UpdateService> controller =
    Robolectric.buildService(UpdateService.class, intent);
controller.create();            // Call onCreate()
controller.startCommand(0, 1);  // Now works
```

---

## Resources

- **Full Plan:** `docs/testing/phase-3-implementation-plan.md` (21 tests with code)
- **Critical Analysis:** `docs/testing/test-review-critical-analysis.md` (test quality issues)
- **EP-001:** `docs/proposals/EP-001-updateservice-testing-modernization.md` (overall strategy)
- **Robolectric Docs:** http://robolectric.org/
- **MockWebServer:** https://github.com/square/okhttp/tree/master/mockwebserver

---

## Decision Points

### Before Starting:

**Q1: Do Real Params tests (16-19)?**
- YES → Full 20 tests, 5-7 days
- NO → Just 15 critical tests, 3-4 days

**Q2: Add MockWebServer now?**
- YES → Can test network errors properly
- NO → Defer to Phase 4, use test resources

**Q3: Refactor UpdateService?**
- YES → Enables all 20 tests
- NO → Blocks tests 8, 16-19, 21 (9 tests)

**Recommendation:**
- ✅ Refactor UpdateService (Day 0)
- ✅ Do tests 1-15 (CRITICAL + HIGH)
- ⚠️ Defer tests 16-19 to Phase 4 if time short
- ⚠️ Simplify tests 20-21 (no MockWebServer initially)

---

## Next Action

```bash
# 1. Review the full plan
cat docs/testing/phase-3-implementation-plan.md

# 2. Discuss with team
# - Is UpdateService refactoring approved?
# - 3-4 days (15 tests) or 5-7 days (20 tests)?
# - Add MockWebServer now or later?

# 3. Start Day 0: UpdateService refactoring
# - Extract getBeerListUrl() method
# - Extract createParams() method

# 4. Start Day 1: AsyncTask tests
# - Implement tests 1-5
# - Validate Robolectric approach
# - Update PR with progress
```
