package ralcock.cbf.model;

import android.content.Context;

public class BeerDatabaseFactory {

    private final Context fContext;

    public BeerDatabaseFactory(final Context context) {
        this.fContext = context;
    }

    public BeerDatabase createDatabase(){
        return new BeerDatabase(new BeerDatabaseHelper(fContext, BeerDatabase.DATABASE_NAME));
    }
}
