package ralcock.cbf.model;

import android.util.Log;
import android.util.TimingLogger;
import android.widget.ArrayAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class BeerListLoader {

    private static final String TAG = BeerListLoader.class.getSimpleName();

    private Vector<Beer> fList;
    private static final String JSON_BREWERY = "brewery";
    private static final String JSON_NAME = "name";
    private static final String JSON_NOTES = "notes";
    private static final String JSON_ABV = "abv";
    private final InputStream fInputStream;

    public BeerListLoader(InputStream inputStream) {
        fInputStream = inputStream;
    }

    public synchronized Vector<Beer> getBeerList() {
        if (fList == null) {
            try {
                fList = loadBeerList();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return fList;
    }

    private Vector<Beer> loadBeerList() throws JSONException, IOException {
        TimingLogger loadTimer = new TimingLogger(TAG, "loadBeerList");
        JSONArray jsonArray = loadJson();

        loadTimer.addSplit("loaded json data");

        Vector<Beer> beers = new Vector<Beer>();
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
            beers.add(beer);
        }

        loadTimer.addSplit("Created list of Beers");
        loadTimer.dumpToLog();
        return beers;
    }

    private JSONArray loadJson() throws IOException, JSONException {
        JSONArray jsonArray;
        Log.i(TAG, "Loading beer list from beers.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStream));
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


    public void loadBeerList(ArrayAdapter<Beer> listAdapter) throws IOException, JSONException {
        JSONArray jsonArray = loadJson();
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

        }
    }
}
