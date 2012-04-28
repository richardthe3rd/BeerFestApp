package ralcock.cbf.model;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;

public class BreweryDaoImplTest extends AndroidTestCase {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private BreweryDao fBreweryDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(),
                BeerListTest.class.getSimpleName());
        fBeerDatabaseHelper = new BeerDatabaseHelper(context);
        fBreweryDao = fBeerDatabaseHelper.getBreweryDao();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        fBeerDatabaseHelper.close();
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
        int numUpdated = fBreweryDao.updateFromFestival(brewery2);
        assertEquals(1, numUpdated);

        fBreweryDao.refresh(brewery);
        assertEquals(brewery.getName(), "NAME2");

    }

    public void testUpdateFromFestivalNew() throws SQLException {
        Brewery brewery = new Brewery("ID", "NAME", "DESCRIPTION");
        int numUpdated = fBreweryDao.updateFromFestival(brewery);
        assertEquals(0, numUpdated);
    }
}
