package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.Dao;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerChangedListener;
import ralcock.cbf.model.SortOrder;

import java.util.List;
import java.util.Set;

public interface Beers extends Dao<Beer, Long> {
    void addBeerChangedListener(BeerChangedListener l);
    void removeBeerChangedListener(BeerChangedListener l);
    Beer getBeerWithId(long id);

    long getNumberOfBeers();

    List<Beer> allBeersList(SortOrder sortOrder,
                            CharSequence filterText,
                            Set<String> filterStyles,
                            Set<String> allergensToHide,
                            Set<String> statusToHide,
                            String categoryToExclude);

    List<Beer> bookmarkedBeersList(SortOrder sortOrder,
                                   CharSequence filterText,
                                   Set<String> filterStyles,
                                   Set<String> allergensToHide,
                                   Set<String> statusToHide);

    /**
     * Returns a list of beers filtered by the specified low/no alcohol category.
     * <p>
     * This method retrieves beers that match the given category,
     * applying additional filters for sort order, text search, styles, allergens, and status.
     *
     * @param sortOrder        the order in which to sort the beers
     * @param filterText       text to filter beer names/descriptions
     * @param filterStyles     set of beer styles to exclude from the results
     * @param allergensToHide  set of allergens to exclude beers containing them
     * @param statusToHide     set of beer statuses to exclude
     * @param category         the category of beers to include (e.g., "low-no")
     * @return a list of beers matching the specified category and filters
     */
    List<Beer> lowNoAlcoholBeersList(SortOrder sortOrder,
                                     CharSequence filterText,
                                     Set<String> filterStyles,
                                     Set<String> allergensToHide,
                                     Set<String> statusToHide,
                                     String category);

    void updateFromFestivalOrCreate(Beer beer);

    Set<String> getAvailableStyles();

    /**
     * Returns a set of all unique allergens present in the beers currently available in the database.
     *
     * @return a set of allergen names (as strings); the set may be empty if no allergens are found
     */
    Set<String> getAvailableAllergens();

    List<Beer> getRatedBeers();

    void updateBeer(Beer beer);
}
