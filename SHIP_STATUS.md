# Vocab9000 Android — ship status

**Date:** 2026-11-13 Asia/Hong_Kong  
**Target:** Play Store shippable by 2026-11-30 Asia/Hong_Kong  
**Days left:** 17

## Landed this run (2026-11-13)

- **Play listing graphics.** Opaque PNG assets matching the launcher mark (`#EEF2FF` / `#4F46E5`):
  - `play-assets/hi-res-icon.png` (512 × 512) for the high-res icon
  - `play-assets/feature-graphic.png` (1024 × 500)
  - Regenerator: `play-assets/generate_store_art.py`
- **Play Console fill-in notes.** [PLAY_CONSOLE.md](./PLAY_CONSOLE.md) has recommended Data safety answers (optional Free Dictionary headword lookup + Android Auto Backup), content-rating direction, category, first-upload steps, and a device smoke-test checklist. Listing copy stays in [STORE_LISTING.md](./STORE_LISTING.md).
- **Hosted privacy policy page.** GitHub Pages is enabled (`main` / `/docs`, HTTPS). The public URL to paste into Play Console is live: https://wjdartmouth.github.io/Vocab9000-Android/privacy.html  
  [PRIVACY_POLICY.md](./PRIVACY_POLICY.md) now includes 简体中文 / 繁體中文 summaries and that URL.

## How to build a signed release

JDK 17, Android SDK, `local.properties` (`sdk.dir`), plus `keystore.properties` pointing at your upload keystore (see [SIGNING.md](./SIGNING.md)):

```bash
./gradlew :app:bundleRelease    # Play: app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease  # Sideload: app/build/outputs/apk/release/app-release.apk
./gradlew :app:testDebugUnitTest
```

## Remaining blockers / next-run work

1. **Real Play upload keystore** — the developer must generate and back up a production upload key (not a box throwaway), keep it off git, and point `keystore.properties` at it before a Play upload. See [SIGNING.md](./SIGNING.md).
2. **Play Console listing** — create the app, paste copy from `STORE_LISTING.md`, upload `play-assets/` graphics, capture **real phone screenshots**, complete content rating and Data safety using [PLAY_CONSOLE.md](./PLAY_CONSOLE.md), add a contact email in Console, and upload a signed AAB.
3. **Device / emulator smoke test** — catalog seed, study / SRS, TTS, reminder, UI language switch, exact-alarm Settings row, `vocab9000://study` deep link, optional definition fetch, and a signed release install on a real API 26+ device or emulator. Checklist is in PLAY_CONSOLE.md. Release packaging succeeded on this box in the 2026-11-11 run; runtime was not exercised here.
4. **Home-screen widget** — still deferred; not part of this milestone unless time remains after listing work.
5. AGP 8.7.3 still warns that current command-line SDK XML is newer than this AGP understands. Harmless for the successful release builds; revisit only if it blocks a later SDK package.

**ETA:** 17 days remain until 2026-11-30. Core catalog / SRS / settings / reminder flows were hardened earlier; this run added Play graphics, Console form drafts, and a live hosted privacy policy URL. Highest-priority remaining work is developer-side Play setup (upload key, Console listing, real screenshots, paste the privacy URL) plus a device smoke test.
