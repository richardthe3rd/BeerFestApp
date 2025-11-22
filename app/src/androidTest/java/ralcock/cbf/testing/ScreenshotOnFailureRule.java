package ralcock.cbf.testing;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * JUnit TestRule that automatically captures a screenshot when a test fails.
 *
 * Screenshots are saved to the app's external files directory under "screenshots/".
 * The filename includes the test class name, method name, and timestamp.
 *
 * Usage:
 * <pre>
 * {@literal @}Rule
 * public ScreenshotOnFailureRule screenshotRule = new ScreenshotOnFailureRule();
 * </pre>
 */
public class ScreenshotOnFailureRule extends TestWatcher {

    private static final String TAG = "ScreenshotOnFailure";
    private static final String SCREENSHOT_DIR = "screenshots";

    @Override
    protected void failed(final Throwable e, final Description description) {
        captureScreenshot(description.getClassName(), description.getMethodName(), "FAILED");
    }

    /**
     * Captures a screenshot with the given prefix.
     *
     * @param className  The test class name
     * @param methodName The test method name
     * @param suffix     A suffix to add to the filename (e.g., "FAILED")
     */
    private void captureScreenshot(final String className, final String methodName, final String suffix) {
        try {
            final UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());

            final File screenshotDir = getScreenshotDirectory();
            final String filename = generateFilename(className, methodName, suffix);
            final File screenshotFile = new File(screenshotDir, filename);

            final boolean success = device.takeScreenshot(screenshotFile);

            if (success) {
                Log.i(TAG, "Screenshot saved: " + screenshotFile.getAbsolutePath());
            } else {
                Log.w(TAG, "Failed to capture screenshot for: " + className + "." + methodName);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error capturing screenshot: " + ex.getMessage(), ex);
        }
    }

    /**
     * Gets the screenshot directory, creating it if necessary.
     */
    private File getScreenshotDirectory() {
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
    private String generateFilename(final String className, final String methodName, final String suffix) {
        final String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return String.format(Locale.US, "%s_%s_%s_%s.png",
                simpleClassName, methodName, suffix, timestamp);
    }
}
