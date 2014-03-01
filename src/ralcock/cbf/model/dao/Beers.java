package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.SortOrder;

import java.util.List;
import java.util.Set;

public interface Beers extends Dao<Beer, Long> {

    Beer getBeerWithId(long id);

    long getNumberOfBeers();

    List<Beer> allBeersList(SortOrder sortOrder,
                            CharSequence filterText,
                            Set<String> filterStyles,
                            Set<String> statusToHide);

    List<Beer> bookmarkedBeersList(SortOrder sortOrder,
                                   CharSequence filterText,
                                   Set<String> filterStyles,
                                   Set<String> statusToHide);

    void updateFromFestivalOrCreate(Beer beer);

    Set<String> getAvailableStyles();

    List<Beer> getRatedBeers();

    void updateBeer(Beer beer);
}
