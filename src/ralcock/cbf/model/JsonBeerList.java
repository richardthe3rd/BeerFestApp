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
import java.util.Vector;

public class JsonBeerList implements Iterable<Beer> {

    private static final String TAG = JsonBeerList.class.getSimpleName();

    private static final String JSON_BREWERY = "brewery";
    private static final String JSON_NAME = "name";
    private static final String JSON_NOTES = "notes";
    private static final String JSON_ABV = "abv";

    private Vector<Beer> fBeers = new Vector<Beer>();
    
    public JsonBeerList(InputStream inputStream) throws IOException, JSONException {
        JSONArray jsonArray = loadJson(inputStream);
        for(int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONObject jsonBreweryObject = jsonObject.getJSONObject(JSON_BREWERY);
            Beer beer = createBeer(
                    jsonBreweryObject.getString(JSON_NAME),
                    jsonBreweryObject.getString(JSON_NOTES),
                    jsonObject.getString(JSON_NAME),
                    jsonObject.getString(JSON_ABV),
                    jsonObject.getString(JSON_NOTES)
            );
            fBeers.add(beer);
        }
    }

    public Iterator<Beer> iterator() {
        return fBeers.iterator();
    }

    private Beer createBeer(final String breweryName, final String breweryNotes,
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

    private JSONArray loadJson(InputStream inputStream) throws IOException, JSONException {
        JSONArray jsonArray;
        Log.i(TAG, "Loading beer list from beers.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }
        String jsonString = builder.toString();
        jsonArray = new JSONArray(jsonString);
        return jsonArray;
    }
}
