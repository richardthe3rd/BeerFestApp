package ralcock.cbf.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerChangedListener;
import ralcock.cbf.model.BeerAccessor;

//OrmLiteBaseActivity<BeerDatabaseHelper>
public final class BeerDetailsActivity extends AppCompatActivity {

    private static final String TAG = BeerDetailsActivity.class.getName();
    public static final String EXTRA_BEER_ID = "BEER";

    private final BeerSharer fBeerSharer;
    private final BeerAccessor fBeerAccessor;
    private ShareActionProvider fShareActionProvider;
    private long fBeerId;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
        fBeerAccessor = new BeerAccessor(this);
    }

    Beer getBeer() {
        return fBeerAccessor.getBeer(fBeerId);
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        fBeerId = getIntent().getExtras().getLong(EXTRA_BEER_ID);

        setContentView(R.layout.beer_details_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(getBeer().getName());
        setSupportActionBar(myToolbar);

        Log.i(TAG, "In BeerDetailsActivity.onCreate with ID " + fBeerId);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_options_menu, menu);

        fShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.shareBeer));
        fShareActionProvider.setShareIntent(fBeerSharer.makeShareIntent(getBeer()));

        fBeerAccessor.getBeers().addBeerChangedListener(new BeerChangedListener() {
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
