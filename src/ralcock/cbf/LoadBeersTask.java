package ralcock.cbf;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import ralcock.cbf.model.JsonBeerList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

final class LoadBeersTask extends AsyncTask<LoadBeersTask.Source, String, LoadBeersTask.Result> {

    private static final String TAG = "cbf." + LoadBeersTask.class.getSimpleName();

    private final Context fContext;
    private final ProgressDialog fDialog;
    private final AppPreferences fAppPreferences;
    private final UpdateBeersTask fUpdateBeersTask;

    LoadBeersTask(final Context context,
                  final UpdateBeersTask updateBeersTask,
                  final AppPreferences appPreferences) {
        fContext = context;
        fDialog = new ProgressDialog(context);
        fUpdateBeersTask = updateBeersTask;
        fAppPreferences = appPreferences;
    }

    @Override
    protected void onPreExecute() {
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
    protected void onPostExecute(Result result) {
        JsonBeerList beerList = result.BeerList;
        if (beerList == null) {
            Throwable t = result.Throwable;
            if (t != null) {
                Log.e(TAG, "Exception while downloading beer list. " + t.getMessage(), t);
                Toast.makeText(fContext, "Failed to download beers. " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            fDialog.dismiss();
        } else {
            Toast.makeText(fContext, "Updated beer list", Toast.LENGTH_LONG).show();
            fDialog.dismiss();

            fUpdateBeersTask.setNumberOfBeers(beerList.size());
            fUpdateBeersTask.execute(beerList);
        }
    }

    @Override
    protected Result doInBackground(Source... sources) {
        final Source source = sources[0];
        try {
            Log.i(TAG, "Starting background initialization of database");

            MessageDigest digest = MessageDigest.getInstance("MD5");
            final InputStream inputStream = source.URL.openStream();
            DigestInputStream digestStream = new DigestInputStream(inputStream, digest);
            final String jsonString = readStream(digestStream);
            String md5 = toHashText(digest);
            if (md5.equals(source.MD5)) {
                Log.i(TAG, "MD5 streams match - nothing has changed, not parsing JSON or updating database.");
                setNextUpdateTime();
                return new Result();
            } else {
                Log.i(TAG, "Previous MD5 was " + source.MD5 + ", new MD5 is " + md5);
                fAppPreferences.setLastUpdateMD5(md5);
                setNextUpdateTime();
                return new Result(new JsonBeerList(jsonString));
            }

        } catch (JSONException e) {
            return new Result(e);
        } catch (IOException e) {
            return new Result(e);
        } catch (NoSuchAlgorithmException e) {
            return new Result(e);
        }
    }

    private void setNextUpdateTime() {
        // 3 hours time
        int hoursToNextUpdate = 3;
        final Date nextUpdateTime = new Date(System.currentTimeMillis() + (hoursToNextUpdate * 60 * 60 * 1000));
        Log.i(TAG, "Will next check for updates after " + nextUpdateTime);
        fAppPreferences.setNextUpdateTime(nextUpdateTime);
    }

    private String toHashText(final MessageDigest digest) {
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
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
        final String MD5;

        Source(URL url, final String md5) {
            URL = url;
            MD5 = md5;
        }
    }

    static class Result {
        final JsonBeerList BeerList;
        final Throwable Throwable;

        private Result() {
            BeerList = null;
            Throwable = null;
        }

        private Result(final JsonBeerList beerList) {
            BeerList = beerList;
            Throwable = null;
        }

        public Result(final Throwable throwable) {
            BeerList = null;
            Throwable = throwable;
        }
    }
}
