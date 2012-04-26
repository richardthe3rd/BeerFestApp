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

public final class BeerDetailsView extends OrmLiteBaseActivity<BeerDatabaseHelper> {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDetailsView.class.getName();

    public static final String EXTRA_BEER_ID = "BEER";

    private Beer fBeer;
    private long fId;
    private final BeerSharer fBeerSharer;

    public BeerDetailsView() {
        fBeerSharer = new BeerSharer(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fId = getIntent().getExtras().getLong(EXTRA_BEER_ID);

        // open DB and make queries on a bg thread
        new ShowBeerTask().execute(fId);
    }

    private void displayBeer() {
        Beer beer = fBeer;
        StarRating rating = new StarRating(0);

        setTitle(beer.getBrewery().getName() + " - " + beer.getName());

        TextView beerTitle = (TextView) findViewById(R.id.beer_name);
        beerTitle.setText(beer.getName());

        TextView beerDetails = (TextView) findViewById(R.id.beer_details);

        String details =
                getResources().getText(R.string.brewery) + ": " + beer.getBrewery().getName() + "\n" +
                        getResources().getText(R.string.abv) + ": " + beer.getAbv() + "%\n";
        beerDetails.setText(details);

        RatingBar ratingBar = ((RatingBar) findViewById(R.id.beer_rating));
        ratingBar.setRating(rating.getNumberOfStars());

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser)
                    rateBeer(new StarRating((int) rating));
            }
        });

        TextView beerNotesView = (TextView) findViewById(R.id.beer_notes);
        beerNotesView.setText(beer.getDescription());
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
            case R.id.clear_rating:
                rateBeer(new StarRating(0));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rateBeer(StarRating rating) {
        /*
        fBeerDatabase.rateBeer(fId, rating);
        fBeerWithRating = fBeerWithRating.rate(rating);
        */
        displayBeer();
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
}
