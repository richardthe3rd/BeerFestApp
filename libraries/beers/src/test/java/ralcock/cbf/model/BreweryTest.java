package ralcock.cbf.model;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BreweryTest
{
    private static final String FESTIVAL_ID = "brew123";
    private static final String NAME = "Test Brewery";
    private static final String DESCRIPTION = "A test brewery";

    private Brewery createTestBrewery() {
        return new Brewery(FESTIVAL_ID, NAME, DESCRIPTION);
    }

    @Test
    public void constructor() {
        Brewery brewery = new Brewery(FESTIVAL_ID, NAME, DESCRIPTION);

        assertThat(brewery.getFestivalID(), equalTo(FESTIVAL_ID));
        assertThat(brewery.getName(), equalTo(NAME));
        assertThat(brewery.getDescription(), equalTo(DESCRIPTION));
    }

    @Test
    public void setAndGetId() {
        Brewery brewery = createTestBrewery();
        brewery.setId(42L);
        assertThat(brewery.getId(), equalTo(42L));
    }

    @Test
    public void equalsReflexive() {
        Brewery brewery = createTestBrewery();
        assertThat(brewery, equalTo(brewery));
    }

    @Test
    public void equalsSymmetric() {
        Brewery brewery1 = createTestBrewery();
        Brewery brewery2 = createTestBrewery();
        assertThat(brewery1, equalTo(brewery2));
        assertThat(brewery2, equalTo(brewery1));
    }

    @Test
    public void equalsTransitive() {
        Brewery brewery1 = createTestBrewery();
        Brewery brewery2 = createTestBrewery();
        Brewery brewery3 = createTestBrewery();
        assertThat(brewery1, equalTo(brewery2));
        assertThat(brewery2, equalTo(brewery3));
        assertThat(brewery1, equalTo(brewery3));
    }

    @Test
    public void equalsWithNull() {
        Brewery brewery = createTestBrewery();
        assertThat(brewery.equals(null), is(false));
    }

    @Test
    public void equalsWithDifferentClass() {
        Brewery brewery = createTestBrewery();
        assertThat(brewery.equals("not a brewery"), is(false));
    }

    @Test
    public void equalsWithNullFields() {
        Brewery brewery1 = new Brewery(null, null, null);
        Brewery brewery2 = new Brewery(null, null, null);
        assertThat(brewery1, equalTo(brewery2));
    }

    @Test
    public void notEqualsWithDifferentFestivalId() {
        Brewery brewery1 = new Brewery("id1", NAME, DESCRIPTION);
        Brewery brewery2 = new Brewery("id2", NAME, DESCRIPTION);
        assertThat(brewery1, not(equalTo(brewery2)));
    }

    @Test
    public void notEqualsWithDifferentName() {
        Brewery brewery1 = new Brewery(FESTIVAL_ID, "Name A", DESCRIPTION);
        Brewery brewery2 = new Brewery(FESTIVAL_ID, "Name B", DESCRIPTION);
        assertThat(brewery1, not(equalTo(brewery2)));
    }

    @Test
    public void notEqualsWithDifferentDescription() {
        Brewery brewery1 = new Brewery(FESTIVAL_ID, NAME, "Description A");
        Brewery brewery2 = new Brewery(FESTIVAL_ID, NAME, "Description B");
        assertThat(brewery1, not(equalTo(brewery2)));
    }

    @Test
    public void hashCodeConsistency() {
        Brewery brewery = createTestBrewery();
        int hash1 = brewery.hashCode();
        int hash2 = brewery.hashCode();
        assertThat(hash1, equalTo(hash2));
    }

    @Test
    public void hashCodeEqualityContract() {
        Brewery brewery1 = createTestBrewery();
        Brewery brewery2 = createTestBrewery();
        assertThat(brewery1, equalTo(brewery2));
        assertThat(brewery1.hashCode(), equalTo(brewery2.hashCode()));
    }

    @Test
    public void hashCodeWithNullFields() {
        Brewery brewery1 = new Brewery(null, null, null);
        Brewery brewery2 = new Brewery(null, null, null);
        assertThat(brewery1.hashCode(), equalTo(brewery2.hashCode()));
    }

    @Test
    public void toStringContainsAllFields() {
        Brewery brewery = createTestBrewery();
        brewery.setId(123L);

        String str = brewery.toString();
        assertThat(str, containsString("Brewery"));
        assertThat(str, containsString("fId=123"));
        assertThat(str, containsString("fFestivalID='" + FESTIVAL_ID));
        assertThat(str, containsString("fName='" + NAME));
        assertThat(str, containsString("fDescription='" + DESCRIPTION));
    }
}
