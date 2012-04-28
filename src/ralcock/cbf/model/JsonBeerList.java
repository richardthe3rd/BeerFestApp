package ralcock.cbf.model;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    private static final String STATUS = "status_text";
    private static final String IDENTIFIER = "id";

    private final InputStream fInputStream;
    private List<Beer> fBeerList;

    public JsonBeerList(final InputStream inputStream) throws IOException, JSONException {
        assert inputStream != null;
        fInputStream = inputStream;
    }

    public Iterator<Beer> iterator() {
        try {
            if (fBeerList == null) {
                JSONObject jsonObject = loadJson(fInputStream);
                fBeerList = makeBeerList(jsonObject);
            }
            return fBeerList.iterator();
        } catch (JSONException jsx) {
            throw new RuntimeException(jsx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        return new Beer(
                product.getString(IDENTIFIER),
                product.getString(NAME),
                (float) product.getDouble(ABV),
                product.getString(DESCRIPTION),
                product.has(STATUS) ? product.getString(STATUS) : "",
                brewery)
                ;
    }

    private Brewery makeBrewery(final JSONObject producer) throws JSONException {
        return new Brewery(
                producer.getString(IDENTIFIER),
                producer.getString(NAME),
                producer.getString(DESCRIPTION)
        );
    }

    private static JSONObject loadJson(final InputStream inputStream) throws IOException, JSONException {
        Log.i(TAG, "Loading beer list from input stream.");
        Reader reader = new InputStreamReader(inputStream);
        StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[0x10000];
        int read;
        do {
            read = reader.read(buffer);
            if (read > 0) {
                builder.append(buffer, 0, read);
            }
        } while (read >= 0);

        return new JSONObject(builder.toString());
    }
}
