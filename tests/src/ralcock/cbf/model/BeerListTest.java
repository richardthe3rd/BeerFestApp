package ralcock.cbf.model;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

public class BeerListTest extends AndroidTestCase {

    private BeerDatabaseHelper fBeerDatabaseHelper;
    private Beer fBeer1;
    private Beer fBeer2;
    private Beer fBeer3;
    private BeerDao fBeerDao;
    private BreweryDao fBreweryDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(),
                BeerListTest.class.getSimpleName());
        fBeerDatabaseHelper = new BeerDatabaseHelper(context);
        fBeerDao = fBeerDatabaseHelper.getBeerDao();
        fBreweryDao = fBeerDatabaseHelper.getBreweryDao();

        Brewery brewery = new Brewery("id1", "First Brewery", "y");
        fBreweryDao.create(brewery);

        Brewery brewery2 = new Brewery("id2", "Best Brewery", "y");
        fBreweryDao.create(brewery2);

        fBeer1 = new Beer(brewery, "A Mild", 1f, "", "");
        fBeerDao.create(fBeer1);

        fBeer2 = new Beer(brewery, "A Best Bitter", 2f, "", "");
        fBeerDao.create(fBeer2);

        fBeer3 = new Beer(brewery2, "A Stout", 3f, "", "");
        fBeerDao.create(fBeer3);

        assertEquals(3, fBeerDao.getNumberOfBeers());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        fBeerDatabaseHelper.close();
    }

    public void testFiltering() throws Exception {
        BeerList list = new BeerList(fBeerDao, fBreweryDao,
                SortOrder.BEER_NAME_ASC, "mild");
        assertEquals(1, list.getCount());
        assertEquals(fBeer1, list.getBeerAt(0));

        list.filterBy("best");
        assertEquals(2, list.getCount());
        assertEquals(fBeer2, list.getBeerAt(0));
        assertEquals(fBeer3, list.getBeerAt(1));
    }

    public void testSorting() throws Exception {
        BeerList list = new BeerList(fBeerDao, fBreweryDao,
                SortOrder.BEER_ABV_ASC, "");
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
        ;


    }
}
