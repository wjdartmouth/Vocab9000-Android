# Vocab9000 — Android

Longman Communication 9000 単語帳の **Android / Jetpack Compose** 版です。  
iOS 版 [Vocab9000](https://github.com/wjdartmouth/Vocab9000) と機能を揃えた並列アプリです。

## 要件

- **Android Studio** Ladybug / Koala 以降（AGP 8.7+、JDK 17）
- エミュレータまたは実機 **API 26+**（Android 8.0）
- インターネットは英語定義の取得時のみ（任意）

## データの出典

| 内容 | 出典 |
|------|------|
| 単語リスト（約 9000 語） | [MuhammadYaseenKhan/Longman-Communication](https://github.com/MuhammadYaseenKhan/Longman-Communication) |
| 日本語訳 | [EJDict-hand](https://github.com/kujirahand/EJDict)（CC0）＋編集部短訳 |
| 中文释义 | CC-CEDICT 等（詳細は [ATTRIBUTION.md](./ATTRIBUTION.md)） |
| 英語定義 | [Free Dictionary API](https://dictionaryapi.dev/)（取得後 Room にキャッシュ） |

**Pearson / Longman Dictionary の定義文は収録・スクレイピングしていません。**

バンドル JSON: `app/src/main/assets/longman9000.json`

## Android Studio で開く

1. Android Studio を起動 → **File → Open** → このリポジトリのルート（`Vocab9000-Android` / `settings.gradle.kts` があるフォルダ）を選択
2. **Sync Project with Gradle Files** を実行（初回は Gradle 8.9 と依存関係のダウンロードに数分かかることがあります）
3. エミュレータまたは実機を選び **Run ▶**

公式 Gradle Wrapper（Gradle 8.9）はリポジトリに同梱しています（`gradlew` / `gradlew.bat` / `gradle/wrapper/`）。Android Studio が `local.properties` の `sdk.dir` を自動生成します。

## コマンドラインでビルド

JDK 17 と Android SDK（compile/target API 35）を用意し、`local.properties` に SDK パスを書いてから:

```bash
# 例: cp local.properties.example local.properties  して sdk.dir を編集
./gradlew :app:assembleDebug
```

`local.properties` は gitignore されています。パスの書き方は [local.properties.example](./local.properties.example) を参照してください。

## リリースビルド（Play 提出用）

アップロード用キーストアの作成、git に含めない `keystore.properties` の設定、R8 minify 付きのリリースビルドは [SIGNING.md](./SIGNING.md) を参照してください。

```bash
./gradlew :app:bundleRelease    # Play 提出用 AAB: app/build/outputs/bundle/release/app-release.aab
./gradlew :app:assembleRelease  # サイドロード用 APK: app/build/outputs/apk/release/app-release.apk
```

`keystore.properties` が無い場合でも minify は有効ですが、成果物は未署名になります（Play には提出できません）。キーストアとパスワードはコミットしないでください。

## 機能

- **カタログ**: A–Z / 品詞フィルタ / 検索、単語詳細、TTS、覚えたマーク、マイデッキへ追加
- **デッキ**: 組み込み仮想デッキ（All / 文字レンジ）＋マイデッキ CRUD、カード追加・編集・削除
- **学習 / SRS**: SM-2（もう一度 / 難しい / 普通 / 簡単）。期限切れ優先、セッション上限、Room 永続化
- **統計**: 覚えた語数・今日の期限・連続学習日数・最終学習、文字別バー、デッキ別進捗
- **設定**: TTS 言語、学習リマインダー時刻（ローカル通知）、UI 言語設定
- **オフライン**: カタログは assets、進捗・定義キャッシュは Room
- **初回起動**: 組み込みデッキ＋「はじめての50語」サンプルをシード
- **ディープリンク**: `vocab9000://study` で学習タブを開く

## 技術スタック

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose
- Room + coroutines / Flow
- Android `TextToSpeech`
- applicationId: `com.jianwang.vocab9000`

## ディレクトリ（概要）

```
Vocab9000-Android/
├── app/src/main/java/com/jianwang/vocab9000/
│   ├── data/          # Room entities, DAOs, repositories
│   ├── domain/        # CatalogEntry, SRSService, StudyItem
│   └── ui/            # Compose screens (catalog/decks/study/stats/settings)
├── app/src/main/assets/longman9000.json
├── gradle/wrapper/    # Official Gradle 8.9 wrapper
├── SIGNING.md         # Release signing + bundleRelease
└── README.md
```

## 注意

- 組み込み「All」デッキは約 9000 語の仮想デッキです。FlashCard を 9000 枚物性化せず、Progress 集計で扱います（iOS と同じ方針）。
- ホーム画面ウィジェットは Android 版では未実装（将来対応可）。詳細は [REPORT.md](./REPORT.md)。
- 出荷状況・残作業は [SHIP_STATUS.md](./SHIP_STATUS.md) を参照してください。

## ライセンス / 帰属

[ATTRIBUTION.md](./ATTRIBUTION.md) · [PRIVACY_POLICY.md](./PRIVACY_POLICY.md)
