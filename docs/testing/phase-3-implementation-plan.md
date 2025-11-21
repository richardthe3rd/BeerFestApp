# Phase 3 Implementation Plan: Critical Integration Tests

**Purpose:** Tests needed for safe refactoring of UpdateService/UpdateTask
**Status:** Ready to Implement
**Timeline:** Week 3 (5-7 days)
**Priority:** HIGH - Blocks refactoring work

---

## Executive Summary

**Current State:**
- ✅ 37 functional tests (Phase 1-2)
- ✅ Business logic covered
- ⚠️ **AsyncTask lifecycle NOT tested** - blocking refactoring
- ⚠️ **Broadcasts NOT verified** - blocking refactoring
- ⚠️ **Real Params logic bypassed** - risk of false confidence

**Gap Analysis:**
We test `doInBackground()` directly but never test:
- Whether `execute()` actually runs the task
- Whether `onProgressUpdate()` gets called
- Whether `onPostExecute()` gets called
- Whether broadcasts are sent
- Whether notifications are updated

**This means: We can't safely refactor AsyncTask/LocalBroadcastManager because we don't have tests proving they work.**

**Phase 3 Goal:** Add 20 integration tests to enable safe refactoring

---

## Test Categories (Priority Order)

### 1. AsyncTask Lifecycle Tests (CRITICAL) - 5 tests

**Why Critical:** AsyncTask is deprecated and must be replaced. Without lifecycle tests, we can't verify the replacement works correctly.

**What to Test:**
- Task execution (does `execute()` call `doInBackground()`?)
- Progress callbacks (does `onProgressUpdate()` get invoked?)
- Completion callbacks (does `onPostExecute()` get invoked?)
- Task cancellation
- Thread safety

**Implementation Approach:**

#### Test 1: Task Execute Calls doInBackground

```java
@Test
public void testExecuteCallsDoInBackground() throws Exception {
    // Arrange
    final AtomicBoolean doInBackgroundCalled = new AtomicBoolean(false);
    final Context context = RuntimeEnvironment.getApplication();
    final BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);

    final String validJson = TestDataFactory.createValidBeerJSON(3);
    final InputStream stream = new ByteArrayInputStream(validJson.getBytes());

    final TestParams params = new TestParams(
            true, true, true, stream, dbHelper
    );

    // Create task that tracks execution
    UpdateTask task = new UpdateTask() {
        @Override
        protected Result doInBackground(Params... params) {
            doInBackgroundCalled.set(true);
            return super.doInBackground(params);
        }
    };

    // Act - Use execute() not direct call
    task.execute(params);

    // Wait for background thread
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("execute() should call doInBackground()",
               doInBackgroundCalled.get());

    // Verify result
    assertTrue("Task should complete successfully",
               task.get().success());
}
```

#### Test 2: OnProgressUpdate Called During Execution

```java
@Test
public void testOnProgressUpdateCalled() throws Exception {
    // Arrange
    final AtomicInteger progressCallCount = new AtomicInteger(0);
    final AtomicInteger lastProgress = new AtomicInteger(0);
    final AtomicInteger lastTotal = new AtomicInteger(0);

    final Context context = RuntimeEnvironment.getApplication();
    final BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);

    // Create larger dataset to ensure multiple progress updates
    final String validJson = TestDataFactory.createValidBeerJSON(50);
    final InputStream stream = new ByteArrayInputStream(validJson.getBytes());

    final TestParams params = new TestParams(
            true, true, true, stream, dbHelper
    );

    // Create task that tracks progress callbacks
    UpdateTask task = new UpdateTask() {
        @Override
        protected void onProgressUpdate(Progress... values) {
            super.onProgressUpdate(values);
            progressCallCount.incrementAndGet();
            lastProgress.set(values[0].getProgress());
            lastTotal.set(values[0].getTotal());
        }
    };

    // Act
    task.execute(params);

    // Wait for completion
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("onProgressUpdate should be called at least once",
               progressCallCount.get() > 0);
    assertTrue("Progress should be > 0", lastProgress.get() > 0);
    assertEquals("Total should match beer count", 50, lastTotal.get());
    assertTrue("Final progress should be near total",
               lastProgress.get() >= 45); // Allow for async timing
}
```

#### Test 3: OnPostExecute Called After Completion

