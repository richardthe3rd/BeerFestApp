package ralcock.cbf.view;

import ralcock.cbf.AppPreferences;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.dao.Beers;

public class LowNoAlcoholListFragment extends BeerListFragment {
    public LowNoAlcoholListFragment() {
        super();
    }

    @Override
    BeerList makeBeerList(final Beers beers) {
        AppPreferences preferences = new AppPreferences(this.getActivity());
        return BeerList.lowNoAlcoholBeers(beers, preferences.getBeerListConfig());
    }

}
