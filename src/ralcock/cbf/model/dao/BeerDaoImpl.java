package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.SortOrder;

import java.sql.SQLException;

public class BeerDaoImpl extends BaseDaoImpl<Beer, Long> implements BeerDao {
    public BeerDaoImpl(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Beer.class);
    }

    public Beer getBeerWithId(final long id) throws SQLException {
        return queryForId(id);
    }

    public long getNumberOfBeers() throws SQLException {
        return countOf();
    }

    public QueryBuilder<Beer, Long> buildSortedFilteredBeerQuery(final BreweryDao breweryDao,
                                                                 final SortOrder sortOrder,
                                                                 final CharSequence filterText) {
        QueryBuilder<Beer, Long> qb = queryBuilder();
        Where where = qb.where();
        try {
            where.like(Beer.NAME_FIELD, "%" + filterText + "%");
            where.or();
            where.in(Beer.BREWERY_FIELD, breweryDao.buildFilteredBreweryQuery(filterText));
            qb.orderBy(sortOrder.columnName(), sortOrder.ascending());
            return qb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