```java
@Test
public void testOnPostExecuteCalled() throws Exception {
    // Arrange
    final AtomicBoolean postExecuteCalled = new AtomicBoolean(false);
    final AtomicReference<UpdateTask.Result> capturedResult =
        new AtomicReference<>();

    final Context context = RuntimeEnvironment.getApplication();
    final BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);

    final String validJson = TestDataFactory.createValidBeerJSON(10);
    final InputStream stream = new ByteArrayInputStream(validJson.getBytes());

    final TestParams params = new TestParams(
            true, true, true, stream, dbHelper
    );

    // Create task that tracks post-execute
    UpdateTask task = new UpdateTask() {
        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            postExecuteCalled.set(true);
            capturedResult.set(result);
        }
    };

    // Act
    task.execute(params);

    // Wait for completion
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("onPostExecute should be called", postExecuteCalled.get());
    assertNotNull("Result should be captured", capturedResult.get());
    assertTrue("Result should indicate success",
               capturedResult.get().success());

    UpdateTask.UpdateResult updateResult =
        (UpdateTask.UpdateResult) capturedResult.get();
    assertEquals("Result should have correct count",
                 10, updateResult.getCount());
}
```

#### Test 4: Task Cancellation Works

```java
@Test
public void testTaskCancellation() throws Exception {
    // Arrange
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    final Context context = RuntimeEnvironment.getApplication();
    final BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);

    // Large dataset to allow time for cancellation
    final String validJson = TestDataFactory.createValidBeerJSON(1000);
    final InputStream stream = new ByteArrayInputStream(validJson.getBytes());

    final TestParams params = new TestParams(
            true, true, true, stream, dbHelper
    );

    UpdateTask task = new UpdateTask() {
        @Override
        protected void onCancelled(Result result) {
            super.onCancelled(result);
            cancelled.set(true);
        }
    };

    // Act - Start and immediately cancel
    task.execute(params);
    task.cancel(true);

    // Wait for cancellation
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("Task should be cancelled", task.isCancelled());
    assertTrue("onCancelled should be called", cancelled.get());
}
```

#### Test 5: Multiple Tasks Can Run Sequentially

```java
@Test
public void testSequentialTaskExecution() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);

    // First task - insert 5 beers
    final String json1 = TestDataFactory.createValidBeerJSON(5);
    final InputStream stream1 = new ByteArrayInputStream(json1.getBytes());
    final TestParams params1 = new TestParams(
            true, true, true, stream1, dbHelper
    );

    UpdateTask task1 = new UpdateTask();

    // Act - Execute first task
    task1.execute(params1);
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    assertEquals("First task should insert 5 beers",
                 5, dbHelper.getBeers().getNumberOfBeers());

    // Second task - incremental update with 3 more
    final String json2 = TestDataFactory.createValidBeerJSON(3);
    final InputStream stream2 = new ByteArrayInputStream(json2.getBytes());
    final TestParams params2 = new TestParams(
            false, true, true, stream2, dbHelper
    );

    UpdateTask task2 = new UpdateTask();

    // Act - Execute second task
    task2.execute(params2);
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Both tasks completed
    assertTrue("First task should complete", task1.get().success());
    assertTrue("Second task should complete", task2.get().success());
    assertEquals("Should have 3 beers after incremental",
                 3, dbHelper.getBeers().getNumberOfBeers());
}
```

**Robolectric Utilities Needed:**
```java
// Force background threads to complete
Robolectric.flushBackgroundThreadScheduler();

// Process main looper (for callbacks)
ShadowLooper.idleMainLooper();

// Get task result
task.get(); // Blocks until complete
```

---

### 2. Broadcast Tests (CRITICAL) - 5 tests

**Why Critical:** LocalBroadcastManager is deprecated. Must verify broadcasts work before replacing.

**What to Test:**
- Progress broadcasts sent during update
- Result broadcasts sent on completion
- Result broadcasts sent on failure
- Broadcast data correctness
- Multiple progress broadcasts

**Implementation Approach:**

#### Test 6: Progress Broadcast Sent During Update

