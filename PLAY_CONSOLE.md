# Play Console submission notes

Draft answers and asset paths for a first Vocab9000 (`com.jianwang.vocab9000`) upload. Confirm each line in Play Console against the live form — labels move occasionally. Do not treat this file as a substitute for the questionnaire you click through yourself.

**App version in the Gradle file today:** `versionCode` 1, `versionName` 1.0.0 (`app/build.gradle.kts`). Bump `versionCode` for every later upload.

**Public privacy policy URL (paste this into Play Console):**  
https://wjdartmouth.github.io/Vocab9000-Android/privacy.html

GitHub Pages is already enabled on `main` / `/docs` with HTTPS. If the URL ever 404s, check GitHub → this repo → Settings → Pages. Source HTML lives in [`docs/`](./docs/).

## Assets to upload

| Play field | File / source | Notes |
|------------|---------------|--------|
| High-res icon (512 × 512, PNG, no alpha) | [play-assets/hi-res-icon.png](./play-assets/hi-res-icon.png) | Matches the in-app launcher mark |
| Feature graphic (1024 × 500, PNG, no alpha) | [play-assets/feature-graphic.png](./play-assets/feature-graphic.png) | |
| Short / full description (EN, JA, zh-CN, zh-TW) | [STORE_LISTING.md](./STORE_LISTING.md) | |
| Phone screenshots (at least 2, up to 8) | *Capture on a device or emulator* | Do not invent UI images |
| 7-inch / 10-inch tablet screenshots | Optional | Only if you want those listings |
| Privacy policy URL | The GitHub Pages link above | Also used on the Data safety form |

### Screenshot capture (do this on a device or emulator)

Play accepts JPEG or 24-bit PNG, between 320 px and 3840 px on each side, 16:9 or 9:16 (or close). Suggested shots, in a clean language (English or Japanese) with no personal data on screen:

1. Catalog (A–Z list)
2. Word detail (gloss + TTS)
3. Study session (front or flipped card, grade buttons)
4. Stats
5. Decks or Settings (optional fifth / sixth)

Use a stock emulator or a factory-reset test profile. Do not include a Google account email, notification shade contents, or other people’s data.

## App details

| Field | Suggested value |
|-------|-----------------|
| App name | Vocab9000 |
| Application ID | `com.jianwang.vocab9000` |
| Default listing language | English (add Japanese, Simplified Chinese, Traditional Chinese from `STORE_LISTING.md`) |
| App category | Education |
| Tags (if offered) | Language learning / Education — pick the closest current tags |
| Contact email | Required by Play. Enter the developer email in Play Console; it is **not** stored in this repository. |
| Support / privacy questions | https://github.com/wjdartmouth/Vocab9000-Android/issues |
| Website (optional) | https://github.com/wjdartmouth/Vocab9000-Android |

## Data safety form (recommended answers)

Vocab9000 has no developer backend, no account, no ads, and no analytics SDK. Two off-device paths still matter: an optional dictionary lookup, and Android Auto Backup if the user has device backup turned on (`android:allowBackup="true"`).

### Overview

- **Does the app collect or share any of the required user data types?** Yes — declare the two cases below. Do not answer “No” solely because there is no Vocab9000 server.
- **Sold / used for advertising / advertising SDKs:** No.
- **Used for fraud prevention, security, or compliance:** No (not applicable).
- **Encrypted in transit:** Yes, for the optional HTTPS dictionary request. Android Backup encryption is handled by Google, not by this app.
- **Users can request deletion:** Local data is removed by clearing app storage or uninstalling. We do not hold a server copy to delete.

### Optional English-definition lookup (user-initiated)

When the user opens an English definition, the app may send **only that English headword** to the Free Dictionary API (`https://api.dictionaryapi.dev`) over HTTPS and cache the response on device. No account, name, or device identifier is sent with the request.

Suggested declaration:

