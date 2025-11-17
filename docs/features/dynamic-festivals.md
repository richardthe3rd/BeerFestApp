# Dynamic Festival Loading

**Vision:** Eliminate annual app releases by loading festival configurations dynamically from a remote JSON resource.

**Status:** Proposed feature
**ROI:** One-time 2-3 week investment saves 1 week/year forever
**Priority:** ⭐⭐⭐⭐⭐ HIGHLY RECOMMENDED

---

## The Problem

**Current workflow for each new festival:**

1. Update build.gradle version
2. Update festival.xml (3 places)
3. Update BeerDatabaseHelper DB_VERSION
4. Test, commit, build
5. Create release
6. Upload to Play Store
7. Wait for review (1-7 days)
8. Users gradually update app

**Total time:** 1-2 weeks per year
**Error rate:** High (easy to miss a file or forget DB version)

---

## The Solution

**Load festival metadata from a remote JSON catalog.**

```
Current:  Hardcoded 2025 Config → Must release new app for 2026
Proposed: Fetch festivals.json → 2026 appears automatically!
```

### User Experience

```
1. User downloads app (one time)
2. App fetches festival catalog
3. Shows list of available festivals:
   • Cambridge Beer Festival 2026 (Active)
   • Cambridge Beer Festival 2025 (Archived)
   • Cambridge Beer Festival 2024 (Archived)
4. User selects festival → Downloads that festival's beer list
5. New festival added to JSON → Appears automatically!
```

**No app update needed for new festivals!**

---

## Architecture

### Festival Catalog JSON

**Hosted at:** `https://data.cambridgebeerfestival.com/festivals.json`

```json
{
  "festivals": [
    {
      "id": "cbf2026",
      "name": "Cambridge Beer Festival 2026",
      "year": 2026,
      "hashtag": "cbf2026",
      "website": "https://www.cambridgebeerfestival.com/",
      "beerListUrl": "https://data.cambridgebeerfestival.com/cbf2026/beer.json",
      "startDate": "2026-05-25",
      "endDate": "2026-05-30",
      "active": true
    },
    {
      "id": "cbf2025",
      "name": "Cambridge Beer Festival 2025",
      "year": 2025,
      "hashtag": "cbf2025",
      "website": "https://www.cambridgebeerfestival.com/",
      "beerListUrl": "https://data.cambridgebeerfestival.com/cbf2025/beer.json",
      "startDate": "2025-05-26",
      "endDate": "2025-05-31",
      "active": false,
      "archived": true
    }
  ],
  "catalogVersion": 3,
  "lastUpdated": "2026-01-15T10:00:00Z"
}
```

### Database Schema Changes

**Add Festival table:**

```java
@DatabaseTable(tableName = "festivals")
public class Festival {
    @DatabaseField(id = true)
    private String id; // "cbf2026"

    @DatabaseField
    private String name;

    @DatabaseField
    private int year;

    @DatabaseField
    private String hashtag;

    @DatabaseField
    private String beerListUrl;

    @DatabaseField
    private String startDate;

    @DatabaseField
    private String endDate;

    @DatabaseField
    private boolean active;

    @DatabaseField
    private boolean archived;

    @DatabaseField
    private long lastDownloaded; // Timestamp

    // Getters/setters...
}
```

**Link beers to festivals:**

```java
@DatabaseTable(tableName = "beers")
public class Beer {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField(foreign = true)
    private Brewery brewery;

    @DatabaseField(foreign = true)
    private Festival festival; // NEW: Link to festival

    // ... other fields
}
```

### Services

**FestivalCatalogUpdateService:**
```java
public class FestivalCatalogUpdateService extends IntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // 1. Download festivals.json
            String json = downloadCatalog(FESTIVAL_CATALOG_URL);

            // 2. Parse festival list
            FestivalCatalog catalog = parseCatalog(json);

            // 3. Save to database
            saveFestivals(catalog.getFestivals());

            // 4. Check if current festival needs update
            Festival current = getCurrentFestival();
            if (current.needsUpdate()) {
                downloadBeerList(current);
            }

            // 5. Notify UI
            broadcastCatalogUpdated();

        } catch (Exception e) {
            Log.e(TAG, "Failed to update festival catalog", e);
        }
    }
}
```

