# Development Documentation

This directory contains documentation for developers working on the BeerFestApp codebase.

---

## Available Guides

### [Code Quality & Formatting](code-quality.md)

**Purpose:** Maintain consistent code style and quality

**Topics covered:**
- Spotless formatter setup and usage
- Android Lint configuration
- Pre-commit hooks
- CI/CD integration
- Troubleshooting common issues

**When to use:**
- Setting up development environment
- Before making commits
- When CI checks fail
- When formatting issues arise

---

## Quick Links

### Code Quality

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply

# Run all pre-commit checks
./gradlew precommit
```

### Other Development Guides

- [Getting Started](../getting-started.md) - Initial setup for new developers
- [Testing](../testing/) - Test coverage and testing practices
- [API Reference](../api/) - Data models and API documentation

---

## Coming Soon

The following development guides are planned:

- **Architecture Guide** - System design, MVC patterns, component relationships
- **Build System** - Gradle configuration, build variants, release process
- **Coding Conventions** - Detailed style guide, naming conventions, patterns
- **Debugging Guide** - Common debugging techniques, tools, tips

---

## Contributing to Documentation

Found an issue or want to improve the docs?

1. Edit the relevant markdown file
2. Follow the existing structure and format
3. Test any code examples you include
4. Submit a pull request

**Documentation standards:**
- Use clear, concise language
- Include code examples where helpful
- Add timestamps/version info for dated content
- Link to related documentation

---

**Last Updated:** 2025-11-18