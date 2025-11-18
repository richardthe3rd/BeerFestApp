# Cambridge Beer Festival Data API Reference

**Last Updated:** 2025-11-18
**API Base URL:** https://data.cambridgebeerfestival.com
**Purpose:** Documentation for multi-festival and multi-beverage-type support

---

## Overview

The Cambridge Beer Festival Data API provides structured beverage and producer data for multiple festivals in both JSON and CSV formats. This API enables the app to support multiple festivals and multiple beverage types beyond just beer.

---

## Available Festivals

### Current & Recent Festivals

| Festival Code | Full Name | Years Available | Notes |
|---------------|-----------|-----------------|-------|
| **cbf** | Cambridge Beer Festival | cbf2023, cbf2024, cbf2025 | Main annual festival |
| **cbfw** | Cambridge Beer Festival Winter | cbfw2018, cbfw2019, cbfw2025 | Winter variant |
| **cof** | Championship of Festivals | cof6-10 | Championship events |
| **waf** | Winter Ale Festival | waf1, waf17-22 | Winter ale focused |

### Historical Data

The API also contains historical data for older festivals:
- CBF: cbf1 through cbf46 (various years)
- Additional historical entries available

---

## URL Structure

### Pattern
```
https://data.cambridgebeerfestival.com/{festival_code}/{beverage_type}.{format}
```

### Examples
```
https://data.cambridgebeerfestival.com/cbf2025/beer.json
https://data.cambridgebeerfestival.com/cbf2025/cider.json
https://data.cambridgebeerfestival.com/cbfw2025/beer.json
```

---

## Available Beverage Types

### CBF 2025 (Main Festival) - Full Range

| Beverage Type | Filename | JSON Size | CSV Size | Description |
|---------------|----------|-----------|----------|-------------|
| **Beer** | `beer.json` / `beer.csv` | 94 KB | 53 KB | Domestic beer offerings |
| **International Beer** | `international-beer.json` / `international-beer.csv` | 52 KB | 22 KB | Foreign/imported beers |
| **Cider** | `cider.json` / `cider.csv` | 23 KB | 10 KB | Apple ciders |
| **Mead** | `mead.json` / `mead.csv` | 3.9 KB | 2.1 KB | Honey wines |
| **Perry** | `perry.json` / `perry.csv` | 8.3 KB | 3.4 KB | Pear ciders |
| **Wine** | `wine.json` / `wine.csv` | 7.5 KB | 4.6 KB | Wines |
| **Apple Juice** | `apple-juice.json` / `apple-juice.csv` | 919 B | 548 B | Non-alcoholic apple juice |
| **Low/No Alcohol** | `low-no.json` / `low-no.csv` | 8.3 KB | 3.0 KB | Low or no-alcohol beverages |

### CBFW 2025 (Winter Festival) - Limited Range

| Beverage Type | Filename | Description |
|---------------|----------|-------------|
| **Beer** | `beer.json` / `beer.csv` | Winter beer offerings |
| **Low/No Alcohol** | `low-no.json` / `low-no.csv` | Low or no-alcohol beverages |

**Note:** Different festivals may offer different beverage types. Always check the festival directory listing before attempting to fetch specific beverage types.

---

## JSON Data Structure

### Overview

All beverage types follow the same consistent JSON structure with a two-level hierarchy:
1. **Producers** (breweries, cideries, meaderies, wineries)
2. **Products** (individual beverages)

### Root Object

