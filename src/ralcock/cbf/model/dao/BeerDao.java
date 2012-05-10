package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.SortOrder;

import java.sql.SQLException;
import java.util.Set;

public interface BeerDao extends Dao<Beer, Long> {

    Beer getBeerWithId(long id) throws SQLException;

    long getNumberOfBeers() throws SQLException;

    QueryBuilder<Beer, Long> buildSortedFilteredBeerQuery(BreweryDao breweryDao,
                                                          SortOrder sortOrder,
                                                          CharSequence filterText,
                                                          Set<String> filterStyles,
                                                          Set<String> statusToHide);

    void updateFromFestivalOrCreate(Beer beer) throws SQLException;

    Set<String> getAvailableStyles() throws SQLException;
}
