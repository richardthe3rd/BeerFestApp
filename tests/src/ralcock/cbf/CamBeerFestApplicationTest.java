package ralcock.cbf;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
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
    private ListView fListView;

    public CamBeerFestApplicationTest() {
        super("ralcock.cbf", CamBeerFestApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        fActivity = getActivity();
        fListView = (ListView) fActivity.findViewById(android.R.id.list);
    }

    public void testPreconditions() throws Exception {
        assertNotNull(fActivity);
    }

    @UiThreadTest
    public void testTextFilter() throws Exception {
        fActivity.filterBy("");
        int originalCount = fListView.getAdapter().getCount();

        fActivity.filterBy("MILD");
        int filteredCount = fListView.getAdapter().getCount();
        assertTrue(filteredCount < originalCount);

        fActivity.filterBy("");
        int resetCount = fListView.getAdapter().getCount();
        assertEquals(originalCount, resetCount);
    }
}
