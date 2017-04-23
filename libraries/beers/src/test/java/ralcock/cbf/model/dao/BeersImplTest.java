package ralcock.cbf.model.dao;

import ralcock.cbf.model.Beer;
import ralcock.cbf.model.Brewery;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeersImplTest {

    private ConnectionSource connectionSource;
    private BeersImpl beersImpl;
    private BreweriesImpl breweriesImpl;

    private static Brewery aBrewery() {
        return new Brewery("festivalId", "name", "description");
    }

    private static Beer aBeer() {
        return new  Beer("festivalId", "name", 4.2f,
                         "description", "style", "status", "cask", aBrewery());
    }

    @Before
    public void setup() throws Exception {
        connectionSource = new JdbcConnectionSource("jdbc:h2:mem:test");
        TableUtils.createTable(connectionSource, Beer.class);
        TableUtils.createTable(connectionSource, Brewery.class);
        beersImpl = DaoManager.createDao(connectionSource, Beer.class);
        breweriesImpl = DaoManager.createDao(connectionSource, Brewery.class);
        beersImpl.setBreweries(breweriesImpl);
    }

    @After
    public void dropTables() throws Exception {
        TableUtils.dropTable(connectionSource, Beer.class, true);
        TableUtils.dropTable(connectionSource, Brewery.class, true);
    }

    private BeersImpl getBeers() throws Exception {
        return beersImpl;
    }

    @Test
    public void defaultConstructedHasNoBeers() throws Exception {
        BeersImpl beers = getBeers();
        assertEquals(beers.getNumberOfBeers(), 0);
    }

    @Test
    public void canAddBeer() throws Exception {
        BeersImpl beers = getBeers();
        beers.updateFromFestivalOrCreate(aBeer());
        assertEquals(beers.getNumberOfBeers(), 1);
    }
}
