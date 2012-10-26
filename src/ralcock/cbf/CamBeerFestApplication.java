package ralcock.cbf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.actions.BeerExporter;
import ralcock.cbf.actions.BeerSearcher;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.JsonBeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.util.ExceptionReporter;
import ralcock.cbf.view.AllBeersListFragment;
import ralcock.cbf.view.AvailableBeersListFragment;
import ralcock.cbf.view.ListChangedListener;
import ralcock.cbf.view.SortByDialogFragment;
import ralcock.cbf.view.TabListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CamBeerFestApplication extends SherlockFragmentActivity
        implements LoadTaskListener, UpdateTaskListener {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private static final int SORT_DIALOG_ID = 0;
    private static final int FILTER_BY_STYLE_DIALOG_ID = 1;
    private static final int LOAD_TASK_PROGRESS_DIALOG_ID = 3;
    private static final int UPDATE_TASK_PROGRESS_DIALOG_ID = 4;
    private static final int ABOUT_DIALOG_ID = 5;

    private final BeerSharer fBeerSharer;
    private final BeerSearcher fBeerSearcher;
    private final ExceptionReporter fExceptionReporter;

    private final AppPreferences fAppPreferences;
    private UpdateBeersTask fUpdateBeersTask;
    private LoadBeersTask fLoadBeersTask;

    private ProgressDialog fLoadProgressDialog;
    private ProgressDialog fUpdateProgressDialog;

    private BeerDatabaseHelper fDBHelper;

    private final List<ListChangedListener> fListChangedListeners = new CopyOnWriteArrayList<ListChangedListener>();

    public CamBeerFestApplication() {
        super();
        fAppPreferences = new AppPreferences(this);
        fBeerSharer = new BeerSharer(this);
        fBeerSearcher = new BeerSearcher(this);
        fExceptionReporter = new ExceptionReporter(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "In onDestroy");
        if (fLoadBeersTask != null) {
            fLoadBeersTask.setListener(null);
        }

        if (fLoadProgressDialog != null && fLoadProgressDialog.isShowing()) {
            dismissDialog(LOAD_TASK_PROGRESS_DIALOG_ID);
        }

        if (fUpdateBeersTask != null)
            fUpdateBeersTask.setListener(null);


        if (fUpdateProgressDialog != null && fUpdateProgressDialog.isShowing()) {
            dismissDialog(UPDATE_TASK_PROGRESS_DIALOG_ID);
        }

        super.onDestroy();
        if (fDBHelper != null) {
            OpenHelperManager.releaseHelper();
        }
    }

    //@Override
    //public Object onRetainNonConfigurationInstance() {
    //    Log.d(TAG, "onRetainNonConfigurationInstance");
    //
    //    return new Tasks(fUpdateBeersTask, fLoadBeersTask);
    //}

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate");

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();

        ActionBar.Tab allBeersTab = actionBar.newTab()
                .setText("All Beers")
                .setTabListener(new TabListener<AllBeersListFragment>(this, "all", AllBeersListFragment.class));
        ActionBar.Tab availableBeersTab = actionBar.newTab()
                .setText("Available Only")
                .setTabListener(new TabListener<AvailableBeersListFragment>(this, "available", AvailableBeersListFragment.class));

        actionBar.setTitle(fAppPreferences.getFilterText());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(allBeersTab);
        actionBar.addTab(availableBeersTab);

        if (savedInstanceState != null) {
            int selectedTab = savedInstanceState.getInt("selected.navigation.index");
            actionBar.setSelectedNavigationItem(selectedTab);
        }

        Object cfgInstance = getLastNonConfigurationInstance();
        if (cfgInstance instanceof Tasks) {
            Tasks tasks = (Tasks) cfgInstance;
            fLoadBeersTask = tasks.getLoadBeersTask();
            if (fLoadBeersTask != null &&
                    fLoadBeersTask.getStatus() == AsyncTask.Status.RUNNING) {
                // We have a running load task
                fLoadBeersTask.setListener(this);
                //showDialog(LOAD_TASK_PROGRESS_DIALOG_ID);
            }

            fUpdateBeersTask = tasks.getUpdateBeersTask();
            if (fUpdateBeersTask != null &&
                    fUpdateBeersTask.getStatus() == AsyncTask.Status.RUNNING) {
                // We have a running update task
                fUpdateBeersTask.setListener(this);
            }
        } else {
            fLoadBeersTask = null;
            fUpdateBeersTask = null;
            if (beerUpdateNeeded()) {
                loadBeersInBackground();
            }
        }
    }

    private long getBeerCount() {
        try {
            return getBeerDao().getNumberOfBeers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
            // TODO: Expand SearchView
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean beerUpdateNeeded() {
        Date nextUpdate = fAppPreferences.getNextUpdateTime();
        Log.i(TAG, "Beer update due after " + nextUpdate);
        Date currentTime = new Date();
        return getBeerCount() == 0 || currentTime.after(nextUpdate);
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this, BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    private BeerDao getBeerDao() {
        return getHelper().getBeerDao();
    }

    private URL beerListUrl() {
        String beerJsonURL = getString(R.string.beer_list_url);
        try {
            return new URL(beerJsonURL);
        } catch (MalformedURLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
            // My fault
            throw new RuntimeException(e);
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
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

    private void loadBeersInBackground() {

        if (fLoadBeersTask != null && fLoadBeersTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(TAG, "LoadBeerTask is already running.");
            return;
        }

        if (!haveNetworkConnection()) {
            Toast.makeText(this, getString(R.string.NoInternetConnection), Toast.LENGTH_LONG).show();
            return;
        }

        String md5 = fAppPreferences.getLastUpdateMD5();
        if (getBeerCount() == 0) {
            md5 = "EMPTY_DATABASE";
        }
        final LoadBeersTask.Source source = new LoadBeersTask.Source(beerListUrl(), md5);

        //noinspection unchecked
        fLoadBeersTask = new LoadBeersTask(fAppPreferences);
        fLoadBeersTask.setListener(this);
        fLoadBeersTask.execute(source);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        int selectedTab = getSupportActionBar().getSelectedNavigationIndex();
        outState.putInt("selected.navigation.index", selectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                searchView.setQueryHint(getResources().getString(R.string.filter_hint));
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(final String query) {
                filterBy(query.toString());
                return true;
            }

            public boolean onQueryTextChange(final String newText) {
                filterBy(newText.toString());
                return true;
            }
        });
        return true;
    }

    void filterBy(String filterText) {
        fireFilterTextChanged(filterText);
        fAppPreferences.setFilterText(filterText);
        getSupportActionBar().setTitle(filterText);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                showSortByDialog();
                return true;
            case R.id.showOnlyStyle:
                //showDialog(FILTER_BY_STYLE_DIALOG_ID);
                return true;
            case R.id.export:
                //doExport();
                return true;
            case R.id.refreshDatabase:
                //loadBeersInBackground();
                return true;
            case R.id.reloadDatabase:
                //doReloadDatabase();
                return true;
            case R.id.visitFestivalWebsite:
                //goToFestivalWebsite();
                return true;
            case R.id.aboutApplication:
                //showDialog(ABOUT_DIALOG_ID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Copied from http://developer.android.com/reference/android/app/DialogFragment.html
    private void showSortByDialog() {
        DialogFragment newFragment = SortByDialogFragment.newInstance(fAppPreferences.getSortOrder());
        newFragment.show(getSupportFragmentManager(), "sortBy");
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.searchBeer:
                    fBeerSearcher.searchBeer(getBeerFromMenuItem(item));
                    return true;
                case R.id.shareBeer:
                    fBeerSharer.shareBeer(getBeerFromMenuItem(item));
                    return true;
            }
            return super.onMenuItemSelected(featureId, item);
        } catch (SQLException e) {
            // getBeerFromMenuItem failed
            fExceptionReporter.report(TAG, e.getMessage(), e);
            return true;
        }
    }

    // Helper for onMenuItemSelected
    private Beer getBeerFromMenuItem(final MenuItem item) throws SQLException {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        return getBeerDao().getBeerWithId(info.id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_BEER_DETAILS_REQUEST_CODE) {
            notifyBeersChanged();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void notifyBeersChanged() {
        /*
        try {
            fBeerList.updateBeerList();
            notifyAdapterBeersChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
        */
    }

    private void doExport() {
        try {
            List<Beer> ratedBeers = getBeerDao().getRatedBeers();
            BeerExporter exporter = new BeerExporter(this);
            exporter.export(ratedBeers);
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        } catch (IOException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private void goToFestivalWebsite() {
        Uri festivalUri = Uri.parse(getString(R.string.festival_website_url));
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, festivalUri);
        startActivity(launchBrowser);
    }

    private void doReloadDatabase() {
        // delete all beers
        getHelper().deleteAll();
        fAppPreferences.setLastUpdateMD5("");
        loadBeersInBackground();
    }

    /*
    @Override
    protected Dialog onCreateDialog(final int id) {
        Dialog dialog;
        switch (id) {
            case SORT_DIALOG_ID:
                dialog = createSortDialog();
                break;
            case FILTER_BY_STYLE_DIALOG_ID:
                dialog = createStylesToHideDialog();
                break;
            case FILTER_BY_AVAILABLE_DIALOG_ID:
                dialog = createAvailabilityDialog();
                break;
            case LOAD_TASK_PROGRESS_DIALOG_ID:
                Log.d(TAG, "Creating LOAD_TASK_PROGRESS_DIALOG");
                fLoadProgressDialog = createLoadProgressDialog();
                dialog = fLoadProgressDialog;
                break;
            case UPDATE_TASK_PROGRESS_DIALOG_ID:
                Log.d(TAG, "Creating UPDATE_TASK_PROGRESS_DIALOG");
                fUpdateProgressDialog = createUpdateProgressDialog();
                dialog = fUpdateProgressDialog;
                break;
            case ABOUT_DIALOG_ID:
                String versionName = "UNKNOWN";
                String appName = getString(R.string.app_name);
                try {
                    final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    versionName = packageInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    fExceptionReporter.report(TAG, e.getMessage(), e);
                }
                dialog = new AlertDialog.Builder(this)
                        .setMessage(appName + "\n" +
                                "Version: " + versionName)
                        .create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    private ProgressDialog createUpdateProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.updating_database));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private ProgressDialog createLoadProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getText(R.string.loading_message));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
        switch (id) {
            case SORT_DIALOG_ID:
                break;
            case FILTER_BY_STYLE_DIALOG_ID:
                prepareStylesToHideDialog(dialog);
                break;
            case FILTER_BY_AVAILABLE_DIALOG_ID:
                break;
        }
    }

    private Dialog createStylesToHideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter_style_dialog_title);

        final Set<String> emptySet = Collections.emptySet();
        final BeerStyleListAdapter listAdapter = new BeerStyleListAdapter(this, emptySet, emptySet);
        builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                // NOTHING TO DO HERE
            }
        });

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                AlertDialog alertDialog = (AlertDialog) dialogInterface;
                BeerStyleListAdapter listAdapter = (BeerStyleListAdapter) alertDialog.getListView().getAdapter();
                filterByStyle(listAdapter.getStylesToHide());
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
            }
        });

        return builder.create();
    }

    private void prepareStylesToHideDialog(final Dialog dialog) {
        try {
            final Set<String> stylesToHide = fAppPreferences.getStylesToHide();
            final Set<String> allStyles = getBeerDao().getAvailableStyles();
            final BeerStyleListAdapter listAdapter = new BeerStyleListAdapter(this, allStyles, stylesToHide);
            AlertDialog alertDialog = (AlertDialog) dialog;
            alertDialog.getListView().setAdapter(listAdapter);
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private void filterByStyle(Set<String> stylesToHide) {
        try {
            fBeerList.stylesToHide(stylesToHide);
            fAppPreferences.setStylesToHide(stylesToHide);
            notifyAdapterBeersChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private void notifyAdapterBeersChanged() {
        fAdapter.notifyDataSetChanged();
    }

    */
    public void notifyLoadTaskStarted() {
        showDialog(LOAD_TASK_PROGRESS_DIALOG_ID);
    }

    public void notifyLoadTaskUpdate(final String[] values) {
        fLoadProgressDialog.setMessage(values[0]);
    }

    public void notifyLoadTaskComplete(final LoadBeersTask.Result result) {
        JsonBeerList beerList = result.BeerList;
        if (beerList == null) {
            Throwable t = result.Throwable;
            if (t != null) {
                fExceptionReporter.report(TAG, "Failed to download beers. " + t.getMessage(), t);
            }
            dismissDialogNoThrow(LOAD_TASK_PROGRESS_DIALOG_ID);
        } else {
            dismissDialogNoThrow(LOAD_TASK_PROGRESS_DIALOG_ID);

            if (fUpdateBeersTask != null && fUpdateBeersTask.getStatus() == AsyncTask.Status.RUNNING) {
                Log.d(TAG, "UpdateBeerTask is already running.");
                return;
            }
            fUpdateBeersTask = new UpdateBeersTask(getApplicationContext(), fExceptionReporter);
            fUpdateBeersTask.setListener(this);
            fUpdateBeersTask.setNumberOfBeers(beerList.size());
            fUpdateBeersTask.execute(beerList);
        }
    }

    public void notifyUpdateStarted(final int max) {
        showDialog(UPDATE_TASK_PROGRESS_DIALOG_ID);
        fUpdateProgressDialog.setProgress(0);
        fUpdateProgressDialog.setMax(max);
    }

    public void notifyUpdateComplete(final Long aLong) {
        notifyBeersChanged();
        dismissDialogNoThrow(UPDATE_TASK_PROGRESS_DIALOG_ID);
    }

    public void notifyUpdateProgress(final Beer[] values) {
        String name = values[0].getName();
        int maxLength = 12;
        if (name.length() > maxLength) {
            name = name.substring(0, maxLength - 3);
            name = name + "...";
        }
        fUpdateProgressDialog.setMessage("Updated " + name);
        fUpdateProgressDialog.incrementProgressBy(1);
    }

    // HACK!!!
    private void dismissDialogNoThrow(int dialogId) {
        try {
            dismissDialog(dialogId);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to dismiss dialog " + dialogId, e);
            // ignore
        }
    }

    // Called when sort dialog is closed.
    public void doDismissSortDialog(final SortOrder sortOrder) {
        sortBy(sortOrder);
    }

    private void sortBy(SortOrder sortOrder) {
        fireSortByChanged(sortOrder);
        fAppPreferences.setSortOrder(sortOrder);
    }

    public void addListChangedListener(final ListChangedListener listChangedListener) {
        fListChangedListeners.add(listChangedListener);
    }

    public void removeListChangedListener(final ListChangedListener listChangedListener) {
        fListChangedListeners.remove(listChangedListener);
    }

    private void fireFilterTextChanged(final String filterText) {
        for (ListChangedListener l : fListChangedListeners) {
            l.filterTextChanged(filterText);
        }
    }

    private void fireSortByChanged(final SortOrder sortOrder) {
        for (ListChangedListener l : fListChangedListeners) {
            l.sortOrderChanged(sortOrder);
        }
    }

    private static class Tasks {
        private final UpdateBeersTask fUpdateBeersTask;
        private final LoadBeersTask fLoadBeersTask;

        Tasks(final UpdateBeersTask updateBeersTask,
              final LoadBeersTask loadBeersTask) {
            fUpdateBeersTask = updateBeersTask;
            fLoadBeersTask = loadBeersTask;
        }

        UpdateBeersTask getUpdateBeersTask() {
            return fUpdateBeersTask;
        }

        LoadBeersTask getLoadBeersTask() {
            return fLoadBeersTask;
        }
    }
}