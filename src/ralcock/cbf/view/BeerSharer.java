package ralcock.cbf.view;

import android.content.Context;
import android.content.Intent;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerWithRating;

public final class BeerSharer {

    private final Context fContext;

    public BeerSharer(final Context context) {
        fContext = context;
    }

    public void shareBeer(final BeerWithRating beerWithRating) {
        Beer beer = beerWithRating.getBeer();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String extraSubject = fContext.getResources().getString(R.string.share_this_beer_subject);
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);

        String ratingString = "";
        int rating = beerWithRating.getRating().getNumberOfStars();
        if (rating > 0) {ratingString = String.format("%d/5", rating);}

        String extraText = fContext.getResources().getString(R.string.share_this_beer_text,
                beer.getBrewery().getName(), beer.getName(), ratingString);
        intent.putExtra(Intent.EXTRA_TEXT, extraText);

        String title = fContext.getResources().getString(R.string.share_this_beer_title);
        fContext.startActivity(Intent.createChooser(intent, title));

    }
}
