package ralcock.cbf.actions;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;

public final class BeerSharer {

    private final Context fContext;

    public BeerSharer(final Context context) {
        fContext = context;
    }

    public void shareBeer(final Beer beer) {
        Intent intent = makeShareIntent(beer);

        String title = fContext.getResources().getString(R.string.share_this_beer_title);
        fContext.startActivity(Intent.createChooser(intent, title));
    }

    public Intent makeShareIntent(final Beer beer) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        final Resources resources = fContext.getResources();

        String festivalName = resources.getString(R.string.festival_name);
        String extraSubject =
                resources.getString(R.string.share_intent_subject, "beer", festivalName);
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);

        String extraText = makeExtraText(resources, beer);

        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        return intent;
    }

    private String makeExtraText(final Resources resources, final Beer beer) {
        String stars = beer.getNumberOfStars().toFancyString();
        String extraText =
                resources.getString(
                        R.string.share_intent_text, beer.getBrewery().getName(), beer.getName());
        String hashTag = resources.getString(R.string.festival_hashtag);
        return String.format("%s %s #%s", extraText, stars, hashTag);
    }
}
