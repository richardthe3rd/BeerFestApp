package ralcock.cbf.view;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;
import ralcock.cbf.model.*;

// TODO: Too many database queries
// TODO: Hold a BeerWithRating instead of fId?
// TODO: Return a BeerWithRating single db query
public class BeerDetailsView extends Activity {
    public static final String EXTRA_BEER_ID = "BEER";

    private BeerDatabase fBeerDatabase;
    private long fId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fId = getIntent().getExtras().getLong(EXTRA_BEER_ID);

        // open DB and make queries on a bg thread
        new ShowBeerTask().execute();

    }

    private void displayBeer(Beer beer, StarRating rating) {

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
                shareBeer();
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
        Beer beer = fBeerDatabase.getBeerForId(fId);
        displayBeer(beer, rating);
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Called from beer_details_view.xml
    public void shareBeer(View button) {
        shareBeer();
    }

    // TODO: This is copy of same method in CamBeerFestApp
    private void shareBeer() {
        // todo
        Beer beer = fBeerDatabase.getBeerForId(fId);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String extraSubject = getResources().getString(R.string.share_this_beer_subject);
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);

        String extraText = getResources().getString(R.string.share_this_beer_text, beer.getBrewery().getName(), beer.getName());
        intent.putExtra(Intent.EXTRA_TEXT, extraText);

        String title = getResources().getString(R.string.share_this_beer_title);
        startActivity(Intent.createChooser(intent, title) );
    }

    @Override
    protected void onDestroy() {
        fBeerDatabase.close();
        super.onDestroy();
    }

    private class ShowBeerTask extends AsyncTask<Void, Void, BeerWithRating>{
        @Override
        protected void onPostExecute(BeerWithRating beerWithRating) {
            setContentView(R.layout.beer_details_view);
            displayBeer(beerWithRating.getBeer(), beerWithRating.getRating());
        }

        @Override
        protected BeerWithRating doInBackground(Void... voids) {
            fBeerDatabase = new BeerDatabase(getApplicationContext());
            Beer beer = fBeerDatabase.getBeerForId(fId);
            StarRating rating = fBeerDatabase.getRatingForBeer(fId);
            return new BeerWithRating(beer, rating);
        }
    }
}
