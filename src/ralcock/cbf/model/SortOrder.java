package ralcock.cbf.model;

import java.io.Serializable;

@SuppressWarnings({"UnusedDeclaration"})
public enum SortOrder implements Serializable {

    BREWERY_NAME_ASC(Beer.BREWERY_FIELD, true, "by Brewery (A-Z)"),
    BREWERY_NAME_DESC(Beer.BREWERY_FIELD, false, "by Brewery (Z-A)"),

    BEER_NAME_ASC(Beer.NAME_FIELD, true, "by Beer (A-Z)"),
    BEER_NAME_DESC(Beer.NAME_FIELD, false, "by Beer (Z-A)"),

    BEER_ABV_ASC(Beer.ABV_FIELD, true, "by ABV (low to high)"),
    BEER_ABV_DESC(Beer.ABV_FIELD, false, "by ABV (high to low)");

    //BEER_RATING_DESC(BeerDatabase.BEER_RATING_COLUMN+" DESC",  "by Rating (high to low)"),
    //BEER_RATING_ASC(BeerDatabase.BEER_RATING_COLUMN+" ASC",  "by Rating (low to high)");

    private final String fColumnName;
    private final boolean fAscending;
    private final String fDescription;

    SortOrder(final String columnName, final boolean ascending, final String description) {
        fColumnName = columnName;
        fAscending = ascending;
        fDescription = description;
    }

    public String getDescription() {
        return fDescription;
    }

    public String toString() {
        return fDescription;
    }

    public String columnName() {
        return fColumnName;
    }

    public boolean ascending() {
        return fAscending;
    }
}
