package ralcock.cbf.model.dao;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;

/**
 * Modern AndroidJUnit4 tests for Breweries DAO.
 *
 * <p>Tests database operations including brewery creation, updates, and special character handling.
 * Migrated from legacy AndroidTestCase framework (2025-11-18).
 */
@RunWith(AndroidJUnit4.class)
public class BreweriesImplTest {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private Breweries fBreweries;
    private Context fContext;

    @Before
    public void setUp() throws Exception {
        fContext = ApplicationProvider.getApplicationContext();
        fBeerDatabaseHelper = new BeerDatabaseHelper(fContext);
        fBreweries = fBeerDatabaseHelper.getBreweries();

        // Clear any existing data to avoid unique constraint violations
        fBeerDatabaseHelper.deleteAll();
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

    @Test
    public void testWithQuote() throws Exception {
        Brewery brewery = new Brewery("ID1", "Quote's Brewery", "NOTES");
        fBreweries.create(brewery);
    }

    @Test
    public void testUpdateFromFestival() throws Exception {
        // Create Brewery in DB
        Brewery brewery = new Brewery("ID1", "NAME", "DESCRIPTION");
        fBreweries.create(brewery);

        Brewery brewery2 = new Brewery("ID1", "NAME2", "DESCRIPTION2");
        fBreweries.updateFromFestivalOrCreate(brewery2);

        fBreweries.refresh(brewery);
        assertEquals(brewery.getName(), "NAME2");
    }

    @Test
    public void testUpdateFromFestivalNew() throws SQLException {
        Brewery brewery = new Brewery("ID", "NAME", "DESCRIPTION");
        fBreweries.updateFromFestivalOrCreate(brewery);
        assertEquals(brewery.getName(), "NAME");
    }
}
