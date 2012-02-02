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
        assertEquals("BREWERY_NAME",  c.getString(c.getColumnIndexOrThrow(BeerDatabase.BREWERY_NAME_COLUMN)));
        assertEquals("BEER_NAME",     c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_NAME_COLUMN)));
        assertEquals("1",             c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_ABV_COLUMN)));
        // beers not rated at start.
        assertNull(c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_RATING_COLUMN)));
    }
}
