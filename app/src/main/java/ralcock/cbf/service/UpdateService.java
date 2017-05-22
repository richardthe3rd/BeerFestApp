package ralcock.cbf.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import ralcock.cbf.AppPreferences;
import ralcock.cbf.CamBeerFestApplication;
import ralcock.cbf.R;
import ralcock.cbf.model.BeerDatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


public class UpdateService extends OrmLiteBaseService<BeerDatabaseHelper> {
    private static final String TAG = UpdateService.class.getName();

    public static final String UPDATE_SERVICE_PROGRESS = "UpdateService.Progress";
    public static final String UPDATE_SERVICE_RESULT = "UpdateService.Result";

    public static final String PROGRESS_EXTRA = "progress";
    public static final String RESULT_EXTRA = "result";
    public static final String CLEAN_UPDATE = "cleanUpdate";

    private final AppPreferences fAppPreferences;

    private LocalBroadcastManager fLocalBroadcastManager;

    private NotificationManager fNotifyManager;
    private NotificationCompat.Builder fBuilder;

    private int fNotificationID = 0;

    private long getBeerCount() {
        return getHelper().getBeers().getNumberOfBeers();
    }

    private NetworkInfo[] getAllNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getAllNetworkInfo();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        NetworkInfo[] info = getAllNetworkInfo();
        for (NetworkInfo ni : info) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                    break;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    haveConnectedMobile = true;
                    break;
                 }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public UpdateService() {
        fAppPreferences = new AppPreferences(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(TAG, "onStartCommand");

        fNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        fBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_caskman)
                .setContentTitle(getString(R.string.update_notification_title))
                .setContentText(getString(R.string.update_in_progress_notification_text));

        // See http://developer.android.com/guide/topics/ui/notifiers/notifications.html#SimpleNotification
        Intent resultIntent = new Intent(this, CamBeerFestApplication.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(CamBeerFestApplication.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        fBuilder.setContentIntent(pendingIntent);

        doUpdate(intent.getBooleanExtra(CLEAN_UPDATE, false));
        return START_NOT_STICKY;
    }

    private void doUpdate(final boolean cleanUpdate) {
        Log.d(TAG, "doUpdate: cleanUpdate=" + cleanUpdate);

        if (!haveNetworkConnection()) {
            Log.i(TAG, "No network connection - not updating.");
            Toast.makeText(this, "No network connection - not updating.", Toast.LENGTH_LONG).show();
            return;
        }

        UpdateTask task = new UpdateTask() {
            @Override
            protected void onProgressUpdate(final Progress... values) {
                Intent broadcastIntent = new Intent(UPDATE_SERVICE_PROGRESS);
                broadcastIntent.putExtra(PROGRESS_EXTRA, values[0]);
                fLocalBroadcastManager.sendBroadcast(broadcastIntent);

                updateProgress(values[0].getProgress(), values[0].getTotal());
            }

            @Override
            protected void onPostExecute(final Result result) {
                Intent broadcastIntent = new Intent(UPDATE_SERVICE_RESULT);
                broadcastIntent.putExtra(RESULT_EXTRA, result);
                fLocalBroadcastManager.sendBroadcast(broadcastIntent);

                fNotificationID = 0;
                if (result.getCount() == 0) {
                    fNotifyManager.cancel(fNotificationID);
                } else {
                    fBuilder.setProgress(fNotificationID, 0, false);
                    fBuilder.setContentText(getString(R.string.update_complete_notification_text, result.getCount()));
                    fNotifyManager.notify(fNotificationID, fBuilder.build());
                }

                // We're done. Stop the service.
                Log.d(TAG, "Stopping UpdateService");
                stopSelf();
            }
        };

        UpdateTask.Params p = new UpdateTask.Params() {
            @Override
            MessageDigest getDigest() throws NoSuchAlgorithmException {
                return MessageDigest.getInstance("MD5");
            }

            @Override
            InputStream openStream() throws IOException {
                String beerJsonURL = getString(R.string.beer_list_url);
                URL url = new URL(beerJsonURL);
                return url.openStream();
            }

            @Override
            BeerDatabaseHelper getDatabaseHelper() {
                return getHelper();
            }

            @Override
            boolean cleanUpdate() {
                return cleanUpdate;
            }

            @Override
            boolean needsUpdate(final byte[] digest) {
                String hashString = toMD5String(digest);
                final String lastUpdateMD5 = fAppPreferences.getLastUpdateMD5();
                Log.d(TAG, "Previous hash was " + lastUpdateMD5 + " new hash is " + hashString);
                return !hashString.equals(lastUpdateMD5);
            }

            @Override
            boolean updateDue() {
                Date nextUpdate = fAppPreferences.getNextUpdateTime();
                Log.i(TAG, "Beer update due after " + nextUpdate);
                Date currentTime = new Date();
                return getBeerCount() == 0 || currentTime.after(nextUpdate);
            }
        };
        task.execute(p);
    }

    private static String toMD5String(final byte[] digest) {
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }

    private void updateProgress(final int progress, final int max) {
        fBuilder.setProgress(max, progress, false);
        fNotifyManager.notify(fNotificationID, fBuilder.build());
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        fLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
