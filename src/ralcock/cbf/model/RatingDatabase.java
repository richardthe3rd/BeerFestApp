package ralcock.cbf.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RatingDatabase extends SQLiteOpenHelper {
    private static final String TAG = RatingDatabase.class.getName();

    private static final int DB_VERSION = 1;
    private static final String BEER_RATINGS_TABLE = "beer_ratings";
    private static final String RATING_COLUMN = "RATING";
    private static final String BREWERY_COLUMN = "BREWERY";
    private static final String BEER_COLUMN = "BEER";

    public RatingDatabase(Context context) {
        super(context, "beer_rating", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = String.format("CREATE TABLE %s (ID INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s INT);",
                BEER_RATINGS_TABLE, BREWERY_COLUMN, BEER_COLUMN, RATING_COLUMN);
        sqLiteDatabase.execSQL(sql);
        Log.i(TAG, "Created RatingDatabase");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    public Rating getRatingForBeer(Beer beer) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(BEER_RATINGS_TABLE, new String[]{RATING_COLUMN},
                    whereBeerIs(beer), null, null, null, null, "1");
            cursor.moveToFirst();
            if (cursor.getCount() == 1) {
                String ratingString = cursor.getString(cursor.getColumnIndexOrThrow(RATING_COLUMN));
                try {
                    return Rating.valueOf(ratingString);
                } catch (IllegalArgumentException iax) {
                    Log.e(TAG, "Illegal rating string " + ratingString + " returning " + Rating.UNRATED);
                    return Rating.UNRATED;
                }
            } else {
                return Rating.UNRATED;
            }
        } finally {
            close(db);
            close(cursor);
        }
    }

    private static void close(SQLiteDatabase db) {
        if (db!=null) db.close();
    }

    private static void close(Cursor c) {
        if (c!=null) c.close();
    }

    public void setRatingForBeer(Beer beer, Rating rating) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();

            ContentValues ratingContent = new ContentValues();
            ratingContent.put(RATING_COLUMN, rating.name());

            String whereBeerIs = whereBeerIs(beer);

            int nUpdated = db.update(BEER_RATINGS_TABLE, ratingContent, whereBeerIs, null);

            if (nUpdated == 0) {
                // need to insert a new row
                ratingContent.put(BREWERY_COLUMN, beer.getBrewery().getName());
                ratingContent.put(BEER_COLUMN, beer.getName());
                Log.d(TAG, "Inserting rating for " + beer);
                db.insert(BEER_RATINGS_TABLE, null, ratingContent);
            } else {
                Log.d(TAG, "Updated rating for " + beer);
            }
        } finally {
            close(db);
            close(cursor);
        }

    }

    private static String whereBeerIs(Beer beer) {
        return String.format("%s=\"%s\" AND %s=\"%s\"",
                        BREWERY_COLUMN, beer.getBrewery().getName(),
                        BEER_COLUMN, beer.getName());
    }
}
