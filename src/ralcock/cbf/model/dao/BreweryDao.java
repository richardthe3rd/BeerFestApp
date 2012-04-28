package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.Brewery;

import java.sql.SQLException;

public interface BreweryDao extends Dao<Brewery, Long> {
    QueryBuilder<Brewery, Long> buildFilteredBreweryQuery(CharSequence filterText);

    int updateFromFestival(Brewery brewery) throws SQLException;
}
