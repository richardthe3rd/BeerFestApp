package ralcock.cbf;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import ralcock.cbf.model.BeerDatabase;
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
    private Toast fHintToast = null;
    private SortOrder fSortOrder = SortOrder.BREWERY_NAME_ASC;
    private BeerDatabase fBeerDatabase;
    private final BeerSharer fBeerSharer;

    private BeerCursorAdapter fAdapter;
    private EditText fEditText = null;

    private TextWatcher fFilterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {}

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            fAdapter.getFilter().filter(s);
        }
    };

    public CamBeerFestApplication() {
        super();
        fBeerSharer = new BeerSharer(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "In onSaveInstanceState");
        outState.putSerializable("BEER_SORT_ORDER", fSortOrder);
        outState.putSerializable("BEER_LIST_FILTER_STRING", fEditText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        Log.d(TAG, "In onRestoreInstanceState");
        initFromSavedState(state);
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "In onDestroy");
        super.onDestroy();
        fEditText.removeTextChangedListener(fFilterTextWatcher);
    }

    private void initFromSavedState(Bundle state) {
        fSortOrder = (SortOrder) state.getSerializable("BEER_SORT_ORDER");
        Log.d(TAG, "initFromSavedState fSortOrder->" + fSortOrder);

        String filter_string = (String)state.getSerializable("BEER_LIST_FILTER_STRING");
        Log.d(TAG, "initFromSavedState fEditText ->" + filter_string);
        fEditText.setText(filter_string);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.beer_list_view);

        // If the toast is showing, cancel it
        if (fHintToast != null) {
            fHintToast.cancel();
        }

        setTitle(getResources().getText(R.string.list_title));

        if (savedInstanceState != null) {
            initFromSavedState(savedInstanceState);
        }

        new CreateListAdapterTask().execute("beers.json");

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

        fEditText = (EditText) findViewById(R.id.search);
        fEditText.addTextChangedListener(fFilterTextWatcher);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_BEER_DETAILS_REQUEST_CODE) {
            Cursor c = fBeerDatabase.getBeerListCursor(fSortOrder);
            getBeerCursorAdapter().changeCursor(c);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        // restore the filter
        fAdapter.getFilter().filter(fEditText.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                showSortDialog();
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
        int checkeditem = items.indexOf(fSortOrder);
        builder.setSingleChoiceItems(listAdapter, checkeditem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                sortBy(items.get(i));
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sortBy(SortOrder sortBy) {
        fSortOrder = sortBy;
        getListView().clearTextFilter();
        Cursor c = fBeerDatabase.getBeerListCursor(fSortOrder);
        getBeerCursorAdapter().changeCursor(c);
        setTitle(getResources().getText(R.string.list_title) + " (" + fSortOrder.getDescription() + ")");
    }

    private BeerCursorAdapter getBeerCursorAdapter() {
        return ((BeerCursorAdapter)getListAdapter());
    }

    private class CreateListAdapterTask extends AsyncTask<String, Void, BeerDatabase> {
        private ProgressDialog fDialog;

        @Override
        protected void onPreExecute() {
            fDialog = ProgressDialog.show(CamBeerFestApplication.this, "",
                    "Loading beers, please wait...", false);
        }

        @Override
        protected void onPostExecute(BeerDatabase beerDatabase) {
            fBeerDatabase = beerDatabase;
            Cursor c = beerDatabase.getBeerListCursor(fSortOrder);
            startManagingCursor(c);
            fAdapter = new BeerCursorAdapter(CamBeerFestApplication.this, c);

            fAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence constraint) {
                    Log.d(TAG, "runQuery: " + constraint);
                    return fBeerDatabase.getFilteredBeerListCursor(fSortOrder, constraint);
                }
            });

            setListAdapter(fAdapter);

            fDialog.dismiss();
        }

        @Override
        protected BeerDatabase doInBackground(String... strings) {
            return new BeerDatabase(CamBeerFestApplication.this);
        }
    }

 }