```java
@Test
public void testProgressBroadcastSent() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final AtomicBoolean broadcastReceived = new AtomicBoolean(false);
    final AtomicInteger receivedProgress = new AtomicInteger(0);
    final AtomicInteger receivedTotal = new AtomicInteger(0);

    // Register broadcast receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastReceived.set(true);
            UpdateTask.Progress progress = (UpdateTask.Progress)
                intent.getSerializableExtra(UpdateService.PROGRESS_EXTRA);
            receivedProgress.set(progress.getProgress());
            receivedTotal.set(progress.getTotal());
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_PROGRESS)
    );

    // Act - Start update service
    final Intent intent = new Intent(context, UpdateService.class);
    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for async execution
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("Progress broadcast should be received",
               broadcastReceived.get());
    assertTrue("Progress should be > 0", receivedProgress.get() > 0);
    assertTrue("Total should be > 0", receivedTotal.get() > 0);

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    controller.destroy();
}
```

#### Test 7: Result Broadcast Sent On Success

```java
@Test
public void testResultBroadcastSentOnSuccess() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final AtomicBoolean broadcastReceived = new AtomicBoolean(false);
    final AtomicBoolean resultSuccess = new AtomicBoolean(false);
    final AtomicInteger resultCount = new AtomicInteger(0);

    // Register result receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastReceived.set(true);
            UpdateTask.Result result = (UpdateTask.Result)
                intent.getSerializableExtra(UpdateService.RESULT_EXTRA);
            resultSuccess.set(result.success());
            if (result instanceof UpdateTask.UpdateResult) {
                resultCount.set(((UpdateTask.UpdateResult) result).getCount());
            }
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_RESULT)
    );

    // Act - Start update service with clean update
    final Intent intent = new Intent(context, UpdateService.class);
    intent.putExtra(UpdateService.CLEAN_UPDATE, true);

    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for completion
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("Result broadcast should be received",
               broadcastReceived.get());
    assertTrue("Result should indicate success", resultSuccess.get());
    assertTrue("Result should have beer count > 0", resultCount.get() > 0);

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    controller.destroy();
}
```

#### Test 8: Result Broadcast Sent On Failure

```java
@Test
public void testResultBroadcastSentOnFailure() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final AtomicBoolean broadcastReceived = new AtomicBoolean(false);
    final AtomicBoolean resultFailed = new AtomicBoolean(false);
    final AtomicReference<Throwable> capturedError =
        new AtomicReference<>();

    // Register result receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastReceived.set(true);
            UpdateTask.Result result = (UpdateTask.Result)
                intent.getSerializableExtra(UpdateService.RESULT_EXTRA);
            resultFailed.set(!result.success());
            capturedError.set(result.getThrowable());
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_RESULT)
    );

    // Act - Start service with invalid beer list URL (will fail)
    // Need to override URL to force failure
    // This requires refactoring UpdateService to be testable
    // For now, document as TODO

    // TODO: This test requires UpdateService refactoring to inject URL
    // Alternative: Use MockWebServer to return error response

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
}
```

#### Test 9: Multiple Progress Broadcasts Sent

```java
@Test
public void testMultipleProgressBroadcastsSent() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final AtomicInteger broadcastCount = new AtomicInteger(0);
    final List<Integer> progressValues = new ArrayList<>();

    // Register receiver that counts broadcasts
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastCount.incrementAndGet();
            UpdateTask.Progress progress = (UpdateTask.Progress)
                intent.getSerializableExtra(UpdateService.PROGRESS_EXTRA);
            progressValues.add(progress.getProgress());
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_PROGRESS)
    );

    // Act - Start update with large dataset (ensures multiple progress updates)
    final Intent intent = new Intent(context, UpdateService.class);
    intent.putExtra(UpdateService.CLEAN_UPDATE, true);

    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for completion
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert
    assertTrue("Should receive multiple progress broadcasts",
               broadcastCount.get() > 1);

    // Verify progress is increasing
    for (int i = 1; i < progressValues.size(); i++) {
        assertTrue("Progress should increase",
                   progressValues.get(i) > progressValues.get(i - 1));
    }

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    controller.destroy();
}
```

#### Test 10: Broadcast Data Correctness

```java
@Test
public void testBroadcastDataCorrectness() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final AtomicReference<String> receivedDigest = new AtomicReference<>();
    final AtomicInteger receivedCount = new AtomicInteger(0);

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateTask.Result result = (UpdateTask.Result)
                intent.getSerializableExtra(UpdateService.RESULT_EXTRA);

            if (result instanceof UpdateTask.UpdateResult) {
                UpdateTask.UpdateResult updateResult =
                    (UpdateTask.UpdateResult) result;
                receivedDigest.set(updateResult.getDigest());
                receivedCount.set(updateResult.getCount());
            }
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_RESULT)
    );

    // Act
    final Intent intent = new Intent(context, UpdateService.class);
    intent.putExtra(UpdateService.CLEAN_UPDATE, true);

    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Broadcast contains valid data
    assertNotNull("Digest should be included", receivedDigest.get());
    assertEquals("Digest should be 32 hex chars",
                 32, receivedDigest.get().length());
    assertTrue("Count should match database",
               receivedCount.get() > 0);

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    controller.destroy();
}
```

