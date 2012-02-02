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
import java.util.List;
import java.util.Vector;

public class JsonBeerList implements Iterable<Beer> {

    private static final String TAG = JsonBeerList.class.getName();

    private static final String PRODUCERS = "producers";
    private static final String NAME = "name";
    private static final String NOTES = "notes";
    private static final String PRODUCE = "produce";
    private static final String ABV = "abv";

    private final InputStream fInputStream;
    private List<Beer> fBeerList;

    public JsonBeerList(final InputStream inputStream) throws IOException, JSONException {
        assert inputStream!=null;
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
        JSONArray producers = json.getJSONArray(PRODUCERS);
        for(int producerIndex=0; producerIndex<producers.length(); producerIndex++){
            JSONObject producer = producers.getJSONObject(producerIndex);
            Brewery brewery = new Brewery(producer.getString(NAME), producer.getString(NOTES));
            JSONArray produce = producer.getJSONArray(PRODUCE);
            for(int produceIndex=0; produceIndex<produce.length(); produceIndex++){
                JSONObject product = produce.getJSONObject(produceIndex);
                Beer beer = new Beer(brewery,
                                product.getString(NAME),
                                (float)product.getDouble(ABV),
                                product.getString(NOTES));
                
                beers.add(beer);
            }
            
        }
        return beers;
    }


    private static JSONObject loadJson(InputStream inputStream) throws IOException, JSONException {
        Log.i(TAG, "Loading beer list from input stream.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }
        String jsonString = builder.toString();
        return new JSONObject(jsonString);
    }
}
