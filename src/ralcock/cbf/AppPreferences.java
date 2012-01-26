package ralcock.cbf;

import android.content.Context;
import android.content.SharedPreferences;
import ralcock.cbf.model.SortOrder;

public final class AppPreferences {

    private static final String PREFERENCES_NAME = CamBeerFestApplication.class.getSimpleName();
    private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.BREWERY_NAME_ASC;
    private static final String SORT_ORDER_KEY = "sortOrder";
    private static final String FILTER_TEXT_KEY = "filterText";

    private final Context fContext;

    public AppPreferences(final Context context) {
        fContext = context;
    }

    public void setSortOrder(final SortOrder sortOrder){
        setPreference(SORT_ORDER_KEY, sortOrder.name());
    }

    public SortOrder getSortOrder() {
        String sortOrderName = getPreference(SORT_ORDER_KEY, DEFAULT_SORT_ORDER.name());
        return SortOrder.valueOf(sortOrderName);
    }
    
    public void setFilterText(String filterText){
        setPreference(FILTER_TEXT_KEY, filterText);
    }
    
    public String getFilterText(){
        return getPreference(FILTER_TEXT_KEY, "");
    }

    private String getPreference(final String key, final String def){
        SharedPreferences settings = getSharedPreferences();
        return settings.getString(key, def);
    }
    
    private void setPreference(final String key, final String value) {
        SharedPreferences settings = getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private SharedPreferences getSharedPreferences() {
        return fContext.getSharedPreferences(PREFERENCES_NAME, 0);
    }

}
