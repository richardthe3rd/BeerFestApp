package ralcock.cbf;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.j256.ormlite.misc.TransactionManager;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;
import java.util.concurrent.Callable;

class UpdateBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = LoadBeersTask.class.getName();

    private final Context fContext;
    private final BeerDatabaseHelper fHelper;
    private UpdateTaskListener fListener;

    UpdateBeersTask(final Context context, final UpdateTaskListener listener) {
        fContext = context;
        fListener = listener;
        fHelper = new BeerDatabaseHelper(context);
    }

    @Override
    protected void onPreExecute() {
        fListener.notifyUpdateStarted();
    }

    @Override
    protected void onPostExecute(final Long aLong) {
        fListener.notifyUpdateComplete(aLong);
    }

    @Override
    protected void onProgressUpdate(final Beer... values) {
        fListener.notifyUpdateProgress(values);
    }

    @Override
    protected Long doInBackground(final Iterable<Beer>... iterables) {
        final Iterable<Beer> beerList = iterables[0];
        long count = 0;
        try {
            count = TransactionManager.callInTransaction(fHelper.getConnectionSource(), new Callable<Long>() {
                public Long call() throws Exception {
                    return initializeDatabase(beerList);
                }
            });
        } catch (SQLException e) {
            Toast.makeText(fContext, "Failed to update database." + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Log.i(TAG, "Finished background initialization of database.");
        return count;
    }

    private long initializeDatabase(Iterable<Beer> beers) throws SQLException {
        long count = 0;
        for (Beer beer : beers) {
            final Brewery brewery = beer.getBrewery();
            if (brewery.getId() == 0) {
                fHelper.getBreweryDao().updateFromFestivalOrCreate(brewery);
            }

            if (beer.getId() == 0) {
                fHelper.getBeerDao().updateFromFestivalOrCreate(beer);
            }

            count++;

            publishProgress(beer);
        }
        return count;
    }

}
