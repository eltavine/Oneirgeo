package com.eltavine.oneirgeo.world.gen.scene.neural;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The overworld: no ground, no sky, only fog and a nervous system grown to the size of the world,
 * pieced together from grey, brown, pale and dried-blood blocks. Hollow somas are rooms lined with
 * flesh, level branches are tunnels, a few long axons are tunnels with no end, and some somas watch.
 */
public final class NeuralWebScene implements Scene {
    private static final Map<Long, NeuralNetwork> NETWORKS = new ConcurrentHashMap<>();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState SYNAPSE = OneirgeoBlocks.SYNAPSE.defaultBlockState();
    private static final BlockState EYE = OneirgeoBlocks.TISSUE_EYE.defaultBlockState();
    private static final List<ResourceKey<LootTable>> LOOT = List.of(BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.STRONGHOLD_CORRIDOR, BuiltInLootTables.ANCIENT_CITY);

    private static NeuralNetwork network(long seed) {
        return NETWORKS.computeIfAbsent(seed, NeuralNetwork::new);
    }

    /**
     * Which way the current runs at a point inside the bore of a hollow axon, as a unit vector, or
     * null outside every axon. {@code seed} is the overworld web's scene seed.
     */
    public static double @org.jspecify.annotations.Nullable [] axonFlow(long seed, double x, double y, double z) {
        int bx = (int) Math.floor(x);
        int by = (int) Math.floor(y);
        int bz = (int) Math.floor(z);
        Capsule c = new Capsule();
        for (Neuron neuron : network(seed).collect(bx, by, bz, bx, by, bz)) {
            int slot = neuron.slot(bx >> 4, bz >> 4);
            if (slot < 0) {
                continue;
            }
            for (int p = neuron.slotStart(slot), end = neuron.slotEnd(slot); p < end; p++) {
                int i = neuron.listed(p);
                if (neuron.kinds[i] != Capsule.AXON || neuron.inner(i) <= 0.0F) {
                    continue;
                }
                c.load(neuron, i);
                double dx = c.bx - c.ax;
                double dy = c.by - c.ay;
                double dz = c.bz - c.az;
                double len2 = dx * dx + dy * dy + dz * dz;
                if (len2 < 1.0E-6) {
                    continue;
                }
                double t = Math.max(0.0, Math.min(1.0, ((x - c.ax) * dx + (y - c.ay) * dy + (z - c.az) * dz) / len2));
                double qx = x - (c.ax + t * dx);
                double qy = y - (c.ay + t * dy);
                double qz = z - (c.az + t * dz);
                double bore = Math.max(0.0, c.inner - 0.2);
                if (qx * qx + qy * qy + qz * qz <= bore * bore) {
                    double length = Math.sqrt(len2);
                    return new double[]{dx / length, dy / length, dz / length};
                }
            }
        }
        return null;
    }

    /** The middle of the bore of some hollow axon reaching the given chunk column, for the self-test; null when none does. */
    public static double @org.jspecify.annotations.Nullable [] axonBore(long seed, int chunkX, int chunkZ, int minY, int maxY) {
        Capsule c = new Capsule();
        int x0 = chunkX << 4;
        int z0 = chunkZ << 4;
        for (Neuron neuron : network(seed).collect(x0, minY, z0, x0 + 15, maxY, z0 + 15)) {
            int slot = neuron.slot(chunkX, chunkZ);
            if (slot < 0) {
                continue;
            }
            for (int p = neuron.slotStart(slot), end = neuron.slotEnd(slot); p < end; p++) {
                int i = neuron.listed(p);
                if (neuron.kinds[i] == Capsule.AXON && neuron.inner(i) > 1.0F) {
                    c.load(neuron, i);
                    double mx = (c.ax + c.bx) * 0.5;
                    double my = (c.ay + c.by) * 0.5;
                    double mz = (c.az + c.bz) * 0.5;
                    if ((int) Math.floor(mx) >> 4 == chunkX && (int) Math.floor(mz) >> 4 == chunkZ && my >= minY && my <= maxY
                            && !SpawnHub.excluded((int) Math.floor(mx), (int) Math.floor(my), (int) Math.floor(mz))) {
                        return new double[]{mx, my, mz};
                    }
                }
            }
        }
        return null;
    }

