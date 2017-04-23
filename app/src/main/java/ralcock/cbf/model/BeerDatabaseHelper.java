package ralcock.cbf.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ralcock.cbf.model.dao.Beers;
import ralcock.cbf.model.dao.BeersImpl;
import ralcock.cbf.model.dao.Breweries;
import ralcock.cbf.R;

import java.sql.SQLException;

public final class BeerDatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "BEERS";

    private static final int DB_VERSION = 27; // cbf44

    private Breweries fBreweries;
    private Beers fBeers;

    public BeerDatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION,  R.raw.ormlite_config);
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase,
                         final ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Beer.class);
            TableUtils.createTable(connectionSource, Brewery.class);
        } catch (SQLException sqlx) {
            throw new RuntimeException(sqlx);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase,
                          final ConnectionSource connectionSource,
                          int old_version, int new_version) {
        try {
            TableUtils.dropTable(connectionSource, Beer.class, true);
            TableUtils.dropTable(connectionSource, Brewery.class, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        onCreate(sqLiteDatabase, connectionSource);
    }

    public Beers getBeers() {
        try {
            if (fBeers == null) {
                BeersImpl beers = DaoManager.createDao(getConnectionSource(), Beer.class);
                beers.setBreweries(getBreweries());
                fBeers = beers;
            }
            return fBeers;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Breweries getBreweries() {
        try {
            if (fBreweries == null) {
                fBreweries = DaoManager.createDao(getConnectionSource(), Brewery.class);
            }
            return fBreweries;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAll() {
        try {
            TableUtils.clearTable(getConnectionSource(), Beer.class);
            TableUtils.clearTable(getConnectionSource(), Brewery.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
