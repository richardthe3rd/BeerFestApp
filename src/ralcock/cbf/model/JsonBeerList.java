package ralcock.cbf.model;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class JsonBeerList implements Iterable<Beer> {

    private static final String TAG = JsonBeerList.class.getName();

    private static final String JSON_BREWERY = "brewery";
    private static final String JSON_NAME = "name";
    private static final String JSON_NOTES = "notes";
    private static final String JSON_ABV = "abv";

    private final InputStream fInputStream;
    private JSONArray fJsonArray;

    public JsonBeerList(final InputStream inputStream) throws IOException, JSONException {
        assert inputStream!=null;
        fInputStream = inputStream;
    }

    public Iterator<Beer> iterator() {
        try {
            if (fJsonArray == null) {
                fJsonArray = loadJson(fInputStream);
            }
            return new JSONArrayBeerIterator(fJsonArray);
        } catch (JSONException jsx) {
            throw new RuntimeException(jsx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class JSONArrayBeerIterator implements Iterator<Beer>{

        private final JSONArray fJsonArray;
        private int index = 0;

        JSONArrayBeerIterator(final JSONArray jsonArray) throws IOException, JSONException {
            fJsonArray = jsonArray;
        }

        public boolean hasNext() {
            return index < fJsonArray.length();
        }

        public Beer next() {
            try {
                JSONObject jsonObject = fJsonArray.getJSONObject(index++);
                JSONObject jsonBreweryObject = jsonObject.getJSONObject(JSON_BREWERY);
                Beer beer = createBeer(
                        jsonBreweryObject.getString(JSON_NAME),
                        jsonBreweryObject.getString(JSON_NOTES),
                        jsonObject.getString(JSON_NAME),
                        jsonObject.getString(JSON_ABV),
                        jsonObject.getString(JSON_NOTES)
                );
                return beer;
            } catch (JSONException jsx) {
                throw new RuntimeException(jsx);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private static Beer createBeer(final String breweryName, final String breweryNotes,
                                       final String beerName, final String abvString, final String beerNotes) {
            float abv;
            try {
                abv = Float.parseFloat(abvString);
            } catch (NumberFormatException nfx) {
                abv = Float.NaN;
            }
            Brewery brewery = new Brewery(breweryName, breweryNotes);
            Beer beer = new Beer(brewery, beerName, abv, beerNotes);
            Log.d(TAG, "Loaded " + beer);
            return beer;
        }

    }


    private static JSONArray loadJson(InputStream inputStream) throws IOException, JSONException {
        Log.i(TAG, "Loading beer list from input stream.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }
        String jsonString = builder.toString();
        JSONArray jsonArray = new JSONArray(jsonString);
        return jsonArray;
    }
}
