package ralcock.cbf;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.concurrent.Callable;

final class UpdateBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = LoadBeersTask.class.getName();

    private final ConnectionSource fConnectionSource;
    private final BreweryDao fBreweryDao;
    private final BeerDao fBeerDao;
    private final Context fContext;
    private final CamBeerFestApplication fApplication;
    private final ProgressDialog fDialog;
    private int fNumberOfBeers;

    UpdateBeersTask(final Context context,
                    final ConnectionSource connectionSource,
                    final BreweryDao breweryDao,
                    final BeerDao beerDao,
                    final CamBeerFestApplication application) {
        fContext = context;
        fConnectionSource = connectionSource;
        fBreweryDao = breweryDao;
        fBeerDao = beerDao;
        fApplication = application;
        fDialog = new ProgressDialog(context);
    }

    void setNumberOfBeers(int numberOfBeers) {
        fNumberOfBeers = numberOfBeers;
    }

    @Override
    protected void onPreExecute() {
        fDialog.setMessage("Updating database");
        fDialog.setProgress(0);
        fDialog.setMax(fNumberOfBeers);
        fDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fDialog.setIndeterminate(false);
        fDialog.setCancelable(false);
        fDialog.show();
    }

    @Override
    protected void onProgressUpdate(final Beer... values) {
        String name = values[0].getName();
        int maxLength = 12;
        if (name.length() > maxLength) {
            name = name.substring(0, maxLength - 3);
            name = name + "...";
        }
        fDialog.setMessage("Updated " + name);
        fDialog.incrementProgressBy(1);
    }

    @Override
    protected void onPostExecute(final Long numBeersUpdated) {
        fApplication.notifyBeersChanged();
        fDialog.dismiss();
    }

    @Override
    protected Long doInBackground(final Iterable<Beer>... iterables) {
        final Iterable<Beer> beerList = iterables[0];
        long count = 0;
        try {
            count = TransactionManager.callInTransaction(fConnectionSource, new Callable<Long>() {
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
                fBreweryDao.updateFromFestivalOrCreate(brewery);
            }

            if (beer.getId() == 0) {
                fBeerDao.updateFromFestivalOrCreate(beer);
            }

            count++;

            publishProgress(beer);
        }
        return count;
    }

}
