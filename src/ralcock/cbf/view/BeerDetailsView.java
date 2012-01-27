package ralcock.cbf.view;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabase;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerWithRating;
import ralcock.cbf.model.StarRating;

public final class BeerDetailsView extends Activity {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = BeerDetailsView.class.getName();

    public static final String EXTRA_BEER_ID = "BEER";

    private BeerDatabase fBeerDatabase;
    private BeerWithRating fBeerWithRating;
    private long fId;
    private BeerSharer fBeerSharer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fId = getIntent().getExtras().getLong(EXTRA_BEER_ID);
        fBeerSharer = new BeerSharer(this);
        // open DB and make queries on a bg thread
        new ShowBeerTask().execute(fId);
    }

    private void displayBeer() {
        Beer beer = fBeerWithRating.getBeer();
        StarRating rating = fBeerWithRating.getRating();

        setTitle(beer.getBrewery().getName() + " - " + beer.getName());

        TextView beerTitle = (TextView)findViewById(R.id.beer_name);
        beerTitle.setText(beer.getName());

        TextView beerDetails = (TextView)findViewById(R.id.beer_details);

        String details =
                getResources().getText(R.string.brewery) + ": " + beer.getBrewery().getName() + "\n" +
                getResources().getText(R.string.abv)     + ": " + beer.getAbv()        + "%\n";
        beerDetails.setText(details);

        RatingBar ratingBar = ((RatingBar)findViewById(R.id.beer_rating));
        ratingBar.setRating(rating.getNumberOfStars());

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser)
                    rateBeer( new StarRating((int)rating) );
            }
        });

        TextView beerNotesView = (TextView)findViewById(R.id.beer_notes);
        beerNotesView.setText(beer.getNotes());
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
                fBeerSharer.shareBeer(fBeerWithRating);
                return true;
            case R.id.clear_rating:
                rateBeer(new StarRating(0));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rateBeer(StarRating rating) {
        fBeerDatabase.rateBeer(fId, rating);
        fBeerWithRating = fBeerWithRating.rate(rating);
        displayBeer();
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Called from beer_details_view.xml
    public void shareBeer(View button) {
        fBeerSharer.shareBeer(fBeerWithRating);
    }

    @Override
    protected void onDestroy() {
        fBeerDatabase.close();
        super.onDestroy();
    }

    private class ShowBeerTask extends AsyncTask<Long, Void, BeerWithRating>{
        @Override
        protected void onPostExecute(BeerWithRating beerWithRating) {
            setContentView(R.layout.beer_details_view);
            fBeerWithRating = beerWithRating;
            displayBeer();
        }

        @Override
        protected BeerWithRating doInBackground(Long... ids) {
            Context context = BeerDetailsView.this;
            //inputStream = context.getAssets().open("beers.json");
            //JsonBeerList jsonBeerList = new JsonBeerList(inputStream);
            BeerDatabaseHelper databaseHelper = new BeerDatabaseHelper(context);
            fBeerDatabase = new BeerDatabase(databaseHelper);
            return fBeerDatabase.getBeerForId(ids[0]);
        }
    }
}
