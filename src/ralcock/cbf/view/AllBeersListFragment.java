package ralcock.cbf.view;

import ralcock.cbf.AppPreferences;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;

public class AllBeersListFragment extends BeerListFragment {
    public AllBeersListFragment() {
        super();
    }


    @Override
    BeerList makeBeerList(final BeerDao beerDao, final BreweryDao breweryDao) throws SQLException {
        AppPreferences preferences = new AppPreferences(this.getActivity());
        return BeerList.allBeers(beerDao, breweryDao, preferences.getBeerListConfig());
    }
}
