package ralcock.cbf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
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
import ralcock.cbf.view.BeerDetailsView;
import ralcock.cbf.view.BeerListAdapter;
import ralcock.cbf.view.BeerSharer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CamBeerFestApplication extends OrmLiteBaseListActivity<BeerDatabaseHelper> {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private EditText fFilterTextBox = null;

    private BeerList fBeerList;
    private final BeerSharer fBeerSharer;

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

        fBeerList = new BeerList(getBeerDao(), getBreweryDao(),
                fAppPreferences.getSortOrder(),
                fAppPreferences.getFilterText());

        fAdapter = new BeerListAdapter(CamBeerFestApplication.this, fBeerList);

        setContentView(R.layout.beer_list_view);

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

        setTitle(getResources().getText(R.string.list_title));

        asyncLoadBeers();

        /*
        fAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence filterText) {
                Log.d(TAG, "FilterQueryProvider.runQuery with filter: " + filterText);
                return fBeerDatabase.getFilteredBeerListCursor(fAppPreferences.getSortOrder(), filterText);
            }
        });
        */
        setListAdapter(fAdapter);

        configureListView();
    }

    private BreweryDao getBreweryDao() {
        return getHelper().getBreweryDao();
    }

    private BeerDao getBeerDao() {
        return getHelper().getBeerDao();
    }

    private InputStream inputStream() throws IOException {
        boolean localJson = false;
        if (localJson) {
            return getAssets().open("beers.json");
        } else {
            // URL is in my Dropbox public folder.
            URL url = new URL("http://dl.dropbox.com/u/4457379/beers.json");
            return url.openStream();
        }
    }

    private void asyncLoadBeers() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getText(R.string.loading_message));
        progressDialog.setIndeterminate(true);

        final LoadBeersTask task = new LoadBeersTask(getBeerDao(), getBreweryDao(), fAdapter, fBeerList, progressDialog);
        try {
            final Iterable<Beer> beers = new JsonBeerList(inputStream());
            //noinspection unchecked
            task.execute(beers);
        } catch (IOException iox) {
            Log.e(TAG, "Failed to load beers.", iox);
        } catch (JSONException jsx) {
            Log.e(TAG, "Failed to load beers.", jsx);
        }
    }

    private void configureListView() {
        ListView lv = getListView();

        //lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CamBeerFestApplication.this, BeerDetailsView.class);
                intent.putExtra(BeerDetailsView.EXTRA_BEER_ID, id);
                startActivityForResult(intent, SHOW_BEER_DETAILS_REQUEST_CODE);
            }
        });

        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu contextMenu, final View view, final ContextMenu.ContextMenuInfo contextMenuInfo) {
                MenuItem shareThisMenu = contextMenu.add(0, Menu.FIRST, 0, R.string.share_this_beer_title);
                shareThisMenu.setIcon(android.R.drawable.ic_menu_send);
                shareThisMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
                        fBeerSharer.shareBeer(getBeerWithId(info.id));
                        return true;
                    }
                });
            }
        });
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
                showSortDialog();
                return true;
            case R.id.reload_database:
                // delete all beers
                getHelper().deleteAll();
                asyncLoadBeers();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSortDialog() {
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
        AlertDialog dialog = builder.create();
        dialog.show();
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