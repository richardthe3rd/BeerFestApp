# Festival Registry Specification

**Last Updated:** 2025-11-18
**Purpose:** Define the structure for a festival metadata registry to enable dynamic festival discovery
**Status:** Proposed Design

---

## Overview

The Cambridge Beer Festival Data API provides raw beverage data but **no festival metadata** (names, dates, descriptions, etc.). This specification defines a festival registry format that the app can use to:

1. Discover available festivals
2. Get festival metadata (name, dates, hashtag)
3. Determine which beverage types are available per festival
4. Construct data API URLs dynamically

---

## Registry Location

**Production hosting options:**

### Option 1: Separate GitHub Repository (Recommended for Production)
```
https://raw.githubusercontent.com/cambridgebeerfestival/festival-registry/main/festivals.json
```

**Advantages:**
- **Separation of concerns** - Festival data separate from app code
- **Independent updates** - Update registry without touching app repo
- **Multiple consumers** - Other apps/services can use same registry
- **Different access control** - Festival organizers can maintain data without app repo access
- **Own CI/CD** - Validate, test, and deploy registry changes independently
- **Version controlled** - Full git history of festival changes
- Free hosting, HTTPS by default

**Recommended structure:**
```
cambridgebeerfestival/festival-registry/
├── festivals.json           # Current production registry
├── festivals-v2.json        # Future schema version
├── schemas/
│   └── festival-schema.json # JSON schema for validation
├── .github/workflows/
│   └── validate.yml         # CI to validate on PR
└── README.md
```

### Option 2: Data API Directory
```
https://data.cambridgebeerfestival.com/festivals.json
```

**Advantages:**
- Same domain as beverage data
- Single source of truth
- No external dependencies
- Controlled by festival organization

### Option 3: GitHub Pages (Custom Domain)
```
https://api.cambridgebeerfestival.com/festivals.json
```

**Advantages:**
- Custom domain
- Fast CDN delivery
- Can serve multiple API endpoints

### Option 4: This App Repository (For Testing/Development Only)
```
https://raw.githubusercontent.com/richardthe3rd/BeerFestApp/main/docs/api/festivals.json
```

**Use case:** Example/reference documentation only, not for production

**Note:** Recommended production approach is **Option 1** (separate repository) or **Option 2** (data API directory). The file in this repo's `docs/api/` is a **reference example** for documentation purposes.

---

## Registry Format

### Complete Example

