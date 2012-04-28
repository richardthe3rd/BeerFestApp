package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;

public class BreweryDaoImpl extends BaseDaoImpl<Brewery, Long> implements BreweryDao {
    public BreweryDaoImpl(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Brewery.class);
    }
}
