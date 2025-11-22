package ralcock.cbf.model;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BeerTest
{
    private static final String FESTIVAL_ID = "fest123";
    private static final String NAME = "Test Beer";
    private static final float ABV = 5.5f;
    private static final String DESCRIPTION = "A test beer";
    private static final String STYLE = "IPA";
    private static final String STATUS = "Available";
    private static final String DISPENSE = "Cask";
    private static final String ALLERGENS = "";

    private Brewery createTestBrewery() {
        return new Brewery("brew123", "Test Brewery", "A test brewery");
    }

    private Beer createTestBeer() {
        return new Beer(FESTIVAL_ID, NAME, ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, createTestBrewery());
    }

    @Test
    public void constructor() {
        Brewery brewery = createTestBrewery();
        Beer beer = new Beer(FESTIVAL_ID, NAME, ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, brewery);

        assertThat(beer.getFestivalID(), equalTo(FESTIVAL_ID));
        assertThat(beer.getName(), equalTo(NAME));
        assertThat(beer.getAbv(), equalTo(ABV));
        assertThat(beer.getDescription(), equalTo(DESCRIPTION));
        assertThat(beer.getStyle(), equalTo(STYLE));
        assertThat(beer.getStatus(), equalTo(STATUS));
        assertThat(beer.getDispenseMethod(), equalTo(DISPENSE));
        assertThat(beer.getAllergens(), equalTo(ALLERGENS));
        assertThat(beer.getBrewery(), equalTo(brewery));
    }

    @Test
    public void equalsReflexive() {
        Beer beer = createTestBeer();
        assertThat(beer, equalTo(beer));
    }

    @Test
    public void equalsSymmetric() {
        Beer beer1 = createTestBeer();
        Beer beer2 = createTestBeer();
        assertThat(beer1, equalTo(beer2));
        assertThat(beer2, equalTo(beer1));
    }

    @Test
    public void equalsTransitive() {
        Beer beer1 = createTestBeer();
        Beer beer2 = createTestBeer();
        Beer beer3 = createTestBeer();
        assertThat(beer1, equalTo(beer2));
        assertThat(beer2, equalTo(beer3));
        assertThat(beer1, equalTo(beer3));
    }

    @Test
    public void equalsWithNull() {
        Beer beer = createTestBeer();
        assertThat(beer.equals(null), is(false));
    }

    @Test
    public void equalsWithDifferentClass() {
        Beer beer = createTestBeer();
        assertThat(beer.equals("not a beer"), is(false));
    }

    @Test
    public void equalsWithNullBrewery() {
        Beer beer1 = new Beer(FESTIVAL_ID, NAME, ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, null);
        Beer beer2 = new Beer(FESTIVAL_ID, NAME, ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, null);
        assertThat(beer1, equalTo(beer2));
    }

    @Test
    public void notEqualsWithDifferentBrewery() {
        Brewery brewery1 = new Brewery("brew1", "Brewery 1", "Description 1");
        Brewery brewery2 = new Brewery("brew2", "Brewery 2", "Description 2");
        Beer beer1 = new Beer(FESTIVAL_ID, NAME, ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, brewery1);
        Beer beer2 = new Beer(FESTIVAL_ID, NAME, ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, brewery2);
        assertThat(beer1, not(equalTo(beer2)));
    }

    @Test
    public void notEqualsWithDifferentName() {
        Beer beer1 = new Beer(FESTIVAL_ID, "Beer A", ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, createTestBrewery());
        Beer beer2 = new Beer(FESTIVAL_ID, "Beer B", ABV, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, createTestBrewery());
        assertThat(beer1, not(equalTo(beer2)));
    }

    @Test
    public void notEqualsWithDifferentAbv() {
        Beer beer1 = new Beer(FESTIVAL_ID, NAME, 4.5f, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, createTestBrewery());
        Beer beer2 = new Beer(FESTIVAL_ID, NAME, 6.5f, DESCRIPTION, STYLE, STATUS, DISPENSE, ALLERGENS, createTestBrewery());
        assertThat(beer1, not(equalTo(beer2)));
    }

    @Test
    public void hashCodeConsistency() {
        Beer beer = createTestBeer();
        int hash1 = beer.hashCode();
        int hash2 = beer.hashCode();
        assertThat(hash1, equalTo(hash2));
    }

    @Test
    public void hashCodeEqualityContract() {
        Beer beer1 = createTestBeer();
        Beer beer2 = createTestBeer();
        assertThat(beer1, equalTo(beer2));
        assertThat(beer1.hashCode(), equalTo(beer2.hashCode()));
    }

    @Test
    public void setAndGetNumberOfStars() {
        Beer beer = createTestBeer();
        StarRating rating = new StarRating(4);
        beer.setNumberOfStars(rating);
        assertThat(beer.getNumberOfStars(), comparesEqualTo(rating));
        assertThat(beer.getRating(), equalTo(4));
    }

    @Test
    public void setAndGetId() {
        Beer beer = createTestBeer();
        beer.setId(42L);
        assertThat(beer.getId(), equalTo(42L));
    }

    @Test
    public void setAndGetIsOnWishList() {
        Beer beer = createTestBeer();
        assertThat(beer.isIsOnWishList(), is(false));
        beer.setIsOnWishList(true);
        assertThat(beer.isIsOnWishList(), is(true));
    }

    @Test
    public void setAndGetName() {
        Beer beer = createTestBeer();
        beer.setName("New Name");
        assertThat(beer.getName(), equalTo("New Name"));
    }

    @Test
    public void setAndGetUserComments() {
        Beer beer = createTestBeer();
        assertThat(beer.getUserComments(), nullValue());
        beer.setUserComments("Great beer!");
        assertThat(beer.getUserComments(), equalTo("Great beer!"));
    }

    @Test
    public void setAndGetDispenseMethod() {
        Beer beer = createTestBeer();
        beer.setDispenseMethod("Keg");
        assertThat(beer.getDispenseMethod(), equalTo("Keg"));
    }

    @Test
    public void toStringContainsAllFields() {
        Beer beer = createTestBeer();
        beer.setId(123L);
        beer.setNumberOfStars(new StarRating(3));
        beer.setIsOnWishList(true);
        beer.setUserComments("Test comment");

        String str = beer.toString();
        assertThat(str, containsString("Beer"));
        assertThat(str, containsString("fId=123"));
        assertThat(str, containsString("fName='" + NAME));
        assertThat(str, containsString("fAbv=" + ABV));
        assertThat(str, containsString("fRating=3"));
        assertThat(str, containsString("fIsOnWishList=true"));
    }
}
