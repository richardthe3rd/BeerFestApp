package ralcock.cbf.model;

import android.test.AndroidTestCase;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class JsonBeerListTest extends AndroidTestCase {

    private final Brewery fBrewery1 = new Brewery("BREWERY_ONE", "BREWERY_ONE_NOTES");
    private final Brewery fBrewery2 = new Brewery("BREWERY_TWO", "BREWERY_TWO_NOTES");

    private final Beer fBrewery1Beer1 = new Beer(fBrewery1, "BEER_ONE", 1.1f, "BEER_ONE_NOTES", "BEER_ONE_STATUS");
    private final Beer fBrewery1Beer2 = new Beer(fBrewery2, "BEER_TWO", 2.2f, "BEER_TWO_NOTES", "BEER_TWO_STATUS");
    private final Beer fBrewery2Beer3 = new Beer(fBrewery2, "BEER_THREE", 3.3f, "BEER_THREE_NOTES", "BEER_THREE_STATUS");

    public void testLoadBeers() throws Exception {
        InputStream inputStream = JsonBeerListTest.class.getResourceAsStream("resources/one_beer.json");
        final Beer expectedBeer = fBrewery1Beer1;
        JsonBeerList jsonBeerList = new JsonBeerList(inputStream);
        for (Beer beer : jsonBeerList) {
            assertEquals(expectedBeer, beer);
        }
    }

    public void testLoadBeers2() throws Exception {
        InputStream inputStream = JsonBeerListTest.class.getResourceAsStream("resources/two_breweries_three_beers.json");

        List<Beer> expectedBeers = new Vector<Beer>(3);
        expectedBeers.add(fBrewery1Beer1);
        expectedBeers.add(fBrewery1Beer2);
        expectedBeers.add(fBrewery2Beer3);

        JsonBeerList jsonBeerList = new JsonBeerList(inputStream);
        int index = 0;
        for (Beer beer : jsonBeerList) {
            assertEquals(expectedBeers.get(index++), beer);
        }

    }


}
