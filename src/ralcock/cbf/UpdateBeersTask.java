package ralcock.cbf;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.misc.TransactionManager;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.util.ExceptionReporter;

import java.sql.SQLException;
import java.util.concurrent.Callable;

class UpdateBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = LoadBeersTask.class.getName();

    private final Context fContext;
    private final ExceptionReporter fExceptionReporter;
    private final BeerDatabaseHelper fHelper;
    private UpdateTaskListener fListener;
    private int fNumberOfBeers;

    UpdateBeersTask(final Context context,
                    final ExceptionReporter exceptionReporter) {
        fHelper = OpenHelperManager.getHelper(context, BeerDatabaseHelper.class);
        fContext = context.getApplicationContext();
        fExceptionReporter = exceptionReporter;
        fNumberOfBeers = 0;
    }

    public int getNumberOfBeers() {
        return fNumberOfBeers;
    }

    public void setNumberOfBeers(final int numberOfBeers) {
        fNumberOfBeers = numberOfBeers;
    }

    public void setListener(final UpdateTaskListener listener) {
        fListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (fListener != null)
            fListener.notifyUpdateStarted(fNumberOfBeers);
    }

    @Override
    protected void onPostExecute(final Long aLong) {
        if (fListener != null)
            fListener.notifyUpdateComplete(aLong);
    }

    @Override
    protected void onProgressUpdate(final Beer... values) {
        if (fListener != null)
            fListener.notifyUpdateProgress(values);
    }

    @Override
    protected Long doInBackground(final Iterable<Beer>... iterables) {
        Log.i(TAG, "Starting update of database from new beer list.");
        final Iterable<Beer> beerList = iterables[0];
        long count = 0;
        try {
            count = TransactionManager.callInTransaction(fHelper.getConnectionSource(), new Callable<Long>() {
                public Long call() throws Exception {
                    return initializeDatabase(beerList);
                }
            });
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to update database." + e.getMessage(), e);
        }
        OpenHelperManager.releaseHelper();
        Log.i(TAG, "Finished update of database from beer list.");
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
