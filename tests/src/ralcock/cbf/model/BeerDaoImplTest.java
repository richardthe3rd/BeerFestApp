package ralcock.cbf.model;


import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BeerDaoImplTest extends AndroidTestCase {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private BeerDao fBeerDao;
    private RenamingDelegatingContext fContext;

    private Beer fBeer1;
    private Beer fBeer2;
    private Beer fBeer3;

    private String fStyle1 = "style1";
    private String fStyle2 = "style2";
    private BreweryDao fBreweryDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fContext = new RenamingDelegatingContext(getContext(),
                BeerListTest.class.getSimpleName() + ".");
        fBeerDatabaseHelper = new BeerDatabaseHelper(fContext);
        fBeerDao = fBeerDatabaseHelper.getBeerDao();
        fBreweryDao = fBeerDatabaseHelper.getBreweryDao();

        Brewery brewery = new Brewery("1", "First Brewery", "y");
        fBreweryDao.create(brewery);

        Brewery brewery2 = new Brewery("2", "Best Brewery", "");
        fBreweryDao.create(brewery2);

        fBeer1 = new Beer("1", "A Mild", 1f, "description1", fStyle1, "status1", brewery);
        fBeerDao.create(fBeer1);

        fBeer2 = new Beer("2", "A Best Bitter", 2f, "description2", fStyle2, "status2", brewery);
        fBeerDao.create(fBeer2);

        fBeer3 = new Beer("3", "A Stout", 3f, "description3", fStyle2, "status3", brewery2);
        fBeerDao.create(fBeer3);

        assertEquals(3, fBeerDao.getNumberOfBeers());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        fBeerDatabaseHelper.close();
        fContext.deleteDatabase(BeerDatabaseHelper.DATABASE_NAME);
    }

    private List<Beer> doQuery(SortOrder sortOrder, CharSequence filterText, Set<String> stylesToHide) throws SQLException {
        QueryBuilder<Beer, Long> qb = fBeerDao.buildSortedFilteredBeerQuery(fBreweryDao, sortOrder, filterText, stylesToHide, stylesToHide);
        return qb.query();
    }

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
}
