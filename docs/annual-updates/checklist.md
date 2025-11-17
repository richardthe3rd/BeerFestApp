# Annual Update Checklist

Print this checklist and check off each item as you complete the annual festival update.

---

## Pre-Update

- [ ] **Clean git status** - Commit or stash any pending work
- [ ] **Create branch** (optional but recommended)
  ```bash
  git checkout -b cbf2026-update
  ```
- [ ] **Check data feed is ready** - Verify new year's beer list JSON exists
  ```bash
  curl -I https://data.cambridgebeerfestival.com/cbf2026/beer.json
  # Should return 200 OK (or will return 404 if not ready yet)
  ```

---

## File Updates

### ✓ File 1: app/build.gradle

- [ ] **Increment versionCode** by 1
  - Current: _____ → New: _____
- [ ] **Update versionName** year
  - Current: "______.0.0.1" → New: "______.0.0.1"

### ✓ File 2: app/src/main/res/values/festival.xml

- [ ] **Update festival_name**
  - "Cambridge Beer Festival ______" → "Cambridge Beer Festival ______"
- [ ] **Update festival_hashtag**
  - "cbf______" → "cbf______"
- [ ] **Update beer_list_url**
  - ".../cbf______/beer.json" → ".../cbf______/beer.json"

### ✓ File 3: BeerDatabaseHelper.java

- [ ] **Increment DB_VERSION** by 1
  - Current: _____ → New: _____
- [ ] **Update comment**
  - "// cbf______" → "// cbf______"

---

## Verification

### Automated Checks

- [ ] **Run version consistency check**
  ```bash
  grep "versionName" app/build.gradle
  grep "festival_name\|festival_hashtag\|beer_list_url" app/src/main/res/values/festival.xml
  grep "DB_VERSION" app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java
  ```
  **All years should match: ______**

- [ ] **Review diff**
  ```bash
  git diff
  ```

### Expected Changes

- [ ] **Exactly 3 files modified**
  - app/build.gradle
  - festival.xml
  - BeerDatabaseHelper.java
- [ ] **No unexpected changes** (no other files modified)

---

## Testing

### Build Tests

- [ ] **Clean build succeeds**
  ```bash
  ./gradlew clean build
  ```
- [ ] **Unit tests pass**
  ```bash
  ./gradlew test
  ```
- [ ] **Lint passes** (or review warnings)
  ```bash
  ./gradlew lint
  ```

### Manual Testing (Optional but Recommended)

- [ ] **App launches** on emulator/device
- [ ] **Festival name** displays correctly in UI
  - Shows: "Cambridge Beer Festival ______"
- [ ] **Beer list downloads** from new URL
  - Check logs for download success
- [ ] **Database migration** works
  - Old data cleared, new data loads
- [ ] **Share function** uses correct hashtag
  - Share a beer, check it includes "#cbf______"
- [ ] **No crashes** during basic usage

---

## Commit & Push

- [ ] **Stage files**
  ```bash
  git add app/build.gradle
  git add app/src/main/res/values/festival.xml
  git add app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java
  ```
- [ ] **Commit with standard message**
  ```bash
  git commit -m "cbf______"
  ```
- [ ] **Push to remote**
  ```bash
  git push origin main
  # (or your branch name)
  ```

---

## CI/CD

- [ ] **GitHub Actions build** passes
  - Check: https://github.com/richardthe3rd/BeerFestApp/actions
- [ ] **No build errors** in CI logs
- [ ] **Download release APK** from artifacts
- [ ] **Install and test APK** on physical device
  ```bash
  adb install -r app-release.apk
  ```

---

## Release

- [ ] **Create git tag**
  ```bash
  git tag -a v______.0.0.1 -m "Release for CBF ______"
  git push origin v______.0.0.1
  ```
- [ ] **Upload to Google Play Store**
  - Upload APK/AAB
  - Update release notes
  - Submit for review
- [ ] **Monitor review status**
  - Typically 1-7 days for approval
- [ ] **Announce release** (optional)
  - Social media
  - Festival organizers
  - User mailing list

---

## Post-Release

- [ ] **Monitor crash reports** (first 24 hours)
  - Check Play Console for crashes
- [ ] **Test on real devices** with various Android versions
- [ ] **Monitor user reviews** for issues
- [ ] **Respond to bug reports** quickly

---

## Common Mistakes to Avoid

- [ ] **Verified**: versionCode was incremented (not set to same value)
- [ ] **Verified**: All years match across all files
- [ ] **Verified**: DB_VERSION was incremented
- [ ] **Verified**: Beer list URL is accessible (not 404)
- [ ] **Verified**: Committed only 3 files (no extra changes)

---

## Notes

Use this space for notes specific to this year's update:

```
Date: ____________
Festival year: ____________
Build number: ____________
Special considerations:
_______________________________________
_______________________________________
_______________________________________
```

---

## Completion

- [ ] **All items above checked**
- [ ] **App successfully released**
- [ ] **Update UPDATING.md** if process changed
- [ ] **Update this checklist** if items were missed

**Updated by:** _________________ **Date:** _____________

---

**Save time next year:** Consider implementing [Dynamic Festival Loading](../features/dynamic-festivals.md) to eliminate this entire process!
