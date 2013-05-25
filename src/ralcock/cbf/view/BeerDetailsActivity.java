package ralcock.cbf.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.util.ExceptionReporter;

import java.sql.SQLException;

//OrmLiteBaseActivity<BeerDatabaseHelper>
public final class BeerDetailsActivity extends SherlockFragmentActivity {

    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDetailsActivity.class.getName();

    public static final String EXTRA_BEER_ID = "BEER";

    private Beer fBeer;
    private final BeerSharer fBeerSharer;
    private final ExceptionReporter fExceptionReporter;

    private BeerDatabaseHelper fDBHelper;
    private ShareActionProvider fShareActionProvider;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
        fExceptionReporter = new ExceptionReporter(this);
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this, BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    Beer getBeer() {
        return fBeer;
    }

    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.beer_details_activity);

        try {
            long id = getIntent().getExtras().getLong(EXTRA_BEER_ID);
            Log.i(TAG, "In BeerDetailsActivity.onCreate with ID " + id);
            fBeer = getHelper().getBeerDao().queryForId(id);
            Log.i(TAG, "In BeerDetailsActivity.onCreate with Beer " + fBeer);
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "", e);
        }

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(fBeer.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.details_options_menu, menu);

        fShareActionProvider = (ShareActionProvider) (menu.findItem(R.id.shareBeer).getActionProvider());
        fShareActionProvider.setShareIntent(fBeerSharer.makeShareIntent(fBeer));
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

    @SuppressWarnings({"UnusedDeclaration"}) // Called from beer_details_activity.xml.xml
    public void shareBeer(View button) {
        fBeerSharer.shareBeer(fBeer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
