package ralcock.cbf.model;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class IterableJSONArray implements Iterable<JSONObject> {
    private final JSONArray fJsonArray;

    IterableJSONArray(final JSONArray jsonArray) {
        fJsonArray = jsonArray;
    }

    public Iterator<JSONObject> iterator() {
        return new JSONArrayIterator(fJsonArray);
    }

    private static final class JSONArrayIterator implements Iterator<JSONObject> {
        private final JSONArray fJsonArray;
        private int fIndex;

        private JSONArrayIterator(final JSONArray jsonArray) {
            fJsonArray = jsonArray;
            fIndex = 0;
        }

        public boolean hasNext() {
            return fIndex < fJsonArray.length();
        }

        public JSONObject next() {
            try {
                return fJsonArray.getJSONObject(fIndex++);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
