# Vocab9000 Android — ship status

**Date:** 2026-11-19 Asia/Hong_Kong  
**Target:** Play Store shippable by 2026-11-30 Asia/Hong_Kong  
**Days left:** 11

## In-repo ship criteria (met)

The repository now meets the in-repo bar for a Play submission:

- Release AAB / APK path is documented in [SIGNING.md](./SIGNING.md) and [README.md](./README.md)
- Store listing drafts are in [STORE_LISTING.md](./STORE_LISTING.md), Console fill-in notes in [PLAY_CONSOLE.md](./PLAY_CONSOLE.md), graphics in `play-assets/`
- Critical catalog / SRS / settings / reminder / deep-link issues found in earlier runs are fixed
- README run path is verified on this box (see today, below)
- Hosted privacy policy: https://wjdartmouth.github.io/Vocab9000-Android/privacy.html

## Verified this run (2026-11-19)

Re-checked after the 2026-11-16 Glance widget landed (that change had not yet been through a minify/release package):

- `./gradlew :app:testDebugUnitTest` — 18 tests, 0 failures
- `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL
- `./gradlew :app:bundleRelease` — BUILD SUCCESSFUL  
  `app/build/outputs/bundle/release/app-release.aab` (~4.9 MB)
- `./gradlew :app:assembleRelease` — BUILD SUCCESSFUL  
  `app/build/outputs/apk/release/app-release.apk` (~2.5 MB)

The release artifacts on this box were signed with a **throwaway** keystore under `/home/box/.vocab9000-keystore/`. Do **not** upload those binaries to Play. Generate and back up a production upload key on a machine you control (see [SIGNING.md](./SIGNING.md)).

Harmless AGP note still appears: command-line SDK XML v4 vs AGP 8.7.3 understanding v3. The strip-debug-symbols step also leaves `libandroidx.graphics.path.so` and `libdatastore_shared_counter.so` unstripped; packaging still succeeds.

## Previously landed

- Glance home-screen widget (`DueStreakWidget`): due-today, streak, learned count; tap opens `vocab9000://study`
- Play listing graphics, Console drafts, GitHub Pages privacy policy
- Release signing setup, R8 minify, reminder / deep-link hardening, zh-rCN / zh-rTW strings, SRS unit tests

## How to build a signed release

JDK 17, Android SDK, `local.properties` (`sdk.dir`), plus `keystore.properties` pointing at **your** upload keystore (see [SIGNING.md](./SIGNING.md)):

```bash
./gradlew :app:bundleRelease    # Play: app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease  # Sideload: app/build/outputs/apk/release/app-release.apk
./gradlew :app:testDebugUnitTest
```

## Remaining work (developer-side only)

In-repo product work for this milestone is complete. What is left cannot be finished from this development box:

1. **Real Play upload keystore** — generate and back up a production upload key (not a box throwaway), keep it off git, and point `keystore.properties` at it before a Play upload. See [SIGNING.md](./SIGNING.md).
2. **Play Console listing** — create the app, paste copy from `STORE_LISTING.md`, upload `play-assets/` graphics, capture **real phone screenshots** on a device or emulator (do not invent UI images), complete content rating and Data safety using [PLAY_CONSOLE.md](./PLAY_CONSOLE.md), add a contact email in Console, and upload a signed AAB.
3. **Device / emulator smoke test** — catalog seed, study / SRS, TTS, reminder, UI language switch, exact-alarm Settings row, `vocab9000://study` deep link, home-screen widget add / tap / data refresh, optional definition fetch, and a signed release install on a real API 26+ device or emulator. Checklist is in PLAY_CONSOLE.md.

This box cannot run an emulator usefully (no KVM access for user `box`, no system images installed), so runtime confirmation has to happen on a developer machine or physical device.

**ETA:** 11 days remain until 2026-11-30. The weekday ship routine stops after this note: further work is the Play Console / key / device steps above, which need the developer account and a real device or emulator.