**BeerListUpdateService (updated):**
```java
public class BeerListUpdateService extends IntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        String festivalId = intent.getStringExtra("festival_id");
        Festival festival = getFestival(festivalId);

        // Download beer list for specific festival
        downloadAndSaveBeers(festival);
    }
}
```

### UI Changes

**Festival Selection Screen:**
```java
public class FestivalSelectionFragment extends Fragment {
    private RecyclerView festivalList;
    private FestivalAdapter adapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        festivalList = view.findViewById(R.id.festivalList);
        adapter = new FestivalAdapter(this::onFestivalSelected);
        festivalList.setAdapter(adapter);

        loadFestivals();
    }

    private void loadFestivals() {
        // Fetch from database (populated by FestivalCatalogUpdateService)
        List<Festival> festivals = getFestivals();
        adapter.setFestivals(festivals);
    }

    private void onFestivalSelected(Festival festival) {
        // Download beer list if not already downloaded
        if (!festival.isDownloaded()) {
            downloadBeerList(festival);
        }

        // Navigate to beer list for this festival
        navigateToBeerList(festival.getId());
    }
}
```

---

## Implementation Phases

### Phase 1: Foundation (2-3 days)

- [ ] Add Festival model and table
- [ ] Add festival_id foreign key to Beer table
- [ ] Database migration logic
- [ ] Test with cbf2024 and cbf2025 data

### Phase 2: Catalog Service (2-3 days)

- [ ] Implement FestivalCatalogUpdateService
- [ ] JSON parsing
- [ ] Error handling and retries
- [ ] Background sync with WorkManager

### Phase 3: Multi-Festival Support (2-3 days)

- [ ] Update BeerListUpdateService for festival-specific downloads
- [ ] Query beers by festival_id
- [ ] Historical data retention

### Phase 4: UI (3-4 days)

- [ ] Festival selection screen
- [ ] "Switch Festival" option in menu
- [ ] Current festival indicator
- [ ] Download progress UI

### Phase 5: Migration (2-3 days)

- [ ] Backward compatibility
- [ ] Migrate existing cbf2025 data
- [ ] Bundled fallback festivals.json
- [ ] Comprehensive testing

### Phase 6: Polish (2-3 days)

- [ ] Handle offline scenarios
- [ ] Cache catalog locally
- [ ] Settings for data retention
- [ ] Analytics/telemetry

**Total: 12-17 days (2-3 weeks)**

---

## Benefits

### For Users
- ✅ No app updates needed for new festivals
- ✅ Browse historical festival data
- ✅ Compare beers across years
- ✅ App always shows current festival automatically

### For Developers
- ✅ No annual app releases
- ✅ Update via JSON file only (5 minutes)
- ✅ No Play Store review delays
- ✅ Instant updates worldwide
- ✅ A/B test different festivals
- ✅ Support multiple concurrent festivals

### For Maintenance
- ✅ Eliminates manual version updates
- ✅ No database version increments
- ✅ Centralized configuration
- ✅ Rollback capability (edit JSON)
- ✅ Can fix festival data without app update

---

## Annual Update Process

### Before (Current)
```bash
1. Update build.gradle version
2. Update festival.xml (3 places)
3. Update BeerDatabaseHelper DB_VERSION
4. Test, commit, build, release to Play Store
5. Wait for review (1-7 days)
6. Users update app
Total time: 1-2 weeks
```

### After (Dynamic)
```bash
1. Add new entry to festivals.json
2. Upload to data.cambridgebeerfestival.com
3. App auto-fetches and shows new festival
Total time: 5 minutes, instant worldwide
```

**Example festivals.json update:**
```bash
# Just add one object to the array!
{
  "id": "cbf2027",
  "name": "Cambridge Beer Festival 2027",
  "year": 2027,
  ...
}

# Upload
scp festivals.json server:/var/www/data.cambridgebeerfestival.com/
```

---

## Migration Strategy

### Database Migration

