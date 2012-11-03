package ralcock.cbf.service;

import android.os.AsyncTask;
import android.util.Log;
import com.j256.ormlite.misc.TransactionManager;
import org.json.JSONException;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.JsonBeerList;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class UpdateTask extends AsyncTask<UpdateTask.Params, UpdateTask.Progress, UpdateTask.Result> {

    private static final String TAG = UpdateTask.class.getName();

    @Override
    protected Result doInBackground(final Params... params) {
        final Params param0 = params[0];

        if (!param0.cleanUpdate() && !param0.updateDue()) {
            return new NoUpdateRequiredResult();
        }

        byte[] digest;
        String jsonString;

        try {
            InputStream inputStream = param0.openStream();
            MessageDigest msgDigest = param0.getDigest();
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, msgDigest);
            jsonString = readEntireStream(digestInputStream);
            digest = msgDigest.digest();
        } catch (IOException iox) {
            return new FailedUpdateResult(iox);
        } catch (NoSuchAlgorithmException nsax) {
            return new FailedUpdateResult(nsax);
        }

        if (param0.cleanUpdate() || param0.needsUpdate(digest)) {
            Log.d(TAG, "Beer list has changed, updating.");
            // Update from JSON
            try {
                final JsonBeerList beerList = new JsonBeerList(jsonString);
                final BeerDatabaseHelper helper = param0.getDatabaseHelper();
                int count = TransactionManager.callInTransaction(helper.getConnectionSource(),
                        new Callable<Integer>() {
                            public Integer call() throws Exception {
                                if (param0.cleanUpdate()) {
                                    helper.deleteAll();
                                }
                                return initializeDatabase(beerList,
                                        helper.getBreweryDao(), helper.getBeerDao());
                            }
                        });
                Log.d(TAG, "Updated " + count + " beers.");
                return new UpdateResult(count, toMD5String(digest));
            } catch (JSONException e) {
                return new FailedUpdateResult(e);
            } catch (SQLException e) {
                return new FailedUpdateResult(e);
            }
        } else {
            // Nothing has changed.
            Log.d(TAG, "Beer list has not changed, not updating.");
            return new NoUpdateRequiredResult();
        }
    }

    private static String toMD5String(final byte[] digest) {
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }

    private static String readEntireStream(final InputStream inputStream) throws IOException {
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

    private int initializeDatabase(JsonBeerList beers,
                                   BreweryDao breweryDao,
                                   BeerDao beerDao) throws SQLException {
        final int size = beers.size();
        int count = 0;
        for (Beer beer : beers) {
            final Brewery brewery = beer.getBrewery();
            if (brewery.getId() == 0) {
                breweryDao.updateFromFestivalOrCreate(brewery);
            }

            if (beer.getId() == 0) {
                beerDao.updateFromFestivalOrCreate(beer);
            }

            count++;
            publishProgress(new Progress(count, size, beer));

        }
        return count;
    }

    public static abstract class Params {
        abstract MessageDigest getDigest() throws NoSuchAlgorithmException;

        abstract InputStream openStream() throws IOException;

        abstract BeerDatabaseHelper getDatabaseHelper();

        abstract boolean cleanUpdate();

        abstract boolean needsUpdate(final byte[] digest);

        abstract boolean updateDue();
    }

    public final static class Progress implements Serializable {
        private final int fCount;
        private final int fSize;
        private final Beer fBeer;

        public int getProgress() {
            return fCount;
        }

        public int getTotal() {
            return fSize;
        }

        public Progress(final int count, final int size, final Beer beer) {
            fCount = count;
            fSize = size;
            fBeer = beer;
        }

        public Beer getBeer() {
            return fBeer;
        }
    }

    public abstract class Result implements Serializable {
        public boolean success() {
            return true;
        }

        public Throwable getThrowable() {
            return null;
        }

        public int getCount() {
            return 0;
        }

        public String getDigest() {
            return "";
        }
    }

    public class NoUpdateRequiredResult extends Result {

    }

    public class UpdateResult extends Result {

        private final int fCount;
        private final String fDigest;

        public UpdateResult(final int count, final String digest) {
            fCount = count;
            fDigest = digest;
        }

        @Override
        public String getDigest() {
            return fDigest;
        }

        @Override
        public int getCount() {
            return fCount;
        }
    }

    private class FailedUpdateResult extends Result {
        private Throwable fThrowable;

        public FailedUpdateResult(final Throwable t) {
            fThrowable = t;
        }

        @Override
        public Throwable getThrowable() {
            return fThrowable;
        }

        @Override
        public boolean success() {
            return false;
        }

    }
}
