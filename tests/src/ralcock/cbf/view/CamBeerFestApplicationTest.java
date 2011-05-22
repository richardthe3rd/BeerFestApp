package ralcock.cbf;

import android.test.ActivityInstrumentationTestCase2;
import ralcock.cbf.view.CamBeerFestApplication;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class ralcock.cbf.view.CamBeerFestApplicationTest \
 * ralcock.cbf.tests/android.test.InstrumentationTestRunner
 */
public class CamBeerFestApplicationTest extends ActivityInstrumentationTestCase2<CamBeerFestApplication> {

    public CamBeerFestApplicationTest() {
        super("ralcock.cbf", CamBeerFestApplication.class);
    }

    public void testPreconditions(){
        assertNotNull(getActivity());
    }


}
