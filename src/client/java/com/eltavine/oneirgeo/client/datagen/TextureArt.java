package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.util.Hash;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Procedural placeholder art in the vanilla 16x style. Every texture is a pure function of its name,
 * so regenerating data never churns the files; replace any PNG by hand and stop generating it.
 */
final class TextureArt {
    static final Map<String, Consumer<Canvas>> BLOCKS = new LinkedHashMap<>();
    static final Map<String, Consumer<Canvas>> ITEMS = new LinkedHashMap<>();
    /** Larger textures, keyed by path under {@code assets/oneirgeo/} without extension. */
    static final Map<String, Sized> EXTRA = new LinkedHashMap<>();

    record Sized(int size, Consumer<Canvas> painter) {
    }

    static void icon(Canvas c) {
        int s = c.size();
        c.fill((x, y) -> {
            double t = y / (double) s;
            int sky = t < 0.62 ? mix(0x4D4B92, 0xE3956F, t / 0.62) : mix(0x6E8F5A, 0x3E5034, (t - 0.62) / 0.38);
            double sx = x - s * 0.72;
            double sy = y - s * 0.56;
            if (t < 0.62 && sx * sx + sy * sy < (s * 0.07) * (s * 0.07)) {
                sky = 0xFFF1D8;
            }
            boolean monolith = Math.abs(x - s * 0.42) < s * 0.07 && y > s * 0.16 && t < 0.7;
            return monolith ? 0x1E1E24 : sky;
        });
    }

    /**
     * The eye in the End's sky, drawn additively: black and transparent pixels show the sky through,
     * so the pupil is a hole and the sclera glows.
     */
    static void eye(Canvas c) {
        int s = c.size();
        double half = s / 2.0;
        c.each((x, y) -> {
            double u = (x + 0.5 - half) / half;
            double v = (y + 0.5 - half) / half;
            double lid = 0.62 * (1.0 - u * u);
            if (Math.abs(v) > lid || Math.abs(u) >= 1.0) {
                c.set(x, y, 0x00000000);
                return;
            }
            double edge = 1.0 - Math.abs(v) / Math.max(lid, 1.0E-3);
            double r = Math.sqrt(u * u + v * v);
            int color;
            if (r < 0.13) {
                color = 0x000000;
            } else if (r < 0.36) {
                double ring = (r - 0.13) / 0.23;
                double fibre = 0.5 + 0.5 * Math.sin(Math.atan2(v, u) * 23.0);
                color = mix(mix(0x2A1640, 0x7E5AA8, ring), 0xB79BD8, fibre * 0.25);
            } else {
                double vein = Math.abs(Math.sin(Math.atan2(v, u) * 7.0 + r * 9.0 + c.fbm(x, y, 4.0) * 2.0));
                color = vein < 0.05 && r > 0.5 ? 0x8E4A52 : mix(0xA59A90, 0xF2ECE2, Math.min(1.0, edge * 2.2));
            }
            int alpha = (int) (255 * Math.min(1.0, edge * 3.0));
            c.set(x, y, alpha << 24 | color);
        });
    }

    /**
     * A 64x64 humanoid skin: head in {@code skin}, body and limbs in {@code cloth}, overlay layers left
     * empty. With {@code eyes}, two pale points where a face would be.
     */
    static void skin(Canvas c, int skin, int cloth, boolean eyes) {
        c.each((x, y) -> c.set(x, y, 0x00000000));
        int[][] skinParts = {{0, 0, 32, 16}};
        int[][] clothParts = {{0, 16, 56, 32}, {16, 48, 48, 64}};
        for (int[] r : skinParts) {
            for (int y = r[1]; y < r[3]; y++) {
                for (int x = r[0]; x < r[2]; x++) {
                    c.set(x, y, 0xFF000000 | c.jitter(skin, 3, x, y));
                }
            }
        }
        for (int[] r : clothParts) {
            for (int y = r[1]; y < r[3]; y++) {
                for (int x = r[0]; x < r[2]; x++) {
                    c.set(x, y, 0xFF000000 | c.jitter(cloth, 5, x, y));
                }
            }
        }
        if (eyes) {
            c.set(10, 11, 0xFFE6DED4);
            c.set(13, 11, 0xFFE6DED4);
        }
    }

