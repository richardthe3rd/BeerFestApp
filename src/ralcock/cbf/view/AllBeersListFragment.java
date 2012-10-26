package ralcock.cbf.view;

import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.Set;

public class AllBeersListFragment extends BeerListFragment {
    public AllBeersListFragment() {
        super();
    }

    @Override
    BeerList makeBeerList(final BeerDao beerDao, final BreweryDao breweryDao,
                          final SortOrder sortOrder, final String filterText,
                          final Set<String> stylesToHide) throws SQLException {
        return new BeerList(beerDao, breweryDao,
                sortOrder,
                filterText,
                stylesToHide,
                false);
    }
}