```json
{
  "version": "1.0",
  "last_updated": "2025-11-18T12:00:00Z",
  "festivals": [
    {
      "id": "cbf2025",
      "name": "Cambridge Beer Festival 2025",
      "short_name": "CBF 2025",
      "type": "cbf",
      "year": 2025,
      "status": "active",
      "dates": {
        "start": "2025-05-19",
        "end": "2025-05-24"
      },
      "location": {
        "venue": "Jesus Green",
        "city": "Cambridge",
        "country": "UK",
        "address": "Jesus Green, Cambridge CB4 3BT",
        "postcode": "CB4 3BT",
        "coordinates": {
          "lat": 52.215297,
          "lon": 0.126847
        },
        "what3words": "risks.purple.shares"
      },
      "metadata": {
        "hashtag": "#cbf2025",
        "description": "The 48th Cambridge Beer Festival featuring over 200 beers, ciders, and more",
        "website": "https://www.cambridgebeerfestival.com",
        "theme": "Celebrating British Brewing Heritage"
      },
      "data": {
        "base_url": "https://data.cambridgebeerfestival.com/cbf2025",
        "available_types": [
          {
            "type": "beer",
            "label": "Beer",
            "url": "beer.json",
            "count": 200,
            "icon": "beer"
          },
          {
            "type": "international-beer",
            "label": "International Beer",
            "url": "international-beer.json",
            "count": 90,
            "icon": "beer_world"
          },
          {
            "type": "cider",
            "label": "Cider",
            "url": "cider.json",
            "count": 50,
            "icon": "cider"
          },
          {
            "type": "mead",
            "label": "Mead",
            "url": "mead.json",
            "count": 15,
            "icon": "mead"
          },
          {
            "type": "perry",
            "label": "Perry",
            "url": "perry.json",
            "count": 20,
            "icon": "perry"
          },
          {
            "type": "wine",
            "label": "Wine",
            "url": "wine.json",
            "count": 25,
            "icon": "wine"
          },
          {
            "type": "apple-juice",
            "label": "Apple Juice",
            "url": "apple-juice.json",
            "count": 5,
            "icon": "apple"
          },
          {
            "type": "low-no",
            "label": "Low/No Alcohol",
            "url": "low-no.json",
            "count": 30,
            "icon": "no_alcohol"
          }
        ]
      }
    },
    {
      "id": "cbfw2025",
      "name": "Cambridge Beer Festival Winter 2025",
      "short_name": "CBFW 2025",
      "type": "cbfw",
      "year": 2025,
      "status": "upcoming",
      "dates": {
        "start": "2025-01-27",
        "end": "2025-02-01"
      },
      "location": {
        "venue": "The Great Hall",
        "city": "Cambridge",
        "country": "UK",
        "coordinates": {
          "lat": 52.204100,
          "lon": 0.119050
        }
      },
      "metadata": {
        "hashtag": "#cbfw2025",
        "description": "Winter edition featuring warming ales and winter specialties",
        "website": "https://www.cambridgebeerfestival.com/winter",
        "theme": "Winter Warmers"
      },
      "data": {
        "base_url": "https://data.cambridgebeerfestival.com/cbfw2025",
        "available_types": [
          {
            "type": "beer",
            "label": "Beer",
            "url": "beer.json",
            "count": 80,
            "icon": "beer"
          },
          {
            "type": "low-no",
            "label": "Low/No Alcohol",
            "url": "low-no.json",
            "count": 10,
            "icon": "no_alcohol"
          }
        ]
      }
    },
    {
      "id": "cbf2024",
      "name": "Cambridge Beer Festival 2024",
      "short_name": "CBF 2024",
      "type": "cbf",
      "year": 2024,
      "status": "past",
      "dates": {
        "start": "2024-05-20",
        "end": "2024-05-25"
      },
      "location": {
        "venue": "Jesus Green",
        "city": "Cambridge",
        "country": "UK"
      },
      "metadata": {
        "hashtag": "#cbf2024",
        "description": "The 47th Cambridge Beer Festival",
        "website": "https://www.cambridgebeerfestival.com"
      },
      "data": {
        "base_url": "https://data.cambridgebeerfestival.com/cbf2024",
        "available_types": [
          {
            "type": "beer",
            "label": "Beer",
            "url": "beer.json",
            "icon": "beer"
          },
          {
            "type": "cider",
            "label": "Cider",
            "url": "cider.json",
            "icon": "cider"
          }
        ]
      }
    }
  ]
}
```

---

## Field Definitions

### Root Object

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `version` | string | Yes | Registry format version (semver) |
| `last_updated` | string (ISO 8601) | Yes | When registry was last updated |
| `festivals` | array | Yes | Array of festival objects |

---

### Festival Object

#### Core Identity

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `id` | string | Yes | Unique festival identifier (matches data API path) | "cbf2025", "cbfw2025" |
| `name` | string | Yes | Full festival name | "Cambridge Beer Festival 2025" |
| `short_name` | string | Yes | Abbreviated name for UI | "CBF 2025" |
| `type` | string | Yes | Festival series code | "cbf", "cbfw", "waf", "cof" |
| `year` | integer | Yes | Festival year | 2025 |
| `status` | string | Yes | Festival status | "upcoming", "active", "past", "cancelled" |

#### Dates Object

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `start` | string (ISO 8601 date) | Yes | Festival start date | "2025-05-19" |
| `end` | string (ISO 8601 date) | Yes | Festival end date | "2025-05-24" |

