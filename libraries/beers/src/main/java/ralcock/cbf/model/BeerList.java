package ralcock.cbf.model;

import ralcock.cbf.model.dao.Beers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeerList {

    private static enum Type {
        ALL,
        BOOKMARKS,
        LOW_NO
    }

    private static final float LOW_NO_MAX_ABV = 0.5f;

    public static class Config {
        public Config() {

        }

        public Config(final SortOrder sortOrder,
                      final CharSequence searchText,
                      final Set<String> stylesToHide,
                      final StatusToShow statusToShow) {
            SortOrder = sortOrder;
            SearchText = searchText;
            StylesToHide = stylesToHide;
            StatusToShow = statusToShow;
        }

        public Config(final SortOrder sortOrder,
                      final CharSequence searchText,
                      final Set<String> stylesToHide,
                      final Set<String> allergensToHide,
                      final StatusToShow statusToShow) {
            SortOrder = sortOrder;
            SearchText = searchText;
            StylesToHide = stylesToHide;
            AllergensToHide = allergensToHide;
            StatusToShow = statusToShow;
        }

        public SortOrder SortOrder = ralcock.cbf.model.SortOrder.BREWERY_NAME_DESC;
        public CharSequence SearchText = "";
        public Set<String> StylesToHide = Collections.emptySet();
        public Set<String> AllergensToHide = Collections.emptySet();
        public StatusToShow StatusToShow = ralcock.cbf.model.StatusToShow.ALL;

        public Config withSortOrder(final SortOrder sortOrder) {
            SortOrder = sortOrder;
            return this;
        }

        public Config withSearchText(final String searchText) {
            SearchText = searchText;
            return this;
        }

        public Config withStylesToHide(final Set<String> stylesToHide) {
            StylesToHide = stylesToHide;
            return this;
        }

        public Config withAllergensToHide(final Set<String> allergensToHide) {
            AllergensToHide = allergensToHide;
            return this;
        }
    }


    private final Beers fBeers;

    private final Type fType;

    private CharSequence fFilterText;
    private SortOrder fSortOrder;

    private List<Beer> fBeerList;
    private Set<String> fFilterStyles;
    private Set<String> fAllergensToHide;
    private Set<String> fStatusToHide;

    private static final Set<String> UNAVAILABLE_STATUS_SET;
    static {
        Set<String> set = new HashSet<>();
        set.add("Ordered");
        set.add("Arrived");
        set.add("Sold Out");
        UNAVAILABLE_STATUS_SET = Collections.unmodifiableSet(set);
    }

    public BeerList(final Beers beers,
                    final Type type,
                    final Config config) {
        fBeers = beers;
        fType = type;
        fSortOrder = config.SortOrder;
        fFilterText = config.SearchText;
        fFilterStyles = config.StylesToHide;
        fAllergensToHide = config.AllergensToHide;
        fStatusToHide = statusToHide(config.StatusToShow);
        updateBeerList();
    }

    public void stylesToHide(final Set<String> stylesToShow) {
        fFilterStyles = stylesToShow;
        updateBeerList();
    }

    public void allergensToHide(final Set<String> allergensToHide) {
        fAllergensToHide = allergensToHide;
        updateBeerList();
    }

    public void filterBy(final CharSequence filterText) {
        fFilterText = filterText;
        updateBeerList();
    }

    public void sortBy(final SortOrder sortOrder) {
        fSortOrder = sortOrder;
        updateBeerList();
    }

    public void setStatusToShow(final StatusToShow statusToShow) {
        fStatusToHide = statusToHide(statusToShow);
        updateBeerList();
    }

    private Set<String> statusToHide(final StatusToShow statusToShow) {
        if (statusToShow == StatusToShow.AVAILABLE_ONLY) {
            return UNAVAILABLE_STATUS_SET;
        } else {
            // Hide nothing
            return Collections.emptySet();
        }
    }

    public void updateBeerList() {
        fBeerList = buildList(fSortOrder, fFilterText, fFilterStyles, fAllergensToHide, fStatusToHide);
    }

    public int getCount() {
        return fBeerList.size();
    }

    public Beer getBeerAt(final int i) {
        return fBeerList.get(i);
    }

    private List<Beer> buildList(final SortOrder sortOrder,
                                 final CharSequence filterText,
                                 final Set<String> stylesToHide,
                                 final Set<String> allergensToHide,
                                 final Set<String> statusToHide) {
        switch (fType) {
            case ALL:
                return fBeers.allBeersList(sortOrder, filterText, stylesToHide, allergensToHide, statusToHide);
            case BOOKMARKS:
                return fBeers.bookmarkedBeersList(sortOrder, filterText, stylesToHide, allergensToHide, statusToHide);
            case LOW_NO:
                return fBeers.lowNoAlcoholBeersList(sortOrder, filterText, stylesToHide, allergensToHide, statusToHide, LOW_NO_MAX_ABV);
            default:
                return fBeers.allBeersList(sortOrder, filterText, stylesToHide, allergensToHide, statusToHide);
        }
    }

    public static BeerList allBeers(final Beers beers,
                                    final Config config) {
        return new BeerList(beers, Type.ALL, config);
    }

    public static BeerList bookmarkedBeers(final Beers beers,
                                           final Config config) {
        return new BeerList(beers, Type.BOOKMARKS, config);
    }

    public static BeerList lowNoAlcoholBeers(final Beers beers,
                                              final Config config) {
        return new BeerList(beers, Type.LOW_NO, config);
    }

}
