package ralcock.cbf;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.view.BeerListAdapter;

import java.sql.SQLException;

class LoadBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = "cbf." + LoadBeersTask.class.getSimpleName();

    private final ProgressDialog fDialog;
    private final BeerListAdapter fBeerListAdapter;
    private final BeerDatabaseHelper fBeerDatabase;
    private BeerList fBeerList;

    private long fStartTime;

    LoadBeersTask(final BeerDatabaseHelper beerDatabase,
                  final BeerListAdapter beerListAdapter,
                  final BeerList beerList,
                  final ProgressDialog progressDialog) {
        fBeerDatabase = beerDatabase;
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
            return fBeerDatabase.getBeerDao().countOf();
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
        for (Beer beer : beers) {
            //todo: merge instead of insert
            try {
                fBeerDatabase.getBreweryDao().create(beer.getBrewery());
                fBeerDatabase.getBeerDao().create(beer);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            publishProgress(beer);
        }
    }
}
