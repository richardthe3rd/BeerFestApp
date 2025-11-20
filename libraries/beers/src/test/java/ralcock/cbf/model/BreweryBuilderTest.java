package ralcock.cbf.model;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

public class BreweryBuilderTest
{
    @Test
    public void buildsWithDefaults() {
        Brewery brewery = aBrewery().build();

        assertThat(brewery.getName(), equalTo(""));
        assertThat(brewery.getFestivalID(), equalTo(""));
        assertThat(brewery.getDescription(), equalTo(""));
    }

    @Test
    public void fluentApiReturnsBuilder() {
        BreweryBuilder builder = aBrewery();
        assertThat(builder.called("name"), sameInstance(builder));
        assertThat(builder.withFestivalId("id"), sameInstance(builder));
        assertThat(builder.withDescription("desc"), sameInstance(builder));
    }

    @Test
    public void buildsWithAllFields() {
        Brewery brewery = aBrewery()
                .withFestivalId("brew123")
                .called("Test Brewery")
                .withDescription("A test brewery")
                .build();

        assertThat(brewery.getFestivalID(), equalTo("brew123"));
        assertThat(brewery.getName(), equalTo("Test Brewery"));
        assertThat(brewery.getDescription(), equalTo("A test brewery"));
    }

    @Test
    public void buildsWithName() {
        Brewery brewery = aBrewery()
                .called("My Brewery")
                .build();

        assertThat(brewery.getName(), equalTo("My Brewery"));
        assertThat(brewery.getFestivalID(), equalTo(""));
        assertThat(brewery.getDescription(), equalTo(""));
    }

    @Test
    public void buildsWithFestivalId() {
        Brewery brewery = aBrewery()
                .withFestivalId("fest123")
                .build();

        assertThat(brewery.getFestivalID(), equalTo("fest123"));
        assertThat(brewery.getName(), equalTo(""));
        assertThat(brewery.getDescription(), equalTo(""));
    }

    @Test
    public void buildsWithDescription() {
        Brewery brewery = aBrewery()
                .withDescription("Test description")
                .build();

        assertThat(brewery.getDescription(), equalTo("Test description"));
        assertThat(brewery.getName(), equalTo(""));
        assertThat(brewery.getFestivalID(), equalTo(""));
    }

    @Test
    public void chainsAllMethods() {
        Brewery brewery = aBrewery()
                .called("Chained Brewery")
                .withFestivalId("chain123")
                .withDescription("Chained description")
                .build();

        assertThat(brewery.getName(), equalTo("Chained Brewery"));
        assertThat(brewery.getFestivalID(), equalTo("chain123"));
        assertThat(brewery.getDescription(), equalTo("Chained description"));
    }

    @Test
    public void multipleBuildsCalled() {
        BreweryBuilder builder = aBrewery()
                .called("Test Brewery")
                .withFestivalId("test123");

        Brewery brewery1 = builder.build();
        Brewery brewery2 = builder.build();

        assertThat(brewery1.getName(), equalTo("Test Brewery"));
        assertThat(brewery2.getName(), equalTo("Test Brewery"));
        assertThat(brewery1, equalTo(brewery2));
        assertThat(brewery1, not(sameInstance(brewery2)));
    }
}