---

### 3. Notification Tests - 5 tests

**Why Important:** Notifications are user-facing. Must work correctly.

**What to Test:**
- Notification created on start
- Notification updated with progress
- Notification shows completion
- Notification cancelled on no-update
- Notification has correct content

**Implementation Approach:**

#### Test 11: Notification Created On Service Start

```java
@Test
public void testNotificationCreatedOnStart() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    final ShadowNotificationManager shadowManager = shadowOf(notificationManager);

    // Act
    final Intent intent = new Intent(context, UpdateService.class);
    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait a moment for notification to post
    ShadowLooper.idleMainLooper();

    // Assert
    assertEquals("Should have 1 notification", 1, shadowManager.size());

    Notification notification = shadowManager.getNotification(0);
    assertNotNull("Notification should exist", notification);

    // Verify notification properties
    assertEquals("Should have correct icon",
                 R.drawable.ic_caskman, notification.icon);

    // Cleanup
    controller.destroy();
}
```

#### Test 12: Notification Updated With Progress

```java
@Test
public void testNotificationUpdatedWithProgress() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    final ShadowNotificationManager shadowManager = shadowOf(notificationManager);

    // Act
    final Intent intent = new Intent(context, UpdateService.class);
    intent.putExtra(UpdateService.CLEAN_UPDATE, true);

    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for progress
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Notification should be updated (still size 1, but content changed)
    assertEquals("Should still have 1 notification", 1, shadowManager.size());

    Notification notification = shadowManager.getNotification(0);
    assertNotNull("Notification should exist", notification);

    // Verify progress is shown
    // Note: Robolectric may not fully support progress bar inspection
    // This test verifies notification was updated (by checking it still exists)

    // Cleanup
    controller.destroy();
}
```

#### Test 13: Notification Shows Completion Message

```java
@Test
public void testNotificationShowsCompletion() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    final ShadowNotificationManager shadowManager = shadowOf(notificationManager);

    // Act - Complete update
    final Intent intent = new Intent(context, UpdateService.class);
    intent.putExtra(UpdateService.CLEAN_UPDATE, true);

    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for completion
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Notification should show completion
    assertTrue("Should have notification after completion",
               shadowManager.size() > 0);

    // Note: Checking notification text requires reflection or Robolectric updates
    // For now, verify notification exists

    // Cleanup
    controller.destroy();
}
```

#### Test 14: Notification Cancelled On No Update

```java
@Test
public void testNotificationCancelledOnNoUpdate() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    final ShadowNotificationManager shadowManager = shadowOf(notificationManager);

    // Set up so update is not due
    AppPreferences prefs = new AppPreferences(context);
    prefs.setNextUpdateTime(new Date(System.currentTimeMillis() + 86400000)); // Tomorrow

    // Act
    final Intent intent = new Intent(context, UpdateService.class);
    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for processing
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Notification should be cancelled (no notification posted)
    assertEquals("Should have no notifications when update not needed",
                 0, shadowManager.size());

    // Cleanup
    controller.destroy();
}
```

#### Test 15: Notification Has Correct Title And Text

```java
@Test
public void testNotificationContent() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    final ShadowNotificationManager shadowManager = shadowOf(notificationManager);

    // Act
    final Intent intent = new Intent(context, UpdateService.class);
    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    ShadowLooper.idleMainLooper();

    // Assert
    assertEquals("Should have 1 notification", 1, shadowManager.size());

    Notification notification = shadowManager.getNotification(0);
    assertNotNull("Notification should exist", notification);

    // Get notification content
    // Note: This requires using Robolectric's shadow methods or reflection
    // String title = getNotificationTitle(notification);
    // String text = getNotificationText(notification);

    // For now, verify notification exists with correct icon
    assertEquals("Should have correct icon",
                 R.drawable.ic_caskman, notification.icon);

    // Cleanup
    controller.destroy();
}
```

