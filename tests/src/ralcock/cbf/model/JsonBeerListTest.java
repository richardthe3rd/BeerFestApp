package ralcock.cbf.model;

import android.test.AndroidTestCase;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class JsonBeerListTest extends AndroidTestCase {

    private final Brewery fBrewery1 = new Brewery("1", "BREWERY_ONE", "BREWERY_ONE_NOTES");
    private final Brewery fBrewery2 = new Brewery("2", "BREWERY_TWO", "BREWERY_TWO_NOTES");

    private final Beer fBrewery1Beer1 = new Beer("1", "BEER_ONE", 1.1f, "BEER_ONE_NOTES", "STYLE1", "BEER_ONE_STATUS", fBrewery1);
    private final Beer fBrewery1Beer2 = new Beer("2", "BEER_TWO", 2.2f, "BEER_TWO_NOTES", "STYLE2", "BEER_TWO_STATUS", fBrewery2);
    private final Beer fBrewery2Beer3 = new Beer("3", "BEER_THREE", 3.3f, "BEER_THREE_NOTES", "STYLE2", "BEER_THREE_STATUS", fBrewery2);

    private static String convertStreamToString(InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    public void testLoadBeers() throws Exception {
        InputStream inputStream = JsonBeerListTest.class.getResourceAsStream("resources/one_beer.json");
        final Beer expectedBeer = fBrewery1Beer1;
        JsonBeerList jsonBeerList = new JsonBeerList(convertStreamToString(inputStream));
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

        JsonBeerList jsonBeerList = new JsonBeerList(convertStreamToString(inputStream));
        int index = 0;
        for (Beer beer : jsonBeerList) {
            assertEquals(expectedBeers.get(index++), beer);
        }

    }


}
