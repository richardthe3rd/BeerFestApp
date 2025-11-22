# Material Design 3 Migration Plan

**Status:** Proposed
**Priority:** Medium
**Effort:** 6-9 days (incremental approach)
**Last Updated:** 2025-11-22

---

## Problem

The app uses outdated Material Design components:
- Theme: `Theme.AppCompat.Light.NoActionBar` (pre-Material Design)
- Material library: `1.8.0` (Material 2)
- Visual style: Flat colors, sharp corners, shadow-based elevation

The app looks dated compared to modern Android apps using Material 3's rounded corners, tonal elevation, and dynamic colors.

---

## Current State

### Dependencies
```groovy
// app/build.gradle
implementation 'com.google.android.material:material:1.8.0'
```

### Theme
```xml
<!-- AndroidManifest.xml -->
android:theme="@style/Theme.AppCompat.Light.NoActionBar"
```

### Components in Use

| Component | Location | M3 Equivalent |
|-----------|----------|---------------|
| `Theme.AppCompat.Light.NoActionBar` | AndroidManifest.xml | `Theme.Material3.Light.NoActionBar` |
| `androidx.appcompat.widget.Toolbar` | 2 layouts | Keep (works with M3) |
| `com.google.android.material.tabs.TabLayout` | beer_listview_activity.xml | Auto-adapts to M3 |
| `androidx.viewpager.widget.ViewPager` | beer_listview_activity.xml | Keep (ViewPager2 migration separate) |
| `androidx.appcompat.widget.SearchView` | menu/list_options_menu.xml | Keep (M3 SearchBar migration separate) |
| `AlertDialog` | 3 dialog fragments | `MaterialAlertDialogBuilder` |
| `RatingBar` | 2 layouts | Keep (may need custom styling) |
| `ThemeOverlay.AppCompat.*` | 2 layouts | `ThemeOverlay.Material3.*` |

### Layout Files (8 total)
- `beer_listview_activity.xml` - Main list with Toolbar + TabLayout
- `beer_details_fragment.xml` - Detail view with Toolbar
- `beer_details_activity.xml` - Detail activity container
- `beer_listview_fragment.xml` - List fragment
- `beer_listitem.xml` - List item layout
- `beer_style_view.xml` - Style indicator
- `about_dialog.xml` - About dialog
- `sortby_dialog_fragment.xml` - Sort options dialog

### Icons
- 40+ PNG icons in `drawable-*` folders
- Named `*_black_*.png` (hardcoded black color)
- No vector drawables

---

## Visual Changes (M2 → M3)

### Color System
| Element | Current (M2) | Material 3 |
|---------|--------------|------------|
| Primary colors | Flat, single shade | Tonal palette (5 tones) |
| Surfaces | White/gray | Tinted with primary color |
| Elevation | Drop shadows | Tonal (darker = higher) |

### Shape
| Element | M2 | M3 |
|---------|-----|-----|
| Buttons | 4dp corners | 20dp (fully rounded) |
| Cards | 4dp corners | 12dp corners |
| Dialogs | 4dp corners | 28dp corners |

### Components
- **Toolbar**: Solid color → Surface-tinted, larger titles
- **Tabs**: Underline indicator → Pill-shaped indicator
- **Dialogs**: Sharp corners → Rounded (28dp)
- **List items**: Compact → More padding, larger touch targets

---

## Risk Analysis

### High Risk

#### 1. SearchView Compatibility
```xml
<!-- menu/list_options_menu.xml -->
app:actionViewClass="androidx.appcompat.widget.SearchView"
```
- AppCompat SearchView, not Material SearchView
- M3 uses completely different `SearchBar` + `SearchView` API
- **15 test files** reference search functionality
- **Mitigation**: Keep AppCompat SearchView initially - it works with M3 theme

### Medium Risk

#### 2. Black PNG Icons
- 40+ icons hardcoded as black PNGs
- May clash with M3's tinted surfaces
- **Mitigation**: Add `android:tint="?attr/colorOnSurface"` or replace incrementally

#### 3. minSdk 21 Degradation
- M3 works on API 21 but with reduced fidelity
- No dynamic colors (requires API 31+)
- **Mitigation**: Test on API 21 emulator before merging

#### 4. Espresso Tests
- Tests check `R.id.mainListView`, `R.id.search`, `android.R.id.list`
- Layout changes could break tests
- **Mitigation**: Don't change IDs; run tests after each step