#### Location Object

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `venue` | string | Yes | Venue name | "Jesus Green" |
| `city` | string | Yes | City | "Cambridge" |
| `country` | string | No | Country code or name | "UK" |
| `address` | string | No | Full postal address | "Jesus Green, Cambridge CB4 3BT" |
| `postcode` | string | No | Postal/ZIP code | "CB4 3BT" |
| `coordinates` | object | **Recommended** | GPS coordinates (required for Maps links) | `{"lat": 52.215297, "lon": 0.126847}` |
| `what3words` | string | No | What3Words address (user-friendly location) | "risks.purple.shares" |

**Coordinates object:**
```json
"coordinates": {
  "lat": 52.215297,   // Latitude (decimal degrees, -90 to 90)
  "lon": 0.126847     // Longitude (decimal degrees, -180 to 180)
}
```

**Important Notes:**
- **Coordinates are strongly recommended** for enabling Maps integration
- Use at least 5-6 decimal places for ~1 meter accuracy
- What3Words addresses are optional but provide user-friendly location sharing
- What3Words free API only supports validation, not coordinate conversion (paid feature)

#### Metadata Object

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `hashtag` | string | Yes | Social media hashtag | "#cbf2025" |
| `description` | string | Yes | Festival description | "The 48th Cambridge Beer Festival..." |
| `website` | string | No | Festival website URL | "https://www.cambridgebeerfestival.com" |
| `theme` | string | No | Festival theme | "Celebrating British Brewing Heritage" |
| `logo_url` | string | No | Logo image URL | "https://example.com/logo.png" |
| `banner_url` | string | No | Banner image URL | "https://example.com/banner.png" |

#### Data Object

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `base_url` | string | Yes | Base URL for data API | "https://data.cambridgebeerfestival.com/cbf2025" |
| `available_types` | array | Yes | Array of beverage type objects | See below |

#### Beverage Type Object

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `type` | string | Yes | Beverage type ID | "beer", "cider", "mead" |
| `label` | string | Yes | Display label | "Beer", "Cider" |
| `url` | string | Yes | Filename at base_url | "beer.json" |
| `count` | integer | No | Estimated count of products | 200 |
| `icon` | string | No | Icon identifier for UI | "beer", "cider" |
| `description` | string | No | Type description | "Traditional cask ales and craft beers" |

---

## Status Values

| Status | Meaning | App Behavior |
|--------|---------|--------------|
| `upcoming` | Festival not yet started | Show in list, allow browsing previous year's data |
| `active` | Festival currently running | Default selection, show "Live" badge |
| `past` | Festival completed | Archive mode, historical browsing |
| `cancelled` | Festival cancelled | Show but disable, display notice |

---

## Festival Type Codes

| Code | Full Name | Typical Schedule |
|------|-----------|------------------|
| `cbf` | Cambridge Beer Festival | May (annual) |
| `cbfw` | Cambridge Beer Festival Winter | January/February |
| `waf` | Winter Ale Festival | January/February |
| `cof` | Championship of Festivals | Varies |

---

## App Implementation Flow

### 1. Festival Discovery

```java
// App startup or refresh
// PRODUCTION: Use separate registry repository or API endpoint
String registryUrl = "https://raw.githubusercontent.com/cambridgebeerfestival/festival-registry/main/festivals.json";
// OR: String registryUrl = "https://data.cambridgebeerfestival.com/festivals.json";

FestivalRegistry registry = fetchRegistry(registryUrl);
```

### 2. Default Festival Selection

```java
// Select the "active" festival, or most recent "upcoming"
Festival defaultFestival = registry.getActiveFestival();
if (defaultFestival == null) {
    defaultFestival = registry.getMostRecentFestival();
}
```

### 3. Build Data URLs

```java
// For each beverage type
for (BeverageType type : festival.data.available_types) {
    String url = festival.data.base_url + "/" + type.url;
    // https://data.cambridgebeerfestival.com/cbf2025/beer.json
    fetchBeverageData(url, type.type);
}
```

