package ralcock.cbf.model;

import android.util.Log;
import android.util.TimingLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BeerListLoader {

    public interface BeerHandler {
        void handleBeer(Beer beer);
    }

    private static final String TAG = BeerListLoader.class.getSimpleName();

    private static final String JSON_BREWERY = "brewery";
    private static final String JSON_NAME = "name";
    private static final String JSON_NOTES = "notes";
    private static final String JSON_ABV = "abv";

    public void loadBeers(InputStream inputStream, BeerHandler handler) throws IOException, JSONException {
        TimingLogger loadTimer = new TimingLogger(TAG, "loadBeerList");
        JSONArray jsonArray = loadJson(inputStream);
        loadTimer.addSplit("loaded json data");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jobject = jsonArray.getJSONObject(i);
            JSONObject jsonBreweryObject = jobject.getJSONObject(JSON_BREWERY);
            Beer beer = createBeer(
                    jsonBreweryObject.getString(JSON_NAME),
                    jsonBreweryObject.getString(JSON_NOTES),
                    jobject.getString(JSON_NAME),
                    jobject.getString(JSON_ABV),
                    jobject.getString(JSON_NOTES)
            );
            handler.handleBeer(beer);
        }
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

    private Beer createBeer(String breweryName, String breweryNotes,
                            String beerName, String abvString, String beerNotes) {
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
