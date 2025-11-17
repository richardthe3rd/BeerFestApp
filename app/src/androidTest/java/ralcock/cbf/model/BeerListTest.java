package ralcock.cbf.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ralcock.cbf.model.dao.Beers;
import ralcock.cbf.model.dao.Breweries;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BeerListTest {

    private static final Set<String> EMPTY_SET = Collections.emptySet();

    private Beers fBeers;
    private Breweries fBreweries;

    @Before
    public void setUp() {
        fBeers = EasyMock.createMock(Beers.class);
        fBreweries = EasyMock.createMock(Breweries.class);
    }

    @Test
    public void testFiltering() {
        final SortOrder sortOrder = SortOrder.BEER_NAME_ASC;

        final String mild = "mild";
        final String best = "best";

        final Beer aMild = new BeerBuilder().called("A Mild").withStyle(mild).build();
        final Beer aBest = new BeerBuilder().called("A Best").withStyle(best).build();
        final Beer anotherBest = new BeerBuilder().called("Another Best").withStyle(best).build();

        expect(fBeers.allBeersList(
                eq(sortOrder),
                eq(mild),
                eq(EMPTY_SET),
                eq(EMPTY_SET)
        )).andReturn(Arrays.asList(aMild));

        expect(fBeers.allBeersList(
                eq(sortOrder),
                eq(best),
                eq(EMPTY_SET),
                eq(EMPTY_SET)
        )).andReturn(Arrays.asList(aBest, anotherBest));

        replay(fBeers, fBreweries);

        final BeerList.Config config = new BeerList.Config().withSortOrder(sortOrder).withSearchText(mild);
        BeerList list = BeerList.allBeers(fBeers, config);

        assertEquals(1, list.getCount());
        assertEquals(aMild, list.getBeerAt(0));

        list.filterBy("best");
        assertEquals(2, list.getCount());
        assertEquals(aBest, list.getBeerAt(0));
        assertEquals(anotherBest, list.getBeerAt(1));

        verify(fBeers, fBreweries);
    }

    @Test
    public void testSorting() {
        final SortOrder sortOrder1 = SortOrder.BEER_ABV_ASC;
        final String filterText = "";

        final Beer beer1 = new Beer();
        final Beer beer2 = new Beer();
        final Beer beer3 = new Beer();

        expect(fBeers.allBeersList(
                eq(sortOrder1),
                eq(filterText),
                eq(EMPTY_SET),
                eq(EMPTY_SET)
        )).andReturn(Arrays.asList(beer1, beer2, beer3));

        final SortOrder sortOrder2 = SortOrder.BREWERY_NAME_DESC;
        expect(fBeers.allBeersList(
                eq(sortOrder2),
                eq(filterText),
                eq(EMPTY_SET),
                eq(EMPTY_SET)
        )).andReturn(Arrays.asList(beer2, beer1, beer3));
        replay(fBeers, fBreweries);

        BeerList list = BeerList.allBeers(fBeers, new BeerList.Config(sortOrder1, filterText, EMPTY_SET, StatusToShow.ALL));

        assertEquals(3, list.getCount());

        Beer[] expectedBeers = new Beer[]{beer1, beer2, beer3};
        for (int n = 0; n < 3; n++) {
            assertEquals("Checking beer " + n, expectedBeers[n], list.getBeerAt(n));
        }

        list.sortBy(sortOrder2);
        assertEquals(3, list.getCount());
        Beer[] expectedBeers2 = new Beer[]{beer2, beer1, beer3};
        for (int n = 2; n > -1; n--) {
            assertEquals("Checking beer " + n, expectedBeers2[n], list.getBeerAt(n));
        }
    }

    @Test
    public void testFilterByStyle() {
        Set<String> stylesToHide = new HashSet<String>();
        stylesToHide.add("style2");

        final SortOrder sortOrder = SortOrder.BEER_NAME_ASC;
        final String filterText = "";

        expect(fBeers.allBeersList(
                eq(sortOrder),
                eq(filterText),
                eq(stylesToHide),
                eq(EMPTY_SET)
        )).andReturn(Arrays.asList(new Beer()));
        replay(fBeers, fBreweries);

        BeerList list = BeerList.allBeers(fBeers,
                new BeerList.Config(sortOrder, filterText, stylesToHide, StatusToShow.ALL));

        assertEquals(1, list.getCount());
        verify(fBeers, fBreweries);
    }
}
