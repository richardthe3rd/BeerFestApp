package ralcock.cbf.model;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SortOrderTest
{
    @Test
    public void allEnumValuesExist() {
        SortOrder[] values = SortOrder.values();
        assertThat(values.length, equalTo(8));
    }

    @Test
    public void breweryNameAscending() {
        assertThat(SortOrder.BREWERY_NAME_ASC.columnName(), equalTo(Beer.BREWERY_FIELD));
        assertThat(SortOrder.BREWERY_NAME_ASC.ascending(), is(true));
        assertThat(SortOrder.BREWERY_NAME_ASC.getDescription(), equalTo("Brewery (A-Z)"));
    }

    @Test
    public void breweryNameDescending() {
        assertThat(SortOrder.BREWERY_NAME_DESC.columnName(), equalTo(Beer.BREWERY_FIELD));
        assertThat(SortOrder.BREWERY_NAME_DESC.ascending(), is(false));
        assertThat(SortOrder.BREWERY_NAME_DESC.getDescription(), equalTo("Brewery (Z-A)"));
    }

    @Test
    public void beerNameAscending() {
        assertThat(SortOrder.BEER_NAME_ASC.columnName(), equalTo(Beer.NAME_FIELD));
        assertThat(SortOrder.BEER_NAME_ASC.ascending(), is(true));
        assertThat(SortOrder.BEER_NAME_ASC.getDescription(), equalTo("Beer (A-Z)"));
    }

    @Test
    public void beerNameDescending() {
        assertThat(SortOrder.BEER_NAME_DESC.columnName(), equalTo(Beer.NAME_FIELD));
        assertThat(SortOrder.BEER_NAME_DESC.ascending(), is(false));
        assertThat(SortOrder.BEER_NAME_DESC.getDescription(), equalTo("Beer (Z-A)"));
    }

    @Test
    public void beerAbvAscending() {
        assertThat(SortOrder.BEER_ABV_ASC.columnName(), equalTo(Beer.ABV_FIELD));
        assertThat(SortOrder.BEER_ABV_ASC.ascending(), is(true));
        assertThat(SortOrder.BEER_ABV_ASC.getDescription(), equalTo("ABV (low to high)"));
    }

    @Test
    public void beerAbvDescending() {
        assertThat(SortOrder.BEER_ABV_DESC.columnName(), equalTo(Beer.ABV_FIELD));
        assertThat(SortOrder.BEER_ABV_DESC.ascending(), is(false));
        assertThat(SortOrder.BEER_ABV_DESC.getDescription(), equalTo("ABV (high to low)"));
    }

    @Test
    public void beerRatingAscending() {
        assertThat(SortOrder.BEER_RATING_ASC.columnName(), equalTo(Beer.RATING_FIELD));
        assertThat(SortOrder.BEER_RATING_ASC.ascending(), is(true));
        assertThat(SortOrder.BEER_RATING_ASC.getDescription(), equalTo("Rating (low to high)"));
    }

    @Test
    public void beerRatingDescending() {
        assertThat(SortOrder.BEER_RATING_DESC.columnName(), equalTo(Beer.RATING_FIELD));
        assertThat(SortOrder.BEER_RATING_DESC.ascending(), is(false));
        assertThat(SortOrder.BEER_RATING_DESC.getDescription(), equalTo("Rating (high to low)"));
    }

    @Test
    public void toStringEqualsDescription() {
        for (SortOrder sortOrder : SortOrder.values()) {
            assertThat(sortOrder.toString(), equalTo(sortOrder.getDescription()));
        }
    }

    @Test
    public void valueOfReturnsCorrectEnum() {
        assertThat(SortOrder.valueOf("BREWERY_NAME_ASC"), equalTo(SortOrder.BREWERY_NAME_ASC));
        assertThat(SortOrder.valueOf("BEER_ABV_DESC"), equalTo(SortOrder.BEER_ABV_DESC));
        assertThat(SortOrder.valueOf("BEER_RATING_ASC"), equalTo(SortOrder.BEER_RATING_ASC));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfWithInvalidString() {
        SortOrder.valueOf("INVALID_SORT_ORDER");
    }

    @Test
    public void enumOrdering() {
        SortOrder[] values = SortOrder.values();
        assertThat(values[0], equalTo(SortOrder.BREWERY_NAME_ASC));
        assertThat(values[1], equalTo(SortOrder.BREWERY_NAME_DESC));
        assertThat(values[2], equalTo(SortOrder.BEER_NAME_ASC));
        assertThat(values[3], equalTo(SortOrder.BEER_NAME_DESC));
        assertThat(values[4], equalTo(SortOrder.BEER_ABV_ASC));
        assertThat(values[5], equalTo(SortOrder.BEER_ABV_DESC));
        assertThat(values[6], equalTo(SortOrder.BEER_RATING_ASC));
        assertThat(values[7], equalTo(SortOrder.BEER_RATING_DESC));
    }

    @Test
    public void columnNamesMatchBeerFields() {
        assertThat(SortOrder.BREWERY_NAME_ASC.columnName(), equalTo("brewery"));
        assertThat(SortOrder.BEER_NAME_ASC.columnName(), equalTo("name"));
        assertThat(SortOrder.BEER_ABV_ASC.columnName(), equalTo("abv"));
        assertThat(SortOrder.BEER_RATING_ASC.columnName(), equalTo("rating"));
    }
}
