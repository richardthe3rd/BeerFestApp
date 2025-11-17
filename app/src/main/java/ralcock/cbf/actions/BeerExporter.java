package ralcock.cbf.actions;

import android.content.Context;
import android.content.Intent;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class BeerExporter {
    private final Context fContext;

    public BeerExporter(final Context context) {
        fContext = context;
    }

    public void export(final List<Beer> ratedBeers) throws IOException {
        Intent intent = makeExportIntent(ratedBeers);
        fContext.startActivity(Intent.createChooser(intent, "Send beer ratings as CSV"));
    }

    public Intent makeExportIntent(final List<Beer> ratedBeers) {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format(Locale.US, "%s, %s, %s, %s\n", "Beer", "Brewery", "Style", "Rating"));

        for (Beer b : ratedBeers) {
            builder.append(String.format("\"%s\", \"%s\", \"%s\", %d\n", b.getName(), b.getBrewery().getName(), b.getStyle(), b.getRating()));
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String festivalName = fContext.getResources().getString(R.string.festival_name);
        String subject = String.format("Beers from %s", festivalName);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());

        return intent;
    }
}
