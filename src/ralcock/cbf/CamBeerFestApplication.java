package ralcock.cbf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;
import ralcock.cbf.view.BeerDetailsActivity;
import ralcock.cbf.view.BeerListAdapter;
import ralcock.cbf.view.BeerSearcher;
import ralcock.cbf.view.BeerSharer;
import ralcock.cbf.view.BeerStyleListAdapter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
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
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "In onDestroy");
        super.onDestroy();
        fFilterTextBox.removeTextChangedListener(fFilterTextWatcher);
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

        fBeerList = new BeerList(getBeerDao(), getBreweryDao(),
                fAppPreferences.getSortOrder(),
                fAppPreferences.getFilterText(),
                fAppPreferences.getStylesToHide(),
                fAppPreferences.getHideUnavailableBeers());

        fAdapter = new BeerListAdapter(CamBeerFestApplication.this, fBeerList);

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

        if (beerUpdateNeeded()) {
            loadBeersInBackground();
        }

        setListAdapter(fAdapter);

        configureListView();
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

    private URL beerListUrl() throws IOException {
        String beerJsonURL = getResources().getText(R.string.beer_list_url).toString();
        return new URL(beerJsonURL);
    }

    private void loadBeersInBackground() {
        final ProgressDialog updateProgressDialog = new ProgressDialog(this);
        final UpdateBeersTask updateBeersTask = new UpdateBeersTask(getHelper().getConnectionSource(),
                getBreweryDao(), getBeerDao(), fBeerList, fAdapter, updateProgressDialog);

        final ProgressDialog loadProgressDialog = new ProgressDialog(this);
        final LoadBeersTask loadBeersTask = new LoadBeersTask(loadProgressDialog, updateBeersTask);
        try {
            final LoadBeersTask.Source source = new LoadBeersTask.Source(beerListUrl());

            //noinspection unchecked
            loadBeersTask.execute(source);

            // 6 hours time
            fAppPreferences.setNextUpdateTime(System.currentTimeMillis() + (6 * 60 * 60 * 1000));
        } catch (IOException iox) {
            Log.e(TAG, "Failed to load beers.", iox);
        }
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.search_beer:
                fBeerSearcher.searchBeer(getBeerWithId(info.id));
                return true;
            case R.id.share_beer:
                fBeerSharer.shareBeer(getBeerWithId(info.id));
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private Beer getBeerWithId(final long id) {
        try {
            return getBeerDao().getBeerWithId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_BEER_DETAILS_REQUEST_CODE) {
            sortBy(fAppPreferences.getSortOrder());
            filterBy(fAppPreferences.getFilterText());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private Dialog createAvailabilityDialog() {
        boolean hideUnavailable = fAppPreferences.getHideUnavailableBeers();
        int selectedChoice = hideUnavailable ? 1 : 0;

        String[] choices = new String[]{"Show unavailable beers", "Hide unavailable beers"};

        ListAdapter listAdapter = new ArrayAdapter<String>(this, R.layout.sort_by_dialog_list_item, choices);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Filter by availability");

        builder.setSingleChoiceItems(listAdapter, selectedChoice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                hideUnavailableBeers(i == 1);
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    private Dialog createSortDialog() {
        final List<SortOrder> items = Arrays.asList(SortOrder.values());

        ListAdapter listAdapter = new ArrayAdapter<SortOrder>(this, R.layout.sort_by_dialog_list_item, items);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_dialog_title);
        int checkedItem = items.indexOf(fAppPreferences.getSortOrder());
        builder.setSingleChoiceItems(listAdapter, checkedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                sortBy(items.get(i));
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    private Dialog createStylesToHideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter_style_dialog_title);

        try {
            final Set<String> stylesToHide = fAppPreferences.getStylesToHide();
            final Set<String> allStyles = getBeerDao().getAvailableStyles();
            ;

            final BeerStyleListAdapter listAdapter = new BeerStyleListAdapter(this, allStyles, stylesToHide);
            builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialogInterface, final int i) {
                }
            });

            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    filterByStyle(listAdapter.getStylesToHide());
                }
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
            }
        });

        return builder.create();
    }

    private void hideUnavailableBeers(boolean hide) {
        fAppPreferences.setHideUnavailableBeers(hide);
        fBeerList.hideUnavailableBeers(hide);
        fAdapter.notifyDataSetChanged();
    }

    private void filterByStyle(Set<String> stylesToHide) {
        fAppPreferences.setStylesToHide(stylesToHide);
        fBeerList.stylesToHide(stylesToHide);
        fAdapter.notifyDataSetChanged();
    }

    private void filterBy(String filterText) {
        fAppPreferences.setFilterText(filterText);
        fBeerList.filterBy(filterText);
        fAdapter.notifyDataSetChanged();
    }

    private void sortBy(SortOrder sortOrder) {
        fAppPreferences.setSortOrder(sortOrder);
        fBeerList.sortBy(sortOrder);
        fAdapter.notifyDataSetChanged();
    }

}