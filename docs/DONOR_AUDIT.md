# Cherry-pick donor audit

Checked the connected repositories on 2026-11-06.

## Donor A: Cowlsly/cherry-pick-one
Upstream: mazzz1y/peel (Peel), a GeckoView-based Android native-like web app manager. Fork licence: **GNU GPL-3.0**, verified from the repository's LICENSE and README.

Ideas to study: isolation boundaries for cookies and storage, group shortcuts, biometric lock, app-specific settings, encrypted backup, web push, browser link routing.

**Do not incorporate GPL-licensed Peel source into a proprietary Tome build.** GPL software can be sold, but redistributing a derivative generally requires GPL-compatible source distribution. Review licence, asset copyright and architecture first if the Tome is ever open sourced with GPL-compatible terms. Design ideas can be independently implemented.

## Donor B: Cowlsly/cherry-pick-two
Upstream: shiahonb777/web-to-app (WebToApp). Its top-level source licence is **Unlicense**, subject to separate licences for bundled assets, dependencies, binaries and third-party samples.

Existing features: WebView and optional GeckoView, title and favicon extraction, web app creation, on-device APK export/signing, HTML and code project packaging, icon tools, extension modules, local runtimes and an APK modifier.

### Code movement performed in this alpha
- Adapted and simplified the safe URL-scheme / URL-normalisation concept from `app/src/main/java/com/webtoapp/util/UrlSafety.kt` into `app/src/main/java/online/cowlsly/tome/UrlPolicy.kt`.
- Deliberate security change: Tome **rejects** HTTP and potentially dangerous URI schemes; WebToApp's original helper defaults some links to HTTP.
- The new BookStore and Tome UI are original implementations that do not depend on donor packages.

### Next candidates for reviewed migration
- `util/UrlMetadataFetcher.kt` and `FaviconFetcher.kt`: title and favicon detection. Refactor networking and cap downloads, then add provenance.
- `util/IconLibraryStorage.kt` / `SvgIconMapper.kt`: icon import and mapping, but inspect dependencies and bundled asset licences.
- `core/apkbuilder/` and `shell/`: APK export and signing. This is a substantial dependency graph, not a stand-alone class; require source-tree inventory, reproducible build tests and security review.
- `data/dao/WebAppDao.kt`: schema and migration ideas, not a direct copy without adapting the Realm of the Tome.
- No AI keys, third-party extension downloads or ad/tracking modules should be copied by default.

## Fork lifecycle
Do not delete either fork until:
1. Every adopted feature has a source/provenance entry and compatible licence.
2. The Tome debug APK compiles and its tests run on Android hardware.
3. Each migrated feature passes a functional and security check.
4. A preserved source archive/reference remains available for attribution.

The donor repositories were not modified or deleted during this work.