---

### 4. Real Params Logic Tests - 4 tests

**Why Important:** TestParams bypasses real decision logic. Must test actual implementation.

**What to Test:**
- Real `updateDue()` checks AppPreferences
- Real `updateDue()` checks beer count = 0
- Real `needsUpdate()` compares MD5
- Real Params work end-to-end

**Challenge:** UpdateService creates Params as anonymous inner class. Need refactoring to make testable.

**Solution Options:**

**Option A: Reflection (Hacky but works now)**
```java
@Test
public void testRealUpdateDueLogic() throws Exception {
    // Use reflection to access inner Params class
    // Not ideal, but works without refactoring
}
```

**Option B: Refactor UpdateService (Proper but requires changes)**
```java
// UpdateService.java - Extract Params creation to protected method
protected Params createParams(final boolean cleanUpdate) {
    return new Params() {
        // ... existing logic
    };
}

// Test
@Test
public void testRealUpdateDueLogic() {
    UpdateService service = createService();
    Params params = service.createParams(false);

    // Test real logic
    assertTrue(params.updateDue());
}
```

**Recommendation:** Use Option B (refactor) - cleaner and enables better testing.

#### Test 16: Real UpdateDue Checks AppPreferences Time

```java
@Test
public void testRealUpdateDueChecksTime() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    AppPreferences prefs = new AppPreferences(context);

    // Set next update to past
    prefs.setNextUpdateTime(new Date(0));

    // Create service
    final Intent intent = new Intent(context, UpdateService.class);
    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();

    UpdateService service = controller.get();

    // TODO: Requires UpdateService refactoring to expose createParams()
    // Params params = service.createParams(false);

    // Assert
    // assertTrue("Should need update when time expired", params.updateDue());

    controller.destroy();
}
```

#### Test 17: Real UpdateDue Checks Beer Count Zero

```java
@Test
public void testRealUpdateDueChecksBeerCount() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    AppPreferences prefs = new AppPreferences(context);
    BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);

    // Set next update to future (should not be due)
    prefs.setNextUpdateTime(new Date(System.currentTimeMillis() + 86400000));

    // Ensure database is empty
    assertEquals("Database should be empty", 0,
                 dbHelper.getBeers().getNumberOfBeers());

    // TODO: Test that updateDue() returns true even though time is future
    // because beer count is 0

    dbHelper.close();
}
```

#### Test 18: Real NeedsUpdate Compares MD5

```java
@Test
public void testRealNeedsUpdateCompareMD5() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();
    AppPreferences prefs = new AppPreferences(context);

    // Set known MD5
    prefs.setLastUpdateMD5("abc123def456");

    // TODO: Create real Params and test needsUpdate()
    // Different MD5 should return true
    // Same MD5 should return false
}
```

#### Test 19: Real Params End-to-End

```java
@Test
public void testRealParamsEndToEnd() throws Exception {
    // This test verifies that UpdateService creates working Params
    // that integrate correctly with AppPreferences and BeerDatabaseHelper

    // TODO: Requires UpdateService refactoring
}
```

**Note:** Tests 16-19 are blocked on UpdateService refactoring. Can be done in Phase 4 if time is short.

---

### 5. End-to-End Integration Tests - 2 tests

**Why Important:** Verify complete flow works as user experiences it.

**What to Test:**
- Complete successful update flow
- Complete error handling flow

#### Test 20: Complete Update Flow Success

