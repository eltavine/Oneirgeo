package com.eltavine.oneirgeo.util;

/**
 * Stateless positional hashing. Worldgen must be a pure function of (seed, position) so that every
 * chunk can rebuild the parts of a structure that cross into it.
 */
public final class Hash {
    private Hash() {
    }

    public static long mix(long x) {
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;
        return x;
    }

    public static long of(long seed, long a) {
        return mix(seed ^ mix(a * 0x9E3779B97F4A7C15L));
    }

    public static long of(long seed, long a, long b) {
        return mix(seed ^ mix(a * 0x9E3779B97F4A7C15L ^ mix(b * 0xC2B2AE3D27D4EB4FL)));
    }

    public static long of(long seed, long a, long b, long c) {
        return mix(seed ^ mix(a * 0x9E3779B97F4A7C15L ^ mix(b * 0xC2B2AE3D27D4EB4FL ^ mix(c * 0x165667B19E3779F9L))));
    }

    public static long of(long seed, long a, long b, long c, long d) {
        return of(of(seed, a, b, c), d);
    }

    public static long salt(String name) {
        long h = 1125899906842597L;
        for (int i = 0; i < name.length(); i++) {
            h = 31 * h + name.charAt(i);
        }
        return mix(h);
    }

    /** Uniform double in [0, 1). */
    public static double unit(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    /** Uniform int in [min, max]. */
    public static int range(long hash, int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + (int) Math.floorMod(hash, (long) (max - min + 1));
    }

    public static boolean chance(long hash, double probability) {
        return unit(hash) < probability;
    }

    /** Derives an independent hash stream from an existing one. */
    public static long next(long hash, int index) {
        return mix(hash + 0x9E3779B97F4A7C15L * (index + 1));
    }
}
