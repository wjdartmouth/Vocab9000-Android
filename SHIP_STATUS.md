# Vocab9000 Android — ship status

**Date:** 2026-11-10 Asia/Hong_Kong  
**Target:** Play Store shippable by 2026-11-30 Asia/Hong_Kong  
**Days left:** 20

## Landed this run (2026-11-10)

- Official **Gradle 8.9 wrapper** is now in the repo: `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` taken from the Gradle `v8.9.0` tag (JAR SHA-256 `498495120a03b9a6ab5d155f5de3c8f0d986a449153702fb80fc80e134484f17`). `distributionUrl` remains `gradle-8.9-bin.zip`; `distributionSha256Sum` is set to the official checksum `d725d707bfabd4dfdc958c624003b3c80accc03f7037b5122c4b1d0ef15cecab`.
- README run steps no longer say the wrapper may be missing. CLI steps document JDK 17, Android SDK, and `local.properties` (`sdk.dir`). `local.properties.example` lists macOS / Linux / Windows paths.
- On this agent box, Debian apt mirrors returned HTTP 500, so JDK 17 was installed from **Eclipse Temurin 17.0.21+9** (not via apt). Android command-line tools **15859902** plus `platforms;android-35`, `build-tools;35.0.0` (Gradle also pulled `build-tools;34.0.0`), and `platform-tools` were installed to `/home/box/Android/Sdk`. These toolchain installs are **local to the box**, not committed.
- `./gradlew :app:assembleDebug` was run and **succeeded** (debug APK packaged). First compile failure was a Material 3 API mismatch in Settings (`ExposedDropdownMenu` is a scope member, not a top-level import; `menuAnchor` updated to `MenuAnchorType.PrimaryNotEditable`). Also fixed a missing `import androidx.compose.ui.unit.dp` in `SettingsScreen.kt` that would have failed the same compile.
- Light ship harden:
  - Daily reminder uses a one-shot alarm and reschedules: `setExactAndAllowWhileIdle` when `canScheduleExactAlarms()` is true (API 31+), otherwise `setAndAllowWhileIdle`, with a `SecurityException` fallback. `ReminderReceiver` no longer fires if the user has turned reminders off.
  - `BootReceiver` stays `exported="true"` (required for system `BOOT_COMPLETED`) and also handles `MY_PACKAGE_REPLACED` so reminders are restored after an app update.
  - `MainActivity` is `singleTop` so the `vocab9000://study` notification / deep link reuses the activity and `onNewIntent` runs.
  - RTL-aware AutoMirrored icons for catalog and speak actions (clears Material icon deprecation warnings).
- Privacy contact URLs now point at the **Android** repo issues (`https://github.com/wjdartmouth/Vocab9000-Android/issues`), not the iOS repo. Last updated: 10 November 2026.
- Empty `res/values-ja/` (no files; never tracked in git) was leftover. Default `values/strings.xml` is already Japanese, so there is no Japanese-string resource gap.

## Remaining blockers / next-run work

1. **Release signing** — release build still has `isMinifyEnabled = false` and no `signingConfig` / keystore notes. Need a Play upload keystore (kept off git), `signingConfig` for release, and a short local signing doc. Then `assembleRelease` / Play App Signing.
2. **R8 / ProGuard** — enable minify for release and verify Room, Gson, and TTS still work after shrinking.
3. **Chinese strings incomplete** — `values-zh-rCN` and `values-zh-rTW` each miss 56 keys (notifications, settings, study status, deck editor, etc.) and fall back to Japanese. Fill those translations before store listing in those locales.
4. **Play Console listing** — store copy, screenshots, feature graphic, content rating, privacy questionnaire, and Data safety form are not started. Use the updated `PRIVACY_POLICY.md` (host a public URL).
5. **Exact alarms on API 33+** — `SCHEDULE_EXACT_ALARM` is declared but not granted by default on newer Android. Inexact fallback is in place; optional Settings UX to send the user to the system exact-alarm screen if they want a precise reminder.
6. **Home-screen widget** — intentionally deferred; do not treat as this milestone unless time remains after signing and listing.
7. **Device / Play pre-launch testing** — debug assemble succeeded on this box; still need emulator or device run-through (catalog seed, study, TTS, reminder, deep link) and a release AAB install smoke test.
8. AGP 8.7.3 warns that current command-line SDK XML is newer than this AGP understands. Harmless for the debug build; revisit only if it blocks a later SDK package.

**ETA:** 20 days remain. Wrapper + debug compile are unblocked. Highest-priority next run: release signing + minify, then zh string fill and Play listing assets.
