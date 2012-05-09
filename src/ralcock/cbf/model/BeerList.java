package ralcock.cbf.model;

import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class BeerList {
    private final BeerDao fBeerDao;
    private final BreweryDao fBreweryDao;

    private CharSequence fFilterText;
    private SortOrder fSortOrder;

    private List<Beer> fBeerList;
    private Set<String> fFilterStyles;

    public BeerList(final BeerDao beerDao,
                    final BreweryDao breweryDao,
                    final SortOrder sortOrder,
                    final CharSequence filterText,
                    final Set<String> filterStyles) {
        fBeerDao = beerDao;
        fBreweryDao = breweryDao;
        fSortOrder = sortOrder;
        fFilterText = filterText;
        fFilterStyles = filterStyles;

        updateBeerList();
    }

    public void stylesToHide(final Set<String> stylesToShow) {
        fFilterStyles = stylesToShow;
        updateBeerList();
    }

    public void filterBy(CharSequence filterText) {
        fFilterText = filterText;
        updateBeerList();
    }

    public void sortBy(SortOrder sortOrder) {
        fSortOrder = sortOrder;
        updateBeerList();
    }

    public void updateBeerList() {
        fBeerList = buildList(fSortOrder, fFilterText, fFilterStyles);
    }

    public int getCount() {
        return fBeerList.size();
    }

    public Beer getBeerAt(final int i) {
        return fBeerList.get(i);
    }

    private List<Beer> buildList(final SortOrder sortOrder,
                                 final CharSequence filterText,
                                 final Set<String> stylesToHide) {
        try {
            QueryBuilder<Beer, Long> qb = fBeerDao.buildSortedFilteredBeerQuery(fBreweryDao, sortOrder, filterText, stylesToHide);
            return qb.query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
