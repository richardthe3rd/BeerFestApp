package ralcock.cbf.model;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.dao.Beers;
import ralcock.cbf.model.Beer;

import java.util.Set;

public class BeerAccessor
{
    private final Context fContext;
    private BeerDatabaseHelper fDBHelper;

    public BeerAccessor(Context context)
    {
        fContext = context;

    }
    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(fContext, BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    public Set<String> getAvailableStyles() {
        return getBeers().getAvailableStyles();
    }
    public Beers getBeers() {
        return getHelper().getBeers();
    }

    public Beer getBeer(final long id) {
        return getBeers().getBeerWithId(id);
    }

    public void updateBeer(final Beer beer) {
        getBeers().updateBeer(beer);
    }

    public void release(){
        if (fDBHelper != null) {
            OpenHelperManager.releaseHelper();
        }
    }


}