### 4. Display Festival Info

```java
// Show in UI
textView.setText(festival.name);
hashtagView.setText(festival.metadata.hashtag);
dateView.setText(formatDates(festival.dates.start, festival.dates.end));
```

### 4a. Open Location in Maps

```java
// Build Maps URL from coordinates
if (festival.location.coordinates != null) {
    double lat = festival.location.coordinates.lat;
    double lon = festival.location.coordinates.lon;

    // Universal approach - let Android choose default Maps app
    Uri geoUri = Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
        lat, lon, lat, lon, Uri.encode(festival.location.venue)));
    Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
    startActivity(mapIntent);

    // Or use web URLs (works on all platforms):
    // Google Maps: https://www.google.com/maps/?q=52.215297,0.126847
    // Apple Maps: https://maps.apple.com/?q=52.215297,0.126847
}
```

**Maps URL Formats:**

| Platform | URL Format | Example |
|----------|------------|---------|
| **Android (geo URI)** | `geo:{lat},{lon}?q={lat},{lon}({label})` | `geo:52.215297,0.126847?q=52.215297,0.126847(Jesus%20Green)` |
| **Google Maps (web)** | `https://www.google.com/maps/?q={lat},{lon}` | `https://www.google.com/maps/?q=52.215297,0.126847` |
| **Apple Maps (web)** | `https://maps.apple.com/?q={lat},{lon}` | `https://maps.apple.com/?q=52.215297,0.126847` |
| **Directions (Google)** | `https://maps.google.com/?daddr={lat},{lon}` | `https://maps.google.com/?daddr=52.215297,0.126847` |
| **Directions (Apple)** | `https://maps.apple.com/?daddr={lat},{lon}` | `https://maps.apple.com/?daddr=52.215297,0.126847` |

**What3Words Integration:**
```java
// Display What3Words address if available (user-friendly)
if (festival.location.what3words != null) {
    // Format: ///word.word.word
    String w3wAddress = "///" + festival.location.what3words;
    what3wordsView.setText(w3wAddress);

    // Open in What3Words app (if installed)
    Uri w3wUri = Uri.parse("https://w3w.co/" + festival.location.what3words);
    Intent w3wIntent = new Intent(Intent.ACTION_VIEW, w3wUri);
    startActivity(w3wIntent);
}
```

### 5. Festival Switching

```java
// User selects different festival
Festival newFestival = registry.getFestivalById("cbf2024");
clearDatabase();
loadFestivalData(newFestival);
updateUI(newFestival);
```

---

## Registry Update Process

### Workflow

1. **New festival announced**
   - Add new festival entry to festivals.json
   - Set status to "upcoming"
   - Add estimated dates and beverage types

2. **Data becomes available**
   - Update `base_url` and `available_types`
   - Add accurate `count` values
   - Verify all URLs return valid data

3. **Festival goes live**
   - Change status from "upcoming" to "active"
   - Update any last-minute changes

4. **Festival ends**
   - Change status from "active" to "past"
   - Archive the data

### Git Workflow (Production Registry)

```bash
# Clone production registry repository
git clone https://github.com/cambridgebeerfestival/festival-registry.git
cd festival-registry

# Edit registry
vim festivals.json

# Validate (CI will also validate)
./scripts/validate.sh festivals.json

# Commit and push
git add festivals.json
git commit -m "Add CBF 2026 festival metadata"
git push origin main

# App will fetch updated registry on next refresh (within cache TTL)
```

**Alternative: Direct Edit on GitHub**
1. Go to https://github.com/cambridgebeerfestival/festival-registry
2. Click "festivals.json"
3. Click edit (pencil icon)
4. Make changes
5. Commit directly to main (or create PR for review)
6. Apps refresh within 24 hours (or on app restart)

---

## Validation Rules

### Required Field Validation

```java
// Validate each festival entry
if (festival.id == null || festival.id.isEmpty()) {
    throw new ValidationException("Festival ID required");
}
if (festival.data == null || festival.data.available_types.isEmpty()) {
    throw new ValidationException("At least one beverage type required");
}
```

