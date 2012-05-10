package ralcock.cbf;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import ralcock.cbf.model.JsonBeerList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

class LoadBeersTask extends AsyncTask<LoadBeersTask.Source, String, JsonBeerList> {

    private static final String TAG = "cbf." + LoadBeersTask.class.getSimpleName();

    private final ProgressDialog fDialog;
    private final UpdateBeersTask fUpdateBeersTask;
    private long fStartTime;

    LoadBeersTask(final ProgressDialog progressDialog,
                  final UpdateBeersTask updateBeersTask) {
        fDialog = progressDialog;
        fUpdateBeersTask = updateBeersTask;
    }

    @Override
    protected void onPreExecute() {
        fStartTime = System.currentTimeMillis();
        fDialog.setMessage(fDialog.getContext().getText(R.string.loading_message));
        fDialog.setIndeterminate(true);
        fDialog.setCancelable(false);
        fDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... msg) {
        fDialog.setMessage(msg[0]);
    }

    @Override
    protected void onPostExecute(JsonBeerList beerList) {
        final long time = (System.currentTimeMillis() - fStartTime) / 1000;

        final String message = "Downloaded " + beerList.size() + " beers in " + time + " seconds.";
        fDialog.setMessage(message);
        Log.i(TAG, message);

        fDialog.dismiss();

        fUpdateBeersTask.setNumberOfBeers(beerList.size());
        fUpdateBeersTask.execute(beerList);
    }

    @Override
    protected JsonBeerList doInBackground(Source... sources) {
        final Source source = sources[0];
        try {
            Log.i(TAG, "Starting background initialization of database");

            final InputStream inputStream = source.URL.openStream();
            final String jsonString = readStream(inputStream);
            return new JsonBeerList(jsonString);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readStream(final InputStream inputStream) throws IOException, JSONException {
        Log.i(TAG, "Loading beer list from input stream.");
        Reader reader = new InputStreamReader(inputStream);
        StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[0x10000];
        int read;
        do {
            read = reader.read(buffer);
            if (read > 0) {
                builder.append(buffer, 0, read);
            }
        } while (read >= 0);

        return builder.toString();
    }

    static class Source {
        final URL URL;

        Source(URL url) {
            URL = url;
        }
    }
}
