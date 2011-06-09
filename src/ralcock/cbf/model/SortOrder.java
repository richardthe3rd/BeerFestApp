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
    BEER_NAME(BeerDatabase.BEER_NAME_COLUMN, "beer"),
    BEER_ABV(BeerDatabase.BEER_ABV_COLUMN, "abv"),
    BEER_RATING(BeerDatabase.BEER_RATING_COLUMN, "rating"),
    BREWERY_NAME(BeerDatabase.BREWERY_NAME_COLUMN, "brewery");

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
