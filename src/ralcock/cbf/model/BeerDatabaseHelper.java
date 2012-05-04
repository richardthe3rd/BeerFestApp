package ralcock.cbf.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ralcock.cbf.R;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;

public final class BeerDatabaseHelper extends OrmLiteSqliteOpenHelper {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDatabaseHelper.class.getName();

    public static final String DATABASE_NAME = "BEERS";

    private static final int DB_VERSION = 11; // added indices + unique

    private BreweryDao fBreweryDao;
    private BeerDao fBeerDao;

    public BeerDatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION, R.raw.ormlite_config);
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

    public BeerDao getBeerDao() {
        try {
            if (fBeerDao == null) {
                fBeerDao = DaoManager.createDao(getConnectionSource(), Beer.class);
            }
            return fBeerDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public BreweryDao getBreweryDao() {
        try {
            if (fBreweryDao == null) {
                fBreweryDao = DaoManager.createDao(getConnectionSource(), Brewery.class);
            }
            return fBreweryDao;
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
