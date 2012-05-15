package ralcock.cbf;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class ralcock.cbf.CamBeerFestApplicationTest \
 * ralcock.cbf.tests/android.test.InstrumentationTestRunner
 */
public class CamBeerFestApplicationTest extends ActivityInstrumentationTestCase2<CamBeerFestApplication> {

    private CamBeerFestApplication fActivity;
    private EditText fSearchBox;
    private ListView fListView;
    private Button fClearSearchBox;

    public CamBeerFestApplicationTest() {
        super("ralcock.cbf", CamBeerFestApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        fActivity = getActivity();
        fClearSearchBox = (Button) fActivity.findViewById(R.id.clearSearchBoxBtn);
        fSearchBox = (EditText) fActivity.findViewById(R.id.searchBox);
        fListView = (ListView) fActivity.findViewById(android.R.id.list);
    }

    public void testPreconditions() throws Exception {
        assertNotNull(fActivity);
        assertNotNull(fClearSearchBox);
        assertNotNull(fSearchBox);
    }

    public void testTextFilter() throws Exception {
        fActivity.runOnUiThread(new Runnable() {
            public void run() {
                fClearSearchBox.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals("", fSearchBox.getText().toString());

        int originalCount = fListView.getAdapter().getCount();
        fActivity.runOnUiThread(new Runnable() {
            public void run() {
                fSearchBox.requestFocus();
            }
        });

        sendKeys("M I L D");
        assertEquals("mild", fSearchBox.getText().toString());
        int filteredCount = fListView.getAdapter().getCount();
        assertTrue(filteredCount < originalCount);

        fActivity.runOnUiThread(new Runnable() {
            public void run() {
                fClearSearchBox.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals("", fSearchBox.getText().toString());
        int resetCount = fListView.getAdapter().getCount();
        assertEquals(originalCount, resetCount);
    }
}
