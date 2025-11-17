package ralcock.cbf.model.dao;


import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.SortOrder;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeersImplTest extends AndroidTestCase {
    private BeerDatabaseHelper fBeerDatabaseHelper;
    private Beers fBeers;
    private RenamingDelegatingContext fContext;

    private Beer fBeer1;
    private Beer fBeer2;
    private Beer fBeer3;

    private String fStyle1 = "style1";
    private String fStyle2 = "style2";
    private Breweries fBreweries;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fContext = new RenamingDelegatingContext(getContext(),
                BeersImplTest.class.getSimpleName() + ".");
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

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        fBeerDatabaseHelper.close();
        fContext.deleteDatabase(BeerDatabaseHelper.DATABASE_NAME);
    }

    private List<Beer> doQuery(SortOrder sortOrder, CharSequence filterText, Set<String> stylesToHide) throws SQLException {
        return fBeers.allBeersList(sortOrder, filterText, stylesToHide, stylesToHide);
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

    public void testGetAvailableStyles() throws Exception {
        Set<String> styles = new HashSet<String>();
        styles.add("style1");
        styles.add("style2");
        assertEquals(styles, fBeers.getAvailableStyles());
    }

}
