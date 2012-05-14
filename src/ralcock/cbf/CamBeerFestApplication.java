package ralcock.cbf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.JsonBeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;
import ralcock.cbf.util.ExceptionReporter;
import ralcock.cbf.view.BeerDetailsActivity;
import ralcock.cbf.view.BeerListAdapter;
import ralcock.cbf.view.BeerSearcher;
import ralcock.cbf.view.BeerSharer;
import ralcock.cbf.view.BeerStyleListAdapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CamBeerFestApplication extends OrmLiteBaseListActivity<BeerDatabaseHelper> {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private static final int SORT_DIALOG_ID = 0;
    private static final int FILTER_BY_STYLE_DIALOG_ID = 1;
    private static final int FILTER_BY_AVAILABLE_DIALOG_ID = 2;

    private BeerListAdapter fAdapter;
    private EditText fFilterTextBox = null;

    private BeerList fBeerList;

    private final BeerSharer fBeerSharer;
    private final BeerSearcher fBeerSearcher;
    private final ExceptionReporter fExceptionReporter;

    private final AppPreferences fAppPreferences;

    private final TextWatcher fFilterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            filterBy(s.toString());
        }
    };

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
        if (fFilterTextBox != null)
            fFilterTextBox.removeTextChangedListener(fFilterTextWatcher);

        super.onDestroy();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.beer_list_view);
        setTitle(getResources().getText(R.string.list_title));

        try {
            fBeerList = new BeerList(getBeerDao(),
                    getBreweryDao(),
                    fAppPreferences.getSortOrder(),
                    fAppPreferences.getFilterText(),
                    fAppPreferences.getStylesToHide(),
                    fAppPreferences.getHideUnavailableBeers());
            fAdapter = new BeerListAdapter(CamBeerFestApplication.this, fBeerList);
            setListAdapter(fAdapter);
            configureListView();
            if (beerUpdateNeeded()) {
                loadBeersInBackground();
            }
            configureFilterTextBox();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
            return;
        }
    }

    private void configureFilterTextBox() {
        fFilterTextBox = (EditText) findViewById(R.id.search);
        fFilterTextBox.setText(fAppPreferences.getFilterText());
        fFilterTextBox.addTextChangedListener(fFilterTextWatcher);

        Button clearFilterButton = (Button) findViewById(R.id.clear_filter_text);
        clearFilterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fFilterTextBox.setText("");
                filterBy("");
            }
        });
    }

    private long getBeerCount() {
        try {
            return getBeerDao().getNumberOfBeers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean beerUpdateNeeded() {
        Date nextUpdate = fAppPreferences.getNextUpdateTime();
        Log.i(TAG, "Beer update due after " + nextUpdate);
        Date currentTime = new Date();
        return getBeerCount() == 0 || currentTime.after(nextUpdate);
    }

    private BreweryDao getBreweryDao() {
        return getHelper().getBreweryDao();
    }

    private BeerDao getBeerDao() {
        return getHelper().getBeerDao();
    }

    private URL beerListUrl() {
        String beerJsonURL = getResources().getText(R.string.beer_list_url).toString();
        try {
            return new URL(beerJsonURL);
        } catch (MalformedURLException e) {
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

        if (!haveNetworkConnection()) {
            Toast.makeText(this,
                    "Not updating beers as there is no internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        final LoadBeersTask loadBeersTask = new MyLoadBeersTask(fAppPreferences);

        String md5 = fAppPreferences.getLastUpdateMD5();
        if (getBeerCount() == 0) {
            md5 = "EMPTY_DATABASE";
        }
        final LoadBeersTask.Source source = new LoadBeersTask.Source(beerListUrl(), md5);

        //noinspection unchecked
        loadBeersTask.execute(source);

    }

    private void configureListView() {
        ListView lv = getListView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CamBeerFestApplication.this, BeerDetailsActivity.class);
                intent.putExtra(BeerDetailsActivity.EXTRA_BEER_ID, id);
                startActivityForResult(intent, SHOW_BEER_DETAILS_REQUEST_CODE);
            }
        });

        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu contextMenu, final View view, final ContextMenu.ContextMenuInfo contextMenuInfo) {
                MenuInflater inflater = new MenuInflater(getApplicationContext());
                inflater.inflate(R.menu.list_context_menu, contextMenu);
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.search_beer:
                    fBeerSearcher.searchBeer(getBeerFromMenuItem(item));
                    return true;
                case R.id.share_beer:
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
        try {
            fBeerList.updateBeerList();
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                showDialog(SORT_DIALOG_ID);
                return true;
            case R.id.show_only_style:
                showDialog(FILTER_BY_STYLE_DIALOG_ID);
                return true;
            case R.id.show_only_available:
                showDialog(FILTER_BY_AVAILABLE_DIALOG_ID);
                return true;
            case R.id.export:
                doExport();
                return true;
            case R.id.refresh_database:
                loadBeersInBackground();
                return true;
            case R.id.reload_database:
                doReloadDatabase();
                return true;
            case R.id.visit_festival_website:
                goToFestivalWebsite();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        Uri festivalUri = Uri.parse(getResources().getString(R.string.festival_website_url));
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, festivalUri);
        startActivity(launchBrowser);
    }

    private void doReloadDatabase() {
        // delete all beers
        getHelper().deleteAll();
        fAppPreferences.setLastUpdateMD5("");
        loadBeersInBackground();
    }

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
            default:
                dialog = null;
        }
        return dialog;
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

    private Dialog createAvailabilityDialog() {
        boolean hideUnavailable = fAppPreferences.getHideUnavailableBeers();
        boolean[] selectedChoice = new boolean[]{hideUnavailable};

        String[] choices = new String[]{
                getResources().getString(R.string.filter_available_hide)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter_available_dialog_title);

        builder.setMultiChoiceItems(choices, selectedChoice, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i, final boolean b) {
                hideUnavailableBeers(b);
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    private Dialog createSortDialog() {
        final SortOrder[] sortOrders = SortOrder.values();
        final CharSequence[] items = new CharSequence[sortOrders.length];
        int checkedItem = -1;
        for (int i = 0; i < items.length; i++) {
            items[i] = sortOrders[i].toString();
            if (sortOrders[i] == fAppPreferences.getSortOrder()) {
                checkedItem = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_dialog_title);
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                sortBy(sortOrders[i]);
                dialogInterface.dismiss();
            }
        });
        return builder.create();
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

    private void hideUnavailableBeers(boolean hide) {
        try {
            fBeerList.hideUnavailableBeers(hide);
            fAppPreferences.setHideUnavailableBeers(hide);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private void filterByStyle(Set<String> stylesToHide) {
        try {
            fBeerList.stylesToHide(stylesToHide);
            fAppPreferences.setStylesToHide(stylesToHide);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private void filterBy(String filterText) {
        try {
            fBeerList.filterBy(filterText);
            fAppPreferences.setFilterText(filterText);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private void sortBy(SortOrder sortOrder) {
        try {
            fBeerList.sortBy(sortOrder);
            fAppPreferences.setSortOrder(sortOrder);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
    }

    private class MyLoadBeersTask extends LoadBeersTask {
        private final ProgressDialog fLoadProgressDialog;

        public MyLoadBeersTask(AppPreferences appPreferences) {
            super(appPreferences);
            fLoadProgressDialog = new ProgressDialog(CamBeerFestApplication.this);
        }

        @Override
        protected void onPreExecute() {
            fLoadProgressDialog.setMessage(CamBeerFestApplication.this.getText(R.string.loading_message));
            fLoadProgressDialog.setIndeterminate(true);
            fLoadProgressDialog.setCancelable(false);
            fLoadProgressDialog.show();
        }

        @Override
        protected void onPostExecute(final Result result) {
            JsonBeerList beerList = result.BeerList;
            if (beerList == null) {
                Throwable t = result.Throwable;
                if (t != null) {
                    Log.e(TAG, "Exception while downloading beer list. " + t.getMessage(), t);
                    Toast.makeText(getApplicationContext(),
                            "Failed to download beers. " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                fLoadProgressDialog.dismiss();
            } else {
                fLoadProgressDialog.dismiss();

                final UpdateBeersTask updateBeersTask = new MyUpdateBeersTask(beerList.size());
                updateBeersTask.execute(beerList);
            }
        }

        @Override
        protected void onProgressUpdate(final String... msg) {
            fLoadProgressDialog.setMessage(msg[0]);
        }
    }

    private class MyUpdateBeersTask extends UpdateBeersTask {
        private final ProgressDialog fUpdateProgressDialog;
        private final int fNumberOfBeers;

        private MyUpdateBeersTask(final int numberOfBeers) {
            super(getApplicationContext(), getConnectionSource(), getBreweryDao(), getBeerDao());
            fUpdateProgressDialog = new ProgressDialog(CamBeerFestApplication.this);
            fNumberOfBeers = numberOfBeers;
        }

        @Override
        protected void onPreExecute() {
            fUpdateProgressDialog.setMessage("Updating database");
            fUpdateProgressDialog.setProgress(0);
            fUpdateProgressDialog.setMax(fNumberOfBeers);
            fUpdateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            fUpdateProgressDialog.setIndeterminate(false);
            fUpdateProgressDialog.setCancelable(false);
            fUpdateProgressDialog.show();
        }

        @Override
        protected void onPostExecute(final Long aLong) {
            CamBeerFestApplication.this.notifyBeersChanged();
            fUpdateProgressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(final Beer... values) {
            String name = values[0].getName();
            int maxLength = 12;
            if (name.length() > maxLength) {
                name = name.substring(0, maxLength - 3);
                name = name + "...";
            }
            fUpdateProgressDialog.setMessage("Updated " + name);
            fUpdateProgressDialog.incrementProgressBy(1);
        }
    }
}