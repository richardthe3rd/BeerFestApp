package ralcock.cbf.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSearcher;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.StarRating;
import ralcock.cbf.util.ExceptionReporter;

import java.sql.SQLException;

//OrmLiteBaseActivity<BeerDatabaseHelper>
public final class BeerDetailsActivity extends SherlockActivity {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDetailsActivity.class.getName();

    public static final String EXTRA_BEER_ID = "BEER";

    private Beer fBeer;
    private final BeerSharer fBeerSharer;
    private final BeerSearcher fBeerSearcher;
    private final ExceptionReporter fExceptionReporter;
    private BeerDetailsView fBeerDetailsView = null;

    private BeerDatabaseHelper fDBHelper;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
        fBeerSearcher = new BeerSearcher(this);
        fExceptionReporter = new ExceptionReporter(this);
    }

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(this, BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.beer_details_activity);
            long id = getIntent().getExtras().getLong(EXTRA_BEER_ID);
            fBeer = getHelper().getBeerDao().queryForId(id);
            displayBeer();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "", e);
        }
    }

    private void displayBeer() {
        if (fBeerDetailsView == null)
            fBeerDetailsView = new BeerDetailsView(this);

        setTitle(fBeer.getName() + " - " + fBeer.getBrewery().getName());

        fBeerDetailsView.BeerNameAndAbv.setText(String.format("%s (%.1f%%)", fBeer.getName(), fBeer.getAbv()));
        fBeerDetailsView.BeerDescription.setText(fBeer.getDescription());
        fBeerDetailsView.BeerStyle.setText(fBeer.getStyle());

        fBeerDetailsView.BeerStatus.setText(fBeer.getStatus());

        fBeerDetailsView.BreweryName.setText(fBeer.getBrewery().getName());
        fBeerDetailsView.BreweryDescription.setText(fBeer.getBrewery().getDescription());

        fBeerDetailsView.BeerRatingBar.setRating(fBeer.getRating());
        fBeerDetailsView.BeerRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser)
                    rateBeer(new StarRating(rating));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.details_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shareBeer:
                fBeerSharer.shareBeer(fBeer);
                return true;
            case R.id.searchBeer:
                fBeerSearcher.searchBeer(fBeer);
                return true;
            case R.id.clearRating:
                rateBeer(StarRating.NO_STARS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rateBeer(StarRating rating) {
        try {
            fBeer.setNumberOfStars(rating);
            getHelper().getBeerDao().update(fBeer);
            displayBeer();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    static final class BeerDetailsView {
        final TextView BeerNameAndAbv;
        final TextView BeerStyle;
        final TextView BeerDescription;
        final RatingBar BeerRatingBar;
        final TextView BreweryName;
        final TextView BreweryDescription;
        final TextView BeerStatus;

        public BeerDetailsView(final Activity activity) {
            BeerNameAndAbv = (TextView) activity.findViewById(R.id.detailsViewBeerNameAndAbv);
            BeerStyle = (TextView) activity.findViewById(R.id.detailsViewBeerStyle);
            BeerDescription = (TextView) activity.findViewById(R.id.detailsViewBeerDescription);
            BreweryName = (TextView) activity.findViewById(R.id.detailsViewBreweryName);
            BreweryDescription = (TextView) activity.findViewById(R.id.detailsViewBreweryDescription);
            BeerRatingBar = (RatingBar) activity.findViewById(R.id.detailsViewBeerRatingBar);
            BeerStatus = (TextView) activity.findViewById(R.id.detailsViewBeerStatus);
        }
    }
}
