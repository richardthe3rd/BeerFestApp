package ralcock.cbf.model;

import android.database.Cursor;
import android.test.AndroidTestCase;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class BeerDatabaseTest extends AndroidTestCase {

    @Override
    public void setUp() throws Exception {
        getContext().deleteDatabase(BeerDatabase.DATABASE_NAME);
    }

    @Override
    public void tearDown() throws Exception {
        getContext().deleteDatabase(BeerDatabase.DATABASE_NAME);
    }

    public void testBeerDatabase() throws IOException, JSONException {
        BeerDatabaseHelper databaseHelper = new BeerDatabaseHelper(getContext());
        BeerDatabase db = new BeerDatabase(databaseHelper);

        InputStream inputStream = BeerDatabaseTest.class.getResourceAsStream("resources/one_beer.txt");
        for(Beer beer: new JsonBeerList(inputStream)){
            db.insertBeer(beer);
        }

        Cursor c = db.getBeerListCursor(SortOrder.BEER_NAME_ASC);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals("BREWERY_NAME",  c.getString(c.getColumnIndexOrThrow(BeerDatabase.BREWERY_NAME_COLUMN)));
        assertEquals("BEER_NAME",     c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_NAME_COLUMN)));
        assertEquals("1",             c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_ABV_COLUMN)));
        // beers not rated at start.
        assertNull(c.getString(c.getColumnIndexOrThrow(BeerDatabase.BEER_RATING_COLUMN)));
    }
}
