package ralcock.cbf.model;

import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeerList {
    private final BeerDao fBeerDao;
    private final BreweryDao fBreweryDao;

    private CharSequence fFilterText;
    private SortOrder fSortOrder;

    private List<Beer> fBeerList;
    private Set<String> fFilterStyles;
    private Set<String> fStatusToHide;

    private static final Set<String> UNAVAILABLE_STATUS_SET = new HashSet<String>() {{
        add("Ordered");
        add("Arrived");
        add("Sold Out");
    }};

    public BeerList(final BeerDao beerDao,
                    final BreweryDao breweryDao,
                    final SortOrder sortOrder,
                    final CharSequence filterText,
                    final Set<String> filterStyles,
                    final boolean hideUnavailableBeers) throws SQLException {
        fBeerDao = beerDao;
        fBreweryDao = breweryDao;
        fSortOrder = sortOrder;
        fFilterText = filterText;
        fFilterStyles = filterStyles;
        fStatusToHide = statusToHide(hideUnavailableBeers);
        updateBeerList();
    }

    public void stylesToHide(final Set<String> stylesToShow) throws SQLException {
        fFilterStyles = stylesToShow;
        updateBeerList();
    }

    public void filterBy(CharSequence filterText) throws SQLException {
        fFilterText = filterText;
        updateBeerList();
    }

    public void sortBy(SortOrder sortOrder) throws SQLException {
        fSortOrder = sortOrder;
        updateBeerList();
    }

    public void hideUnavailableBeers(final boolean hideUnavailable) throws SQLException {
        fStatusToHide = statusToHide(hideUnavailable);
        updateBeerList();
    }

    private Set<String> statusToHide(final boolean hideUnavailable) {
        if (hideUnavailable) {
            return UNAVAILABLE_STATUS_SET;
        } else {
            // Hide nothing
            return Collections.emptySet();
        }
    }

    public void updateBeerList() throws SQLException {
        fBeerList = buildList(fSortOrder, fFilterText, fFilterStyles, fStatusToHide);
    }

    public int getCount() {
        return fBeerList.size();
    }

    public Beer getBeerAt(final int i) {
        return fBeerList.get(i);
    }

    private List<Beer> buildList(final SortOrder sortOrder,
                                 final CharSequence filterText,
                                 final Set<String> stylesToHide,
                                 final Set<String> statusToHide) throws SQLException {
        QueryBuilder<Beer, Long> qb = fBeerDao.buildSortedFilteredBeerQuery(fBreweryDao,
                sortOrder, filterText, stylesToHide, statusToHide);
        return qb.query();
    }

}
