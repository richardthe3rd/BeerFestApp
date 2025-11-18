package ralcock.cbf.model.dao;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.SortOrder;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Modern AndroidJUnit4 tests for Beers DAO.
 *
 * Tests database operations including filtering and style queries.
 * Migrated from legacy AndroidTestCase framework (2025-11-18).
 */
@RunWith(AndroidJUnit4.class)
public class BeersImplTest {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private Beers fBeers;
    private Context fContext;

    private Beer fBeer1;
    private Beer fBeer2;
    private Beer fBeer3;

    private String fStyle1 = "style1";
    private String fStyle2 = "style2";
    private Breweries fBreweries;

    @Before
    public void setUp() throws Exception {
        fContext = ApplicationProvider.getApplicationContext();
        fBeerDatabaseHelper = new BeerDatabaseHelper(fContext);
        fBeers = fBeerDatabaseHelper.getBeers();
        fBreweries = fBeerDatabaseHelper.getBreweries();

        // Clear any existing data to avoid unique constraint violations
        fBeerDatabaseHelper.deleteAll();

        Brewery brewery = new Brewery("1", "First Brewery", "y");
        fBreweries.create(brewery);

        Brewery brewery2 = new Brewery("2", "Best Brewery", "");
        fBreweries.create(brewery2);

        fBeer1 = new Beer("1", "A Mild", 1f, "description1", fStyle1, "status1", "cask", brewery);
        fBeers.create(fBeer1);

        fBeer2 = new Beer("2", "A Best Bitter", 2f, "description2", fStyle2, "status2", "cask", brewery);
        fBeers.create(fBeer2);

        fBeer3 = new Beer("3", "A Stout", 3f, "description3", fStyle2, "status3", "cask", brewery2);
        fBeers.create(fBeer3);

        assertEquals(3, fBeers.getNumberOfBeers());
    }

    @After
    public void tearDown() throws Exception {
        if (fBeerDatabaseHelper != null) {
            fBeerDatabaseHelper.close();
        }
        if (fContext != null) {
            fContext.deleteDatabase(BeerDatabaseHelper.DATABASE_NAME);
        }
    }

    private List<Beer> doQuery(SortOrder sortOrder, CharSequence filterText, Set<String> stylesToHide) throws SQLException {
        return fBeers.allBeersList(sortOrder, filterText, stylesToHide, stylesToHide);
    }

    @Test
    public void testFiltering() throws Exception {
        Set<String> showAllStyles = Collections.emptySet();
        {
            List<Beer> list = doQuery(SortOrder.BEER_ABV_ASC, "mild", showAllStyles);
            assertEquals(1, list.size());
            assertEquals(fBeer1, list.get(0));
        }
        {
            List<Beer> list = doQuery(SortOrder.BEER_ABV_ASC, "best", showAllStyles);
            assertEquals(2, list.size());
            assertEquals(fBeer2, list.get(0));
            assertEquals(fBeer3, list.get(1));
        }
    }

    @Test
    public void testGetAvailableStyles() throws Exception {
        Set<String> styles = new HashSet<String>();
        styles.add("style1");
        styles.add("style2");
        assertEquals(styles, fBeers.getAvailableStyles());
    }

}
