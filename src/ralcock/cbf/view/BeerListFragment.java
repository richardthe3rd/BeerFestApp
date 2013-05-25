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
import com.actionbarsherlock.app.SherlockListFragment;
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
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;
import ralcock.cbf.util.ExceptionReporter;

import java.sql.SQLException;
import java.util.Set;

public abstract class BeerListFragment extends SherlockListFragment implements ListChangedListener {
    private static final String TAG = BeerListFragment.class.getName();

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private BeerDatabaseHelper fDBHelper;
    private BeerList fBeerList;
    private BeerListAdapter fAdapter;

    private ExceptionReporter fExceptionReporter;
    private BeerSharer fBeerSharer;
    private BeerSearcher fBeerSearcher;

    protected BeerListFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beer_listview_fragment, container, false);
    }

    abstract BeerList makeBeerList(final BeerDao beerDao, final BreweryDao breweryDao) throws SQLException;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CamBeerFestApplication application = getCamBeerFestApplication();

        fExceptionReporter = new ExceptionReporter(application);
        fBeerSharer = new BeerSharer(application);
        fBeerSearcher = new BeerSearcher(application);

        application.addListChangedListener(this);

        try {
            fBeerList = makeBeerList(getBeerDao(), getBreweryDao());
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to make BeerList", e);
        }
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
        try {
            getBeerDao().update(beer);
            beersChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Update beer list failed.", e);
        }
    }

    private Beer getBeer(final long id) {
        try {
            return getBeerDao().getBeerWithId(id);
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to get beer with ID " + id, e);
            return null;
        }
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
        try {
            fBeerList.filterBy(filterText);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to update Filter text", e);
        }
    }

    public void sortOrderChanged(final SortOrder sortOrder) {
        try {
            fBeerList.sortBy(sortOrder);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to update sort order", e);
        }
    }

    public void stylesToHideChanged(final Set<String> stylesToHide) {
        try {
            fBeerList.stylesToHide(stylesToHide);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to update hidden styles", e);
        }
    }

    public void statusToShowChanged(final StatusToShow statusToShow) {
        try {
            fBeerList.setStatusToShow(statusToShow);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException sqlx) {
            fExceptionReporter.report(TAG, "Failed to toggle status to show.", sqlx);
        }
    }

    public void beersChanged() {
        try {
            Log.i(TAG, "beersChanged: notifying ListAdapter of changed DataSet.");
            fBeerList.updateBeerList();
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "Failed to update beer list", e);
        }
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this.getActivity(), BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    private BreweryDao getBreweryDao() {
        return getHelper().getBreweryDao();
    }

    private BeerDao getBeerDao() {
        return getHelper().getBeerDao();
    }
}
