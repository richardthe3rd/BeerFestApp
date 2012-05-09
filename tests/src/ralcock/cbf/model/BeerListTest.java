package ralcock.cbf.model;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BeerListTest extends AndroidTestCase {

    private BeerDatabaseHelper fBeerDatabaseHelper;
    private Beer fBeer1;
    private Beer fBeer2;
    private Beer fBeer3;
    private BeerDao fBeerDao;
    private BreweryDao fBreweryDao;
    private RenamingDelegatingContext fContext;
    private String fStyle1 = "style1";
    private String fStyle2 = "style2";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fContext = new RenamingDelegatingContext(getContext(), BeerListTest.class.getSimpleName() + ".");
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

    public void testFiltering() throws Exception {
        Set<String> emptySet = Collections.emptySet();
        BeerList list = new BeerList(fBeerDao, fBreweryDao,
                SortOrder.BEER_NAME_ASC, "mild", emptySet);
        assertEquals(1, list.getCount());
        assertEquals(fBeer1, list.getBeerAt(0));

        list.filterBy("best");
        assertEquals(2, list.getCount());
        assertEquals(fBeer2, list.getBeerAt(0));
        assertEquals(fBeer3, list.getBeerAt(1));
    }

    public void testSorting() throws Exception {
        Set<String> emptySet = Collections.emptySet();
        BeerList list = new BeerList(fBeerDao, fBreweryDao,
                SortOrder.BEER_ABV_ASC, "", emptySet);
        assertEquals(3, list.getCount());

        Beer[] expectedBeers = new Beer[]{fBeer1, fBeer2, fBeer3};
        for (int n = 0; n < 3; n++) {
            assertEquals("Checking beer " + n, expectedBeers[n], list.getBeerAt(n));
        }
        ;

        list.sortBy(SortOrder.BEER_ABV_DESC);
        assertEquals(3, list.getCount());
        Beer[] expectedBeers2 = new Beer[]{fBeer3, fBeer2, fBeer1};
        for (int n = 2; n > -1; n--) {
            assertEquals("Checking beer " + n, expectedBeers2[n], list.getBeerAt(n));
        }
    }

    public void testFilterByStyle() throws Exception {
        Set<String> stylesToHide = new HashSet<String>();
        stylesToHide.add(fStyle2);

        BeerList list = new BeerList(fBeerDao, fBreweryDao,
                SortOrder.BEER_ABV_ASC, "", stylesToHide);

        assertEquals(1, list.getCount());

    }

    public void testStyles() throws Exception {
        Set<String> styles = new HashSet<String>();
        styles.add(fStyle1);
        styles.add(fStyle2);

        assertEquals(styles, fBeerDao.getAvailableStyles());
    }
}
