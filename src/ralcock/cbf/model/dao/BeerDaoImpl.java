package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Beer;

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

}
