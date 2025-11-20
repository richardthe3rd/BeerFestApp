package ralcock.cbf.testutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Factory for creating test data for UpdateTask tests.
 */
public class TestDataFactory {

    /**
     * Creates valid beer list JSON with specified number of beers.
     * Format matches expected JsonBeerList structure:
     * {
     *   "producers": [{"name": "Brewery 0", "products": [{"name": "Beer 0", "abv": 4.0, ...}]}]
     * }
     */
    public static String createValidBeerJSON(final int count) {
        try {
            final JSONObject root = new JSONObject();
            final JSONArray producers = new JSONArray();

            for (int i = 0; i < count; i++) {
                final JSONObject producer = new JSONObject();
                producer.put("name", "Brewery " + i);
                producer.put("location", "Location " + i);

                final JSONArray products = new JSONArray();
                final JSONObject product = new JSONObject();
                product.put("id", String.valueOf(i));
                product.put("name", "Beer " + i);
                product.put("abv", String.valueOf(4.0 + (i % 10) * 0.5));
                product.put("notes", "Test beer " + i);
                product.put("style", "Test Style");
                product.put("status_text", "Available");
                product.put("dispense", "Cask");

                products.put(product);
                producer.put("products", products);
                producers.put(producer);
            }

            root.put("producers", producers);
            return root.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Failed to create test JSON", e);
        }
    }

    /**
     * Creates malformed JSON that will fail parsing.
     */
    public static String createMalformedBeerJSON() {
        return "{invalid json}";
    }

    /**
     * Creates empty beer list JSON (no beers).
     */
    public static String createEmptyBeerJSON() {
        try {
            final JSONObject root = new JSONObject();
            root.put("producers", new JSONArray());
            return root.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Failed to create empty JSON", e);
        }
    }
}
