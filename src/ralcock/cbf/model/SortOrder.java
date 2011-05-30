package ralcock.cbf.model;

import java.io.Serializable;

/**
* Created by IntelliJ IDEA.
* User: RichardAndCathy
* Date: 30/05/11
* Time: 23:20
* To change this template use File | Settings | File Templates.
*/
public enum SortOrder implements Serializable {
    BEER_NAME("beer_name", "beer"),
    BEER_ABV("beer_abv", "abv"),
    BEER_RATING("beer_rating", "rating"),
    BREWERY_NAME("brewery_name", "brewery");

    public String getColumnName() {
        return fColumnName;
    }

    public String getDescription() {
        return fDescription;
    }

    public boolean isAscending() {
        return fAscending;
    }

    public SortOrder reverse() {
        fAscending = !fAscending;
        return this;
    }

    private String fColumnName;
    private String fDescription;
    private boolean fAscending = true;

    SortOrder(String columnName, String msg) {
        fColumnName = columnName;
        fDescription = msg;
    }
}
