package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;

public class BreweryDaoImpl extends BaseDaoImpl<Brewery, Long> implements BreweryDao {
    public BreweryDaoImpl(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Brewery.class);
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
}