    static {
        EXTRA.put("textures/environment/celestial/eye", new Sized(64, TextureArt::eye));
        EXTRA.put("textures/entity/faceless", new Sized(64, c -> skin(c, 0xD9D3C9, 0x6B6762, false)));
        EXTRA.put("textures/entity/stalker", new Sized(64, c -> skin(c, 0x0D0C0C, 0x090808, true)));
        EXTRA.put("textures/entity/lifeguard", new Sized(64, c -> {
            skin(c, 0xC69B74, 0xB3302A, false);
            for (int y = 20; y < 32; y++) {
                for (int x = 16; x < 40; x++) {
                    c.set(x, y, 0xFF000000 | c.jitter(0xE9E2D6, 3, x, y));
                }
            }
        }));
        EXTRA.put("textures/entity/nurse", new Sized(64, c -> {
            skin(c, 0xE6DCD0, 0xD3E2DF, true);
            for (int x = 8; x < 24; x++) {
                c.set(x, 0, 0xFFF2F2EE);
                c.set(x, 1, 0xFFF2F2EE);
            }
        }));
        BLOCKS.put("monolith_stone", c -> c.fill((x, y) -> c.jitter(0x2B2B31, 5, x, y)).each((x, y) -> {
            if (x == 0 || y == 15) {
                c.set(x, y, shade(c.get(x, y), 0.8));
            }
        }));
        BLOCKS.put("cloud", c -> c.fill((x, y) -> mix(0xE3E6F3, 0xFFFFFF, smooth(c.fbm(x, y, 6.0)))));
        BLOCKS.put("temple_marble", c -> c.fill((x, y) -> {
            double vein = Math.abs(Math.sin((x * 0.7 + y * 0.35) + c.fbm(x, y, 5.0) * 3.0));
            return vein < 0.12 ? c.jitter(0xB9B5AC, 3, x, y) : c.jitter(0xEDEAE3, 3, x, y);
        }));
        BLOCKS.put("faded_plaster", c -> c.fill((x, y) -> mix(0xE4DED1, 0xCFC6B4, smooth(c.fbm(x, y, 7.0)) * 0.6)).crack(0xB8AE99, 2));
        BLOCKS.put("fluorescent_light_off", c -> c.fill((x, y) -> {
            int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
            return edge == 0 ? 0x8A8C86 : (edge == 1 ? 0x9FA19A : c.jitter(0xB4B6AE, 2, x, y));
        }));
        BLOCKS.put("pool_tile", c -> tiles(c, 0xF1F7F7, 0xC4D2D3, 4));
        BLOCKS.put("pool_tile_blue", c -> tiles(c, 0x79D3DD, 0x56ADB6, 4));
        BLOCKS.put("pool_light", c -> c.fill((x, y) -> {
            int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
            return edge == 0 ? 0xD5ECF0 : (edge == 1 ? 0xEFFBFF : 0xFFFFFF);
        }));
        BLOCKS.put("wallpaper", c -> c.fill((x, y) -> {
            int base = (x % 4 == 0) ? 0xB8A24C : 0xCAB35B;
            if ((x % 4 == 2) && ((y + (x / 4) * 2) % 6 == 0)) {
                base = 0xDBC76E;
            }
            return mix(base, 0x9E8A3E, smooth(c.fbm(x, y, 9.0)) * 0.35);
        }));
        BLOCKS.put("damp_carpet", c -> c.fill((x, y) -> {
            double damp = smooth(c.fbm(x + 40, y, 6.0));
            return c.jitter(mix(0xB8A15F, 0x8E7A45, damp > 0.62 ? 0.8 : 0.1), 6, x, y);
        }));
        BLOCKS.put("ceiling_tile", c -> c.fill((x, y) -> {
            if (x == 0 || y == 0) {
                return 0xB9B5AC;
            }
            return (x % 3 == 1 && y % 3 == 1 && c.chance(x, y, 0.7)) ? 0xA9A59C : c.jitter(0xDAD7CF, 2, x, y);
        }));
        BLOCKS.put("fluorescent_light", c -> c.fill((x, y) -> {
            if (x == 0 || y == 0 || x == 15 || y == 15) {
                return 0xBFC3C7;
            }
            return (y == 4 || y == 5 || y == 10 || y == 11) ? 0xF4FBFF : 0xFFFFFF;
        }));
        BLOCKS.put("ash", c -> c.fill((x, y) -> {
            int base = c.jitter(0x8E8B88, 6, x, y);
            return c.chance(x, y, 0.08) ? 0x5E5B58 : (c.chance(x + 7, y, 0.05) ? 0xB4B1AD : base);
        }));
        BLOCKS.put("cold_lava", c -> c.fill((x, y) -> c.jitter(0x1D1616, 3, x, y)).crack(0x6A1E12, 4).crack(0xA0381E, 2));
        BLOCKS.put("boiler_plate", c -> c.fill((x, y) -> {
            int base = mix(0x8A4E2C, 0x5E3018, smooth(c.fbm(x, y * 3, 5.0)) * 0.7);
            if ((x == 1 || x == 14) && (y == 1 || y == 14)) {
                return 0xC08A5C;
            }
            return y == 7 ? shade(base, 0.8) : c.jitter(base, 4, x, y);
        }));
        BLOCKS.put("ember", c -> c.fill((x, y) -> {
            double heat = smooth(c.fbm(x, y, 4.0));
            return heat > 0.7 ? 0xFFB54C : (heat > 0.35 ? c.jitter(0xE0581C, 8, x, y) : 0x7A1E0A);
        }));
        BLOCKS.put("star_stone", c -> c.fill((x, y) -> {
            if (c.chance(x, y, 0.05)) {
                return 0xFFFFFF;
            }
            return c.chance(x + 3, y + 9, 0.03) ? 0xB8A8E8 : c.jitter(0xD6D4E0, 4, x, y);
        }));
        BLOCKS.put("grave_stone", c -> c.fill((x, y) -> c.jitter(0x8C8C90, 5, x, y)).crack(0x6A6A6E, 3));
        BLOCKS.put("night_plaster", c -> c.fill((x, y) -> c.jitter(0xE6EBF4, 3, x, y)));
        BLOCKS.put("synapse", c -> c.fill((x, y) -> {
            double dx = x - 7.5;
            double dy = y - 7.5;
            double glow = Math.max(0.0, 1.0 - Math.sqrt(dx * dx + dy * dy) / 8.5);
            return mix(c.jitter(0x8F7F86, 4, x, y), 0xF2E3E6, glow * glow + 0.15 * smooth(c.fbm(x, y, 5.0)));
        }));
        BLOCKS.put("tissue_eye", c -> c.fill((x, y) -> {
            double dx = (x - 7.5) / 7.5;
            double dy = (y - 7.5) / 4.6;
            double r = Math.sqrt(dx * dx + dy * dy);
            if (r > 1.0) {
                return c.jitter(0x5A3A3C, 6, x, y);
            }
            double pupil = Math.sqrt((x - 7.5) * (x - 7.5) + (y - 7.5) * (y - 7.5));
            if (pupil < 1.6) {
                return 0x0A0606;
            }
            if (pupil < 3.2) {
                return mix(0x3B2A1E, 0x6E5A3A, (pupil - 1.6) / 1.6);
            }
            return (x * 5 + y * 3) % 13 == 0 ? 0xB07070 : c.jitter(0xE8E0D2, 3, x, y);
        }));
        BLOCKS.put("mirror_surface", c -> c.fill((x, y) -> {
            boolean glint = (x + y) % 11 == 0 || (x * 2 + y) % 17 == 0;
            return glint ? argb(190, 0xFFFFFF) : argb(130, mix(0xA9DDFB, 0xD8F2FF, smooth(c.fbm(x, y, 8.0))));
        }));
        BLOCKS.put("updraft_vent", c -> c.fill((x, y) -> {
            if (x == 0 || y == 0 || x == 15 || y == 15) {
                return 0x8A8E94;
            }
            return (y % 3 == 0) ? 0x9FA4AA : 0x34383E;
        }));
        BLOCKS.put("elevator", c -> c.fill((x, y) -> {
            if (x == 0 || y == 0 || x == 15 || y == 15) {
                return 0x6E7279;
            }
            boolean arrowUp = y >= 2 && y <= 6 && Math.abs(x - 7.5) <= (y - 1) * 0.6;
            boolean arrowDown = y >= 9 && y <= 13 && Math.abs(x - 7.5) <= (14 - y) * 0.6;
            return arrowUp || arrowDown ? 0xF2E6A0 : c.jitter(0xA6AAB0, 3, x, y);
        }));
        doorSet("backrooms_door", 0xCDB98A, 0x9D8A5E, 0x5A4A2A);
        doorSet("exit_door", 0xF0EEE8, 0xC8C4BA, 0x8A8A8A);
        doorSet("wake_door", 0x5F78C8, 0x3E55A0, 0xF4E6A0);
        doorSet("closed_door", 0x26262A, 0x18181B, 0x3A3A40);
        BLOCKS.put("mirror_frame", c -> c.fill((x, y) -> {
            int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
            return edge <= 1 ? 0xD8DCE2 : (edge == 2 ? 0xA9AFB8 : c.jitter(0xEEF1F4, 2, x, y));
        }));
        BLOCKS.put("mirror_portal", c -> c.fill((x, y) -> {
            double wave = Math.sin(x * 0.8 + c.fbm(x, y, 4.0) * 4.0) * 0.5 + 0.5;
            return argb(180, mix(0xCFE9FF, 0xFFFFFF, wave));
        }));

        BLOCKS.put("steam_vent", c -> c.fill((x, y) -> {
            int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
            if (edge == 0) {
                return 0x3A3633;
            }
            return x % 3 == 0 ? 0x1C1A19 : c.jitter(0x6E6660, 4, x, y);
        }));
        BLOCKS.put("television_front", c -> c.fill((x, y) -> {
            boolean screen = x >= 2 && x <= 11 && y >= 2 && y <= 11 && !((x == 2 || x == 11) && (y == 2 || y == 11));
            if (screen) {
                return x + y < 7 ? 0x3A4448 : c.jitter(0x1A1F22, 3, x, y);
            }
            if (x == 13 && (y == 3 || y == 6)) {
                return 0x9A8F80;
            }
            if (x >= 13 && x <= 14 && y >= 9 && y <= 13 && y % 2 == 1) {
                return 0x2E2A26;
            }
            return c.jitter(0x5A534B, 3, x, y);
        }));
        BLOCKS.put("television_front_on", c -> c.fill((x, y) -> {
            boolean screen = x >= 2 && x <= 11 && y >= 2 && y <= 11 && !((x == 2 || x == 11) && (y == 2 || y == 11));
            if (screen) {
                return mix(0x7F96A8, 0xE4ECF2, Hash.unit(Hash.of(c.seed, x, y, 0x5A0)));
            }
            if (x == 13 && (y == 3 || y == 6)) {
                return 0x9A8F80;
            }
            if (x >= 13 && x <= 14 && y >= 9 && y <= 13 && y % 2 == 1) {
                return 0x2E2A26;
            }
            return c.jitter(0x5A534B, 3, x, y);
        }));
        BLOCKS.put("television_side", c -> c.fill((x, y) -> {
            double grain = Math.sin(y * 1.3 + c.fbm(x, y, 3.0) * 3.0);
            return c.jitter(grain > 0.6 ? 0x5C4231 : 0x6B4F3A, 3, x, y);
        }));
        BLOCKS.put("television_top", c -> c.fill((x, y) -> c.jitter(0x544D46, 3, x, y)));
        BLOCKS.put("telephone", c -> c.fill((x, y) -> c.jitter(0xD9CBB0, 3, x, y)));
        BLOCKS.put("telephone_dial", c -> c.fill((x, y) -> {
            double dx = x - 7.5;
            double dy = y - 7.5;
            double r = Math.sqrt(dx * dx + dy * dy);
            boolean hole = r > 3.5 && r < 6.0 && Math.floorMod((int) Math.round(Math.atan2(dy, dx) / (Math.PI * 2) * 10), 10) != 9
                    && Math.abs(r - 4.8) < 0.8 && (x + y) % 2 == 0;
            return hole ? 0x3B3530 : (r < 2.2 ? 0xEFE7D6 : c.jitter(0xD3C4A6, 2, x, y));
        }));
        BLOCKS.put("clock_rim", c -> c.fill((x, y) -> c.jitter(0x4A3424, 4, x, y)));
        BLOCKS.put("clock_face", c -> {
            c.fill((x, y) -> {
                double dx = x + 0.5 - 8.0;
                double dy = y + 0.5 - 8.0;
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r > 7.2) {
                    return 0x4A3424;
                }
                if (r > 6.4) {
                    return 0x2B2018;
                }
                boolean mark = r > 5.0 && Math.abs(Math.sin(Math.atan2(dy, dx) * 6.0)) < 0.18;
                return mark ? 0x3A332C : c.jitter(0xEDE7D8, 2, x, y);
            });
            double hour = Math.toRadians((3 + 17 / 60.0) * 30.0);
            double minute = Math.toRadians(17 * 6.0);
            for (double t = 0.0; t <= 1.0; t += 0.05) {
                c.set((int) Math.floor(8.0 + Math.sin(hour) * 3.4 * t), (int) Math.floor(8.0 - Math.cos(hour) * 3.4 * t), 0xFF1E1A16);
                c.set((int) Math.floor(8.0 + Math.sin(minute) * 5.6 * t), (int) Math.floor(8.0 - Math.cos(minute) * 5.6 * t), 0xFF1E1A16);
            }
        });
        BLOCKS.put("hospital_bed_sheet", c -> c.fill((x, y) -> mix(0xF2F4F4, 0xD3D9DC, smooth(c.fbm(x, y, 4.0)) * 0.7)));
        BLOCKS.put("hospital_bed_frame", c -> c.fill((x, y) -> {
            int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
            return edge == 0 ? 0x7E868C : c.jitter(0xA9B0B5, 2, x, y);
        }));
        BLOCKS.put("hospital_bed_pillow", c -> c.fill((x, y) -> mix(0xF7F7F2, 0xDEDFD8, smooth(c.fbm(x, y, 3.0)) * 0.5)));
        BLOCKS.put("iv_metal", c -> c.fill((x, y) -> c.jitter((x + y) % 5 == 0 ? 0xE6E9EC : 0xBFC4C9, 2, x, y)));
        BLOCKS.put("iv_bag", c -> c.each((x, y) -> c.set(x, y, argb(y > 10 ? 150 : 96, y > 10 ? 0xE9D9A6 : 0xDCE8EE))));
        BLOCKS.put("monitor_case", c -> c.fill((x, y) -> c.jitter(0xBFC2B8, 3, x, y)));
        BLOCKS.put("monitor_screen", c -> c.fill((x, y) -> {
            int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
            if (edge <= 1) {
                return 0xBFC2B8;
            }
            int[] trace = {9, 9, 9, 9, 8, 3, 12, 9, 9, 9, 9, 9, 9, 9};
            int line = trace[Math.max(0, Math.min(trace.length - 1, x - 1))];
            return Math.abs(y - line) <= (x == 6 || x == 7 ? 3 : 0) && y >= Math.min(line, 9) - (x == 6 ? 6 : 0) ? 0x3CFF7A : 0x070A08;
        }));
        BLOCKS.put("chair_plastic", c -> c.fill((x, y) -> c.jitter(0xC9792F, 4, x, y)));
        BLOCKS.put("chair_metal", c -> c.fill((x, y) -> c.jitter(0x7D8084, 3, x, y)));
        BLOCKS.put("telescope_brass", c -> c.fill((x, y) -> c.jitter(y % 6 == 0 ? 0xD9B262 : 0xB08A45, 4, x, y)));
        BLOCKS.put("telescope_lens", c -> c.fill((x, y) -> {
            double dx = x - 7.5;
            double dy = y - 7.5;
            if (dx * dx + dy * dy > 42.0) {
                return 0xB08A45;
            }
            return x == 5 && y == 5 ? 0xFFFFFF : mix(0x1E2A44, 0x3B4E7A, smooth(c.fbm(x, y, 3.0)));
        }));

