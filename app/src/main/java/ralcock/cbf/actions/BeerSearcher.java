package ralcock.cbf.actions;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import ralcock.cbf.model.Beer;

public class BeerSearcher {
    private final Context fContext;

    public BeerSearcher(final Context context) {
        //To change body of created methods use File | Settings | File Templates.
        fContext = context;
    }

    public void searchBeer(final Beer beer) {
        Intent intent = makeSearchIntent(beer);
        fContext.startActivity(intent);
    }

    public Intent makeSearchIntent(final Beer beer) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        String query = String.format("\"%s\" \"%s\"", beer.getBrewery().getName(), beer.getName());
        intent.putExtra(SearchManager.QUERY, query);
        return intent;
    }
}
