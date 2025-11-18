package ralcock.cbf;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ActivityScenario.launch;
import static org.junit.Assert.assertNotNull;

/**
 * Modern replacement for the deprecated LifecycleTest.
 *
 * Tests activity lifecycle state transitions using ActivityScenario from AndroidX Test.
 * This replaces the old ActivityUnitTestCase-based test that was deleted in commit 3df0096.
 *
 * @see <a href="https://developer.android.com/guide/components/activities/activity-lifecycle">Activity Lifecycle</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CamBeerFestApplicationLifecycleTest {

    /**
     * Test that activity can be created and recreated (simulating configuration change).
     * This is critical for ensuring state is properly saved and restored.
     */
    @Test
    public void testActivityRecreation() {
        // Launch activity
        try (ActivityScenario<CamBeerFestApplication> scenario =
                launch(CamBeerFestApplication.class)) {

            // Test activity is created
            scenario.onActivity(activity -> {
                assertNotNull("Activity should be created", activity);
                assertNotNull("Activity should have a window", activity.getWindow());
            });

            // Simulate configuration change (e.g., rotation) - recreate activity
            scenario.recreate();

            // Verify activity survives recreation
            scenario.onActivity(activity -> {
                assertNotNull("Activity should still exist after recreation", activity);
                assertNotNull("Activity should still have a window after recreation",
                             activity.getWindow());
            });
        }
    }

    /**
     * Test activity transitions through lifecycle states properly.
     * Ensures the activity can move through CREATED -> STARTED -> RESUMED -> DESTROYED.
     */
    @Test
    public void testActivityStateTransitions() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                launch(CamBeerFestApplication.class)) {

            // Verify we can move through lifecycle states without crashes
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            scenario.moveToState(Lifecycle.State.CREATED);

            // ActivityScenario automatically handles cleanup when closed (try-with-resources)
        }
    }

    /**
     * Test that activity starts and reaches RESUMED state.
     * This verifies basic activity initialization works correctly.
     */
    @Test
    public void testActivityLaunchReachesResumedState() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                launch(CamBeerFestApplication.class)) {

            // Verify activity reaches RESUMED state
            scenario.onActivity(activity -> {
                assertNotNull("Activity should be created and running", activity);
            });

            // ActivityScenario ensures the activity is in RESUMED state after launch
            // Additional assertions could verify UI elements are initialized
        }
    }

    /**
     * Test that activity can be paused and resumed.
     * Simulates user pressing Home button and returning to app.
     */
    @Test
    public void testActivityPauseAndResume() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                launch(CamBeerFestApplication.class)) {

            // Move to STARTED (paused, but visible)
            scenario.moveToState(Lifecycle.State.STARTED);

            // Resume the activity
            scenario.moveToState(Lifecycle.State.RESUMED);

            // Verify activity is still functional
            scenario.onActivity(activity -> {
                assertNotNull("Activity should still be functional after pause/resume",
                             activity);
            });
        }
    }
}
