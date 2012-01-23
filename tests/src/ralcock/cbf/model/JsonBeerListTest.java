package ralcock.cbf.model;

import android.test.AndroidTestCase;

import java.io.InputStream;

public class JsonBeerListTest extends AndroidTestCase {

    public void testLoadBeers() throws Exception {
        InputStream inputStream = BeerDatabaseTest.class.getResourceAsStream("resources/one_beer.txt");
        final Beer expectedBeer = new Beer(new Brewery("BREWERY_NAME", "BREWERY_NOTES"), "BEER_NAME", 1.0f, "BEER_NOTES");
        JsonBeerList jsonBeerList = new JsonBeerList(inputStream);
        for(Beer beer : jsonBeerList) {
            assertEquals(expectedBeer, beer);
        }

    }
}
