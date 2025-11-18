package ralcock.cbf;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CamBeerFestApplicationInstrumentedTest {

    @Rule
    public ActivityTestRule<CamBeerFestApplication> mActivityRule =
            new ActivityTestRule<>(CamBeerFestApplication.class);

    @Test
    public void testCamBeerFestApplicationStarts() {
        onView(withId(R.id.mainListView)).check(matches(isDisplayed()));
    }
}
