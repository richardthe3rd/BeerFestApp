package ralcock.cbf.actions;

import android.content.Intent;
import android.test.AndroidTestCase;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;

import static ralcock.cbf.model.BeerBuilder.aBeer;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

public class BeerSharerTest extends AndroidTestCase {

    private static void assertNoStars(String extraText) {
        char star = 0x272F;
        assertTrue("'" + extraText + "' should not contain any stars",
                !extraText.contains(String.valueOf(star)));
    }

    private static void assertStars(int n, String extraText) {
        String stars = new StarRating(n).toFancyString();
        assertTrue("'" + extraText + "' should contain '" + stars + "'",
                extraText.contains(stars));
    }

    public void testExtraTextUnrated() throws Exception {
        String theBeerName = "TheBeerName";
        final String theBreweryName = "TheBreweryName";
        Beer beer = aBeer()
                .called(theBeerName)
                .from(aBrewery().called(theBreweryName))
                .build();

        String theHashTag = getContext().getResources().getString(R.string.festival_hashtag);

        BeerSharer sharer = new BeerSharer(getContext());
        Intent intent = sharer.makeShareIntent(beer);
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertTrue("'" + extraText + "' should contain '" + theBeerName + "'", extraText.contains(theBeerName));
        assertTrue("'" + extraText + "' should contain '" + theBreweryName + "'", extraText.contains(theBreweryName));
        assertTrue("'" + extraText + "' should contain '" + theHashTag + "'", extraText.contains(theHashTag));
        assertNoStars(extraText);
    }

    public void testExtraTextWithStars() throws Exception {
        String theBeerName = "AnotherBeerName";
        final String theBreweryName = "AnotherBreweryName";

        Beer beer = aBeer()
                .called(theBeerName)
                .from(aBrewery().called(theBreweryName))
                .build();

        String theHashTag = getContext().getResources().getString(R.string.festival_hashtag);
        beer.setNumberOfStars(new StarRating(3));

        BeerSharer sharer = new BeerSharer(getContext());
        Intent intent = sharer.makeShareIntent(beer);
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);

        assertTrue("'" + extraText + "' should contain '" + theBeerName + "'", extraText.contains(theBeerName));
        assertTrue("'" + extraText + "' should contain '" + theBreweryName + "'", extraText.contains(theBreweryName));
        assertTrue("'" + extraText + "' should contain '" + theHashTag + "'", extraText.contains(theHashTag));
        assertStars(3, extraText);
    }

    public void testShareSubject() throws Exception {
        String theBeerName = "Yet Another Beer";
        final String theBreweryName = "Yet Another Brewery";

        Beer beer = aBeer()
                .called(theBeerName)
                .from(aBrewery().called(theBreweryName))
                .build();

        BeerSharer sharer = new BeerSharer(getContext());
        Intent intent = sharer.makeShareIntent(beer);
        String extraSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        final CharSequence theFestivalName = getContext().getResources().getString(R.string.festival_name);
        assertTrue("'" + extraSubject + "' should contain '" + theFestivalName + '"', extraSubject.contains(theFestivalName));

    }
}
