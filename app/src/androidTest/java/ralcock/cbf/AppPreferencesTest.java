package ralcock.cbf;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.StatusToShow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AppPreferencesTest {

    private AppPreferences fAppPreferences;
    private Context fContext;

    @Before
    public void setUp() {
        fContext = ApplicationProvider.getApplicationContext();
        fAppPreferences = new AppPreferences(fContext);
        // Clear all preferences before each test
        fContext.getSharedPreferences(CamBeerFestApplication.class.getSimpleName(), 0)
                .edit()
                .clear()
                .commit();
    }

    @After
    public void tearDown() {
        // Clean up preferences after each test
        fContext.getSharedPreferences(CamBeerFestApplication.class.getSimpleName(), 0)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void getSortOrder_returnsDefaultWhenNotSet() {
        SortOrder sortOrder = fAppPreferences.getSortOrder();
        assertThat(sortOrder, equalTo(SortOrder.BREWERY_NAME_ASC));
    }

    @Test
    public void setSortOrder_storesSortOrder() {
        fAppPreferences.setSortOrder(SortOrder.BEER_NAME_ASC);
        SortOrder retrieved = fAppPreferences.getSortOrder();
        assertThat(retrieved, equalTo(SortOrder.BEER_NAME_ASC));
    }

    @Test
    public void setSortOrder_persistsAcrossInstances() {
        fAppPreferences.setSortOrder(SortOrder.BEER_ABV_DESC);

        AppPreferences newInstance = new AppPreferences(fContext);
        SortOrder retrieved = newInstance.getSortOrder();
        assertThat(retrieved, equalTo(SortOrder.BEER_ABV_DESC));
    }

    @Test
    public void setSortOrder_canSetAllSortOrderValues() {
        for (SortOrder sortOrder : SortOrder.values()) {
            fAppPreferences.setSortOrder(sortOrder);
            SortOrder retrieved = fAppPreferences.getSortOrder();
            assertThat("Sort order should match for " + sortOrder,
                      retrieved, equalTo(sortOrder));
        }
    }

    @Test
    public void getFilterText_returnsEmptyStringWhenNotSet() {
        String filterText = fAppPreferences.getFilterText();
        assertThat(filterText, equalTo(""));
    }

    @Test
    public void setFilterText_storesFilterText() {
        fAppPreferences.setFilterText("IPA");
        String retrieved = fAppPreferences.getFilterText();
        assertThat(retrieved, equalTo("IPA"));
    }

    @Test
    public void setFilterText_persistsAcrossInstances() {
        fAppPreferences.setFilterText("Stout");

        AppPreferences newInstance = new AppPreferences(fContext);
        String retrieved = newInstance.getFilterText();
        assertThat(retrieved, equalTo("Stout"));
    }

    @Test
    public void setFilterText_handlesEmptyString() {
        fAppPreferences.setFilterText("IPA");
        fAppPreferences.setFilterText("");
        String retrieved = fAppPreferences.getFilterText();
        assertThat(retrieved, equalTo(""));
    }

    @Test
    public void setFilterText_handlesSpecialCharacters() {
        String specialText = "Test & Beer's \"Ale\" (5%)";
        fAppPreferences.setFilterText(specialText);
        String retrieved = fAppPreferences.getFilterText();
        assertThat(retrieved, equalTo(specialText));
    }

    @Test
    public void getStylesToHide_returnsEmptySetWhenNotSet() {
        Set<String> styles = fAppPreferences.getStylesToHide();
        assertThat(styles, notNullValue());
        assertThat(styles.isEmpty(), is(true));
    }

    @Test
    public void setStylesToHide_storesStyles() {
        Set<String> styles = new HashSet<>(Arrays.asList("IPA", "Stout", "Lager"));
        fAppPreferences.setStylesToHide(styles);

        Set<String> retrieved = fAppPreferences.getStylesToHide();
        assertThat(retrieved, equalTo(styles));
    }

    @Test
    public void setStylesToHide_persistsAcrossInstances() {
        Set<String> styles = new HashSet<>(Arrays.asList("Pale Ale", "Porter"));
        fAppPreferences.setStylesToHide(styles);

        AppPreferences newInstance = new AppPreferences(fContext);
        Set<String> retrieved = newInstance.getStylesToHide();
        assertThat(retrieved, equalTo(styles));
    }

    @Test
    public void setStylesToHide_handlesEmptySet() {
        Set<String> styles = new HashSet<>();
        fAppPreferences.setStylesToHide(styles);

        Set<String> retrieved = fAppPreferences.getStylesToHide();
        assertThat(retrieved.isEmpty(), is(true));
    }

    @Test
    public void setStylesToHide_handlesSingleStyle() {
        Set<String> styles = new HashSet<>(Arrays.asList("IPA"));
        fAppPreferences.setStylesToHide(styles);

        Set<String> retrieved = fAppPreferences.getStylesToHide();
        assertThat(retrieved, hasSize(1));
        assertThat(retrieved, contains("IPA"));
    }

    @Test
    public void setStylesToHide_handlesStylesWithSpecialCharacters() {
        Set<String> styles = new HashSet<>(Arrays.asList("American IPA", "Milk Stout", "Session Ale"));
        fAppPreferences.setStylesToHide(styles);

        Set<String> retrieved = fAppPreferences.getStylesToHide();
        assertThat(retrieved, equalTo(styles));
    }

    @Test
    public void getNextUpdateTime_returnsEpochWhenNotSet() {
        Date nextUpdateTime = fAppPreferences.getNextUpdateTime();
        assertThat(nextUpdateTime, notNullValue());
        assertThat(nextUpdateTime.getTime(), equalTo(0L));
    }

    @Test
    public void setNextUpdateTime_storesTime() {
        Date now = new Date();
        fAppPreferences.setNextUpdateTime(now);

        Date retrieved = fAppPreferences.getNextUpdateTime();
        assertThat(retrieved.getTime(), equalTo(now.getTime()));
    }

    @Test
    public void setNextUpdateTime_persistsAcrossInstances() {
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        fAppPreferences.setNextUpdateTime(futureDate);

        AppPreferences newInstance = new AppPreferences(fContext);
        Date retrieved = newInstance.getNextUpdateTime();
        assertThat(retrieved.getTime(), equalTo(futureDate.getTime()));
    }

    @Test
    public void setNextUpdateTime_handlesEpochTime() {
        Date epoch = new Date(0);
        fAppPreferences.setNextUpdateTime(epoch);

        Date retrieved = fAppPreferences.getNextUpdateTime();
        assertThat(retrieved.getTime(), equalTo(0L));
    }

    @Test
    public void getHideUnavailableBeers_returnsFalseByDefault() {
        boolean hide = fAppPreferences.getHideUnavailableBeers();
        assertThat(hide, is(false));
    }

    @Test
    public void setHideUnavailableBeers_storesValue() {
        fAppPreferences.setHideUnavailableBeers(true);
        boolean retrieved = fAppPreferences.getHideUnavailableBeers();
        assertThat(retrieved, is(true));
    }

    @Test
    public void setHideUnavailableBeers_persistsAcrossInstances() {
        fAppPreferences.setHideUnavailableBeers(true);

        AppPreferences newInstance = new AppPreferences(fContext);
        boolean retrieved = newInstance.getHideUnavailableBeers();
        assertThat(retrieved, is(true));
    }

    @Test
    public void setHideUnavailableBeers_canToggle() {
        fAppPreferences.setHideUnavailableBeers(true);
        assertThat(fAppPreferences.getHideUnavailableBeers(), is(true));

        fAppPreferences.setHideUnavailableBeers(false);
        assertThat(fAppPreferences.getHideUnavailableBeers(), is(false));
    }

    @Test
    public void getLastUpdateMD5_returnsEmptyStringWhenNotSet() {
        String md5 = fAppPreferences.getLastUpdateMD5();
        assertThat(md5, equalTo(""));
    }

    @Test
    public void setLastUpdateMD5_storesMD5() {
        String testMD5 = "5d41402abc4b2a76b9719d911017c592";
        fAppPreferences.setLastUpdateMD5(testMD5);

        String retrieved = fAppPreferences.getLastUpdateMD5();
        assertThat(retrieved, equalTo(testMD5));
    }

    @Test
    public void setLastUpdateMD5_persistsAcrossInstances() {
        String testMD5 = "098f6bcd4621d373cade4e832627b4f6";
        fAppPreferences.setLastUpdateMD5(testMD5);

        AppPreferences newInstance = new AppPreferences(fContext);
        String retrieved = newInstance.getLastUpdateMD5();
        assertThat(retrieved, equalTo(testMD5));
    }

    @Test
    public void setLastUpdateMD5_handlesEmptyString() {
        fAppPreferences.setLastUpdateMD5("test");
        fAppPreferences.setLastUpdateMD5("");

        String retrieved = fAppPreferences.getLastUpdateMD5();
        assertThat(retrieved, equalTo(""));
    }

    @Test
    public void getStatusToShow_returnsAllWhenHideUnavailableIsFalse() {
        fAppPreferences.setHideUnavailableBeers(false);
        StatusToShow status = fAppPreferences.getStatusToShow();
        assertThat(status, equalTo(StatusToShow.ALL));
    }

    @Test
    public void getStatusToShow_returnsAvailableOnlyWhenHideUnavailableIsTrue() {
        fAppPreferences.setHideUnavailableBeers(true);
        StatusToShow status = fAppPreferences.getStatusToShow();
        assertThat(status, equalTo(StatusToShow.AVAILABLE_ONLY));
    }

    @Test
    public void getStatusToShow_reflectsToggleChanges() {
        fAppPreferences.setHideUnavailableBeers(false);
        assertThat(fAppPreferences.getStatusToShow(), equalTo(StatusToShow.ALL));

        fAppPreferences.setHideUnavailableBeers(true);
        assertThat(fAppPreferences.getStatusToShow(), equalTo(StatusToShow.AVAILABLE_ONLY));

        fAppPreferences.setHideUnavailableBeers(false);
        assertThat(fAppPreferences.getStatusToShow(), equalTo(StatusToShow.ALL));
    }

    @Test
    public void getBeerListConfig_returnsConfigWithAllPreferences() {
        SortOrder expectedSort = SortOrder.BEER_ABV_DESC;
        String expectedFilter = "Test Filter";
        Set<String> expectedStyles = new HashSet<>(Arrays.asList("IPA", "Stout"));
        boolean expectedHide = true;

        fAppPreferences.setSortOrder(expectedSort);
        fAppPreferences.setFilterText(expectedFilter);
        fAppPreferences.setStylesToHide(expectedStyles);
        fAppPreferences.setHideUnavailableBeers(expectedHide);

        BeerList.Config config = fAppPreferences.getBeerListConfig();

        assertThat(config.getSortOrder(), equalTo(expectedSort));
        assertThat(config.getFilterText(), equalTo(expectedFilter));
        assertThat(config.getStylesToHide(), equalTo(expectedStyles));
        assertThat(config.getStatusToShow(), equalTo(StatusToShow.AVAILABLE_ONLY));
    }

    @Test
    public void getBeerListConfig_returnsDefaultValuesWhenNothingSet() {
        BeerList.Config config = fAppPreferences.getBeerListConfig();

        assertThat(config.getSortOrder(), equalTo(SortOrder.BREWERY_NAME_ASC));
        assertThat(config.getFilterText(), equalTo(""));
        assertThat(config.getStylesToHide().isEmpty(), is(true));
        assertThat(config.getStatusToShow(), equalTo(StatusToShow.ALL));
    }

    @Test
    public void getBeerListConfig_reflectsPreferenceChanges() {
        BeerList.Config config1 = fAppPreferences.getBeerListConfig();
        assertThat(config1.getSortOrder(), equalTo(SortOrder.BREWERY_NAME_ASC));

        fAppPreferences.setSortOrder(SortOrder.BEER_NAME_DESC);
        BeerList.Config config2 = fAppPreferences.getBeerListConfig();
        assertThat(config2.getSortOrder(), equalTo(SortOrder.BEER_NAME_DESC));
    }

    @Test
    public void multiplePreferences_workIndependently() {
        // Set multiple preferences
        fAppPreferences.setSortOrder(SortOrder.BEER_RATING_DESC);
        fAppPreferences.setFilterText("Test");
        fAppPreferences.setHideUnavailableBeers(true);
        Set<String> styles = new HashSet<>(Arrays.asList("Lager"));
        fAppPreferences.setStylesToHide(styles);
        Date testDate = new Date(123456789L);
        fAppPreferences.setNextUpdateTime(testDate);
        fAppPreferences.setLastUpdateMD5("test-md5");

        // Verify all are stored correctly
        assertThat(fAppPreferences.getSortOrder(), equalTo(SortOrder.BEER_RATING_DESC));
        assertThat(fAppPreferences.getFilterText(), equalTo("Test"));
        assertThat(fAppPreferences.getHideUnavailableBeers(), is(true));
        assertThat(fAppPreferences.getStylesToHide(), equalTo(styles));
        assertThat(fAppPreferences.getNextUpdateTime().getTime(), equalTo(123456789L));
        assertThat(fAppPreferences.getLastUpdateMD5(), equalTo("test-md5"));
    }

    @Test
    public void multipleInstances_sharePreferences() {
        AppPreferences instance1 = new AppPreferences(fContext);
        AppPreferences instance2 = new AppPreferences(fContext);

        instance1.setSortOrder(SortOrder.BEER_ABV_ASC);
        instance1.setFilterText("Shared");

        assertThat(instance2.getSortOrder(), equalTo(SortOrder.BEER_ABV_ASC));
        assertThat(instance2.getFilterText(), equalTo("Shared"));
    }
}
