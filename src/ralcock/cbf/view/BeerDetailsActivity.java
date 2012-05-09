package ralcock.cbf.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.StarRating;

import java.sql.SQLException;

public final class BeerDetailsActivity extends OrmLiteBaseActivity<BeerDatabaseHelper> {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDetailsActivity.class.getName();

    public static final String EXTRA_BEER_ID = "BEER";

    private Beer fBeer;
    private final BeerSharer fBeerSharer;
    private final BeerSearcher fBeerSearcher;
    private BeerDetailsView fBeerDetailsView = null;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
        fBeerSearcher = new BeerSearcher(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // open DB and make queries on a bg thread
        new ShowBeerTask().execute(getIntent().getExtras().getLong(EXTRA_BEER_ID));
    }

    private void displayBeer() {
        if (fBeerDetailsView == null)
            fBeerDetailsView = new BeerDetailsView();

        setTitle(fBeer.getBrewery().getName() + " - " + fBeer.getName());

        fBeerDetailsView.BeerNameAndAbv.setText(String.format("%s (%.1f%%)", fBeer.getName(), fBeer.getAbv()));
        fBeerDetailsView.BeerDescription.setText(fBeer.getDescription());
        fBeerDetailsView.BeerStyle.setText(fBeer.getStyle());

        fBeerDetailsView.Status.setText(fBeer.getStatus());

        fBeerDetailsView.BreweryName.setText(fBeer.getBrewery().getName());
        fBeerDetailsView.BreweryDescription.setText(fBeer.getBrewery().getDescription());

        fBeerDetailsView.BeerRating.setNumStars(fBeer.getRating());
        fBeerDetailsView.BeerRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser)
                    rateBeer(new StarRating(rating));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(getApplicationContext());
        inflater.inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_beer:
                fBeerSharer.shareBeer(fBeer);
                return true;
            case R.id.search_beer:
                fBeerSearcher.searchBeer(fBeer);
                return true;
            case R.id.clear_rating:
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

    @SuppressWarnings({"UnusedDeclaration"}) // Called from beer_details_view.xml
    public void shareBeer(View button) {
        fBeerSharer.shareBeer(fBeer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ShowBeerTask extends AsyncTask<Long, Void, Beer> {

        @Override
        protected void onPostExecute(Beer beer) {
            setContentView(R.layout.beer_details_view);
            fBeer = beer;
            displayBeer();
        }

        @Override
        protected Beer doInBackground(Long... ids) {
            try {
                return getHelper().getBeerDao().queryForId(ids[0]);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    final class BeerDetailsView {
        final TextView BeerNameAndAbv;
        final TextView BeerStyle;
        final TextView BeerDescription;
        final RatingBar BeerRating;
        final TextView BreweryName;
        final TextView BreweryDescription;
        final TextView Status;

        public BeerDetailsView() {
            BeerNameAndAbv = (TextView) findViewById(R.id.details_view_beer_name_and_abv);
            BeerStyle = (TextView) findViewById(R.id.details_view_beer_style);
            BeerDescription = (TextView) findViewById(R.id.details_view_beer_description);
            BreweryName = (TextView) findViewById(R.id.details_view_brewery_name);
            BreweryDescription = (TextView) findViewById(R.id.details_view_brewery_description);
            BeerRating = (RatingBar) findViewById(R.id.details_view_beer_rating);
            Status = (TextView) findViewById(R.id.details_view_beer_status);
        }
    }
}
