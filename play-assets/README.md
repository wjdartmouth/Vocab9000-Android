# Play Store graphic assets

These files are ready to upload in Play Console → your app → Store presence / Main store listing. They match the in-app launcher colors (`#EEF2FF` background, `#4F46E5` bars from `ic_launcher_foreground.xml`).

| File | Play field | Spec |
|------|------------|------|
| [hi-res-icon.png](./hi-res-icon.png) | High-res icon | 512 × 512, 32-bit PNG, **no alpha** |
| [feature-graphic.png](./feature-graphic.png) | Feature graphic | 1024 × 500, PNG, **no alpha** |

**Phone / tablet screenshots are not in this folder.** Capture those from a device or emulator (see [PLAY_CONSOLE.md](../PLAY_CONSOLE.md)). Do not invent UI screenshots.

To regenerate the two graphics (requires the Inter and Noto Sans CJK fonts used on the original machine):

```bash
python3 play-assets/generate_store_art.py
```

The committed PNGs are the source of truth for a Play upload. You do not need to rerun the script unless you change the layout or copy.
