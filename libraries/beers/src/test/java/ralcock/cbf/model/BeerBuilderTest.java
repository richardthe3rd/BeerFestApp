package ralcock.cbf.model;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ralcock.cbf.model.BeerBuilder.aBeer;

public class BeerBuilderTest
{
    @Test
    public void buildsWithDefaults() {
        Beer beer = aBeer().build();

        assertThat(beer.getFestivalID(), equalTo(""));
        assertThat(beer.getName(), equalTo(""));
        assertThat(beer.getAbv(), equalTo(0.0f));
        assertThat(beer.getDescription(), equalTo(""));
        assertThat(beer.getStyle(), equalTo(""));
        assertThat(beer.getStatus(), equalTo(""));
        assertThat(beer.getDispenseMethod(), equalTo(""));
        assertThat(beer.getBrewery(), nullValue());
    }

    @Test
    public void fluentApiReturnsBuilder() {
        BeerBuilder builder = aBeer();
        assertThat(builder.withFestivalId("id"), sameInstance(builder));
        assertThat(builder.called("name"), sameInstance(builder));
        assertThat(builder.withABV(5.5f), sameInstance(builder));
        assertThat(builder.withDescription("desc"), sameInstance(builder));
        assertThat(builder.withStyle("IPA"), sameInstance(builder));
        assertThat(builder.withStatus("Available"), sameInstance(builder));
        assertThat(builder.withDispenseMethod("Cask"), sameInstance(builder));
    }

    @Test
    public void buildsWithAllFields() {
        Brewery brewery = new Brewery("brew123", "Test Brewery", "Description");
        Beer beer = aBeer()
                .withFestivalId("fest123")
                .called("Test Beer")
                .withABV(5.5f)
                .withDescription("A test beer")
                .withStyle("IPA")
                .withStatus("Available")
                .withDispenseMethod("Cask")
                .fromBrewery(brewery)
                .build();

        assertThat(beer.getFestivalID(), equalTo("fest123"));
        assertThat(beer.getName(), equalTo("Test Beer"));
        assertThat(beer.getAbv(), equalTo(5.5f));
        assertThat(beer.getDescription(), equalTo("A test beer"));
        assertThat(beer.getStyle(), equalTo("IPA"));
        assertThat(beer.getStatus(), equalTo("Available"));
        assertThat(beer.getDispenseMethod(), equalTo("Cask"));
        assertThat(beer.getBrewery(), equalTo(brewery));
    }

    @Test
    public void buildsFromBreweryBuilder() {
        BreweryBuilder breweryBuilder = BreweryBuilder.aBrewery()
                .withFestivalId("brew123")
                .called("Test Brewery")
                .withDescription("A test brewery");

        Beer beer = aBeer()
                .called("Test Beer")
                .from(breweryBuilder)
                .build();

        assertThat(beer.getName(), equalTo("Test Beer"));
        assertThat(beer.getBrewery(), notNullValue());
        assertThat(beer.getBrewery().getName(), equalTo("Test Brewery"));
        assertThat(beer.getBrewery().getFestivalID(), equalTo("brew123"));
    }

    @Test
    public void fromBreweryBuilderReturnsBuilder() {
        BreweryBuilder breweryBuilder = BreweryBuilder.aBrewery();
        BeerBuilder beerBuilder = aBeer();
        assertThat(beerBuilder.from(breweryBuilder), sameInstance(beerBuilder));
    }

    @Test
    public void chainsAllMethods() {
        Brewery brewery = new Brewery("brew123", "Test Brewery", "Description");

        Beer beer = aBeer()
                .withFestivalId("fest123")
                .called("Chained Beer")
                .withABV(6.0f)
                .withDescription("Chained description")
                .withStyle("Stout")
                .withStatus("Unavailable")
                .withDispenseMethod("Keg")
                .fromBrewery(brewery)
                .build();

        assertThat(beer.getName(), equalTo("Chained Beer"));
        assertThat(beer.getAbv(), equalTo(6.0f));
        assertThat(beer.getStyle(), equalTo("Stout"));
    }
}
