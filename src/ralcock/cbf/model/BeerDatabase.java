package ralcock.cbf.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public final class BeerDatabase {

    public static final String BEERS_TABLE = "beers";
    public static final String RATINGS_TABLE = "ratings";
    public static final String BEER_NAME_COLUMN = "beer_name";
    public static final String BEER_NOTES_COLUMN = "beer_notes";
    public static final String BEER_ABV_COLUMN = "beer_abv";
    public static final String BREWERY_NAME_COLUMN = "brewery_name";
    public static final String BREWERY_NOTES_COLUMN = "brewery_notes";
    public static final String BEER_ID_COLUMN = "beer_id";
    public static final String BEER_RATING_COLUMN = "beer_rating";

    private SQLiteDatabase fReadableDatabase;
    private SQLiteDatabase fWritableDatabase;

    public BeerDatabase(Context context) {
        BeerDatabaseHelper beerDatabaseHelper = new BeerDatabaseHelper(context, new BeerListLoader());
        fReadableDatabase = beerDatabaseHelper.getReadableDatabase();
        fWritableDatabase = beerDatabaseHelper.getWritableDatabase();
    }

    public BeerWithRating getBeerForId(long beerId) {
        Cursor cursor = null;
        try {
            // TODO: Get rating in single query
            cursor = fReadableDatabase.query(BEERS_TABLE,
                    null, "_id="+beerId, null, null, null, null);
            cursor.moveToFirst();
            String breweryName = cursor.getString(cursor.getColumnIndexOrThrow(BREWERY_NAME_COLUMN));
            String breweryNotes = cursor.getString(cursor.getColumnIndexOrThrow(BREWERY_NOTES_COLUMN));
            Brewery brewery = new Brewery(breweryName, breweryNotes);
            String beerName = cursor.getString(cursor.getColumnIndexOrThrow(BEER_NAME_COLUMN));
            float beerAbv = cursor.getFloat(cursor.getColumnIndexOrThrow(BEER_ABV_COLUMN));
            String beerNotes = cursor.getString(cursor.getColumnIndexOrThrow(BEER_NOTES_COLUMN));

            Beer beer = new Beer(brewery, beerName, beerAbv, beerNotes);
            StarRating rating = getRatingForBeer(beerId);

            return new BeerWithRating(beer, rating);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public StarRating getRatingForBeer(long beerId) {
        Cursor cursor = null;
        try {
            cursor = fReadableDatabase.query(RATINGS_TABLE,
                    new String[]{BEER_RATING_COLUMN},
                    BEER_ID_COLUMN+"="+beerId, null, null, null, null, "1");
            cursor.moveToFirst();

            int rating = 0;
            if (cursor.getCount() == 1)
                rating = cursor.getInt(cursor.getColumnIndexOrThrow(BEER_RATING_COLUMN));

            return new StarRating(rating);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public void rateBeer(long beerId, StarRating rating) {
        ContentValues cv = new ContentValues();
        cv.put(BEER_ID_COLUMN, beerId);
        cv.put(BEER_RATING_COLUMN, rating.getNumberOfStars());
        int nRowsUpdated = fWritableDatabase.update(RATINGS_TABLE, cv,
                BeerDatabase.BEER_ID_COLUMN+"="+beerId, null);
        if (nRowsUpdated == 0) {
            fWritableDatabase.insert(RATINGS_TABLE, BEER_RATING_COLUMN, cv);
        }
    }

    public void close() {
        fReadableDatabase.close();
        fWritableDatabase.close();
    }

    public Cursor getBeerListCursor(SortOrder sortOrder) {
        String orderByClause = sortOrder.getColumnName() + (sortOrder.isAscending() ? " ASC" : " DESC");

        String sqlStatement = "SELECT " + BEERS_TABLE + "._id, " + BEER_NAME_COLUMN + ", " + BEER_ABV_COLUMN + ", " + BREWERY_NAME_COLUMN + ", " + BEER_RATING_COLUMN
                + " FROM " + BEERS_TABLE + " LEFT JOIN " + RATINGS_TABLE + " ON " + BEERS_TABLE + "._id=" + RATINGS_TABLE+"."+BEER_ID_COLUMN
                + " ORDER BY " + orderByClause;

        return fReadableDatabase.rawQuery(sqlStatement, null);
    }

    public Cursor getBeerListCursor(SortOrder sortOrder, CharSequence constraint) {
        String orderByClause = sortOrder.getColumnName() + (sortOrder.isAscending() ? " ASC" : " DESC");

        String sqlStatement = "SELECT " + BEERS_TABLE + "._id, " + BEER_NAME_COLUMN + ", " + BEER_ABV_COLUMN + ", " + BREWERY_NAME_COLUMN + ", " + BEER_RATING_COLUMN
                + " FROM " + BEERS_TABLE + " LEFT JOIN " + RATINGS_TABLE + " ON " + BEERS_TABLE + "._id=" + RATINGS_TABLE+"."+BEER_ID_COLUMN
                + " WHERE " + BEER_NAME_COLUMN + " LIKE " + "'%"+constraint+"%'" + " OR " + BREWERY_NAME_COLUMN + " LIKE " + "'%"+constraint+"%'"
                + " ORDER BY " + orderByClause;

        return fReadableDatabase.rawQuery(sqlStatement, null);
    }

    private static final class BeerDatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "BeerDatabaseHelper";

        private static final int DB_VERSION = 2;
        private static final String DB_NAME = "BEERS";

        private final BeerListLoader fLoader;
        private final Context fContext;

        public BeerDatabaseHelper(Context context, BeerListLoader loader) {
            super(context, DB_NAME, null, DB_VERSION);
            fContext = context;
            fLoader = loader;
        }

        @Override
        public void onCreate(final SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE " + BEERS_TABLE + " " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    BEER_NAME_COLUMN + " TEXT, " +
                    BEER_NOTES_COLUMN + " TEXT, " +
                    BEER_ABV_COLUMN + " REAL, " +
                    BREWERY_NAME_COLUMN + " TEXT, " +
                    BREWERY_NOTES_COLUMN + " TEXT" +
                    ")");

            InputStream inputStream = null;
            try {
                inputStream = fContext.getAssets().open("beers.json");
                fLoader.loadBeers(inputStream, new BeerListLoader.BeerHandler() {
                    public void handleBeer(Beer beer) {
                        ContentValues cv = new ContentValues();
                        cv.put(BEER_NAME_COLUMN, beer.getName());
                        cv.put(BEER_NOTES_COLUMN, beer.getNotes());
                        cv.put(BEER_ABV_COLUMN, beer.getAbv());
                        cv.put(BREWERY_NAME_COLUMN, beer.getBrewery().getName());
                        cv.put(BREWERY_NOTES_COLUMN, beer.getBrewery().getDescription());
                        sqLiteDatabase.insert(BEERS_TABLE, BEER_NAME_COLUMN, cv);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } finally {
                if (inputStream!=null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.i(TAG, "Failed to close stream", e);
                    }
                }
            }

            sqLiteDatabase.execSQL("CREATE TABLE " + RATINGS_TABLE + " " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BEER_ID_COLUMN + " INTEGER, " +
                    BEER_RATING_COLUMN + " REAL " +
                    ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_version, int new_version) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BEERS_TABLE);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RATINGS_TABLE);
            onCreate(sqLiteDatabase);
        }
    }
}
