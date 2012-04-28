package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.SortOrder;

import java.sql.SQLException;

public interface BeerDao extends Dao<Beer, Long> {

    Beer getBeerWithId(long id) throws SQLException;

    long getNumberOfBeers() throws SQLException;

    QueryBuilder<Beer, Long> buildSortedFilteredBeerQuery(BreweryDao breweryDao, SortOrder sortOrder, CharSequence filterText);

    int updateFromFestival(Beer beer) throws SQLException;
}
