package ralcock.cbf;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CamBeerFestApplicationInstrumentedTest {

    @Rule
    public ActivityTestRule<CamBeerFestApplication> mActivityRule =
            new ActivityTestRule<>(CamBeerFestApplication.class);

    @Test
    public void testCamBeerFestApplicationStarts() {
        onView(withId(R.layout.beer_listview_activity))
            .check(matches(isDisplayed()));
    }
}
