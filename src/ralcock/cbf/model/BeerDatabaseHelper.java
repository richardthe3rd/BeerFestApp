package ralcock.cbf.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public final class BeerDatabaseHelper extends OrmLiteSqliteOpenHelper {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDatabaseHelper.class.getName();

    public static final String DATABASE_NAME = "BEERS";

    private static final int DB_VERSION = 5; // using ormlite

    public BeerDatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
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

    public Dao<Beer, Long> getBeerDao() {
        try {
            return DaoManager.createDao(getConnectionSource(), Beer.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dao<Brewery, Long> getBreweryDao() {
        try {
            return DaoManager.createDao(getConnectionSource(), Brewery.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Beer getBeerWithId(final long beerId) {
        try {
            return getBeerDao().queryForId(beerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Beer> listAllBeers() {
        try {
            return getBeerDao().queryForAll();
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
