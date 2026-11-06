# Tome of Interplanar Portals | Product specification (2026-11-06)

## Identity
Android-first magical grimoire for storing and launching website applications, shortcuts and personal tools. Permanent destination repository: Cowlsly/my-tools_tome-of-interplanar-portals_by-cowlsly-. Owner keeps the final choice of brand and licences.

## The five chapters
1. **Portals**: saved website apps, displayed as 12 positions on each page.
2. **Starred**: favourite entries from any chapter, also paginated.
3. **Shortcuts**: shortcut-only pages, distinct from installed website apps.
4. **Secrets**: privileged development utilities and connectors. Not available merely because book was unlocked.
5. **Settings**: book access controls, sorting, backup, appearance and user permissions.

The five chapter labels can be renamed; the split between starred and ordinary entries is not optional.

## Behavioural acceptance criteria
- Visually luxurious animated book cover, metallic clasps, parchment, magic runes, page-turn animation and chapter tabs. Cover artwork and page-turning are **not yet implemented**.
- First launch requires six-digit PIN creation and confirmation. Later opens require PIN or opt-in biometric unlock. Do not store a plaintext PIN.
- A rune is a globally unique identifier for one entry. An occupied rune cannot be selected elsewhere until the original owner changes icons or is deleted. This also applies between portals and shortcuts.
- Entries can be renamed, tagged, recategorised, assigned icons and marked as favourite.
- Ordering supports newest-first, alphabetical and persistently manual user order. Initial app has basic custom ordering fields, but visual drag/reorder still needs work.
- Any page has exactly 12 visible slots, with subsequent pages supporting unlimited entries.
- Build web-app entries from a user-entered HTTPS URL without requiring a computer. The present alpha launches in an internal hardened WebView; exporting independent APKs is a separate feature.
- Export user-approved home-screen shortcuts. Need Android ShortcutManager implementation and tests.
- Build complete standalone APK web wrappers on-device, without uploading private user data to external build servers. Requires a separate audited builder/signing module.
- Back pages (owner/developer only): inspect/extract user-owned APK packages, compile/decompile lawful user-owned code, adapter/connector settings. Require separate role checking and step-up authentication.
- In a later version, support Cowlsly.Online login and account recovery. NEVER reuse a Cowlsly.Online account security passphrase inside the Tome.
- Provide explicit backup/export and restoration tests before anyone relies on the app as a password-protected archive.
- Permissions: no arbitrary intents, no silent install, no automatic package extraction, no remote code execution, no unreviewed module installation.

## Security milestones
- v0.1 alpha: PIN hashing with random salt; opt-in strong biometric; app-private catalogue; HTTPS-only embedded navigation and no file access.
- Blocking release issues: encrypt catalogue at rest, strengthen anti-bruteforce using platform-backed enforcement, lifecycle auto-lock, backup/recovery tests, biometric enrolment change and threat model, input validation tests.
- Developer tools must never be reachable with book unlock alone. Future Cowlsly identity integration must use independent scoped tokens and account recovery, not shared passwords.

## Work packages
1. Inventory donor projects and audit licences (started).
2. Build shell, 5 chapters, 12 slots, uniqueness, PIN, biometric, HTTPS launch (initial alpha in feature branch).
3. Icon library: PNG/SVG/favicons, custom rune art, uniqueness database constraints.
4. Functional rearrangement, entry import/export, tagged search and system shortcuts.
5. Isolated web-app sessions and account login browser handoff.
6. Unlicense donor APK packaging: source audit, modular extraction, signing, anti-tampering, build verification, device tests.
7. Secret tools / connector capability grants and step-up auth.
8. Cowlsly.Online account recovery, encrypted backups, release QA, Play developer programme.
