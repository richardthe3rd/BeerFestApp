package ralcock.cbf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;

public class LifecycleTest extends ActivityUnitTestCase<CamBeerFestApplication> {

    private Activity fActivity;

    public LifecycleTest() {
        super(CamBeerFestApplication.class);
    }

    @UiThreadTest
    public void testLifeCycle(){
        Intent intent = new Intent(getInstrumentation().getTargetContext(), CamBeerFestApplication.class);
        startActivity(intent, new Bundle(), null);
        fActivity = getActivity();

        getInstrumentation().callActivityOnStart(fActivity);
        getInstrumentation().callActivityOnResume(fActivity);

        //TODO: assert that the value is the expected one (based on what you fed in the bundle)
        Bundle newBundle = new Bundle();
        getInstrumentation().callActivityOnSaveInstanceState(fActivity, newBundle);
        getInstrumentation().callActivityOnPause(fActivity);
        getInstrumentation().callActivityOnStop(fActivity);
        getInstrumentation().callActivityOnDestroy(fActivity);

        // Initialize activity with the saved bundle
        getInstrumentation().callActivityOnCreate(fActivity, newBundle);
        getInstrumentation().callActivityOnResume(fActivity);
    }

}
