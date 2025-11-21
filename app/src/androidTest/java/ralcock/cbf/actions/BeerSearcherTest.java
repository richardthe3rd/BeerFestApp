package ralcock.cbf.actions;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ralcock.cbf.model.Beer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ralcock.cbf.model.BeerBuilder.aBeer;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

/**
 * Tests for BeerSearcher web search functionality.
 *
 * Tests verify Intent action, query formatting, and edge case handling.
 */
@RunWith(AndroidJUnit4.class)
public class BeerSearcherTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testSearchIntentAction() {
        Beer beer = aBeer()
                .called("Test Beer")
                .from(aBrewery().called("Test Brewery"))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        assertEquals("Intent action should be ACTION_WEB_SEARCH",
                Intent.ACTION_WEB_SEARCH, intent.getAction());
    }

    @Test
    public void testSearchQueryFormat() {
        final String beerName = "Bitter";
        final String breweryName = "Milton Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain brewery name in quotes",
                query.contains("\"" + breweryName + "\""));
        assertTrue("Query should contain beer name in quotes",
                query.contains("\"" + beerName + "\""));
    }

    @Test
    public void testSearchQueryContainsBothNames() {
        final String beerName = "Cambridge Bitter";
        final String breweryName = "City of Cambridge Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);
        String expectedQuery = String.format("\"%s\" \"%s\"", breweryName, beerName);

        assertEquals("Query should be formatted correctly", expectedQuery, query);
    }

    @Test
    public void testSearchQueryWithSpecialCharacters() {
        final String beerName = "Porter & Stout";
        final String breweryName = "O'Hanlon's Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain brewery name with apostrophe",
                query.contains(breweryName));
        assertTrue("Query should contain beer name with ampersand",
                query.contains(beerName));
    }

    @Test
    public void testSearchQueryWithLongNames() {
        final String beerName = "Extra Special Bitter Premium Ale";
        final String breweryName = "The Really Long Brewery Name Company Limited";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain full brewery name",
                query.contains("\"" + breweryName + "\""));
        assertTrue("Query should contain full beer name",
                query.contains("\"" + beerName + "\""));
    }

    @Test
    public void testSearchQueryWithEmptyBeerName() {
        final String beerName = "";
        final String breweryName = "Test Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);
        String expectedQuery = String.format("\"%s\" \"%s\"", breweryName, beerName);

        assertEquals("Query should handle empty beer name", expectedQuery, query);
    }

    @Test
    public void testSearchQueryWithEmptyBreweryName() {
        final String beerName = "Test Beer";
        final String breweryName = "";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);
        String expectedQuery = String.format("\"%s\" \"%s\"", breweryName, beerName);

        assertEquals("Query should handle empty brewery name", expectedQuery, query);
    }

    @Test
    public void testSearchQueryWithWhitespaceInNames() {
        final String beerName = "Double  Spaced  Beer";
        final String breweryName = "Triple   Spaced   Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should preserve whitespace in beer name",
                query.contains(beerName));
        assertTrue("Query should preserve whitespace in brewery name",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryWithLeadingTrailingWhitespace() {
        final String beerName = "  Beer With Spaces  ";
        final String breweryName = "  Brewery With Spaces  ";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should preserve leading/trailing whitespace in beer name",
                query.contains(beerName));
        assertTrue("Query should preserve leading/trailing whitespace in brewery name",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryWithNumbers() {
        final String beerName = "IPA 2024 Edition";
        final String breweryName = "Brewery No. 5";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain numbers in beer name",
                query.contains(beerName));
        assertTrue("Query should contain numbers in brewery name",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryWithUnicodeCharacters() {
        final String beerName = "Bière Française 🍺";
        final String breweryName = "Bräuhaus München";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain Unicode characters in beer name",
                query.contains(beerName));
        assertTrue("Query should contain Unicode characters in brewery name",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryWithMixedCase() {
        final String beerName = "MiXeD CaSe BeEr";
        final String breweryName = "mIxEd cAsE bReWeRy";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should preserve case in beer name",
                query.contains(beerName));
        assertTrue("Query should preserve case in brewery name",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryWithVeryLongNames() {
        StringBuilder longBeerName = new StringBuilder();
        StringBuilder longBreweryName = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longBeerName.append("VeryLongBeerName");
            longBreweryName.append("VeryLongBreweryName");
        }

        Beer beer = aBeer()
                .called(longBeerName.toString())
                .from(aBrewery().called(longBreweryName.toString()))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null for very long names", query);
        assertTrue("Query should contain very long beer name",
                query.contains(longBeerName.toString()));
        assertTrue("Query should contain very long brewery name",
                query.contains(longBreweryName.toString()));
    }

    @Test
    public void testSearchQueryWithQuotes() {
        final String beerName = "The \"Best\" Beer";
        final String breweryName = "\"Premium\" Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain beer name with quotes",
                query.contains(beerName));
        assertTrue("Query should contain brewery name with quotes",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryWithNewlines() {
        final String beerName = "Beer\nWith\nNewlines";
        final String breweryName = "Brewery\nWith\nNewlines";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);

        assertNotNull("Search query should not be null", query);
        assertTrue("Query should contain beer name with newlines",
                query.contains(beerName));
        assertTrue("Query should contain brewery name with newlines",
                query.contains(breweryName));
    }

    @Test
    public void testSearchQueryOrderIsBreweryThenBeer() {
        final String beerName = "ZZZ Beer";
        final String breweryName = "AAA Brewery";

        Beer beer = aBeer()
                .called(beerName)
                .from(aBrewery().called(breweryName))
                .build();

        BeerSearcher searcher = new BeerSearcher(context);
        Intent intent = searcher.makeSearchIntent(beer);

        String query = intent.getStringExtra(SearchManager.QUERY);
        String expectedQuery = String.format("\"%s\" \"%s\"", breweryName, beerName);

        assertEquals("Query should have brewery first, then beer", expectedQuery, query);

        // Also verify order by checking index positions
        int breweryIndex = query.indexOf(breweryName);
        int beerIndex = query.indexOf(beerName);
        assertTrue("Brewery should appear before beer in query",
                breweryIndex < beerIndex);
    }
}
