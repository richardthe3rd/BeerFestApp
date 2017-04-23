package ralcock.cbf.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class JsonBeerList implements Iterable<Beer> {

    private static final String TAG = JsonBeerList.class.getName();

    private static final String PRODUCERS = "producers";
    private static final String PRODUCTS = "products";

    private static final String NAME = "name";
    private static final String DESCRIPTION = "notes";
    private static final String ABV = "abv";
    private static final String STYLE = "style";
    private static final String STATUS = "status_text";
    private static final String IDENTIFIER = "id";

    private List<Beer> fBeerList;

    public JsonBeerList(final String jsonString) throws JSONException {
        fBeerList = makeBeerList(new JSONObject(jsonString));
    }

    public Iterator<Beer> iterator() {
        return fBeerList.iterator();
    }

    public int size() {
        return fBeerList.size();
    }

    private List<Beer> makeBeerList(final JSONObject json) throws JSONException {
        Vector<Beer> beers = new Vector<Beer>();

        IterableJSONArray producers = new IterableJSONArray(json.getJSONArray(PRODUCERS));
        for (JSONObject producer : producers) {
            Brewery brewery = makeBrewery(producer);
            IterableJSONArray produce = new IterableJSONArray(producer.getJSONArray(PRODUCTS));
            for (JSONObject product : produce) {
                Beer beer = makeBeer(brewery, product);
                beers.add(beer);
            }
        }
        return beers;
    }

    private Beer makeBeer(final Brewery brewery, final JSONObject product) throws JSONException {
        return new BeerBuilder()
            .fromBrewery(brewery)
            .withFestivalId(product.getString(IDENTIFIER))
            .called(product.isNull(NAME) ? "" : product.getString(NAME))
            .withDescription(product.isNull(DESCRIPTION) ? "" : product.getString(DESCRIPTION))
            .withABV(product.isNull(ABV)       ? Float.NaN : (float)product.getDouble(ABV))
            .withStyle(product.isNull(STYLE)   ? "Unknown" : product.getString(STYLE))
            .withStatus(product.isNull(STATUS) ? "Unknown" : product.getString(STATUS))
            .build();
    }

    private Brewery makeBrewery(final JSONObject producer) throws JSONException {
        return new Brewery(
            producer.getString(IDENTIFIER),
            producer.getString(NAME),
            producer.getString(DESCRIPTION)
        );
    }
}
