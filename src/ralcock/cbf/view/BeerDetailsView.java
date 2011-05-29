package ralcock.cbf.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;
import ralcock.cbf.model.RatingDatabase;

public class BeerDetailsView extends Activity {
    public static final String EXTRA_BEER = "BEER";
    public static final String EXTRA_BEER_POSITION = "POSITION";

    public static final int RESULT_NOT_MODIFIED = 400;
    public static final int RESULT_MODIFIED = 800;

    private RatingDatabase fRatingsDatabase;
    private int fPosition;
    private Beer fBeer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fRatingsDatabase = new RatingDatabase(getApplicationContext());

        setContentView(R.layout.beer_details_view);

        fPosition = getIntent().getExtras().getInt(EXTRA_BEER_POSITION);
        fBeer = (Beer)getIntent().getExtras().getSerializable(EXTRA_BEER);
        fBeer.setContext(getApplicationContext());

        setResult(RESULT_NOT_MODIFIED, null);
        displayBeer();
    }

    private void displayBeer() {
        setTitle(fBeer.getBrewery().getName() + " - " + fBeer.getName());

        TextView beerTitle = (TextView)findViewById(R.id.beer_name);
        beerTitle.setText(fBeer.getName());

        StarRating rating = fRatingsDatabase.getRatingForBeer(fBeer);

        TextView beerDetails = (TextView)findViewById(R.id.beer_details);

        String details =
                getResources().getText(R.string.brewery) + ": " + fBeer.getBrewery().getName() + "\n" +
                getResources().getText(R.string.abv)     + ": " + fBeer.getAbv()        + "%\n";
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
        beerNotesView.setText(fBeer.getNotes());
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
                displayBeer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rateBeer(StarRating rating) {
        fBeer.setRating(rating);
        Intent data = new Intent();
        data.putExtra(EXTRA_BEER, fBeer);
        data.putExtra(EXTRA_BEER_POSITION, fPosition);
        setResult(RESULT_MODIFIED, data);
        displayBeer();
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Called from beer_details_view.xml
    public void shareBeer(View button) {
        shareBeer();
    }

    // TODO: This is copy of same method in CamBeerFestApp
    private void shareBeer() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String extraSubject = getResources().getString(R.string.share_this_beer_subject);
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);

        String extraText = getResources().getString(R.string.share_this_beer_text, fBeer.getBrewery().getName(), fBeer.getName());
        intent.putExtra(Intent.EXTRA_TEXT, extraText);

        String title = getResources().getString(R.string.share_this_beer_title);
        startActivity(Intent.createChooser(intent, title) );
    }

}
