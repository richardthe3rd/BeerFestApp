package ralcock.cbf;

import android.content.Context;
import android.content.Intent;
import ralcock.cbf.model.Beer;

import java.io.IOException;
import java.util.List;

public final class BeerExporter {
    private final Context fContext;

    public BeerExporter(final Context context) {
        fContext = context;
    }

    public void export(final List<Beer> ratedBeers) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s, %s, %s, %s\n", "Beer", "Brewery", "Style", "Rating"));

        for (Beer b : ratedBeers) {
            builder.append(String.format("\"%s\", \"%s\", \"%s\", %d\n", b.getName(), b.getBrewery().getName(), b.getStyle(), b.getRating()));
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_SUBJECT, "Beers from cbf39");
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());

        fContext.startActivity(Intent.createChooser(intent, "Send beer ratings as CSV"));
    }
}
