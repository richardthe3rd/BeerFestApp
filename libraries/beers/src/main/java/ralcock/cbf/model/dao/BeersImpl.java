package ralcock.cbf.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerChangedListener;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.model.SortOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BeersImpl extends BaseDaoImpl<Beer, Long> implements Beers {

    private final Logger logger = LoggerFactory.getLogger(BeersImpl.class);

    private Breweries fBreweries;

    private final CopyOnWriteArrayList<BeerChangedListener> fListeners
        = new CopyOnWriteArrayList<BeerChangedListener>();

    private static BeerAccessException newBeerAccessException(final String msg, final Throwable cause) {
        LoggerFactory.getLogger(BeersImpl.class).error(msg, cause);
        return new BeerAccessException(msg, cause);
    }

    @SuppressWarnings("UnusedDeclaration")
    public BeersImpl(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Beer.class);
    }

    @SuppressWarnings("UnusedDeclaration")
    public BeersImpl(final ConnectionSource connectionSource, DatabaseTableConfig<Beer> config) throws SQLException {
        super(connectionSource, config);
    }

    public Beer getBeerWithId(final long id) {
        try {
            Beer beer = queryForId(id);
            logger.info("Got Beer {} '{}'", id, beer.getName());
            return beer;
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to getBeerWithId " + id, e);
        }
    }

    public long getNumberOfBeers() {
        try {
            return countOf();
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to getNumberOfBeers", e);
        }
    }

    public Set<String> getAvailableStyles() {
        QueryBuilder<Beer, Long> qb = queryBuilder();
        qb.selectColumns(Beer.STYLE_FIELD);
        qb.distinct();
        qb.orderBy(Beer.STYLE_FIELD, true);

        GenericRawResults<String[]> results = null;
        try {
            results = queryRaw(qb.prepareStatementString());
            Set<String> styles = new TreeSet<String>();
            List<String[]> resultList = results.getResults();
            for (String[] array : resultList) {
                final String style = array[0];
                if (style.length() > 0) {
                    styles.add(style);
                }
            }
            return styles;
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to get available styles", e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (IOException e) {
                    throw newBeerAccessException("Failed to close results.", e);
                }
            }
        }
    }

    public List<Beer> getRatedBeers() {
        try {
            QueryBuilder<Beer, Long> qb = queryBuilder();
            Where where = qb.where();
            where.gt(Beer.RATING_FIELD, 0);
            return qb.query();
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to get rated beers", e);
        }
    }

    public void updateBeer(final Beer beer) {
        try {
            update(beer);
            fireBeerChanged(beer);
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to update beer", e);
        }
    }

    public void addBeerChangedListener(BeerChangedListener l) {
        fListeners.add(l);
    }

    public void removeBeerChangedListener(BeerChangedListener l) {
        fListeners.remove(l);
    }

    private void fireBeerChanged(final Beer beer) {
        for(BeerChangedListener l : fListeners) {
            l.beerChanged(beer);
        }
    }

    public List<Beer> allBeersList(final SortOrder sortOrder,
                                   final CharSequence filterText,
                                   final Set<String> stylesToHide,
                                   final Set<String> allergensToHide,
                                   final Set<String> statusToHide) {
        QueryBuilder<Beer, Long> query = buildSortedFilteredBeerQuery(sortOrder, filterText, stylesToHide, statusToHide);
        try {
            List<Beer> beers = query.query();
            return filterByAllergens(beers, allergensToHide);
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to get all beers list", e);
        }
    }

    public List<Beer> bookmarkedBeersList(final SortOrder sortOrder,
                                          final CharSequence filterText,
                                          final Set<String> stylesToHide,
                                          final Set<String> allergensToHide,
                                          final Set<String> statusToHide) {
        QueryBuilder<Beer, Long> query = buildBookmarkQuery(sortOrder, filterText, stylesToHide, statusToHide);
        try {
            List<Beer> beers = query.query();
            return filterByAllergens(beers, allergensToHide);
        } catch (SQLException e) {
            throw newBeerAccessException("Failed to get bookmarked beer list", e);
        }
    }

    private List<Beer> filterByAllergens(final List<Beer> beers, final Set<String> allergensToHide) {
        if (allergensToHide == null || allergensToHide.isEmpty()) {
            return beers;
        }
        List<Beer> filtered = new java.util.ArrayList<>();
        for (Beer beer : beers) {
            if (!containsAnyAllergen(beer.getAllergens(), allergensToHide)) {
                filtered.add(beer);
            }
        }
        return filtered;
    }

    private boolean containsAnyAllergen(final String beerAllergens, final Set<String> allergensToHide) {
        if (beerAllergens == null || beerAllergens.isEmpty()) {
            return false;
        }
        String lowerAllergens = beerAllergens.toLowerCase();
        for (String allergen : allergensToHide) {
            if (lowerAllergens.contains(allergen.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private QueryBuilder<Beer, Long> buildBookmarkQuery(final SortOrder sortOrder,
                                                        final CharSequence filterText,
                                                        final Set<String> stylesToHide,
                                                        final Set<String> statusToHide) {
        QueryBuilder<Beer, Long> qb = queryBuilder();
        Where where = qb.where();
        try {
            doWhere(where, fBreweries, filterText, stylesToHide, statusToHide);
            where.and().eq(Beer.ON_WISH_LIST_FIELD, true);
            qb.orderBy(sortOrder.columnName(), sortOrder.ascending());
            return qb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private QueryBuilder<Beer, Long> buildSortedFilteredBeerQuery(final SortOrder sortOrder,
                                                                  final CharSequence filterText,
                                                                  final Set<String> stylesToHide,
                                                                  final Set<String> statusToHide) {
        QueryBuilder<Beer, Long> qb = queryBuilder();
        Where where = qb.where();
        try {
            doWhere(where, fBreweries, filterText, stylesToHide, statusToHide);
            qb.orderBy(sortOrder.columnName(), sortOrder.ascending());
            return qb;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void doWhere(final Where where,
                                final Breweries breweries,
                                final CharSequence filterText,
                                final Set<String> stylesToHide,
                                final Set<String> statusToHide) throws SQLException {
        //noinspection unchecked
        SelectArg nameFilter = new SelectArg("%"+filterText+"%");
        SelectArg styleFilter = new SelectArg("%"+filterText+"%");
        SelectArg descFilter = new SelectArg("%"+filterText+"%");
        where.and(
                where.not().in(Beer.STATUS_FIELD, statusToHide),
                where.not().in(Beer.STYLE_FIELD, stylesToHide),
                where.or(
                        where.or(
                                where.like(Beer.NAME_FIELD, nameFilter),
                                where.like(Beer.STYLE_FIELD, styleFilter),
                                where.like(Beer.DESCRIPTION_FIELD, descFilter)
                        ),
                        where.in(Beer.BREWERY_FIELD, breweries.buildFilteredBreweryQuery(filterText))
                )
        );
    }

    public void updateFromFestivalOrCreate(final Beer festivalBeerDescription) {
        final Brewery brewery = festivalBeerDescription.getBrewery();
        if (brewery.getId() == 0) {
            fBreweries.updateFromFestivalOrCreate(brewery);
        }

        if (festivalBeerDescription.getId() == 0) {
            try {
                doUpdateOrCreate(festivalBeerDescription);
            } catch (SQLException e) {
                throw newBeerAccessException("Failed to update beer " + festivalBeerDescription + " from festival description", e);
            }
        }
    }

    private void doUpdateOrCreate(final Beer festivalBeerDescription) throws SQLException {
        SelectArg beerName = new SelectArg(festivalBeerDescription.getName());
        SelectArg beerDescription = new SelectArg(festivalBeerDescription.getDescription());
        SelectArg beerBrewery = new SelectArg(festivalBeerDescription.getBrewery());
        SelectArg beerAbv = new SelectArg(festivalBeerDescription.getAbv());
        SelectArg beerStatus = new SelectArg(festivalBeerDescription.getStatus());
        SelectArg beerStyle = new SelectArg(festivalBeerDescription.getStyle());
        SelectArg beerAllergens = new SelectArg(festivalBeerDescription.getAllergens());
        SelectArg beerFestivalId = new SelectArg(festivalBeerDescription.getFestivalID());

        UpdateBuilder<Beer, Long> updateBuilder = updateBuilder();
        updateBuilder.updateColumnValue(Beer.NAME_FIELD, beerName);
        updateBuilder.updateColumnValue(Beer.DESCRIPTION_FIELD, beerDescription);
        updateBuilder.updateColumnValue(Beer.BREWERY_FIELD, beerBrewery);
        updateBuilder.updateColumnValue(Beer.ABV_FIELD, beerAbv);
        updateBuilder.updateColumnValue(Beer.STATUS_FIELD, beerStatus);
        updateBuilder.updateColumnValue(Beer.STYLE_FIELD, beerStyle);
        updateBuilder.updateColumnValue(Beer.ALLERGENS_FIELD, beerAllergens);

        updateBuilder.where().eq(Beer.FESTIVAL_ID_FIELD, beerFestivalId);
        PreparedUpdate<Beer> preparedUpdate = updateBuilder.prepare();

        if (update(preparedUpdate) == 1) {
            // update the brewery's Id field to match the database
            QueryBuilder<Beer, Long> queryBuilder = queryBuilder();
            queryBuilder.where().eq(Brewery.FESTIVAL_ID_FIELD, beerFestivalId);
            festivalBeerDescription.setId(queryBuilder.queryForFirst().getId());
        } else {
            create(festivalBeerDescription);
        }
    }

    public void setBreweries(final Breweries breweries) {
        fBreweries = breweries;
    }
}
