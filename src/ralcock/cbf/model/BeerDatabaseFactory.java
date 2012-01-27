package ralcock.cbf.model;

import android.content.Context;

public class BeerDatabaseFactory {

    private Context fContext;

    public BeerDatabaseFactory(Context context) {
        this.fContext = context;
    }

    public BeerDatabase createDatabase(){
        return new BeerDatabase(new BeerDatabaseHelper(fContext));
    }
}
