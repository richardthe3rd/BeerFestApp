package ralcock.cbf;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.ListView;
import ralcock.cbf.model.BeerDatabase;
import ralcock.cbf.model.BeerDatabaseFactory;
import ralcock.cbf.model.BeerWithRating;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.view.BeerCursorAdapter;
import ralcock.cbf.view.BeerDetailsView;
import ralcock.cbf.view.BeerSharer;

import java.util.Arrays;
import java.util.List;

public class CamBeerFestApplication extends ListActivity {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private BeerDatabase fBeerDatabase;

    private BeerCursorAdapter fAdapter;

    private EditText fFilterTextBox = null;

    private final BeerSharer fBeerSharer;

    private final AppPreferences fAppPreferences;

    private final TextWatcher fFilterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            filterBy(s.toString());
        }
    };
    private BeerDatabaseFactory fDatabaseFactory;


    public CamBeerFestApplication() {
        super();
        fAppPreferences = new AppPreferences(this);
        fDatabaseFactory = new BeerDatabaseFactory(this);
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

        setContentView(R.layout.beer_list_view);

        fFilterTextBox = (EditText) findViewById(R.id.search);
        fFilterTextBox.setText(fAppPreferences.getFilterText());
        fFilterTextBox.addTextChangedListener(fFilterTextWatcher);

        Button clearFilterButton = (Button)findViewById(R.id.clear_filter_text);
        clearFilterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fFilterTextBox.setText("");
                filterBy("");
            }
        });
        
        setTitle(getResources().getText(R.string.list_title));

        fBeerDatabase = fDatabaseFactory.createDatabase();
        Cursor cursor = fBeerDatabase.getFilteredBeerListCursor(
                            fAppPreferences.getSortOrder(),
                            fAppPreferences.getFilterText());
        startManagingCursor(cursor);

        asyncLoadBeers("beers.json");

        // List adapter setup
        fAdapter = new BeerCursorAdapter(CamBeerFestApplication.this, cursor);
        fAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence filterText) {
                Log.d(TAG, "FilterQueryProvider.runQuery with filter: " + filterText);
                return fBeerDatabase.getFilteredBeerListCursor(fAppPreferences.getSortOrder(), filterText);
            }
        });
        setListAdapter(fAdapter);

        configureListView();
    }

    private void asyncLoadBeers(String source) {
        new LoadBeersTask(this, fBeerDatabase).execute(source);
    }

    private void configureListView() {
        ListView lv = getListView();

        lv.setTextFilterEnabled(true);

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
                        BeerWithRating beerToShare = fBeerDatabase.getBeerForId(info.id);
                        fBeerSharer.shareBeer(beerToShare);
                        return true;
                    }
                });
            }
        });
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
                fBeerDatabase.clearAll();
                asyncLoadBeers("beers.json");
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
        fAdapter.getFilter().filter(filterText);
        fAppPreferences.setFilterText(filterText);
    }

    private void sortBy(SortOrder sortOrder) {
        fAppPreferences.setSortOrder(sortOrder);
        updateCursor();
    }

    void updateCursor() {
        Cursor c = fBeerDatabase.getFilteredBeerListCursor(fAppPreferences.getSortOrder(), fAppPreferences.getFilterText());
        fAdapter.changeCursor(c);
    }

}