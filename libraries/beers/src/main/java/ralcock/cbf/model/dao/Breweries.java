package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import ralcock.cbf.model.Brewery;

public interface Breweries extends Dao<Brewery, Long> {
    QueryBuilder<Brewery, Long> buildFilteredBreweryQuery(CharSequence filterText);

    void updateFromFestivalOrCreate(Brewery brewery);
}
