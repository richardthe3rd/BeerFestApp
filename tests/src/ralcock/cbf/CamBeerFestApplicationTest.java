package ralcock.cbf;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.ListView;
import ralcock.cbf.model.Beer;

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
        fActivity = this.getActivity();
        fListView = fActivity.getListView();
        assertNotNull(fListView);

        getInstrumentation().runOnMainSync( new Runnable(){
            public void run() {
                assertEquals(237, fListView.getCount());
            }
        });
    }

    public void testEndToEnd() throws Exception {
        getInstrumentation().waitForIdleSync();

        Beer beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "Adnams", "Gunhill");

        // sort by abv
        /*
        getInstrumentation().invokeMenuActionSync(fActivity, R.id.sort_by_abv, 0);
        beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "Belhaven", "Belhaven 60/- Ale");

        // and reverse sort by abv
        getInstrumentation().invokeMenuActionSync(fActivity, R.id.sort_by_abv, 0);
        beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "Green Jack", "Baltic Trader");

        // sort by beer
        getInstrumentation().invokeMenuActionSync(fActivity, R.id.sort_by_beer, 0);
        beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "Ha'penny", "16 String Jack");

        // reverse by beer
        getInstrumentation().invokeMenuActionSync(fActivity, R.id.sort_by_beer, 0);
        beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "Son of Sid", "XL Ale");

        // sort by brewery
        getInstrumentation().invokeMenuActionSync(fActivity, R.id.sort_by_brewery, 0);
        beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "Adnams", "Gunhill");

        getInstrumentation().invokeMenuActionSync(fActivity, R.id.sort_by_brewery, 0);
        beer = (Beer)fListView.getItemAtPosition(0);
        assertBeerIs(beer, "York", "Guzzler");
        */
        // todo this can't be right!
        /*
        getInstrumentation().sendStringSync("mild");
        while (fListView.getCount() != 14) {
            getInstrumentation().waitForIdleSync();
        }
        assertEquals(14, fListView.getCount());
        */

        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor("ralcock.cbf.view.BeerDetailsView",
                new Instrumentation.ActivityResult(0, null), /*block=*/true);

        // click the first child - should start details activity
        TouchUtils.tapView(this, fListView.getChildAt(0));

        getInstrumentation().waitForMonitor(monitor);
    }

    private void assertBeerIs(Beer beer, String brewery, String name) {
        assertEquals(brewery, beer.getBrewery().getName());
        assertEquals(name, beer.getName());
    }
}
