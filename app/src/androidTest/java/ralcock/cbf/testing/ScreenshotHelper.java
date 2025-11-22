package ralcock.cbf.testing;

import android.os.Environment;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for manually capturing screenshots during instrumented tests.
 *
 * Use this to capture screenshots at specific points in your tests,
 * not just on failure. Useful for documenting UI states or debugging.
 *
 * Usage:
 * <pre>
 * // In your test method:
 * ScreenshotHelper.capture("login_screen_loaded");
 * ScreenshotHelper.capture("after_clicking_submit");
 * </pre>
 */
public final class ScreenshotHelper {

    private static final String TAG = "ScreenshotHelper";
    private static final String SCREENSHOT_DIR = "screenshots";

    private ScreenshotHelper() {
        // Utility class
    }

    /**
     * Captures a screenshot with the given name.
     *
     * @param name A descriptive name for the screenshot (e.g., "beer_details_view")
     * @return The File where the screenshot was saved, or null if capture failed
     */
    public static File capture(final String name) {
        try {
            final UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());

            final File screenshotDir = getScreenshotDirectory();
            final String filename = generateFilename(name);
            final File screenshotFile = new File(screenshotDir, filename);

            final boolean success = device.takeScreenshot(screenshotFile);

            if (success) {
                Log.i(TAG, "Screenshot saved: " + screenshotFile.getAbsolutePath());
                return screenshotFile;
            } else {
                Log.w(TAG, "Failed to capture screenshot: " + name);
                return null;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error capturing screenshot: " + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Captures a screenshot with automatic naming based on calling test method.
     * Uses stack trace to determine the calling test class and method.
     *
     * @param description A short description to append to the auto-generated name
     * @return The File where the screenshot was saved, or null if capture failed
     */
    public static File captureWithContext(final String description) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Find the first element that looks like a test method (skip this class and Thread)
        String testContext = "unknown";
        for (StackTraceElement element : stackTrace) {
            final String className = element.getClassName();
            if (className.contains("Test") && !className.equals(ScreenshotHelper.class.getName())) {
                final String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                testContext = simpleClassName + "_" + element.getMethodName();
                break;
            }
        }

        return capture(testContext + "_" + description);
    }

    /**
     * Gets the screenshot directory, creating it if necessary.
     */
    private static File getScreenshotDirectory() {
        final File externalDir = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        final File screenshotDir = new File(externalDir, SCREENSHOT_DIR);

        if (!screenshotDir.exists()) {
            final boolean created = screenshotDir.mkdirs();
            if (!created) {
                Log.w(TAG, "Could not create screenshot directory: " + screenshotDir.getAbsolutePath());
            }
        }

        return screenshotDir;
    }

    /**
     * Generates a unique filename for the screenshot.
     */
    private static String generateFilename(final String name) {
        final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        // Sanitize name to be filename-safe
        final String safeName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        return String.format(Locale.US, "%s_%s.png", safeName, timestamp);
    }

    /**
     * Returns the directory where screenshots are stored.
     * Useful for logging or verification purposes.
     *
     * @return The screenshot directory
     */
    public static File getScreenshotDir() {
        return getScreenshotDirectory();
    }
}
