package ralcock.cbf;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;
import ralcock.cbf.view.BeerListAdapter;

import java.sql.SQLException;
import java.util.concurrent.Callable;

class LoadBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = "cbf." + LoadBeersTask.class.getSimpleName();

    private ConnectionSource fConnectionSource;
    private final BeerDao fBeerDao;
    private final BreweryDao fBreweryDao;
    private final BeerList fBeerList;
    private final BeerListAdapter fBeerListAdapter;

    private final ProgressDialog fDialog;
    private long fStartTime;

    LoadBeersTask(final ConnectionSource connectionSource,
                  final BeerDao beerDao,
                  final BreweryDao breweryDao,
                  final BeerListAdapter beerListAdapter,
                  final BeerList beerList,
                  final ProgressDialog progressDialog) {
        fConnectionSource = connectionSource;
        fBeerDao = beerDao;
        fBreweryDao = breweryDao;
        fBeerListAdapter = beerListAdapter;
        fBeerList = beerList;
        fDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        fStartTime = System.currentTimeMillis();
        fDialog.show();
    }

    @Override
    protected void onProgressUpdate(Beer... beers) {
        fDialog.setMessage("Loaded " + beers[0].getName());
    }

    @Override
    protected void onPostExecute(Long count) {
        final long time = (System.currentTimeMillis() - fStartTime) / 1000;

        final String message = "Loaded " + count + " beers in " + time + " seconds.";
        fDialog.setMessage(message);
        Log.i(TAG, message);

        fBeerList.updateBeerList();
        fBeerListAdapter.notifyDataSetChanged();
        fDialog.dismiss();
    }

    @Override
    protected Long doInBackground(Iterable<Beer>... beers) {
        final Iterable<Beer> beerList = beers[0];
        try {
            Log.i(TAG, "Starting background initialization of database from " + beers[0]);
            final long count = TransactionManager.callInTransaction(fConnectionSource, new Callable<Long>() {
                public Long call() throws Exception {
                    return initializeDatabase(beerList);
                }
            });
            Log.i(TAG, "Finished background initialization of database.");
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long initializeDatabase(Iterable<Beer> beers) {
        long count = 0;
        for (Beer beer : beers) {
            try {
                final Brewery brewery = beer.getBrewery();
                if (brewery.getId() == 0) {
                    fBreweryDao.updateFromFestivalOrCreate(brewery);
                }

                if (beer.getId() == 0) {
                    fBeerDao.updateFromFestivalOrCreate(beer);
                }

                count++;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            publishProgress(beer);
        }
        return count;
    }
}
