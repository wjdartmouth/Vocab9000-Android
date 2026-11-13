#!/usr/bin/env python3
"""Generate Play Console high-res icon and feature graphic from Vocab9000 brand colors.

Brand (matches app/src/main/res/drawable launcher vectors):
  background #EEF2FF, bars #4F46E5
Outputs are opaque RGB PNGs (Play rejects alpha on these assets).
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUT = Path(__file__).resolve().parent
BG = (0xEE, 0xF2, 0xFF)
INK = (0x4F, 0x46, 0xE5)
INK_DARK = (0x1E, 0x1B, 0x4B)
INK_MUTED = (0x43, 0x38, 0xCA)
WHITE = (255, 255, 255)

INTER = "/usr/share/fonts/truetype/sand-box/google/Inter/Inter-VariableFont_opsz,wght.ttf"
NOTO_JP_REG = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
NOTO_JP_BOLD = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"


def inter(size: int, weight: str = "Regular") -> ImageFont.FreeTypeFont:
    font = ImageFont.truetype(INTER, size)
    font.set_variation_by_name(weight)
    return font


def noto(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    path = NOTO_JP_BOLD if bold else NOTO_JP_REG
    return ImageFont.truetype(path, size, index=0)


def draw_wordmark(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], radius: int | None = None) -> None:
    """Draw the three-bar wordmark (last bar shorter) inside a square box."""
    x, y, w, h = box
    if radius is None:
        radius = max(8, w // 16)
    draw.rounded_rectangle((x, y, x + w, y + h), radius=radius, fill=BG)
    # Proportions from ic_launcher_foreground.xml (viewport 108, bars at y=34/50/66).
    inset_x = w * 30 / 108
    bar_w = w * 48 / 108
    short_w = w * 32 / 108
    bar_h = max(6, int(h * 8 / 108))
    top = y + h * 34 / 108
    gap = h * 16 / 108
    for i, width in enumerate((bar_w, bar_w, short_w)):
        by = top + i * gap
        draw.rounded_rectangle(
            (x + inset_x, by, x + inset_x + width, by + bar_h),
            radius=bar_h / 2,
            fill=INK,
        )


def make_icon() -> Image.Image:
    size = 512
    img = Image.new("RGB", (size, size), BG)
    draw = ImageDraw.Draw(img)
    # Full-bleed background already matches the launcher. Draw bars with the
    # same 108-viewport proportions so Play's circular crop still shows them.
    draw_wordmark(draw, (0, 0, size, size), radius=0)
    return img


def text_size(font: ImageFont.FreeTypeFont, text: str) -> tuple[int, int]:
    box = font.getbbox(text)
    return box[2] - box[0], box[3] - box[1]


def make_feature() -> Image.Image:
    w, h = 1024, 500
    img = Image.new("RGB", (w, h), BG)
    # Soft centered indigo glow (still opaque). Keep art in the middle ~70%
    # so promo placements that crop the sides do not cut off the wordmark.
    shade = Image.new("RGB", (w, h), BG)
    sd = ImageDraw.Draw(shade)
    sd.ellipse((180, -140, 844, 640), fill=(0xE0, 0xE7, 0xFF))
    img = Image.blend(img, shade.filter(ImageFilter.GaussianBlur(48)), 0.85)

    card = 220
    gap = 44
    title = "Vocab9000"
    en1 = "Study the Longman 9000"
    en2 = "On-device SRS flashcards."
    ja = "端末の中で、間隔反復の単語帳。"
    title_font = inter(72, "Bold")
    en_font = inter(26, "Medium")
    ja_font = noto(24, bold=False)
    title_w, title_h = text_size(title_font, title)
    en_w = max(text_size(en_font, en1)[0], text_size(en_font, en2)[0])
    en_h = text_size(en_font, en1)[1]
    ja_w, ja_h = text_size(ja_font, ja)
    text_w = max(title_w, en_w, ja_w)
    group_w = card + gap + text_w
    cx = (w - group_w) // 2
    cy = (h - card) // 2

    shadow = Image.new("RGB", (w, h), img.getpixel((0, 0)))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle((cx + 8, cy + 14, cx + card + 8, cy + card + 14), radius=44, fill=(0xC7, 0xD2, 0xFE))
    img = Image.blend(img, shadow.filter(ImageFilter.GaussianBlur(16)), 0.55)
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle((cx, cy, cx + card, cy + card), radius=44, fill=WHITE)
    pad = 6
    draw_wordmark(draw, (cx + pad, cy + pad, card - 2 * pad, card - 2 * pad), radius=38)

    tx = cx + card + gap
    block_h = title_h + 20 + en_h + 8 + en_h + 36 + ja_h
    ty = cy + (card - block_h) // 2
    draw.text((tx, ty), title, font=title_font, fill=INK_DARK)
    ty += title_h + 20
    draw.text((tx, ty), en1, font=en_font, fill=INK_MUTED)
    ty += en_h + 8
    draw.text((tx, ty), en2, font=en_font, fill=INK_MUTED)
    ty += en_h + 36
    draw.rounded_rectangle((tx, ty - 16, tx + 48, ty - 10), radius=3, fill=INK)
    draw.text((tx, ty), ja, font=ja_font, fill=INK)
    return img.convert("RGB")


def main() -> None:
    icon = make_icon()
    feature = make_feature()
    icon_path = OUT / "hi-res-icon.png"
    feature_path = OUT / "feature-graphic.png"
    icon.save(icon_path, "PNG", optimize=True)
    feature.save(feature_path, "PNG", optimize=True)
    for path, expect in ((icon_path, (512, 512)), (feature_path, (1024, 500))):
        with Image.open(path) as im:
            assert im.mode == "RGB", im.mode
            assert im.size == expect, im.size
            assert "A" not in (im.mode or "")
        print(f"wrote {path} {expect[0]}x{expect[1]} RGB")


if __name__ == "__main__":
    main()