```json
{
  "timestamp": "2025-05-24T00:01:00Z",
  "producers": [ /* array of producer objects */ ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | string (ISO 8601) | Last update time for this dataset |
| `producers` | array | Array of producer objects (see below) |

---

## Producer Object

Each producer (brewery, cidery, etc.) contains:

```json
{
  "name": "All Day",
  "location": "Reepham, Norfolk",
  "id": "632047e5b2a712a7707f6b28ac722b1e706f1589",
  "year_founded": 2014,
  "notes": "Reepham, Norfolk est. 2014",
  "products": [ /* array of product objects */ ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Producer name |
| `location` | string | Yes | Geographic location |
| `id` | string | Yes | Unique identifier (SHA-1 hash) |
| `year_founded` | integer | No | Year the producer was established |
| `notes` | string | No | Additional information about the producer |
| `products` | array | Yes | Array of product objects (beverages) |

---

## Product Object

Each product (individual beverage) contains:

```json
{
  "name": "Let's Cask - Strong Golden Ale",
  "id": "3e908babc017695281dc1f9887be46c6ffb9e0a3",
  "category": "beer",
  "style": "Golden Ale",
  "dispense": "cask",
  "abv": "5.4",
  "notes": "Heritage Range - Crisp Heritage malts and a massive whack of fresh Goldings...",
  "status_text": "Plenty left",
  "bar": "Arctic",
  "allergens": {"gluten": 1}
}
```

### Core Fields

| Field | Type | Required | Description | Example Values |
|-------|------|----------|-------------|----------------|
| `name` | string | Yes | Product name | "Let's Cask - Strong Golden Ale" |
| `id` | string | Yes | Unique identifier (SHA-1 hash) | "3e908babc017..." |
| `category` | string | Yes | Beverage category | "beer", "cider", "mead", "foreign beer" |
| `abv` | string | Yes | Alcohol by volume (%) | "5.4", "8.4", "14.5" |
| `dispense` | string | Yes | Serving method | See dispense methods below |
| `style` | string | No | Style/variety | "Golden Ale", "IPA", "Dry" |
| `notes` | string | No | Flavor description | "Crisp Heritage malts..." |
| `status_text` | string | No | Availability status | "Plenty left", "Arrived" |
| `bar` | string or boolean | No | Venue/location | "Arctic", "Main Bar", true/false |
| `allergens` | object | No | Allergen flags | `{"gluten": 1, "sulphites": 1}` |

### Dispense Methods

Common values for the `dispense` field:

| Value | Description | Common For |
|-------|-------------|------------|
| `cask` | Traditional cask ale | Beer |
| `keg` | Standard keg | Beer |
| `keykeg` | KeyKeg (pressurized) | Beer, low-no |
| `bottle` | Bottled | International beer, mead, wine |
| `cider tub` | Cider serving vessel | Cider, perry |
| `mead polypin` | Mead container | Mead |

### Allergens Object

The `allergens` object uses numeric flags (1 = present):

```json
"allergens": {
  "gluten": 1,
  "sulphites": 1
}
```

Common allergens:
- `gluten`
- `sulphites`

**Note:** An empty object `{}` means no allergens listed.

---

## Category-Specific Variations

### Beer vs International Beer

| Feature | beer.json | international-beer.json |
|---------|-----------|-------------------------|
| **Category value** | "beer" | "foreign beer" |
| **Location tracking** | UK-focused | Global (15+ countries) |
| **Status field** | Less common | "Arrived" tracking |
| **Dispense** | Cask-heavy | Bottle/keg-heavy |
| **Use case** | Domestic offerings | Import inventory |

### Cider/Perry

- `style` field often null
- `dispense` typically "cider tub"
- Focus on sweetness/dryness in notes
- Smaller producer counts

### Mead

- `dispense` typically "bottle" or "mead polypin"
- Higher ABV range (10-17%)
- Strong flavor descriptions in notes
- Common allergen: sulphites

### Wine

- Traditional wine categories
- Bottle dispense
- Wine-specific styling

### Low/No Alcohol

- ABV typically < 0.5%
- Various dispense methods
- Mixed beverage types (beer-style, cider-style)

---

## Example API Calls

### Fetch Beer Data for CBF 2025

```bash
curl https://data.cambridgebeerfestival.com/cbf2025/beer.json
```

### Fetch Cider Data for CBF 2025

```bash
curl https://data.cambridgebeerfestival.com/cbf2025/cider.json
```

### Fetch Winter Festival Beer Data

```bash
curl https://data.cambridgebeerfestival.com/cbfw2025/beer.json
```

### Check Festival Directory

```bash
curl https://data.cambridgebeerfestival.com/cbf2025/
```

---

## Festival Discovery & Metadata

### Problem: No Metadata in Data API

The data API at `https://data.cambridgebeerfestival.com` provides **raw beverage data only** - no festival metadata (names, dates, descriptions, etc.).

### Solution: Festival Registry

A separate **festival registry** (e.g., `festivals.json`) hosted on GitHub provides:
- Festival metadata (name, dates, hashtag, website)
- Available beverage types per festival
- Data API URL construction
- Festival status (active, upcoming, past)

**See:** [Festival Registry Specification](festival-registry-spec.md) for complete details.

### Quick Example

**Current approach (static):**
```xml
<!-- app/src/main/res/values/festival.xml -->
<string name="festival_name">Cambridge Beer Festival 2025</string>
<string name="festival_hashtag">cbf2025</string>
<string name="beer_list_url">https://data.cambridgebeerfestival.com/cbf2025/beer.json</string>
```

**Proposed approach (dynamic):**
```java
// Fetch festival registry (production would use separate repo or data API)
// Production: https://raw.githubusercontent.com/cambridgebeerfestival/festival-registry/main/festivals.json
// Or: https://data.cambridgebeerfestival.com/festivals.json
String registryUrl = "https://raw.githubusercontent.com/cambridgebeerfestival/festival-registry/main/festivals.json";
FestivalRegistry registry = fetchRegistry(registryUrl);

// Get active festival
Festival festival = registry.getActiveFestival();

// Build URLs dynamically
for (BeverageType type : festival.data.available_types) {
    String url = festival.data.base_url + "/" + type.url;
    fetchBeverageData(url, type.type);
}
```

**Benefits:**
- No app rebuild needed to switch festivals
- Support multiple festivals in-app
- Support multiple beverage types
- Centralized festival configuration

---

## Implementation Considerations

### For Multi-Festival Support

1. **Festival Configuration**
   - Store festival code (e.g., "cbf2025", "cbfw2025")
   - Store available beverage types per festival
   - Support switching between active festivals

2. **URL Construction**
   ```java
   String url = String.format(
       "https://data.cambridgebeerfestival.com/%s/%s.json",
       festivalCode,
       beverageType
   );
   ```

3. **Festival Discovery**
   - Fetch root directory listing to discover available festivals
   - Parse directory names to extract festival codes
   - Allow user to select from available festivals

### For Multi-Beverage Support

1. **Database Schema Updates**
   - Add `category` field to Beer/Beverage table
   - Support different dispense methods
   - Handle category-specific styling

2. **Data Loading**
   - Fetch multiple beverage types concurrently
   - Merge into single database
   - Preserve category distinctions

3. **UI Updates**
   - Filter by beverage category
   - Display category-specific icons
   - Handle different terminology (brewery vs cidery)

4. **Field Mapping**
   ```java
   // Current app uses:
   Beer.style    → maps to JSON "style"
   Beer.brewery  → maps to Producer "name"
   Beer.abv      → maps to Product "abv"

   // New fields needed:
   Beer.category     → maps to Product "category"
   Beer.dispense     → maps to Product "dispense"
   Beer.statusText   → maps to Product "status_text"
   Beer.bar          → maps to Product "bar"
   ```

### Data Consistency

1. **Required Fields**
   - Always present: name, id, category, abv, dispense
   - Validate these fields before database insertion

2. **Optional Fields**
   - Handle null/missing: style, notes, status_text, bar
   - Default empty strings or null values appropriately

3. **Type Conversions**
   - ABV: String → Float (handle parsing errors)
   - Year founded: Integer (may be null)
   - Allergens: Object → structured data

### Error Handling

1. **HTTP Errors**
   - 404: Festival or beverage type not available
   - Network errors: Retry logic
   - Timeout handling

2. **Data Validation**
   - Check JSON structure before parsing
   - Validate required fields present
   - Handle malformed data gracefully

3. **Festival Availability**
   - Check directory listing before fetching
   - Gracefully handle missing beverage types
   - Provide user feedback on availability

---

## Migration Path

### Phase 1: Single Festival, Multiple Beverages
1. Update data model to support `category` field
2. Fetch and merge multiple beverage types from cbf2025
3. Update UI to filter by category
4. Keep existing festival.xml structure

### Phase 2: Dynamic Festival Loading
1. Add festival selection UI
2. Implement festival discovery (directory parsing)
3. Store festival metadata (name, year, available types)
4. Dynamic URL construction based on selected festival

### Phase 3: Full Multi-Festival Support
1. Support multiple active festivals simultaneously
2. Festival switching without app restart
3. Historical festival browsing
4. Festival comparison features

---

## Data Freshness

**Update Schedule:** Data files are typically updated:
- Before festival start (finalized lineup)
- During festival (status_text changes for "sold out", etc.)
- May be updated multiple times during festival

**Caching Strategy:**
- Check timestamp field for data freshness
- Re-fetch if timestamp changed
- Consider TTL of 1-4 hours during festival
- Longer TTL (24h) outside festival dates

---

## API Limitations

1. **No API Key Required** - Public access
2. **No Rate Limiting Observed** - Be respectful with requests
3. **No Versioning** - API structure may change
4. **No HTTPS Certificate Validation Issues** - Standard HTTPS
5. **No Pagination** - Full datasets in single response
6. **No Filtering** - Must fetch full file and filter client-side
7. **No Real-time Updates** - Static JSON files, not live API

---

## Testing Endpoints

### Verify Festival Exists
```bash
curl -I https://data.cambridgebeerfestival.com/cbf2025/
# Should return 200 OK
```

### Verify Beverage Type Available
```bash
curl -I https://data.cambridgebeerfestival.com/cbf2025/beer.json
# Should return 200 OK with Content-Type: application/json
```

### Get Data Size Before Fetching
```bash
curl -I https://data.cambridgebeerfestival.com/cbf2025/beer.json
# Check Content-Length header
```

---

## Related Documentation

- **[Dynamic Festival Loading Feature](../features/dynamic-festivals.md)** - Proposed implementation
- **[Beverage Type Support](../features/README.md#cider-and-mead-support)** - Multiple drink types
- **[Current Data Model](../../libraries/beers/src/main/java/ralcock/cbf/model/)** - Existing entities
- **[BeerDatabaseHelper](../../app/src/main/java/ralcock/cbf/model/BeerDatabaseHelper.java)** - Current database setup

---

## Appendix: Sample JSON Responses

### Beer (Domestic)

```json
{
  "timestamp": "2025-05-24T00:01:00Z",
  "producers": [
    {
      "name": "All Day",
      "location": "Reepham, Norfolk",
      "id": "632047e5b2a712a7707f6b28ac722b1e706f1589",
      "year_founded": 2014,
      "notes": "Reepham, Norfolk est. 2014",
      "products": [
        {
          "name": "Let's Cask - Strong Golden Ale",
          "style": "Golden Ale",
          "dispense": "cask",
          "abv": "5.4",
          "status_text": "Plenty left",
          "allergens": {"gluten": 1},
          "id": "3e908babc017695281dc1f9887be46c6ffb9e0a3",
          "notes": "Heritage Range - Crisp Heritage malts and a massive whack of fresh Goldings...",
          "category": "beer",
          "bar": "Arctic"
        }
      ]
    }
  ]
}
```

### Cider

```json
{
  "timestamp": "2025-05-24T00:01:00Z",
  "producers": [
    {
      "name": "Ross on Wye Cider & Perry Co.",
      "location": "Ross on Wye, Herefordshire",
      "id": "abc123...",
      "products": [
        {
          "name": "Strong Kentish Cider",
          "abv": "8.4",
          "category": "cider",
          "dispense": "cider tub",
          "style": null,
          "allergens": {},
          "id": "def456...",
          "notes": "Made from Kentish cider apples",
          "bar": "Cider Bar"
        }
      ]
    }
  ]
}
```

### Mead

```json
{
  "timestamp": "2025-05-24T00:01:00Z",
  "producers": [
    {
      "name": "Lindisfarne",
      "location": "Holy Island, Northumberland",
      "id": "ghi789...",
      "year_founded": 1962,
      "products": [
        {
          "name": "Original",
          "id": "jkl012...",
          "category": "mead",
          "abv": "14.5",
          "notes": "Dry/Sweet balance, light mead",
          "dispense": "mead polypin",
          "allergens": {},
          "status_text": "Available",
          "bar": "Mead Bar"
        }
      ]
    }
  ]
}
```

### International Beer

```json
{
  "timestamp": "2025-05-24T00:01:00Z",
  "producers": [
    {
      "name": "Chimay",
      "location": "Chimay, Belgium",
      "id": "mno345...",
      "year_founded": 1862,
      "notes": "Trappist brewery",
      "products": [
        {
          "name": "Première (Red)",
          "id": "pqr678...",
          "category": "foreign beer",
          "abv": "7.0",
          "dispense": "keg",
          "style": "Dubbel",
          "notes": "Dark red, fruity, slightly bitter",
          "status_text": "Arrived",
          "allergens": {"gluten": 1},
          "bar": "International Bar"
        }
      ]
    }
  ]
}
```

---

## Document Change History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-18 | Initial documentation based on API exploration |

---

## Questions or Issues?

- **GitHub Issues:** https://github.com/richardthe3rd/BeerFestApp/issues
- **API Issues:** Contact Cambridge Beer Festival directly
- **This Documentation:** Submit PR to update this file