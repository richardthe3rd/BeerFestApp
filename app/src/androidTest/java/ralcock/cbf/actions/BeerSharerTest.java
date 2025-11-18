package ralcock.cbf.actions;

import static org.junit.Assert.assertTrue;
import static ralcock.cbf.model.BeerBuilder.aBeer;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;

@RunWith(AndroidJUnit4.class)
public class BeerSharerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    private static void assertNoStars(String extraText) {
        char star = 0x272F;
        assertTrue(
                "'" + extraText + "' should not contain any stars",
                !extraText.contains(String.valueOf(star)));
    }

    private static void assertStars(int n, String extraText) {
        String stars = new StarRating(n).toFancyString();
        assertTrue("'" + extraText + "' should contain '" + stars + "'", extraText.contains(stars));
    }

    @Test
    public void testExtraTextUnrated() {
        String theBeerName = "TheBeerName";
        final String theBreweryName = "TheBreweryName";
        Beer beer = aBeer().called(theBeerName).from(aBrewery().called(theBreweryName)).build();

        String theHashTag = context.getResources().getString(R.string.festival_hashtag);

        BeerSharer sharer = new BeerSharer(context);
        Intent intent = sharer.makeShareIntent(beer);
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertTrue(
                "'" + extraText + "' should contain '" + theBeerName + "'",
                extraText.contains(theBeerName));
        assertTrue(
                "'" + extraText + "' should contain '" + theBreweryName + "'",
                extraText.contains(theBreweryName));
        assertTrue(
                "'" + extraText + "' should contain '" + theHashTag + "'",
                extraText.contains(theHashTag));
        assertNoStars(extraText);
    }

    @Test
    public void testExtraTextWithStars() {
        String theBeerName = "AnotherBeerName";
        final String theBreweryName = "AnotherBreweryName";

        Beer beer = aBeer().called(theBeerName).from(aBrewery().called(theBreweryName)).build();

        String theHashTag = context.getResources().getString(R.string.festival_hashtag);
        beer.setNumberOfStars(new StarRating(3));

        BeerSharer sharer = new BeerSharer(context);
        Intent intent = sharer.makeShareIntent(beer);
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);

        assertTrue(
                "'" + extraText + "' should contain '" + theBeerName + "'",
                extraText.contains(theBeerName));
        assertTrue(
                "'" + extraText + "' should contain '" + theBreweryName + "'",
                extraText.contains(theBreweryName));
        assertTrue(
                "'" + extraText + "' should contain '" + theHashTag + "'",
                extraText.contains(theHashTag));
        assertStars(3, extraText);
    }

    @Test
    public void testShareSubject() {
        String theBeerName = "Yet Another Beer";
        final String theBreweryName = "Yet Another Brewery";

        Beer beer = aBeer().called(theBeerName).from(aBrewery().called(theBreweryName)).build();

        BeerSharer sharer = new BeerSharer(context);
        Intent intent = sharer.makeShareIntent(beer);
        String extraSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        final CharSequence theFestivalName =
                context.getResources().getString(R.string.festival_name);
        assertTrue(
                "'" + extraSubject + "' should contain '" + theFestivalName + '"',
                extraSubject.contains(theFestivalName));
    }
}
