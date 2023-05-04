package ralcock.cbf;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.StatusToShow;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class AppPreferences {

    private static final String TAG = AppPreferences.class.getName();

    private static final String PREFERENCES_NAME = CamBeerFestApplication.class.getSimpleName();

    private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.BREWERY_NAME_ASC;
    private static final boolean DEFAULT_HIDE_UNAVAILABLE = false;

    private static final String SORT_ORDER_KEY = "sortOrder";
    private static final String FILTER_TEXT_KEY = "filterText";
    private static final String NEXT_UPDATE_TIME_KEY = "lastUpdateTime";
    private static final String HIDE_UNAVAILABLE_KEY = "hideUnavailable";
    private static final String STYLES_TO_HIDE_KEY = "stylesToHide";
    private static final String LAST_UPDATE_MD5_KEY = "lastUpdateMD5";

    private final Context fContext;

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

    private Date getPreference(final String key, final Date date) {
        SharedPreferences settings = getSharedPreferences();
        return new Date(settings.getLong(key, date.getTime()));
    }

    private void setPreference(final String key, final Date date) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, date.getTime());
        editor.apply();
    }

    private String getPreference(final String key, final String def) {
        SharedPreferences settings = getSharedPreferences();
        return settings.getString(key, def);
    }

    private boolean getPreference(final String key, final boolean def) {
        SharedPreferences settings = getSharedPreferences();
        return settings.getBoolean(key, def);
    }

    private void setPreference(final String key, final String value) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void setPreference(final String key, final Set<String> strings) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        JSONArray json = new JSONArray(strings);
        editor.putString(key, json.toString());
        editor.apply();
    }

    private void setPreference(final String key, final boolean value) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
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

    public Date getNextUpdateTime() {
        return getPreference(NEXT_UPDATE_TIME_KEY, new Date(0));
    }

    public void setNextUpdateTime(Date nextUpdateTime) {
        setPreference(NEXT_UPDATE_TIME_KEY, nextUpdateTime);
    }

    public void setHideUnavailableBeers(final boolean hide) {
        setPreference(HIDE_UNAVAILABLE_KEY, hide);
    }

    public boolean getHideUnavailableBeers() {
        return getPreference(HIDE_UNAVAILABLE_KEY, DEFAULT_HIDE_UNAVAILABLE);
    }

    public void setLastUpdateMD5(final String md5) {
        setPreference(LAST_UPDATE_MD5_KEY, md5);
    }

    public String getLastUpdateMD5() {
        return getPreference(LAST_UPDATE_MD5_KEY, "");
    }

    public StatusToShow getStatusToShow() {
        boolean hideUnavailableBeers = getHideUnavailableBeers();
        if (hideUnavailableBeers) {
            return StatusToShow.AVAILABLE_ONLY;
        } else {
            return StatusToShow.ALL;
        }
    }

    public BeerList.Config getBeerListConfig() {
        return new BeerList.Config(
                getSortOrder(),
                getFilterText(),
                getStylesToHide(),
                getStatusToShow());
    }
}
