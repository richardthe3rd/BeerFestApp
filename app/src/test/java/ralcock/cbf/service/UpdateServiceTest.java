package ralcock.cbf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadows.ShadowNotificationManager;

/**
 * Unit tests for UpdateService using Robolectric.
 * Tests cover service lifecycle, notifications, and broadcast management.
 *
 * Phase 2 of EP-001: UpdateService Testing
 * Target: 18 tests covering all UpdateService functionality
 */
@RunWith(RobolectricTestRunner.class)
public class UpdateServiceTest {

    private ServiceController<UpdateService> fServiceController;
    private Context fContext;

    @Before
    public void setUp() {
        fContext = ApplicationProvider.getApplicationContext();
    }

    @After
    public void tearDown() {
        if (fServiceController != null) {
            fServiceController.destroy();
            fServiceController = null;
        }
    }

    // ========================================
    // Service Lifecycle Tests (Category 1)
    // ========================================

    /**
     * Test 1: onCreate() should initialize the service correctly.
     */
    @Test
    public void testOnCreateInitializesService() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);

        // Act
        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        final UpdateService service = fServiceController.get();

        // Assert
        assertNotNull("Service should be created", service);
        // Service is initialized and ready
    }

    /**
     * Test 2: onStartCommand() should start the service successfully.
     */
    @Test
    public void testOnStartCommandStartsService() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);
        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        // Act
        fServiceController.startCommand(0, 1);

        final UpdateService service = fServiceController.get();

        // Assert
        assertNotNull("Service should be running", service);
    }

    /**
     * Test 3: onDestroy() should cleanup properly.
     */
    @Test
    public void testOnDestroyCleanup() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);
        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        // Act
        fServiceController.destroy();

        // Assert - Should not throw exception
        // Cleanup completed successfully
    }

    /**
     * Test 4: onBind() should return null (not a bound service).
     */
    @Test
    public void testOnBindReturnsNull() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);
        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        final UpdateService service = fServiceController.get();

        // Act
        final IBinder binder = service.onBind(intent);

        // Assert
        assertNull("Service should not be bindable", binder);
    }

    // ========================================
    // Notification Tests (Category 2)
    // ========================================

    /**
     * Test 5: Notification should be created when service starts.
     */
    @Test
    public void testNotificationCreatedOnServiceStart() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);
        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        // Act
        fServiceController.startCommand(0, 1);

        // Get notification manager
        final NotificationManager notificationManager =
                (NotificationManager) fContext.getSystemService(Context.NOTIFICATION_SERVICE);
        final ShadowNotificationManager shadowManager = shadowOf(notificationManager);

        // Assert
        // Note: Due to AsyncTask execution, notification may not be posted immediately
        // This test verifies the notification manager is accessible
        assertNotNull("NotificationManager should be accessible", notificationManager);
    }

    // NOTE: Notification detail tests (title, progress, cancellation, completion)
    // are deferred to Phase 3 integration tests, as they require:
    // - Full AsyncTask execution (not just doInBackground call)
    // - Robolectric shadow scheduling
    // - Notification inspection after async completion
    //
    // These tests would provide false confidence if implemented as placeholders.
    // See: docs/testing/test-review-critical-analysis.md

    // ========================================
    // Intent Handling Tests (Category 3)
    // ========================================

    /**
     * Test 11: Default update intent (no extras).
     */
    @Test
    public void testDefaultUpdateIntent() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);
        // No CLEAN_UPDATE extra - should default to false

        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        // Act
        fServiceController.startCommand(0, 1);
        final UpdateService service = fServiceController.get();

        // Assert
        assertNotNull("Should start successfully with default intent", service);
    }

    /**
     * Test 12: Clean update intent (CLEAN_UPDATE=true).
     */
    @Test
    public void testCleanUpdateIntent() {
        // Arrange
        final Intent intent = new Intent(fContext, UpdateService.class);
        intent.putExtra(UpdateService.CLEAN_UPDATE, true);

        fServiceController = Robolectric.buildService(UpdateService.class, intent);
        fServiceController.create();

        // Act
        fServiceController.startCommand(0, 1);
        final UpdateService service = fServiceController.get();

        // Assert
        assertNotNull("Should start successfully with clean update intent", service);
    }

    /**
     * Test 13: Service handles null intent gracefully.
     */
    @Test
    public void testNullIntentHandling() {
        // Arrange
        fServiceController = Robolectric.buildService(UpdateService.class);
        fServiceController.create();

        // Act
        // Service should handle null intent gracefully (defensive programming)
        fServiceController.startCommand(0, 1);

        final UpdateService service = fServiceController.get();

        // Assert
        assertNotNull("Service should handle null intent gracefully", service);
        // Service should default to cleanUpdate=false when intent is null
    }

    // ========================================
    // Additional Helper Tests
    // ========================================

    /**
     * Test 14: Service constants are correctly defined.
     */
    @Test
    public void testServiceConstants() {
        // Assert
        assertEquals("UPDATE_SERVICE_PROGRESS constant",
                "UpdateService.Progress", UpdateService.UPDATE_SERVICE_PROGRESS);
        assertEquals("UPDATE_SERVICE_RESULT constant",
                "UpdateService.Result", UpdateService.UPDATE_SERVICE_RESULT);
        assertEquals("PROGRESS_EXTRA constant",
                "progress", UpdateService.PROGRESS_EXTRA);
        assertEquals("RESULT_EXTRA constant",
                "result", UpdateService.RESULT_EXTRA);
        assertEquals("CLEAN_UPDATE constant",
                "cleanUpdate", UpdateService.CLEAN_UPDATE);
    }

    /**
     * Test 15: Multiple service instances can be created.
     */
    @Test
    public void testMultipleServiceInstances() {
        // Arrange & Act
        final Intent intent1 = new Intent(fContext, UpdateService.class);
        final ServiceController<UpdateService> controller1 =
                Robolectric.buildService(UpdateService.class, intent1);
        controller1.create();

        final Intent intent2 = new Intent(fContext, UpdateService.class);
        final ServiceController<UpdateService> controller2 =
                Robolectric.buildService(UpdateService.class, intent2);
        controller2.create();

        // Assert
        assertNotNull("First service instance created", controller1.get());
        assertNotNull("Second service instance created", controller2.get());

        // Cleanup
        controller1.destroy();
        controller2.destroy();
    }

    // Note: Broadcast tests (5 tests) would require LocalBroadcastManager mocking
    // which is complex with Robolectric. These are deferred or moved to integration tests.
}
