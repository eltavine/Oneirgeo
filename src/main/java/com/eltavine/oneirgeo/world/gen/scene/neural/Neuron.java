package com.eltavine.oneirgeo.world.gen.scene.neural;

import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.util.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

/**
 * A neuron grown to the size of a building, or of a mountain: a soma, dendrites that fork down to
 * glowing boutons, and an axon in myelin beads that reaches into a neighbouring cell. Thick, level
 * branches are hollow and can be walked through.
 */
final class Neuron {
    static final Neuron NONE = new Neuron(0, 0, 0, 0, 0, new Capsule.Builder(), new int[0], List.of());
    private static final float MIN_RADIUS = 1.0F;
    private static final float SHELL = 1.4F;
    static final int STRAND_MAX = 28;
    private static final float PIECE = 10.0F;
    static final int LOOP_PERIOD = 12;
    static final int LOOP_PERIODS = 22;
    static final int LOOP_SEAM_AT = 10;
    private static final float LOOP_BORE = 2.2F;

    /** The axon whose bore repeats every {@code v}; walking its length you never get anywhere. */
    record Loop(float sx, float sy, float sz, int vx, int vy, int vz) {
        SeamVolume seam() {
            float cx = this.sx + (LOOP_SEAM_AT + 0.35F) * this.vx;
            float cy = this.sy + (LOOP_SEAM_AT + 0.35F) * this.vy;
            float cz = this.sz + (LOOP_SEAM_AT + 0.35F) * this.vz;
            double length = Math.sqrt(this.vx * this.vx + this.vy * this.vy + this.vz * this.vz);
            boolean alongX = Math.abs(this.vx) >= Math.abs(this.vz);
            double spread = LOOP_BORE * length / Math.max(1, alongX ? Math.abs(this.vx) : Math.abs(this.vz)) + 1.0;
            int minY = (int) Math.floor(cy - LOOP_BORE + 1.0);
            int maxY = (int) Math.floor(cy + LOOP_BORE) + 1;
            Box trigger;
            Direction moving;
            if (alongX) {
                int x = (int) Math.floor(cx);
                trigger = new Box(x, minY, (int) Math.floor(cz - spread), x + 1, maxY, (int) Math.ceil(cz + spread));
                moving = this.vx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                int z = (int) Math.floor(cz);
                trigger = new Box((int) Math.floor(cx - spread), minY, z, (int) Math.ceil(cx + spread), maxY, z + 1);
                moving = this.vz > 0 ? Direction.SOUTH : Direction.NORTH;
            }
            return SeamVolume.translate(trigger, -this.vx, -this.vy, -this.vz, moving);
        }

        Box bounds() {
            float ex = this.sx + LOOP_PERIODS * this.vx;
            float ey = this.sy + LOOP_PERIODS * this.vy;
            float ez = this.sz + LOOP_PERIODS * this.vz;
            int r = 5;
            return new Box((int) Math.floor(Math.min(this.sx, ex)) - r, (int) Math.floor(Math.min(this.sy, ey)) - r,
                    (int) Math.floor(Math.min(this.sz, ez)) - r, (int) Math.ceil(Math.max(this.sx, ex)) + r,
                    (int) Math.ceil(Math.max(this.sy, ey)) + r, (int) Math.ceil(Math.max(this.sz, ez)) + r);
        }
    }

    /** Where a neuron of a cell sits and how big its soma is; cheap enough to ask about neighbours. */
    record Header(float x, float y, float z, int radius, long hash) {
    }

