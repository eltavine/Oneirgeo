package com.eltavine.oneirgeo.story;

import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import com.eltavine.oneirgeo.world.gen.scene.end.EndIslands;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * The tin box the dreamer and their father buried under an observatory. Which observatory only the
 * world seed knows: the one closest to a point far out in the star cemetery. The numbers on the
 * back of the star map, remembered in the observatory chapter, are where to dig.
 */
public final class TimeCapsule {
    /** Below the telescope, under the chamber floor and a few blocks of stone. */
    public static final int DEPTH = 4;
    private static final Map<Long, Optional<EndIslands.Island>> CACHE = new ConcurrentHashMap<>();

    private TimeCapsule() {
    }

    public static Optional<EndIslands.Island> island(long seed) {
        return CACHE.computeIfAbsent(seed, TimeCapsule::find);
    }

    public static boolean is(long seed, EndIslands.Island island) {
        return island(seed).map(found -> found.x() == island.x() && found.z() == island.z()).orElse(false);
    }

    public static BlockPos box(EndIslands.Island island) {
        return new BlockPos(island.x(), island.top() - DEPTH, island.z());
    }

    /** Where the box is on this server, if the End is a dream the mod made. */
    public static Optional<BlockPos> box(MinecraftServer server) {
        OptionalLong seed = seed(server);
        return seed.isPresent() ? island(seed.getAsLong()).map(TimeCapsule::box) : Optional.empty();
    }

    /** The seed the star cemetery grows its islands from. */
    public static OptionalLong seed(MinecraftServer server) {
        ServerLevel end = server.getLevel(Level.END);
        if (end != null && end.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
            LayoutSampler sampler = generator.sampler(end);
            int layer = sampler.layerIndex(0);
            if (layer >= 0) {
                return OptionalLong.of(sampler.layerSeed(layer));
            }
        }
        return OptionalLong.empty();
    }

    private static Optional<EndIslands.Island> find(long seed) {
        long h = Hash.of(seed, 0x7C4F);
        double angle = Hash.unit(h) * Math.PI * 2.0;
        double distance = 700.0 + Hash.unit(Hash.next(h, 1)) * 900.0;
        int tx = (int) Math.round(EndIslands.SPAWN_X + Math.cos(angle) * distance);
        int tz = (int) Math.round(EndIslands.SPAWN_Z + Math.sin(angle) * distance);
        int cx = Math.floorDiv(tx, EndIslands.CELL);
        int cz = Math.floorDiv(tz, EndIslands.CELL);
        EndIslands.Island best = null;
        long bestDistance = Long.MAX_VALUE;
        for (int ring = 0; ring <= 24 && best == null; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    EndIslands.Island island = EndIslands.inCell(seed, cx + dx, cz + dz);
                    if (island == null || island.kind() != EndIslands.Kind.OBSERVATORY) {
                        continue;
                    }
                    long ex = island.x() - tx;
                    long ez = island.z() - tz;
                    if (ex * ex + ez * ez < bestDistance) {
                        bestDistance = ex * ex + ez * ez;
                        best = island;
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