```java
@Test
public void testCompleteUpdateFlowSuccess() throws Exception {
    // Arrange - Full integration test
    final Context context = RuntimeEnvironment.getApplication();
    final BeerDatabaseHelper dbHelper = new BeerDatabaseHelper(context);
    final AppPreferences prefs = new AppPreferences(context);

    // Set up initial state
    prefs.setNextUpdateTime(new Date(0)); // Update is due
    assertEquals("Database should start empty", 0,
                 dbHelper.getBeers().getNumberOfBeers());

    // Track completion
    final AtomicBoolean updateCompleted = new AtomicBoolean(false);
    final AtomicInteger finalCount = new AtomicInteger(0);

    // Register result receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateTask.Result result = (UpdateTask.Result)
                intent.getSerializableExtra(UpdateService.RESULT_EXTRA);
            if (result.success()) {
                updateCompleted.set(true);
                if (result instanceof UpdateTask.UpdateResult) {
                    finalCount.set(((UpdateTask.UpdateResult) result).getCount());
                }
            }
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_RESULT)
    );

    // Act - Start update service (simulates user clicking refresh)
    final Intent intent = new Intent(context, UpdateService.class);
    intent.putExtra(UpdateService.CLEAN_UPDATE, true);

    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    // Wait for complete flow
    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Verify every part of the flow
    assertTrue("Update should complete successfully", updateCompleted.get());
    assertTrue("Should have beers in database",
               dbHelper.getBeers().getNumberOfBeers() > 0);
    assertEquals("Broadcast count should match database",
                 finalCount.get(), dbHelper.getBeers().getNumberOfBeers());

    // Verify preferences updated
    assertNotNull("Last update MD5 should be set",
                  prefs.getLastUpdateMD5());
    assertTrue("Last update MD5 should be 32 chars",
               prefs.getLastUpdateMD5().length() == 32);

    // Verify notification
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    ShadowNotificationManager shadowManager = shadowOf(notificationManager);
    assertTrue("Should have notification", shadowManager.size() > 0);

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    controller.destroy();
    dbHelper.close();
}
```

#### Test 21: Complete Update Flow With Network Error

```java
@Test
public void testCompleteUpdateFlowNetworkError() throws Exception {
    // Arrange
    final Context context = RuntimeEnvironment.getApplication();

    // Track failure
    final AtomicBoolean errorReceived = new AtomicBoolean(false);
    final AtomicReference<Throwable> capturedError = new AtomicReference<>();

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateTask.Result result = (UpdateTask.Result)
                intent.getSerializableExtra(UpdateService.RESULT_EXTRA);
            if (!result.success()) {
                errorReceived.set(true);
                capturedError.set(result.getThrowable());
            }
        }
    };

    LocalBroadcastManager.getInstance(context).registerReceiver(
        receiver,
        new IntentFilter(UpdateService.UPDATE_SERVICE_RESULT)
    );

    // Act - Start service (will fail due to invalid URL in resources)
    // TODO: Requires MockWebServer to properly test network errors

    final Intent intent = new Intent(context, UpdateService.class);
    final ServiceController<UpdateService> controller =
        Robolectric.buildService(UpdateService.class, intent);
    controller.create();
    controller.startCommand(0, 1);

    Robolectric.flushBackgroundThreadScheduler();
    ShadowLooper.idleMainLooper();

    // Assert - Error should be handled gracefully
    // (Actual behavior depends on beer_list_url in test resources)

    // Cleanup
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    controller.destroy();
}
```

---

## Implementation Timeline

### Day 1-2: AsyncTask Lifecycle Tests (5 tests)
- **Priority:** CRITICAL
- **Tests:** 1-5
- **Estimated Time:** 8-12 hours
- **Blockers:** None
- **Output:** UpdateTask lifecycle fully tested

### Day 3-4: Broadcast Tests (5 tests)
- **Priority:** CRITICAL
- **Tests:** 6-10
- **Estimated Time:** 8-12 hours
- **Blockers:** Need UpdateService to be testable (inject dependencies)
- **Output:** Communication layer tested

### Day 5: Notification Tests (5 tests)
- **Priority:** HIGH
- **Tests:** 11-15
- **Estimated Time:** 6-8 hours
- **Blockers:** Limited Robolectric notification support
- **Output:** User-facing notifications tested

### Day 6: Real Params Tests (4 tests)
- **Priority:** MEDIUM (can defer to Phase 4)
- **Tests:** 16-19
- **Estimated Time:** 6-8 hours
- **Blockers:** **Requires UpdateService refactoring**
- **Output:** Real business logic tested (not TestParams)

### Day 7: End-to-End Integration (2 tests)
- **Priority:** HIGH
- **Tests:** 20-21
- **Estimated Time:** 4-6 hours
- **Blockers:** Requires MockWebServer setup
- **Output:** Complete flow tested

**Total Estimated Time:** 32-46 hours (5-7 days)

---

## Dependencies and Blockers

### Required Refactoring

#### 1. UpdateService Dependency Injection (HIGH PRIORITY)

**Current Problem:**
```java
// UpdateService.java - Line 124
URL url = new URL(getString(R.string.beer_list_url));
return url.openStream();
```
Cannot test with different URLs or MockWebServer.

