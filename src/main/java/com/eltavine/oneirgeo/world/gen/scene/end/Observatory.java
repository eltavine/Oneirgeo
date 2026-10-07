package com.eltavine.oneirgeo.world.gen.scene.end;

import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import com.eltavine.oneirgeo.registry.OneirgeoItems;
import com.eltavine.oneirgeo.story.TimeCapsule;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

/**
 * A collapsed observatory: a drum with a broken dome over a round chamber, and four corridors that
 * each bend clockwise before reaching the outside. Everything visible from the corridors has
 * four-fold symmetry, so the seams in three of them can quietly turn the traveller onto the fourth:
 * whichever way you leave, you leave by the same door.
 */
final class Observatory {
    static final int DRUM = 18;
    private static final int CHAMBER = 6;
    private static final int DOME = 11;
    private static final int CORNER = 14;
    private static final int TRIGGER = 10;
    private static final int CORRIDOR_HEIGHT = 4;
    private static final int RIM = 9;

    private static final BlockState WALL = Blocks.END_STONE_BRICKS.defaultBlockState();
    private static final BlockState FLOOR = Blocks.PURPUR_BLOCK.defaultBlockState();
    private static final BlockState FLOOR_RING = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState SHELL = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState RIB = Blocks.PURPUR_PILLAR.defaultBlockState();
    private static final BlockState LIGHT = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
    private static final BlockState TUBE = Blocks.COPPER_BLOCK.waxed().oxidized().defaultBlockState();
    private static final BlockState LENS = Blocks.TINTED_GLASS.defaultBlockState();
    private static final BlockState ROD = Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP);
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private Observatory() {
    }

    static void build(SceneContext ctx, EndIslands.Island island) {
        int ox = island.x();
        int oz = island.z();
        int floor = island.top();
        int reach = DRUM + 8;
        if (!ctx.intersectsChunk(ox - reach, oz - reach, ox + reach, oz + reach)) {
            return;
        }
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int x = ctx.originX() + lx;
                int z = ctx.originZ() + lz;
                int rx = x - ox;
                int rz = z - oz;
                if (rx * rx + rz * rz > reach * reach) {
                    continue;
                }
                for (int y = floor - 1; y <= floor + RIM + DOME; y++) {
                    BlockState state = blockAt(island.hash(), rx, y - floor, rz);
                    if (state != null) {
                        ctx.place(x, y, z, state);
                    }
                }
                BlockState debris = debris(island.hash(), rx, rz);
                if (debris != null) {
                    int h = 1 + (int) Math.floorMod(Hash.of(island.hash(), rx, rz, 0xDEB), 2L);
                    for (int i = 1; i <= h; i++) {
                        ctx.place(x, floor + i, z, debris);
                    }
                }
            }
        }
    }

    static void registerSpace(SceneContext ctx, EndIslands.Island island) {
        int ox = island.x();
        int oz = island.z();
        int floor = island.top();
        Direction exit = island.facing();
        ctx.protect(new Box(ox - DRUM - 1, floor - 1, oz - DRUM - 1, ox + DRUM + 2, floor + RIM + DOME + 1, oz + DRUM + 2));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d == exit) {
                continue;
            }
            Direction e = d.getClockWise();
            int x0 = ox + TRIGGER * d.getStepX() - e.getStepX();
            int z0 = oz + TRIGGER * d.getStepZ() - e.getStepZ();
            int x1 = ox + TRIGGER * d.getStepX() + e.getStepX();
            int z1 = oz + TRIGGER * d.getStepZ() + e.getStepZ();
            Box trigger = new Box(Math.min(x0, x1), floor + 1, Math.min(z0, z1),
                    Math.max(x0, x1) + 1, floor + 1 + CORRIDOR_HEIGHT, Math.max(z0, z1) + 1);
            int turns = Math.floorMod(exit.get2DDataValue() - d.get2DDataValue(), 4);
            int dx = TRIGGER * (exit.getStepX() - d.getStepX());
            int dz = TRIGGER * (exit.getStepZ() - d.getStepZ());
            ctx.seam(new SeamVolume(trigger, dx, 0, dz, turns, d.get3DDataValue()));
        }
    }

    /**
     * The barrel under the telescope tube, four telescopes facing out (all four, so the chamber still
     * looks the same from every corridor), and under one observatory of the cemetery, the tin box.
     */
    static void decorate(DecorationContext ctx, EndIslands.Island island) {
        BlockPos pos = new BlockPos(island.x(), island.top() + 1, island.z());
        if (ctx.canPlace(pos)) {
            ctx.barrel(pos, BuiltInLootTables.END_CITY_TREASURE);
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            ctx.furniture(pos.relative(d, 3), OneirgeoBlocks.TELESCOPE, d);
        }
        if (TimeCapsule.is(ctx.seed(), island)) {
            BlockPos box = TimeCapsule.box(island);
            if (ctx.canPlace(box)) {
                ItemStack tape = new ItemStack(OneirgeoItems.VHS_TAPE);
                tape.set(OneirgeoComponents.TAPE, "capsule");
                ctx.chestWith(box, Direction.NORTH, List.of(new ItemStack(OneirgeoItems.CHILD_DRAWING), tape, new ItemStack(OneirgeoItems.GLASS_MARBLE)));
            }
        }
    }

    /**
     * The block at an offset from the chamber centre, with {@code h} measured from the floor; null
     * keeps whatever the island put there. Must stay invariant under quarter turns of (rx, rz).
     */
    private static @Nullable BlockState blockAt(long hash, int rx, int h, int rz) {
        int d2 = rx * rx + rz * rz;
        if (corridor(rx, rz) && h >= 1 && h <= CORRIDOR_HEIGHT) {
            return AIR;
        }
        if (h == CORRIDOR_HEIGHT + 1 && d2 < (DRUM - 0.5) * (DRUM - 0.5) && corridorCentre(rx, rz)) {
            return LIGHT;
        }
        if (h <= RIM) {
            if (d2 > (DRUM + 0.5) * (DRUM + 0.5)) {
                return null;
            }
            if (d2 <= (CHAMBER + 0.5) * (CHAMBER + 0.5)) {
                if (h == -1) {
                    return WALL;
                }
                if (h == 0) {
                    return d2 >= (CHAMBER - 1) * (CHAMBER - 1) || d2 <= 2 ? FLOOR_RING : FLOOR;
                }
                if (rx == 0 && rz == 0) {
                    return h == 1 ? null : TUBE;
                }
                if (h == 1 && Math.abs(rx) == 4 && Math.abs(rz) == 4) {
                    return ROD;
                }
                return AIR;
            }
            if (h == RIM && d2 > (DRUM - 0.5) * (DRUM - 0.5)) {
                return canonical(rx, rz) % 2 == 0 ? WALL : AIR;
            }
            return h % 4 == 0 && d2 > (DRUM - 0.5) * (DRUM - 0.5) ? RIB : WALL;
        }
        int dy = h - RIM;
        double dist = Math.sqrt(d2 + (double) dy * dy);
        if (rx == 0 && rz == 0 && dy <= DOME - 5) {
            return dy == DOME - 5 ? LENS : TUBE;
        }
        if (dist < DOME - 1) {
            return AIR;
        }
        if (dist <= DOME) {
            if (dy >= DOME - 3 || Hash.chance(Hash.of(hash, canonical(rx, rz), dy, 0xD0E), 0.36)) {
                return AIR;
            }
            return canonical(rx, rz) % 5 == 0 ? RIB : SHELL;
        }
        return null;
    }

    /** Inside one of the four L-shaped corridors (segment out of the chamber, then a clockwise bend). */
    private static boolean corridor(int rx, int rz) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Direction e = d.getClockWise();
            int a = rx * d.getStepX() + rz * d.getStepZ();
            int b = rx * e.getStepX() + rz * e.getStepZ();
            if (a >= CHAMBER && a <= CORNER + 1 && Math.abs(b) <= 1) {
                return true;
            }
            if (a >= CORNER - 1 && a <= CORNER + 1 && b >= -1 && b <= DRUM + 6) {
                return true;
            }
        }
        return false;
    }

    private static boolean corridorCentre(int rx, int rz) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Direction e = d.getClockWise();
            int a = rx * d.getStepX() + rz * d.getStepZ();
            int b = rx * e.getStepX() + rz * e.getStepZ();
            if (b == 0 && a > CHAMBER && a <= CORNER) {
                return true;
            }
            if (a == CORNER && b >= 0 && b <= DRUM) {
                return true;
            }
        }
        return false;
    }

    /** Same value for all four quarter-turn images of an offset. */
    private static int canonical(int rx, int rz) {
        int x = rx;
        int z = rz;
        for (int i = 0; i < 4 && !(x > 0 && z >= 0) && !(x == 0 && z == 0); i++) {
            int t = x;
            x = -z;
            z = t;
        }
        return (x * 73 + z * 31) & 0x7FFF;
    }

    /** Fallen pieces of the dome around the drum; outside, so they may be asymmetric. */
    private static @Nullable BlockState debris(long hash, int rx, int rz) {
        int d2 = rx * rx + rz * rz;
        if (d2 <= (DRUM + 1.5) * (DRUM + 1.5) || d2 > (DRUM + 7) * (DRUM + 7) || corridor(rx, rz)) {
            return null;
        }
        long h = Hash.of(hash, rx, rz, 0xDEB);
        if (!Hash.chance(h, 0.07)) {
            return null;
        }
        return Hash.chance(Hash.next(h, 1), 0.7) ? SHELL : RIB;
    }
}
