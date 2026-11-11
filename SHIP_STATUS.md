# Vocab9000 Android — ship status

**Date:** 2026-11-11 Asia/Hong_Kong  
**Target:** Play Store shippable by 2026-11-30 Asia/Hong_Kong  
**Days left:** 19

## Landed this run (2026-11-11)

- **Release signing** is wired in `app/build.gradle.kts` without secrets in git. If `keystore.properties` exists at the repo root, the release `signingConfig` is applied (`storeFile` may be relative to the repo root or absolute). If it does not exist, release builds still enable minify but stay unsigned. `.gitignore` now excludes `keystore.properties`, `*.jks`, `*.keystore`, and upload-keystore filenames. See [keystore.properties.example](./keystore.properties.example) and [SIGNING.md](./SIGNING.md) for `keytool` (JDK 17), Play App Signing (upload key vs Google-held app signing key), and build commands. The developer must create the **real Play upload key**; any keystore generated on this agent box is a local throwaway only.
- **R8 minify** is on for release: `isMinifyEnabled = true` and `isShrinkResources = true`. [app/proguard-rules.pro](./app/proguard-rules.pro) keeps Room entities and converters, Gson `TypeToken` plus `DefinitionService.FreeDictionaryResponse` (and nested API models), and `SRSGrade` enum members. Catalog JSON is parsed with `JsonParser`, not mapped POJOs. Coroutines / DataStore / WorkManager rely on their consumer rules (DataStore and WorkManager are dependencies but not used directly by app code today). Compose is left on the default Android optimize file.
- On this box, a throwaway upload keystore was created **outside the git tree** (`/home/box/.vocab9000-keystore/`, with a gitignored local `keystore.properties`). It is **not** the production Play upload key and must not be reused for store upload.
- `./gradlew :app:bundleRelease` **succeeded** (R8 + resource shrink + signed AAB). Output: `app/build/outputs/bundle/release/app-release.aab` (about 3.6 MB on this run). `./gradlew :app:assembleRelease` also **succeeded**: `app/build/outputs/apk/release/app-release.apk` (about 1.8 MB; APK Signature Scheme v2 verified). AAB/APK files are gitignored and were not committed. Harmless notes: AGP still warns that command-line SDK XML is newer than this AGP understands; native-lib strip skipped `libandroidx.graphics.path.so` and `libdatastore_shared_counter.so` and packaged them as-is.
- **Chinese strings are complete.** `values-zh-rCN` (Simplified) and `values-zh-rTW` (Traditional) now include all 88 keys from the default `values/strings.xml` (the previous 56 missing keys are filled). Placeholders (`%1$d`, `%1$s`, `%2$d`) match the Japanese default and English. Existing translations were kept. `values-en` was already complete.
- Draft Play listing copy is in [STORE_LISTING.md](./STORE_LISTING.md) (EN, JA, zh-CN, zh-TW short + full descriptions). No screenshots or feature graphic were invented. The privacy policy still needs a **public HTTPS URL**.

## How to build a signed release

JDK 17, Android SDK, `local.properties` (`sdk.dir`), plus `keystore.properties` pointing at your upload keystore (see [SIGNING.md](./SIGNING.md)):

```bash
./gradlew :app:bundleRelease    # Play: app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease  # Sideload: app/build/outputs/apk/release/app-release.apk
```

## Remaining blockers / next-run work

1. **Real Play upload keystore** — the developer must generate and back up a production upload key (not the box throwaway), keep it off git, and point `keystore.properties` at it before a Play upload.
2. **Play Console listing** — screenshots, feature graphic, high-res icon, content rating, Data safety form, and store listing publication. Draft text is in `STORE_LISTING.md`.
3. **Hosted privacy policy URL** — [PRIVACY_POLICY.md](./PRIVACY_POLICY.md) exists in-repo but Play requires a public HTTPS page.
4. **Device / emulator smoke test** — catalog seed, study / SRS, TTS, reminder, `vocab9000://study` deep link, and a release AAB/APK install check on a real API 26+ device or emulator. Release packaging succeeded on this box; runtime was not exercised here.
5. **Exact alarms on API 33+** — `SCHEDULE_EXACT_ALARM` is declared but not granted by default on newer Android. Inexact fallback is in place; optional Settings UX to open the system exact-alarm screen remains optional.
6. **Home-screen widget** — still deferred; not part of this milestone unless time remains after listing work.
7. AGP 8.7.3 still warns that current command-line SDK XML is newer than this AGP understands. Harmless for the successful release builds; revisit only if it blocks a later SDK package.

**ETA:** 19 days remain until 2026-11-30. Signing, minify, and Chinese strings are unblocked; a signed, shrunk AAB can be produced once the real upload key is in place. Highest-priority next work: developer upload key + hosted privacy URL + Play Console assets (screenshots, content rating, Data safety) and a device smoke test.