```java
@Override
public void onUpgrade(SQLiteDatabase db, ConnectionSource conn,
                      int oldVersion, int newVersion) {
    if (oldVersion < 33) { // Adding festival support
        // 1. Create Festival table
        TableUtils.createTable(conn, Festival.class);

        // 2. Create cbf2025 festival entry for existing data
        Festival cbf2025 = new Festival();
        cbf2025.setId("cbf2025");
        cbf2025.setName("Cambridge Beer Festival 2025");
        cbf2025.setYear(2025);
        cbf2025.setHashtag("cbf2025");
        cbf2025.setBeerListUrl("https://data.cambridgebeerfestival.com/cbf2025/beer.json");
        cbf2025.setActive(true);

        Festivals festivals = getDao(Festival.class);
        festivals.create(cbf2025);

        // 3. Add festival_id column to beers
        db.execSQL("ALTER TABLE beers ADD COLUMN festival_id TEXT");

        // 4. Link all existing beers to cbf2025
        db.execSQL("UPDATE beers SET festival_id = 'cbf2025'");

        // 5. Add foreign key constraint (optional, for data integrity)
        // Note: SQLite doesn't enforce FKs unless enabled
    }
}
```

### Bundled Fallback

Include festivals.json in app as fallback:

```
app/src/main/res/raw/festivals_fallback.json
```

If network fetch fails, use bundled version.

---

## Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Catalog URL unreachable | High | Bundle festivals.json in app as fallback |
| Invalid JSON format | High | JSON schema validation, catch parse errors |
| Database migration failure | Critical | Extensive testing, backup/restore |
| Increased complexity | Medium | Phased rollout, documentation |
| User confusion (multiple festivals) | Low | Clear UI, default to current festival |
| Large database (many festivals) | Low | Cleanup old festivals (>3 years) |

---

## Testing Plan

### Unit Tests
- [ ] Festival model parsing
- [ ] Catalog download and parse
- [ ] Database foreign key relationships
- [ ] Query beers by festival

### Integration Tests
- [ ] End-to-end festival selection
- [ ] Download and display beers
- [ ] Switch between festivals
- [ ] Database migration

### Manual Testing
- [ ] Install old version, add data
- [ ] Upgrade to new version
- [ ] Verify migration successful
- [ ] Download multiple festivals
- [ ] Switch between them

---

## Future Enhancements

Once dynamic festivals are implemented:

- **Multi-event support:** Support non-CBF events
- **Push notifications:** Notify when new festival announced
- **Festival comparison:** Compare same beer across years
- **Statistics:** "You've tried 47 beers across 3 festivals!"
- **Sync across devices:** Cloud-sync favorites via festival+beer ID

---

## Files to Create/Modify

### New Files
- `libraries/beers/src/main/java/ralcock/cbf/model/Festival.java`
- `libraries/beers/src/main/java/ralcock/cbf/model/FestivalCatalog.java`
- `libraries/beers/src/main/java/ralcock/cbf/model/dao/Festivals.java`
- `libraries/beers/src/main/java/ralcock/cbf/model/dao/FestivalsImpl.java`
- `app/src/main/java/ralcock/cbf/service/FestivalCatalogUpdateService.java`
- `app/src/main/java/ralcock/cbf/view/FestivalSelectionFragment.java`
- `app/src/main/res/layout/fragment_festival_selection.xml`
- `app/src/main/res/raw/festivals_fallback.json`

### Modified Files
- `Beer.java` - Add festival foreign key
- `BeerDatabaseHelper.java` - Add Festival table, migration
- `CamBeerFestApplication.java` - Festival selection support
- `UpdateService.java` - Multi-festival awareness
- `festival.xml` - Keep only catalog URL (remove hardcoded year)

---

## Related Documentation

- [Annual Updates](../annual-updates/) - Current manual process
- [Database Schema](../api/data-models.md) - Current structure
- [Troubleshooting Stale Data](../troubleshooting/stale-data.md) - Problem this solves

---

**Status:** Ready for implementation
**Next Steps:** Get stakeholder approval, create implementation branch

---

**Back to:** [Features](README.md) | [Main Documentation](../../CLAUDE.md)
