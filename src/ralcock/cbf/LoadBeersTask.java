package ralcock.cbf;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TimingLogger;
import org.json.JSONException;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabase;
import ralcock.cbf.model.JsonBeerList;
import ralcock.cbf.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

class LoadBeersTask extends AsyncTask<String, Beer, Long> {

    private static final String TAG = "cbf."+LoadBeersTask.class.getSimpleName();

    private final ProgressDialog fDialog;
    private final CamBeerFestApplication fApplication;
    private final BeerDatabase fBeerDatabase;

    LoadBeersTask(CamBeerFestApplication application, BeerDatabase beerDatabase) {
        fApplication = application;
        fBeerDatabase = beerDatabase;
        fDialog = new ProgressDialog(fApplication);
        fDialog.setMessage(fApplication.getResources().getText(R.string.loading_message));
        fDialog.setIndeterminate(true);
    }

    @Override
    protected void onPreExecute() {
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
        fDialog.setMessage("Loaded " + count + " beers.");
        fApplication.updateCursor();
        fDialog.dismiss();
    }

    @Override
    protected Long doInBackground(String... inputs) {
        if (fBeerDatabase.countBeers()==0) {
            Log.i(TAG, "Starting background initialization of database from " + inputs[0]);
            initializeDatabase(fApplication, inputs[0]);
            final long count = fBeerDatabase.countBeers();
            Log.i(TAG, "Finished background initialization of database. Loaded " + count);
            return count;
        } else {
            return 0L;
        }
    }

    private void initializeDatabase(Context context, String input) {
        InputStream inputStream = null;
        try {
            TimingLogger tlogger = new TimingLogger(TAG, "Opening stream");
            inputStream = context.getAssets().open(input);
            tlogger.addSplit("Opened stream");
            for(Beer beer: new JsonBeerList(inputStream)){
                fBeerDatabase.insertBeer(beer);
                publishProgress(beer);
            }
            tlogger.addSplit("Inserted all beers.");
            tlogger.dumpToLog();
        } catch (IOException iox) {
            // Failed
            Log.e(TAG, "Exception while initializing database.", iox);
        } catch (JSONException jx) {
            // Failed
            Log.e(TAG, "Exception while initializing database.", jx);
        } finally {
            IOUtils.safeClose(TAG, inputStream);
        }
    }
}
