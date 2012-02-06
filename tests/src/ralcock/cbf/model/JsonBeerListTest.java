package ralcock.cbf.model;

import android.test.AndroidTestCase;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class JsonBeerListTest extends AndroidTestCase {

    public void testLoadBeers() throws Exception {
        InputStream inputStream = BeerDatabaseTest.class.getResourceAsStream("resources/one_beer.json");
        final Beer expectedBeer = new Beer(new Brewery("BREWERY_ONE", "BREWERY_ONE_NOTES"), "BEER_ONE", 1.0f, "BEER_ONE_NOTES", "BEER_ONE_STATUS");
        JsonBeerList jsonBeerList = new JsonBeerList(inputStream);
        for(Beer beer : jsonBeerList) {
            assertEquals(expectedBeer, beer);
        }
    }

    public void testLoadBeers2() throws Exception {
        InputStream inputStream = BeerDatabaseTest.class.getResourceAsStream("resources/two_breweries_three_beers.json");

        List<Beer> expectedBeers = new Vector<Beer>(3);
        expectedBeers.add(new Beer(new Brewery("BREWERY_ONE", "BREWERY_ONE_NOTES"), "BEER_ONE",   1.1f, "BEER_ONE_NOTES", "BEER_ONE_STATUS"));
        expectedBeers.add(new Beer(new Brewery("BREWERY_TWO", "BREWERY_TWO_NOTES"), "BEER_TWO",   2.2f, "BEER_TWO_NOTES", "BEER_TWO_STATUS"));
        expectedBeers.add(new Beer(new Brewery("BREWERY_TWO", "BREWERY_TWO_NOTES"), "BEER_THREE", 3.3f, "BEER_THREE_NOTES", "BEER_THREE_STATUS"));

        JsonBeerList jsonBeerList = new JsonBeerList(inputStream);
        int index = 0;
        for(Beer beer : jsonBeerList) {
            assertEquals(expectedBeers.get(index++), beer);
        }

    }


}
