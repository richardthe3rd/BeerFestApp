package ralcock.cbf.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ralcock.cbf.model.BeerBuilder.aBeer;
import static ralcock.cbf.model.BreweryBuilder.aBrewery;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.StarRating;

/**
 * Tests for BeerExporter CSV export functionality.
 *
 * <p>Tests verify CSV formatting, Intent contents, and edge case handling.
 */
@RunWith(AndroidJUnit4.class)
public class BeerExporterTest {

    private BeerExporter fExporter;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        fExporter = new BeerExporter(context);
    }

    @Test
    public void testIntentAction() {
        Beer beer =
                aBeer().called("Test Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("IPA")
                        .build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        assertEquals("Intent action should be ACTION_SEND", Intent.ACTION_SEND, intent.getAction());
    }

    @Test
    public void testIntentType() {
        Beer beer =
                aBeer().called("Test Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("IPA")
                        .build();

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        assertEquals("Intent type should be text/plain", "text/plain", intent.getType());
    }

    @Test
    public void testIntentSubject() {
        Beer beer =
                aBeer().called("Test Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("IPA")
                        .build();

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        assertNotNull("Intent should have EXTRA_SUBJECT", subject);
        assertTrue("Subject should contain 'Beers from'", subject.startsWith("Beers from "));
        assertTrue(
                "Subject should contain festival name",
                subject.contains("Cambridge Beer Festival"));
    }

    @Test
    public void testCSVHeader() {
        Beer beer =
                aBeer().called("Test Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("IPA")
                        .build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null", csv);
        assertTrue("CSV should contain header", csv.startsWith("Beer, Brewery, Style, Rating\n"));
    }

    @Test
    public void testCSVSingleBeerFormat() {
        Beer beer =
                aBeer().called("Test Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("IPA")
                        .build();
        beer.setNumberOfStars(new StarRating(4));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");
        assertEquals("CSV should have 2 lines (header + 1 beer)", 2, lines.length);
        assertEquals("Beer, Brewery, Style, Rating", lines[0]);
        assertEquals("\"Test Beer\", \"Test Brewery\", \"IPA\", 4", lines[1]);
    }

    @Test
    public void testCSVMultipleBeers() {
        Beer beer1 =
                aBeer().called("First Beer")
                        .from(aBrewery().called("First Brewery"))
                        .withStyle("Pale Ale")
                        .build();
        beer1.setNumberOfStars(new StarRating(3));

        Beer beer2 =
                aBeer().called("Second Beer")
                        .from(aBrewery().called("Second Brewery"))
                        .withStyle("Stout")
                        .build();
        beer2.setNumberOfStars(new StarRating(5));

        Beer beer3 =
                aBeer().called("Third Beer")
                        .from(aBrewery().called("Third Brewery"))
                        .withStyle("Lager")
                        .build();
        beer3.setNumberOfStars(new StarRating(2));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer1, beer2, beer3));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");
        assertEquals("CSV should have 4 lines (header + 3 beers)", 4, lines.length);
        assertEquals("\"First Beer\", \"First Brewery\", \"Pale Ale\", 3", lines[1]);
        assertEquals("\"Second Beer\", \"Second Brewery\", \"Stout\", 5", lines[2]);
        assertEquals("\"Third Beer\", \"Third Brewery\", \"Lager\", 2", lines[3]);
    }

    @Test
    public void testCSVEmptyList() {
        List<Beer> beers = new ArrayList<Beer>();

        Intent intent = fExporter.makeExportIntent(beers);

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null for empty list", csv);
        assertEquals(
                "CSV should only have header for empty list",
                "Beer, Brewery, Style, Rating\n",
                csv);
    }

    @Test
    public void testCSVQuotesInBeerName() {
        Beer beer =
                aBeer().called("The \"Quoted\" Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("IPA")
                        .build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");
        // Note: Current implementation doesn't escape quotes, so we test actual behavior
        assertTrue(
                "CSV should contain beer name with quotes",
                lines[1].contains("The \"Quoted\" Beer"));
    }

    @Test
    public void testCSVQuotesInBreweryName() {
        Beer beer =
                aBeer().called("Test Beer")
                        .from(aBrewery().called("The \"Quoted\" Brewery"))
                        .withStyle("IPA")
                        .build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");
        assertTrue(
                "CSV should contain brewery name with quotes",
                lines[1].contains("The \"Quoted\" Brewery"));
    }

    @Test
    public void testCSVCommasInNames() {
        Beer beer =
                aBeer().called("Beer, The Great")
                        .from(aBrewery().called("Brewery, Inc."))
                        .withStyle("IPA, Strong")
                        .build();
        beer.setNumberOfStars(new StarRating(4));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null", csv);
        // CSV wraps fields in quotes, so commas are handled
        assertTrue("CSV should contain beer name", csv.contains("Beer, The Great"));
        assertTrue("CSV should contain brewery name", csv.contains("Brewery, Inc."));
        assertTrue("CSV should contain style", csv.contains("IPA, Strong"));
    }

    @Test
    public void testCSVNewlinesInNames() {
        Beer beer =
                aBeer().called("Beer\nWith\nNewlines")
                        .from(aBrewery().called("Brewery\nName"))
                        .withStyle("Style\nType")
                        .build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null", csv);
        // Note: Current implementation doesn't escape newlines specially
        assertTrue("CSV should contain beer name", csv.contains("Beer\nWith\nNewlines"));
    }

    @Test
    public void testCSVSpecialCharacters() {
        Beer beer =
                aBeer().called("Spëcîål Béér & Co.'s")
                        .from(aBrewery().called("Brëwéry™"))
                        .withStyle("IPÄ")
                        .build();
        beer.setNumberOfStars(new StarRating(5));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null", csv);
        assertTrue(
                "CSV should contain special characters in beer name",
                csv.contains("Spëcîål Béér & Co.'s"));
        assertTrue(
                "CSV should contain special characters in brewery name", csv.contains("Brëwéry™"));
        assertTrue("CSV should contain special characters in style", csv.contains("IPÄ"));
    }

    @Test
    public void testCSVUnratedBeer() {
        Beer beer =
                aBeer().called("Unrated Beer")
                        .from(aBrewery().called("Test Brewery"))
                        .withStyle("Lager")
                        .build();
        // Don't set rating, defaults to 0

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");
        assertEquals("\"Unrated Beer\", \"Test Brewery\", \"Lager\", 0", lines[1]);
    }

    @Test
    public void testCSVAllRatingValues() {
        List<Beer> beers = new ArrayList<Beer>();

        // Test all ratings from 0 to 5
        for (int rating = 0; rating <= 5; rating++) {
            Beer beer =
                    aBeer().called("Beer " + rating)
                            .from(aBrewery().called("Brewery"))
                            .withStyle("IPA")
                            .build();
            beer.setNumberOfStars(new StarRating(rating));
            beers.add(beer);
        }

        Intent intent = fExporter.makeExportIntent(beers);

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");

        assertEquals("CSV should have 7 lines (header + 6 beers)", 7, lines.length);
        for (int rating = 0; rating <= 5; rating++) {
            String expected =
                    String.format("\"Beer %d\", \"Brewery\", \"IPA\", %d", rating, rating);
            assertEquals(
                    "Line " + (rating + 1) + " should match expected format",
                    expected,
                    lines[rating + 1]);
        }
    }

    @Test
    public void testCSVVeryLongNames() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longName.append("VeryLongName");
        }

        Beer beer =
                aBeer().called(longName.toString())
                        .from(aBrewery().called(longName.toString()))
                        .withStyle(longName.toString())
                        .build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null for very long names", csv);
        assertTrue("CSV should contain long beer name", csv.contains(longName.toString()));
    }

    @Test
    public void testCSVEmptyStrings() {
        Beer beer = aBeer().called("").from(aBrewery().called("")).withStyle("").build();
        beer.setNumberOfStars(new StarRating(3));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");
        assertEquals("\"\", \"\", \"\", 3", lines[1]);
    }

    @Test
    public void testCSVLargeNumberOfBeers() {
        List<Beer> beers = new ArrayList<Beer>();
        for (int i = 0; i < 1000; i++) {
            Beer beer =
                    aBeer().called("Beer " + i)
                            .from(aBrewery().called("Brewery " + i))
                            .withStyle("Style " + i)
                            .build();
            beer.setNumberOfStars(new StarRating((i % 5) + 1));
            beers.add(beer);
        }

        Intent intent = fExporter.makeExportIntent(beers);

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null for large list", csv);
        String[] lines = csv.split("\n");
        assertEquals("CSV should have 1001 lines (header + 1000 beers)", 1001, lines.length);
    }

    @Test
    public void testCSVVariousStyles() {
        String[] styles = {
            "Pale Ale", "IPA", "Stout", "Porter", "Lager",
            "Pilsner", "Wheat Beer", "Belgian", "Sour", "Barley Wine"
        };

        List<Beer> beers = new ArrayList<Beer>();
        for (int i = 0; i < styles.length; i++) {
            Beer beer =
                    aBeer().called("Beer " + i)
                            .from(aBrewery().called("Brewery"))
                            .withStyle(styles[i])
                            .build();
            beer.setNumberOfStars(new StarRating(3));
            beers.add(beer);
        }

        Intent intent = fExporter.makeExportIntent(beers);

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull("CSV should not be null", csv);

        // Verify all styles are present
        for (String style : styles) {
            assertTrue("CSV should contain style: " + style, csv.contains(style));
        }
    }

    @Test
    public void testCSVOrderPreserved() {
        Beer beer1 =
                aBeer().called("Alpha Beer")
                        .from(aBrewery().called("Alpha Brewery"))
                        .withStyle("IPA")
                        .build();
        beer1.setNumberOfStars(new StarRating(5));

        Beer beer2 =
                aBeer().called("Beta Beer")
                        .from(aBrewery().called("Beta Brewery"))
                        .withStyle("Stout")
                        .build();
        beer2.setNumberOfStars(new StarRating(3));

        Beer beer3 =
                aBeer().called("Gamma Beer")
                        .from(aBrewery().called("Gamma Brewery"))
                        .withStyle("Lager")
                        .build();
        beer3.setNumberOfStars(new StarRating(4));

        Intent intent = fExporter.makeExportIntent(Arrays.asList(beer1, beer2, beer3));

        String csv = intent.getStringExtra(Intent.EXTRA_TEXT);
        String[] lines = csv.split("\n");

        assertTrue("First beer should be Alpha", lines[1].contains("Alpha Beer"));
        assertTrue("Second beer should be Beta", lines[2].contains("Beta Beer"));
        assertTrue("Third beer should be Gamma", lines[3].contains("Gamma Beer"));
    }
}
