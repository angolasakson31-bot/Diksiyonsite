#!/usr/bin/env python3
"""
DiksiPro icon generator — pure Python, no deps.
Creates icon.png (1024x1024), adaptive-icon.png (1024x1024), splash.png (1284x2778).
Design: dark navy bg, gold soundwave bars + "DiksiPro" text area.
"""
import struct
import zlib
import os
import math

def make_png(width, height, pixels):
    """pixels: list of (r,g,b) tuples, row by row."""
    def chunk(tag, data):
        c = struct.pack('>I', len(data)) + tag + data
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return c + struct.pack('>I', crc)

    signature = b'\x89PNG\r\n\x1a\n'
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr = chunk(b'IHDR', ihdr_data)

    raw_rows = []
    for y in range(height):
        row = b'\x00'  # filter type None
        for x in range(width):
            r, g, b = pixels[y * width + x]
            row += struct.pack('BBB', r, g, b)
        raw_rows.append(row)

    raw = b''.join(raw_rows)
    compressed = zlib.compress(raw, 6)
    idat = chunk(b'IDAT', compressed)
    iend = chunk(b'IEND', b'')
    return signature + ihdr + idat + iend

# ── Color palette ────────────────────────────────────────────────────────────
BG    = (11, 16, 23)       # #0b1017
GOLD  = (201, 168, 76)     # #c9a84c
GOLDD = (160, 130, 55)     # dimmed gold
WHITE = (220, 232, 245)    # near-white text
S1    = (17, 25, 36)       # card bg
S2    = (22, 32, 46)       # slightly lighter

def lerp(a, b, t):
    return int(a + (b - a) * t)

def lerp_color(c1, c2, t):
    return (lerp(c1[0], c2[0], t), lerp(c1[1], c2[1], t), lerp(c1[2], c2[2], t))

def in_circle(cx, cy, r, x, y):
    return (x - cx)**2 + (y - cy)**2 <= r**2

def in_rect(rx, ry, rw, rh, x, y):
    return rx <= x < rx + rw and ry <= y < ry + rh

def rounded_rect(rx, ry, rw, rh, corner, x, y):
    if not in_rect(rx, ry, rw, rh, x, y):
        return False
    # check corners
    for (cx, cy) in [(rx+corner, ry+corner), (rx+rw-corner, ry+corner),
                     (rx+corner, ry+rh-corner), (rx+rw-corner, ry+rh-corner)]:
        # is x,y in corner zone?
        if x < rx+corner and y < ry+corner:
            return in_circle(rx+corner, ry+corner, corner, x, y)
        if x >= rx+rw-corner and y < ry+corner:
            return in_circle(rx+rw-corner, ry+corner, corner, x, y)
        if x < rx+corner and y >= ry+rh-corner:
            return in_circle(rx+corner, ry+rh-corner, corner, x, y)
        if x >= rx+rw-corner and y >= ry+rh-corner:
            return in_circle(rx+rw-corner, ry+rh-corner, corner, x, y)
    return True

# ── Simple bitmap font for "D", "P" letters (pixel art style) ────────────────
# 7x9 pixel bitmaps for letters we need
FONT = {
    'D': [
        "######.",
        "#.....#",
        "#.....#",
        "#.....#",
        "#.....#",
        "#.....#",
        "#.....#",
        "#.....#",
        "######.",
    ],
    'i': [
        "..#..",
        ".....",
        "..#..",
        "..#..",
        "..#..",
        "..#..",
        "..###",
    ],
    'k': [
        "#...#.",
        "#..#..",
        "#.#...",
        "##....",
        "#.#...",
        "#..#..",
        "#...#.",
    ],
    's': [
        ".####",
        "#....",
        "#....",
        ".###.",
        "....#",
        "....#",
        "####.",
    ],
    'P': [
        "#####.",
        "#....#",
        "#....#",
        "#####.",
        "#.....",
        "#.....",
        "#.....",
        "#.....",
        "#.....",
    ],
    'r': [
        "#.###",
        "##...",
        "#....",
        "#....",
        "#....",
    ],
    'o': [
        ".###.",
        "#...#",
        "#...#",
        "#...#",
        ".###.",
    ],
}

