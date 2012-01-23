package ralcock.cbf.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class BeerDatabaseHelper extends SQLiteOpenHelper {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = "BeerDatabaseHelper";

    private static final int DB_VERSION = 3; // For Winter Ale Festival 2012

    private final Iterable<Beer> fBeers;

    public BeerDatabaseHelper(final Context context, final Iterable<Beer> beers) {
        super(context, BeerDatabase.DATABASE_NAME, null, DB_VERSION);
        fBeers = beers;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + BeerDatabase.BEERS_TABLE + " " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                BeerDatabase.BEER_NAME_COLUMN + " TEXT, " +
                BeerDatabase.BEER_NOTES_COLUMN + " TEXT, " +
                BeerDatabase.BEER_ABV_COLUMN + " REAL, " +
                BeerDatabase.BREWERY_NAME_COLUMN + " TEXT, " +
                BeerDatabase.BREWERY_NOTES_COLUMN + " TEXT" +
                ")");

        for(Beer beer : fBeers) {
            ContentValues cv = new ContentValues();
            cv.put(BeerDatabase.BEER_NAME_COLUMN, beer.getName());
            cv.put(BeerDatabase.BEER_NOTES_COLUMN, beer.getNotes());
            cv.put(BeerDatabase.BEER_ABV_COLUMN, beer.getAbv());
            cv.put(BeerDatabase.BREWERY_NAME_COLUMN, beer.getBrewery().getName());
            cv.put(BeerDatabase.BREWERY_NOTES_COLUMN, beer.getBrewery().getDescription());
            sqLiteDatabase.insert(BeerDatabase.BEERS_TABLE, BeerDatabase.BEER_NAME_COLUMN, cv);
        }

        sqLiteDatabase.execSQL("CREATE TABLE " + BeerDatabase.RATINGS_TABLE + " " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BeerDatabase.BEER_ID_COLUMN + " INTEGER, " +
                BeerDatabase.BEER_RATING_COLUMN + " REAL " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_version, int new_version) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BeerDatabase.BEERS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BeerDatabase.RATINGS_TABLE);
        onCreate(sqLiteDatabase);
    }
}
