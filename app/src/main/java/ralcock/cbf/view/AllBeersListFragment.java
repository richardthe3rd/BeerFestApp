package ralcock.cbf.view;

import ralcock.cbf.AppPreferences;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.dao.Beers;

public class AllBeersListFragment extends BeerListFragment {
    public AllBeersListFragment() {
        super();
    }

    @Override
    BeerList makeBeerList(final Beers beers) {
        AppPreferences preferences = new AppPreferences(this.getActivity());
        return BeerList.allBeers(beers, preferences.getBeerListConfig());
    }
}
