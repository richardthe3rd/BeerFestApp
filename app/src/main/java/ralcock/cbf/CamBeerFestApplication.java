package ralcock.cbf;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.Beers;
import ralcock.cbf.service.UpdateService;
import ralcock.cbf.service.UpdateTask;
import ralcock.cbf.util.ExceptionReporter;
import ralcock.cbf.view.AboutDialogFragment;
import ralcock.cbf.view.BeerListFragmentPagerAdapter;
import ralcock.cbf.view.FilterByStyleDialogFragment;
import ralcock.cbf.view.ListChangedListener;
import ralcock.cbf.view.SortByDialogFragment;

// TODO: Migrate from deprecated android.app.DialogFragment to androidx.fragment.app.DialogFragment
// android.app.DialogFragment was deprecated in API 28 (Android 9.0)
// This affects: AboutDialogFragment, SortByDialogFragment, FilterByStyleDialogFragment
@SuppressWarnings("deprecation")
public class CamBeerFestApplication extends AppCompatActivity {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private final ExceptionReporter fExceptionReporter;

    private final AppPreferences fAppPreferences;

    private BeerDatabaseHelper fDBHelper;

    private final List<ListChangedListener> fListChangedListeners =
            new CopyOnWriteArrayList<ListChangedListener>();

    // TODO: Migrate from deprecated LocalBroadcastManager to LiveData or other alternatives
    // LocalBroadcastManager was deprecated in AndroidX 1.1.0
    // Recommended alternatives: LiveData, StateFlow, or direct callbacks
    @SuppressWarnings("deprecation")
    private LocalBroadcastManager fLocalBroadcastManager;

    private BroadcastReceiver fBroadcastReceiver;

    public CamBeerFestApplication() {
        super();
        fAppPreferences = new AppPreferences(this);
        fExceptionReporter = new ExceptionReporter(this);
    }

    /** Called when the activity is first created. */
    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.beer_listview_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setLogo(R.drawable.ic_caskman);

        // Reset search (TODO: better way of doing this!)
        filterBy("");

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(
                new BeerListFragmentPagerAdapter(
                        getSupportFragmentManager(), CamBeerFestApplication.this));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (savedInstanceState != null) {
            int selectedTab = savedInstanceState.getInt("selected.navigation.index");
            Log.i(TAG, "restoring tab " + selectedTab);
            viewPager.setCurrentItem(selectedTab);
        }

        fLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        fBroadcastReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(final Context context, final Intent intent) {
                        if (intent.getAction().equals(UpdateService.UPDATE_SERVICE_RESULT)) {
                            Log.i(TAG, "Received " + UpdateService.UPDATE_SERVICE_RESULT);
                            UpdateTask.Result result =
                                    (UpdateTask.Result)
                                            intent.getSerializableExtra(UpdateService.RESULT_EXTRA);
                            doReceivedUpdateServiceResult(result);
                        }
                    }
                };
    }

    private void doReceivedUpdateServiceResult(final UpdateTask.Result result) {
        if (result.success()) {
            // Updated
            fAppPreferences.setLastUpdateMD5(result.getDigest());
            fAppPreferences.setNextUpdateTime(calcNextUpdateTime());
            notifyBeersChanged();
        } else {
            // Failed - notify of failure.
            Toast.makeText(this, result.getThrowable().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Date calcNextUpdateTime() {
        Date now = new Date();
        // Can't use TimeUnit.Day on older Androids.
        long one_day = 4 * 60 * 60 * TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
        return new Date(now.getTime() + one_day);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "In onStart");
        super.onStart();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onResume() {
        Log.d(TAG, "In onResume");
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UpdateService.UPDATE_SERVICE_PROGRESS);
        filter.addAction(UpdateService.UPDATE_SERVICE_RESULT);
        fLocalBroadcastManager.registerReceiver(fBroadcastReceiver, filter);

        // Start the update service
        startService(new Intent(this, UpdateService.class));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onPause() {
        Log.d(TAG, "In onPause");
        fLocalBroadcastManager.unregisterReceiver(fBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "In onDestroy");
        super.onDestroy();
        if (fDBHelper != null) {
            OpenHelperManager.releaseHelper();
        }
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
            // TODO: Expand SearchView
        }
        return super.onKeyUp(keyCode, event);
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this, BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    private Beers getBeerDao() {
        return getHelper().getBeers();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        int selectedTab = viewPager.getCurrentItem();
        Log.i(TAG, "onSaveInstanceState saving " + selectedTab);
        outState.putInt("selected.navigation.index", selectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnSearchClickListener(
                new View.OnClickListener() {
                    public void onClick(final View view) {
                        searchView.setQueryHint(getResources().getString(R.string.filter_hint));
                    }
                });

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    public boolean onQueryTextSubmit(final String query) {
                        filterBy(query);
                        searchView.clearFocus();
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
        int itemId = item.getItemId();
        if (itemId == R.id.sort) {
            showSortByDialog();
            return true;
        } else if (itemId == R.id.showOnlyStyle) {
            showFilterByStyleDialog();
            return true;
        } else if (itemId == R.id.hideUnavailable) {
            return true;
        } else if (itemId == R.id.visitFestivalWebsite) {
            visitFestivalWebsite();
            return true;
        } else if (itemId == R.id.aboutApplication) {
            showAboutDialog();
            return true;
        } else if (itemId == R.id.export) {
            // doExport();
            return true;
        } else if (itemId == R.id.refreshDatabase) {
            // Start the update service
            startService(new Intent(this, UpdateService.class));
            return true;
        } else if (itemId == R.id.reloadDatabase) {
            // Start the update service with the CLEAN_UPDATE flag
            final Intent intent = new Intent(this, UpdateService.class);
            intent.putExtra(UpdateService.CLEAN_UPDATE, true);
            startService(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        String versionName = "UNKNOWN";
        String appName = getString(R.string.app_name);
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
        DialogFragment newFragment = AboutDialogFragment.newInstance(appName, versionName);
        newFragment.show(getFragmentManager(), "about");
    }

    // Copied from
    // http://developer.android.com/reference/android/app/DialogFragment.html
    private void showSortByDialog() {
        DialogFragment newFragment =
                SortByDialogFragment.newInstance(fAppPreferences.getSortOrder());
        newFragment.show(getFragmentManager(), "sortBy");
    }

    private void showFilterByStyleDialog() {
        final Set<String> allStyles = getBeerDao().getAvailableStyles();
        final Set<String> stylesToHide = fAppPreferences.getStylesToHide();
        final DialogFragment newFragment =
                FilterByStyleDialogFragment.newInstance(stylesToHide, allStyles);
        newFragment.show(getFragmentManager(), "filterByStyle");
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
        fireBeerListChanged();
    }

    private void visitFestivalWebsite() {
        Uri festivalUri = Uri.parse(getString(R.string.festival_website_url));
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, festivalUri);
        startActivity(launchBrowser);
    }

    // Called when sort dialog is closed.
    public void doDismissSortDialog(final SortOrder sortOrder) {
        sortBy(sortOrder);
    }

    public void doDismissFilterByStyleDialog(final Set<String> stylesToHide) {
        filterByBeerStyle(stylesToHide);
    }

    private void sortBy(SortOrder sortOrder) {
        fireSortByChanged(sortOrder);
        fAppPreferences.setSortOrder(sortOrder);
    }

    private void filterByBeerStyle(Set<String> stylesToHide) {
        fireStylesToHideChanged(stylesToHide);
        fAppPreferences.setStylesToHide(stylesToHide);
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

    private void fireStylesToHideChanged(final Set<String> stylesToHide) {
        for (ListChangedListener l : fListChangedListeners) {
            l.stylesToHideChanged(stylesToHide);
        }
    }

    private void fireBeerListChanged() {
        for (ListChangedListener l : fListChangedListeners) {
            l.beersChanged();
        }
    }
}