**Solution:**
```java
// Add protected method for URL override
protected String getBeerListUrl() {
    return getString(R.string.beer_list_url);
}

// UpdateService.Params.openStream()
URL url = new URL(getBeerListUrl());
return url.openStream();

// In tests, override:
UpdateService testService = new UpdateService() {
    @Override
    protected String getBeerListUrl() {
        return mockServer.url("/beers.json").toString();
    }
};
```

#### 2. UpdateService Params Extraction (MEDIUM PRIORITY)

**Current Problem:**
Params is anonymous inner class, can't test directly.

**Solution:**
```java
// Extract to protected method
protected Params createParams(final boolean cleanUpdate) {
    return new Params() {
        // ... existing logic
    };
}

// In tests:
Params realParams = service.createParams(false);
assertTrue(realParams.updateDue());
```

### Required Infrastructure

#### 1. MockWebServer Setup

```gradle
// app/build.gradle
testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
```

```java
// Test setup
@Before
public void setUp() throws Exception {
    mockServer = new MockWebServer();
    mockServer.start();

    // Enqueue response
    mockServer.enqueue(new MockResponse()
        .setBody(TestDataFactory.createValidBeerJSON(10))
        .setHeader("Content-Type", "application/json"));
}

@After
public void tearDown() throws Exception {
    mockServer.shutdown();
}
```

#### 2. Robolectric Utilities Helper

```java
// TestUtil.java
public class TestUtil {
    public static void waitForAsyncTask() {
        Robolectric.flushBackgroundThreadScheduler();
        ShadowLooper.idleMainLooper();
    }

    public static void waitForSeconds(int seconds) {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    }
}
```

---

## Success Criteria

### Phase 3 Complete When:

✅ **All 20 tests implemented and passing**
- [ ] AsyncTask lifecycle (5 tests)
- [ ] Broadcasts (5 tests)
- [ ] Notifications (5 tests)
- [ ] Real Params (4 tests) - Can defer to Phase 4
- [ ] End-to-end (2 tests)

✅ **Coverage targets met**
- [ ] UpdateTask: 85%+ line coverage
- [ ] UpdateService: 80%+ line coverage
- [ ] Integration: End-to-end flow covered

✅ **Infrastructure ready**
- [ ] MockWebServer configured
- [ ] Robolectric utilities working
- [ ] CI runs all tests successfully

✅ **Documentation complete**
- [ ] Test patterns documented
- [ ] Robolectric gotchas documented
- [ ] Refactoring blockers identified

✅ **Ready for refactoring**
- [ ] Can confidently replace AsyncTask
- [ ] Can confidently replace LocalBroadcastManager
- [ ] Tests will catch regressions

---

## Risk Mitigation

### Risk 1: Robolectric Limitations

**Risk:** Robolectric may not fully support AsyncTask/notifications
**Mitigation:**
- Start with AsyncTask tests first (Day 1) to validate approach
- If blocked, pivot to instrumented tests for async behavior
- Document limitations and workarounds

### Risk 2: UpdateService Not Testable

**Risk:** Current UpdateService design prevents proper testing
**Mitigation:**
- Refactor UpdateService on Day 3 (before broadcast tests)
- Extract dependencies (URL, Params creation)
- Keep refactoring minimal and focused

### Risk 3: Time Overrun

**Risk:** 20 tests may take longer than 7 days
**Mitigation:**
- Tests 1-10 are CRITICAL (AsyncTask + Broadcasts)
- Tests 11-15 are HIGH (Notifications)
- Tests 16-19 can be deferred to Phase 4 (Real Params)
- Tests 20-21 can be simplified if needed

### Risk 4: Flaky Tests

**Risk:** Async tests may be timing-sensitive
**Mitigation:**
- Use Robolectric's deterministic scheduling
- Avoid `Thread.sleep()` - use `ShadowLooper` instead
- Add retries for truly async operations
- Document timing assumptions

---

## Next Steps

1. **Review this plan** - Get team buy-in on approach
2. **Refactor UpdateService** - Enable testability (Day 0)
3. **Start Day 1** - AsyncTask lifecycle tests (validate approach)
4. **Daily standup** - Track progress, adjust timeline
5. **Update PR** - Keep PR #49 updated with progress

---

**Questions Before Starting?**
- Is UpdateService refactoring approved?
- Are we okay deferring Real Params tests (16-19) to Phase 4?
- Should we add MockWebServer now or defer?
- Any concerns about Robolectric approach?
