package ralcock.cbf.model;

import java.io.Serializable;

@SuppressWarnings({"UnusedDeclaration"})
public enum SortOrder implements Serializable {

    BREWERY_NAME_ASC(BeerDatabase.BREWERY_NAME_COLUMN+" ASC", "by Brewery (A-Z)"),
    BREWERY_NAME_DESC(BeerDatabase.BREWERY_NAME_COLUMN+" DESC", "by Brewery (Z-A)"),

    BEER_NAME_ASC(BeerDatabase.BEER_NAME_COLUMN+" ASC", "by Beer (A-Z)"),
    BEER_NAME_DESC(BeerDatabase.BEER_NAME_COLUMN+" DESC", "by Beer (Z-A)"),

    BEER_ABV_DESC(BeerDatabase.BEER_ABV_COLUMN+" DESC", "by ABV (high to low)"),
    BEER_ABV_ASC(BeerDatabase.BEER_ABV_COLUMN+" ASC", "by ABV (low to high)"),

    BEER_RATING_DESC(BeerDatabase.BEER_RATING_COLUMN+" DESC",  "by Rating (high to low)"),
    BEER_RATING_ASC(BeerDatabase.BEER_RATING_COLUMN+" ASC",  "by Rating (low to high)");

    private final String fOrderByClause;

    private final String fDescription;

    SortOrder(final String orderByClause, final String description) {
        fOrderByClause = orderByClause;
        fDescription = description;
    }

    public String getOrderByClause() {
        return fOrderByClause;
    }

    public String getDescription() {
        return fDescription;
    }

    public String toString() {
        return fDescription;
    }

}
