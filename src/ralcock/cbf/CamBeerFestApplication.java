package ralcock.cbf;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerListLoader;
import ralcock.cbf.view.BeerDetailsView;
import ralcock.cbf.view.BeerListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

public class CamBeerFestApplication extends ListActivity {
    private static final String TAG = CamBeerFestApplication.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;
    private ArrayAdapter<Beer> fListAdapter;
    private Beer.Comparator fComparator = null;
    private Vector<Beer> fBeerList;
    private Toast fHintToast = null;

    public CamBeerFestApplication() {
        super();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "In onSaveInstanceState");
        outState.putSerializable("BEER_LIST", fBeerList.toArray(new Beer[0]));
        outState.putSerializable("BEER_COMPARATOR", fComparator);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        Log.d(TAG, "In onRestoreInstanceState");
        initFromSavedState(state);
        super.onRestoreInstanceState(state);
    }

    private void initFromSavedState(Bundle state) {
        Beer[] beers = (Beer[]) state.getSerializable("BEER_LIST");
        fBeerList = new Vector<Beer>(Arrays.asList(beers));
        fComparator = (Beer.Comparator) state.getSerializable("BEER_COMPARATOR");
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the toast is showing, cancel it
        if (fHintToast != null) {
            fHintToast.cancel();
        }

        if (savedInstanceState != null) {
            initFromSavedState(savedInstanceState);
        }

        new InitBeerListTask().execute("beers.json");

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CamBeerFestApplication.this, BeerDetailsView.class);
                Beer beer = (Beer) getListView().getItemAtPosition(position);
                intent.putExtra(BeerDetailsView.EXTRA_BEER_POSITION, position);
                intent.putExtra(BeerDetailsView.EXTRA_BEER, beer);
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
                        Beer beer = (Beer) getListView().getItemAtPosition(info.position);
                        shareBeer(beer);
                        return true;
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivity " + requestCode + ", " + resultCode);
        if (requestCode == SHOW_BEER_DETAILS_REQUEST_CODE) {
            if (resultCode == BeerDetailsView.RESULT_MODIFIED) {
                int position = data.getExtras().getInt(BeerDetailsView.EXTRA_BEER_POSITION);
                fListAdapter.getItem(position).updateRating();
                // need to redraw the list view
                //Log.i(TAG, "Invalidating listview");
                fListAdapter.notifyDataSetChanged();
                fListAdapter.sort(fComparator);
                //getListView().invalidateViews();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
            case R.id.sort_by_abv:
                if (fComparator instanceof Beer.AbvComparator) {
                    fComparator = new Beer.ReverseComparator(fComparator);
                } else {
                    fComparator = new Beer.AbvComparator();
                }
                updateSorting();
                return true;
            case R.id.sort_by_brewery:
                if (fComparator instanceof Beer.BreweryComparator) {
                    fComparator = new Beer.ReverseComparator(fComparator);
                } else {
                    fComparator = new Beer.BreweryComparator();
                }
                updateSorting();
                return true;
            case R.id.sort_by_beer:
                if (fComparator instanceof Beer.BeerComparator) {
                    fComparator = new Beer.ReverseComparator(fComparator);
                } else {
                    fComparator = new Beer.BeerComparator();
                }
                updateSorting();
                return true;
            case R.id.sort_by_rating:
                if (fComparator instanceof Beer.RatingComparator) {
                    fComparator = new Beer.ReverseComparator(fComparator);
                } else {
                    fComparator = new Beer.RatingComparator();
                }
                updateSorting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSorting() {
        fListAdapter.sort(fComparator);
        setTitle(getResources().getText(R.string.app_name) + " (" + fComparator.getDescription() + ")");
        Toast.makeText(getApplicationContext(), "Sort again to reverse direction", Toast.LENGTH_SHORT).show();
    }

    private void shareBeer(Beer beer) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String extraSubject = getResources().getString(R.string.share_this_beer_subject);
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);

        String extraText = getResources().getString(R.string.share_this_beer_text, beer.getBrewery().getName(), beer.getName());
        intent.putExtra(Intent.EXTRA_TEXT, extraText);

        String title = getResources().getString(R.string.share_this_beer_title);
        startActivity(Intent.createChooser(intent, title));
    }

    private void updateListAdapter() {
        fListAdapter = new BeerListAdapter(getApplicationContext(), fBeerList);
        if (fComparator != null) {
            fListAdapter.sort(fComparator);
        }
        setListAdapter(fListAdapter);
    }

    private class InitBeerListTask extends AsyncTask<String, Integer, Vector<Beer>> {

        private ProgressDialog fDialog;

        @Override
        protected void onPreExecute() {
            fDialog = ProgressDialog.show(CamBeerFestApplication.this, "", "Loading beers, please wait...", true);
        }

        @Override
        protected void onPostExecute(Vector<Beer> beers) {
            fBeerList = beers;
            updateListAdapter();
            fDialog.dismiss();
            fHintToast = Toast.makeText(CamBeerFestApplication.this, R.string.hint, Toast.LENGTH_LONG);
            fHintToast.show();
        }

        @Override
        protected Vector<Beer> doInBackground(String... jsonFiles) {

            if (fBeerList != null) {
                return fBeerList;
            }

            Log.i(TAG, "Initialising beer list from file");
            InputStream jsonStream = null;
            try {
                jsonStream = getAssets().open(jsonFiles[0]);
                BeerListLoader beerListLoader = new BeerListLoader(getApplicationContext(), jsonStream);
                return beerListLoader.getBeerList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (jsonStream != null) try {
                    jsonStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }

        }
    }
}