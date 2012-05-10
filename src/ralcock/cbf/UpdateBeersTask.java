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

public class UpdateBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = LoadBeersTask.class.getName();
    private ConnectionSource fConnectionSource;
    private BreweryDao fBreweryDao;
    private BeerDao fBeerDao;
    private final BeerList fBeerList;
    private final BeerListAdapter fBeerListAdapter;

    private final ProgressDialog fDialog;
    private int fNumberOfBeers;

    public UpdateBeersTask(final ConnectionSource connectionSource,
                           final BreweryDao breweryDao,
                           final BeerDao beerDao,
                           final BeerList beerList,
                           final BeerListAdapter beerListAdapter,
                           final ProgressDialog dialog) {
        fConnectionSource = connectionSource;
        fBreweryDao = breweryDao;
        fBeerDao = beerDao;
        fBeerList = beerList;
        fBeerListAdapter = beerListAdapter;
        fDialog = dialog;
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
        fBeerList.updateBeerList();
        fBeerListAdapter.notifyDataSetChanged();
        fDialog.dismiss();
    }

    @Override
    protected Long doInBackground(final Iterable<Beer>... iterables) {
        final Iterable<Beer> beerList = iterables[0];
        final long count;
        try {
            count = TransactionManager.callInTransaction(fConnectionSource, new Callable<Long>() {
                public Long call() throws Exception {
                    return initializeDatabase(beerList);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Log.i(TAG, "Finished background initialization of database.");
        return count;
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
