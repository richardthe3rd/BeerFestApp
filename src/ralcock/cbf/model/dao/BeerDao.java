package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import ralcock.cbf.model.Beer;

import java.sql.SQLException;

public interface BeerDao extends Dao<Beer, Long> {

    Beer getBeerWithId(long id) throws SQLException;

    long getNumberOfBeers() throws SQLException;
}
