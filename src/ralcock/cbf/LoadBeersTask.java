package ralcock.cbf;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabase;

class LoadBeersTask extends AsyncTask<Iterable<Beer>, Beer, Long> {

    private static final String TAG = "cbf."+LoadBeersTask.class.getSimpleName();

    private final ProgressDialog fDialog;
    private final BeerListView fBeerListView;
    private final BeerDatabase fBeerDatabase;
    private long fStartTime;

    LoadBeersTask(final BeerDatabase beerDatabase,
                  final BeerListView beerListView,
                  final ProgressDialog progressDialog) {
        fBeerDatabase = beerDatabase;
        fBeerListView = beerListView;
        fDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        fStartTime = System.currentTimeMillis();
        if(fBeerDatabase.countBeers()==0) {
            fDialog.show();
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
        fBeerListView.updateCursor();
        fDialog.dismiss();
    }

    @Override
    protected Long doInBackground(Iterable<Beer>... beers) {
        // todo: Need a more intelligent way of deciding to do an update.
        if (fBeerDatabase.countBeers()==0) {
            Log.i(TAG, "Starting background initialization of database from " + beers[0]);
            initializeDatabase(beers[0]);
            final long count = fBeerDatabase.countBeers();
            Log.i(TAG, "Finished background initialization of database. Loaded " + count);
            return count;
        } else {
            return 0L;
        }
    }

    private void initializeDatabase(Iterable<Beer> beers) {
        for(Beer beer: beers){
            //todo: merge instead of insert
            fBeerDatabase.insertBeer(beer);
            publishProgress(beer);
        }
    }
}
