# Release signing and Play builds

This document is for a **human developer** preparing a Play Store upload. Create and keep your **own** Play upload keystore. Any keystore generated on a shared CI or agent box is a local throwaway for proving the Gradle wiring — **do not** treat it as the production upload key, and never commit keystores or passwords.

## 1. Generate an upload keystore (JDK 17)

Use the same JDK 17 you build with (`JAVA_HOME`). The command below creates a PKCS12 keystore (the JDK 17 `keytool` default) valid for a long time. You will be prompted for a store password, key password, and certificate name fields.

```bash
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -storetype PKCS12 \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias upload
```

Store the file **outside git** (or at least do not commit it). A typical layout is a path in your home directory, for example `~/.vocab9000-keystore/upload-keystore.jks`. Keep a secure offline backup of the keystore and both passwords. If you lose the upload key, you must request an upload-key reset in Play Console.

For PKCS12, `keytool` often uses the same password for the store and the key; that is fine. Use the same values in `keystore.properties`.

## 2. Create `keystore.properties` (gitignored)

```bash
cp keystore.properties.example keystore.properties
```

Edit the four fields:

| Property        | Meaning |
|-----------------|---------|
| `storeFile`     | Path to the `.jks` / `.keystore` / PKCS12 file. May be **relative to the repository root** or an **absolute** path. |
| `storePassword` | Keystore password |
| `keyAlias`      | Alias used with `keytool` (e.g. `upload`) |
| `keyPassword`   | Key password (often the same as the store password for PKCS12) |

`keystore.properties`, `*.jks`, `*.keystore`, and upload-keystore filenames are listed in `.gitignore`. Confirm `git status` never shows them before you commit.

If `keystore.properties` is **missing**, Gradle still configures the release build with **R8 minify and resource shrinking**, but the package is **unsigned**. That is useful for a local minify check; Play will not accept an unsigned App Bundle.

## 3. Play App Signing

Play App Signing is required for new apps:

- **Upload key** — the key you create above. You sign the AAB you upload to Play Console with this key. Vocab9000’s Gradle `signingConfig` is this upload key.
- **App signing key** — held by Google. Play re-signs the APKs that users install. You do not ship those APKs yourself.

On first upload, Play Console walks you through App Signing (Google can generate the app signing key). You only need to keep the upload keystore safe after that.

## 4. Build a release App Bundle or APK

Requires JDK 17, the Android SDK (compile/target API 35), and `local.properties` with `sdk.dir` (see [local.properties.example](./local.properties.example)).

Preferred for Play Console:

```bash
./gradlew :app:bundleRelease
```

Output (when signed):

```
app/build/outputs/bundle/release/app-release.aab
```

APK (sideload / smoke test; not the usual Play upload):

```bash
./gradlew :app:assembleRelease
```

Output (when signed):

```
app/build/outputs/apk/release/app-release.apk
```

If signing is not configured, the APK name may be `app-release-unsigned.apk`. Do not commit `*.aab` or `*.apk` (already gitignored).

## 5. Notes

- Release builds enable R8 minify (`isMinifyEnabled`) and resource shrinking (`isShrinkResources`). If a shrink-related crash appears, adjust [app/proguard-rules.pro](./app/proguard-rules.pro) rather than turning minify off.
- Version code / name live in `app/build.gradle.kts` (`versionCode`, `versionName`). Bump `versionCode` for each Play upload.
