package ralcock.cbf.service;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * InputStream that combines JSON beer lists from multiple URLs into a single stream.
 * Each URL should return a JSON object with a "producers" array.
 * The combined result merges all producers into a single JSON object.
 */
public class MultiUrlInputStream extends InputStream {
    private static final String TAG = MultiUrlInputStream.class.getName();
    private static final String PRODUCERS = "producers";

    private final ByteArrayInputStream fCombinedStream;

    public MultiUrlInputStream(final String[] urls) throws IOException {
        JSONArray combinedProducers = new JSONArray();

        for (String urlString : urls) {
            try {
                Log.i(TAG, "Fetching from: " + urlString);
                URL url = new URL(urlString);
                InputStream inputStream = url.openStream();
                String jsonString = readEntireStream(inputStream);
                inputStream.close();

                JSONObject json = new JSONObject(jsonString);
                if (json.has(PRODUCERS)) {
                    JSONArray producers = json.getJSONArray(PRODUCERS);
                    for (int i = 0; i < producers.length(); i++) {
                        combinedProducers.put(producers.get(i));
                    }
                    Log.i(TAG, "Added " + producers.length() + " producers from " + urlString);
                }
            } catch (JSONException e) {
                Log.w(TAG, "Failed to parse JSON from " + urlString, e);
            } catch (IOException e) {
                Log.w(TAG, "Failed to fetch from " + urlString + ", continuing with other URLs", e);
            }
        }

        JSONObject combinedJson = new JSONObject();
        try {
            combinedJson.put(PRODUCERS, combinedProducers);
        } catch (JSONException e) {
            throw new IOException("Failed to create combined JSON", e);
        }

        byte[] bytes = combinedJson.toString().getBytes(StandardCharsets.UTF_8);
        fCombinedStream = new ByteArrayInputStream(bytes);
        Log.i(TAG, "Combined " + combinedProducers.length() + " total producers");
    }

    private static String readEntireStream(final InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[0x10000];
        int read;
        do {
            read = reader.read(buffer);
            if (read > 0) {
                builder.append(buffer, 0, read);
            }
        } while (read >= 0);
        return builder.toString();
    }

    @Override
    public int read() throws IOException {
        return fCombinedStream.read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return fCombinedStream.read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return fCombinedStream.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        return fCombinedStream.available();
    }

    @Override
    public void close() throws IOException {
        fCombinedStream.close();
    }
}
