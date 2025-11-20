package ralcock.cbf.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class IterableJSONArrayTest
{
    @Test
    public void iteratorOverEmptyArray() {
        JSONArray jsonArray = new JSONArray();
        IterableJSONArray iterable = new IterableJSONArray(jsonArray);

        Iterator<JSONObject> iterator = iterable.iterator();
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void iteratorOverSingleElement() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("key", "value");
        jsonArray.put(obj);

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        assertThat(iterator.hasNext(), is(true));
        JSONObject result = iterator.next();
        assertThat(result.getString("key"), equalTo("value"));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void iteratorOverMultipleElements() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put("id", 1);
        JSONObject obj2 = new JSONObject();
        obj2.put("id", 2);
        JSONObject obj3 = new JSONObject();
        obj3.put("id", 3);

        jsonArray.put(obj1);
        jsonArray.put(obj2);
        jsonArray.put(obj3);

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getInt("id"), equalTo(1));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getInt("id"), equalTo(2));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getInt("id"), equalTo(3));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void hasNextDoesNotAdvance() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("key", "value");
        jsonArray.put(obj);

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.hasNext(), is(true));

        JSONObject result = iterator.next();
        assertThat(result.getString("key"), equalTo("value"));
    }

    @Test
    public void nextIncrementsPosition() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < 5; i++) {
            JSONObject obj = new JSONObject();
            obj.put("index", i);
            jsonArray.put(obj);
        }

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        for (int i = 0; i < 5; i++) {
            assertThat(iterator.next().getInt("index"), equalTo(i));
        }
        assertThat(iterator.hasNext(), is(false));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeThrowsUnsupportedOperationException() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject());

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        iterator.next();
        iterator.remove();
    }

    @Test
    public void forEachLoop() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < 3; i++) {
            JSONObject obj = new JSONObject();
            obj.put("id", i);
            jsonArray.put(obj);
        }

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);

        int count = 0;
        for (JSONObject obj : iterable) {
            assertThat(obj.getInt("id"), equalTo(count));
            count++;
        }
        assertThat(count, equalTo(3));
    }

    @Test(expected = RuntimeException.class)
    public void nextWithNonObjectElement() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("not a json object");

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        assertThat(iterator.hasNext(), is(true));
        iterator.next(); // Should throw RuntimeException wrapping JSONException
    }

    @Test(expected = RuntimeException.class)
    public void nextWhenHasNextIsFalse() {
        JSONArray jsonArray = new JSONArray();
        IterableJSONArray iterable = new IterableJSONArray(jsonArray);
        Iterator<JSONObject> iterator = iterable.iterator();

        assertThat(iterator.hasNext(), is(false));
        iterator.next(); // Should throw exception
    }

    @Test
    public void multipleIterators() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put("id", 1);
        JSONObject obj2 = new JSONObject();
        obj2.put("id", 2);
        jsonArray.put(obj1);
        jsonArray.put(obj2);

        IterableJSONArray iterable = new IterableJSONArray(jsonArray);

        Iterator<JSONObject> iterator1 = iterable.iterator();
        Iterator<JSONObject> iterator2 = iterable.iterator();

        assertThat(iterator1.next().getInt("id"), equalTo(1));
        assertThat(iterator2.next().getInt("id"), equalTo(1));
        assertThat(iterator1.next().getInt("id"), equalTo(2));
        assertThat(iterator2.next().getInt("id"), equalTo(2));
    }
}
