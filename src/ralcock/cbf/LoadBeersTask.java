package ralcock.cbf;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;
import ralcock.cbf.view.BeerListAdapter;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

class LoadBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = "cbf." + LoadBeersTask.class.getSimpleName();

    private final BeerDao fBeerDao;
    private final BreweryDao fBreweryDao;
    private final BeerList fBeerList;
    private final BeerListAdapter fBeerListAdapter;

    private final ProgressDialog fDialog;
    private long fStartTime;

    LoadBeersTask(final BeerDao beerDao, final BreweryDao breweryDao,
                  final BeerListAdapter beerListAdapter,
                  final BeerList beerList,
                  final ProgressDialog progressDialog) {
        fBeerDao = beerDao;
        fBreweryDao = breweryDao;
        fBeerListAdapter = beerListAdapter;
        fBeerList = beerList;
        fDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        fStartTime = System.currentTimeMillis();
        if (getNumberOfBeers() == 0) {
            fDialog.show();
        }
    }

    private long getNumberOfBeers() {
        try {
            return fBeerDao.getNumberOfBeers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onProgressUpdate(Beer... beers) {
        fDialog.setMessage("Loaded " + beers[0].getName());
    }

    @Override
    protected void onPostExecute(Long count) {
        final long time = (System.currentTimeMillis() - fStartTime) / 1000;
        fDialog.setMessage("Loaded " + count + " beers in " + time + " seconds.");
        fBeerList.updateBeerList();
        fBeerListAdapter.notifyDataSetChanged();
        fDialog.dismiss();
    }

    @Override
    protected Long doInBackground(Iterable<Beer>... beers) {
        // todo: Need a more intelligent way of deciding to do an update.
        if (getNumberOfBeers() == 0) {
            Log.i(TAG, "Starting background initialization of database from " + beers[0]);
            initializeDatabase(beers[0]);
            final long count = getNumberOfBeers();
            Log.i(TAG, "Finished background initialization of database. Loaded " + count);
            return count;
        } else {
            return 0L;
        }
    }

    private void initializeDatabase(Iterable<Beer> beers) {
        Set<Brewery> breweries = new HashSet<Brewery>();
        for (Beer beer : beers) {
            try {

                final Brewery brewery = beer.getBrewery();
                if (breweries.add(brewery)) {
                    if (0 == fBreweryDao.updateFromFestival(brewery)) {
                        // new brewery
                        fBreweryDao.create(brewery);
                    }
                    fBreweryDao.create(brewery);
                }

                //todo: merge instead of insert
                fBeerDao.create(beer);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            publishProgress(beer);
        }
    }
}
