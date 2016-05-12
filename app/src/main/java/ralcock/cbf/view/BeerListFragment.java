package ralcock.cbf.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.support.v4.app.ListFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.CamBeerFestApplication;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSearcher;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.StatusToShow;
import ralcock.cbf.model.dao.Beers;

import java.util.Set;

public abstract class BeerListFragment extends ListFragment implements ListChangedListener {
    private static final String TAG = BeerListFragment.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private BeerDatabaseHelper fDBHelper;
    private BeerList fBeerList;
    private BeerListAdapter fAdapter;

    private BeerSharer fBeerSharer;
    private BeerSearcher fBeerSearcher;

    protected BeerListFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beer_listview_fragment, container, false);
    }

    abstract BeerList makeBeerList(final Beers beers);

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CamBeerFestApplication application = getCamBeerFestApplication();

        fBeerSharer = new BeerSharer(application);
        fBeerSearcher = new BeerSearcher(application);

        application.addListChangedListener(this);

        fBeerList = makeBeerList(getBeers());
        fAdapter = new BeerListAdapter(getActivity(), fBeerList);
        setListAdapter(fAdapter);

        // Add list click listener.
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i(TAG, "Starting BeerDetails Activity with ID " + id);
                Intent intent = new Intent(getActivity(), BeerDetailsActivity.class);
                intent.putExtra(BeerDetailsActivity.EXTRA_BEER_ID, id);
                startActivityForResult(intent, SHOW_BEER_DETAILS_REQUEST_CODE);
            }
        });

        getListView().setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu contextMenu, final View view, final ContextMenu.ContextMenuInfo contextMenuInfo) {
                getActivity().getMenuInflater().inflate(R.menu.list_context_menu, contextMenu);
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
                Beer beer = getBeer(info.id);
                boolean isBookMarked = beer.isIsOnWishList();
                contextMenu.findItem(R.id.unBookmarkBeer).setVisible(isBookMarked);
                contextMenu.findItem(R.id.bookmarkBeer).setVisible(!isBookMarked);
            }
        });
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Beer beer = getBeer(info.id);
        switch (item.getItemId()) {
            case R.id.bookmarkBeer:
                toggleBookmark(beer);
                return true;
            case R.id.unBookmarkBeer:
                toggleBookmark(beer);
                return true;
            case R.id.shareBeer:
                fBeerSharer.shareBeer(beer);
                return true;
            case R.id.searchBeer:
                fBeerSearcher.searchBeer(beer);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void toggleBookmark(final Beer beer) {
        beer.setIsOnWishList(!beer.isIsOnWishList());
        getBeers().updateBeer(beer);
        beersChanged();
    }

    private Beer getBeer(final long id) {
        return getBeers().getBeerWithId(id);
    }

    private CamBeerFestApplication getCamBeerFestApplication() {
        return (CamBeerFestApplication) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getCamBeerFestApplication().removeListChangedListener(this);
    }

    public void filterTextChanged(String filterText) {
        fBeerList.filterBy(filterText);
        fAdapter.notifyDataSetChanged();
    }

    public void sortOrderChanged(final SortOrder sortOrder) {
        fBeerList.sortBy(sortOrder);
        fAdapter.notifyDataSetChanged();
    }

    public void stylesToHideChanged(final Set<String> stylesToHide) {
        fBeerList.stylesToHide(stylesToHide);
        fAdapter.notifyDataSetChanged();
    }

    public void statusToShowChanged(final StatusToShow statusToShow) {
        fBeerList.setStatusToShow(statusToShow);
        fAdapter.notifyDataSetChanged();
    }

    public void beersChanged() {
        Log.i(TAG, "beersChanged: notifying ListAdapter of changed DataSet.");
        fBeerList.updateBeerList();
        fAdapter.notifyDataSetChanged();
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this.getActivity(), BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    private Beers getBeers() {
        return getHelper().getBeers();
    }
}