### URL Validation

```java
// Verify data URLs are accessible
for (BeverageType type : festival.data.available_types) {
    String url = festival.data.base_url + "/" + type.url;
    if (!isUrlAccessible(url)) {
        Log.w("FestivalRegistry", "URL not accessible: " + url);
    }
}
```

### Date Validation

```java
// Ensure end date is after start date
if (festival.dates.end.isBefore(festival.dates.start)) {
    throw new ValidationException("End date must be after start date");
}
```

---

## Mapping from Current festival.xml

### Current Static Configuration (festival.xml)

```xml
<!-- app/src/main/res/values/festival.xml -->
<resources>
    <string name="app_name">Cambridge Beer Festival</string>
    <string name="festival_name">Cambridge Beer Festival 2025</string>
    <string name="festival_hashtag">cbf2025</string>
    <string name="festival_website_url">https://www.cambridgebeerfestival.com/</string>
    <string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>
</resources>
```

### Mapping to festivals.json

| festival.xml | festivals.json | Notes |
|--------------|----------------|-------|
| `festival_name` | `festival.name` | Full festival name |
| `festival_hashtag` | `festival.metadata.hashtag` | Social media hashtag (add # if missing) |
| `festival_website_url` | `festival.metadata.website` | Festival website URL |
| `beer_list_url` | `festival.data.base_url` + `available_types[].url` | Split into base URL + beverage types |
| _(none)_ | `festival.id` | **New:** Unique ID (e.g., "cbf2025") |
| _(none)_ | `festival.dates` | **New:** Start/end dates |
| _(none)_ | `festival.data.available_types` | **New:** Support multiple beverage types |
| _(none)_ | `festival.status` | **New:** "active", "upcoming", "past" |

### Example Conversion

**Current festival.xml:**
```xml
<string name="festival_name">Cambridge Beer Festival 2025</string>
<string name="festival_hashtag">cbf2025</string>
<string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>
```

**Equivalent festivals.json entry:**
```json
{
  "id": "cbf2025",
  "name": "Cambridge Beer Festival 2025",
  "short_name": "CBF 2025",
  "type": "cbf",
  "year": 2025,
  "status": "active",
  "metadata": {
    "hashtag": "#cbf2025",
    "website": "https://www.cambridgebeerfestival.com/"
  },
  "data": {
    "base_url": "https://data.cambridgebeerfestival.com/cbf2025",
    "available_types": [
      {"type": "beer", "label": "Beer", "url": "beer.json"},
      {"type": "cider", "label": "Cider", "url": "cider.json"},
      {"type": "mead", "label": "Mead", "url": "mead.json"}
    ]
  }
}
```

---

## Migration Strategy

### Phase 1: Static Configuration (Current)
**Status:** Current implementation
- Festival metadata hard-coded in `festival.xml`
- Single festival support only
- Requires app rebuild to change festivals
- Only beer data loaded

### Phase 2: Single Dynamic Festival
**Status:** Proposed next step
- Fetch `festivals.json` from GitHub
- Use active festival to replace `festival.xml` values
- Still single festival at a time
- Support multiple beverage types
- **No app rebuild needed** to switch festivals

```java
// Fetch registry, use active festival
Festival festival = registry.getActiveFestival();
updateAppConfig(festival);
loadMultipleBeverageTypes(festival);
```

### Phase 3: Multi-Festival Support
**Status:** Future enhancement
- User can browse and select from festival list
- Switch between festivals in-app
- Compare festivals side-by-side
- Historical festival browsing

```java
// User can select from list
List<Festival> festivals = registry.getAllFestivals();
showFestivalPicker(festivals);
```

---

## Error Handling

### Registry Fetch Failure

```java
try {
    FestivalRegistry registry = fetchRegistry(registryUrl);
} catch (IOException e) {
    // Fall back to embedded default
    return getEmbeddedDefaultFestival();
}
```

### Invalid Registry Format

```java
try {
    FestivalRegistry registry = parseRegistry(json);
} catch (JsonException e) {
    Log.e("Registry", "Invalid format, using cache", e);
    return getCachedRegistry();
}
```

### Missing Festival Data

```java
Festival festival = registry.getFestivalById("cbf2025");
if (festival == null || festival.data.available_types.isEmpty()) {
    showError("Festival data not available");
    return;
}
```

---

## Caching Strategy

### Registry Caching

```java
// Cache registry for 24 hours
SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
String cachedRegistry = prefs.getString("festival_registry", null);
long cacheTime = prefs.getLong("registry_cache_time", 0);

if (System.currentTimeMillis() - cacheTime > 24 * 60 * 60 * 1000) {
    // Cache expired, re-fetch
    registry = fetchRegistry(registryUrl);
    prefs.edit()
        .putString("festival_registry", registry.toJson())
        .putLong("registry_cache_time", System.currentTimeMillis())
        .apply();
}
```

### Festival Data Caching

```java
// Cache beverage data per festival
String cacheKey = festival.id + "_" + beverageType;
// Only re-fetch if festival is "active" and cache > 4 hours old
```

---

## Security Considerations

### HTTPS Required

All URLs must use HTTPS to prevent man-in-the-middle attacks.

```java
if (!url.startsWith("https://")) {
    throw new SecurityException("Only HTTPS URLs allowed");
}
```

### Content Validation

Validate JSON structure before parsing to prevent injection attacks.

```java
// Validate against JSON schema
if (!isValidRegistryJson(json)) {
    throw new ValidationException("Invalid registry format");
}
```

### URL Whitelisting

Only allow data from trusted domains.

```java
List<String> allowedDomains = Arrays.asList(
    "data.cambridgebeerfestival.com",
    "raw.githubusercontent.com",
    "richardthe3rd.github.io"
);
if (!isAllowedDomain(url, allowedDomains)) {
    throw new SecurityException("Untrusted data source");
}
```

### Coordinate Validation

Validate GPS coordinates before use.

```java
// Validate coordinates are in valid ranges
if (lat < -90 || lat > 90) {
    throw new ValidationException("Invalid latitude: " + lat);
}
if (lon < -180 || lon > 180) {
    throw new ValidationException("Invalid longitude: " + lon);
}

// Validate coordinates are not 0,0 (common error/placeholder)
if (lat == 0.0 && lon == 0.0) {
    Log.w("Location", "Suspicious coordinates (0,0) - may be placeholder");
}
```

---

## Location & Maps Integration

### GPS Coordinates

**Why include coordinates?**
- Enables one-tap navigation to festival venue
- Works with Google Maps, Apple Maps, and any mapping app
- Provides precise location (address search can be ambiguous)
- Required for "Get Directions" functionality

**Coordinate Accuracy:**
```
Decimal Places | Accuracy
---------------|----------
1              | ~11 km
2              | ~1.1 km
3              | ~110 m
4              | ~11 m
5              | ~1.1 m   ← Minimum recommended
6              | ~0.11 m  ← Best for venues
```

**Example for Jesus Green, Cambridge:**
```json
"coordinates": {
  "lat": 52.215297,
  "lon": 0.126847
}
```

### What3Words Integration

**What is What3Words?**
- Divides the world into 3m x 3m squares
- Each square has a unique 3-word address
- More memorable than GPS coordinates
- Useful for user communication and sharing

**Example:** `///risks.purple.shares` = Jesus Green, Cambridge

**API Limitations:**
- **Free tier:** Only supports AutoSuggest (validation)
- **Paid tier required** for coordinate conversion
- **Recommendation:** Include in registry for display, but don't rely on it for navigation

**When to use:**
```json
"what3words": "risks.purple.shares"  // Optional, user-friendly
"coordinates": {                      // Required for navigation
  "lat": 52.215297,
  "lon": 0.126847
}
```

### Building Maps Links

**Android Intent (Recommended):**
```java
// Let user choose their preferred Maps app
String label = Uri.encode(venue);
Uri uri = Uri.parse(String.format(Locale.US,
    "geo:%f,%f?q=%f,%f(%s)", lat, lon, lat, lon, label));
Intent intent = new Intent(Intent.ACTION_VIEW, uri);
startActivity(intent);
```

**Web URLs (Universal):**
```java
// Google Maps
String googleUrl = String.format(Locale.US,
    "https://www.google.com/maps/?q=%f,%f", lat, lon);

// Apple Maps
String appleUrl = String.format(Locale.US,
    "https://maps.apple.com/?q=%f,%f", lat, lon);

// With venue name in query
String googleUrl = String.format(Locale.US,
    "https://www.google.com/maps/search/?api=1&query=%f,%f",
    lat, lon);
```

**Navigation URLs:**
```java
// Start navigation to venue
String navUrl = String.format(Locale.US,
    "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
    lat, lon);

// Apple Maps navigation
String appleNav = String.format(Locale.US,
    "https://maps.apple.com/?daddr=%f,%f&dirflg=d",
    lat, lon);
```

### How to Find Coordinates

**Google Maps:**
1. Go to https://maps.google.com
2. Right-click on location
3. Click coordinates to copy (e.g., "52.215297, 0.126847")

**Apple Maps:**
1. Open Maps app
2. Drop a pin on location
3. Swipe up, coordinates shown in details

**What3Words:**
1. Go to https://what3words.com
2. Search for location
3. Note the 3-word address (e.g., "risks.purple.shares")

---

## Example: Minimal Registry

For initial implementation, a minimal registry could be:

```json
{
  "version": "1.0",
  "last_updated": "2025-11-18T12:00:00Z",
  "festivals": [
    {
      "id": "cbf2025",
      "name": "Cambridge Beer Festival 2025",
      "short_name": "CBF 2025",
      "type": "cbf",
      "year": 2025,
      "status": "active",
      "dates": {
        "start": "2025-05-19",
        "end": "2025-05-24"
      },
      "location": {
        "venue": "Jesus Green",
        "city": "Cambridge"
      },
      "metadata": {
        "hashtag": "#cbf2025",
        "description": "The 48th Cambridge Beer Festival"
      },
      "data": {
        "base_url": "https://data.cambridgebeerfestival.com/cbf2025",
        "available_types": [
          {"type": "beer", "label": "Beer", "url": "beer.json"},
          {"type": "cider", "label": "Cider", "url": "cider.json"},
          {"type": "mead", "label": "Mead", "url": "mead.json"}
        ]
      }
    }
  ]
}
```

---

## Related Documentation

- **[Data API Reference](data-api-reference.md)** - Beverage data structure
- **[Dynamic Festivals Feature](../features/dynamic-festivals.md)** - Implementation plan
- **[Current Festival Config](../../app/src/main/res/values/festival.xml)** - Current static config

---

## Questions & Next Steps

### Open Questions

1. **Registry update frequency:** How often should app check for updates?
2. **Version compatibility:** How to handle schema changes?
3. **Offline support:** What if registry unavailable?
4. **Multi-language:** Support for internationalization?

### Implementation Tasks

- [x] Create example festivals.json in app repo for documentation (docs/api/festivals.json)
- [ ] Create production festival-registry repository (separate repo)
- [ ] Set up CI/CD validation for production registry
- [ ] Implement registry fetcher in Java
- [ ] Add festival data model classes
- [ ] Create festival picker UI
- [ ] Implement caching strategy
- [ ] Add unit tests for registry parsing
- [ ] Update documentation with actual URLs

---

## Document Information

**Version:** 1.0.0
**Status:** Proposed Specification
**Last Updated:** 2025-11-18
**Authors:** AI Assistant
**Review Status:** Awaiting review

---

**Note:** This is a proposed specification. The actual implementation may vary based on requirements and constraints discovered during development.