    /** The room where the clocks stopped, at the foot of the stair under the vault. */
    public static com.eltavine.oneirgeo.space.Box finalRoom() {
        return SpawnHub.ROOM;
    }

    /** The lowest step of the stair under the vault, and the turn of it that loops. */
    public static int[] stair() {
        return new int[]{SpawnHub.STAIR_X, SpawnHub.STAIR_Z, SpawnHub.STAIR_TOP, SpawnHub.STAIR_BOTTOM, SpawnHub.STEPS};
    }

    /** Neurons and capsules held by the networks of every seed, for the memory stress test. */
    public static long[] cacheStats() {
        long[] sum = new long[2];
        for (NeuralNetwork network : NETWORKS.values()) {
            long[] cached = network.cached();
            sum[0] += cached[0];
            sum[1] += cached[1];
        }
        return sum;
    }

    /**
     * Development measurement: grows, in a network of its own, every neuron reaching the full height
     * of a 5 x 5 chunk area, and reports how many there are, how long they took and how much memory
     * they keep.
     */
    public static String measure(long seed) {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long before = runtime.totalMemory() - runtime.freeMemory();
        long started = System.nanoTime();
        NeuralNetwork network = new NeuralNetwork(seed);
        for (int cx = -2; cx <= 2; cx++) {
            for (int cz = -2; cz <= 2; cz++) {
                network.collect(cx * 16, -2032, cz * 16, cx * 16 + 15, 2031, cz * 16 + 15);
            }
        }
        double seconds = (System.nanoTime() - started) / 1.0E9;
        System.gc();
        long after = runtime.totalMemory() - runtime.freeMemory();
        long[] cached = network.cached();
        java.lang.ref.Reference.reachabilityFence(network);
        return String.format("%d neurons, %d capsules, %.1f MB retained, grown in %.2fs", cached[0], cached[1],
                (after - before) / 1048576.0, seconds);
    }

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int x0 = ctx.originX();
        int z0 = ctx.originZ();
        List<Neuron> neurons = network(seed).collect(x0, ctx.minY(), z0, x0 + 15, ctx.maxY(), z0 + 15);
        Neuron hub = SpawnHub.hub(seed);
        boolean withHub = hub.intersects(x0, ctx.minY(), z0, x0 + 15, ctx.maxY(), z0 + 15);
        int chunkX = x0 >> 4;
        int chunkZ = z0 >> 4;
        Capsule c = new Capsule();
        for (Neuron neuron : neurons) {
            this.pieces(ctx, seed, neuron, chunkX, chunkZ, c, false, false);
        }
        if (withHub) {
            this.pieces(ctx, seed, hub, chunkX, chunkZ, c, true, false);
        }
        for (Neuron neuron : neurons) {
            this.pieces(ctx, seed, neuron, chunkX, chunkZ, c, false, true);
        }
        if (withHub) {
            this.pieces(ctx, seed, hub, chunkX, chunkZ, c, true, true);
        }
        for (Neuron neuron : neurons) {
            if (neuron.hollow > 0.0F) {
                this.room(ctx, seed, neuron);
            }
            this.glows(ctx, neuron, false);
            for (Neuron.Loop loop : neuron.loops) {
                ctx.seam(loop.seam());
                ctx.protectSolid(loop.bounds());
            }
        }
        if (withHub) {
            this.glows(ctx, hub, true);
        }
        SpawnHub.build(ctx, seed);
    }

    /** Tissue of every capsule of a neuron reaching this chunk, or, with {@code bores}, the empty insides of the hollow ones. */
    private void pieces(SceneContext ctx, long seed, Neuron neuron, int chunkX, int chunkZ, Capsule c, boolean hub, boolean bores) {
        int slot = neuron.slot(chunkX, chunkZ);
        if (slot < 0) {
            return;
        }
        for (int p = neuron.slotStart(slot), end = neuron.slotEnd(slot); p < end; p++) {
            int i = neuron.listed(p);
            if (!bores) {
                this.tissue(ctx, seed, c.load(neuron, i), hub);
            } else if (neuron.inner(i) > 0.0F) {
                this.bore(ctx, c.load(neuron, i), hub);
            }
        }
    }

    /** Solid tissue and the walls of hollow pieces. */
    private void tissue(SceneContext ctx, long seed, Capsule c, boolean hub) {
        int x0 = ctx.originX();
        int z0 = ctx.originZ();
        if (!c.intersects(x0, ctx.minY(), z0, x0 + 15, ctx.maxY(), z0 + 15)) {
            return;
        }
        int xa = Math.max(c.minX, x0);
        int xb = Math.min(c.maxX, x0 + 15);
        int za = Math.max(c.minZ, z0);
        int zb = Math.min(c.maxZ, z0 + 15);
        int ya = Math.max(c.minY, ctx.minY());
        int yb = Math.min(c.maxY, ctx.maxY());
        float dx = c.bx - c.ax;
        float dy = c.by - c.ay;
        float dz = c.bz - c.az;
        float len2 = dx * dx + dy * dy + dz * dz;
        float inner2 = c.inner * c.inner;
        float lining2 = (c.inner + 1.2F) * (c.inner + 1.2F);
        float reach = Math.max(c.ra, c.rb);
        float[] span = new float[2];
        for (int x = xa; x <= xb; x++) {
            for (int z = za; z <= zb; z++) {
                if (!column(c, x, z, reach, span)) {
                    continue;
                }
                int yFrom = Math.max(ya, (int) Math.floor(span[0]));
                int yTo = Math.min(yb, (int) Math.ceil(span[1]));
                for (int y = yFrom; y <= yTo; y++) {
                    if (hub ? SpawnHub.cut(x, y, z) : SpawnHub.excluded(x, y, z)) {
                        continue;
                    }
                    float px = x + 0.5F - c.ax;
                    float py = y + 0.5F - c.ay;
                    float pz = z + 0.5F - c.az;
                    float t = len2 > 0.0F ? Math.max(0.0F, Math.min(1.0F, (px * dx + py * dy + pz * dz) / len2)) : 0.0F;
                    float qx = px - t * dx;
                    float qy = py - t * dy;
                    float qz = pz - t * dz;
                    float d2 = qx * qx + qy * qy + qz * qz;
                    float r = c.ra + (c.rb - c.ra) * t;
                    if (d2 > r * r || d2 <= inner2) {
                        continue;
                    }
                    BlockState state;
                    if (c.kind == Capsule.LOOP) {
                        state = NeuralPalette.at(seed, x - c.phase * c.px, y - c.phase * c.py, z - c.phase * c.pz);
                    } else if (c.kind == Capsule.SOMA && c.inner > 0.0F && d2 <= lining2) {
                        state = NeuralPalette.flesh(seed, x, y, z);
                    } else {
                        state = NeuralPalette.at(seed, x, y, z);
                    }
                    ctx.set(x - x0, y, z - z0, state);
                }
            }
        }
    }

    /** The empty inside of hollow pieces, cut after all tissue so tunnels stay open where they cross. */
    private void bore(SceneContext ctx, Capsule c, boolean hub) {
        int x0 = ctx.originX();
        int z0 = ctx.originZ();
        if (!c.intersects(x0, ctx.minY(), z0, x0 + 15, ctx.maxY(), z0 + 15)) {
            return;
        }
        int reach = (int) Math.ceil(c.inner);
        int xa = Math.max((int) Math.floor(Math.min(c.ax, c.bx)) - reach, x0);
        int xb = Math.min((int) Math.ceil(Math.max(c.ax, c.bx)) + reach, x0 + 15);
        int za = Math.max((int) Math.floor(Math.min(c.az, c.bz)) - reach, z0);
        int zb = Math.min((int) Math.ceil(Math.max(c.az, c.bz)) + reach, z0 + 15);
        int ya = Math.max((int) Math.floor(Math.min(c.ay, c.by)) - reach, ctx.minY());
        int yb = Math.min((int) Math.ceil(Math.max(c.ay, c.by)) + reach, ctx.maxY());
        float dx = c.bx - c.ax;
        float dy = c.by - c.ay;
        float dz = c.bz - c.az;
        float len2 = dx * dx + dy * dy + dz * dz;
        float inner2 = c.inner * c.inner;
        float[] span = new float[2];
        for (int x = xa; x <= xb; x++) {
            for (int z = za; z <= zb; z++) {
                if (!column(c, x, z, c.inner, span)) {
                    continue;
                }
                int yFrom = Math.max(ya, (int) Math.floor(span[0]));
                int yTo = Math.min(yb, (int) Math.ceil(span[1]));
                for (int y = yFrom; y <= yTo; y++) {
                    if (hub ? SpawnHub.cut(x, y, z) : SpawnHub.excluded(x, y, z)) {
                        continue;
                    }
                    float px = x + 0.5F - c.ax;
                    float py = y + 0.5F - c.ay;
                    float pz = z + 0.5F - c.az;
                    float t = len2 > 0.0F ? Math.max(0.0F, Math.min(1.0F, (px * dx + py * dy + pz * dz) / len2)) : 0.0F;
                    float qx = px - t * dx;
                    float qy = py - t * dy;
                    float qz = pz - t * dz;
                    if (qx * qx + qy * qy + qz * qz <= inner2) {
                        ctx.set(x - x0, y, z - z0, AIR);
                    }
                }
            }
        }
    }

    /**
     * Heights a capsule can occupy in the column through block (x, z): the part of the segment that
     * comes within {@code reach} horizontally, widened by {@code reach} up and down. False when the
     * column is never touched.
     */
    private static boolean column(Capsule c, int x, int z, float reach, float[] span) {
        float hx = x + 0.5F - c.ax;
        float hz = z + 0.5F - c.az;
        float dx = c.bx - c.ax;
        float dz = c.bz - c.az;
        float a = dx * dx + dz * dz;
        float tLo = 0.0F;
        float tHi = 1.0F;
        if (a > 1.0E-6F) {
            float b = -(hx * dx + hz * dz);
            float cc = hx * hx + hz * hz - reach * reach;
            float disc = b * b - a * cc;
            if (disc < 0.0F) {
                return false;
            }
            float root = (float) Math.sqrt(disc);
            tLo = Math.max(0.0F, (-b - root) / a);
            tHi = Math.min(1.0F, (-b + root) / a);
            if (tLo > tHi) {
                return false;
            }
        } else if (hx * hx + hz * hz > reach * reach) {
            return false;
        }
        float yA = c.ay + tLo * (c.by - c.ay);
        float yB = c.ay + tHi * (c.by - c.ay);
        span[0] = Math.min(yA, yB) - reach - 0.5F;
        span[1] = Math.max(yA, yB) + reach - 0.5F;
        return true;
    }

    private static int floorY(Neuron soma) {
        return (int) Math.floor(soma.y - soma.hollow * 0.45F);
    }

    /** A level floor across a hollow soma, and now and then a door that should not be here. */
    private void room(SceneContext ctx, long seed, Neuron soma) {
        int floor = floorY(soma);
        if (floor < ctx.minY() || floor > ctx.maxY()) {
            return;
        }
        double dy = floor + 0.5 - soma.y;
        double r = Math.sqrt(Math.max(0.0, soma.hollow * soma.hollow - dy * dy));
        int x0 = ctx.originX();
        int z0 = ctx.originZ();
        for (int x = Math.max(x0, (int) Math.floor(soma.x - r)); x <= Math.min(x0 + 15, (int) Math.ceil(soma.x + r)); x++) {
            for (int z = Math.max(z0, (int) Math.floor(soma.z - r)); z <= Math.min(z0 + 15, (int) Math.ceil(soma.z + r)); z++) {
                double hx = x + 0.5 - soma.x;
                double hz = z + 0.5 - soma.z;
                if (hx * hx + hz * hz <= r * r && !SpawnHub.excluded(x, floor, z)) {
                    ctx.set(x - x0, floor, z - z0, NeuralPalette.at(seed, x, floor, z));
                }
            }
        }
        long h = Hash.of(seed, (long) Math.floor(soma.x), (long) Math.floor(soma.y), (long) Math.floor(soma.z));
        if (Hash.chance(Hash.next(h, 1), 0.12)) {
            int x = (int) Math.floor(soma.x) + 2;
            int z = (int) Math.floor(soma.z);
            BlockState door = OneirgeoBlocks.BACKROOMS_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
            ctx.place(x, floor + 1, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            ctx.place(x, floor + 2, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    private void glows(SceneContext ctx, Neuron neuron, boolean hub) {
        int[] g = neuron.glows;
        for (int o = 0; o < g.length; o += 4) {
            int x = g[o];
            int y = g[o + 1];
            int z = g[o + 2];
            if (ctx.ownsWorld(x, z) && y >= ctx.minY() && y <= ctx.maxY()
                    && !(hub ? SpawnHub.cut(x, y, z) : SpawnHub.excluded(x, y, z))) {
                ctx.place(x, y, z, g[o + 3] == Neuron.EYE ? EYE : SYNAPSE);
            }
        }
    }

    @Override
    public void decorate(DecorationContext ctx) {
        long seed = ctx.seed();
        int x0 = ctx.originX();
        int z0 = ctx.originZ();
        for (Neuron neuron : network(seed).collect(x0, ctx.info().minY(), z0, x0 + 15, ctx.info().maxY(), z0 + 15)) {
            if (neuron.hollow <= 0.0F) {
                continue;
            }
            BlockPos pos = new BlockPos((int) Math.floor(neuron.x), floorY(neuron) + 1, (int) Math.floor(neuron.z));
            long h = Hash.of(seed, pos.getX(), pos.getY(), pos.getZ());
            if (!ctx.canPlace(pos) || !ctx.getBlock(pos).isAir()) {
                continue;
            }
            if (Hash.chance(h, 0.6)) {
                ctx.chest(pos, Direction.from2DDataValue(Hash.range(Hash.next(h, 1), 0, 3)), LOOT.get(Hash.range(Hash.next(h, 2), 0, LOOT.size() - 1)));
            } else if (Hash.chance(Hash.next(h, 3), 0.35)) {
                ctx.mimic(pos);
            }
            if (Hash.chance(Hash.next(h, 5), 0.3)) {
                livingRoom(ctx, pos);
            }
        }
        if (ctx.originX() <= 40 && ctx.originX() + 15 >= -40 && ctx.originZ() <= 40 && ctx.originZ() + 15 >= -40) {
            SpawnHub.decorate(ctx);
        }
    }

    /** Inside a hollow soma, what is left of a living room: a television, a chair in front of it, a telephone. */
    private static void livingRoom(DecorationContext ctx, BlockPos centre) {
        ctx.furniture(centre.offset(3, 0, 0), OneirgeoBlocks.TELEVISION, Direction.WEST);
        ctx.furniture(centre.offset(-1, 0, 0), OneirgeoBlocks.WAITING_CHAIR, Direction.EAST);
        ctx.furniture(centre.offset(0, 0, 3), OneirgeoBlocks.TELEPHONE, Direction.NORTH);
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return x * x + z * z <= 24 * 24 ? SpawnHub.PLATFORM : Integer.MIN_VALUE;
    }
}
