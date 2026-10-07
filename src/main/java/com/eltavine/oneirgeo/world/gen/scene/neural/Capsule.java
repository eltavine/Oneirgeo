package com.eltavine.oneirgeo.world.gen.scene.neural;

/**
 * One straight piece of tissue: a capsule from a to b whose radius tapers from {@code ra} to {@code rb}.
 * Hollow pieces ({@code inner > 0}) keep a shell of tissue around an empty bore.
 * <p>
 * Neurons keep their capsules packed in arrays; this is a reusable view of one of them, filled by
 * {@link #load}.
 */
final class Capsule {
    static final int TISSUE = 0;
    static final int SOMA = 1;
    static final int BOUTON = 2;
    /**
     * Period {@code phase} of a tube that repeats exactly every {@code (px, py, pz)}; its blocks repeat
     * with it, so the tube is the same wherever you stand in it.
     */
    static final int LOOP = 3;
    /** A piece of an axon: grown like any tissue, but its bore carries a current along it, from a towards b. */
    static final int AXON = 4;
    /** Floats stored per capsule: ax, ay, az, bx, by, bz, ra, rb, inner. */
    static final int STRIDE = 9;

    float ax;
    float ay;
    float az;
    float bx;
    float by;
    float bz;
    float ra;
    float rb;
    float inner;
    int kind;
    int px;
    int py;
    int pz;
    int phase;
    int minX;
    int minY;
    int minZ;
    int maxX;
    int maxY;
    int maxZ;

    /** Points this view at capsule {@code i} of a neuron. */
    Capsule load(Neuron neuron, int i) {
        float[] g = neuron.geometry;
        int o = i * STRIDE;
        this.ax = g[o];
        this.ay = g[o + 1];
        this.az = g[o + 2];
        this.bx = g[o + 3];
        this.by = g[o + 4];
        this.bz = g[o + 5];
        this.ra = g[o + 6];
        this.rb = g[o + 7];
        this.inner = g[o + 8];
        this.kind = neuron.kinds[i];
        if (this.kind == LOOP) {
            Neuron.Loop loop = neuron.loops.getFirst();
            this.px = loop.vx();
            this.py = loop.vy();
            this.pz = loop.vz();
            this.phase = (i - neuron.loopStart) >> 1;
        } else {
            this.px = 0;
            this.py = 0;
            this.pz = 0;
            this.phase = 0;
        }
        float r = Math.max(this.ra, this.rb);
        this.minX = (int) Math.floor(Math.min(this.ax, this.bx) - r);
        this.minY = (int) Math.floor(Math.min(this.ay, this.by) - r);
        this.minZ = (int) Math.floor(Math.min(this.az, this.bz) - r);
        this.maxX = (int) Math.ceil(Math.max(this.ax, this.bx) + r);
        this.maxY = (int) Math.ceil(Math.max(this.ay, this.by) + r);
        this.maxZ = (int) Math.ceil(Math.max(this.az, this.bz) + r);
        return this;
    }

    boolean intersects(int x0, int y0, int z0, int x1, int y1, int z1) {
        return this.maxX >= x0 && this.minX <= x1 && this.maxY >= y0 && this.minY <= y1 && this.maxZ >= z0 && this.minZ <= z1;
    }

    /** The capsules of a growing neuron, appended in order into packed arrays. */
    static final class Builder {
        private float[] geometry = new float[STRIDE * 64];
        private byte[] kinds = new byte[64];
        private int size;
        /** Index of the first loop capsule, or -1. */
        int loopStart = -1;

        int size() {
            return this.size;
        }

        void tissue(float ax, float ay, float az, float bx, float by, float bz, float ra, float rb, float inner) {
            this.add(ax, ay, az, bx, by, bz, ra, rb, inner, TISSUE);
        }

        void axon(float ax, float ay, float az, float bx, float by, float bz, float ra, float rb, float inner) {
            this.add(ax, ay, az, bx, by, bz, ra, rb, inner, AXON);
        }

        void sphere(float x, float y, float z, float r, float inner, int kind) {
            this.add(x, y, z, x, y, z, r, r, inner, kind);
        }

        /** Loop capsules come in pairs per period, all of one neuron's loop in a row. */
        void loop(float ax, float ay, float az, float bx, float by, float bz, float r, float inner) {
            if (this.loopStart < 0) {
                this.loopStart = this.size;
            }
            this.add(ax, ay, az, bx, by, bz, r, r, inner, LOOP);
        }

        private void add(float ax, float ay, float az, float bx, float by, float bz, float ra, float rb, float inner, int kind) {
            if (this.size == this.kinds.length) {
                this.kinds = java.util.Arrays.copyOf(this.kinds, this.size * 2);
                this.geometry = java.util.Arrays.copyOf(this.geometry, this.size * 2 * STRIDE);
            }
            int o = this.size * STRIDE;
            float[] g = this.geometry;
            g[o] = ax;
            g[o + 1] = ay;
            g[o + 2] = az;
            g[o + 3] = bx;
            g[o + 4] = by;
            g[o + 5] = bz;
            g[o + 6] = ra;
            g[o + 7] = rb;
            g[o + 8] = inner;
            this.kinds[this.size++] = (byte) kind;
        }

        float[] geometry() {
            return java.util.Arrays.copyOf(this.geometry, this.size * STRIDE);
        }

        byte[] kinds() {
            return java.util.Arrays.copyOf(this.kinds, this.size);
        }
    }
}
