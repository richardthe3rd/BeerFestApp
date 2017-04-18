package ralcock.cbf.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Window;
import android.support.v7.widget.ShareActionProvider;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerChangedListener;
import ralcock.cbf.model.BeerDatabaseHelper;

//OrmLiteBaseActivity<BeerDatabaseHelper>
public final class BeerDetailsActivity extends AppCompatActivity {

    private static final String TAG = BeerDetailsActivity.class.getName();
    public static final String EXTRA_BEER_ID = "BEER";

    private final BeerSharer fBeerSharer;

    private BeerDatabaseHelper fDBHelper;
    private ShareActionProvider fShareActionProvider;
    private long fBeerId;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this, BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    Beer getBeer() {
        return getHelper().getBeers().getBeerWithId(fBeerId);
    }

    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.beer_details_activity);

        fBeerId = getIntent().getExtras().getLong(EXTRA_BEER_ID);
        Log.i(TAG, "In BeerDetailsActivity.onCreate with ID " + fBeerId);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getBeer().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_options_menu, menu);

        fShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.shareBeer));
        fShareActionProvider.setShareIntent(fBeerSharer.makeShareIntent(getBeer()));

		getHelper().getBeers().addBeerChangedListener(new BeerChangedListener() {
			public void beerChanged(final Beer beer) {
				fShareActionProvider.setShareIntent(
					fBeerSharer.makeShareIntent(beer)
				);
			}
		});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareBeer(View button) {
        fBeerSharer.shareBeer(getBeer());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
