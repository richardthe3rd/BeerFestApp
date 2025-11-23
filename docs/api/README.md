# API Documentation

This directory contains documentation for the Cambridge Beer Festival data APIs and festival registry.

## Files

### [data-api-reference.md](data-api-reference.md)
Complete documentation of the **beverage data API** at `https://data.cambridgebeerfestival.com`:
- Available festivals (cbf, cbfw, waf, cof)
- 8 beverage types (beer, cider, mead, perry, wine, etc.)
- JSON data structure (producers → products)
- Field definitions and examples

### [festival-registry-spec.md](festival-registry-spec.md)
Specification for the **festival metadata registry** (`festivals.json`):
- Registry format and schema
- Festival metadata (name, dates, location, hashtag)
- Location data (GPS coordinates, What3Words)
- Multiple beverage type support
- Production hosting recommendations

### [festivals.json](festivals.json)
**Example festival registry** for documentation and testing purposes.

**⚠️ Important:** This is a **reference example** only. In production, the festival registry should be hosted in a **separate repository** or on the data API server for proper separation of concerns.

**Production locations (future):**
- Separate repo: `https://raw.githubusercontent.com/cambridgebeerfestival/festival-registry/main/festivals.json`
- Data API: `https://data.cambridgebeerfestival.com/festivals.json`

### [example-beer-list.json](example-beer-list.json)
**Comprehensive example** of beverage data API response demonstrating all field types and variations.

**Includes examples of:**
- Demonstrated beverage categories (beer, cider, perry, mead, foreign beer)
- All dispense methods (cask, keg, keykeg, bottle, cider tub, mead polypin)
- Different status text values (Sold Out, Plenty left, Nearly finished!, etc.)
- Optional vs required fields
- Null and empty field handling
- Allergen information formats

**Use this for:**
- Understanding API response structure
- Testing data parsers
- Development and debugging
- Reference when building integrations

### [beer-list-schema.json](beer-list-schema.json)
**JSON Schema (draft-07)** defining structure and validation rules for beverage data API responses.

**Enables:**
- Automated validation of API responses
- IDE autocomplete and IntelliSense
- Documentation generation
- Testing and CI/CD integration

**Validation example:**
```bash
# Python
pip install jsonschema
python3 -c "import json, jsonschema; \
  schema = json.load(open('docs/api/beer-list-schema.json')); \
  data = json.load(open('docs/api/example-beer-list.json')); \
  jsonschema.validate(data, schema); print('Valid!')"

# Node.js
npm install -g ajv-cli
ajv validate -s docs/api/beer-list-schema.json -d docs/api/example-beer-list.json
```

---

## Quick Start

### Understanding the Data Architecture

```
┌─────────────────────────────────────┐
│  Festival Registry (festivals.json) │  ← Metadata: which festivals, what drinks available
│  - Festival info (name, dates)      │  ← Hosted separately (own repo or data.cbf.com)
│  - Location (GPS, What3Words)       │
│  - Available beverage types         │
└─────────────────────────────────────┘
                 ↓ (provides URLs)
┌─────────────────────────────────────┐
│  Beverage Data API                  │  ← Actual drink data: producers + products
│  data.cambridgebeerfestival.com     │  ← Already exists, no changes needed
│  - beer.json, cider.json, etc.      │
└─────────────────────────────────────┘
```

### For App Developers

1. **Fetch festival registry** to discover available festivals and their data URLs
2. **Parse festival metadata** (name, dates, location) for UI display
3. **Fetch beverage data** for the selected festival using URLs from registry
4. **Display drinks** grouped by category (beer, cider, mead, etc.)

### For Festival Organizers

1. **Update festivals.json** when new festival announced
2. **Set status** to "upcoming" → "active" → "past" as festival progresses
3. **Add location data** (GPS coordinates for Maps integration)
4. **List available beverages** (beer, cider, mead, etc.) for the festival

---

## Example: Adding CBF 2026

```json
{
  "id": "cbf2026",
  "name": "Cambridge Beer Festival 2026",
  "short_name": "CBF 2026",
  "type": "cbf",
  "year": 2026,
  "status": "upcoming",
  "dates": {
    "start": "2026-05-18",
    "end": "2026-05-23"
  },
  "location": {
    "venue": "Jesus Green",
    "city": "Cambridge",
    "country": "UK",
    "coordinates": {
      "lat": 52.215297,
      "lon": 0.126847
    },
    "what3words": "risks.purple.shares"
  },
  "metadata": {
    "hashtag": "#cbf2026",
    "description": "The 49th Cambridge Beer Festival",
    "website": "https://www.cambridgebeerfestival.com/"
  },
  "data": {
    "base_url": "https://data.cambridgebeerfestival.com/cbf2026",
    "available_types": [
      {"type": "beer", "label": "Beer", "url": "beer.json"},
      {"type": "cider", "label": "Cider", "url": "cider.json"},
      {"type": "mead", "label": "Mead", "url": "mead.json"}
    ]
  }
}
```

---

## Benefits of This Architecture

### For App
- **No app rebuild** to switch festivals
- **Multi-festival support** - browse past/future festivals
- **Multi-beverage support** - beer, cider, mead, etc.
- **Maps integration** - one-tap navigation to venue

### For Festival Organizers
- **Independent updates** - change festival info without app release
- **Version controlled** - git history of all changes
- **Easy to maintain** - simple JSON file, no coding required
- **Multiple apps** - same registry can serve mobile, web, etc.

---

## Related Documentation

- **[Main Documentation](../../CLAUDE.md)** - App overview and setup
- **[Dynamic Festivals Feature](../features/dynamic-festivals.md)** - Implementation plan
- **[Current Data Model](../../libraries/beers/src/main/java/ralcock/cbf/model/)** - Existing entities

---

## Questions?

- **App issues:** [GitHub Issues](https://github.com/richardthe3rd/BeerFestApp/issues)
- **API issues:** Contact Cambridge Beer Festival
- **Update docs:** Submit PR to this repository