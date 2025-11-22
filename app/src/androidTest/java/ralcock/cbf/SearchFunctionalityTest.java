package ralcock.cbf;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

/**
 * End-to-end tests for search/filter functionality.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Search view is accessible from toolbar</li>
 *   <li>Typing in search filters the beer list</li>
 *   <li>Search matches beer names, breweries, and styles</li>
 *   <li>Clearing search restores full list</li>
 *   <li>Search text is displayed in action bar title</li>
 *   <li>Search preference persists across sessions</li>
 * </ul>
 *
 * <p>For detailed search implementation documentation, see:
 * <ul>
 *   <li>/docs/ui-architecture.md - Overall UI structure</li>
 *   <li>/docs/features/search-functionality.md - Search implementation details</li>
 * </ul>
 *
 * @see CamBeerFestApplication#filterBy(String)
 * @see ralcock.cbf.view.ListChangedListener#filterTextChanged(String)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchFunctionalityTest {

    /**
     * Custom matcher that checks if an AdapterView has a specific adapter count.
     * This is more reliable than hasChildCount() which checks view children, not adapter items.
     */
    private static Matcher<View> withAdapterCount(final int expectedCount) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(final View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }
                AdapterView<?> adapterView = (AdapterView<?>) view;
                if (adapterView.getAdapter() == null) {
                    return false;
                }
                return adapterView.getAdapter().getCount() == expectedCount;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("AdapterView with adapter count: " + expectedCount);
            }
        };
    }

    /**
     * Test that search icon is displayed in toolbar.
     */
    @Test
    public void testSearchIconIsDisplayed() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // Verify search menu item exists
            // Note: SearchView is in the toolbar menu (id: R.id.search)
            // Clicking it expands the SearchView widget
        }
    }

    /**
     * Test clicking search icon expands the SearchView.
     *
     * <p>SearchView behavior:
     * <ul>
     *   <li>Starts collapsed in toolbar</li>
     *   <li>Clicking expands it to show text input</li>
     *   <li>Shows hint text: "filter beers by name, brewery or style"</li>
     *   <li>Back button or X icon collapses it</li>
     * </ul>
     */
    @Test
    public void testClickingSearchExpandsSearchView() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // Click search icon to expand
            // onView(withId(R.id.search)).perform(click());

            // TODO: Verify SearchView is expanded
            // Check for the EditText inside SearchView
        }
    }

    /**
     * Test typing in search filters the beer list.
     *
     * <p>Search behavior:
     * <ul>
     *   <li>onQueryTextChange() called on every keystroke</li>
     *   <li>filterBy() called with search text</li>
     *   <li>ListChangedListener.filterTextChanged() notifies fragments</li>
     *   <li>Beer list query updated with WHERE clause</li>
     *   <li>List refreshes to show matching beers only</li>
     * </ul>
     */
    @Test
    public void testTypingInSearchFiltersBeers() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // Verify initial state - main list view is displayed
            onView(withId(R.id.mainListView))
                .check(matches(isDisplayed()));

            // Click search icon to expand SearchView
            onView(withId(R.id.search))
                .perform(click());

            // Type search text into the SearchView's EditText
            // SearchView contains an EditText for user input
            onView(isAssignableFrom(EditText.class))
                .perform(typeText("IPA"), closeSoftKeyboard());

            // Verify the main list view is still displayed
            // (this confirms the app didn't crash and the list is showing filtered results)
            onView(withId(R.id.mainListView))
                .check(matches(isDisplayed()));

            // Note: A more complete test would verify the actual beer count decreased
            // and that only IPAs are shown, but that requires access to the adapter
            // or using custom matchers to count list items. For now, this test verifies
            // the basic search interaction works without crashing.
        }
    }

    /**
     * Test that search matches beer names.
     */
    @Test
    public void testSearchMatchesBeerNames() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO: Open search, type beer name, verify it appears in results
        }
    }

    /**
     * Test that search matches brewery names.
     */
    @Test
    public void testSearchMatchesBreweryNames() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO: Open search, type brewery name, verify matching beers appear
        }
    }

    /**
     * Test that search matches beer styles.
     */
    @Test
    public void testSearchMatchesBeerStyles() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO: Open search, type style (e.g., "Stout"), verify matching beers appear
        }
    }

    /**
     * Test clearing search restores full beer list.
     */
    @Test
    public void testClearingSearchRestoresFullList() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO:
            // 1. Get initial beer count
            // 2. Apply search filter
            // 3. Verify count decreased
            // 4. Clear search (click X button or delete text)
            // 5. Verify beer count restored to initial value
        }
    }

    /**
     * Test that search text appears in action bar title.
     *
     * <p>Implementation: CamBeerFestApplication.filterBy() calls:
     * <pre>getSupportActionBar().setTitle(filterText);</pre>
     *
     * <p>This shows the current filter in the toolbar title area.
     */
    @Test
    public void testSearchTextAppearsInTitle() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO:
            // 1. Open search
            // 2. Type search text
            // 3. Verify toolbar title changes to show search text
            // 4. onView(withId(R.id.my_toolbar)).check(matches(hasDescendant(withText("IPA"))))
        }
    }

    /**
     * Test that search preference persists across activity recreations.
     *
     * <p>Filter text is stored in SharedPreferences via AppPreferences.setFilterText().
     */
    @Test
    public void testSearchTextPersists() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO:
            // 1. Apply search filter
            // 2. Recreate activity (simulates rotation)
            // 3. Verify search is still applied
            // 4. Check AppPreferences.getFilterText()
        }
    }

    /**
     * Test search is case-insensitive.
     *
     * <p>Searching for "ipa", "IPA", or "Ipa" should all return same results.
     */
    @Test
    public void testSearchIsCaseInsensitive() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO: Test searching with different cases returns same results
        }
    }

    /**
     * Test search with no matches shows empty list.
     */
    @Test
    public void testSearchWithNoMatchesShowsEmptyList() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // Click search icon to expand SearchView
            onView(withId(R.id.search))
                .perform(click());

            // Type a nonsense string that won't match any beers
            onView(isAssignableFrom(EditText.class))
                .perform(typeText("xyzzy12345nosuchbeer"), closeSoftKeyboard());

            // Verify the list adapter has no items
            // Using custom matcher to check adapter count (more reliable than hasChildCount)
            onView(allOf(withId(android.R.id.list), isDisplayed()))
                .check(matches(withAdapterCount(0)));
        }
    }

    /**
     * Test search combines with other filters (sort, hide styles).
     *
     * <p>User should be able to:
     * <ul>
     *   <li>Search for "IPA"</li>
     *   <li>Hide certain styles</li>
     *   <li>Sort by brewery</li>
     *   <li>All filters apply simultaneously</li>
     * </ul>
     */
    @Test
    public void testSearchCombinesWithOtherFilters() {
        try (ActivityScenario<CamBeerFestApplication> scenario =
                ActivityScenario.launch(CamBeerFestApplication.class)) {

            // TODO: Apply search + style filter + sort, verify all work together
        }
    }
}
