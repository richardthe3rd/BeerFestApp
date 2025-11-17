# Feature Proposals

This section documents proposed features and improvements for BeerFestApp.

---

## High Priority

### [Dynamic Festival Loading](dynamic-festivals.md) ⭐⭐⭐⭐⭐

**Problem:** Annual app releases are error-prone and time-consuming.

**Solution:** Load festival metadata from remote JSON, eliminating the need for app releases.

**Status:** Proposed
**Effort:** 2-3 weeks
**ROI:** Saves 1 week/year forever

[Read full proposal →](dynamic-festivals.md)

---

## Medium Priority

### Cider and Mead Support

**Problem:** Festival includes cider and mead, but app only shows beers.

**User feedback:**
- "Where are the ciders?"
- "Can't find mead section"

**Solution:**
- Add beverage type field to data model
- Filter/group by type (Beer, Cider, Mead)
- Navigation tabs for each type

**Status:** Documented in [Known Issues](../../CLAUDE.md#4-beverage-type-limitation)
**Effort:** 1-2 weeks

### UI Modernization

**Problem:** App uses outdated design patterns (pre-2020).

**Solution:**
- Material Design 3 upgrade
- Dark mode support
- Modern architecture (ViewModel, LiveData)
- Better visual hierarchy

**Status:** Fully documented in [CLAUDE.md](../../CLAUDE.md#uiux-modernization)
**Effort:** 6-9 weeks (3 phases)

### Testing Improvements

**Problem:** Insufficient test coverage leads to production bugs.

**Issues:**
- Only 1 instrumented test
- No UI automation
- Crashes not caught before release

**Solution:**
- Add comprehensive Espresso tests
- Integration tests for data updates
- Crash reporting (Firebase/Sentry)

**Effort:** 2-3 weeks

---

## Low Priority

### Multi-Language Support

**Note:** Low priority since festival is UK-based.

**Languages to consider:**
- French
- German
- Spanish

**Effort:** 1 week

### Search Suggestions

**Enhancement:** Add autocomplete to search.

**Benefits:**
- Better discoverability
- Popular searches
- Faster user experience

**Effort:** 3-5 days

### Push Notifications

**Use cases:**
- New festival announced
- Beer list updated
- Festival starting soon

**Effort:** 1 week

---

## Completed Features

_(None yet - this is for tracking completed proposals)_

---

## Feature Request Process

### Proposing a New Feature

1. **Check existing proposals** in this directory
2. **Create a new markdown file** with:
   - Problem statement
   - Proposed solution
   - Benefits and ROI
   - Implementation plan
   - Effort estimate
3. **Add to this README** with priority
4. **Discuss with maintainers**

### Feature Template

```markdown
# Feature Name

**Problem:** What problem does this solve?

**Proposed Solution:** How would you solve it?

**Benefits:**
- User benefit 1
- User benefit 2
- Developer benefit

**Implementation Plan:**
1. Step 1
2. Step 2
3. Step 3

**Effort Estimate:** X weeks

**Risks:** What could go wrong?

**Alternatives Considered:** Other approaches
```

---

## Prioritization Criteria

Features are prioritized based on:

1. **User impact** - How many users benefit?
2. **Maintenance burden** - Does it reduce ongoing work?
3. **Effort** - Time to implement
4. **Technical debt** - Does it improve code quality?
5. **Stakeholder requests** - Festival organizers' needs

**High Priority:** High impact + reasonable effort
**Medium Priority:** Moderate impact or higher effort
**Low Priority:** Nice-to-have, low impact

---

## Related Documentation

- [Annual Updates](../annual-updates/) - Most common maintenance task
- [Troubleshooting](../troubleshooting/) - Current pain points
- [Architecture](../architecture/) - Technical constraints

---

**Want to contribute?** See [Development Guide](../development/) for how to get started.
