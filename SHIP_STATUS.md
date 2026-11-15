# Vocab9000 Android — ship status

**Date:** 2026-11-16 Asia/Hong_Kong  
**Target:** Play Store shippable by 2026-11-30 Asia/Hong_Kong  
**Days left:** 14

## Landed this run (2026-11-16)

- **Home-screen widget (previously deferred).** A Glance Material 3 widget, `DueStreakWidget`, parallels the iOS `DueStreakWidget`:
  - Shows due-today count, study streak, and learned-word count
  - Tap opens the study flow via `vocab9000://study`
  - Counts come from `StatsService` Progress aggregation (the All deck is not materialized as 9000 FlashCards)
  - Refreshes after SRS grades and “mark learned”, after a UI language change, on app / boot start, and on a 30-minute WorkManager cycle
  - Localized in ja / en / zh-rCN / zh-rTW
  - How to add it is documented in [README.md](./README.md) and the smoke-test list in [PLAY_CONSOLE.md](./PLAY_CONSOLE.md)

## Previously landed

- Play listing graphics in `play-assets/`, Console fill-in notes in [PLAY_CONSOLE.md](./PLAY_CONSOLE.md), listing copy in [STORE_LISTING.md](./STORE_LISTING.md)
- Hosted privacy policy: https://wjdartmouth.github.io/Vocab9000-Android/privacy.html
- Release signing setup, R8 minify, reminder / deep-link hardening, zh-rCN / zh-rTW strings, SRS unit tests (see earlier commits)

## How to build a signed release

JDK 17, Android SDK, `local.properties` (`sdk.dir`), plus `keystore.properties` pointing at your upload keystore (see [SIGNING.md](./SIGNING.md)):

```bash
./gradlew :app:bundleRelease    # Play: app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease  # Sideload: app/build/outputs/apk/release/app-release.apk
./gradlew :app:testDebugUnitTest
```

This development box must not be treated as the source of a production Play upload key. A throwaway keystore on a shared agent machine (for example `/home/box/.vocab9000-keystore/`) is for local packaging experiments only.

## Remaining blockers / next-run work

1. **Real Play upload keystore** — the developer must generate and back up a production upload key (not a box throwaway), keep it off git, and point `keystore.properties` at it before a Play upload. See [SIGNING.md](./SIGNING.md).
2. **Play Console listing** — create the app, paste copy from `STORE_LISTING.md`, upload `play-assets/` graphics, capture **real phone screenshots** on a device or emulator (do not invent UI images), complete content rating and Data safety using [PLAY_CONSOLE.md](./PLAY_CONSOLE.md), add a contact email in Console, and upload a signed AAB.
3. **Device / emulator smoke test** — catalog seed, study / SRS, TTS, reminder, UI language switch, exact-alarm Settings row, `vocab9000://study` deep link, home-screen widget add / tap / data refresh, optional definition fetch, and a signed release install on a real API 26+ device or emulator. Checklist is in PLAY_CONSOLE.md.

This box cannot run an emulator usefully (no KVM access for user `box`, no system images installed), so runtime confirmation has to happen on a developer machine or physical device.

4. AGP 8.7.3 still warns that current command-line SDK XML is newer than this AGP understands. Harmless for successful builds; revisit only if it blocks a later SDK package.

**ETA:** 14 days remain until 2026-11-30. In-repo product work for this milestone, including the previously deferred home-screen widget, is complete. Remaining work is developer-side: a real Play upload key, Play Console listing with real device screenshots, and a device or emulator smoke test.
