package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;

public class BreweryDaoImpl extends BaseDaoImpl<Brewery, Long> implements BreweryDao {
    public BreweryDaoImpl(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Brewery.class);
    }

    public BreweryDaoImpl(final ConnectionSource connectionSource, DatabaseTableConfig<Brewery> config) throws SQLException {
        super(connectionSource, config);
    }


    public QueryBuilder<Brewery, Long> buildFilteredBreweryQuery(final CharSequence filterText) {
        QueryBuilder<Brewery, Long> qb = queryBuilder();
        qb.selectColumns(Brewery.ID_FIELD);
        try {
            qb.where().like(Brewery.NAME_FIELD, "%" + filterText + "%");
            return qb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFromFestivalOrCreate(final Brewery brewery) throws SQLException {
        SelectArg breweryName = new SelectArg(brewery.getName());
        SelectArg breweryDescription = new SelectArg(brewery.getDescription());
        SelectArg breweryFestivalId = new SelectArg(brewery.getFestivalID());

        UpdateBuilder<Brewery, Long> updateBuilder = updateBuilder();
        updateBuilder.updateColumnValue(Brewery.NAME_FIELD, breweryName);
        updateBuilder.updateColumnValue(Brewery.DESCRIPTION_FIELD, breweryDescription);
        updateBuilder.where().eq(Brewery.FESTIVAL_ID_FIELD, breweryFestivalId);
        PreparedUpdate<Brewery> preparedUpdate = updateBuilder.prepare();

        if (update(preparedUpdate) == 1) {
            // update the brewery's Id field to match the database
            QueryBuilder<Brewery, Long> queryBuilder = queryBuilder();
            queryBuilder.where().eq(Brewery.FESTIVAL_ID_FIELD, breweryFestivalId);
            brewery.setId(queryBuilder.queryForFirst().getId());
        } else {
            create(brewery);
        }
    }

}
