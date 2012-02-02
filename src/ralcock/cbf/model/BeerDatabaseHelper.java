package ralcock.cbf.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class BeerDatabaseHelper extends SQLiteOpenHelper {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDatabaseHelper.class.getName();

    private static final int DB_VERSION = 3; // For Winter Ale Festival 2012

    public BeerDatabaseHelper(final Context context, final String databaseName) {
        super(context, databaseName, null, DB_VERSION);
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