### Low Risk

#### 5. ThemeOverlay References
```xml
android:theme="@style/ThemeOverlay.AppCompat.ActionBar"
app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
```
- May not exist in pure M3 context
- **Mitigation**: Update to `ThemeOverlay.Material3.*` equivalents

#### 6. ProGuard Rules
- Old config in `proguard.cfg`
- Modern Material library includes consumer rules
- **Mitigation**: Probably fine; verify release build works

---

## Implementation Plan

### Phase 1: Foundation (1-2 hours)

**Step 1.1: Update dependency**
```groovy
// app/build.gradle
implementation 'com.google.android.material:material:1.12.0'
```

**Step 1.2: Create themes.xml**
```xml
<!-- app/src/main/res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.BeerFest" parent="Theme.Material3.Light.NoActionBar">
        <!-- Primary brand color -->
        <item name="colorPrimary">@color/beer_primary</item>
        <item name="colorOnPrimary">@color/beer_on_primary</item>

        <!-- Secondary brand color -->
        <item name="colorSecondary">@color/beer_secondary</item>
        <item name="colorOnSecondary">@color/beer_on_secondary</item>

        <!-- Surface colors -->
        <item name="colorSurface">@color/beer_surface</item>
        <item name="colorOnSurface">@color/beer_on_surface</item>

        <!-- Keep AppCompat toolbar working -->
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
    </style>
</resources>
```

**Step 1.3: Create colors.xml**
```xml
<!-- app/src/main/res/values/colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Primary: Beer amber/gold -->
    <color name="beer_primary">#C17900</color>
    <color name="beer_on_primary">#FFFFFF</color>

    <!-- Secondary: Complementary -->
    <color name="beer_secondary">#5D5016</color>
    <color name="beer_on_secondary">#FFFFFF</color>

    <!-- Surface -->
    <color name="beer_surface">#FFFBFF</color>
    <color name="beer_on_surface">#1E1B16</color>

    <!-- Background -->
    <color name="beer_background">#FFFBFF</color>
    <color name="beer_on_background">#1E1B16</color>
</resources>
```

**Step 1.4: Update AndroidManifest.xml**
```xml
android:theme="@style/Theme.BeerFest"
```

**Checkpoint**: Build and run tests. App should compile and look slightly different.

---

### Phase 2: Layout Updates (2-3 hours)

**Step 2.1: Update ThemeOverlays in layouts**

Replace in `beer_listview_activity.xml` and `beer_details_fragment.xml`:
```xml
<!-- Before -->
android:theme="@style/ThemeOverlay.AppCompat.ActionBar"
app:popupTheme="@style/ThemeOverlay.AppCompat.Light"

<!-- After -->
android:theme="@style/ThemeOverlay.Material3.ActionBar"
app:popupTheme="@style/ThemeOverlay.Material3.Light"
```

**Step 2.2: Replace hardcoded colors**

Find and replace across all layout files:
```xml
<!-- Before -->
android:background="@android:color/white"
android:background="@android:color/darker_gray"

<!-- After -->
android:background="?attr/colorSurface"
android:background="?attr/colorOutline"
```

**Step 2.3: Update separator views**
```xml
<!-- Before -->
<View android:background="@android:color/darker_gray" ... />

<!-- After -->
<com.google.android.material.divider.MaterialDivider ... />
```

**Checkpoint**: Build and run tests. Verify layouts render correctly.

---

### Phase 3: Dialog Updates (1 hour)

**Step 3.1: Update dialog fragments**

In `AboutDialogFragment.java`, `FilterByStyleDialogFragment.java`, `SortByDialogFragment.java`:
```java
// Before
import androidx.appcompat.app.AlertDialog;
new AlertDialog.Builder(getActivity())

// After
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
new MaterialAlertDialogBuilder(getActivity())
```

**Checkpoint**: Build and run tests. Verify dialogs have rounded corners.

---

### Phase 4: Icon Tinting (1-2 hours)

**Step 4.1: Add tint to toolbar icons**

In `beer_listview_activity.xml` and `beer_details_fragment.xml`, update Toolbar:
```xml
<androidx.appcompat.widget.Toolbar
    ...
    app:iconTint="?attr/colorOnSurface" />
```

**Step 4.2: Add tint to bookmark icons**

