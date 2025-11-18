# UI Architecture Documentation

**Last Updated:** 2025-11-18
**App Version:** 2025.0.0.1 (versionCode 27)
**Purpose:** Comprehensive documentation of BeerFestApp UI structure for testing and modernization

---

## Table of Contents

- [Overview](#overview)
- [Main Activity Structure](#main-activity-structure)
- [Beer List View](#beer-list-view)
- [Beer Details View](#beer-details-view)
- [Dialogs](#dialogs)
- [Menu System](#menu-system)
- [Search Functionality](#search-functionality)
- [Sorting and Filtering](#sorting-and-filtering)
- [Star Rating System](#star-rating-system)
- [Favorites/Wishlist System](#favoriteswishlist-system)
- [Data Flow Patterns](#data-flow-patterns)
- [Modernization Opportunities](#modernization-opportunities)

---

## Overview

BeerFestApp uses a classic Android architecture (pre-2020):
- **Pattern:** Traditional Activity + Fragment with manual listeners
- **UI Framework:** XML layouts with AppCompat
- **Navigation:** ViewPager with TabLayout
- **Dialogs:** Legacy DialogFragment (android.app, deprecated)
- **Data Sync:** LocalBroadcastManager (deprecated)
- **Persistence:** SharedPreferences for settings, OrmLite for database

---

## Main Activity Structure

### CamBeerFestApplication

**File:** `app/src/main/java/ralcock/cbf/CamBeerFestApplication.java`
**Layout:** `app/src/main/res/layout/beer_listview_activity.xml`
**Extends:** `AppCompatActivity`

### Components

```
┌─ Toolbar (id: my_toolbar) ─────────────────────────────┐
│  [🍺 Logo]              [🔍 Search] [Sort] [Filter] [⋮] │
├────────────────────────────────────────────────────────┤
│  ┌─ TabLayout (id: sliding_tabs) ───────────────────┐  │
│  │   [All Beers]  [Wishlist]  [Tried]  [...]        │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌─ ViewPager (id: viewpager) ───────────────────────┐ │
│  │                                                     │ │
│  │   ┌─ BeerListFragment ─────────────────────────┐  │ │
│  │   │                                             │  │ │
│  │   │  ┌─ Beer List Item ──────────────────────┐ │  │ │
│  │   │  │ [Beer Name]              [★★★☆☆] [🔖] │ │  │ │
│  │   │  │ [Brewery]                              │ │  │ │
│  │   │  │ [Style] [Dispense] [Status]            │ │  │ │
│  │   │  └────────────────────────────────────────┘ │  │ │
│  │   │  ┌─ Beer List Item ──────────────────────┐ │  │ │
│  │   │  │ ...                                    │ │  │ │
│  │   │  └────────────────────────────────────────┘ │  │ │
│  │   │                                             │  │ │
│  │   └─────────────────────────────────────────────┘  │ │
│  │                                                     │ │
│  └─────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

### Key XML Elements

| Element ID | Type | Purpose |
|------------|------|---------|
| `my_toolbar` | Toolbar | App bar with menu and search |
| `viewpager` | ViewPager | Swipeable tab content |
| `sliding_tabs` | TabLayout | Tab navigation |

### Lifecycle

```java
onCreate() {
    // 1. Set content view
    // 2. Configure toolbar
    // 3. Setup ViewPager with BeerListFragmentPagerAdapter
    // 4. Connect TabLayout to ViewPager
    // 5. Restore saved tab position
    // 6. Register LocalBroadcastReceiver for update service
}

onResume() {
    // 1. Register broadcast receiver
    // 2. Start UpdateService (downloads beer list)
}

onPause() {
    // 1. Unregister broadcast receiver
}

onDestroy() {
    // 1. Release database helper
}
```

---

## Beer List View

### BeerListFragment

**File:** `app/src/main/java/ralcock/cbf/view/BeerListFragment.java`
**Layout:** `app/src/main/res/layout/beer_listview_fragment.xml`
**Item Layout:** `app/src/main/res/layout/beer_listitem.xml`

### List Item Structure

```xml
<!-- beer_listitem.xml -->
<RelativeLayout>
    <!-- Left side: Beer info -->
    <TextView android:id="@+id/beerName" />        <!-- Bold, 18sp -->
    <TextView android:id="@+id/breweryName" />     <!-- Bold, 14sp -->
    <TextView android:id="@+id/beerStyle" />       <!-- 12sp -->
    <TextView android:id="@+id/beerDispense" />    <!-- 12sp, uppercase -->
    <TextView android:id="@+id/beerStatus" />      <!-- 12sp -->

    <!-- Right side: Interactive elements -->
    <ImageView android:id="@+id/bookmark_image" /> <!-- Top-right -->
    <RatingBar android:id="@+id/beerRatingBar" />  <!-- Bottom-right, read-only -->
</RelativeLayout>
```

### Data Displayed Per Beer

| Field | Source | Display |
|-------|--------|---------|
| **Beer Name** | `Beer.getName()` | Bold, black, 18sp |
| **Brewery** | `Beer.getBrewery().getName()` | Bold, 14sp |
| **Style** | `Beer.getStyle()` | Gray, 12sp (e.g., "IPA", "Stout") |
| **Dispense** | `Beer.getDispenseMethod()` | Uppercase, 12sp (e.g., "CASK", "KEG") |
| **Status** | `Beer.getStatus()` | 12sp (e.g., "Available", "Sold Out") |
| **Rating** | `Beer.getRating()` | RatingBar, 0-5 stars, read-only |
| **Bookmark** | `Beer.isIsOnWishList()` | Hollow or filled bookmark icon |

### Interactions

- **Click beer item** → Opens `BeerDetailsActivity`
- **Bookmark icon** → Visual indicator only (clicking happens in details view)
- **Rating bar** → Read-only display (editing happens in details view)

---

## Beer Details View

### BeerDetailsActivity

**File:** `app/src/main/java/ralcock/cbf/view/BeerDetailsActivity.java`
**Layout:** `app/src/main/res/layout/beer_details_activity.xml`
**Fragment:** `BeerDetailsFragment` (contains actual content)

### BeerDetailsFragment

**File:** `app/src/main/java/ralcock/cbf/view/BeerDetailsFragment.java`
**Layout:** `app/src/main/res/layout/beer_details_fragment.xml`

### Layout Structure

```
┌─ Toolbar ──────────────────────────────────────────┐
│  [← Back]  [Beer Name]                  [Share ↗]  │
├────────────────────────────────────────────────────┤
│                                                     │
│  [Beer Name and ABV]                         [🔖]  │
│                                                     │
│  Style: [Beer Style]                                │
│  Dispense: [Cask/Keg/Bottle]                       │
│  Status: [Available/Sold Out]                      │
│                                                     │
│  [Beer Description paragraph...]                   │
│                                                     │
│  Brewery: [Brewery Name]                           │
│  [Brewery Description paragraph...]                │
│                                                     │
│  Your Rating:  [★★★☆☆]                             │
│                                                     │
│  [Click here to search for this beer online]       │
│                                                     │
└────────────────────────────────────────────────────┘
```

### Key UI Elements

| Element ID | Type | Purpose | Interaction |
|------------|------|---------|-------------|
| `detailsViewBeerNameAndAbv` | TextView | "Beer Name (ABV%)" | Read-only |
| `detailsViewBeerStyle` | TextView | Beer style | Read-only |
| `detailsViewBeerDispense` | TextView | How beer is served | Read-only |
| `detailsViewBeerStatus` | TextView | Availability status | Read-only |
| `detailsViewBeerDescription` | TextView | Beer description | Read-only |
| `detailsViewBreweryName` | TextView | Brewery name | Read-only |
| `detailsViewBreweryDescription` | TextView | Brewery description | Read-only |
| `detailsViewBeerRatingBar` | RatingBar | Star rating (0-5) | **Interactive** - Click/drag to rate |
| `bookmark_image` | ImageView | Wishlist toggle | **Interactive** - Click to bookmark |
| `clickToSearchOnline` | TextView | Search link | **Interactive** - Opens browser search |

### Interactions

#### Rating a Beer

```
User drags rating bar to 4 stars
    ↓
OnRatingBarChangeListener.onRatingChanged(ratingBar, 4.0, fromUser=true)
    ↓
if (fromUser) → rateBeer(beer, new StarRating(4.0))
    ↓
beer.setNumberOfStars(StarRating(4))
    ↓
getHelper().getBeers().updateBeer(beer)  // Persist to database
    ↓
displayBeer(beer)  // Refresh UI
```

#### Bookmarking a Beer

```
User clicks bookmark icon
    ↓
OnClickListener.onClick()
    ↓
toggleBookmark(beer)
    ↓
beer.setIsOnWishList(!beer.isIsOnWishList())
    ↓
getHelper().getBeers().updateBeer(beer)  // Persist to database
    ↓
displayBeer(beer)  // Refresh UI (icon changes hollow ↔ filled)
```

#### Sharing a Beer

- **Menu Item:** Share icon in toolbar
- **Provider:** ShareActionProvider
- **Content:** Beer name, brewery, style, rating, festival hashtag
- **Intent:** `Intent.ACTION_SEND` with `EXTRA_TEXT`

#### Searching Online

- **UI:** Clickable link "Click here to search for this beer online"
- **Action:** Opens default browser with Google search
- **Query:** Beer name + brewery name

---

## Dialogs

### 1. SortByDialogFragment

**File:** `app/src/main/java/ralcock/cbf/view/SortByDialogFragment.java`
**Layout:** `app/src/main/res/layout/sortby_dialog_fragment.xml`
**Deprecated:** Uses `android.app.DialogFragment` (should migrate to androidx)

#### Purpose
Allows user to select sort order for beer list.

#### Sort Options (SortOrder enum)
- `BY_NAME` - Alphabetical by beer name
- `BY_BREWERY` - Alphabetical by brewery name
- `BY_STYLE` - Grouped by beer style
- `BY_ABV` - By alcohol content
- `BY_RATING` - By star rating (highest first)
- Other options may exist - see `SortOrder.java`

#### Flow
```
User clicks sort icon (toolbar)
    ↓
showSortByDialog()
    ↓
SortByDialogFragment.newInstance(currentSortOrder)
    ↓
Dialog shows radio buttons for each SortOrder
    ↓
User selects option and clicks OK
    ↓
doDismissSortDialog(selectedSortOrder)
    ↓
sortBy(selectedSortOrder)
    ↓
AppPreferences.setSortOrder(selectedSortOrder)  // Persist
    ↓
fireSortByChanged(selectedSortOrder)  // Notify listeners
    ↓
ListChangedListener.sortOrderChanged()  // Update query
    ↓
Beer list reorders
```

### 2. FilterByStyleDialogFragment

**File:** `app/src/main/java/ralcock/cbf/view/FilterByStyleDialogFragment.java`
**Deprecated:** Uses `android.app.DialogFragment` (should migrate to androidx)

#### Purpose
Allows user to hide specific beer styles from the list.

#### Beer Styles
- **Source:** Queried from database via `getBeerDao().getAvailableStyles()`
- **Examples:** IPA, Stout, Porter, Lager, Bitter, Pale Ale, etc.
- **Dynamic:** Styles vary based on festival data

#### UI
- Checkbox for each available style
- Checked = hidden from list
- Unchecked = visible in list

#### Flow
```
User clicks filter icon (toolbar)
    ↓
showFilterByStyleDialog()
    ↓
getBeerDao().getAvailableStyles()  // Query distinct styles from DB
    ↓
AppPreferences.getStylesToHide()  // Get current hidden styles
    ↓
FilterByStyleDialogFragment.newInstance(stylesToHide, allStyles)
    ↓
Dialog shows checkboxes for each style
    ↓
User checks/unchecks styles and clicks OK
    ↓
doDismissFilterByStyleDialog(selectedStylesToHide)
    ↓
filterByBeerStyle(selectedStylesToHide)
    ↓
AppPreferences.setStylesToHide(selectedStylesToHide)  // Persist
    ↓
fireStylesToHideChanged(selectedStylesToHide)  // Notify listeners
    ↓
ListChangedListener.stylesToHideChanged()  // Update query WHERE clause
    ↓
Beer list filters out hidden styles
```

### 3. AboutDialogFragment

**File:** `app/src/main/java/ralcock/cbf/view/AboutDialogFragment.java`
**Layout:** `app/src/main/res/layout/about_dialog.xml`
**Deprecated:** Uses `android.app.DialogFragment` (should migrate to androidx)

#### Purpose
Displays app information: name, version, credits, privacy policy link.

#### Content
- App name: "Cambridge Beer Festival"
- Version: From `PackageInfo.versionName` (e.g., "2025.0.0.1")
- Developer credits
- Privacy policy link (if applicable)

---

## Menu System

### Toolbar Menu (list_options_menu.xml)

**File:** `app/src/main/res/menu/list_options_menu.xml`

#### Visible Menu Items (showAsAction="ifRoom")

| Icon | ID | Title | Action | Implementation |
|------|-----|-------|--------|----------------|
| 🔍 | `search` | Search | Expands SearchView widget | `onCreateOptionsMenu()` |
| ⇅ | `sort` | Sort | Opens SortByDialogFragment | `showSortByDialog()` |
| 🔧 | `showOnlyStyle` | Filter by Style | Opens FilterByStyleDialogFragment | `showFilterByStyleDialog()` |

#### Hidden Menu Items (showAsAction="never" or visible="false")

| ID | Title | Action | Status |
|----|-------|--------|--------|
| `hideUnavailable` | Show only available | Toggle sold-out beers | Currently hidden (visible="false") |
| `visitFestivalWebsite` | Visit festival website | Opens browser | Currently hidden |
| `export` | Export | Export beer list to CSV | Currently hidden |
| `refreshDatabase` | Refresh | Re-download beer list | Currently hidden |
| `reloadDatabase` | Reload | Clean download (clears cache) | Currently hidden |
| `aboutApplication` | About | Shows AboutDialogFragment | Visible |

### Details Menu (details_options_menu.xml)

**File:** `app/src/main/res/menu/details_options_menu.xml`

| Icon | ID | Title | Action |
|------|-----|-------|--------|
| ↗ | `shareBeer` | Share | Opens share dialog |

---

## Search Functionality

### SearchView Widget

**Location:** Toolbar menu (id: `search`)
**Type:** `androidx.appcompat.widget.SearchView`
**Behavior:** Collapses/expands in action bar

### UI Flow

```
User clicks search icon
    ↓
SearchView expands
    ↓
Shows hint: "filter beers by name, brewery or style"
    ↓
User types "IPA"
    ↓
OnQueryTextListener.onQueryTextChange("IPA")  // Called on every keystroke
    ↓
filterBy("IPA")
    ↓
getSupportActionBar().setTitle("IPA")  // Show search text in toolbar
    ↓
AppPreferences.setFilterText("IPA")  // Persist
    ↓
fireFilterTextChanged("IPA")  // Notify listeners
    ↓
ListChangedListener.filterTextChanged("IPA")
    ↓
Beer list query updated with WHERE clause:
    WHERE name LIKE '%IPA%'
       OR brewery LIKE '%IPA%'
       OR style LIKE '%IPA%'
    ↓
Beer list shows only matching beers
```

### Search Scope

Search matches across multiple fields:
- **Beer Name** - e.g., "IPA" matches "Punk IPA"
- **Brewery Name** - e.g., "Brew" matches "BrewDog IPA"
- **Beer Style** - e.g., "Stout" matches all stouts

### Search Characteristics

- **Case-insensitive** - "ipa" = "IPA" = "Ipa"
- **Partial match** - "Brew" matches "Brewery", "BrewDog", "Homebrewer"
- **Real-time** - Updates on every keystroke
- **Persistent** - Survives activity recreation
- **Clearable** - X button or backspace clears filter

---

## Sorting and Filtering

### Sort Order Enum

**File:** `app/src/main/java/ralcock/cbf/model/SortOrder.java`

Available sort options (verify in source):
```java
public enum SortOrder {
    BY_NAME,        // Beer name A-Z
    BY_BREWERY,     // Brewery name A-Z
    BY_STYLE,       // Beer style (grouped)
    BY_ABV,         // Alcohol content
    BY_RATING,      // Star rating (high to low)
    // ... possibly others
}
```

### Filter by Style

**Data Type:** `Set<String>` of style names to hide
**Storage:** SharedPreferences via `AppPreferences`
**Default:** Empty set (all styles visible)

**Example:**
```java
Set<String> stylesToHide = new HashSet<>();
stylesToHide.add("IPA");
stylesToHide.add("Lager");
// Beer list will hide all IPAs and Lagers
```

### Combining Filters

Filters combine with AND logic:

```
Search: "Pale"
+ Sort: BY_BREWERY
+ Hide Styles: ["Lager", "Stout"]
= Shows only beers matching "Pale" (in name/brewery/style),
  excluding Lagers and Stouts,
  sorted by brewery name
```

---

## Star Rating System

### Data Model

**Field:** `Beer.fNumberOfStars` (int, 0-5)
**Wrapper:** `StarRating` class (validation + business logic)
**Storage:** SQLite via OrmLite

### UI Components

| Location | Element | Type | Editable | Purpose |
|----------|---------|------|----------|---------|
| List Item | `beerRatingBar` | RatingBar | ❌ No (isIndicator=true) | Display rating |
| Details View | `detailsViewBeerRatingBar` | RatingBar | ✅ Yes | Set/change rating |

### Rating Scale

- **0 stars** = Unrated (default)
- **1 star** = Poor
- **2 stars** = Below average
- **3 stars** = Average
- **4 stars** = Good
- **5 stars** = Excellent

### Implementation

```java
// BeerDetailsFragment.java

// Display current rating
fBeerDetailsView.BeerRatingBar.setRating(beer.getRating());

// Listen for rating changes
fBeerDetailsView.BeerRatingBar.setOnRatingBarChangeListener(
    new RatingBar.OnRatingBarChangeListener() {
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            if (fromUser) {
                rateBeer(beer, new StarRating(rating));
            }
        }
    }
);

// Save rating to database
private void rateBeer(Beer beer, StarRating rating) {
    beer.setNumberOfStars(rating);
    getHelper().getBeers().updateBeer(beer);
    displayBeer(beer);  // Refresh UI
}
```

### Database Operation

```java
// BeersImpl.java
public void updateBeer(Beer beer) {
    try {
        fDao.update(beer);  // OrmLite Dao.update()
        fireBeerChanged(beer);  // Notify listeners
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

---

## Favorites/Wishlist System

### Data Model

**Field:** `Beer.fIsOnWishList` (boolean)
**Default:** `false`
**Storage:** SQLite via OrmLite

### UI Indicators

| Icon | State | Drawable Resource |
|------|-------|-------------------|
| 🔖 (hollow) | Not on wishlist | `ic_bookmark_border_black_48dp` |
| 🔖 (filled) | On wishlist | `ic_bookmark_black_48dp` |

### Locations

- **List Item** (`beer_listitem.xml`): Bookmark icon top-right (visual indicator only)
- **Details View** (`beer_details_fragment.xml`): Clickable bookmark button

### Implementation

```java
// BeerDetailsFragment.java

// Display bookmark state
if (beer.isIsOnWishList()) {
    fBeerDetailsView.BookmarkImage.setImageResource(R.drawable.ic_bookmark_black_48dp);
} else {
    fBeerDetailsView.BookmarkImage.setImageResource(R.drawable.ic_bookmark_border_black_48dp);
}

// Toggle bookmark on click
fBeerDetailsView.BookmarkImage.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        toggleBookmark(beer);
    }
});

// Toggle and save
private void toggleBookmark(Beer beer) {
    beer.setIsOnWishList(!beer.isIsOnWishList());
    getHelper().getBeers().updateBeer(beer);
    displayBeer(beer);  // Refresh UI
}
```

### Use Cases

- Mark beers to try during festival
- Track which beers to revisit
- Create a personal tasting list
- Filter to show only wishlist beers (if wishlist tab exists)

---

## Data Flow Patterns

### Observer Pattern - ListChangedListener

**File:** `app/src/main/java/ralcock/cbf/view/ListChangedListener.java`

```java
public interface ListChangedListener {
    void sortOrderChanged(SortOrder sortOrder);
    void stylesToHideChanged(Set<String> stylesToHide);
    void filterTextChanged(String filterText);
    void beersChanged();  // Called when beer data updated
}
```

#### Registration

```java
// CamBeerFestApplication.java
private final List<ListChangedListener> fListChangedListeners
    = new CopyOnWriteArrayList<>();

public void addListChangedListener(ListChangedListener listener) {
    fListChangedListeners.add(listener);
}

public void removeListChangedListener(ListChangedListener listener) {
    fListChangedListeners.remove(listener);
}
```

#### Notification

```java
// When sort order changes
private void fireSortByChanged(SortOrder sortOrder) {
    for (ListChangedListener l : fListChangedListeners) {
        l.sortOrderChanged(sortOrder);
    }
}

// When styles to hide change
private void fireStylesToHideChanged(Set<String> stylesToHide) {
    for (ListChangedListener l : fListChangedListeners) {
        l.stylesToHideChanged(stylesToHide);
    }
}

// When filter text changes
private void fireFilterTextChanged(String filterText) {
    for (ListChangedListener l : fListChangedListeners) {
        l.filterTextChanged(filterText);
    }
}

// When beer data updated
private void fireBeerListChanged() {
    for (ListChangedListener l : fListChangedListeners) {
        l.beersChanged();
    }
}
```

#### Benefits

- **Decoupling:** Main activity doesn't need references to fragments
- **Multiple listeners:** Multiple tabs can listen simultaneously
- **Thread-safe:** CopyOnWriteArrayList allows concurrent iteration
- **Consistent updates:** All views update together

### Broadcast Pattern - UpdateService

**Uses:** `LocalBroadcastManager` (deprecated)
**Purpose:** Notify UI when beer list download completes

```java
// Service sends broadcast
Intent broadcast = new Intent(UPDATE_SERVICE_RESULT);
broadcast.putExtra(RESULT_EXTRA, result);
fLocalBroadcastManager.sendBroadcast(broadcast);

// Activity receives broadcast
fBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(UpdateService.UPDATE_SERVICE_RESULT)) {
            UpdateTask.Result result = intent.getSerializableExtra(RESULT_EXTRA);
            doReceivedUpdateServiceResult(result);
        }
    }
};
```

**Modernization Opportunity:** Replace with LiveData or StateFlow

---

## Modernization Opportunities

### Current Architecture Issues

| Component | Current | Issue | Recommended Replacement |
|-----------|---------|-------|------------------------|
| DialogFragment | `android.app.DialogFragment` | Deprecated in API 28 | `androidx.fragment.app.DialogFragment` |
| LocalBroadcastManager | `androidx.localbroadcastmanager.content` | Deprecated in AndroidX 1.1.0 | LiveData, StateFlow, or callbacks |
| ViewPager | `androidx.viewpager.widget.ViewPager` | Old API | `androidx.viewpager2.widget.ViewPager2` |
| Listener pattern | Manual `ListChangedListener` | Boilerplate, not lifecycle-aware | LiveData with ViewModel |
| Database | OrmLite | No compile-time verification | Room Database |
| Direct database queries | Manual SQL building | Error-prone | Room with Flow |

### Recommended Modern Stack

```
Current                          →  Modern Replacement
──────────────────────────────────────────────────────────
Activity + Fragment              →  Activity + Fragment (same)
Manual listeners                 →  ViewModel + LiveData/Flow
OrmLite database                 →  Room Database
SharedPreferences                →  DataStore (or keep SharedPreferences)
LocalBroadcastManager            →  LiveData/StateFlow
DialogFragment (android.app)     →  DialogFragment (androidx.fragment.app)
ViewPager                        →  ViewPager2
XML layouts                      →  XML layouts + Jetpack Compose (gradual)
AppCompat components             →  Material Design 3 components
No dark mode                     →  DayNight theme support
```

### Modernization Benefits

#### 1. ViewModel + LiveData

**Before:**
```java
// Manual listener pattern
public interface ListChangedListener {
    void sortOrderChanged(SortOrder sortOrder);
}

// Activity maintains listener list
private final List<ListChangedListener> fListChangedListeners;

// Manual notification
private void fireSortByChanged(SortOrder sortOrder) {
    for (ListChangedListener l : fListChangedListeners) {
        l.sortOrderChanged(sortOrder);
    }
}
```

**After:**
```kotlin
// ViewModel with LiveData
class BeerListViewModel : ViewModel() {
    private val _sortOrder = MutableLiveData<SortOrder>()
    val sortOrder: LiveData<SortOrder> = _sortOrder

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}

// Fragment observes
viewModel.sortOrder.observe(viewLifecycleOwner) { sortOrder ->
    // Update UI automatically
    refreshBeerList(sortOrder)
}
```

**Benefits:**
- Lifecycle-aware (no leaks)
- Less boilerplate
- Automatic UI updates
- Easier testing

#### 2. Room Database

**Before (OrmLite):**
```java
Dao<Beer, Integer> dao = getHelper().getBeers().getDao();
QueryBuilder<Beer, Integer> qb = dao.queryBuilder();
qb.where().eq("fStyle", "IPA");
List<Beer> beers = qb.query();
```

**After (Room):**
```kotlin
@Query("SELECT * FROM beers WHERE style = :style")
fun getBeersByStyle(style: String): Flow<List<Beer>>

// Usage
viewModel.getBeersByStyle("IPA").collect { beers ->
    // Update UI
}
```

**Benefits:**
- Compile-time SQL verification
- Type-safe queries
- Reactive with Flow
- Better performance
- Easier migrations

#### 3. Material Design 3

**Current:** Material Design 1 (circa 2015)
**Recommended:** Material Design 3 (Material You, 2021+)

**Benefits:**
- Dynamic color theming
- Improved accessibility
- Modern aesthetics
- Dark mode support
- Better touch targets

#### 4. ViewPager2

**Benefits:**
- RTL support
- Vertical scrolling support
- Better performance
- RecyclerView-based (consistent API)
- Fragment lifecycle fixes

### Migration Strategy

**Phase 1: Quick Wins (1-2 days)**
1. Migrate DialogFragments to androidx.fragment.app
2. Add Material Design 3 dependency
3. Update theme to MaterialComponents
4. Enable dark mode support (DayNight theme)

**Phase 2: Architecture (1-2 weeks)**
1. Add ViewModel + LiveData
2. Replace ListChangedListener with LiveData observers
3. Replace LocalBroadcastManager with LiveData
4. Migrate SharedPreferences to DataStore

**Phase 3: Database (2-3 weeks)**
1. Add Room Database
2. Create migration from OrmLite to Room
3. Replace OrmLite DAOs with Room DAOs
4. Add Flow for reactive queries

**Phase 4: UI Components (2-3 weeks)**
1. Migrate ViewPager to ViewPager2
2. Update list items to Material Design 3
3. Add elevation and shadows
4. Improve touch targets for accessibility

**Phase 5: Optional - Jetpack Compose (4-6 weeks)**
1. Start with simple screens (About dialog)
2. Gradually migrate fragments to Compose
3. Interop with existing XML layouts
4. Full Compose migration (long-term)

---

## Related Documentation

- [Testing Documentation](./test-migration.md) - Test framework migration
- [Feature Proposals](./features/) - Proposed improvements
- [Troubleshooting](./troubleshooting/) - Common UI issues
- [Annual Updates](./annual-updates/) - Version update process

---

## Changelog

**2025-11-18:**
- Initial comprehensive UI architecture documentation
- Documented all major UI components and flows
- Added modernization recommendations
- Created for test development and future refactoring

---

**Questions or improvements?** Open an issue or PR at https://github.com/richardthe3rd/BeerFestApp
