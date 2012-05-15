package ralcock.cbf.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSearcher;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.StarRating;
import ralcock.cbf.util.ExceptionReporter;

import java.sql.SQLException;

public final class BeerDetailsActivity extends OrmLiteBaseActivity<BeerDatabaseHelper> {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDetailsActivity.class.getName();

    public static final String EXTRA_BEER_ID = "BEER";

    private Beer fBeer;
    private final BeerSharer fBeerSharer;
    private final BeerSearcher fBeerSearcher;
    private final ExceptionReporter fExceptionReporter;
    private BeerDetailsView fBeerDetailsView = null;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
        fBeerSearcher = new BeerSearcher(this);
        fExceptionReporter = new ExceptionReporter(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            long id = getIntent().getExtras().getLong(EXTRA_BEER_ID);
            fBeer = getHelper().getBeerDao().queryForId(id);
            displayBeer();
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, "", e);
        }
    }

    private void displayBeer() {
        if (fBeerDetailsView == null)
            fBeerDetailsView = new BeerDetailsView();

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
        MenuInflater inflater = new MenuInflater(getApplicationContext());
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

    final class BeerDetailsView {
        final TextView BeerNameAndAbv;
        final TextView BeerStyle;
        final TextView BeerDescription;
        final RatingBar BeerRatingBar;
        final TextView BreweryName;
        final TextView BreweryDescription;
        final TextView BeerStatus;

        public BeerDetailsView() {
            BeerNameAndAbv = (TextView) findViewById(R.id.detailsViewBeerNameAndAbv);
            BeerStyle = (TextView) findViewById(R.id.detailsViewBeerStyle);
            BeerDescription = (TextView) findViewById(R.id.detailsViewBeerDescription);
            BreweryName = (TextView) findViewById(R.id.detailsViewBreweryName);
            BreweryDescription = (TextView) findViewById(R.id.detailsViewBreweryDescription);
            BeerRatingBar = (RatingBar) findViewById(R.id.detailsViewBeerRatingBar);
            BeerStatus = (TextView) findViewById(R.id.detailsViewBeerStatus);
        }
    }
}
