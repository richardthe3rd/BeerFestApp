package ralcock.cbf.actions;

import android.content.Intent;
import android.test.AndroidTestCase;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ralcock.cbf.model.BeerBuilder.aBeer;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

/**
 * Tests for BeerExporter CSV export functionality.
 *
 * NOTE: These tests have limited coverage because BeerExporter.export() calls startActivity()
 * directly, making it difficult to verify the Intent contents without refactoring. These tests
 * primarily verify that CSV formatting doesn't crash with various inputs.
 *
 * FUTURE IMPROVEMENT: Refactor BeerExporter to have a makeExportIntent() method (similar to
 * BeerSharer.makeShareIntent()) that returns the Intent without starting the activity. This
 * would allow tests to verify:
 * - CSV header format
 * - CSV row format
 * - Proper escaping of quotes and commas
 * - Intent action, type, and extras
 */
public class BeerExporterTest extends AndroidTestCase {

    private BeerExporter fExporter;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fExporter = new BeerExporter(getContext());
    }

    public void testExportSingleBeer() throws Exception {
        Beer beer = aBeer()
                .called("Test Beer")
                .from(aBrewery().called("Test Brewery"))
                .withStyle("IPA")
                .build();
        beer.setNumberOfStars(new StarRating(4));

        List<Beer> beers = Arrays.asList(beer);

        // Method calls startActivity, so we verify it doesn't crash
        try {
            fExporter.export(beers);
            // If we get here without exception, the CSV was formatted correctly
        } catch (Exception e) {
            fail("Export should not throw exception for single valid beer: " + e.getMessage());
        }
    }

    public void testExportMultipleBeers() throws Exception {
        Beer beer1 = aBeer()
                .called("First Beer")
                .from(aBrewery().called("First Brewery"))
                .withStyle("Pale Ale")
                .build();
        beer1.setNumberOfStars(new StarRating(3));

        Beer beer2 = aBeer()
                .called("Second Beer")
                .from(aBrewery().called("Second Brewery"))
                .withStyle("Stout")
                .build();
        beer2.setNumberOfStars(new StarRating(5));

        Beer beer3 = aBeer()
                .called("Third Beer")
                .from(aBrewery().called("Third Brewery"))
                .withStyle("Lager")
                .build();
        beer3.setNumberOfStars(new StarRating(2));

        List<Beer> beers = Arrays.asList(beer1, beer2, beer3);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should not throw exception for multiple valid beers: " + e.getMessage());
        }
    }

    public void testExportEmptyList() throws Exception {
        List<Beer> beers = new ArrayList<Beer>();

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should not throw exception for empty list: " + e.getMessage());
        }
    }

    public void testExportBeerWithQuotesInName() throws Exception {
        Beer beer = aBeer()
                .called("The \"Quoted\" Beer")
                .from(aBrewery().called("Test Brewery"))
                .withStyle("IPA")
                .build();
        beer.setNumberOfStars(new StarRating(3));

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle quotes in beer name: " + e.getMessage());
        }
    }

    public void testExportBeerWithCommasInName() throws Exception {
        Beer beer = aBeer()
                .called("Beer, The Great")
                .from(aBrewery().called("Brewery, Inc."))
                .withStyle("IPA, Strong")
                .build();
        beer.setNumberOfStars(new StarRating(4));

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle commas in names: " + e.getMessage());
        }
    }

    public void testExportBeerWithNewlinesInName() throws Exception {
        Beer beer = aBeer()
                .called("Beer\nWith\nNewlines")
                .from(aBrewery().called("Brewery\nName"))
                .withStyle("Style\nType")
                .build();
        beer.setNumberOfStars(new StarRating(3));

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle newlines in names: " + e.getMessage());
        }
    }

    public void testExportBeerWithSpecialCharacters() throws Exception {
        Beer beer = aBeer()
                .called("Spëcîål Béér & Co.'s")
                .from(aBrewery().called("Brëwéry™"))
                .withStyle("IPÄ")
                .build();
        beer.setNumberOfStars(new StarRating(5));

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle special characters: " + e.getMessage());
        }
    }

    public void testExportUnratedBeer() throws Exception {
        Beer beer = aBeer()
                .called("Unrated Beer")
                .from(aBrewery().called("Test Brewery"))
                .withStyle("Lager")
                .build();
        // Don't set rating, defaults to 0

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle unrated beers: " + e.getMessage());
        }
    }

    public void testExportBeerWithVeryLongName() throws Exception {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longName.append("VeryLongBeerName");
        }

        Beer beer = aBeer()
                .called(longName.toString())
                .from(aBrewery().called(longName.toString()))
                .withStyle(longName.toString())
                .build();
        beer.setNumberOfStars(new StarRating(3));

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle very long names: " + e.getMessage());
        }
    }

    public void testExportBeerWithEmptyStrings() throws Exception {
        Beer beer = aBeer()
                .called("")
                .from(aBrewery().called(""))
                .withStyle("")
                .build();
        beer.setNumberOfStars(new StarRating(3));

        List<Beer> beers = Arrays.asList(beer);

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle empty strings: " + e.getMessage());
        }
    }

    public void testExportLargeNumberOfBeers() throws Exception {
        List<Beer> beers = new ArrayList<Beer>();
        for (int i = 0; i < 1000; i++) {
            Beer beer = aBeer()
                    .called("Beer " + i)
                    .from(aBrewery().called("Brewery " + i))
                    .withStyle("Style " + i)
                    .build();
            beer.setNumberOfStars(new StarRating((i % 5) + 1));
            beers.add(beer);
        }

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle large number of beers: " + e.getMessage());
        }
    }

    public void testExportMixedRatings() throws Exception {
        List<Beer> beers = new ArrayList<Beer>();

        // Test all ratings from 0 to 5
        for (int rating = 0; rating <= 5; rating++) {
            Beer beer = aBeer()
                    .called("Beer with rating " + rating)
                    .from(aBrewery().called("Test Brewery"))
                    .withStyle("IPA")
                    .build();
            beer.setNumberOfStars(new StarRating(rating));
            beers.add(beer);
        }

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle all rating values: " + e.getMessage());
        }
    }

    public void testExportWithVariousStyles() throws Exception {
        String[] styles = {
                "Pale Ale", "IPA", "Stout", "Porter", "Lager",
                "Pilsner", "Wheat Beer", "Belgian", "Sour", "Barley Wine"
        };

        List<Beer> beers = new ArrayList<Beer>();
        for (String style : styles) {
            Beer beer = aBeer()
                    .called("Test Beer")
                    .from(aBrewery().called("Test Brewery"))
                    .withStyle(style)
                    .build();
            beer.setNumberOfStars(new StarRating(3));
            beers.add(beer);
        }

        try {
            fExporter.export(beers);
        } catch (Exception e) {
            fail("Export should handle various beer styles: " + e.getMessage());
        }
    }
}
