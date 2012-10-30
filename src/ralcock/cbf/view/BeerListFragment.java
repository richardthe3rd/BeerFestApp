package ralcock.cbf.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.AppPreferences;
import ralcock.cbf.CamBeerFestApplication;
import ralcock.cbf.R;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.Set;

public abstract class BeerListFragment extends SherlockListFragment implements ListChangedListener {

    private static final int SHOW_BEER_DETAILS_REQUEST_CODE = 1;

    private BeerDatabaseHelper fDBHelper;
    private BeerList fBeerList;
    private BeerListAdapter fAdapter;
    private AppPreferences fAppPreferences;

    protected BeerListFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beer_listview_fragment, container, false);
    }

    abstract BeerList makeBeerList(final BeerDao beerDao, final BreweryDao breweryDao,
                                   final SortOrder sortOrder, final String filterText,
                                   final Set<String> stylesToHide) throws SQLException;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CamBeerFestApplication application = getCamBeerFestApplication();
        application.addListChangedListener(this);

        fAppPreferences = new AppPreferences(getActivity());
        try {
            fBeerList = makeBeerList(getBeerDao(), getBreweryDao(),
                    fAppPreferences.getSortOrder(),
                    fAppPreferences.getFilterText(),
                    fAppPreferences.getStylesToHide());
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }
        fAdapter = new BeerListAdapter(getActivity(), fBeerList);
        setListAdapter(fAdapter);

        // Add list click listener.
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), BeerDetailsActivity.class);
                intent.putExtra(BeerDetailsActivity.EXTRA_BEER_ID, id);
                startActivityForResult(intent, SHOW_BEER_DETAILS_REQUEST_CODE);
            }
        });

        // TODO: "Context" menu on list?
        /*
        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu contextMenu, final View view, final ContextMenu.ContextMenuInfo contextMenuInfo) {
                MenuInflater inflater = getSupportMenuInflater();
                inflater.inflate(R.menu.list_context_menu, contextMenu);
            }
        });
        */
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
            //TODO: e.printStackTrace();
        }
    }

    public void sortOrderChanged(final SortOrder sortOrder) {
        try {
            fBeerList.sortBy(sortOrder);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            // TODO:
        }
    }

    public void stylesToHideChanged(final Set<String> stylesToHide) {
        try {
            fBeerList.stylesToHide(stylesToHide);
            fAdapter.notifyDataSetChanged();
        } catch (SQLException e) {
            // TODO:
        }
    }

    public void beersChanged() {
        fAdapter.notifyDataSetChanged();
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
