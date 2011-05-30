package ralcock.cbf.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;


public class BeerDatabase extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "BEERS";
    private final Context fContext;

    public BeerDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        fContext = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE beers "+
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "beer_name TEXT, beer_notes TEXT, beer_abv REAL, brewery_name TEXT, brewery_notes TEXT"+
                ")");

        InputStream inputStream;
        try {
            inputStream = fContext.getAssets().open("beers.json");
            BeerListLoader loader = new BeerListLoader( fContext, inputStream);
            loader.loadBeers(new BeerListLoader.BeerHandler() {
                public void handleBeer(Beer beer) {
                    ContentValues cv = new ContentValues();
                    cv.put("beer_name", beer.getName());
                    cv.put("beer_notes", beer.getNotes());
                    cv.put("beer_abv", beer.getAbv());
                    cv.put("brewery_name", beer.getBrewery().getName());
                    cv.put("brewery_notes", beer.getBrewery().getDescription());
                    sqLiteDatabase.insert("beers", "beer_name", cv);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_version, int new_version) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS beers");
        onCreate(sqLiteDatabase);
    }

}
