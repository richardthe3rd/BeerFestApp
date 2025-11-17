package ralcock.cbf.model.dao;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;

public class BreweriesImplTest extends AndroidTestCase {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private Breweries fBreweries;
    private RenamingDelegatingContext fContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fContext = new RenamingDelegatingContext(getContext(),
                BreweriesImplTest.class.getSimpleName() + ".");
        fBeerDatabaseHelper = new BeerDatabaseHelper(fContext);
        fBreweries = fBeerDatabaseHelper.getBreweries();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        fBeerDatabaseHelper.close();
        fContext.deleteDatabase(BeerDatabaseHelper.DATABASE_NAME);
    }

    public void testWithQuote() throws Exception {
        Brewery brewery = new Brewery("ID1", "Quote's Brewery", "NOTES");
        fBreweries.create(brewery);
    }

    public void testUpdateFromFestival() throws Exception {
        // Create Brewery in DB
        Brewery brewery = new Brewery("ID1", "NAME", "DESCRIPTION");
        fBreweries.create(brewery);

        Brewery brewery2 = new Brewery("ID1", "NAME2", "DESCRIPTION2");
        fBreweries.updateFromFestivalOrCreate(brewery2);

        fBreweries.refresh(brewery);
        assertEquals(brewery.getName(), "NAME2");

    }

    public void testUpdateFromFestivalNew() throws SQLException {
        Brewery brewery = new Brewery("ID", "NAME", "DESCRIPTION");
        fBreweries.updateFromFestivalOrCreate(brewery);
        assertEquals(brewery.getName(), "NAME");
    }
}
