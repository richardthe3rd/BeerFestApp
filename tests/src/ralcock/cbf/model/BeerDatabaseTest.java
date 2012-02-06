package ralcock.cbf.model;

import android.database.Cursor;
import android.test.AndroidTestCase;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class BeerDatabaseTest extends AndroidTestCase {

    private BeerDatabase fBeerDataBase;

    @Override
    public void setUp() throws Exception {
       fBeerDataBase = new BeerDatabase(new BeerDatabaseHelper(getContext(), null));
    }

    public void testBeerDatabase() throws IOException, JSONException {

        InputStream inputStream = BeerDatabaseTest.class.getResourceAsStream("resources/one_beer.json");
        for(Beer beer: new JsonBeerList(inputStream)){
            fBeerDataBase.insertBeer(beer);
        }

        Cursor c = fBeerDataBase.getBeerListCursor(SortOrder.BEER_NAME_ASC);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        final String actualBreweryName = c.getString(c.getColumnIndexOrThrow(BeerDatabase.BREWERY_NAME_COLUMN));
        assertEquals("BREWERY_ONE", actualBreweryName);
        final String actualBeerName = c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_NAME_COLUMN));
        assertEquals("BEER_ONE", actualBeerName);
        final float actualBeerAbv = c.getFloat(c.getColumnIndexOrThrow(BeerDatabase.BEER_ABV_COLUMN));
        assertEquals(1.0f, actualBeerAbv);
        final String actualBeerStatus = c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_STATUS_COLUMN));
        assertEquals("BEER_ONE_STATUS", actualBeerStatus);
        // beers not rated at start.
        assertNull(c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_RATING_COLUMN)));
    }
}
