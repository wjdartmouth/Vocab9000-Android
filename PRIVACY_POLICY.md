# Privacy Policy — Vocab9000 (Android)

**Last updated:** 9 November 2026  
**App:** Vocab9000 (`com.jianwang.vocab9000`)  
**Developer:** Jian Wang  
**Contact:** [GitHub Issues](https://github.com/wjdartmouth/Vocab9000/issues)

This policy describes how the Android edition of Vocab9000 handles information. The app is designed for on-device learning with minimal data leaving your device.

---

## English

### Summary
Vocab9000 does **not** require an account, does **not** show ads, and does **not** track you for advertising or analytics. Learning progress stays on your device. Optional network use is limited to fetching English definitions. Local notifications use data only on your device.

### Information we do **not** collect
- No sign-up, login, or user profile
- No advertising identifiers or third-party ad SDKs
- No analytics / crash-reporting SDKs that send personal data to us
- No sale or sharing of personal data with data brokers

### Information stored **on your device**
Vocab9000 stores learning-related data locally using **Room** (SQLite) and SharedPreferences, including for example:
- Decks and flashcards you create or edit
- Spaced-repetition / progress fields (e.g. learned status, review schedule)
- Cached English definitions after a successful fetch
- Your notification preference, reminder time, TTS language, and UI language preference

This data remains on your device unless you back up or transfer the device with your own system tools (e.g. Google Backup), which is controlled by Google and your account settings—not by Vocab9000 servers (we do not operate an app backend for user data).

### Optional network: Free Dictionary API
If you view English definitions (e.g. on a word detail or study screen), the app may request definitions from the **Free Dictionary API** (`https://api.dictionaryapi.dev`) for the English headword.  
- Only the word needed for lookup is sent to that service.  
- Successful responses may be **cached on device** so later opens work offline.  
- If the request fails or you are offline, learning still works using bundled glosses (Japanese / Chinese as available).  
We do not control the Free Dictionary API’s own servers or policies; use of that service is optional for the English-definition feature.

### Local notifications
If you enable the daily study reminder in Settings and grant notification permission, Vocab9000 schedules **local** alarms/notifications on your device (default evening time, configurable).  
- Notifications are not sent from our servers.  
- You can turn reminders off in-app or revoke permission in Android Settings.

### Children’s privacy
Vocab9000 is a general vocabulary study app. We do not knowingly collect personal information from children. There is no account system for creating child profiles.

### Your choices
- Disable network: avoid opening English-definition fetches; core study with bundled glosses still works.  
- Disable notifications: Settings in-app or Android Settings → Apps → Vocab9000.  
- Delete local study data: clear app storage or uninstall the app.

### Changes
We may update this policy when the app’s practices change. The “Last updated” date will be revised; continued use after changes means you accept the updated policy where applicable.

### Contact
Questions about privacy: open an issue at  
https://github.com/wjdartmouth/Vocab9000/issues

---

## 日本語（要約）

Vocab9000（Android）は **アカウント不要・広告なし・トラッキングなし** の単語帳アプリです。

- **端末内:** 学習進捗・デッキ・間隔反復データなどは **Room** 等で端末に保存します。当社のユーザーデータ用サーバーはありません。  
- **任意の通信:** 英語の定義表示時のみ **Free Dictionary API** に英単語を問い合わせ、成功時は端末にキャッシュします。失敗・オフラインでもバンドル訳で学習できます。  
- **通知:** 設定でオンにした場合のみ **ローカル通知**（サーバー配信ではありません）。  
- **連絡先:** https://github.com/wjdartmouth/Vocab9000/issues

詳細は上記 English 本文に準じます。
