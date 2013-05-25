package ralcock.cbf.model;

import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeerList {

    private static enum Type {
        ALL,
        BOOKMARKS
    }

    public static class Config {
        public Config() {

        }

        public Config(final SortOrder sortOrder,
                      final CharSequence searchText,
                      final Set<String> stylesToHide,
                      final StatusToShow statusToShow) {
            SortOrder = sortOrder;
            SearchText = searchText;
            StylesToHide = stylesToHide;
            StatusToShow = statusToShow;
        }

        public SortOrder SortOrder;
        public CharSequence SearchText;
        public Set<String> StylesToHide;
        public StatusToShow StatusToShow;
    }

    ;

    private final BeerDao fBeerDao;
    private final BreweryDao fBreweryDao;

    private final Type fType;

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
                    final Type type,
                    final Config config) throws SQLException {
        fBeerDao = beerDao;
        fBreweryDao = breweryDao;
        fType = type;
        fSortOrder = config.SortOrder;
        fFilterText = config.SearchText;
        fFilterStyles = config.StylesToHide;
        fStatusToHide = statusToHide(config.StatusToShow);
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

    public void setStatusToShow(final StatusToShow statusToShow) throws SQLException {
        fStatusToHide = statusToHide(statusToShow);
        updateBeerList();
    }

    private Set<String> statusToHide(final StatusToShow statusToShow) {
        if (statusToShow == StatusToShow.AVAILABLE_ONLY) {
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
        if (fType == Type.ALL)
            return fBeerDao.allBeersList(fBreweryDao,
                    sortOrder, filterText, stylesToHide, statusToHide);
        else {
            return fBeerDao.bookmarkedBeersList(fBreweryDao,
                    sortOrder, filterText, stylesToHide, statusToHide);
        }
    }

    public static BeerList allBeers(final BeerDao beerDao,
                                    final BreweryDao breweryDao,
                                    final Config config) throws SQLException {
        return new BeerList(beerDao, breweryDao,
                Type.ALL, config);
    }

    public static BeerList bookmarkedBeers(final BeerDao beerDao,
                                           final BreweryDao breweryDao,
                                           final Config config) throws SQLException {
        return new BeerList(beerDao, breweryDao,
                Type.BOOKMARKS, config);
    }

}
