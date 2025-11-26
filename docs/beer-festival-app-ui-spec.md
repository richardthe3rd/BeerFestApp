# Beer Festival App - UI Specification

**Version:** 1.6  
**Date:** November 23, 2025  
**Platform:** Android  
**Design System:** Material Design 3

---

## Table of Contents

1. [Overview](#overview)
2. [Design Principles](#design-principles)
3. [Navigation Architecture](#navigation-architecture)
4. [Screen Specifications](#screen-specifications)
5. [Component Library](#component-library)
6. [User Workflows](#user-workflows)
7. [End-to-End Test Scenarios](#end-to-end-test-scenarios)
8. [Material 3 Implementation Notes](#material-3-implementation-notes)

---

## Overview

### Purpose
A mobile application that allows users to browse, filter, and favorite drinks at beer festivals. Users can access current and past festivals, with a focus on quick, one-handed drink discovery.

### Target Users
Festival attendees who need to:
- Quickly find their next drink (primary use case)
- Track drinks they want to try via favorites
- Share drink discoveries with friends

### Technical Context
- **Platform:** Android (API 21+)
- **Design System:** Material Design 3
- **Expected Data Volume:** 100+ drinks per festival
- **Categories:** 10 drink categories (Beer is largest, ~60-70% of drinks)
- **Usage Context:** One-handed operation while standing/walking at festival

---

## Design Principles

### 1. Thumb-First Design
- All frequent actions within bottom 2/3 of screen (thumb zone)
- Minimum touch targets: 48dp (Material 3 standard)
- Recommended touch targets: 56-72dp for primary actions
- Bottom navigation for frequent tasks

### 2. Outdoor Readability
- High contrast ratios (WCAG AA minimum: 4.5:1 for normal text)
- Large, legible typography (minimum 16sp body, 20sp+ for drink names)
- Dark mode as default (battery saving, outdoor readability)
- Bold iconography with simple strokes

### 3. Speed & Efficiency
- Maximum 2 taps to filter by category
- Immediate visual feedback on all interactions
- Offline-first architecture (festival data downloaded)
- Skeleton screens instead of spinners

### 4. Clear Information Hierarchy
- Drink name is most prominent element
- Quick-scan card layout
- Persistent context (always show which festival is active)

---

## Navigation Architecture

### Information Hierarchy

```
Festival (Context - Top)
    ↓
├─ Drinks (Bottom Nav)
│   ├─ Filter by Category
│   ├─ Sort Options
│   └─ Drink Details
│
└─ Favorites (Bottom Nav)
    └─ Favorited Drinks List
```

### Navigation Pattern

**Top Navigation:** Festival context (less frequent, two-handed acceptable)  
**Bottom Navigation:** Primary app functions (frequent, one-handed optimized)

```
┌─────────────────────────────────┐
│         TOP AREA                 │
│   Festival Selector (rare)       │
├─────────────────────────────────┤
│                                  │
│      MAIN CONTENT AREA           │
│       (scrollable)               │
│                                  │
│                                  │
└─────────────────────────────────┘
┌─────────────────────────────────┐
│   BOTTOM NAVIGATION              │
│   Drinks | Favorites             │
└─────────────────────────────────┘
```

---

## Screen Specifications

### 1. Drinks Screen (Home/Default)

**Purpose:** Browse all drinks in current festival, filter by category, sort results

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│ ┃ TOP APP BAR (64dp)            ┃ │
│ ┃ Summer Beer Fest 2024    🔽   ┃ │ ← Festival selector
│ ┃ Nov 22-24                  🔍 ┃ │ ← Search icon
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│ ┃ FILTER BAR (56dp)             ┃ │
│ ┃ [ 🎛️ Filter ]    [ ↕️ Sort ]  ┃ │ ← Action buttons
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│ ┌───────────────────────────────┐ │
│ │ SUBTITLE (optional, 32dp)     │ │
│ │ 127 drinks                    │ │ ← Or "87 beers" when filtered
│ └───────────────────────────────┘ │
├───────────────────────────────────┤
│ ┌───────────────────────────────┐ │
│ │ DRINK CARD (120dp)            │ │
│ │ Hazy IPA              ♡       │ │ ← Heart icon (40dp tap)
│ │ Cloudwater Brew Co.           │ │
│ │ 6.5% • IPA • Draft            │ │
│ └───────────────────────────────┘ │
│                                   │
│ ┌───────────────────────────────┐ │
│ │ DRINK CARD                    │ │
│ │ Pilsner Urquell       ♥       │ │ ← Filled heart (favorited)
│ │ Pilsner Urquell Brewery       │ │
│ │ 4.4% • Pilsner • Draft        │ │
│ └───────────────────────────────┘ │
│                                   │
│ [Scrollable list continues...]    │
│                                   │
│                                   │
└───────────────────────────────────┘
┌───────────────────────────────────┐
│ BOTTOM NAV BAR (80dp)             │
│  [ Drinks ]      [ Favorites ]    │
│      🍺               ⭐           │
│   (active)                        │
└───────────────────────────────────┘
```

#### Component Specifications

**Top App Bar**
- Height: 64dp
- Background: `surfaceContainer` (Material 3)
- Elevation: 0dp (flat design)
- Layout:
  - Left: Festival name (Title Large - 22sp)
  - Sub-text: Festival dates (Body Medium - 14sp, medium emphasis)
  - Right: Dropdown icon (24dp) + Search icon (24dp)
  - Padding: 16dp horizontal, 8dp vertical

**Filter Bar**
- Height: 56dp
- Background: `surface` (Material 3)
- Layout: Two equal-width buttons with 8dp gap
- Padding: 16dp horizontal
- Buttons:
  - Style: `FilledTonalButton` (Material 3)
  - Height: 40dp
  - Icon + Label
  - Corner radius: 20dp (fully rounded)

**Drink Card**
- Height: 120dp (minimum)
- Background: `surfaceContainerLow` (Material 3)
- Corner radius: 12dp
- Elevation: 1dp (subtle shadow)
- Padding: 16dp
- Margin: 16dp horizontal, 8dp vertical
- Ripple effect on tap

**Card Content Layout:**
```
┌─────────────────────────────────────┐
│ Drink Name (Title Medium, 16sp)  ♡ │ ← 40dp touch target for heart
│ Brewery Name • Location (Body Med)  │ ← Brewery + location
│ ABV% • Style • Dispense (Body Small)│ ← Separated by bullets
│ 🟢 Plenty left / ⚠️ Low / ⭕ Out     │ ← Availability (optional)
└─────────────────────────────────────┘
```

**Brewery Location:**
- Shows producer location parsed from producer.notes (see "Brewery Location Parsing" section)
- Format: "Adnams • Southwold, Suffolk"
- Uses bullet separator between name and location
- Typography: Body Medium (14sp), Medium emphasis
- Truncates location if very long
- If location parsing fails, show brewery name only

**Dispense Field:**
- Shows how drink is served: Keg, Cask, Polypin, Bottle, Can, etc.
- Displayed in third position after ABV and Style
- Example: "5.2% • IPA • Cask"

**Availability Indicator** (Optional feature):
- Position: Bottom of card, left-aligned
- Typography: Label Small (11sp), Medium weight
- Colors:
  - 🟢 Green: "Plenty left" (Success color)
  - ⚠️ Amber: "Running low" (Warning color)
  - ⭕ Red: "Sold out" (Error color)
- Icon: 8dp circle or emoji
- Only show if festival has real-time inventory tracking

**Typography:**
- Drink Name: Title Medium (16sp), Bold, High emphasis (87% opacity)
- Brewery: Body Medium (14sp), Regular, Medium emphasis (60% opacity)
- Details: Body Small (12sp), Regular, Medium emphasis (60% opacity)

**Heart Icon:**
- Size: 24dp icon
- Touch target: 40dp minimum
- States:
  - Unfilled: `outline` style, Medium emphasis color
  - Filled: `filled` style, Primary color
- Haptic feedback on tap
- Animation: Scale + fade (150ms duration)

#### States

**Default State:**
- Shows all drinks from current festival
- Filter button: "Filter" (no active filter indicator)
- Sort button: "Sort" (default sort applied)
- Drink count subtitle visible

**Filtered State:**
- Filter button shows: "Filter: Beer (87) ✕"
- Small close icon (✕) in button for quick clear
- Drink count updates: "87 beers"
- Cards show only filtered category

**Empty State:**
- Illustration (optional)
- "No drinks found"
- "Try adjusting your filters"
- Button: "Clear Filters"

**Loading State:**
- Skeleton screens (3-4 card outlines)
- No spinner animation
- Fade-in when loaded

#### Interactions

**Tap Drink Card** → Navigate to Drink Detail Screen  
**Tap Heart Icon** → Toggle favorite (immediate feedback, haptic)  
**Swipe Right on Card** → Add to favorites (alternative gesture)  
**Swipe Left on Card** → Dismiss/hide (optional feature)  
**Pull to Refresh** → Reload drink list (if real-time updates)  
**Tap Filter Button** → Open Filter Bottom Sheet  
**Tap Sort Button** → Open Sort Bottom Sheet  
**Tap Festival Name** → Open Festival Selector Bottom Sheet  
**Tap Search Icon** → Open full-screen search

---

### 2. Favorites Screen

**Purpose:** Quick access to bookmarked drinks across current festival

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│ ┃ TOP APP BAR                   ┃ │
│ ┃ Summer Beer Fest 2024    🔽   ┃ │
│ ┃ 15 favorites              🔍  ┃ │ ← Count replaces dates
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
├───────────────────────────────────┤
│ ┌───────────────────────────────┐ │
│ │ DRINK CARD                    │ │
│ │ Pilsner Urquell       ♥       │ │ ← All cards have filled hearts
│ │ Pilsner Urquell Brewery       │ │
│ │ 4.4% • Pilsner • Draft        │ │
│ └───────────────────────────────┘ │
│                                   │
│ [Scrollable favorites list...]    │
│                                   │
└───────────────────────────────────┘
┌───────────────────────────────────┐
│ BOTTOM NAV BAR                    │
│  [ Drinks ]      [ Favorites ]    │
│      🍺               ⭐           │
│                    (active)       │
└───────────────────────────────────┘
```

#### Component Specifications

**Identical to Drinks Screen except:**
- No Filter/Sort bar (optional: could add sort only)
- Subtitle shows favorite count instead of total drinks
- All cards display filled hearts
- Tap heart → Unfavorite (removes from list with animation)

#### States

**Default State:**
- List of all favorited drinks in current festival
- Sorted by date added (most recent first)

**Empty State:**
- Icon: Empty star or heart illustration
- "No favorites yet"
- "Tap the ♡ on drinks you want to try"
- Button: "Browse Drinks" → Navigate to Drinks screen

**After Unfavoriting:**
- Card slides out with animation (200ms)
- Count updates in header
- If last favorite removed → Show empty state

---

### 3. Filter Bottom Sheet

**Purpose:** Select drink category and apply additional filters

#### Layout Structure

```
┌─────────────────────────────────────┐
│              ━━━━                   │ ← Drag handle
│                                     │
│ Filters                             │ ← Title (Title Large, 22sp)
│                                     │
│ CATEGORY                            │ ← Section label (Label Large)
│ ○ All (127)                         │
│ ● Beer (87)                ← Selected (filled radio)
│ ○ Cider (15)                        │
│ ○ Wine (12)                         │
│ ○ Mead (8)                          │
│ ○ Spirits (6)                       │
│ ○ Cocktails (5)                     │
│ ○ Sake (3)                          │
│ ○ Kombucha (2)                      │
│ ○ Low/No Alcohol (1)                │
│                                     │
│ ABV RANGE                           │ ← Section label
│ ●━━━━━○━━━━━━━━━━━●                 │ ← RangeSlider
│ 0%                        12%       │ ← Current values
│                                     │
│ QUICK FILTERS                       │
│ ☑ Show favorites only               │ ← Checkbox
│ ☐ Available now                     │
│                                     │
│ ┌──────────┐  ┌──────────────────┐ │
│ │  Clear   │  │  Apply (87)      │ │ ← Buttons
│ └──────────┘  └──────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

#### Component Specifications

**Bottom Sheet Container**
- Type: `ModalBottomSheet` (Material 3)
- Corner radius: 28dp (top corners only)
- Elevation: 3dp
- Background: `surfaceContainerLow`
- Padding: 24dp horizontal, 16dp vertical
- Drag handle: 4dp height, 32dp width, centered

**Section Labels**
- Typography: Label Large (14sp), Medium weight
- Color: Medium emphasis (60% opacity)
- Margin: 24dp top, 12dp bottom

**Radio Button List**
- Component: `RadioButton` (Material 3)
- Height: 56dp per item (touch target)
- Layout: Icon (24dp) + Label + Count
- Label: Body Large (16sp)
- Count: Body Medium (14sp), Medium emphasis, right-aligned

**Range Slider**
- Component: `RangeSlider` (Material 3)
- Height: 48dp (includes touch target)
- Track height: 4dp
- Thumb size: 20dp diameter
- Active track color: Primary
- Inactive track color: Surface variant
- Value labels below slider

**Checkboxes**
- Component: `Checkbox` (Material 3)
- Height: 48dp per item
- Layout: Icon (24dp) + Label
- Label: Body Large (16sp)

**Action Buttons**
- Container: Fixed at bottom, 16dp margin
- Layout: Row with 8dp gap
- Clear button:
  - Style: `OutlinedButton`
  - Width: Flexible (~40%)
  - Height: 48dp
- Apply button:
  - Style: `FilledButton`
  - Width: Flexible (~60%)
  - Height: 48dp
  - Shows count of filtered results

#### Behavior

**Opening:**
- Slides up from bottom (300ms ease-out animation)
- Dimmed scrim behind sheet (60% opacity black)
- Tap scrim or swipe down to dismiss without applying

**Category Selection:**
- Single selection (radio buttons)
- Tap to select, immediate visual feedback
- Count updates in "Apply" button
- Selecting "All" disables ABV and quick filters (optional)

**ABV Range:**
- Dual thumb slider
- Continuous interaction (updates count live)
- Default range: 0% to max ABV in dataset
- Step size: 0.5%

**Quick Filters:**
- Independent checkboxes
- Can combine with category filter
- "Show favorites only" + "Beer" = favorited beers only

**Clear Button:**
- Resets all filters to default
- Updates preview count
- Does NOT close sheet

**Apply Button:**
- Closes sheet
- Applies filters to Drinks screen
- Updates Filter button label
- Shows count in button: "Apply (87)"

**Dismissing Without Changes:**
- Swipe down gesture
- Tap scrim
- System back button
- No filters applied (maintains previous state)

---

### 4. Sort Bottom Sheet

**Purpose:** Select sorting method for drink list

#### Layout Structure

```
┌─────────────────────────────────────┐
│              ━━━━                   │ ← Drag handle
│                                     │
│ Sort By                             │ ← Title
│                                     │
│ ○ Name (A-Z)                        │
│ ● ABV (High to Low)        ← Selected
│ ○ ABV (Low to High)                 │
│ ○ Brewery (A-Z)                     │
│ ○ Style (A-Z)                       │
│ ○ Recently Added                    │
│                                     │
│            [ Done ]                 │ ← Single button
│                                     │
└─────────────────────────────────────┘
```

#### Component Specifications

**Bottom Sheet Container**
- Same as Filter sheet specifications
- Shorter height (fits content)

**Radio Button List**
- Component: `RadioButton` (Material 3)
- Height: 56dp per item
- Single selection
- No counts needed

**Sort Options:**
1. Name (A-Z) - Alphabetical by drink name
2. ABV (High to Low) - Strongest first
3. ABV (Low to High) - Lightest first
4. Brewery (A-Z) - Alphabetical by brewery
5. Style (A-Z) - Groups by style (IPA, Lager, etc.)
6. Recently Added - Newest additions first (if applicable)

**Done Button**
- Style: `FilledButton`
- Width: Full width minus padding
- Height: 48dp
- Margin: 16dp top

#### Behavior

**Selection:**
- Single tap selects and auto-closes sheet (no Done button needed)
- Alternative: Require Done button tap (safer, allows preview)

**Applying Sort:**
- Immediately re-sorts drink list
- Updates Sort button label: "Sort: ABV High→Low"
- Persists for session (until changed or app closed)

**Default Sort:**
- Name (A-Z) on first load
- Or last used sort (save preference)

---

### 5. Festival Selector Bottom Sheet

**Purpose:** Switch between current and past festivals

#### Layout Structure

```
┌─────────────────────────────────────┐
│              ━━━━                   │
│                                     │
│ Select Festival                     │ ← Title
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ ✓ Summer Beer Fest 2024      ℹ️ │ │ ← Selected + info button
│ │   Nov 22-24 • 127 drinks        │ │
│ │   15 favorites                  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │   Spring Beer Fest 2024      ℹ️ │ │ ← Info button
│ │   Apr 15-17 • 143 drinks        │ │
│ │   22 favorites                  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │   Oktoberfest 2023           ℹ️ │ │
│ │   Oct 7-9 • 98 drinks           │ │
│ │   8 favorites                   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Scrollable list if many festivals] │
│                                     │
└─────────────────────────────────────┘
```

#### Component Specifications

**Festival Card**
- Component: `Card` with `outlined` style (Material 3)
- Height: Auto (96dp typical)
- Corner radius: 12dp
- Padding: 16dp
- Margin: 8dp vertical
- Ripple effect on tap (main card area)
- Info button: IconButton (24dp), top-right corner
  - Icon: Info circle (ℹ️)
  - Touch target: 48dp
  - Separate from card tap area

**Selected Festival:**
- Border: 2dp, Primary color
- Checkmark icon: 24dp, top-right corner (left of info button)
- Background: `surfaceContainerHighest`

**Unselected Festival:**
- Border: 1dp, Outline color
- Background: `surfaceContainer`

**Card Content:**
- Festival name: Title Medium (16sp), Bold
- Date range: Body Medium (14sp), Medium emphasis
- Metadata line: Body Small (12sp), Low emphasis
  - Format: "127 drinks • 15 favorites"
  - Separator: " • " (bullet)

**Visual Indicator for Current Festival:**
- Optional: Green dot (🟢) or "LIVE" badge next to name
- Helps distinguish current vs past festivals

#### Behavior

**Opening:**
- Slides up from bottom
- Current festival appears first in list
- Past festivals in reverse chronological order

**Selection:**
- Single tap on card (main area) → Switches to that festival
- Immediate selection (no confirmation needed)
- Sheet auto-closes with animation (200ms)
- Entire app context switches to selected festival
- Navigation returns to Drinks screen
- Brief loading state while switching data

**Info Button:**
- Tap info button (ℹ️) → Opens Festival Overview screen
- Does NOT switch festivals
- Allows viewing details before switching
- Can be accessed for any festival (current or past)

**Visual Feedback:**
- Selected card highlights with border + checkmark
- Smooth transition animation on selection
- Optimistic UI (show selected immediately, load in background)

---

### 5a. Festival Overview Screen

**Purpose:** Display comprehensive festival information including dates, location, and external links

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ← [Back]             Summer Beer    │ ← Top app bar
│                      Fest 2024       │
├─────────────────────────────────────┤
│                                     │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│ ┃ HERO SECTION                    ┃ │
│ ┃                                 ┃ │
│ ┃ Summer Beer Fest 2024           ┃ │ ← Festival name (28sp)
│ ┃ Nov 22-24, 2025                 ┃ │ ← Dates (18sp)
│ ┃                                 ┃ │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│                                     │
│ OVERVIEW                            │ ← Section header
│ 127 drinks • 15 favorites           │
│ 45 breweries represented            │ ← Optional stats
│                                     │
│ LOCATION                            │
│ Victoria Park                       │ ← Venue name
│ London, E9 7BT                      │ ← Address
│                                     │
│ [ 📍 Open in Maps ]                 │ ← Maps button
│                                     │
│ FESTIVAL HOURS                      │ ← Optional section
│ Friday 5PM - 11PM                   │
│ Saturday 12PM - 11PM                │
│ Sunday 12PM - 6PM                   │
│                                     │
│ DESCRIPTION                         │
│ Join us for three days of craft    │
│ beer, cider, and more. Featuring   │
│ local breweries and international  │
│ favorites...                        │
│                                     │
│ [ 🔗 Visit Festival Website ]       │ ← Website button
│                                     │
│ ┌──────────────────────────────┐   │
│ │ [ Switch to this festival ]  │   │ ← Primary action (if not current)
│ └──────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

#### Component Specifications

**Top App Bar**
- Style: Standard with back button
- Title: Festival name (Title Large)
- Background: `surfaceContainer`
- Elevation: 0dp

**Hero Section**
- Background: Gradient or solid `primaryContainer`
- Padding: 24dp
- Min height: 160dp

**Festival Name**
- Typography: Headline Medium (28sp), Bold
- Color: High emphasis

**Dates**
- Typography: Title Medium (18sp), Regular
- Color: Medium emphasis
- Format: "Nov 22-24, 2025"

**Section Headers**
- Typography: Title Medium (16sp), Medium weight
- Color: High emphasis
- Margin: 24dp top, 12dp bottom

**Body Content**
- Typography: Body Large (16sp), Regular
- Color: Medium emphasis
- Line height: 24sp

**Location Section**
- Venue: Body Large (16sp), Bold
- Address: Body Medium (14sp), Medium emphasis
- Geocoded for maps integration

**Maps Button**
- Style: `OutlinedButton` (Material 3)
- Icon: Location pin (20dp)
- Full width
- Height: 48dp
- Opens device maps app with festival location

**Website Button**
- Style: `OutlinedButton` (Material 3)
- Icon: Link (20dp)
- Full width
- Height: 48dp
- Opens festival website in browser

**Switch Festival Button** (Only if viewing non-current festival)
- Style: `FilledButton` (Material 3)
- Full width
- Height: 48dp
- Margin: 16dp top
- Action: Switches to this festival, closes overview, returns to Drinks screen

#### Behavior

**Opening:**
- Slide in from right (Material motion)
- Can be accessed from Festival Selector (info button)
- Can also be accessed from top bar menu (optional)

**Maps Button:**
- Tap → Opens device default maps app
- Intent with geo coordinates or address
- Falls back to web maps if no app available
- Example: `geo:51.5333,-0.0333?q=Victoria+Park+London`

**Website Button:**
- Tap → Opens festival website in browser
- Uses Chrome Custom Tabs (in-app browser) if available
- Falls back to default browser
- URL stored in FestivalEntity

**Switch Festival Button:**
- Only visible if viewing a different festival than current
- Tap → Switches app context to this festival
- Closes overview screen
- Returns to Drinks screen with new festival loaded
- Shows brief confirmation (Snackbar): "Switched to [Festival Name]"

**Current Festival:**
- If viewing current festival, no switch button
- Optional: Show "Current Festival" badge in hero

**Back Navigation:**
- Returns to Festival Selector
- Or returns to previous screen if accessed from menu

**Scrolling:**
- All content scrollable
- Top bar can collapse/elevate on scroll (optional)

---

### 6. Drink Detail Screen

**Purpose:** Show comprehensive information about a single drink

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ← [Back]                   ⋮ [Menu] │ ← Top app bar
├─────────────────────────────────────┤
│                                     │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│ ┃ HERO SECTION                    ┃ │
│ ┃                                 ┃ │
│ ┃ Hazy IPA                        ┃ │ ← Title (32sp)
│ ┃ Cloudwater Brew Co.             ┃ │ ← Brewery (20sp)
│ ┃ Manchester, UK                  ┃ │ ← Location (16sp, med emphasis)
│ ┃                                 ┃ │
│ ┃ [ ♥ Favorite ]  [ Share ]       ┃ │ ← Action buttons
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ KEY INFO CHIPS (scrollable)     │ │
│ │ [ 6.5% ][ IPA ][ Cask ]         │ │
│ │ [ 🟢 Plenty left ]               │ │ ← Availability chip
│ └─────────────────────────────────┘ │
│                                     │
│ DESCRIPTION                         │ ← Section header
│ A hazy, juicy New England-style    │
│ IPA with tropical fruit notes...   │ ← Body text
│                                     │
│ ⚠️ Contains: Gluten, Barley         │ ← Allergen warning (red)
│                                     │
│ DETAILS                             │
│ Style: India Pale Ale               │
│ ABV: 6.5%                           │
│ Dispense: Cask                      │
│ Bar: Arctic                         │ ← NEW: Festival bar location
│                                     │
│ TASTING NOTES                       │
│ • Tropical fruits (mango, passion)  │
│ • Citrus (grapefruit)               │
│ • Smooth, creamy mouthfeel          │
│                                     │
│ YOUR RATING                         │ ← Personal rating
│ ☆☆☆☆☆ Not rated yet                 │ ← Tap stars to rate
│                                     │
│ 🔗 Search for this beer online      │ ← External link
│                                     │
│ SIMILAR DRINKS                      │ ← Section
│ [ Same Style ] [ Same Dispense ]   │ ← Filter chips
│ [ Similar ABV ]                     │
│                                     │
│ ┌────────────┐ ┌────────────┐      │
│ │ IPA Card   │ │ IPA Card   │      │ ← Horizontal scroll
│ │ 6.2% Cask  │ │ 6.8% Cask  │      │
│ └────────────┘ └────────────┘      │
│                                     │
└─────────────────────────────────────┘
```

#### Component Specifications

**Top App Bar**
- Style: Transparent or `surfaceContainer`
- Back button: Icon only (24dp)
- Menu button (⋮): Options like Share, Report issue
- Elevation: 0dp (scrolls under content)

**Hero Section**
- Background: Gradient or solid `primaryContainer`
- Padding: 24dp
- Min height: 180dp
- Content centered

**Drink Name**
- Typography: Headline Medium (32sp), Bold
- Color: High emphasis

**Brewery Name**
- Typography: Title Large (20sp), Regular
- Color: Medium emphasis
- Margin: 4dp top

**Brewery Location**
- Typography: Title Medium (16sp), Regular
- Color: Medium emphasis (slightly lower than brewery name)
- Margin: 2dp top
- Example: "Manchester, UK", "Ellon, Scotland"

**Action Buttons**
- Container: Row, 8dp gap, 16dp top margin
- Favorite button:
  - Style: `FilledTonalButton` if unfavorited
  - Style: `FilledButton` if favorited
  - Icon: Heart (filled/outline)
  - Label: "Favorite" / "Favorited"
- Share button:
  - Style: `OutlinedButton`
  - Icon: Share icon
  - Label: "Share"

**Info Chips**
- Component: `AssistChip` (Material 3)
- Height: 32dp
- Horizontal scroll if needed
- Gap: 8dp between chips
- Content: Icon (optional) + Label

**Availability Chip** (Optional feature):
- Component: `AssistChip` with status color
- Colors match availability:
  - Green tint: "Plenty left"
  - Amber/Orange tint: "Running low"
  - Red/Error tint: "Sold out"
- Icon: Circle (8dp) in matching color
- Only show if festival has real-time inventory

**Allergen Warning**
- Position: Directly below description, before DETAILS section
- Typography: Label Large (14sp), Medium weight
- Color: Error color (red) from Material theme
- Icon: ⚠️ Warning triangle (16dp) before text
- Format: "Contains: [allergen list]"
- Examples: "Contains: Gluten", "Contains: Gluten, Barley, Wheat"
- Background: Optional light red tint (`errorContainer`)
- Padding: 12dp all sides
- Corner radius: 8dp
- Always show if allergen data exists

**DETAILS Section**
- Section header: "DETAILS" (Title Medium, 16sp, Medium weight)
- Margin: 24dp top, 12dp bottom
- Layout: Key-value pairs in table format
- Fields (in order):
  1. **Style** - Drink style (e.g., "India Pale Ale", "Stout")
     - Show if available (nullable)
  2. **ABV** - Alcohol by volume (e.g., "6.5%")
     - Always shown, formatted with %
  3. **Dispense** - Serving method (e.g., "Cask", "Keg", "Bottle")
     - Always shown, from API dispense field
  4. **Bar** - Festival bar location (e.g., "Arctic", "Main Bar")
     - Show if available (nullable)
     - Very useful for large multi-bar festivals
- Typography: Body Large (16sp), Regular
- Color: Medium emphasis
- Key width: ~80dp
- Value: Remaining width
- Line height: 32dp (adequate spacing)
**Rating Section** (Personal/Local Only)
- Position: After TASTING NOTES, before external link
- Section header: "YOUR RATING" (same style as other headers)
- Display format: Interactive 5-star row
  - Stars: Unfilled (☆) or filled (★), 24dp each, tappable
  - Layout: 5 stars in horizontal row
  - Text: "Not rated yet" or "You rated X stars"
  - Typography: Body Medium (14sp)
- Interaction: Direct tap on stars (1-5)
  - No separate dialog needed
  - Immediate visual feedback
  - Haptic feedback on tap
- Storage: Local only (saved per user/device)
  - Uses SharedPreferences or Room DB
  - Key: `rating_[festivalId]_[drinkId]`
- States:
  - Unrated: ☆☆☆☆☆ "Not rated yet"
  - Rated 3 stars: ★★★☆☆ "You rated 3 stars"
- No aggregation, no sync, no counts

**External Search Link**
- Position: After RATING section
- Format: "🔗 Search for this beer online"
- Component: `TextButton` with link icon
- Typography: Label Large (14sp)
- Color: Primary color (clickable link appearance)
- Action: Opens default browser with search query
  - Query format: "[Drink Name] [Brewery]"
  - Uses Android Intent.ACTION_WEB_SEARCH
- Icon: Link/search icon (20dp) before text

**Section Headers**
- Typography: Title Medium (16sp), Medium weight
- Color: High emphasis
- Margin: 24dp top, 8dp bottom
- Optional: Divider line below (1dp, Outline color)

**Body Content**
- Typography: Body Large (16sp), Regular
- Color: Medium emphasis
- Line height: 24sp (1.5x)
- Max width: 600dp (for tablets)

**Details List**
- Layout: Key-value pairs
- Key: Label Large (14sp), Medium weight, 40% width
- Value: Body Large (16sp), Regular, 60% width
- Row height: 40dp
- Vertical dividers between rows (optional)

**Similar Drinks Section**
- Position: After external search link, before end of screen
- Section header: "SIMILAR DRINKS"

**Filter Chips:**
- Component: `FilterChip` (Material 3)
- Layout: Horizontal row with 4dp gap, wraps if needed
- Three filter options (can combine):
  1. "Same Style" - Matches drink style (e.g., all IPAs)
  2. "Same Dispense" - Matches dispense method (e.g., all Cask)
  3. "Similar ABV" - Within ±1% ABV range
- Multiple chips can be active simultaneously
- Default: All three active on load
- Tap chip to toggle filter on/off

**Drink Cards:**
- Horizontal scrolling list (LazyRow)
- Card size: 160dp wide, 120dp tall
- Shows: Drink name, Brewery, ABV%, Dispense
- Layout per card:
  - Line 1: Drink name (truncated if long)
  - Line 2: Brewery name (truncated)
  - Line 3: ABV% • Dispense
- Tap card → Navigate to that drink's detail screen
- Maximum 20 results shown

**Matching Logic:**
- Same Style: Exact match on style field
- Same Dispense: Exact match on dispense (Keg, Cask, etc.)
- Similar ABV: Current ABV ±1%
  - Example: 6.5% drink shows 5.5% - 7.5% range
- Combines filters with AND logic when multiple active
- If no matches found: Hide section entirely
- Sort results by closest ABV match first

**Empty State:**
- If no similar drinks: Don't show section at all
- Section only appears when ≥1 match found

**Accessibility:**
- Filter chips announce state: "Same Style, selected" or "Same Style, not selected"
- Drink cards: "[Drink] by [Brewery], [ABV] percent, [Dispense]"
- Horizontal scroll announced by screen reader

#### Behavior

**Opening:**
- Slide in from right (Material motion)
- Shared element transition from drink card (optional)

**Favorite Button:**
- Toggle favorite status
- Updates icon and label immediately
- Haptic feedback
- Syncs with Favorites list

**Share Button:**
- Opens Android share sheet
- Content: "[Drink Name] by [Brewery] at [Festival Name] - [App deep link]"

**Rating Interaction:** (Local Only)
- Tap any of the 5 stars directly
- Stars fill from left to selected star (e.g., tap 3rd star = ★★★☆☆)
- Immediate visual update (no dialog, no loading)
- Haptic feedback on tap
- Text updates: "You rated X stars"
- Saved locally to device storage (SharedPreferences or Room)
- Can change rating by tapping different star
- No confirmation needed (instant feedback)
- No network request, no backend sync
- Rating persists per festival/drink combination

**External Search Link:**
- Tap link opens default browser
- Search query: "[Drink Name] [Brewery]"
- Uses standard web search (Google, etc.)
- Opens in new browser instance (doesn't leave app)
- Optional: Use Chrome Custom Tabs for in-app browser experience

**Allergen Warning:**
- Always visible (no interaction needed)
- High contrast for accessibility
- Screen readers announce as important information
- No dismiss action (always critical info)

**Availability Chip:**
- Visual indicator only (no tap action)
- Updates in real-time if data refreshed
- Color changes based on status

**Back Navigation:**
- Back button or system gesture
- Returns to previous screen (maintains scroll position)

**Scrolling:**
- Content scrolls under transparent top bar
- Optional: Top bar gains background on scroll (Material elevation)

---

### 7. Search Screen

**Purpose:** Full-text search across all drinks in current festival

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ← [Back]  [Search field......] [X] │ ← Search bar
├─────────────────────────────────────┤
│                                     │
│ RECENT SEARCHES (if no query)      │ ← Optional section
│ • Pilsner                           │
│ • Low ABV                           │
│ • Cloudwater                        │
│                                     │
│ --- OR ---                          │
│                                     │
│ SEARCH RESULTS (if query entered)   │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Hazy IPA              ♡       │  │ ← Same card as main list
│ │ Cloudwater Brew Co.           │  │
│ │ 6.5% • IPA • Draft            │  │
│ └───────────────────────────────┘  │
│                                     │
│ [More results...]                   │
│                                     │
│ --- OR ---                          │
│                                     │
│ NO RESULTS                          │
│ 🔍 No drinks found for "xyz"        │
│ Try different keywords              │
│                                     │
└─────────────────────────────────────┘
```

#### Component Specifications

**Search Bar**
- Component: `SearchBar` (Material 3)
- Height: 56dp
- Auto-focus on open
- Placeholder: "Search drinks, breweries, styles..."
- Leading icon: Back arrow
- Trailing icon: Clear (X) - shows when text entered

**Search Field**
- Typography: Body Large (16sp)
- Input type: Text
- Action: Search on keyboard submit or live (debounced)

**Recent Searches**
- Only shown when search field is empty
- List of recent search terms (last 5-10)
- Tap to populate search field
- Clear individual: Swipe left to delete

**Search Results**
- Same drink card component as Drinks screen
- Sorted by relevance (best match first)
- Searches: Drink name, brewery, style, description

**No Results State**
- Icon: Search/magnifying glass illustration
- Message: "No drinks found for '[query]'"
- Suggestion: "Try different keywords"

#### Behavior

**Opening:**
- Full-screen overlay
- Keyboard opens immediately
- Smooth fade-in animation

**Live Search:**
- Debounced input (300ms delay)
- Shows results as user types
- Optional: Minimum 2 characters to trigger

**Closing:**
- Back button
- System back gesture
- Tap outside (if implemented as modal)

**Selecting Result:**
- Tap drink card → Navigate to Drink Detail
- Search screen remains in back stack

---

## Component Library

### Reusable Components

#### 1. Drink Card

**Component Name:** `DrinkCard`

**Properties:**
- `drinkId: String`
- `drinkName: String`
- `breweryName: String`
- `breweryLocation: String?` (e.g., "Southwold, Suffolk"; parsed from producer.notes, may be null)
- `abv: Float`
- `style: String`
- `dispense: String` (Keg, Cask, Polypin, Bottle, Can, etc.)
- `isFavorited: Boolean`
- `availabilityStatus: AvailabilityStatus?` (Plenty, Low, Out, null)
- `onCardClick: () -> Unit`
- `onFavoriteClick: () -> Unit`

**Material 3 Components Used:**
- `Card` (outlined or elevated)
- `IconButton` (for heart icon)
- `Text` (Title Medium, Body Medium, Body Small, Label Small)

**States:**
- Default
- Pressed (ripple effect)
- Favorited (filled heart)
- Sold out (optional visual dimming)

**Accessibility:**
- Content description: "[Drink name] by [Brewery], [ABV] percent, [Style], [Availability status], tap to view details"
- Heart button: "Add to favorites" / "Remove from favorites"
- Minimum touch target: 48dp for heart
- Availability announced by screen reader

---

#### 2. Bottom Sheet Container

**Component Name:** `FilterBottomSheet`, `SortBottomSheet`, `FestivalSelectorBottomSheet`

**Properties:**
- `title: String`
- `content: @Composable () -> Unit`
- `onDismiss: () -> Unit`

**Material 3 Components Used:**
- `ModalBottomSheet`
- Drag handle (built-in)

**Behavior:**
- Swipe down to dismiss
- Tap scrim to dismiss
- System back button to dismiss

**Accessibility:**
- Announces sheet opening
- Focus moves to sheet content
- Escape closes sheet

---

#### 3. Festival Selector Button

**Component Name:** `FestivalSelectorButton`

**Properties:**
- `festivalName: String`
- `festivalDates: String`
- `onClick: () -> Unit`

**Layout:**
- Two-line button
- Dropdown icon on right
- Tap target: Full width

**States:**
- Default
- Pressed

---

#### 4. Filter/Sort Button

**Component Name:** `FilterButton`, `SortButton`

**Properties:**
- `label: String`
- `activeFilterCount: Int?` (for badge)
- `onClick: () -> Unit`

**Material 3 Components Used:**
- `FilledTonalButton`
- Optional: `Badge` component

**States:**
- Inactive: "Filter" / "Sort"
- Active: "Filter: Beer (87) ✕" / "Sort: ABV ↓"

---

#### 5. Allergen Warning

**Component Name:** `AllergenWarning`

**Properties:**
- `allergens: List<String>` (e.g., ["Gluten", "Barley"])

**Material 3 Components Used:**
- `Surface` with `errorContainer` background
- `Icon` (warning triangle)
- `Text` (Label Large)

**Layout:**
- Icon (16dp) + "Contains: " + allergen list (comma-separated)
- Padding: 12dp all sides
- Corner radius: 8dp
- Color: Error color scheme

**Accessibility:**
- Semantic role: Important announcement
- Content description: "Allergen warning: Contains [list]"
- High priority for screen readers

---

#### 6. Rating Display (Local Only)

**Component Name:** `PersonalRatingWidget`

**Properties:**
- `drinkId: String`
- `festivalId: String`
- `currentRating: Int?` (user's rating 1-5, null if unrated)
- `onRatingChanged: (Int) -> Unit`

**Material 3 Components Used:**
- `IconButton` (for each star, tappable)
- `Icon` (star filled/outline)
- `Text` (Body Medium for status text)

**Layout:**
- Section header: "YOUR RATING"
- Row of 5 interactive stars (24dp each)
- Status text below: "Not rated yet" or "You rated X stars"

**Interaction:**
- Each star is independently tappable
- Tap star N → fills stars 1 through N
- Immediate visual feedback (no delay)
- Haptic feedback on tap
- Saves to local storage (SharedPreferences or Room)

**States:**
- Unrated: ☆☆☆☆☆ "Not rated yet"
- Rated 1: ★☆☆☆☆ "You rated 1 star"
- Rated 3: ★★★☆☆ "You rated 3 stars"
- Rated 5: ★★★★★ "You rated 5 stars"

**Storage:**
- Local only (no backend sync)
- Key format: `rating_${festivalId}_${drinkId}`
- Persists across app sessions

**Accessibility:**
- Each star: "Rate X stars" content description
- Current rating announced: "Your rating: X out of 5 stars"
- Status text readable by screen reader

---

#### 7. Availability Indicator

**Component Name:** `AvailabilityIndicator`

**Properties:**
- `status: AvailabilityStatus` (Plenty, Low, Out)

**Material 3 Components Used:**
- `AssistChip` or text with icon
- Colored circle icon

**Visual Design:**
- 🟢 Green: "Plenty left"
- ⚠️ Amber: "Running low"  
- ⭕ Red: "Sold out"

**States:**
- Only visible if festival has real-time tracking
- Updates when data refreshes

**Accessibility:**
- Clear color contrast
- Text label (not just color)
- Announced by screen reader

---

#### 8. External Link Button

**Component Name:** `ExternalSearchLink`

**Properties:**
- `drinkName: String`
- `breweryName: String`

**Material 3 Components Used:**
- `TextButton` with link icon
- Opens web search

**Behavior:**
- Constructs search query: "[drink] [brewery]"
- Opens in default browser or Chrome Custom Tab
- Icon: Link/search (20dp)

**Accessibility:**
- Clearly labeled as external action
- "Opens in browser" hint

---

#### 9. Similar Drinks Widget

**Component Name:** `SimilarDrinksWidget`

**Properties:**
- `currentDrink: DrinkEntity`
- `allDrinks: List<DrinkEntity>`
- `onDrinkClick: (String) -> Unit` (drinkId)

**Material 3 Components Used:**
- `FilterChip` (for filter options)
- `Card` (for drink cards)
- `LazyRow` (horizontal scrolling)

**Filter Options:**
- Same Style (exact match)
- Same Dispense (exact match)
- Similar ABV (±1% range)

**Behavior:**
- All filters active by default
- Tap filter chip to toggle
- Combines active filters with AND logic
- Updates results immediately
- Shows up to 20 results
- Sorts by closest ABV match
- If no matches: Hide entire section

**Card Layout:**
```
┌────────────────┐
│ Drink Name     │ ← Title Small (14sp)
│ Brewery        │ ← Body Small (12sp)
│ 6.2% • Cask    │ ← ABV + Dispense
└────────────────┘
```

**States:**
- Loading: Skeleton cards
- Results: Show filtered drinks
- Empty: Hide section (no empty state message)

**Accessibility:**
- Filter chips announce selection state
- Drink cards fully accessible
- Horizontal scroll announced

---

### Typography Scale (Material 3)
---

### Typography Scale (Material 3)

```
Display Large: 57sp (unused in this app)
Display Medium: 45sp (unused)
Display Small: 36sp (unused)

Headline Large: 32sp (Drink Detail hero)
Headline Medium: 28sp (unused)
Headline Small: 24sp (unused)

Title Large: 22sp (Top bar festival name, section titles)
Title Medium: 16sp (Drink card name, detail headers)
Title Small: 14sp (unused)

Body Large: 16sp (Drink detail body text, search field)
Body Medium: 14sp (Drink card brewery, subtitles)
Body Small: 12sp (Drink card metadata, captions)

Label Large: 14sp (Button text, section labels)
Label Medium: 12sp (Chip text)
Label Small: 11sp (unused)
```

**Font Family:** Roboto (Android default)  
**Alternative:** Consider a font with personality (Poppins, Inter) for brand differentiation

---

### Color Scheme (Material 3 Dynamic Color)

**Recommended Approach:** Use Material 3 dynamic color based on user's wallpaper (Android 12+)

**Custom Seed Color (if not using dynamic):**
- Primary: Amber/Copper tone (#D97706 or similar)
- Rationale: Evokes beer, warm, approachable
- Material Theme Builder will generate full palette

**Key Colors:**
- Primary: Main brand color (buttons, active states)
- On Primary: Text/icons on primary color
- Primary Container: Subtle backgrounds
- On Primary Container: Text on primary container
- Surface: Main background
- Surface Container: Card backgrounds
- Outline: Borders, dividers

**Dark Mode (Recommended Default):**
- Easier to read outdoors in bright sun
- Saves battery (OLED screens)
- Reduces eye strain in low light
- All Material 3 color tokens adjust automatically

---

### Iconography

**Icon Set:** Material Symbols (Google)  
**Style:** Rounded (softer, more friendly than sharp)  
**Weight:** 400 (Regular) for most icons  
**Size:** 24dp standard, 20dp for small contexts

**Key Icons:**
- Drinks: 🍺 `local_bar` or `liquor`
- Favorites: ⭐ `star` / `star_outline`
- Heart: ❤️ `favorite` / `favorite_border`
- Filter: 🎛️ `tune` or `filter_list`
- Sort: ↕️ `sort` or `swap_vert`
- Search: 🔍 `search`
- Share: `share`
- Festival/Calendar: 📅 `event` or `calendar_today`
- Dropdown: `arrow_drop_down`
- Close: `close`
- Back: `arrow_back`
- Menu: `more_vert`

---

### Elevation & Shadows (Material 3)

Material 3 uses tonal elevation (color overlays) rather than shadows:

- Level 0: Surface (no elevation)
- Level 1: Cards, bottom sheets (subtle tonal change)
- Level 2: FABs, active states
- Level 3: Modals, dialogs
- Level 4: Navigation bar
- Level 5: Top app bar (when elevated)

**Implementation:** Use Material 3 elevation tokens, which apply appropriate color overlays in light/dark mode

---

### Motion & Animation

**Duration Standards:**
- Micro-interactions: 100-150ms (button press, checkbox)
- Small transitions: 200-250ms (card expansion, menu open)
- Medium transitions: 300-400ms (screen change, bottom sheet)
- Large transitions: 400-500ms (page navigation)

**Easing:**
- Standard: Emphasized decelerate (Material motion)
- Enter screen: Decelerate (starts fast, ends slow)
- Exit screen: Accelerate (starts slow, ends fast)

**Key Animations:**
- Favorite heart: Scale (0.8 → 1.2 → 1.0) + fade, 150ms
- Bottom sheet: Slide up with decelerate, 300ms
- Card press: Ripple effect (Material default)
- Screen navigation: Slide (shared axis), 300ms
- Loading: Skeleton pulse (1.5s loop)

---

## User Workflows

### Primary Workflows

#### Workflow 1: Browse and Favorite a Drink

**Goal:** User finds a drink they want to try and bookmarks it

**Steps:**
1. User opens app → Lands on Drinks screen (current festival auto-selected)
2. User scrolls through drink list
3. User taps heart icon on a drink card
4. Heart fills with color, haptic feedback
5. Drink is now in Favorites list

**Alternate Path:**
- 3a. User taps drink card to see details first
- 3b. User reads description
- 3c. User taps "Favorite" button in detail screen
- 3d. Returns to list (back button), sees filled heart

**Success Criteria:**
- Heart icon updates immediately (no delay)
- Drink appears in Favorites tab
- Visual feedback confirms action

---

#### Workflow 2: Filter by Drink Category

**Goal:** User wants to see only beers (or another category)

**Steps:**
1. User is on Drinks screen
2. User taps "Filter" button
3. Bottom sheet opens with category list
4. User taps "Beer" radio button
5. "Apply (87)" button shows updated count
6. User taps "Apply"
7. Sheet closes, list refreshes to show only beers
8. Filter button now shows "Filter: Beer (87) ✕"

**Alternate Path:**
- 6a. User changes mind, taps "Clear" button
- 6b. All filters reset, user can re-select
- 6c. Or user swipes down / taps scrim to dismiss without applying

**Success Criteria:**
- Filter applies immediately on "Apply" tap
- Drink count updates correctly
- Filter button shows active state
- List shows only selected category

---

#### Workflow 3: Sort Drinks by ABV

**Goal:** User wants to see strongest drinks first

**Steps:**
1. User is on Drinks screen (any filter state)
2. User taps "Sort" button
3. Bottom sheet opens with sort options
4. User taps "ABV (High to Low)"
5. Sheet auto-closes (or user taps Done)
6. List re-sorts immediately, strongest drinks at top
7. Sort button shows "Sort: ABV High→Low"

**Success Criteria:**
- Sort applies immediately
- Drinks reorder correctly (visible change)
- Sort button shows active state
- Sort persists across app usage (until changed)

---

#### Workflow 4: Switch to Past Festival

**Goal:** User wants to see drinks from a previous festival

**Steps:**
1. User is on Drinks screen (current festival)
2. User taps festival name at top
3. Bottom sheet opens with festival list
4. User scrolls to "Oktoberfest 2023"
5. User taps that festival card
6. Sheet closes
7. Brief loading state (optional skeleton)
8. Drinks list updates to show Oktoberfest drinks
9. Top bar shows "Oktoberfest 2023"
10. Favorites tab now shows favorites from Oktoberfest

**Success Criteria:**
- Festival switches seamlessly
- All app data updates (drinks, favorites)
- No data from previous festival bleeds through
- User can navigate back to current festival easily

---

#### Workflow 5: Search for a Specific Drink

**Goal:** User remembers a drink name and wants to find it quickly

**Steps:**
1. User is on Drinks screen
2. User taps search icon (top right)
3. Search screen opens, keyboard appears
4. User types "Pilsner"
5. Results appear as user types (debounced)
6. User sees "Pilsner Urquell" in results
7. User taps that card
8. Drink Detail screen opens
9. User reads details, taps "Favorite"

**Alternate Path:**
- 5a. No results found
- 5b. "No drinks found" message displays
- 5c. User clears search, tries different term

**Success Criteria:**
- Search is fast (< 500ms after typing stops)
- Results are relevant (searches name, brewery, style)
- Keyboard doesn't block results
- Easy to clear and try again

---

#### Workflow 6: View Favorited Drinks

**Goal:** User at the bar wants to quickly review their saved drinks

**Steps:**
1. User taps "Favorites" in bottom navigation
2. Favorites screen shows list of bookmarked drinks
3. User scrolls through 15 saved drinks
4. User decides on "Hazy IPA"
5. User taps card to see details (optional)
6. User orders drink at bar

**Alternate Path:**
- 3a. User changes mind on one drink
- 3b. User taps filled heart to unfavorite
- 3c. Card animates out, count decreases

**Success Criteria:**
- Favorites load instantly (offline data)
- List shows all favorited drinks from current festival
- Easy to unfavorite from this screen
- One-handed scrolling works smoothly

---

### Edge Case Workflows

#### Workflow 7: First Time User

**Steps:**
1. User installs app
2. Opens app
3. Festival selector appears (no default festival)
4. User selects "Summer Beer Fest 2024"
5. App downloads festival data (loading state)
6. Lands on Drinks screen with full list
7. Optional: Onboarding tooltip points to key features

---

#### Workflow 8: Offline Usage

**Steps:**
1. User at festival with no cell service
2. Opens app (festival data previously downloaded)
3. Browses drinks, filters work normally
4. Favorites drinks (saved locally)
5. Leaves festival area, gets signal
6. App syncs favorites to cloud (background)

**Requirement:** Offline-first architecture

---

#### Workflow 9: No Favorites Yet

**Steps:**
1. New user taps "Favorites" tab
2. Empty state appears
3. Message: "No favorites yet" + illustration
4. Button: "Browse Drinks"
5. User taps button → Returns to Drinks screen

---

#### Workflow 10: View Festival Details and Get Directions

**Goal:** User wants to learn more about the festival and get directions to the venue

**Steps:**
1. User is on Drinks screen (or any screen)
2. User taps festival name at top
3. Festival Selector bottom sheet opens
4. User taps info button (ℹ️) on "Summer Beer Fest 2024"
5. Festival Overview screen opens
6. User sees festival dates, location, description
7. User reads festival hours and description
8. User taps "Open in Maps" button
9. Device maps app opens with festival location
10. User gets directions to venue

**Alternate Path A - Visit Website:**
- 8a. User taps "Visit Festival Website" instead
- 8b. Browser opens to festival website
- 8c. User learns more about the festival

**Alternate Path B - Switch Festival:**
- 8a. User is viewing a past festival's overview
- 8b. User taps "Switch to this festival" button
- 8c. App switches to that festival context
- 8d. Returns to Drinks screen with that festival loaded

**Success Criteria:**
- Festival details clearly displayed
- Maps integration works smoothly
- Website opens in browser
- Festival switching works correctly

---

## End-to-End Test Scenarios

### E2E Test 1: Browse and Favorite Flow

**Test Name:** `test_browse_and_favorite_drink`

**Preconditions:**
- App installed with test data
- Current festival: "Test Festival 2024" (50 drinks)
- User has 0 favorites

**Steps:**
1. Launch app
2. Assert: Lands on Drinks screen
3. Assert: "Test Festival 2024" shown in top bar
4. Assert: Drink list shows 50 drinks
5. Scroll to 3rd drink card
6. Assert: Heart icon is outline (unfavorited)
7. Tap heart icon on 3rd drink
8. Assert: Heart fills with color (favorited state)
9. Assert: Haptic feedback occurs
10. Tap "Favorites" tab in bottom nav
11. Assert: Favorites screen shows 1 drink
12. Assert: 3rd drink from step 5 is in favorites list

**Expected Result:** Drink successfully favorited and appears in Favorites list

**Validation Points:**
- Heart icon state change (outline → filled)
- Favorites count updates
- Drink appears in Favorites tab
- UI responds within 100ms

---

### E2E Test 2: Filter by Category

**Test Name:** `test_filter_drinks_by_category`

**Preconditions:**
- App at Drinks screen
- Current festival has: 30 beers, 10 ciders, 10 wines

**Steps:**
1. Assert: Drink list shows 50 drinks total
2. Assert: Filter button shows "Filter" (no active filter)
3. Tap "Filter" button
4. Assert: Bottom sheet opens with category list
5. Assert: "All (50)" is selected by default
6. Tap "Beer" radio button
7. Assert: "Apply (30)" button shows beer count
8. Tap "Apply" button
9. Assert: Bottom sheet closes
10. Assert: Drink list now shows 30 drinks
11. Assert: All visible drinks are in Beer category
12. Assert: Filter button shows "Filter: Beer (30) ✕"
13. Tap ✕ on filter button
14. Assert: Filter clears, list shows 50 drinks again

**Expected Result:** Filter correctly reduces list to beers only, then clears

**Validation Points:**
- Count updates accurately
- Only beers shown after filter
- Filter button state updates
- Clear filter works

---

### E2E Test 3: Sort Drinks

**Test Name:** `test_sort_drinks_by_abv`

**Preconditions:**
- App at Drinks screen
- Drinks with various ABVs: 4.5%, 6.2%, 8.0%, 5.5%, 7.1%

**Steps:**
1. Note: Default sort is Name (A-Z)
2. Get first drink ABV (record value)
3. Tap "Sort" button
4. Assert: Bottom sheet opens
5. Tap "ABV (High to Low)" option
6. Assert: Sheet closes
7. Assert: First drink in list has highest ABV
8. Assert: Drinks are in descending ABV order
9. Assert: Sort button shows "Sort: ABV High→Low"

**Expected Result:** Drinks reorder by ABV, highest first

**Validation Points:**
- Sort applies immediately
- Correct order (can sample first 3 drinks)
- Sort button label updates

---

### E2E Test 4: Switch Festival

**Test Name:** `test_switch_to_past_festival`

**Preconditions:**
- Two festivals in database:
  - "Current Festival" (active): 50 drinks
  - "Past Festival" (ended): 40 drinks
- Currently viewing "Current Festival"

**Steps:**
1. Assert: Top bar shows "Current Festival"
2. Assert: Drink list shows 50 drinks
3. Tap festival name in top bar
4. Assert: Bottom sheet opens with festival list
5. Assert: "Current Festival" has checkmark (selected)
6. Tap "Past Festival" card
7. Assert: Bottom sheet closes
8. Wait for data load (max 2 seconds)
9. Assert: Top bar shows "Past Festival"
10. Assert: Drink list shows 40 drinks
11. Assert: Drinks are from Past Festival (check sample names)

**Expected Result:** App context switches to past festival completely

**Validation Points:**
- Festival name updates
- Drink count changes correctly
- No drinks from previous festival remain
- Favorites also switch context

---

### E2E Test 5: Search Flow

**Test Name:** `test_search_for_drink`

**Preconditions:**
- Festival has drinks including "Pilsner Urquell" by "Pilsner Urquell Brewery"

**Steps:**
1. Tap search icon in top bar
2. Assert: Search screen opens, keyboard visible
3. Assert: Search field is focused
4. Type "Pilsner" in search field
5. Wait 300ms (debounce)
6. Assert: Results appear
7. Assert: "Pilsner Urquell" is in results
8. Assert: Results contain 1-5 drinks (depends on data)
9. Tap "Pilsner Urquell" card
10. Assert: Navigate to Drink Detail screen
11. Assert: Drink name is "Pilsner Urquell"

**Expected Result:** Search finds correct drink, detail screen loads

**Validation Points:**
- Search results appear quickly (< 500ms)
- Results are relevant
- Tap navigates correctly
- Back button returns to search (preserves query)

---

### E2E Test 6: View Favorites

**Test Name:** `test_view_favorites_list`

**Preconditions:**
- User has favorited 5 drinks in current festival

**Steps:**
1. Start at Drinks screen
2. Tap "Favorites" tab in bottom nav
3. Assert: Navigate to Favorites screen
4. Assert: Top bar shows "[Festival Name]"
5. Assert: Subtitle shows "5 favorites"
6. Assert: List shows 5 drink cards
7. Assert: All hearts are filled (favorited state)
8. Tap heart on 1st drink to unfavorite
9. Assert: Card animates out
10. Assert: List now shows 4 drinks
11. Assert: Subtitle updates to "4 favorites"

**Expected Result:** Favorites screen shows correct drinks, unfavorite works

**Validation Points:**
- Count is accurate
- Only favorited drinks shown
- Unfavorite removes from list
- Animation is smooth

---

### E2E Test 7: Empty States

**Test Name:** `test_empty_favorites_state`

**Preconditions:**
- User has 0 favorites in current festival

**Steps:**
1. Tap "Favorites" tab
2. Assert: Empty state appears
3. Assert: Message reads "No favorites yet"
4. Assert: Illustration/icon is visible
5. Assert: "Browse Drinks" button is visible
6. Tap "Browse Drinks" button
7. Assert: Navigate back to Drinks screen

**Expected Result:** Empty state displays, button navigates correctly

---

### E2E Test 8: Filter with Multiple Options

**Test Name:** `test_filter_by_category_and_abv`

**Preconditions:**
- Festival has beers with ABVs ranging 4-10%

**Steps:**
1. Tap "Filter" button
2. Select "Beer" category
3. Adjust ABV range slider to 5-7%
4. Assert: "Apply (X)" button shows reduced count
5. Tap "Apply"
6. Assert: List shows only beers with 5-7% ABV
7. Verify: Sample 3 drinks meet criteria
8. Tap "Filter" button again
9. Tap "Clear" button
10. Tap "Apply"
11. Assert: All drinks shown again

**Expected Result:** Combined filters work, clear resets all

---

### E2E Test 9: Drink Detail Navigation

**Test Name:** `test_view_drink_details`

**Preconditions:**
- Festival has drink "Hazy IPA" with complete data

**Steps:**
1. At Drinks screen
2. Find "Hazy IPA" card (scroll if needed)
3. Tap card
4. Assert: Navigate to Drink Detail screen
5. Assert: Title shows "Hazy IPA"
6. Assert: Brewery name visible
7. Assert: ABV chip visible
8. Assert: Description text visible
9. Assert: "Favorite" button visible
10. Tap "Favorite" button
11. Assert: Button changes to "Favorited" state
12. Tap back button
13. Assert: Return to Drinks screen
14. Assert: "Hazy IPA" card now has filled heart

**Expected Result:** Detail screen shows complete info, favorite syncs

---

### E2E Test 10: Offline Access

**Test Name:** `test_offline_browsing`

**Preconditions:**
- App has cached festival data
- Device in airplane mode (no network)

**Steps:**
1. Launch app
2. Assert: Drinks screen loads from cache
3. Assert: Drink list is visible (not loading state)
4. Scroll through list
5. Assert: All content loads (no broken images/data)
6. Tap a drink card
7. Assert: Detail screen loads
8. Tap "Favorite" button
9. Assert: Favorite action succeeds (saved locally)
10. Return to Drinks screen
11. Tap "Favorites" tab
12. Assert: Favorited drink appears in list

**Expected Result:** Full app functionality works offline

**Validation Points:**
- No network errors shown
- All data cached properly
- Favorites persist locally
- UI indicates offline mode (optional)

---

### E2E Test 11: Rapid Favoriting

**Test Name:** `test_rapid_favorite_multiple_drinks`

**Preconditions:**
- Drinks screen with 10+ drinks visible

**Steps:**
1. Tap heart on 1st drink
2. Immediately tap heart on 2nd drink
3. Immediately tap heart on 3rd drink
4. Assert: All 3 hearts fill correctly
5. Assert: No UI lag or missed taps
6. Tap "Favorites" tab
7. Assert: All 3 drinks in favorites list
8. Assert: Count shows "3 favorites"

**Expected Result:** Rapid interactions handled correctly, no race conditions

---

### E2E Test 12: Festival Switching Preserves State

**Test Name:** `test_festival_switch_preserves_favorites`

**Preconditions:**
- Current festival has 3 favorited drinks
- Past festival has 2 favorited drinks

**Steps:**
1. At current festival, assert 3 favorites
2. Switch to past festival
3. Tap "Favorites" tab
4. Assert: Shows 2 favorites from past festival
5. Assert: Current festival favorites NOT shown
6. Switch back to current festival
7. Tap "Favorites" tab
8. Assert: Shows original 3 favorites
9. Assert: Favorites persisted correctly

**Expected Result:** Each festival maintains separate favorite lists

---

## Material 3 Implementation Notes

### Compose Setup

```kotlin
// Theme setup
@Composable
fun BeerFestivalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Key Material 3 Components to Use

**Navigation:**
- `NavigationBar` (bottom navigation)
- `TopAppBar` (top app bar)

**Surfaces:**
- `Card` (outlined or elevated variants)
- `ModalBottomSheet`

**Buttons:**
- `FilledButton` (primary actions)
- `FilledTonalButton` (secondary actions)
- `OutlinedButton` (tertiary actions)
- `IconButton` (icon-only actions)

**Selection Controls:**
- `RadioButton` (single selection)
- `Checkbox` (multiple selection)
- `RangeSlider` (ABV range)

**Input:**
- `SearchBar` (Material 3 search)
- `TextField` (if needed)

**Feedback:**
- `SnackBar` (transient messages)
- Ripple effects (built-in to clickable components)

---

### Accessibility Requirements

**Minimum Standards:**
- Touch targets: 48dp minimum (Material guideline)
- Color contrast: WCAG AA (4.5:1 for normal text)
- Text scaling: Support up to 200% font scale
- Screen reader: All interactive elements labeled
- Focus order: Logical tab order

**Content Descriptions:**
- All IconButtons must have contentDescription
- Images must have meaningful descriptions
- Interactive cards should announce their purpose

**Semantic Properties:**
```kotlin
Card(
    modifier = Modifier.semantics {
        contentDescription = "Hazy IPA by Cloudwater Brew Co, 6.5 percent ABV"
        role = Role.Button
    }
)
```

---

### Performance Targets

**Metrics:**
- App launch: < 2 seconds (cold start)
- Screen navigation: < 300ms
- Filter/sort application: < 500ms
- Search results: < 500ms
- Image loading: Progressive (blur-up)

**Optimization:**
- Lazy loading for long lists (`LazyColumn`)
- Image caching (Coil library)
- Database indexing on search fields
- Minimal recomposition (use `remember`, `derivedStateOf`)

---

### Data Architecture

**Recommended Structure:**
```
FestivalEntity
├── id: String
├── name: String
├── startDate: Date
├── endDate: Date
├── location: String (venue name)
├── address: String? (full address)
├── latitude: Double? (for maps)
├── longitude: Double? (for maps)
├── description: String? (festival description)
├── websiteUrl: String? (festival website)
├── hours: Map<String, String>? (day -> hours, e.g. "Friday" -> "5PM-11PM")
├── hasRealTimeInventory: Boolean
└── drinks: List<DrinkEntity>

DrinkEntity
├── id: String (SHA-1 hash from API)
├── festivalId: String (foreign key)
├── name: String (product.name)
├── brewery: String (producer.name)
├── breweryLocation: String? (parsed from producer.notes - e.g., "Southwold, Suffolk"; see parsing note below)
├── category: String (product.category - beer, cider, mead, perry, wine, low-no)
├── style: String? (product.style - nullable)
├── abv: Float (product.abv - parsed from string)
├── description: String? (product.notes)
├── dispense: String (product.dispense - Keg, Cask, Polypin, Bottle, Can, KeyKeg)
├── bar: String? [PROPOSED] (product.bar - e.g., "Arctic", "Main Bar"; optional, may be missing for festivals with a single bar or legacy APIs. This is a proposed new field and may not exist in current APIs. If not present in the API, set to null.)
├── allergens: Map<String, Int>? (product.allergens - {"gluten": 1, "sulphites": 1})
└── statusText: String? (product.status_text - "Plenty left", "Running low", "Sold out")

> **Note:** The `bar` field is optional and may not be present in all API responses. For festivals with a single bar, or for legacy data sources that do not provide a `bar` field, this value should be set to `null`. Only use this field if the API includes it.

**Brewery Location Parsing:**
The `breweryLocation` field is derived from the `producer.notes` field in the API. The `notes` field contains combined location and establishment year information in the format: `"Location, Region est. YEAR"`. 

Example API values:
- `"Southwold, Suffolk est. 1890"` → `"Southwold, Suffolk"`
- `"Lawrence Hill, Bristol est. 2007"` → `"Lawrence Hill, Bristol"`
- `"Cambridge, Cambs est. 2013"` → `"Cambridge, Cambs"`
- `"London est. 2008"` → `"London"`

```kotlin
/**
 * Parses brewery location from producer.notes field.
 * The notes field typically contains: "Location est. Year" or "Location, Region est. Year"
 * 
 * @param notes The producer.notes value from the API
 * @return The location portion, or null if parsing fails
 */
fun parseBreweryLocation(notes: String?): String? {
  if (notes.isNullOrBlank()) return null
  
  // Pattern: everything before " est." (case insensitive)
  val estPattern = Regex("""\s+est\.\s*""", RegexOption.IGNORE_CASE)
  val match = estPattern.find(notes)
  
  return if (match != null && match.range.first > 0) {
    notes.substring(0, match.range.first).trim()
  } else {
    // If no "est." found, return the whole notes as location
    // (may be just a location without establishment year)
    notes.trim().takeIf { it.isNotEmpty() }
  }
}
```

> **Note:** If the API is updated in the future to provide a dedicated `producer.location` field, prefer using that over parsing `producer.notes`.

**Availability Status Mapping:**
For UI display, map statusText to availability enum:
- "Plenty left" / "Arrived" / "Available" → "plenty" (green 🟢)
- "Running low" / "Low" → "low" (amber ⚠️)
- "Sold out" / "Out" → "out" (red ⭕)
- null → hide indicator

```kotlin
fun mapAvailabilityStatus(statusText: String?): AvailabilityStatus? {
  return when {
    statusText == null -> null
    statusText.contains("plenty", ignoreCase = true) -> AvailabilityStatus.PLENTY
    statusText.contains("arrived", ignoreCase = true) -> AvailabilityStatus.PLENTY
    statusText.contains("available", ignoreCase = true) -> AvailabilityStatus.PLENTY
    statusText.contains("low", ignoreCase = true) -> AvailabilityStatus.LOW
    statusText.contains("out", ignoreCase = true) -> AvailabilityStatus.OUT
    statusText.contains("sold", ignoreCase = true) -> AvailabilityStatus.OUT
    else -> AvailabilityStatus.PLENTY // Default to available
  }
}

enum class AvailabilityStatus {
  PLENTY,  // Green
  LOW,     // Amber
  OUT      // Red
}
```

FavoriteEntity
├── userId: String
├── drinkId: String
├── festivalId: String
└── createdAt: Date

PersonalRatingEntity (Local only - no sync)
├── drinkId: String
├── festivalId: String
├── rating: Int (1-5)
└── updatedAt: Date
```

**Local Storage:** 
- Room Database (SQLite) for drinks, favorites, festivals

> ⚠️ **Breaking Change:**  
> The current production implementation uses **OrmLite** for local SQLite storage (see `BeerDatabaseHelper.java`). Migrating to Room Database is a significant architectural change and will require a coordinated migration plan, including data migration and updates to all data access code.  
> **Do not implement Room without planning for migration from OrmLite.**  
> See project documentation for migration guidelines and ensure all stakeholders are aware of this change.
- SharedPreferences OR Room for personal ratings (local only)
  - Key format: `rating_${festivalId}_${drinkId}`
  - No userId needed (device-local)

**Remote Sync:** 
- Firebase / REST API for drinks, favorites, festivals
- Ratings NOT synced (local only per device)

**Offline:** 
- Cache festival data on download
- Sync favorites on connection
- Ratings always local (no network dependency)

---

### Error Handling

**Network Errors:**
- Show inline error message
- "Retry" button
- Don't block entire UI

**No Festival Data:**
- Empty state with illustration
- "Download festival" button
- Explain offline functionality

**Search No Results:**
- Clear message: "No drinks found for '[query]'"
- Suggestion: "Try different keywords"
- No dead-end (easy to clear search)

**General Errors:**
- Use Snackbar for transient errors
- Use Dialog for critical errors
- Always provide actionable next step

---

## Appendix: Visual Mockup Examples

### Mockup 1: Drinks Screen (Default State)

```
┌─────────────────────────────────────┐
│ Summer Beer Fest 2024          🔽 🔍│  ← Top bar (64dp)
│ Nov 22-24, 2024                     │
├─────────────────────────────────────┤
│ [ 🎛️ Filter ]        [ ↕️ Sort ]    │  ← Filter bar (56dp)
├─────────────────────────────────────┤
│ 127 drinks                          │  ← Subtitle (32dp)
├─────────────────────────────────────┤
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Hazy Daydream IPA         ♡  │  │  ← Drink card (120dp)
│ │ Cloudwater Brew Co.          │  │
│ │ 6.5% • New England IPA • 🍺  │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Pilsner Urquell           ♡  │  │
│ │ Pilsner Urquell Brewery      │  │
│ │ 4.4% • Czech Pilsner • 🍺    │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Somerset Cider            ♥  │  │  ← Favorited
│ │ Sheppy's Cider               │  │
│ │ 5.5% • Medium Dry Cider • 🍎 │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Chardonnay Reserve        ♡  │  │
│ │ Napa Valley Wines            │  │
│ │ 13.5% • White Wine • 🍷      │  │
│ └───────────────────────────────┘  │
│                                     │
│  ⋮ (more drinks)                    │
│                                     │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│      [ Drinks ]     [ Favorites ]   │  ← Bottom nav (80dp)
│          🍺              ⭐          │
│       (filled)                      │
└─────────────────────────────────────┘
```

---

### Mockup 2: Filter Bottom Sheet (Open)

```
┌─────────────────────────────────────┐
│              ━━━━                   │  ← Drag handle
│                                     │
│ Filters                             │  ← Title (22sp)
│                                     │
│ CATEGORY                            │  ← Label (14sp, 60% opacity)
│ ○ All (127)                         │
│ ● Beer (87)                ← Selected│
│ ○ Cider (15)                        │
│ ○ Wine (12)                         │
│ ○ Mead (8)                          │
│ ○ Spirits (6)                       │
│ ○ Cocktails (5)                     │
│ ○ Sake (3)                          │
│ ○ Kombucha (2)                      │
│ ○ Non-Alcoholic (1)                 │
│                                     │
│ ABV RANGE                           │
│ ●━━━━━○━━━━━━━━━━━●                 │  ← Range slider
│ 0%                        12%       │
│                                     │
│ QUICK FILTERS                       │
│ ☑ Show favorites only               │
│ ☐ Available now                     │
│                                     │
│ ┌──────────┐  ┌──────────────────┐ │
│ │  Clear   │  │  Apply (87)      │ │
│ └──────────┘  └──────────────────┘ │
└─────────────────────────────────────┘
```

---

### Mockup 3: Drinks Screen (Filtered State)

```
┌─────────────────────────────────────┐
│ Summer Beer Fest 2024          🔽 🔍│
│ Nov 22-24, 2024                     │
├─────────────────────────────────────┤
│ [ 🎛️ Beer (87) ✕ ]   [ ↕️ Sort ]   │  ← Active filter
├─────────────────────────────────────┤
│ 87 beers                            │  ← Updated count
├─────────────────────────────────────┤
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Hazy Daydream IPA         ♡  │  │
│ │ Cloudwater Brew Co.          │  │
│ │ 6.5% • New England IPA • 🍺  │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Pilsner Urquell           ♡  │  │
│ │ Pilsner Urquell Brewery      │  │
│ │ 4.4% • Czech Pilsner • 🍺    │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ West Coast IPA            ♡  │  │
│ │ Stone Brewing                │  │
│ │ 7.0% • American IPA • 🍺     │  │
│ └───────────────────────────────┘  │
│                                     │
│  ⋮ (only beers shown)               │
│                                     │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│      [ Drinks ]     [ Favorites ]   │
│          🍺              ⭐          │
└─────────────────────────────────────┘
```

---

### Mockup 4: Favorites Screen

```
┌─────────────────────────────────────┐
│ Summer Beer Fest 2024          🔽 🔍│
│ 15 favorites                        │  ← Count instead of dates
├─────────────────────────────────────┤
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Somerset Cider            ♥  │  │  ← All hearts filled
│ │ Sheppy's Cider               │  │
│ │ 5.5% • Medium Dry Cider      │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Hazy Daydream IPA         ♥  │  │
│ │ Cloudwater Brew Co.          │  │
│ │ 6.5% • New England IPA       │  │
│ └───────────────────────────────┘  │
│                                     │
│ ┌───────────────────────────────┐  │
│ │ Barrel Aged Stout         ♥  │  │
│ │ The Kernel Brewery           │  │
│ │ 11.0% • Imperial Stout       │  │
│ └───────────────────────────────┘  │
│                                     │
│  ⋮ (more favorites)                 │
│                                     │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│      [ Drinks ]     [ Favorites ]   │
│          🍺              ⭐          │
│                       (filled)      │
└─────────────────────────────────────┘
```

---

### Mockup 5: Drink Detail Screen

```
┌─────────────────────────────────────┐
│ ←                              ⋮    │  ← Top bar
├─────────────────────────────────────┤
│                                     │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│ ┃                                 ┃ │
│ ┃ Hazy Daydream IPA               ┃ │  ← Hero (gradient bg)
│ ┃ Cloudwater Brew Co.             ┃ │
│ ┃ Manchester, UK                  ┃ │  ← Brewery location
│ ┃                                 ┃ │
│ ┃ [ ♥ Favorited ]   [ Share ]    ┃ │  ← Action buttons
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ [ 6.5% ][ IPA ][ Draft ]        │ │  ← Info chips
│ │ [ 🟢 Plenty left ]               │ │  ← Availability
│ └─────────────────────────────────┘ │
│                                     │
│ DESCRIPTION                         │
│ A juicy, hazy New England-style    │
│ IPA bursting with tropical fruit   │
│ notes. Brewed with Citra and       │
│ Mosaic hops for maximum flavor.    │
│                                     │
│ ⚠️ Contains: Gluten, Barley         │  ← Allergen warning
│                                     │
│ DETAILS                             │
│ Style      India Pale Ale           │
│ ABV        6.5%                     │
│ Dispense   Cask                     │
│ Bar        Arctic                   │  ← Festival bar
│                                     │
│ TASTING NOTES                       │
│ • Mango and passion fruit           │
│ • Grapefruit and citrus zest        │
│ • Smooth, creamy mouthfeel          │
│ • Balanced bitterness               │
│                                     │
│ YOUR RATING                         │  ← Personal rating
│ ★★★☆☆ You rated 3 stars             │  ← Local only
│                                     │
│ 🔗 Search for this beer online      │  ← External link
│                                     │
│ SIMILAR DRINKS                      │
│ [Same Style][Same Dispense][ABV]   │  ← Filter chips
│ ┌──────────┐ ┌──────────┐         │
│ │ West IPA │ │ Punk IPA │         │  ← Horizontal scroll
│ │ 6.2% Cask│ │ 5.6% Cask│         │
│ └──────────┘ └──────────┘         │
│                                     │
└─────────────────────────────────────┘
```

---

## Development Checklist

### Phase 1: Core Functionality
- [ ] Set up Material 3 theme with dynamic color
- [ ] Implement bottom navigation (Drinks, Favorites)
- [ ] Create Drink Card component
- [ ] Build Drinks screen with lazy list
- [ ] Implement favorite toggle functionality
- [ ] Build Favorites screen
- [ ] Add offline data persistence (Room DB)

### Phase 2: Filtering & Sorting
- [ ] Create Filter bottom sheet
- [ ] Implement category filtering
- [ ] Add ABV range slider
- [ ] Create Sort bottom sheet
- [ ] Implement sort logic (ABV, Name, etc.)
- [ ] Update button states to show active filters

### Phase 3: Festival Management
- [ ] Build Festival Selector bottom sheet
- [ ] Add info button to festival cards
- [ ] Create Festival Overview screen
- [ ] Implement maps integration (geo intent)
- [ ] Implement website link (browser/Custom Tabs)
- [ ] Implement festival switching
- [ ] Handle festival data loading
- [ ] Preserve favorites per festival

### Phase 4: Details & Search
- [ ] Create Drink Detail screen
- [ ] Implement full-screen search
- [ ] Add search debouncing and results
- [ ] Add availability status display (optional)
- [ ] Implement allergen warning component
- [ ] Build rating display and submission
- [ ] Add external search link functionality

### Phase 5: Polish & Accessibility
- [ ] Add all animations and transitions
- [ ] Implement haptic feedback
- [ ] Add content descriptions
- [ ] Test with TalkBack
- [ ] Test with large fonts (200% scale)
- [ ] Optimize performance (lazy loading)

### Phase 6: Testing
- [ ] Write unit tests for business logic
- [ ] Write E2E tests (all scenarios above)
- [ ] Test offline functionality
- [ ] Test on various screen sizes
- [ ] Test dark/light modes

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Nov 23, 2025 | Initial | Complete UI specification |
| 1.1 | Nov 23, 2025 | Update | Added availability status, allergen warnings, rating system, and external search link |
| 1.2 | Nov 23, 2025 | Update | Changed rating system to local-only (no aggregation, no sync) |
| 1.3 | Nov 23, 2025 | Update | Added Festival Overview screen with dates, location, maps, and website links |
| 1.4 | Nov 23, 2025 | Update | Changed "format/serving" to "dispense" field; Enhanced Similar Drinks with filter chips |
| 1.5 | Nov 23, 2025 | Update | API compatibility update: Removed IBU and price; Added bar and brewery location fields |
| 1.6 | Nov 26, 2025 | Update | Fixed breweryLocation field mapping: changed from non-existent producer.location to parsing from producer.notes; Added parsing function and examples |

---

**Document Status:** ✅ Ready for Development
