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
2. Gradle Sync を実行（初回は依存関係のダウンロードに数分かかることがあります）
3. エミュレータまたは実機を選び **Run ▶**

Gradle Wrapper が未同梱の場合、Android Studio がプロジェクトを開いたときに Wrapper を生成・利用します。CLI で使う場合:

```bash
# Android Studio 同梱の SDK / JDK を使い、プロジェクト内で:
./gradlew :app:assembleDebug
```

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
└── README.md
```

## 注意

- 組み込み「All」デッキは約 9000 語の仮想デッキです。FlashCard を 9000 枚物性化せず、Progress 集計で扱います（iOS と同じ方針）。
- ホーム画面ウィジェットは Android 版では未実装（将来対応可）。詳細は [REPORT.md](./REPORT.md)。

## ライセンス / 帰属

[ATTRIBUTION.md](./ATTRIBUTION.md) · [PRIVACY_POLICY.md](./PRIVACY_POLICY.md)
