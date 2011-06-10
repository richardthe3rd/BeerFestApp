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
    BREWERY_NAME_ASC(BeerDatabase.BREWERY_NAME_COLUMN+" ASC", "by brewery (A-Z)"),
    BREWERY_NAME_DESC(BeerDatabase.BREWERY_NAME_COLUMN+" DESC", "by brewery (Z-A)"),

    BEER_NAME_ASC(BeerDatabase.BEER_NAME_COLUMN+" ASC", "by beer (A-Z)"),
    BEER_NAME_DESC(BeerDatabase.BEER_NAME_COLUMN+" DESC", "by beer (Z-A)"),

    BEER_ABV_DESC(BeerDatabase.BEER_ABV_COLUMN+" DeSC", "by ABV (high to low)"),
    BEER_ABV_ASC(BeerDatabase.BEER_ABV_COLUMN+" ASC", "by ABV (low to high)"),

    BEER_RATING_DESC(BeerDatabase.BEER_RATING_COLUMN+" DeSC",  "by rating (high to low)"),
    BEER_RATING_ASC(BeerDatabase.BEER_RATING_COLUMN+" ASC",  "by rating (low to high)");

    private String fOrderByClause;

    private String fDescription;

    public String getOrderByClause() {
        return fOrderByClause;
    }

    public String getDescription() {
        return fDescription;
    }

    public String toString() {
        return fDescription;
    }

    SortOrder(String orderByClause, String description) {
        fOrderByClause = orderByClause;
        fDescription = description;
    }
}
