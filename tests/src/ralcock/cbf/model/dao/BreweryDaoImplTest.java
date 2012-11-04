package ralcock.cbf.model.dao;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.BeerListTest;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;

public class BreweryDaoImplTest extends AndroidTestCase {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private BreweryDao fBreweryDao;
    private RenamingDelegatingContext fContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fContext = new RenamingDelegatingContext(getContext(),
                BeerListTest.class.getSimpleName() + ".");
        fBeerDatabaseHelper = new BeerDatabaseHelper(fContext);
        fBreweryDao = fBeerDatabaseHelper.getBreweryDao();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        fBeerDatabaseHelper.close();
        fContext.deleteDatabase(BeerDatabaseHelper.DATABASE_NAME);
    }

    public void testWithQuote() throws Exception {
        Brewery brewery = new Brewery("ID1", "Quote's Brewery", "NOTES");
        fBreweryDao.create(brewery);
    }

    public void testUpdateFromFestival() throws Exception {
        // Create Brewery in DB
        Brewery brewery = new Brewery("ID1", "NAME", "DESCRIPTION");
        fBreweryDao.create(brewery);

        Brewery brewery2 = new Brewery("ID1", "NAME2", "DESCRIPTION2");
        fBreweryDao.updateFromFestivalOrCreate(brewery2);

        fBreweryDao.refresh(brewery);
        assertEquals(brewery.getName(), "NAME2");

    }

    public void testUpdateFromFestivalNew() throws SQLException {
        Brewery brewery = new Brewery("ID", "NAME", "DESCRIPTION");
        fBreweryDao.updateFromFestivalOrCreate(brewery);
        assertEquals(brewery.getName(), "NAME");
    }
}
