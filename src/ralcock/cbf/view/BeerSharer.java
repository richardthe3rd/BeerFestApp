package ralcock.cbf.view;

import android.content.Context;
import android.content.Intent;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;

public final class BeerSharer {

    private final Context fContext;

    public BeerSharer(final Context context) {
        fContext = context;
    }

    public void shareBeer(final Beer beer) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String extraSubject = fContext.getResources().getString(R.string.share_this_beer_subject);
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);

        String extraText;
        int rating = beer.getRating();
        if (rating > 0) {
            extraText = fContext.getResources().getString(R.string.share_this_rated_beer_text,
                    rating, beer.getBrewery().getName(), beer.getName());
        } else {
            extraText = fContext.getResources().getString(R.string.share_this_beer_text,
                    beer.getBrewery().getName(), beer.getName());
        }

        intent.putExtra(Intent.EXTRA_TEXT, extraText);

        String title = fContext.getResources().getString(R.string.share_this_beer_title);
        fContext.startActivity(Intent.createChooser(intent, title));

    }
}