- **Data type:** App activity → Other user activity (or “In-app search history” if that is a closer current label — it is a single headword lookup, not a stored search history we collect).
- **Collected / shared:** Shared with a third party (the Free Dictionary API). The developer does not operate a server that stores this request.
- **Required or optional:** Optional. Core study works from bundled Japanese and Chinese glosses without this request.
- **Purpose:** App functionality.
- **Sold / ads / sharing for ads:** No.
- **Ephemeral processing:** The API response is cached on the device after a successful fetch; the developer does not retain it on a backend.

### Android Auto Backup (system backup, if the user has it enabled)

Because `android:allowBackup` is true, Google’s backup service may include the app’s local data when the user backs up the device. The app does not run its own backup server. If Play still requires this to be listed as collected (it is transmitted off the device by the OS), declare approximately:

- Custom decks and cards: App activity → Other user-generated content (or Files and docs if that is closer)
- Spaced-repetition progress and preferences (reminder time, TTS / UI language): App activity or App info — pick the closest current labels
- Cached English definitions: the same “app functionality” bucket as the optional lookup
- **Purpose:** App functionality (device backup and restore)
- **Required or optional:** Optional from the user’s point of view (controlled in system backup settings)
- **Sold / ads:** No

If you later set `android:allowBackup="false"`, remove the backup disclosures and update [PRIVACY_POLICY.md](./PRIVACY_POLICY.md).

### What not to declare

- Approximate location, precise location, personal info, financial, health, messages, photos, contacts, calendar, device identifiers, advertising ID — the app does not collect these.
- Crash or analytics SDKs — none are included.

## Content rating questionnaire (expected direction)

Complete the official IARC questionnaire in Play Console. Expected direction for this app, not a substituted rating certificate:

- Educational / reference vocabulary study; general audience, not a children’s-only product and not a social network.
- No violence, sexual content, drug use, or gambling features.
- No in-app chat or communication with other users.
- No location sharing.
- No digital goods or paid features in the current build.
- Users can create decks and cards, but those stay on the device and are not published to other users — this is not shared user-generated content.
- The app is not a general-purpose web browser. Network use is the optional dictionary lookup above.
- The bundled Longman Communication 9000 list is a general English word list. This note does not audit every headword for occasional everyday terms; answer the language/mature-content questions honestly if the form asks about dictionary content.

The form itself issues the rating (often Everyone / PEGI 3 for this kind of app). Do not guess a rating and skip the questionnaire.

## First upload

1. Create your own Play upload keystore and `keystore.properties` (see [SIGNING.md](./SIGNING.md)). Do not use a keystore generated on a shared agent box.
2. `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`
3. In Play Console, create the app, accept Play App Signing (Google can generate the app signing key), and upload the AAB signed with your upload key.
4. Paste listing copy, upload the icon, feature graphic, and screenshots, complete Data safety and content rating, and add the privacy policy URL.
5. Smoke-test a release install on an API 26+ device or emulator before rolling out (even a closed testing track is enough for a first check).

## Device smoke-test checklist

Still required before a production rollout. On API 26+ (ideally also a current system image):

- [ ] First launch seeds built-in decks and the starter 50-word sample
- [ ] Catalog browse, search, word detail, mark mastered (survives leaving the word)
- [ ] TTS playback
- [ ] Study / SM-2 grade buttons and interval hints
- [ ] Stats (mastered, due, streak)
- [ ] Settings: UI language switch applies immediately; reminder toggle; exact-alarm row on API 31+
- [ ] Daily reminder notification opens `vocab9000://study`
- [ ] Cold start from `vocab9000://study`
- [ ] Optional English definition fetch (online) and cached reopen (offline)
- [ ] Signed release AAB/APK installs and launches after R8 minify

## Still only the developer can do

- Generate and back up the production upload keystore
- Create the Play Console app, complete the forms, and upload the AAB
- Capture real screenshots
- Confirm the privacy URL loads over HTTPS (GitHub Pages, after it is enabled)
- Add the Play contact email in Console