    final float x;
    final float y;
    final float z;
    final float radius;
    /** Radius of the soma's empty inside, 0 when solid. */
    final float hollow;
    /** Capsules packed {@link Capsule#STRIDE} floats apiece; read them through {@link Capsule#load}. */
    final float[] geometry;
    final byte[] kinds;
    /** Index of the first capsule of the loop, -1 without one. */
    final int loopStart;
    /** Glowing blocks, four ints apiece: x, y, z and {@link #GLOW} or {@link #EYE}. */
    final int[] glows;
    final List<Loop> loops;
    final int minX;
    final int minY;
    final int minZ;
    final int maxX;
    final int maxY;
    final int maxZ;
    /**
     * Which capsules reach which chunk column. Columns are numbered from the neuron's own corner
     * ({@code (cx - chunkX0) * chunkDepth + (cz - chunkZ0)}); {@link #columns} lists the numbers in
     * ascending order, {@link #starts} where each one's capsule indices begin in {@link #listed}.
     */
    private final int chunkX0;
    private final int chunkZ0;
    private final int chunkDepth;
    private final int[] columns;
    private final int[] starts;
    private final char[] listed;

    Neuron(float x, float y, float z, float radius, float hollow, Capsule.Builder capsules, int[] glows, List<Loop> loops) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.hollow = hollow;
        this.geometry = capsules.geometry();
        this.kinds = capsules.kinds();
        this.loopStart = capsules.loopStart;
        this.glows = glows;
        this.loops = loops;
        int count = this.kinds.length;
        if (count > Character.MAX_VALUE + 1) {
            throw new IllegalStateException("A neuron of " + count + " capsules is too large to index");
        }
        int x0 = Integer.MAX_VALUE;
        int y0 = Integer.MAX_VALUE;
        int z0 = Integer.MAX_VALUE;
        int x1 = Integer.MIN_VALUE;
        int y1 = Integer.MIN_VALUE;
        int z1 = Integer.MIN_VALUE;
        Capsule c = new Capsule();
        int[] ranges = new int[count * 4];
        for (int i = 0; i < count; i++) {
            c.load(this, i);
            x0 = Math.min(x0, c.minX);
            y0 = Math.min(y0, c.minY);
            z0 = Math.min(z0, c.minZ);
            x1 = Math.max(x1, c.maxX);
            y1 = Math.max(y1, c.maxY);
            z1 = Math.max(z1, c.maxZ);
            ranges[i * 4] = c.minX >> 4;
            ranges[i * 4 + 1] = c.maxX >> 4;
            ranges[i * 4 + 2] = c.minZ >> 4;
            ranges[i * 4 + 3] = c.maxZ >> 4;
        }
        this.minX = x0;
        this.minY = y0;
        this.minZ = z0;
        this.maxX = x1;
        this.maxY = y1;
        this.maxZ = z1;
        this.chunkX0 = x0 >> 4;
        this.chunkZ0 = z0 >> 4;
        this.chunkDepth = count == 0 ? 1 : (z1 >> 4) - this.chunkZ0 + 1;
        int pairs = 0;
        for (int i = 0; i < count; i++) {
            pairs += (ranges[i * 4 + 1] - ranges[i * 4] + 1) * (ranges[i * 4 + 3] - ranges[i * 4 + 2] + 1);
        }
        long[] codes = new long[pairs];
        int n = 0;
        for (int i = 0; i < count; i++) {
            for (int cx = ranges[i * 4]; cx <= ranges[i * 4 + 1]; cx++) {
                for (int cz = ranges[i * 4 + 2]; cz <= ranges[i * 4 + 3]; cz++) {
                    codes[n++] = (long) ((cx - this.chunkX0) * this.chunkDepth + (cz - this.chunkZ0)) << 16 | i;
                }
            }
        }
        Arrays.sort(codes);
        int distinct = 0;
        for (int k = 0; k < pairs; k++) {
            if (k == 0 || codes[k] >>> 16 != codes[k - 1] >>> 16) {
                distinct++;
            }
        }
        this.columns = new int[distinct];
        this.starts = new int[distinct + 1];
        this.listed = new char[pairs];
        int column = -1;
        for (int k = 0; k < pairs; k++) {
            if (k == 0 || codes[k] >>> 16 != codes[k - 1] >>> 16) {
                column++;
                this.columns[column] = (int) (codes[k] >>> 16);
                this.starts[column] = k;
            }
            this.listed[k] = (char) (codes[k] & 0xFFFF);
        }
        this.starts[distinct] = pairs;
    }

    int capsuleCount() {
        return this.kinds.length;
    }

    /**
     * Where the capsules that may reach a chunk column are listed, or -1 when none do: they are
     * {@link #listed} at positions {@link #slotStart} (inclusive) to {@link #slotEnd}, in the order
     * they were grown.
     */
    int slot(int chunkX, int chunkZ) {
        int lx = chunkX - this.chunkX0;
        int lz = chunkZ - this.chunkZ0;
        if (lx < 0 || lz < 0 || lz >= this.chunkDepth) {
            return -1;
        }
        int k = Arrays.binarySearch(this.columns, lx * this.chunkDepth + lz);
        return k < 0 ? -1 : k;
    }

    int slotStart(int slot) {
        return this.starts[slot];
    }

    int slotEnd(int slot) {
        return this.starts[slot + 1];
    }

    int listed(int position) {
        return this.listed[position];
    }

    /** Bore radius of capsule {@code i}, without loading it. */
    float inner(int i) {
        return this.geometry[i * Capsule.STRIDE + 8];
    }

    boolean intersects(int x0, int y0, int z0, int x1, int y1, int z1) {
        return this != NONE && this.maxX >= x0 && this.minX <= x1 && this.maxY >= y0 && this.minY <= y1 && this.maxZ >= z0 && this.minZ <= z1;
    }

    static @Nullable Header header(long seed, NeuralNetwork.Tier tier, int cx, int cy, int cz) {
        long h = Hash.of(Hash.of(seed, tier.index(), 0x4E0), cx, cy, cz);
        if (!Hash.chance(h, tier.chance())) {
            return null;
        }
        int r = Hash.range(Hash.next(h, 1), tier.somaMin(), tier.somaMax());
        int margin = Math.min(r + 2, tier.cell() / 2 - 1);
        int x = cx * tier.cell() + Hash.range(Hash.next(h, 2), margin, tier.cell() - margin);
        int y = cy * tier.cell() + Hash.range(Hash.next(h, 3), margin, tier.cell() - margin);
        int z = cz * tier.cell() + Hash.range(Hash.next(h, 4), margin, tier.cell() - margin);
        return new Header(x + 0.5F, y + 0.5F, z + 0.5F, r, h);
    }

    static Neuron build(long seed, NeuralNetwork.Tier tier, int cx, int cy, int cz) {
        Header head = header(seed, tier, cx, cy, cz);
        if (head == null) {
            return NONE;
        }
        return grow(head, tier, seed, cx, cy, cz, true);
    }

    /** What branches need while growing: the output, and the neighbouring somas a tip may grow into. */
    private record Growth(Capsule.Builder out, IntArrayList glows, List<Header> near, float reach, boolean strands) {
    }

    /** Grows soma, dendrites, axons, a collateral to a bigger neuron, and sometimes a pair of eyes. */
    static Neuron grow(Header head, NeuralNetwork.Tier tier, long seed, int cx, int cy, int cz, boolean mayHollow) {
        long h = head.hash();
        float r = head.radius();
        Capsule.Builder capsules = new Capsule.Builder();
        IntArrayList glows = new IntArrayList();
        List<Loop> loops = new ArrayList<>();
        List<Header> near = tier.index() >= 0 && tier.axonMax() > 0 ? neighbours(seed, tier, cx, cy, cz) : List.of();
        Growth growth = new Growth(capsules, glows, near, tier.cell() * 1.8F, tier.hasStrands());
        float hollow = mayHollow && r >= 10 && Hash.chance(Hash.next(h, 5), 0.75) ? r - 3.0F : 0.0F;
        capsules.sphere(head.x(), head.y(), head.z(), r, hollow, Capsule.SOMA);
        int dendrites = Hash.range(Hash.next(h, 6), tier.dendriteMin(), tier.dendriteMax());
        for (int i = 0; i < dendrites; i++) {
            long dh = Hash.next(h, 100 + i);
            float[] dir = direction(dh);
            float length = r * tier.lengthFactor() * (0.8F + 0.4F * (float) Hash.unit(Hash.next(dh, 1)));
            float radius = Math.max(MIN_RADIUS, r * tier.radiusFactor() * (0.8F + 0.4F * (float) Hash.unit(Hash.next(dh, 2))));
            branch(growth, head.x() + dir[0] * r * 0.8F, head.y() + dir[1] * r * 0.8F, head.z() + dir[2] * r * 0.8F,
                    dir, length, radius, tier.depth(), dh);
        }
        int axons = tier.axonMax() > 0 ? Hash.range(Hash.next(h, 7), tier.axonMin(), tier.axonMax()) : 0;
        for (int i = 0; i < axons && !near.isEmpty(); i++) {
            long ah = Hash.next(h, 50 + i);
            if (i == 0 && tier.loopChance() > 0.0 && Hash.chance(Hash.next(ah, 1), tier.loopChance())) {
                loop(head, ah, capsules, loops);
                continue;
            }
            Header other = near.get((int) Math.floorMod(Hash.next(ah, 2) + i * 7919L, (long) near.size()));
            axon(head, other, ah, capsules);
        }
        if (tier.parent() >= 0) {
            Header parent = nearestParent(seed, NeuralNetwork.TIERS[tier.parent()], head, tier.linkRange());
            if (parent != null) {
                float radius = Math.max(MIN_RADIUS, r * 0.2F);
                link(capsules, head.x(), head.y(), head.z(), head.radius(), parent.x(), parent.y(), parent.z(), parent.radius(), radius, Hash.next(h, 60));
            }
        }
        if (r >= 8 && Hash.chance(Hash.next(h, 8), 0.3)) {
            eyes(glows, head, Hash.next(h, 9));
        }
        return new Neuron(head.x(), head.y(), head.z(), r, hollow, capsules, glows.toIntArray(), List.copyOf(loops));
    }

    /** Existing neurons of the same tier in the 26 surrounding cells. */
    private static List<Header> neighbours(long seed, NeuralNetwork.Tier tier, int cx, int cy, int cz) {
        List<Header> out = new ArrayList<>(26);
        for (int[] n : NEIGHBOURS) {
            Header other = header(seed, tier, cx + n[0], cy + n[1], cz + n[2]);
            if (other != null) {
                out.add(other);
            }
        }
        return out;
    }

    /** The closest neuron one size up, among the eight cells of its grid around this one. */
    private static @Nullable Header nearestParent(long seed, NeuralNetwork.Tier parent, Header head, int range) {
        int bx = Math.floorDiv((int) head.x() - parent.cell() / 2, parent.cell());
        int by = Math.floorDiv((int) head.y() - parent.cell() / 2, parent.cell());
        int bz = Math.floorDiv((int) head.z() - parent.cell() / 2, parent.cell());
        Header best = null;
        float bestDistance = range;
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    Header other = header(seed, parent, bx + dx, by + dy, bz + dz);
                    if (other == null) {
                        continue;
                    }
                    float d = distance(head.x(), head.y(), head.z(), other.x(), other.y(), other.z()) - other.radius();
                    if (d < bestDistance) {
                        bestDistance = d;
                        best = other;
                    }
                }
            }
        }
        return best;
    }

    private static float distance(float ax, float ay, float az, float bx, float by, float bz) {
        float dx = bx - ax;
        float dy = by - ay;
        float dz = bz - az;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void branch(Growth g, float px, float py, float pz, float[] dir, float length, float radius, int depth, long h) {
        float[] bent = perturb(dir, 0.25F, Hash.next(h, 7));
        float[] side = perturb(dir, 1.4F, Hash.next(h, 11));
        float mx = px + dir[0] * length * 0.5F + side[0] * length * 0.08F;
        float my = py + dir[1] * length * 0.5F + side[1] * length * 0.08F;
        float mz = pz + dir[2] * length * 0.5F + side[2] * length * 0.08F;
        float ex = mx + bent[0] * length * 0.5F;
        float ey = my + bent[1] * length * 0.5F;
        float ez = mz + bent[2] * length * 0.5F;
        float r1 = Math.max(MIN_RADIUS, radius * 0.9F);
        float r2 = Math.max(MIN_RADIUS, radius * 0.78F);
        boolean hollow = radius >= 3.0F && Math.abs(dir[1]) < 0.45F && Hash.chance(Hash.next(h, 8), 0.65);
        piece(g.out(), px, py, pz, mx, my, mz, radius, r1, hollow ? r1 - SHELL : 0.0F);
        piece(g.out(), mx, my, mz, ex, ey, ez, r1, r2, hollow ? r2 - SHELL : 0.0F);
        if (g.strands() && radius >= 1.5F && Hash.chance(Hash.next(h, 12), 0.22)) {
            strand(g, mx, my - radius * 0.7F, mz, Hash.next(h, 13));
        }
        if (depth > 1) {
            int children = Hash.chance(Hash.next(h, 9), 0.3) ? 3 : 2;
            for (int k = 0; k < children; k++) {
                float[] childDir = perturb(bent, 0.45F + 0.35F * (float) Hash.unit(Hash.next(h, 20 + k)), Hash.next(h, 25 + k));
                branch(g, ex, ey, ez, childDir, length * 0.68F, Math.max(MIN_RADIUS, r2 * 0.85F), depth - 1, Hash.next(h, 30 + k));
            }
            return;
        }
        Header target = g.near().isEmpty() || !Hash.chance(Hash.next(h, 14), 0.35) ? null : nearest(g.near(), ex, ey, ez, g.reach());
        if (target != null) {
            link(g.out(), ex, ey, ez, 0.0F, target.x(), target.y(), target.z(), target.radius(), r2, Hash.next(h, 15));
            return;
        }
        float bouton = Math.max(1.2F, r2 * 1.8F);
        g.out().sphere(ex, ey, ez, bouton, 0.0F, Capsule.BOUTON);
        if (Hash.chance(Hash.next(h, 10), 0.3)) {
            glow(g.glows(), (int) Math.floor(ex + bent[0] * (bouton - 0.5F)), (int) Math.floor(ey + bent[1] * (bouton - 0.5F)),
                    (int) Math.floor(ez + bent[2] * (bouton - 0.5F)), GLOW);
        }
    }

    private static @Nullable Header nearest(List<Header> near, float x, float y, float z, float reach) {
        Header best = null;
        float bestDistance = reach;
        for (Header other : near) {
            float d = distance(x, y, z, other.x(), other.y(), other.z()) - other.radius();
            if (d < bestDistance) {
                bestDistance = d;
                best = other;
            }
        }
        return best;
    }

    /** A thin thread hanging from a branch into the fog, with a drop of tissue at its end. */
    private static void strand(Growth g, float x, float y, float z, long h) {
        float length = 6.0F + (STRAND_MAX - 6.0F) * (float) Hash.unit(h);
        float ex = x + ((float) Hash.unit(Hash.next(h, 1)) - 0.5F) * 2.0F;
        float ez = z + ((float) Hash.unit(Hash.next(h, 2)) - 0.5F) * 2.0F;
        piece(g.out(), x, y, z, ex, y - length, ez, 0.65F, 0.55F, 0.0F);
        g.out().sphere(ex, y - length, ez, 1.1F, 0.0F, Capsule.BOUTON);
        if (Hash.chance(Hash.next(h, 3), 0.2)) {
            glow(g.glows(), (int) Math.floor(ex), (int) Math.floor(y - length - 0.6F), (int) Math.floor(ez), GLOW);
        }
    }

    private static void glow(IntArrayList glows, int x, int y, int z, int kind) {
        glows.add(x);
        glows.add(y);
        glows.add(z);
        glows.add(kind);
    }

    /** Two pale eyes set into the side of a soma, looking out into the fog. */
    private static void eyes(IntArrayList glows, Header head, long h) {
        double phi = Hash.unit(h) * Math.PI * 2.0;
        float dx = (float) Math.cos(phi);
        float dz = (float) Math.sin(phi);
        float r = head.radius() - 0.5F;
        float apart = Math.max(2.0F, head.radius() * 0.15F);
        int y = (int) Math.floor(head.y() + head.radius() * 0.1F);
        for (int side = -1; side <= 1; side += 2) {
            float x = head.x() + dx * r - dz * apart * side;
            float z = head.z() + dz * r + dx * apart * side;
            glow(glows, (int) Math.floor(x), y, (int) Math.floor(z), EYE);
        }
    }

    static final int GLOW = 0;
    static final int EYE = 1;

    private static final int[][] NEIGHBOURS = neighbours();

    private static int[][] neighbours() {
        int[][] out = new int[26][];
        int i = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        out[i++] = new int[]{dx, dy, dz};
                    }
                }
            }
        }
        return out;
    }

    /** An axon: a cable in myelin beads to a neighbouring soma of the same size. */
    private static void axon(Header head, Header other, long h, Capsule.Builder out) {
        float dx = other.x() - head.x();
        float dy = other.y() - head.y();
        float dz = other.z() - head.z();
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < head.radius() + other.radius() + 4) {
            return;
        }
        float ux = dx / distance;
        float uy = dy / distance;
        float uz = dz / distance;
        float sx = head.x() + ux * head.radius() * 0.9F;
        float sy = head.y() + uy * head.radius() * 0.9F;
        float sz = head.z() + uz * head.radius() * 0.9F;
        float span = distance - (head.radius() + other.radius()) * 0.9F;
        float radius = Math.max(MIN_RADIUS, head.radius() * 0.14F);
        boolean hollow = radius >= 3.0F && Math.abs(uy) < 0.45F && Hash.chance(Hash.next(h, 3), 0.7);
        float bore = hollow ? radius - SHELL : 0.0F;
        float node = hollow ? Math.max(radius * 0.62F, bore + 1.2F) : Math.max(MIN_RADIUS, radius * 0.62F);
        float bead = Math.max(4.0F, radius * 4.0F);
        int segments = Math.max(1, (int) (span / bead));
        float step = span / segments;
        for (int s = 0; s < segments; s++) {
            float t0 = s * step;
            float t1 = t0 + step * 0.7F;
            float t2 = t0 + step;
            piece(out, sx + ux * t0, sy + uy * t0, sz + uz * t0, sx + ux * t1, sy + uy * t1, sz + uz * t1, radius, radius, bore, true);
            piece(out, sx + ux * t1, sy + uy * t1, sz + uz * t1, sx + ux * t2, sy + uy * t2, sz + uz * t2, node, node, bore, true);
        }
    }

    /**
     * A sagging strand of tissue from a point (or the surface of a soma of radius {@code fromRadius})
     * to the surface of another soma.
     */
    private static void link(Capsule.Builder out, float ax, float ay, float az, float fromRadius, float bx, float by, float bz, float toRadius,
                             float radius, long h) {
        float d = distance(ax, ay, az, bx, by, bz);
        if (d < fromRadius + toRadius + 2.0F) {
            return;
        }
        float ux = (bx - ax) / d;
        float uy = (by - ay) / d;
        float uz = (bz - az) / d;
        float sx = ax + ux * fromRadius * 0.85F;
        float sy = ay + uy * fromRadius * 0.85F;
        float sz = az + uz * fromRadius * 0.85F;
        float ex = bx - ux * toRadius * 0.85F;
        float ey = by - uy * toRadius * 0.85F;
        float ez = bz - uz * toRadius * 0.85F;
        float sag = d * (0.06F + 0.08F * (float) Hash.unit(h));
        float mx = (sx + ex) * 0.5F;
        float my = (sy + ey) * 0.5F - sag;
        float mz = (sz + ez) * 0.5F;
        piece(out, sx, sy, sz, mx, my, mz, radius, radius * 0.8F, 0.0F);
        piece(out, mx, my, mz, ex, ey, ez, radius * 0.8F, radius, 0.0F);
    }

    /**
     * Adds a tapered segment in pieces no longer than {@link #PIECE}, so every piece has a tight
     * bounding box and chunks only test the blocks a piece can actually reach.
     */
    private static void piece(Capsule.Builder out, float ax, float ay, float az, float bx, float by, float bz, float ra, float rb, float inner) {
        piece(out, ax, ay, az, bx, by, bz, ra, rb, inner, false);
    }

    private static void piece(Capsule.Builder out, float ax, float ay, float az, float bx, float by, float bz, float ra, float rb, float inner, boolean axon) {
        float length = distance(ax, ay, az, bx, by, bz);
        int pieces = Math.max(1, (int) Math.ceil(length / PIECE));
        for (int i = 0; i < pieces; i++) {
            float t0 = (float) i / pieces;
            float t1 = (float) (i + 1) / pieces;
            float x0 = ax + (bx - ax) * t0;
            float y0 = ay + (by - ay) * t0;
            float z0 = az + (bz - az) * t0;
            float x1 = ax + (bx - ax) * t1;
            float y1 = ay + (by - ay) * t1;
            float z1 = az + (bz - az) * t1;
            float r0 = ra + (rb - ra) * t0;
            float r1 = ra + (rb - ra) * t1;
            if (axon) {
                out.axon(x0, y0, z0, x1, y1, z1, r0, r1, inner);
            } else {
                out.tissue(x0, y0, z0, x1, y1, z1, r0, r1, inner);
            }
        }
    }

    private static void loop(Header head, long h, Capsule.Builder out, List<Loop> loops) {
        double phi = Hash.unit(Hash.next(h, 5)) * Math.PI * 2.0;
        int vx = (int) Math.round(Math.cos(phi) * LOOP_PERIOD);
        int vz = (int) Math.round(Math.sin(phi) * LOOP_PERIOD);
        int vy = Hash.range(Hash.next(h, 6), -1, 1);
        if (Math.abs(vx) + Math.abs(vz) < LOOP_PERIOD / 2) {
            vx = LOOP_PERIOD;
        }
        float length = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        float sx = head.x() + vx / length * (head.radius() - 2.0F);
        float sy = head.y() + vy / length * (head.radius() - 2.0F);
        float sz = head.z() + vz / length * (head.radius() - 2.0F);
        for (int k = 0; k < LOOP_PERIODS; k++) {
            float ax = sx + k * vx;
            float ay = sy + k * vy;
            float az = sz + k * vz;
            float mx = ax + 0.7F * vx;
            float my = ay + 0.7F * vy;
            float mz = az + 0.7F * vz;
            out.loop(ax, ay, az, mx, my, mz, 3.6F, LOOP_BORE);
            out.loop(mx, my, mz, ax + vx, ay + vy, az + vz, 3.0F, LOOP_BORE);
        }
        loops.add(new Loop(sx, sy, sz, vx, vy, vz));
    }

    static float[] direction(long h) {
        double u = Hash.unit(h) * 2.0 - 1.0;
        double phi = Hash.unit(Hash.next(h, 1)) * Math.PI * 2.0;
        double s = Math.sqrt(1.0 - u * u);
        return new float[]{(float) (s * Math.cos(phi)), (float) u, (float) (s * Math.sin(phi))};
    }

    /** A direction turned away from {@code dir} by about {@code angle} radians, around a random axis. */
    static float[] perturb(float[] dir, float angle, long h) {
        float[] w = direction(h);
        float dot = w[0] * dir[0] + w[1] * dir[1] + w[2] * dir[2];
        float px = w[0] - dir[0] * dot;
        float py = w[1] - dir[1] * dot;
        float pz = w[2] - dir[2] * dot;
        float pl = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (pl < 1.0E-4F) {
            return dir;
        }
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float x = dir[0] * c + px / pl * s;
        float y = dir[1] * c + py / pl * s;
        float z = dir[2] * c + pz / pl * s;
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        return new float[]{x / l, y / l, z / l};
    }
}
