package ralcock.cbf.model;

import static org.junit.Assert.assertEquals;

import androidx.test.runner.AndroidJUnit4;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JsonBeerListTest {

    private final Brewery fBrewery1 = new Brewery("1", "BREWERY_ONE", "BREWERY_ONE_NOTES");
    private final Brewery fBrewery2 = new Brewery("2", "BREWERY_TWO", "BREWERY_TWO_NOTES");

    private final Beer fBrewery1Beer1 =
            new BeerBuilder()
                    .withFestivalId("1")
                    .called("BEER_ONE")
                    .withABV(1.1f)
                    .withDescription("BEER_ONE_NOTES")
                    .withStyle("STYLE1")
                    .withStatus("BEER_ONE_STATUS")
                    .fromBrewery(fBrewery1)
                    .build();

    private final Beer fBrewery2Beer2 =
            new BeerBuilder()
                    .withFestivalId("2")
                    .called("BEER_TWO")
                    .withABV(2.2f)
                    .withDescription("BEER_TWO_NOTES")
                    .withStyle("STYLE2")
                    .withStatus("BEER_TWO_STATUS")
                    .fromBrewery(fBrewery2)
                    .build();

    private final Beer fBrewery2Beer3 =
            new BeerBuilder()
                    .withFestivalId("3")
                    .called("BEER_THREE")
                    .withABV(3.3f)
                    .withDescription("BEER_THREE_NOTES")
                    .withStyle("STYLE3")
                    .withStatus("BEER_THREE_STATUS")
                    .fromBrewery(fBrewery2)
                    .build();

    private static String convertStreamToString(InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    @Test
    public void testLoadBeers() throws JSONException {
        InputStream inputStream =
                JsonBeerListTest.class.getResourceAsStream("/ralcock/cbf/model/one_beer.json");
        final Beer expectedBeer = fBrewery1Beer1;
        JsonBeerList jsonBeerList = new JsonBeerList(convertStreamToString(inputStream));
        for (Beer beer : jsonBeerList) {
            assertEquals(expectedBeer, beer);
        }
    }

    @Test
    public void testLoadBeers2() throws JSONException {
        InputStream inputStream =
                JsonBeerListTest.class.getResourceAsStream(
                        "/ralcock/cbf/model/two_breweries_three_beers.json");

        List<Beer> expectedBeers = new Vector<Beer>(3);
        expectedBeers.add(fBrewery1Beer1);
        expectedBeers.add(fBrewery2Beer2);
        expectedBeers.add(fBrewery2Beer3);

        JsonBeerList jsonBeerList = new JsonBeerList(convertStreamToString(inputStream));
        int index = 0;
        for (Beer beer : jsonBeerList) {
            assertEquals(expectedBeers.get(index++), beer);
        }
    }
}
