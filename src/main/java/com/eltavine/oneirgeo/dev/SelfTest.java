package com.eltavine.oneirgeo.dev;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.world.gen.GenerationStats;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Development smoke test, enabled with {@code -Doneirgeo.selftest=true} (the {@code runSelftest}
 * Gradle task): on the first tick, generates chunks around every dimension's spawn and logs what it
 * found and how long generation took; stops the server two seconds later.
 */
public final class SelfTest {
    private SelfTest() {
    }

    public static void init() {
        if (!Boolean.getBoolean("oneirgeo.selftest")) {
            return;
        }
        int[] ticks = {0};
        Stress[] stress = {null};
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks[0]++;
            if (ticks[0] == 1) {
                run(server);
                stress[0] = new Stress(server, Integer.getInteger("oneirgeo.selftest.rounds", 4));
            } else if (stress[0] != null && stress[0].tick()) {
                stress[0] = null;
                server.halt(false);
            }
        });
    }

    /**
     * Memory stress test: round after round, generates a fresh area of three dimensions far from
     * everything, lets it unload and save, and measures the heap after a full collection. A leak
     * shows as a heap that keeps growing from round to round; saving too many chunks at once as an
     * out-of-memory error during an unload.
     */
    private static final class Stress {
        private static final int RADIUS = 5;
        private static final int MIN_WAIT = 100;
        private static final int MAX_WAIT = 2400;
        private static final int STABLE = 60;
        private final MinecraftServer server;
        private final int rounds;
        private final java.util.List<Long> heaps = new java.util.ArrayList<>();
        private int round = -1;
        private int waited;
        private int lowest = Integer.MAX_VALUE;
        private int stableFor;
        private int baselineChunks = -1;

        Stress(MinecraftServer server, int rounds) {
            this.server = server;
            this.rounds = rounds;
        }

        /** Chunks loaded, or still waiting to unload, in every dimension. */
        private int loadedChunks() {
            int sum = 0;
            for (ServerLevel level : this.server.getAllLevels()) {
                sum += level.getChunkSource().getLoadedChunksCount() + pendingUnloads(level);
            }
            return sum;
        }

        private static int pendingUnloads(ServerLevel level) {
            try {
                java.lang.reflect.Field field = net.minecraft.server.level.ChunkMap.class.getDeclaredField("pendingUnloads");
                field.setAccessible(true);
                return ((java.util.Map<?, ?>) field.get(level.getChunkSource().chunkMap)).size();
            } catch (ReflectiveOperationException e) {
                return 0;
            }
        }

        /** True once every round is measured. */
        boolean tick() {
            this.waited++;
            int loaded = this.loadedChunks();
            if (loaded < this.lowest) {
                this.lowest = loaded;
                this.stableFor = 0;
            } else {
                this.stableFor++;
            }
            boolean drained = this.baselineChunks < 0 ? this.stableFor >= STABLE : loaded <= this.baselineChunks + 32;
            if (this.waited < MIN_WAIT || (!drained && this.waited < MAX_WAIT)) {
                return false;
            }
            if (this.baselineChunks < 0) {
                this.baselineChunks = loaded;
            }
            long flushing = System.nanoTime();
            for (ServerLevel level : this.server.getAllLevels()) {
                level.getChunkSource().chunkMap.synchronize(true).join();
            }
            Oneirgeo.LOGGER.info("[selftest] stress: pending chunk writes flushed in {}s", String.format("%.1f", (System.nanoTime() - flushing) / 1.0E9));
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            long used = (runtime.totalMemory() - runtime.freeMemory()) >> 20;
            this.heaps.add(used);
            long[] neural = com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene.cacheStats();
            Oneirgeo.LOGGER.info("[selftest] stress {}: {} chunks loaded or waiting to unload after {} ticks, {} MB used after GC, neural cache {} neurons / {} capsules",
                    this.round < 0 ? "baseline" : "round " + this.round, loaded, this.waited, used, neural[0], neural[1]);
            for (ServerLevel level : this.server.getAllLevels()) {
                internals(level);
            }
            if (this.round < 0 || this.round == this.rounds - 1) {
                histogram(this.round < 0 ? "baseline" : "last round");
            }
            this.round++;
            if (this.round >= this.rounds) {
                this.report();
                return true;
            }
            this.load(this.round);
            this.waited = 0;
            this.lowest = Integer.MAX_VALUE;
            this.stableFor = 0;
            return false;
        }

        private void load(int round) {
            long started = System.nanoTime();
            int centre = 400 + round * 60;
            int count = 0;
            for (ServerLevel level : new ServerLevel[]{this.server.overworld(), this.server.getLevel(net.minecraft.world.level.Level.NETHER),
                    this.server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.BACKROOMS)}) {
                for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                    for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                        level.getChunk(centre + dx, centre + dz);
                        count++;
                    }
                }
            }
            Oneirgeo.LOGGER.info("[selftest] stress round {}: generated {} chunks around chunk {},{} in {}s", round, count, centre, centre,
                    String.format("%.1f", (System.nanoTime() - started) / 1.0E9));
        }

        /** Sizes of the chunk map's own collections, which hold chunks between loading and unloading. */
        private static void internals(ServerLevel level) {
            StringBuilder out = new StringBuilder();
            for (String name : new String[]{"updatingChunkMap", "visibleChunkMap", "pendingUnloads", "pendingGenerationTasks", "toDrop", "unloadQueue"}) {
                try {
                    java.lang.reflect.Field field = net.minecraft.server.level.ChunkMap.class.getDeclaredField(name);
                    field.setAccessible(true);
                    Object value = field.get(level.getChunkSource().chunkMap);
                    int size = value instanceof java.util.Map<?, ?> map ? map.size() : value instanceof java.util.Collection<?> c ? c.size() : -1;
                    out.append(name).append('=').append(size).append(' ');
                } catch (ReflectiveOperationException e) {
                    out.append(name).append("=? ");
                }
            }
            Oneirgeo.LOGGER.info("[selftest] stress {} chunk map: {}", level.dimension().identifier(), out);
        }

        /** The biggest classes on the heap, straight from the JVM's own class histogram. */
        private static void histogram(String when) {
            try {
                String text = (String) java.lang.management.ManagementFactory.getPlatformMBeanServer().invoke(
                        new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand"), "gcClassHistogram",
                        new Object[]{new String[0]}, new String[]{String[].class.getName()});
                text.lines().limit(28).forEach(line -> Oneirgeo.LOGGER.info("[selftest] histogram {}: {}", when, line));
            } catch (Exception e) {
                Oneirgeo.LOGGER.warn("[selftest] no class histogram", e);
            }
        }

        private void report() {
            long first = this.heaps.size() > 1 ? this.heaps.get(1) : this.heaps.getFirst();
            long last = this.heaps.getLast();
            long highest = this.heaps.stream().mapToLong(Long::longValue).max().orElse(0L);
            boolean steady = last - first < 96;
            Oneirgeo.LOGGER.info("[selftest] stress: heap after each round {} MB, highest {} MB of {} MB allowed; {} MB change from the first round to the last: {}",
                    this.heaps, highest, Runtime.getRuntime().maxMemory() >> 20, last - first, steady ? "steady, no leak" : "GROWING, possible leak");
        }
    }

    /**
     * A hash of every block state around the overworld spawn, to tell whether a change to the
     * generator changed what it generates. Chunk (-2, -2) is left out: the test lights a mirror there.
     */
    private static long digest(ServerLevel level, int radius) {
        long h = 1125899906842597L;
        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                if (cx == -2 && cz == -2) {
                    continue;
                }
                net.minecraft.world.level.chunk.LevelChunkSection[] sections = level.getChunk(cx, cz).getSections();
                for (int s = 0; s < sections.length; s++) {
                    net.minecraft.world.level.chunk.LevelChunkSection section = sections[s];
                    if (section.hasOnlyAir()) {
                        h = h * 31 + s;
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                h = h * 31 + net.minecraft.world.level.block.Block.getId(section.getBlockState(x, y, z));
                            }
                        }
                    }
                }
            }
        }
        return h;
    }

    /** Every seam must land its traveller in open space: two blocks of nothing solid. */
    private static void checkSeams(ServerLevel level, int radius) {
        checkSeams(level, 0, 0, radius);
    }

    private static void checkSeams(ServerLevel level, int centerX, int centerZ, int radius) {
        int seams = 0;
        int traps = 0;
        int bad = 0;
        Set<com.eltavine.oneirgeo.space.SeamVolume> seen = new HashSet<>();
        for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
            for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                ChunkSpaceData data = chunk.getAttachedOrElse(OneirgeoAttachments.SPACE,
                        ChunkSpaceData.EMPTY);
                traps += data.trapZones().size();
                for (SeamVolume seam : data.seams()) {
                    if (!seen.add(seam)) {
                        continue;
                    }
                    seams++;
                    Box t = seam.trigger();
                    Vec3 feet = new Vec3(t.centerX(), t.minY() + 0.0, t.centerZ());
                    if (seam.dy() > 0 || seam.direction() == Direction.DOWN.get3DDataValue()) {
                        feet = new Vec3(t.centerX(), t.minY() + 0.5, t.centerZ());
                    }
                    Vec3 dest = seam.apply(feet);
                    BlockPos foot = BlockPos.containing(dest);
                    net.minecraft.world.phys.shapes.VoxelShape floorShape = level.getBlockState(foot).getCollisionShape(level, foot);
                    boolean open = (floorShape.isEmpty() || floorShape.max(Direction.Axis.Y) <= dest.y - foot.getY() + 1.0E-3)
                            && level.getBlockState(foot.above()).getCollisionShape(level, foot.above()).isEmpty();
                    if (!open) {
                        bad++;
                        if (bad <= 8) {
                            Oneirgeo.LOGGER.warn("[selftest] seam {} -> {} lands in {} / {}", t, foot.toShortString(),
                                    level.getBlockState(foot), level.getBlockState(foot.above()));
                        }
                    }
                }
            }
        }
        Oneirgeo.LOGGER.info("[selftest] {} seams near chunk {},{}: {} unique, {} trap volumes, {} landing in solid blocks",
                level.dimension().identifier(), centerX, centerZ, seams, traps, bad);
    }

    /** The spawn chimney, its flip shaft, an entity falling up it, and a placed portal. */
    private static void checkNether(ServerLevel nether) throws ClassNotFoundException {
        Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
        Class.forName("net.minecraft.server.PlayerAdvancements");
        Class.forName("net.minecraft.server.level.ServerPlayer");
        nether.getChunk(900 >> 4, 900 >> 4);
        int ground = com.eltavine.oneirgeo.world.gen.scene.nether.AshPlainsScene.GROUND;
        for (int[] probe : new int[][]{{48, ground + 3, 40}, {57, ground + 2, 40}, {48, ground - 1, 40}, {48, ground + 600, 40}, {48, 0, 40}, {48, 1700, 40}, {48, 1904, 40}}) {
            BlockPos pos = new BlockPos(probe[0], probe[1], probe[2]);
            Oneirgeo.LOGGER.info("[selftest] nether probe {} = {} flipped {}", pos.toShortString(), nether.getBlockState(pos),
                    com.eltavine.oneirgeo.space.SpaceQuery.isFlipped(nether, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        }
        for (int y : new int[]{-322, -300, 0, 1500, 1700, 1900}) {
            Oneirgeo.LOGGER.info("[selftest] nether flipped at (48, {}, 40): {}, at (900, {}, 900): {}", y,
                    com.eltavine.oneirgeo.space.SpaceQuery.isFlipped(nether, 48.5, y, 40.5), y,
                    com.eltavine.oneirgeo.space.SpaceQuery.isFlipped(nether, 900.5, y, 900.5));
        }

        net.minecraft.world.entity.animal.pig.Pig pig = net.minecraft.world.entity.EntityTypes.PIG.create(nether,
                net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (pig != null) {
            pig.snapTo(900.5, 1600.0, 900.5, 0.0F, 0.0F);
            nether.addFreshEntity(pig);
            double start = pig.getY();
            for (int i = 0; i < 40; i++) {
                pig.tick();
            }
            Oneirgeo.LOGGER.info("[selftest] pig in the hanging city: flipped {}, y {} -> {}, fall distance {}",
                    com.eltavine.oneirgeo.space.Gravity.isFlipped(pig), String.format("%.2f", start), String.format("%.2f", pig.getY()), pig.fallDistance);
            pig.discard();
        }

        java.util.Optional<net.minecraft.util.BlockUtil.FoundRectangle> portal = com.eltavine.oneirgeo.world.PortalPlacement.create(
                nether, new BlockPos(300, 64, 300), Direction.Axis.X);
        if (portal != null && portal.isPresent()) {
            BlockPos corner = portal.get().minCorner;
            Oneirgeo.LOGGER.info("[selftest] nether portal placed at {}: {} on {}", corner.toShortString(),
                    nether.getBlockState(corner), nether.getBlockState(corner.below()));
        } else {
            Oneirgeo.LOGGER.warn("[selftest] nether portal placement fell back to vanilla");
        }
    }

    /** The arrival island and its door, the nearest observatory's seams, a spiral stair and the sea. */
    private static void checkEnd(ServerLevel end) {
        long seed = ((OneirgeoChunkGenerator) end.getChunkSource().getGenerator()).sampler().layerSeed(1);
        for (int[] probe : new int[][]{{100, 48, 0}, {88, 49, 0}, {88, 50, 0}, {420, 41, 60}, {424, 50, 60}, {0, -1000, 0}, {0, -1060, 0}}) {
            BlockPos pos = new BlockPos(probe[0], probe[1], probe[2]);
            Oneirgeo.LOGGER.info("[selftest] end probe {} = {}", pos.toShortString(), end.getBlockState(pos));
        }
        int walls = 0;
        int lit = 0;
        for (int x = -96; x <= 96; x++) {
            for (int z = -96; z <= 96; z++) {
                net.minecraft.world.level.block.state.BlockState state = end.getBlockState(new BlockPos(x, -998, z));
                if (!state.isAir()) {
                    walls++;
                }
                if (state.is(net.minecraft.world.level.block.Blocks.OCHRE_FROGLIGHT)) {
                    lit++;
                }
            }
        }
        Oneirgeo.LOGGER.info("[selftest] night sea buildings near origin: {} wall blocks at y -998, {} lit windows", walls, lit);
        for (int y = -1000; y <= -955; y++) {
            for (int x = 0; x < 56; x++) {
                for (int z = 0; z < 56; z++) {
                    if (end.getBlockState(new BlockPos(x, y, z)).is(net.minecraft.world.level.block.Blocks.SEA_LANTERN)) {
                        Oneirgeo.LOGGER.info("[selftest] night sea lighthouse lamp at {} {} {}", x, y, z);
                    }
                }
            }
        }
        com.eltavine.oneirgeo.world.gen.scene.end.EndIslands.Island found = null;
        int counted = 0;
        for (int r = 0; r <= 8 && found == null; r++) {
            for (int cx = -r; cx <= r && found == null; cx++) {
                for (int cz = -r; cz <= r && found == null; cz++) {
                    var island = com.eltavine.oneirgeo.world.gen.scene.end.EndIslands.inCell(seed, cx, cz);
                    if (island != null) {
                        counted++;
                        if (island.kind() == com.eltavine.oneirgeo.world.gen.scene.end.EndIslands.Kind.OBSERVATORY) {
                            found = island;
                        }
                    }
                }
            }
        }
        if (found == null) {
            Oneirgeo.LOGGER.warn("[selftest] no observatory among {} islands", counted);
            return;
        }
        Oneirgeo.LOGGER.info("[selftest] observatory at {} {} {} exit {}", found.x(), found.top(), found.z(), found.facing());
        checkSeams(end, found.x() >> 4, found.z() >> 4, 2);
        BlockPos centre = new BlockPos(found.x(), found.top() + 1, found.z());
        Oneirgeo.LOGGER.info("[selftest] observatory centre {} = {}, corridor {} = {}", centre.toShortString(), end.getBlockState(centre),
                centre.east(10).toShortString(), end.getBlockState(centre.east(10)));
    }

    /** Seams of the backrooms and poolrooms, and a mirror lit in the overworld leading to the mirror sea. */
    private static void checkDreams(MinecraftServer server) {
        ServerLevel backrooms = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.BACKROOMS);
        ServerLevel pools = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.POOLROOMS);
        ServerLevel sea = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.MIRROR_SEA);
        checkSeams(backrooms, 0, 0, 4);
        checkSeams(pools, 0, 0, 5);

        ServerLevel overworld = server.overworld();
        BlockPos base = new BlockPos(-30, 66, -30);
        for (int a = -1; a <= 2; a++) {
            for (int h = 0; h <= 4; h++) {
                boolean border = a == -1 || a == 2 || h == 0 || h == 4;
                overworld.setBlockAndUpdate(base.east(a).above(h), border
                        ? com.eltavine.oneirgeo.registry.OneirgeoBlocks.MIRROR_FRAME.defaultBlockState()
                        : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
        boolean lit = com.eltavine.oneirgeo.world.MirrorPortals.tryLight(overworld, base.above(2));
        Oneirgeo.LOGGER.info("[selftest] mirror lit: {}, inside = {}", lit, overworld.getBlockState(base.above(1)));
        net.minecraft.world.entity.animal.pig.Pig pig = net.minecraft.world.entity.EntityTypes.PIG.create(overworld,
                net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (pig != null && lit) {
            pig.snapTo(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 2.5, 0.0F, 0.0F);
            net.minecraft.world.level.portal.TeleportTransition to = com.eltavine.oneirgeo.world.MirrorPortals.destination(overworld, pig, base.above(1));
            if (to != null) {
                BlockPos at = BlockPos.containing(to.position());
                Oneirgeo.LOGGER.info("[selftest] mirror leads to {} {}: standing on {}", to.newLevel().dimension().identifier(), at.toShortString(),
                        to.newLevel().getBlockState(at));
            }
            pig.discard();
        }
        int props = 0;
        for (int x = -64; x < 64; x++) {
            for (int z = -64; z < 64; z++) {
                if (!sea.getBlockState(new BlockPos(x, 1, z)).isAir()) {
                    props++;
                }
            }
        }
        Oneirgeo.LOGGER.info("[selftest] mirror sea: surface {}, {} prop columns near origin, reflection at -2: {}",
                sea.getBlockState(BlockPos.ZERO), props, sea.getBlockState(new BlockPos(base.getX() + 4, -2, base.getZ())));
    }

    /** How many blocks of a kind the loaded chunks within {@code radius} chunks of a chunk hold, sections skipped by palette. */
    private static int count(ServerLevel level, int centerX, int centerZ, int radius, net.minecraft.world.level.block.Block block) {
        int found = 0;
        for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
            for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (net.minecraft.world.level.chunk.LevelChunkSection section : chunk.getSections()) {
                    if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(block))) {
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                if (section.getBlockState(x, y, z).is(block)) {
                                    found++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    private static @org.jspecify.annotations.Nullable BlockPos first(ServerLevel level, int centerX, int centerZ, int radius, net.minecraft.world.level.block.Block block) {
        return first(level, centerX, centerZ, radius, block, false);
    }

    /** The first block of a kind in the chunks around a chunk, or with {@code onPillar} the first standing on a quartz pillar. */
    private static @org.jspecify.annotations.Nullable BlockPos first(ServerLevel level, int centerX, int centerZ, int radius,
                                                                     net.minecraft.world.level.block.Block block, boolean onPillar) {
        for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
            for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                net.minecraft.world.level.chunk.LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    net.minecraft.world.level.chunk.LevelChunkSection section = sections[i];
                    if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(block))) {
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                if (section.getBlockState(x, y, z).is(block)) {
                                    BlockPos pos = new BlockPos((cx << 4) + x, level.getMinY() + (i << 4) + y, (cz << 4) + z);
                                    if (!onPillar || level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.QUARTZ_PILLAR)) {
                                        return pos;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** The new rooms: furniture in every dimension's structures, the steam vents of the boiler rooms. */
    private static void checkContent(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        ServerLevel nether = server.getLevel(net.minecraft.world.level.Level.NETHER);
        ServerLevel backrooms = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.BACKROOMS);
        Oneirgeo.LOGGER.info("[selftest] content overworld near spawn: {} televisions, {} telephones, {} chairs, {} clocks",
                count(overworld, 0, 0, 6, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEVISION),
                count(overworld, 0, 0, 6, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEPHONE),
                count(overworld, 0, 0, 6, com.eltavine.oneirgeo.registry.OneirgeoBlocks.WAITING_CHAIR),
                count(overworld, 0, 0, 6, com.eltavine.oneirgeo.registry.OneirgeoBlocks.STOPPED_CLOCK));
        Oneirgeo.LOGGER.info("[selftest] content nether near spawn: {} hospital beds, {} drips, {} monitors, {} steam vents",
                count(nether, 0, 0, 3, com.eltavine.oneirgeo.registry.OneirgeoBlocks.HOSPITAL_BED),
                count(nether, 0, 0, 3, com.eltavine.oneirgeo.registry.OneirgeoBlocks.IV_STAND),
                count(nether, 0, 0, 3, com.eltavine.oneirgeo.registry.OneirgeoBlocks.HEART_MONITOR),
                count(nether, 0, 0, 3, com.eltavine.oneirgeo.registry.OneirgeoBlocks.STEAM_VENT));
        Oneirgeo.LOGGER.info("[selftest] content backrooms near spawn: {} waiting chairs, {} telephones, {} televisions, {} clocks",
                count(backrooms, 0, 0, 4, com.eltavine.oneirgeo.registry.OneirgeoBlocks.WAITING_CHAIR),
                count(backrooms, 0, 0, 4, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEPHONE),
                count(backrooms, 0, 0, 4, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEVISION),
                count(backrooms, 0, 0, 4, com.eltavine.oneirgeo.registry.OneirgeoBlocks.STOPPED_CLOCK));
    }

    /**
     * The story, remembered by a fake player: entries come back in order and stop at seven, a
     * complete chapter hands over its tape, the star map entry carries the place of the time capsule,
     * the capsule is really there, and fragments from a Nether chest belong to the fever.
     */
    private static void checkStory(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        net.fabricmc.fabric.api.entity.FakePlayer fake = net.fabricmc.fabric.api.entity.FakePlayer.get(overworld);
        fake.setAttached(OneirgeoAttachments.STORY, com.eltavine.oneirgeo.story.StoryProgress.EMPTY);
        fake.getInventory().clearContent();
        int remembered = 0;
        while (com.eltavine.oneirgeo.story.Story.remember(fake, com.eltavine.oneirgeo.story.Chapter.OBSERVATORY)) {
            remembered++;
        }
        com.eltavine.oneirgeo.story.StoryProgress progress = com.eltavine.oneirgeo.story.Story.get(fake);
        boolean tape = false;
        for (int i = 0; i < fake.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = fake.getInventory().getItem(i);
            tape |= stack.is(com.eltavine.oneirgeo.registry.OneirgeoItems.VHS_TAPE)
                    && "observatory".equals(stack.get(com.eltavine.oneirgeo.registry.OneirgeoComponents.TAPE));
        }
        java.util.Optional<BlockPos> box = com.eltavine.oneirgeo.story.TimeCapsule.box(server);
        Oneirgeo.LOGGER.info("[selftest] story: remembered {} of the observatory before it stopped, complete {}, tape given {}, star map {} {} / capsule at {}",
                remembered, progress.complete(com.eltavine.oneirgeo.story.Chapter.OBSERVATORY), tape,
                progress.secretX(), progress.secretZ(), box.map(BlockPos::toShortString).orElse("none"));
        for (com.eltavine.oneirgeo.story.Chapter chapter : com.eltavine.oneirgeo.story.Chapter.values()) {
            while (com.eltavine.oneirgeo.story.Story.remember(fake, chapter)) {
                remembered++;
            }
        }
        Oneirgeo.LOGGER.info("[selftest] story: {} entries remembered in all, everything remembered {}; story attachment persistent {}, kept on death {}",
                com.eltavine.oneirgeo.story.Story.get(fake).total(), com.eltavine.oneirgeo.story.Story.get(fake).allComplete(),
                OneirgeoAttachments.STORY.isPersistent(), OneirgeoAttachments.STORY.copyOnDeath());
        if (box.isPresent()) {
            ServerLevel end = server.getLevel(net.minecraft.world.level.Level.END);
            BlockPos at = box.get();
            end.getChunk(at.getX() >> 4, at.getZ() >> 4);
            String contents = "-";
            if (end.getBlockEntity(at) instanceof net.minecraft.world.Container container) {
                StringBuilder items = new StringBuilder();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    net.minecraft.world.item.ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty()) {
                        items.append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
                        String tapeName = stack.get(com.eltavine.oneirgeo.registry.OneirgeoComponents.TAPE);
                        items.append(tapeName == null ? "" : "(" + tapeName + ")").append(' ');
                    }
                }
                contents = items.toString().trim();
            }
            Oneirgeo.LOGGER.info("[selftest] time capsule at {}: {} holding [{}], telescopes on its island {}",
                    at.toShortString(), end.getBlockState(at), contents,
                    count(end, at.getX() >> 4, at.getZ() >> 4, 1, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELESCOPE));
        }
        int[] stair = com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene.stair();
        Vec3 loopFeet = new Vec3(stair[0] + 0.5, stair[2] - stair[4] * 3 + 1.0625, stair[1] + 0.5);
        com.eltavine.oneirgeo.space.SeamVolume loop = com.eltavine.oneirgeo.space.SpaceQuery.seamFor(overworld, loopFeet, new Vec3(0.0, -0.1, -0.2), false);
        com.eltavine.oneirgeo.space.SeamVolume awakeLoop = com.eltavine.oneirgeo.space.SpaceQuery.seamFor(overworld, loopFeet, new Vec3(0.0, -0.1, -0.2), true);
        BlockPos firstStep = new BlockPos(stair[0], stair[2], stair[1]);
        BlockPos lastStep = new BlockPos(stair[0], stair[3], stair[1] + 1);
        Oneirgeo.LOGGER.info("[selftest] stair: first step {} = {}, last step {} = {}, the fourth turn loops {} (back up {}), lets the awake pass {}",
                firstStep.toShortString(), overworld.getBlockState(firstStep).getBlock().getDescriptionId(),
                lastStep.toShortString(), overworld.getBlockState(lastStep).getBlock().getDescriptionId(),
                loop != null, loop == null ? 0 : loop.dy(), loop != null && awakeLoop == null);
        com.eltavine.oneirgeo.space.Box room = com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene.finalRoom();
        fake.snapTo(room.centerX(), room.minY(), room.centerZ(), 0.0F, 0.0F);
        boolean firstVisit = com.eltavine.oneirgeo.story.Story.visitFinalRoom(fake);
        boolean secondVisit = com.eltavine.oneirgeo.story.Story.visitFinalRoom(fake);
        Oneirgeo.LOGGER.info("[selftest] final room {}: bed {}, first visit plays the last tape {}, second visit {}, final flag {}, beds in the room {}",
                room, overworld.getBlockState(new BlockPos((int) room.minX() + 3, (int) room.minY(), stair[1])).getBlock().getDescriptionId(),
                firstVisit, secondVisit, com.eltavine.oneirgeo.story.Story.get(fake).has(com.eltavine.oneirgeo.story.StoryProgress.FINAL),
                count(overworld, (int) room.centerX() >> 4, (int) room.centerZ() >> 4, 1, com.eltavine.oneirgeo.registry.OneirgeoBlocks.HOSPITAL_BED));
        ServerLevel nether = server.getLevel(net.minecraft.world.level.Level.NETHER);
        net.minecraft.world.level.storage.loot.LootTable table = server.reloadableRegistries().getLootTable(
                net.minecraft.world.level.storage.loot.BuiltInLootTables.NETHER_BRIDGE);
        int fragments = 0;
        int fever = 0;
        for (int i = 0; i < 40; i++) {
            net.minecraft.world.level.storage.loot.LootParams params = new net.minecraft.world.level.storage.loot.LootParams.Builder(nether)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, new Vec3(0.5, -900.0, 0.5))
                    .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
            for (net.minecraft.world.item.ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(com.eltavine.oneirgeo.registry.OneirgeoItems.MEMORY_FRAGMENT)) {
                    fragments++;
                    if (stack.get(com.eltavine.oneirgeo.registry.OneirgeoComponents.CHAPTER) == com.eltavine.oneirgeo.story.Chapter.FEVER) {
                        fever++;
                    }
                }
            }
        }
        Oneirgeo.LOGGER.info("[selftest] story: 40 Nether chests held {} memory fragments, {} of them of the fever", fragments, fever);
    }

    /** Each dimension's new mechanic, and the small mysteries, tried on a fake player. */
    private static void checkMechanics(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        net.fabricmc.fabric.api.entity.FakePlayer fake = net.fabricmc.fabric.api.entity.FakePlayer.get(overworld);
        fake.snapTo(16.5, 66.0, 0.5, 0.0F, 0.0F);
        StringBuilder mysteries = new StringBuilder();
        for (com.eltavine.oneirgeo.world.Mysteries.Kind kind : com.eltavine.oneirgeo.world.Mysteries.Kind.values()) {
            mysteries.append(kind).append('=').append(com.eltavine.oneirgeo.world.Mysteries.happen(fake, kind)).append(' ');
        }
        BlockPos phone = com.eltavine.oneirgeo.world.Mysteries.nearest(overworld, fake.blockPosition(), 24,
                state -> state.is(com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEPHONE));
        BlockPos screen = com.eltavine.oneirgeo.world.Mysteries.nearest(overworld, fake.blockPosition(), 24,
                state -> state.is(com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEVISION));
        Oneirgeo.LOGGER.info("[selftest] mysteries in the house: {}; telephone ringing {}, television on {}", mysteries,
                phone != null && overworld.getBlockState(phone).getValue(com.eltavine.oneirgeo.block.TelephoneBlock.RINGING),
                screen != null && overworld.getBlockState(screen).getValue(com.eltavine.oneirgeo.block.TelevisionBlock.PLAYING));

        double[] bore = null;
        long webSeed = 0L;
        if (overworld.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
            LayoutSampler sampler = generator.sampler(overworld);
            webSeed = sampler.layerSeed(sampler.layerIndex(0));
            for (int r = 0; r <= 6 && bore == null; r++) {
                for (int cx = -r; cx <= r && bore == null; cx++) {
                    for (int cz = -r; cz <= r && bore == null; cz++) {
                        if (Math.max(Math.abs(cx), Math.abs(cz)) == r) {
                            bore = com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene.axonBore(webSeed, 4 + cx, 4 + cz, -600, 600);
                        }
                    }
                }
            }
        }
        if (bore != null) {
            BlockPos at = BlockPos.containing(bore[0], bore[1], bore[2]);
            overworld.getChunk(at.getX() >> 4, at.getZ() >> 4);
            double[] flow = com.eltavine.oneirgeo.world.AxonCurrents.flowAt(overworld, bore[0], bore[1], bore[2]);
            fake.snapTo(bore[0], bore[1] - 0.9, bore[2], 0.0F, 0.0F);
            fake.setDeltaMovement(Vec3.ZERO);
            if (flow != null) {
                com.eltavine.oneirgeo.world.AxonCurrents.carry(overworld, fake, flow, 1);
            }
            Oneirgeo.LOGGER.info("[selftest] axon current: bore at {} is {}, flow {}, a player there is pushed at {}",
                    at.toShortString(), overworld.getBlockState(at).getBlock().getDescriptionId(),
                    flow == null ? "none" : String.format("(%.2f, %.2f, %.2f)", flow[0], flow[1], flow[2]),
                    String.format("%.3f", fake.getDeltaMovement().length()));
        } else {
            Oneirgeo.LOGGER.warn("[selftest] axon current: no hollow axon near spawn");
        }

        ServerLevel nether = server.getLevel(net.minecraft.world.level.Level.NETHER);
        BlockPos vent = first(nether, 0, 0, 3, com.eltavine.oneirgeo.registry.OneirgeoBlocks.STEAM_VENT);
        if (vent != null) {
            int bursts = 0;
            for (long t = 0; t < 90; t++) {
                bursts += com.eltavine.oneirgeo.block.SteamVentBlock.bursting(nether.getGameTime() + t, vent) ? 1 : 0;
            }
            net.minecraft.world.entity.animal.pig.Pig pig = net.minecraft.world.entity.EntityTypes.PIG.create(nether, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            double thrown = 0.0;
            if (pig != null) {
                pig.snapTo(vent.getX() + 0.5, vent.getY() + 0.75, vent.getZ() + 0.5, 0.0F, 0.0F);
                com.eltavine.oneirgeo.block.SteamVentBlock.launch(pig);
                thrown = pig.getDeltaMovement().y;
                pig.discard();
            }
            Oneirgeo.LOGGER.info("[selftest] steam vent at {}: bursting {} ticks of every 90, a burst throws up at {} blocks/tick, ceiling above open {}",
                    vent.toShortString(), bursts, thrown, nether.getBlockState(vent.above(7)).isAir());
        } else {
            Oneirgeo.LOGGER.warn("[selftest] steam vent: none found");
        }

        ServerLevel end = server.getLevel(net.minecraft.world.level.Level.END);
        java.util.Optional<BlockPos> capsule = com.eltavine.oneirgeo.story.TimeCapsule.box(server);
        BlockPos scope = capsule.isPresent() ? com.eltavine.oneirgeo.world.Mysteries.nearest(end, new BlockPos(-140, 27, -48), 30,
                state -> state.is(com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELESCOPE)) : null;
        Oneirgeo.LOGGER.info("[selftest] telescope near the spawn observatory: {}, it shows: {}", scope == null ? "none" : scope.toShortString(),
                scope == null ? "-" : com.eltavine.oneirgeo.block.TelescopeBlock.reading(end, scope).getString());

        ServerLevel sea = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.MIRROR_SEA);
        net.fabricmc.fabric.api.entity.FakePlayer swimmer = net.fabricmc.fabric.api.entity.FakePlayer.get(sea);
        swimmer.snapTo(3.5, 0.0625, 3.5, 0.0F, 0.0F);
        boolean onTop = com.eltavine.oneirgeo.world.MirrorDive.onMirror(swimmer);
        com.eltavine.oneirgeo.world.MirrorDive.dive(swimmer);
        Oneirgeo.LOGGER.info("[selftest] mirror sea: gravity flipped below {}, above {}; on the mirror {}, after diving at y {} in flipped gravity {}, on the mirror there {}; beds {}, televisions {}, telephones {}, suitcases {}",
                com.eltavine.oneirgeo.space.SpaceQuery.isFlipped(sea, 3.5, -5.0, 3.5), com.eltavine.oneirgeo.space.SpaceQuery.isFlipped(sea, 3.5, 5.0, 3.5),
                onTop, String.format("%.2f", swimmer.getY()), com.eltavine.oneirgeo.space.SpaceQuery.isFlipped(sea, swimmer.getX(), swimmer.getY() + 0.9, swimmer.getZ()),
                swimmer.getY() + swimmer.getBbHeight() > -0.3 && swimmer.getY() + swimmer.getBbHeight() <= 0.05,
                count(sea, 0, 0, 12, com.eltavine.oneirgeo.registry.OneirgeoBlocks.HOSPITAL_BED),
                count(sea, 0, 0, 12, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEVISION),
                count(sea, 0, 0, 12, com.eltavine.oneirgeo.registry.OneirgeoBlocks.TELEPHONE),
                count(sea, 0, 0, 12, net.minecraft.world.level.block.Blocks.CHEST));

        if (sea.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator seaGenerator) {
            LayoutSampler seaSampler = seaGenerator.sampler(sea);
            StringBuilder cases = new StringBuilder();
            for (BlockPos suitcase : com.eltavine.oneirgeo.world.gen.scene.dream.MirrorSeaScene.suitcases(seaSampler.layerSeed(seaSampler.layerIndex(0)), 12)) {
                sea.getChunk(suitcase.getX() >> 4, suitcase.getZ() >> 4);
                cases.append(suitcase.toShortString()).append('=').append(sea.getBlockState(suitcase).getBlock().getDescriptionId()).append(' ');
            }
            Oneirgeo.LOGGER.info("[selftest] mirror sea suitcases: {}", cases);
        }
        ServerLevel pools = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.POOLROOMS);
        int guardsBefore = pools.getEntitiesOfClass(com.eltavine.oneirgeo.entity.LifeguardEntity.class,
                new net.minecraft.world.phys.AABB(-96, pools.getMinY(), -96, 96, pools.getMaxY(), 96)).size();
        net.fabricmc.fabric.api.entity.FakePlayer bather = net.fabricmc.fabric.api.entity.FakePlayer.get(pools);
        BlockPos poolChair = first(pools, 0, 0, 3, com.eltavine.oneirgeo.registry.OneirgeoBlocks.WAITING_CHAIR, true);
        if (poolChair != null) {
            bather.snapTo(poolChair.getX() + 6.5, poolChair.getY() - 3.0, poolChair.getZ() + 0.5, 0.0F, 0.0F);
            com.eltavine.oneirgeo.entity.LifeguardEntity.post(pools, bather);
        }
        java.util.List<com.eltavine.oneirgeo.entity.LifeguardEntity> guards = pools.getEntitiesOfClass(com.eltavine.oneirgeo.entity.LifeguardEntity.class,
                new net.minecraft.world.phys.AABB(-96, pools.getMinY(), -96, 96, pools.getMaxY(), 96));
        Oneirgeo.LOGGER.info("[selftest] poolrooms: {} lifeguards before anyone swims, {} after a swimmer comes to the chair at {}",
                guardsBefore, guards.size(), poolChair == null ? "none" : poolChair.toShortString());
        String rescue = "-";
        if (!guards.isEmpty()) {
            com.eltavine.oneirgeo.entity.LifeguardEntity guard = guards.getFirst();
            net.fabricmc.fabric.api.entity.FakePlayer diver = net.fabricmc.fabric.api.entity.FakePlayer.get(pools);
            diver.snapTo(guard.getX() + 8.0, guard.getY() - 2.0, guard.getZ(), 0.0F, 0.0F);
            boolean rescued = guard.rescue(pools, diver);
            rescue = rescued + " to " + diver.blockPosition().toShortString() + " in " + pools.getBlockState(diver.blockPosition()).getBlock().getDescriptionId();
        }
        Oneirgeo.LOGGER.info("[selftest] poolrooms: {} lifeguards near spawn, {} lifeguard chairs, {} lockers; rescue {}",
                guards.size(), count(pools, 0, 0, 5, com.eltavine.oneirgeo.registry.OneirgeoBlocks.WAITING_CHAIR),
                count(pools, 0, 0, 5, net.minecraft.world.level.block.Blocks.CHEST), rescue);

        ServerLevel backrooms = server.getLevel(com.eltavine.oneirgeo.world.OneirgeoDimensions.BACKROOMS);
        net.fabricmc.fabric.api.entity.FakePlayer wanderer = net.fabricmc.fabric.api.entity.FakePlayer.get(backrooms);
        wanderer.snapTo(5.5, 1.0, 5.5, 0.0F, 0.0F);
        int out = com.eltavine.oneirgeo.world.LightsOut.goOut(wanderer);
        java.util.List<com.eltavine.oneirgeo.entity.NurseEntity> nurses = backrooms.getEntitiesOfClass(com.eltavine.oneirgeo.entity.NurseEntity.class,
                wanderer.getBoundingBox().inflate(48.0));
        int stillLit = com.eltavine.oneirgeo.world.Mysteries.all(backrooms, wanderer.blockPosition(), 20,
                state -> state.is(com.eltavine.oneirgeo.registry.OneirgeoBlocks.FLUORESCENT_LIGHT) && state.getValue(com.eltavine.oneirgeo.block.FluorescentLightBlock.LIT)).size();
        String bed = "-";
        if (!nurses.isEmpty()) {
            Vec3 before = wanderer.position();
            nurses.getFirst().putBackToBed(backrooms, wanderer);
            bed = "moved from " + BlockPos.containing(before).toShortString() + " to " + wanderer.level().dimension().identifier() + " " + wanderer.blockPosition().toShortString();
        }
        Oneirgeo.LOGGER.info("[selftest] backrooms: {} lights went out, {} still lit nearby, {} nurse(s) came; put back to bed: {}", out, stillLit, nurses.size(), bed);
        if (wanderer.level() != backrooms) {
            ((ServerLevel) wanderer.level()).removePlayerImmediately(wanderer, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        com.eltavine.oneirgeo.survival.Lucidity.set(fake, 0.3F);
        Oneirgeo.LOGGER.info("[selftest] mysteries: the camcorder glitch at low lucidity {}",
                com.eltavine.oneirgeo.world.Mysteries.happen(fake, com.eltavine.oneirgeo.world.Mysteries.Kind.GLITCH));
    }

    private static void run(MinecraftServer server) {
        int radius = Integer.getInteger("oneirgeo.selftest.radius", 3);
        try {
            for (ServerLevel level : server.getAllLevels()) {
                GenerationStats.reset();
                BlockPos spawn = level.getRespawnData().pos();
                int cx = spawn.getX() >> 4;
                int cz = spawn.getZ() >> 4;
                long started = System.nanoTime();
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        level.getChunk(cx + dx, cz + dz);
                    }
                }
                double seconds = (System.nanoTime() - started) / 1.0E9;
                LevelChunk chunk = level.getChunk(cx, cz);
                int top = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, spawn.getX() & 15, spawn.getZ() & 15);
                StringBuilder column = new StringBuilder();
                for (int y = 2; y >= -2; y--) {
                    column.append(y).append('=').append(level.getBlockState(new BlockPos(spawn.getX(), y, spawn.getZ())).getBlock().getDescriptionId()).append(' ');
                }
                StringBuilder maps = new StringBuilder();
                chunk.getHeightmaps().forEach(e -> maps.append(e.getKey()).append('=').append(e.getValue().getFirstAvailable(spawn.getX() & 15, spawn.getZ() & 15)).append(' '));
                Oneirgeo.LOGGER.info("[selftest] {} column {} heightmaps {}", level.dimension().identifier(), column, maps);
                String layer = "-";
                if (level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
                    LayoutSampler sampler = generator.sampler();
                    int index = sampler.layerIndex(spawn.getY());
                    layer = index < 0 ? "none" : sampler.layer(index).name() + "/" + sampler.entry(index, sampler.region(index, spawn.getX(), spawn.getZ()).entryIndex()).scene();
                }
                Oneirgeo.LOGGER.info("[selftest] {} spawn {} top {} layer {} biome {}: {} chunks in {}s, {} ms avg terrain ({} ms CPU), {} ms worst",
                        level.dimension().identifier(), spawn.toShortString(), top, layer,
                        level.getBiome(spawn).unwrapKey().map(k -> k.identifier().toString()).orElse("?"),
                        (2 * radius + 1) * (2 * radius + 1), String.format("%.2f", seconds),
                        String.format("%.2f", GenerationStats.averageMillis()), String.format("%.2f", GenerationStats.averageCpuMillis()),
                        String.format("%.2f", GenerationStats.worstMillis()));
            }
            ServerLevel overworld = server.overworld();
            Oneirgeo.LOGGER.info("[selftest] overworld terrain digest {}", Long.toHexString(digest(overworld, radius)));
            for (int[] probe : new int[][]{{-6, 28, -3}, {-8, 28, -3}, {-14, 50, -6}, {12, 66, 0}, {12, 67, 0}, {0, 64, 0}, {0, 70, 0}}) {
                BlockPos pos = new BlockPos(probe[0], probe[1], probe[2]);
                Oneirgeo.LOGGER.info("[selftest] probe {} = {}", pos.toShortString(), overworld.getBlockState(pos));
            }
            checkSeams(overworld, 6);
            checkNether(server.getLevel(net.minecraft.world.level.Level.NETHER));
            checkEnd(server.getLevel(net.minecraft.world.level.Level.END));
            checkDreams(server);
            for (String table : new String[]{"default", "the_nether", "the_end", "dream"}) {
                boolean loaded = server.reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.LOOT_TABLE, Oneirgeo.id("supply/" + table))) != net.minecraft.world.level.storage.loot.LootTable.EMPTY;
                Oneirgeo.LOGGER.info("[selftest] supply table {} loaded: {}", table, loaded);
            }
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
                    Oneirgeo.LOGGER.info("[selftest] {} terrain benchmark: {} ms per chunk, fastest of 6 rounds of 4 x 4 chunks on one thread",
                            level.dimension().identifier(), String.format("%.2f", generator.benchmarkTerrain(level, 8, 8, 4, 6)));
                }
            }
            checkContent(server);
            checkStory(server);
            checkMechanics(server);
            Oneirgeo.LOGGER.info("[selftest] neural cache for 5 x 5 chunks: {}",
                    com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene.measure(server.overworld().getSeed()));
            Runtime runtime = Runtime.getRuntime();
            System.gc();
            Oneirgeo.LOGGER.info("[selftest] heap after generating every spawn: {} MB used of {} MB",
                    (runtime.totalMemory() - runtime.freeMemory()) >> 20, runtime.maxMemory() >> 20);
            Oneirgeo.LOGGER.info("[selftest] passed");
        } catch (Throwable t) {
            Oneirgeo.LOGGER.error("[selftest] failed", t);
        }
    }
}
