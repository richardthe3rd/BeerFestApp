# Share Function Issues

**Problem:** Share feature doesn't work properly or shows limited options.

**Reported Bugs:**
- Share button doesn't work at all
- Wrong hashtag in shared posts
- Share widget from beer details view doesn't show all sharing options (unlike long press on list)

---

## Bug: Limited Share Options from Details View

**Symptom:** When sharing from beer details screen, only some apps appear in share menu. Long-pressing a beer in the list shows more options.

**Root Cause:** Missing `Intent.createChooser()` in details view share handler.

### Diagnosis

```bash
# Check BeerSharer implementation
grep -n "createChooser\|Intent.ACTION_SEND" app/src/main/java/ralcock/cbf/actions/BeerSharer.java

# Compare implementations
grep -A 10 "shareIntent" app/src/main/java/ralcock/cbf/view/BeerDetailsFragment.java
grep -A 10 "shareIntent" app/src/main/java/ralcock/cbf/view/BeerListFragment.java
```

### The Problem

**Incorrect Implementation (Limited Options):**
```java
// In BeerDetailsFragment.java - WRONG!
Intent shareIntent = new Intent(Intent.ACTION_SEND);
shareIntent.setType("text/plain");
shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
startActivity(shareIntent); // ← Missing chooser!
```

This directly starts ONE app's share handler, instead of showing a chooser.

**Correct Implementation (All Options):**
```java
// In BeerListFragment.java - CORRECT!
Intent shareIntent = new Intent(Intent.ACTION_SEND);
shareIntent.setType("text/plain");
shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);

// Create chooser to show ALL apps
Intent chooser = Intent.createChooser(shareIntent, "Share beer via...");
if (shareIntent.resolveActivity(getPackageManager()) != null) {
    startActivity(chooser);
}
```

### The Fix

**Step 1:** Update BeerSharer.java to always use chooser

```java
public class BeerSharer {
    public static void shareBeer(Context context, Beer beer) {
        String shareText = formatShareText(beer);
        String shareSubject = formatShareSubject(beer);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);

        // IMPORTANT: Use createChooser for full share menu
        Intent chooser = Intent.createChooser(shareIntent, "Share beer via...");

        // Safety check
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        } else {
            Toast.makeText(context, "No apps available to share", Toast.LENGTH_SHORT).show();
        }
    }

    private static String formatShareText(Beer beer) {
        String festivalHashtag = getString(R.string.festival_hashtag);
        return String.format("Drinking %s by %s at #%s!",
            beer.getName(),
            beer.getBrewery().getName(),
            festivalHashtag);
    }

    private static String formatShareSubject(Beer beer) {
        String festivalName = getString(R.string.festival_name);
        return String.format("Drinking a %s at the %s",
            beer.getName(),
            festivalName);
    }
}
```

**Step 2:** Use BeerSharer consistently everywhere

```java
// In BeerDetailsFragment.java
shareButton.setOnClickListener(v -> {
    BeerSharer.shareBeer(getContext(), currentBeer);
});

// In BeerListFragment.java (long-press handler)
@Override
public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    Beer beer = adapter.getItem(position);
    BeerSharer.shareBeer(getContext(), beer);
    return true;
}
```

### Testing

```bash
# Build and install
./gradlew installDebug

# Manual test:
# 1. Open beer details
# 2. Tap share button
# 3. Verify all apps appear: Twitter, Facebook, WhatsApp, Email, etc.
# 4. Share to one app
# 5. Verify text includes beer name, brewery, and correct hashtag
```

---

## Bug: Wrong Hashtag

**Symptom:** Shared text uses old festival hashtag (e.g., #cbf2024 instead of #cbf2025).

### Root Cause

`festival.xml` wasn't updated during annual festival update.

### Diagnosis

```bash
# Check current hashtag
grep "festival_hashtag" app/src/main/res/values/festival.xml

# Compare to festival year
grep "festival_name" app/src/main/res/values/festival.xml

# Should match! If festival_name says 2025 but hashtag says 2024, there's a mismatch.
```

### Fix

Update `festival.xml`:
```xml
<resources>
    <string name="festival_hashtag">cbf2025</string>  <!-- Update this -->
</resources>
```

See [Annual Updates](../annual-updates/manual-process.md#step-2-update-festivalxml) for full process.

---

## Bug: Share Text Incomplete

**Symptom:** Shared text missing beer name, brewery, or other details.

### Root Cause

Null values in Beer object.

### Diagnosis

```java
// Add logging to BeerSharer
Log.d("BeerSharer", "Beer: " + beer);
Log.d("BeerSharer", "  Name: " + beer.getName());
Log.d("BeerSharer", "  Brewery: " + beer.getBrewery());
if (beer.getBrewery() != null) {
    Log.d("BeerSharer", "  Brewery Name: " + beer.getBrewery().getName());
}
```

### Fix

Add null checks:
```java
private static String formatShareText(Beer beer) {
    if (beer == null) {
        return "Check out the Cambridge Beer Festival!";
    }

    String beerName = beer.getName() != null ? beer.getName() : "a great beer";

    String breweryName = "a local brewery";
    if (beer.getBrewery() != null && beer.getBrewery().getName() != null) {
        breweryName = beer.getBrewery().getName();
    }

    String hashtag = getString(R.string.festival_hashtag);

    return String.format("Drinking %s by %s at #%s!", beerName, breweryName, hashtag);
}
```

---

## Bug: Share Button Doesn't Work at All

**Symptom:** Tapping share does nothing.

### Common Causes

1. **No click listener set**
2. **Exception thrown silently**
3. **Button disabled**

### Diagnosis

```java
// Add logging
shareButton.setOnClickListener(v -> {
    Log.d("ShareButton", "Share button clicked");
    try {
        BeerSharer.shareBeer(getContext(), currentBeer);
        Log.d("ShareButton", "Share completed");
    } catch (Exception e) {
        Log.e("ShareButton", "Share failed", e);
        Toast.makeText(getContext(), "Share failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
});
```

Check logcat:
```bash
adb logcat | grep ShareButton
```

### Fix

```java
// Ensure listener is set in onViewCreated or onCreateView
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    shareButton = view.findViewById(R.id.shareButton);
    shareButton.setOnClickListener(v -> shareBeer());
}

private void shareBeer() {
    if (currentBeer == null) {
        Toast.makeText(getContext(), "No beer to share", Toast.LENGTH_SHORT).show();
        return;
    }

    try {
        BeerSharer.shareBeer(getContext(), currentBeer);
    } catch (Exception e) {
        Log.e(TAG, "Failed to share beer", e);
        Toast.makeText(getContext(), "Failed to share", Toast.LENGTH_SHORT).show();
    }
}
```

---

## Testing Checklist

- [ ] Share from beer details view shows all apps
- [ ] Share from list long-press shows all apps
- [ ] Both methods show same apps
- [ ] Share text includes beer name
- [ ] Share text includes brewery name
- [ ] Share text includes correct hashtag (#cbf2025)
- [ ] Share to Twitter works
- [ ] Share to WhatsApp works
- [ ] Share to Email works
- [ ] Share subject line correct

---

## Related Issues

- [Annual Update Process](../annual-updates/manual-process.md#step-2-update-festivalxml) - Updating hashtag
- [BeerSharer.java](../../app/src/main/java/ralcock/cbf/actions/BeerSharer.java) - Implementation
- [Android Share Intent Docs](https://developer.android.com/training/sharing/send)

---

**Back to:** [Troubleshooting](README.md)