def draw_char_bitmap(pixels, width, height, char, px, py, scale, color):
    bitmap = FONT.get(char)
    if not bitmap:
        return
    for row_i, row in enumerate(bitmap):
        for col_i, cell in enumerate(row):
            if cell == '#':
                for sy in range(scale):
                    for sx in range(scale):
                        nx = px + col_i * scale + sx
                        ny = py + row_i * scale + sy
                        if 0 <= nx < width and 0 <= ny < height:
                            pixels[ny * width + nx] = color

# ── Sound wave bars ───────────────────────────────────────────────────────────
def draw_icon(width, height, padding_ratio=0.15):
    pixels = [BG] * (width * height)

    cx = width // 2
    cy = height // 2

    # Subtle radial gradient on background
    for y in range(height):
        for x in range(width):
            dx = (x - cx) / width
            dy = (y - cy) / height
            dist = math.sqrt(dx*dx + dy*dy)
            t = min(dist * 1.5, 1.0)
            glow = lerp_color(S1, BG, t)
            pixels[y * width + x] = glow

    # ── Gold ring ──────────────────────────────────────────────────────────────
    ring_r = int(width * 0.44)
    ring_thick = max(3, int(width * 0.004))
    for y in range(height):
        for x in range(width):
            d = math.sqrt((x-cx)**2 + (y-cy)**2)
            if ring_r - ring_thick <= d <= ring_r:
                t = abs(d - ring_r) / ring_thick
                alpha = max(0, 1 - t) * 0.3
                bg = pixels[y * width + x]
                blended = (
                    int(bg[0] * (1-alpha) + GOLD[0] * alpha),
                    int(bg[1] * (1-alpha) + GOLD[1] * alpha),
                    int(bg[2] * (1-alpha) + GOLD[2] * alpha),
                )
                pixels[y * width + x] = blended

    # ── Sound wave bars (centered) ─────────────────────────────────────────────
    n_bars = 7
    bar_heights_ratio = [0.18, 0.32, 0.52, 0.65, 0.52, 0.32, 0.18]
    total_bar_area = int(width * 0.55)
    bar_w = int(total_bar_area / n_bars * 0.55)
    bar_gap = int(total_bar_area / n_bars * 0.45)
    bar_corner = max(2, bar_w // 3)

    bars_total_w = n_bars * bar_w + (n_bars - 1) * bar_gap
    bars_start_x = cx - bars_total_w // 2

    # Bars sit in the upper-center area
    bar_base_y = int(height * 0.62)  # bottom of bars

    for i, h_ratio in enumerate(bar_heights_ratio):
        bh = int(height * h_ratio * 0.5)
        bx = bars_start_x + i * (bar_w + bar_gap)
        by = bar_base_y - bh

        # Color: center bar brighter
        center_dist = abs(i - n_bars//2) / (n_bars//2)
        t = 1 - center_dist * 0.4
        bar_color = (
            int(GOLD[0] * t + GOLDD[0] * (1-t)),
            int(GOLD[1] * t + GOLDD[1] * (1-t)),
            int(GOLD[2] * t + GOLDD[2] * (1-t)),
        )

        for y in range(height):
            for x in range(width):
                if rounded_rect(bx, by, bar_w, bh, bar_corner, x, y):
                    pixels[y * width + x] = bar_color

    # ── "DiksiPro" text below bars ─────────────────────────────────────────────
    scale = max(1, width // 128)  # scale font to image size

    text = "DiksiPro"
    char_w = 8 * scale  # 7px + 1 gap
    text_w = len(text) * char_w
    text_x = cx - text_w // 2
    text_y = int(height * 0.67)

    for i, ch in enumerate(text):
        col = GOLD if ch in ('D','P') else WHITE
        char_px = text_x + i * char_w
        draw_char_bitmap(pixels, width, height, ch, char_px, text_y, scale, col)

    return pixels

# ── Generate icon.png (1024x1024) ─────────────────────────────────────────────
print("Generating icon.png (1024×1024)...")
W, H = 1024, 1024
pix = draw_icon(W, H)
data = make_png(W, H, pix)
out = "/home/user/Diksiyonsite/DiksiProApp/assets/icon.png"
with open(out, 'wb') as f:
    f.write(data)
print(f"  Saved {len(data)//1024}KB → {out}")

# ── Generate adaptive-icon.png (1024x1024, slightly smaller design) ───────────
print("Generating adaptive-icon.png (1024×1024)...")
pix2 = draw_icon(W, H, padding_ratio=0.25)
data2 = make_png(W, H, pix2)
out2 = "/home/user/Diksiyonsite/DiksiProApp/assets/adaptive-icon.png"
with open(out2, 'wb') as f:
    f.write(data2)
print(f"  Saved {len(data2)//1024}KB → {out2}")

# ── Generate splash.png (1284x2778, iPhone 12 Pro Max) ────────────────────────
print("Generating splash.png (1284×2778)...")
SW, SH = 1284, 2778
splash_pix = [BG] * (SW * SH)
scx, scy = SW // 2, SH // 2

# Gradient background
for y in range(SH):
    for x in range(SW):
        dx = (x - scx) / SW
        dy = (y - scy) / SH
        dist = math.sqrt(dx*dx + dy*dy)
        t = min(dist * 1.2, 1.0)
        splash_pix[y * SW + x] = lerp_color(S1, BG, t)

# Draw icon design centered (scaled for splash)
icon_size = int(SW * 0.5)
icon_offset_x = scx - icon_size // 2
icon_offset_y = scy - icon_size // 2 - int(SH * 0.05)

# Sound wave bars in splash
n_bars = 7
bar_heights_ratio = [0.18, 0.32, 0.52, 0.65, 0.52, 0.32, 0.18]
total_bar_area = int(icon_size * 0.7)
bar_w = int(total_bar_area / n_bars * 0.55)
bar_gap = int(total_bar_area / n_bars * 0.45)
bar_corner = max(2, bar_w // 3)
bars_total_w = n_bars * bar_w + (n_bars - 1) * bar_gap
bars_start_x = scx - bars_total_w // 2
bar_base_y = icon_offset_y + int(icon_size * 0.62)

for i, h_ratio in enumerate(bar_heights_ratio):
    bh = int(icon_size * h_ratio * 0.5)
    bx = bars_start_x + i * (bar_w + bar_gap)
    by = bar_base_y - bh
    center_dist = abs(i - n_bars//2) / (n_bars//2)
    t = 1 - center_dist * 0.4
    bar_color = (
        int(GOLD[0] * t + GOLDD[0] * (1-t)),
        int(GOLD[1] * t + GOLDD[1] * (1-t)),
        int(GOLD[2] * t + GOLDD[2] * (1-t)),
    )
    for y in range(SH):
        for x in range(SW):
            if rounded_rect(bx, by, bar_w, bh, bar_corner, x, y):
                splash_pix[y * SW + x] = bar_color

# Brand text on splash
s_scale = max(1, SW // 100)
s_text = "DiksiPro"
s_char_w = 8 * s_scale
s_text_w = len(s_text) * s_char_w
s_text_x = scx - s_text_w // 2
s_text_y = bar_base_y + int(icon_size * 0.05)
for i, ch in enumerate(s_text):
    col = GOLD if ch in ('D','P') else WHITE
    char_px = s_text_x + i * s_char_w
    draw_char_bitmap(splash_pix, SW, SH, ch, char_px, s_text_y, s_scale, col)

data3 = make_png(SW, SH, splash_pix)
out3 = "/home/user/Diksiyonsite/DiksiProApp/assets/splash.png"
with open(out3, 'wb') as f:
    f.write(data3)
print(f"  Saved {len(data3)//1024}KB → {out3}")

print("\nDone! All assets generated.")
