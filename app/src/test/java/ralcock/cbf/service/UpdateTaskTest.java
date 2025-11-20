package ralcock.cbf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.testutil.TestDataFactory;

/**
 * Unit tests for UpdateTask using Robolectric for Android framework mocking.
 * Tests cover network operations, MD5 digest calculation, JSON parsing, and database updates.
 */
@RunWith(RobolectricTestRunner.class)
public class UpdateTaskTest {

    private BeerDatabaseHelper fDbHelper;

    @Before
    public void setUp() {
        // Database helper will be initialized per test
        fDbHelper = null;
    }

    @After
    public void tearDown() {
        if (fDbHelper != null) {
            fDbHelper.close();
        }
    }

    /**
     * Test 1: When update is not due, should return NoUpdateRequiredResult without downloading.
     */
    @Test
    public void testNoUpdateWhenNotDue() throws Exception {
        // Arrange
        final TestParams params = new TestParams(
                false,  // cleanUpdate = false
                false,  // updateDue = false
                null,   // needsUpdate not checked
                null,   // stream not needed
                null    // dbHelper not needed
        );

        final UpdateTask task = new UpdateTask();

        // Act
        final UpdateTask.Result result = task.doInBackground(params);

        // Assert
        assertNotNull(result);
        assertTrue("Expected NoUpdateRequiredResult when update not due",
                result instanceof UpdateTask.NoUpdateRequiredResult);
    }

    /**
     * Test 2: Successful download and database update with valid JSON.
     */
    @Test
    public void testSuccessfulDownloadAndUpdate() throws Exception {
        // Arrange - Create in-memory database
        final Context context = RuntimeEnvironment.getApplication();
        fDbHelper = new BeerDatabaseHelper(context);

        // Create test data
        final String validJson = TestDataFactory.createValidBeerJSON(5);
        final InputStream stream = new ByteArrayInputStream(validJson.getBytes());

        final TestParams params = new TestParams(
                false,  // cleanUpdate = false
                true,   // updateDue = true
                true,   // needsUpdate = true (MD5 differs)
                stream,
                fDbHelper
        );

        final UpdateTask task = new UpdateTask();

        // Act
        final UpdateTask.Result result = task.doInBackground(params);

        // Assert
        assertNotNull(result);

        // Debug: Check if update was successful
        if (!result.success()) {
            final Throwable throwable = result.getThrowable();
            if (throwable != null) {
                throwable.printStackTrace();
                throw new AssertionError("Update failed with exception: " + throwable.getMessage(), throwable);
            }
            throw new AssertionError("Update was not successful, got result type: " + result.getClass().getName());
        }

        assertTrue("Expected UpdateResult after successful update, but got: " + result.getClass().getName(),
                result instanceof UpdateTask.UpdateResult);

        final UpdateTask.UpdateResult updateResult = (UpdateTask.UpdateResult) result;
        assertEquals("Should have updated 5 beers", 5, updateResult.getCount());

        // Verify database was updated
        assertEquals("Database should contain 5 beers", 5, fDbHelper.getBeers().getNumberOfBeers());

        // Verify MD5 digest was calculated
        assertNotNull("MD5 digest should be present", updateResult.getDigest());
        assertTrue("MD5 digest should not be empty", updateResult.getDigest().length() > 0);
    }

    /**
     * Test 3: When MD5 matches (needsUpdate returns false), should return NoUpdateRequiredResult.
     */
    @Test
    public void testNoUpdateWhenMD5Matches() throws Exception {
        // Arrange
        final String validJson = TestDataFactory.createValidBeerJSON(3);
        final InputStream stream = new ByteArrayInputStream(validJson.getBytes());

        final TestParams params = new TestParams(
                false,  // cleanUpdate = false
                true,   // updateDue = true
                false,  // needsUpdate = false (MD5 matches)
                stream,
                null    // dbHelper not needed
        );

        final UpdateTask task = new UpdateTask();

        // Act
        final UpdateTask.Result result = task.doInBackground(params);

        // Assert
        assertNotNull(result);
        assertTrue("Expected NoUpdateRequiredResult when MD5 matches",
                result instanceof UpdateTask.NoUpdateRequiredResult);
    }

    /**
     * Test implementation of UpdateTask.Params for testing.
     * Allows control over all decision points in UpdateTask logic.
     */
    private static class TestParams extends UpdateTask.Params {
        private final boolean fCleanUpdate;
        private final boolean fUpdateDue;
        private final Boolean fNeedsUpdate;
        private final InputStream fStream;
        private final BeerDatabaseHelper fDatabaseHelper;

        public TestParams(
                final boolean cleanUpdate,
                final boolean updateDue,
                final Boolean needsUpdate,
                final InputStream stream,
                final BeerDatabaseHelper databaseHelper) {
            fCleanUpdate = cleanUpdate;
            fUpdateDue = updateDue;
            fNeedsUpdate = needsUpdate;
            fStream = stream;
            fDatabaseHelper = databaseHelper;
        }

        @Override
        MessageDigest getDigest() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance("MD5");
        }

        @Override
        InputStream openStream() {
            return fStream;
        }

        @Override
        BeerDatabaseHelper getDatabaseHelper() {
            return fDatabaseHelper;
        }

        @Override
        boolean cleanUpdate() {
            return fCleanUpdate;
        }

        @Override
        boolean needsUpdate(final byte[] digest) {
            if (fNeedsUpdate == null) {
                throw new IllegalStateException("needsUpdate not configured for this test");
            }
            return fNeedsUpdate;
        }

        @Override
        boolean updateDue() {
            return fUpdateDue;
        }
    }
}
