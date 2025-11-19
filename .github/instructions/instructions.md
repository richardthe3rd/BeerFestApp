# Repository Instructions — quick reference

A one‑minute drill for contributors and maintainers. This file is intentionally tiny — CLAUDE.md and docs/ contain the authoritative, longer guides.

When to consult this file
- You need the absolute shortest steps for the annual festival update or a quick pre-PR sanity check.

Quick annual-update steps (one minute)
1. Update festival year using the helper:
   ./scripts/update-festival-year.sh 2026
2. Run tests:
   ./gradlew test
3. Build or assemble to sanity-check:
   ./gradlew assembleDebug
4. Commit and push:
   git commit -am "cbf2026"
   git push origin YOUR_BRANCH
5. Open a PR referencing "cbf2026" and attach screenshots if UI changed.

Where to find complete info (do not duplicate)
- CLAUDE.md — high-level rationale, full checklists, commands, and the canonical "Annual Update Checklist".
  Path: ./CLAUDE.md (see section "Annual Update Checklist")
- docs/ — step‑by‑step how-tos (getting-started, troubleshooting, annual-updates, etc.)
  Notable: docs/annual-updates/ and docs/getting-started.md

Minimum pre-PR sanity check (keep this short)
- Ran unit tests: ./gradlew test
- Assembled debug APK: ./gradlew assembleDebug
- Verified festival year / DB changes are consistent (see CLAUDE.md for DB_VERSION guidance)
- Include screenshots and migration notes in PR if DB or UI changed

Why this file is small
- CLAUDE.md already contains the full checklist, commands, and pitfalls. This file avoids duplication and serves as the quick pointer for someone who needs only the minimal steps.
