package ralcock.cbf;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import ralcock.cbf.model.SortOrder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class AppPreferences {

    private static final String TAG = AppPreferences.class.getName();

    private static final String PREFERENCES_NAME = CamBeerFestApplication.class.getSimpleName();
    private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.BREWERY_NAME_ASC;

    private static final String SORT_ORDER_KEY = "sortOrder";
    private static final String FILTER_TEXT_KEY = "filterText";
    private static final String LAST_UPDATE_TIME_KEY = "lastUpdateTime";

    private final Context fContext;
    private static final String STYLES_TO_HIDE_KEY = "stylesToHide";

    public AppPreferences(final Context context) {
        fContext = context;
    }

    public void setSortOrder(final SortOrder sortOrder) {
        setPreference(SORT_ORDER_KEY, sortOrder.name());
    }

    public SortOrder getSortOrder() {
        String sortOrderName = getPreference(SORT_ORDER_KEY, DEFAULT_SORT_ORDER.name());
        return SortOrder.valueOf(sortOrderName);
    }

    public void setStylesToHide(Set<String> stylesToHide) {
        setPreference(STYLES_TO_HIDE_KEY, stylesToHide);
    }

    public Set<String> getStylesToHide() {
        return getPreference(STYLES_TO_HIDE_KEY, new HashSet<String>());
    }

    public void setFilterText(String filterText) {
        setPreference(FILTER_TEXT_KEY, filterText);
    }

    public String getFilterText() {
        return getPreference(FILTER_TEXT_KEY, "");
    }

    public Date getLastUpdateTime() {
        return getPreference(LAST_UPDATE_TIME_KEY, new Date(0));
    }

    private Date getPreference(final String key, final Date date) {
        SharedPreferences settings = getSharedPreferences();
        return new Date(settings.getLong(key, date.getTime()));
    }

    public void setLastUpdateTime(final Date date) {
        setPreference(LAST_UPDATE_TIME_KEY, date);
    }

    private void setPreference(final String key, final Date date) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, date.getTime());
        editor.commit();
    }

    private String getPreference(final String key, final String def) {
        SharedPreferences settings = getSharedPreferences();
        return settings.getString(key, def);
    }

    private void setPreference(final String key, final String value) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void setPreference(final String key, final Set<String> strings) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        JSONArray json = new JSONArray(strings);
        editor.putString(key, json.toString());
        editor.commit();
    }

    private Set<String> getPreference(final String key, final Set<String> strings) {
        SharedPreferences settings = getSharedPreferences();

        try {
            Set<String> out = new HashSet<String>();
            JSONArray json = new JSONArray(settings.getString(key, "[]"));
            for (int i = 0; i < json.length(); i++) {
                out.add(json.getString(i));
            }
            return out;
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
            return strings;
        }
    }

    private SharedPreferences getSharedPreferences() {
        return fContext.getSharedPreferences(PREFERENCES_NAME, 0);
    }

}
