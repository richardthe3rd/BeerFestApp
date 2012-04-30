package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.Brewery;
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

    public void updateFromFestivalOrCreate(final Beer beer) throws SQLException {
        SelectArg beerName = new SelectArg(beer.getName());
        SelectArg beerDescription = new SelectArg(beer.getDescription());
        SelectArg beerBrewery = new SelectArg(beer.getBrewery());
        SelectArg beerAbv = new SelectArg(beer.getAbv());
        SelectArg beerStatus = new SelectArg(beer.getStatus());
        SelectArg beerFestivalId = new SelectArg(beer.getFestivalID());

        UpdateBuilder<Beer, Long> updateBuilder = updateBuilder();
        updateBuilder.updateColumnValue(Beer.NAME_FIELD, beerName);
        updateBuilder.updateColumnValue(Beer.DESCRIPTION_FIELD, beerDescription);
        updateBuilder.updateColumnValue(Beer.BREWERY_FIELD, beerBrewery);
        updateBuilder.updateColumnValue(Beer.ABV_FIELD, beerAbv);
        updateBuilder.updateColumnValue(Beer.STATUS_FIELD, beerStatus);

        updateBuilder.where().eq(Beer.FESTIVAL_ID_FIELD, beerFestivalId);
        PreparedUpdate<Beer> preparedUpdate = updateBuilder.prepare();

        if (update(preparedUpdate) == 1) {
            // update the brewery's Id field to match the database
            QueryBuilder<Beer, Long> queryBuilder = queryBuilder();
            queryBuilder.where().eq(Brewery.FESTIVAL_ID_FIELD, beerFestivalId);
            beer.setId(queryBuilder.queryForFirst().getId());
        } else {
            create(beer);
        }

    }

}
