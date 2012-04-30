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
        fDialog.show();
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
        Log.i(TAG, "Starting background initialization of database from " + beers[0]);
        initializeDatabase(beers[0]);
        final long count = getNumberOfBeers();
        Log.i(TAG, "Finished background initialization of database. Loaded " + count);
        return count;
    }

    private void initializeDatabase(Iterable<Beer> beers) {
        for (Beer beer : beers) {
            try {
                final Brewery brewery = beer.getBrewery();
                if (brewery.getId() == 0) {
                    fBreweryDao.updateFromFestivalOrCreate(brewery);
                }

                if (beer.getId() == 0) {
                    fBeerDao.updateFromFestivalOrCreate(beer);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            publishProgress(beer);
        }
    }
}