        ITEMS.put("mirror_shard", c -> c.each((x, y) -> {
            boolean inside = Math.abs(x - 7.5) + Math.abs(y - 7.5) * 0.6 < 6.5 && x > 3 && x < 12;
            if (inside) {
                c.set(x, y, (x + y) % 5 == 0 ? 0xFFFFFFFF : argb(255, mix(0xA9DDFB, 0xE8F6FF, (y / 15.0))));
            }
        }));
        ITEMS.put("memory_fragment", c -> c.each((x, y) -> {
            boolean paper = x >= 3 && x <= 12 && y >= 2 && y <= 13 && !(x == 12 && y <= 4);
            if (paper) {
                int color = (y % 3 == 0 && x > 4 && x < 11) ? 0xFF8E8676 : 0xFFE9E1CC;
                c.set(x, y, color);
            }
        }));
        ITEMS.put("dream_journal", c -> c.each((x, y) -> {
            boolean cover = x >= 3 && x <= 12 && y >= 2 && y <= 13;
            if (!cover) {
                return;
            }
            boolean spine = x <= 4;
            boolean pages = x == 12 && y >= 3 && y <= 12;
            boolean label = x >= 6 && x <= 10 && y >= 5 && y <= 7;
            int color = spine ? 0xFF3A2A33 : pages ? 0xFFE8E0CC : label ? 0xFFD9CFB8 : 0xFF000000 | c.jitter(0x5A3F4E, 4, x, y);
            c.set(x, y, color);
        }));
        ITEMS.put("vhs_tape", c -> c.each((x, y) -> {
            boolean shell = x >= 1 && x <= 14 && y >= 4 && y <= 12;
            if (!shell) {
                return;
            }
            boolean label = x >= 3 && x <= 12 && y >= 5 && y <= 7;
            boolean window = x >= 5 && x <= 10 && y >= 9 && y <= 11;
            boolean reel = window && (x == 6 || x == 9) && y == 10;
            int color = label ? 0xFFE6DFCF : reel ? 0xFF6E5A4A : window ? 0xFF2E2A2C : 0xFF000000 | c.jitter(0x1C1B1E, 4, x, y);
            if (label && y == 6 && x >= 4 && x <= 10 && (x % 2 == 0)) {
                color = 0xFF5A5C8A;
            }
            c.set(x, y, color);
        }));
        ITEMS.put("glass_marble", c -> c.each((x, y) -> {
            double dx = x - 7.5;
            double dy = y - 7.5;
            double r = Math.sqrt(dx * dx + dy * dy);
            if (r > 5.2) {
                return;
            }
            double swirl = Math.sin(Math.atan2(dy, dx) * 2.0 + r * 1.1);
            int color = swirl > 0.55 ? 0xE4785A : mix(0x8FC8DE, 0xD8F0F8, (7.5 - dy) / 12.0);
            if (x == 5 && y == 5) {
                color = 0xFFFFFF;
            }
            c.set(x, y, 0xE0000000 | color);
        }));
        ITEMS.put("child_drawing", c -> {
            c.each((x, y) -> c.set(x, y, x >= 1 && x <= 14 && y >= 2 && y <= 13 ? 0xFFF4EFE2 : 0x00000000));
            int[][] red = {{3, 8}, {4, 7}, {5, 6}, {6, 7}, {7, 8}, {3, 9}, {7, 9}, {3, 10}, {7, 10}, {3, 11}, {4, 11}, {5, 11}, {6, 11}, {7, 11}};
            for (int[] p : red) {
                c.set(p[0], p[1], 0xFFC0392B);
            }
            for (int[] p : new int[][]{{11, 3}, {12, 3}, {11, 4}, {12, 4}}) {
                c.set(p[0], p[1], 0xFFF1C40F);
            }
            for (int[] p : new int[][]{{9, 9}, {9, 10}, {9, 11}, {11, 9}, {11, 10}, {11, 11}}) {
                c.set(p[0], p[1], 0xFF34495E);
            }
            c.set(13, 10, 0xFF111111);
            c.set(13, 11, 0xFF111111);
        });
        ITEMS.put("lucid_tea", c -> c.each((x, y) -> {
            boolean cup = x >= 4 && x <= 11 && y >= 6 && y <= 13;
            boolean handle = (x == 12 || x == 13) && y >= 8 && y <= 11;
            if (cup || handle) {
                c.set(x, y, y == 6 ? 0xFF8A6A3A : (cup && y < 9 && x > 4 && x < 11 ? 0xFFB08A4A : 0xFFEDE7DA));
            }
            if (y <= 4 && (x == 6 || x == 9) && (y + x) % 2 == 0) {
                c.set(x, y, 0x88FFFFFF);
            }
        }));
    }

    private TextureArt() {
    }

    static void tiles(Canvas c, int tile, int grout, int size) {
        c.fill((x, y) -> {
            if (x % size == 0 || y % size == 0) {
                return grout;
            }
            long h = Hash.of(c.seed, x / size, y / size);
            return shade(tile, 0.96 + Hash.unit(h) * 0.06);
        });
    }

    static void doorSet(String name, int panel, int frame, int knob) {
        BLOCKS.put(name + "_top", c -> door(c, panel, frame, knob, true));
        BLOCKS.put(name + "_bottom", c -> door(c, panel, frame, knob, false));
        ITEMS.put(name, c -> c.each((x, y) -> {
            if (x >= 4 && x <= 11 && y >= 1 && y <= 14) {
                boolean edge = x == 4 || x == 11 || y == 1;
                c.set(x, y, 0xFF000000 | (edge ? frame : (x == 10 && y == 8 ? knob : panel)));
            }
        }));
    }

    static void door(Canvas c, int panel, int frame, int knob, boolean top) {
        c.fill((x, y) -> {
            if (x <= 1 || x >= 14 || (top && y <= 0) || (!top && y >= 15)) {
                return frame;
            }
            if (!top && x == 11 && (y == 1 || y == 2)) {
                return knob;
            }
            boolean inset = (x >= 4 && x <= 11) && (top ? (y >= 2 && y <= 13) : (y >= 4 && y <= 13));
            return inset ? shade(panel, 0.9) : c.jitter(panel, 2, x, y);
        });
    }

    static int argb(int alpha, int rgb) {
        return (alpha & 0xFF) << 24 | (rgb & 0xFFFFFF);
    }

    static int shade(int color, double factor) {
        int a = color >>> 24;
        int r = (int) Math.min(255, ((color >> 16) & 0xFF) * factor);
        int g = (int) Math.min(255, ((color >> 8) & 0xFF) * factor);
        int b = (int) Math.min(255, (color & 0xFF) * factor);
        return a << 24 | r << 16 | g << 8 | b;
    }

    static int mix(int a, int b, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        int r = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (a & 0xFF000000) | r << 16 | g << 8 | bl;
    }

    static double smooth(double v) {
        return Math.max(0.0, Math.min(1.0, v * 0.5 + 0.5));
    }

    interface Painter {
        int color(int x, int y);
    }

    interface Visitor {
        void visit(int x, int y);
    }

    static final class Canvas {
        final BufferedImage image;
        final long seed;

        Canvas(String name, int size) {
            this.image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            this.seed = Hash.salt(name);
        }

        int size() {
            return this.image.getWidth();
        }

        Canvas fill(Painter painter) {
            for (int y = 0; y < this.size(); y++) {
                for (int x = 0; x < this.size(); x++) {
                    int color = painter.color(x, y);
                    this.set(x, y, (color >>> 24) == 0 ? (0xFF000000 | color) : color);
                }
            }
            return this;
        }

        Canvas each(Visitor visitor) {
            for (int y = 0; y < this.size(); y++) {
                for (int x = 0; x < this.size(); x++) {
                    visitor.visit(x, y);
                }
            }
            return this;
        }

        /** Random-walk cracks of the given colour. */
        Canvas crack(int color, int count) {
            for (int i = 0; i < count; i++) {
                long h = Hash.of(this.seed, i, 0xC4AC);
                int x = Hash.range(h, 0, this.size() - 1);
                int y = Hash.range(Hash.next(h, 1), 0, this.size() - 1);
                for (int step = 0; step < 9; step++) {
                    this.set(Math.floorMod(x, this.size()), Math.floorMod(y, this.size()), 0xFF000000 | color);
                    long s = Hash.next(h, step + 2);
                    x += Hash.range(s, -1, 1);
                    y += 1;
                }
            }
            return this;
        }

        void set(int x, int y, int argb) {
            this.image.setRGB(x, y, argb);
        }

        int get(int x, int y) {
            return this.image.getRGB(x, y);
        }

        int jitter(int rgb, int amount, int x, int y) {
            double t = Hash.unit(Hash.of(this.seed, x, y)) * 2.0 - 1.0;
            return shade(0xFF000000 | rgb, 1.0 + t * amount / 100.0 * 2.0);
        }

        boolean chance(int x, int y, double probability) {
            return Hash.chance(Hash.of(this.seed, x, y, 0x5EC), probability);
        }

        /** Tileable fractal noise in [-1, 1]. */
        double fbm(int x, int y, double scale) {
            int s = this.size();
            double total = 0.0;
            double norm = 0.0;
            double amp = 1.0;
            for (int o = 0; o < 3; o++) {
                int period = Math.max(1, (int) (s / scale) << o);
                double fx = x * period / (double) s;
                double fy = y * period / (double) s;
                int x0 = (int) Math.floor(fx);
                int y0 = (int) Math.floor(fy);
                double tx = fx - x0;
                double ty = fy - y0;
                double a = this.lattice(x0, y0, period, o);
                double b = this.lattice(x0 + 1, y0, period, o);
                double cc = this.lattice(x0, y0 + 1, period, o);
                double d = this.lattice(x0 + 1, y0 + 1, period, o);
                tx = tx * tx * (3 - 2 * tx);
                ty = ty * ty * (3 - 2 * ty);
                double v = (a + (b - a) * tx) + ((cc + (d - cc) * tx) - (a + (b - a) * tx)) * ty;
                total += v * amp;
                norm += amp;
                amp *= 0.5;
            }
            return total / norm;
        }

        private double lattice(int x, int y, int period, int octave) {
            return Hash.unit(Hash.of(this.seed + octave, Math.floorMod(x, period), Math.floorMod(y, period))) * 2.0 - 1.0;
        }
    }
}
