package ralcock.cbf.model;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import ralcock.cbf.model.dao.BeerDao;
import ralcock.cbf.model.dao.BreweryDao;

import java.sql.SQLException;
import java.util.List;

public class BeerList {
    private final BeerDao fBeerDao;
    private final BreweryDao fBreweryDao;

    private CharSequence fFilterText;
    private SortOrder fSortOrder;

    private List<Beer> fBeerList;

    public BeerList(final BeerDao beerDao,
                    final BreweryDao breweryDao,
                    final SortOrder sortOrder,
                    final CharSequence filterText) {
        fBeerDao = beerDao;
        fBreweryDao = breweryDao;
        fSortOrder = sortOrder;
        fFilterText = filterText;

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
        fBeerList = buildList(fSortOrder, fFilterText);
    }

    public int getCount() {
        return fBeerList.size();
    }

    public Beer getBeerAt(final int i) {
        return fBeerList.get(i);
    }

    private QueryBuilder<Brewery, Long> makeBreweryQuery(final CharSequence charSequence) {
        QueryBuilder<Brewery, Long> qb = fBreweryDao.queryBuilder();
        qb.selectColumns(Brewery.ID_FIELD);
        try {
            qb.where().like(Brewery.NAME_FIELD, "%" + charSequence + "%");
            return qb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Beer> buildList(final SortOrder sortOrder,
                                 final CharSequence charSequence) {
        QueryBuilder<Beer, Long> qb = fBeerDao.queryBuilder();
        Where where = qb.where();
        try {
            where.like(Beer.NAME_FIELD, "%" + charSequence + "%");
            where.or();
            where.in(Beer.BREWERY_FIELD, makeBreweryQuery(charSequence));
            qb.orderBy(sortOrder.columnName(), sortOrder.ascending());
            return qb.query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