In `beer_listitem.xml` and `beer_details_fragment.xml`:
```xml
<ImageView
    android:src="@drawable/ic_bookmark_border_black_48dp"
    android:tint="?attr/colorOnSurfaceVariant"
    ... />
```

**Checkpoint**: Build and verify icons look appropriate on tinted surfaces.

---

### Phase 5: Polish (Optional, 2-3 hours)

**Step 5.1: RatingBar styling**

If RatingBar looks off, add custom style:
```xml
<style name="Widget.BeerFest.RatingBar" parent="Widget.AppCompat.RatingBar">
    <item name="android:progressTint">?attr/colorPrimary</item>
</style>
```

**Step 5.2: Add dark theme support**

Create `values-night/themes.xml`:
```xml
<style name="Theme.BeerFest" parent="Theme.Material3.Dark.NoActionBar">
    <!-- Dark theme colors -->
</style>
```

---

## Deferred Work (Separate PRs)

### ViewPager2 Migration
- Different adapter API
- Fragment lifecycle changes
- Requires significant code changes
- **Recommendation**: Keep ViewPager, migrate later

### SearchView to SearchBar
- M3 SearchBar + SearchView is completely different API
- Would require Java code changes
- Tests explicitly depend on current behavior
- **Recommendation**: Keep AppCompat SearchView indefinitely

### Vector Drawable Migration
- Replace 40+ PNGs with tinted vectors
- Large effort, small visual gain
- **Recommendation**: Add vectors for new icons only

---

## Testing Checklist

### After Each Phase
- [ ] `./gradlew build` succeeds
- [ ] `./gradlew test` passes
- [ ] `./gradlew connectedCheck` passes (if device available)
- [ ] Manual check on emulator

### Final Verification
- [ ] Test on API 21 emulator (minSdk)
- [ ] Test on API 35 emulator (targetSdk)
- [ ] Test on physical device
- [ ] Verify search functionality works
- [ ] Verify dialogs open/close correctly
- [ ] Verify rating bar interaction
- [ ] Verify icons visible on all screens

---

## Rollback Plan

If issues arise:
1. `git revert` the problematic commit
2. Each phase is a separate commit for easy rollback
3. Theme change is the riskiest - can revert to `Theme.AppCompat` quickly

---

## Success Metrics

| Metric | Before | After |
|--------|--------|-------|
| Material library version | 1.8.0 | 1.12.0 |
| Theme | AppCompat | Material3 |
| Dialog corners | Sharp | Rounded (28dp) |
| Tab indicator | Underline | Pill |
| Surface colors | White | Tinted |
| All tests passing | Yes | Yes |

---

## References

- [Material Design 3 for Android](https://m3.material.io/develop/android/mdc-android)
- [Migrate to Material 3](https://developer.android.com/develop/ui/views/theming/material3/migrate)
- [Material 3 Color System](https://m3.material.io/styles/color/overview)
- [Material Theme Builder](https://m3.material.io/theme-builder)

---

## Appendix: Files to Modify

### Definite Changes
| File | Change |
|------|--------|
| `app/build.gradle` | Update material dependency |
| `app/src/main/AndroidManifest.xml` | Change theme reference |
| `app/src/main/res/values/themes.xml` | **CREATE** - M3 theme definition |
| `app/src/main/res/values/colors.xml` | **CREATE** - Color palette |
| `app/src/main/res/layout/beer_listview_activity.xml` | ThemeOverlay, colors |
| `app/src/main/res/layout/beer_details_fragment.xml` | ThemeOverlay, colors |
| `app/src/main/res/layout/beer_listitem.xml` | Icon tint |
| `AboutDialogFragment.java` | MaterialAlertDialogBuilder |
| `FilterByStyleDialogFragment.java` | MaterialAlertDialogBuilder |
| `SortByDialogFragment.java` | MaterialAlertDialogBuilder |

### Optional Changes
| File | Change |
|------|--------|
| `app/src/main/res/values-night/themes.xml` | **CREATE** - Dark theme |
| `beer_details_activity.xml` | Colors if needed |
| `beer_listview_fragment.xml` | Colors if needed |
| `about_dialog.xml` | Layout improvements |
| `sortby_dialog_fragment.xml` | Layout improvements |

---

**Document Author:** Claude
**Review Status:** Ready for implementation
