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
import org.json.JSONException;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.JsonBeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;
import ralcock.cbf.view.BeerDetailsActivity;
import ralcock.cbf.view.BeerListAdapter;
import ralcock.cbf.view.BeerSearcher;
import ralcock.cbf.view.BeerSharer;
import ralcock.cbf.view.BeerStyleListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CamBeerFestApplication extends OrmLiteBaseListActivity<BeerDatabaseHelper> {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private static final int SORT_DIALOG_ID = 0;
    private static final int FILTER_BY_STYLE_DIALOG_ID = 1;

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

    private BeerListAdapter fAdapter;

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
                fAppPreferences.getStylesToHide());

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
            Log.i(TAG, "Updating beers as last updated at " + fAppPreferences.getLastUpdateTime());
            loadBeersInBackground();
        }

        setListAdapter(fAdapter);

        configureListView();
    }

    private boolean beerUpdateNeeded() {
        try {
            if (0 == getBeerDao().getNumberOfBeers()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Date currentTime = new Date();
        Date lastUpdate = fAppPreferences.getLastUpdateTime();
        long deltaInMilliSec = currentTime.getTime() - lastUpdate.getTime();
        long deltaInSec = TimeUnit.MILLISECONDS.toSeconds(deltaInMilliSec);
        return deltaInSec > hoursInSeconds(12);
    }

    private long hoursInSeconds(final long hours) {
        return hours * 60 * 20;
    }

    private BreweryDao getBreweryDao() {
        return getHelper().getBreweryDao();
    }

    private BeerDao getBeerDao() {
        return getHelper().getBeerDao();
    }

    private InputStream inputStream() throws IOException {
        String beerJsonURL = getResources().getText(R.string.beer_list_url).toString();
        URL url = new URL(beerJsonURL);
        return url.openStream();
    }

    private void loadBeersInBackground() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getText(R.string.loading_message));
        progressDialog.setIndeterminate(true);

        final LoadBeersTask task = new LoadBeersTask(getHelper().getConnectionSource(),
                getBeerDao(), getBreweryDao(), fAdapter, fBeerList, progressDialog);
        try {
            final Iterable<Beer> beers = new JsonBeerList(inputStream());
            //noinspection unchecked
            task.execute(beers);
            fAppPreferences.setLastUpdateTime(new Date());
        } catch (IOException iox) {
            Log.e(TAG, "Failed to load beers.", iox);
        } catch (JSONException jsx) {
            Log.e(TAG, "Failed to load beers.", jsx);
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
            default:
                dialog = null;
        }
        return dialog;
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
        /*
        final Set<String> stylesToHide = fAppPreferences.getStylesToHide();

        Set<String> allStyles;
        try {
            allStyles = getBeerDao().getAvailableStyles();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        final String[] styleArray = allStyles.toArray(new String[allStyles.size()]);

        boolean[] checked = new boolean[allStyles.size()];
        for (int i = 0; i < styleArray.length; i++) {
            checked[i] = !stylesToHide.contains(styleArray[i]);
        }

        final String[] itemArray = new String[styleArray.length + 1];
        itemArray[0] = "Show All";
        System.arraycopy(styleArray, 0, itemArray, 1, styleArray.length);

        final boolean[] itemChecked = new boolean[styleArray.length + 1];
        itemChecked[0] = !stylesToHide.isEmpty();
        System.arraycopy(checked, 0, itemChecked, 1, checked.length);
        */
        // TODO: Use a custom list adapter (builder.setAdapter())

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