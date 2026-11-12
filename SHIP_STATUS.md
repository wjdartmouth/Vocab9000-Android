# Vocab9000 Android — ship status

**Date:** 2026-11-12 Asia/Hong_Kong  
**Target:** Play Store shippable by 2026-11-30 Asia/Hong_Kong  
**Days left:** 18

## Landed this run (2026-11-12)

- **Catalog “mark as mastered” now persists.** `SeedService.setLearned` no longer routes through a single Good / Again grade (a first Good never set `isLearned`, so the checkmark vanished after leaving the word). It now writes `SRSService.markManuallyLearned` (interval at least 6 days, reps at least 2, `isLearned = true`) or `clearManualLearned` (back to a new-card snapshot).
- **UI language applies immediately.** `AppLocale` wraps `Application` / `MainActivity` with the saved preference (ja / en / zh-Hans / zh-TW) and calls `Locale.setDefault`, so catalog backs and strings follow the setting. Changing the language in Settings recreates the activity; a process restart is no longer required.
- **Exact-alarm Settings row (API 31+).** When daily reminders are on, Settings shows whether exact alarms are allowed and, if not, opens the system `SCHEDULE_EXACT_ALARM` screen. Inexact fallback is unchanged. Returning to the screen refreshes the status and reschedules.
- **Reminder due count is computed at fire time.** `ReminderReceiver` uses `goAsync()` and `StatsService.dueTodayCount()` so the notification body is not stuck with yesterday’s queued extra. A shared `countDue` helper keeps stats and reminders consistent.
- **Grade-button interval hints** now preview the actual SM-2 result (`formatIntervalHint`) instead of the current interval.
- **Unit tests** for SM-2 scheduling, manual mastered, interval hints, study streak, and due-count rules (`./gradlew :app:testDebugUnitTest`).

## How to build a signed release

JDK 17, Android SDK, `local.properties` (`sdk.dir`), plus `keystore.properties` pointing at your upload keystore (see [SIGNING.md](./SIGNING.md)):

```bash
./gradlew :app:bundleRelease    # Play: app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease  # Sideload: app/build/outputs/apk/release/app-release.apk
./gradlew :app:testDebugUnitTest
```

## Remaining blockers / next-run work

1. **Real Play upload keystore** — the developer must generate and back up a production upload key (not the box throwaway), keep it off git, and point `keystore.properties` at it before a Play upload.
2. **Play Console listing** — screenshots, feature graphic, high-res icon, content rating, Data safety form, and store listing publication. Draft text is in `STORE_LISTING.md`.
3. **Hosted privacy policy URL** — [PRIVACY_POLICY.md](./PRIVACY_POLICY.md) exists in-repo but Play requires a public HTTPS page.
4. **Device / emulator smoke test** — catalog seed, study / SRS, TTS, reminder, UI language switch, exact-alarm Settings row, `vocab9000://study` deep link, and a release AAB/APK install check on a real API 26+ device or emulator. Release packaging succeeded on this box in the 2026-11-11 run; runtime was not exercised here.
5. **Home-screen widget** — still deferred; not part of this milestone unless time remains after listing work.
6. AGP 8.7.3 still warns that current command-line SDK XML is newer than this AGP understands. Harmless for the successful release builds; revisit only if it blocks a later SDK package.

**ETA:** 18 days remain until 2026-11-30. Core catalog / SRS / settings / reminder flows are hardened and unit-tested. Highest-priority remaining work is developer-side Play setup (upload key, hosted privacy URL, Console assets) plus a device smoke test.
