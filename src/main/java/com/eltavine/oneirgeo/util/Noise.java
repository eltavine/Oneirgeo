package com.eltavine.oneirgeo.util;

/** Small seeded value-noise helpers with smoothstep interpolation; output range is roughly [-1, 1]. */
public final class Noise {
    private Noise() {
    }

    private static double lattice(long seed, long x, long z) {
        return Hash.unit(Hash.of(seed, x, z)) * 2.0 - 1.0;
    }

    private static double lattice(long seed, long x, long y, long z) {
        return Hash.unit(Hash.of(seed, x, y, z)) * 2.0 - 1.0;
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    public static double value2(long seed, double x, double z) {
        long x0 = (long) Math.floor(x);
        long z0 = (long) Math.floor(z);
        double tx = smooth(x - x0);
        double tz = smooth(z - z0);
        double a = lattice(seed, x0, z0);
        double b = lattice(seed, x0 + 1, z0);
        double c = lattice(seed, x0, z0 + 1);
        double d = lattice(seed, x0 + 1, z0 + 1);
        double ab = a + (b - a) * tx;
        double cd = c + (d - c) * tx;
        return ab + (cd - ab) * tz;
    }

    public static double value3(long seed, double x, double y, double z) {
        long x0 = (long) Math.floor(x);
        long y0 = (long) Math.floor(y);
        long z0 = (long) Math.floor(z);
        double tx = smooth(x - x0);
        double ty = smooth(y - y0);
        double tz = smooth(z - z0);
        double c000 = lattice(seed, x0, y0, z0);
        double c100 = lattice(seed, x0 + 1, y0, z0);
        double c010 = lattice(seed, x0, y0 + 1, z0);
        double c110 = lattice(seed, x0 + 1, y0 + 1, z0);
        double c001 = lattice(seed, x0, y0, z0 + 1);
        double c101 = lattice(seed, x0 + 1, y0, z0 + 1);
        double c011 = lattice(seed, x0, y0 + 1, z0 + 1);
        double c111 = lattice(seed, x0 + 1, y0 + 1, z0 + 1);
        double x00 = c000 + (c100 - c000) * tx;
        double x10 = c010 + (c110 - c010) * tx;
        double x01 = c001 + (c101 - c001) * tx;
        double x11 = c011 + (c111 - c011) * tx;
        double y0v = x00 + (x10 - x00) * ty;
        double y1v = x01 + (x11 - x01) * ty;
        return y0v + (y1v - y0v) * tz;
    }

    /** Fractal sum of {@link #value2}; {@code scale} is the wavelength of the first octave in blocks. */
    public static double fbm2(long seed, double x, double z, double scale, int octaves) {
        double sum = 0.0;
        double amplitude = 1.0;
        double norm = 0.0;
        double frequency = 1.0 / scale;
        for (int o = 0; o < octaves; o++) {
            sum += value2(seed + o * 7919L, x * frequency, z * frequency) * amplitude;
            norm += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        return sum / norm;
    }

    public static double fbm3(long seed, double x, double y, double z, double scale, int octaves) {
        double sum = 0.0;
        double amplitude = 1.0;
        double norm = 0.0;
        double frequency = 1.0 / scale;
        for (int o = 0; o < octaves; o++) {
            sum += value3(seed + o * 7919L, x * frequency, y * frequency, z * frequency) * amplitude;
            norm += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        return sum / norm;
    }
}
