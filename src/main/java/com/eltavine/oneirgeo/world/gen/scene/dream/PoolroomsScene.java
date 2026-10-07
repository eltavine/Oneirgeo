package com.eltavine.oneirgeo.world.gen.scene.dream;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * White tile and still water, hall upon hall for the whole height of the world: pools lit from
 * below, colonnades with shallow channels, walls of round arches. Some stairwells spiral down around a
 * glass column of water and never arrive: every turn is the same turn.
 */
public final class PoolroomsScene implements Scene {
    static final int FLOOR = 16;
    private static final int CELL = 24;
    private static final int HALL = 10;

    private static final BlockState TILE = OneirgeoBlocks.POOL_TILE.defaultBlockState();
    private static final BlockState BLUE = OneirgeoBlocks.POOL_TILE_BLUE.defaultBlockState();
    private static final BlockState LIGHT = OneirgeoBlocks.POOL_LIGHT.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState GRIME = Blocks.DYED_TERRACOTTA.pick(net.minecraft.world.item.DyeColor.LIGHT_GRAY).defaultBlockState();

    private enum Kind {
        POOL,
        COLONNADE,
        ARCHES,
        STAIRWELL
    }

    private static Kind kind(long seed, int floor, int cx, int cz) {
        double roll = Hash.unit(Hash.of(seed, floor, cx, cz, 0x9001));
        if (roll < 0.4) {
            return Kind.POOL;
        }
        if (roll < 0.7) {
            return Kind.COLONNADE;
        }
        return roll < 0.94 ? Kind.ARCHES : Kind.STAIRWELL;
    }

    private static boolean wakeDoor(long seed, int floor, int cx, int cz) {
        return Hash.chance(Hash.of(seed, floor, cx, cz, 0x3A4E), 0.035);
    }

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int floorMin = Math.floorDiv(ctx.minY() + 3, FLOOR);
        int floorMax = Math.floorDiv(ctx.maxY() - 12, FLOOR);
        for (int floor = floorMin; floor <= floorMax; floor++) {
            this.floor(ctx, seed, floor);
        }
        for (int floor = floorMin + 3; floor <= floorMax; floor++) {
            for (int cz = Math.floorDiv(ctx.originZ(), CELL); cz <= Math.floorDiv(ctx.originZ() + 15, CELL); cz++) {
                for (int cx = Math.floorDiv(ctx.originX(), CELL); cx <= Math.floorDiv(ctx.originX() + 15, CELL); cx++) {
                    if (kind(seed, floor, cx, cz) == Kind.STAIRWELL) {
                        this.stairwell(ctx, floor, cx, cz);
                    }
                }
            }
        }
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                ctx.set(lx, ctx.minY(), lz, bedrock);
                ctx.set(lx, ctx.maxY(), lz, bedrock);
            }
        }
    }

    /** Slab y0-3..y0, hall y0+1..y0+10, ceiling y0+11..y0+12. */
    private void floor(SceneContext ctx, long seed, int floor) {
        int y0 = floor * FLOOR;
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                if (!ctx.owns(lx, lz)) {
                    continue;
                }
                int x = ctx.originX() + lx;
                int z = ctx.originZ() + lz;
                int cx = Math.floorDiv(x, CELL);
                int cz = Math.floorDiv(z, CELL);
                int u = x - cx * CELL;
                int v = z - cz * CELL;
                ctx.fill(lx, lz, y0 - 3, y0, TILE);
                ctx.fill(lx, lz, y0 + 1, y0 + HALL, AIR);
                boolean lamp = Math.floorMod(u, 12) == 6 && Math.floorMod(v, 12) == 6 && !Hash.chance(Hash.of(seed, x, y0, z, 0x1A3), 0.4);
                ctx.set(lx, y0 + HALL + 1, lz, lamp ? LIGHT : TILE);
                if (Hash.chance(Hash.of(seed, x, y0, z, 0x6213), 0.07)) {
                    ctx.set(lx, y0, lz, GRIME);
                }
                ctx.set(lx, y0 + HALL + 2, lz, TILE);
                switch (kind(seed, floor, cx, cz)) {
                    case POOL -> this.pool(ctx, lx, lz, y0, u, v, Hash.chance(Hash.of(seed, floor, cx, cz, 0xD27), 0.25));
                    case COLONNADE -> this.colonnade(ctx, seed, lx, lz, y0, u, v, floor, cx, cz);
                    case ARCHES -> this.arches(ctx, seed, lx, lz, y0, u, v, floor, cx, cz);
                    default -> {
                    }
                }
            }
        }
    }

    /** A sunken pool lit from below; a quarter of them have been drained and left empty. */
    private void pool(SceneContext ctx, int lx, int lz, int y0, int u, int v, boolean drained) {
        if (u == 1 && v == 12 && !drained) {
            ctx.fill(lx, lz, y0 + 1, y0 + 3, Blocks.QUARTZ_PILLAR.defaultBlockState());
            ctx.set(lx, y0 + 4, lz, OneirgeoBlocks.WAITING_CHAIR.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST));
        }
        boolean inside = u >= 3 && u <= 20 && v >= 3 && v <= 20;
        boolean rim = u >= 2 && u <= 21 && v >= 2 && v <= 21 && !inside;
        if (rim) {
            ctx.set(lx, y0, lz, BLUE);
        } else if (inside) {
            boolean shallow = u <= 6;
            ctx.fill(lx, lz, shallow ? y0 : y0 - 2, y0, drained ? AIR : WATER);
            boolean lamp = !shallow && Math.floorMod(u, 6) == 0 && Math.floorMod(v, 6) == 0;
            ctx.set(lx, shallow ? y0 - 1 : y0 - 3, lz, lamp ? LIGHT : BLUE);
        }
    }

    private void colonnade(SceneContext ctx, long seed, int lx, int lz, int y0, int u, int v, int floor, int cx, int cz) {
        int pu = Math.floorMod(u, 8);
        int pv = Math.floorMod(v, 8);
        if ((pu == 3 || pu == 4) && (pv == 3 || pv == 4)) {
            ctx.fill(lx, lz, y0 + 1, y0 + HALL, TILE);
            ctx.set(lx, y0 + 1, lz, BLUE);
            return;
        }
        if (v == 11 || v == 12) {
            ctx.set(lx, y0, lz, WATER);
            ctx.set(lx, y0 - 1, lz, BLUE);
        }
        if (wakeDoor(seed, floor, cx, cz) && v == 6 && u >= 11 && u <= 13) {
            if (u == 12) {
                BlockState door = OneirgeoBlocks.WAKE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
                ctx.set(lx, y0 + 1, lz, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
                ctx.set(lx, y0 + 2, lz, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
                ctx.set(lx, y0 + 3, lz, BLUE);
            } else {
                ctx.fill(lx, lz, y0 + 1, y0 + 3, BLUE);
            }
        }
    }

    /** Walls on the west and north edge of the cell, each pierced by a round arch. */
    private void arches(SceneContext ctx, long seed, int lx, int lz, int y0, int u, int v, int floor, int cx, int cz) {
        int along;
        if (u == 0) {
            along = v;
        } else if (v == 0) {
            along = u;
        } else {
            return;
        }
        int d = Math.abs(along - 12);
        int archTop = d <= 3 ? y0 + 5 + (int) Math.sqrt(16 - d * d) : y0;
        for (int y = y0 + 1; y <= y0 + HALL; y++) {
            if (y > archTop) {
                ctx.set(lx, y, lz, y == y0 + 1 ? BLUE : TILE);
            }
        }
        if (u == 0 && v == 5 && wakeDoor(seed, floor, cx, cz)) {
            BlockState door = OneirgeoBlocks.WAKE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST);
            ctx.set(lx, y0 + 1, lz, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            ctx.set(lx, y0 + 2, lz, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    /**
     * A shaft 7 by 7 at the middle of the cell, falling three floors: a square spiral of sixteen steps
     * per turn (one block each) round a glass tube of water. Anyone going down past the third turn is
     * lifted back by one turn, deep enough that a step always hangs overhead and everything looks the same.
     */
    private void stairwell(SceneContext ctx, int floor, int cx, int cz) {
        int y0 = floor * FLOOR;
        int x0 = cx * CELL + 9;
        int z0 = cz * CELL + 9;
        if (!ctx.intersectsChunk(x0, z0, x0 + 6, z0 + 6)) {
            return;
        }
        int bottom = y0 - 3 * FLOOR - 3;
        for (int u = 0; u <= 6; u++) {
            for (int v = 0; v <= 6; v++) {
                int x = x0 + u;
                int z = z0 + v;
                boolean wall = u == 0 || u == 6 || v == 0 || v == 6;
                for (int y = bottom; y <= y0 + 1; y++) {
                    BlockState state;
                    if (y == bottom) {
                        state = TILE;
                    } else if (wall) {
                        if (y == y0 + 1) {
                            state = u == 1 && v == 0 ? AIR : GLASS;
                        } else {
                            state = y > y0 ? AIR : TILE;
                        }
                    } else if (u >= 2 && u <= 4 && v >= 2 && v <= 4) {
                        state = u == 3 && v == 3 ? (y <= y0 - 3 ? WATER : AIR) : (y <= y0 - 3 ? GLASS : AIR);
                    } else {
                        int index = ring(u - 1, v - 1);
                        int depth = y0 - 1 - y;
                        state = depth >= 0 && Math.floorMod(depth, 16) == index ? TILE : AIR;
                    }
                    if (y > y0 && !wall) {
                        continue;
                    }
                    ctx.place(x, y, z, state);
                }
            }
        }
        ctx.seam(SeamVolume.translate(new Box(x0 + 1, y0 - 44, z0 + 1, x0 + 6, y0 - 43, z0 + 6), 0, FLOOR, 0, Direction.DOWN));
        ctx.protect(new Box(x0, bottom, z0, x0 + 7, y0 + 2, z0 + 7));
    }

    /** Clockwise position of a cell on the edge of the 5 by 5 interior, starting at its north-west corner. */
    private static int ring(int a, int b) {
        if (b == 0) {
            return a;
        }
        if (a == 4) {
            return 4 + b;
        }
        if (b == 4) {
            return 8 + (4 - a);
        }
        return 12 + (4 - b);
    }

    /** In some colonnades, a row of benches by a locker someone left full. */
    @Override
    public void decorate(DecorationContext ctx) {
        long seed = ctx.seed();
        int floorMin = Math.floorDiv(ctx.info().minY() + 3, FLOOR);
        int floorMax = Math.floorDiv(ctx.info().maxY() - 12, FLOOR);
        for (int floor = floorMin; floor <= floorMax; floor++) {
            int y0 = floor * FLOOR;
            for (int cz = Math.floorDiv(ctx.originZ(), CELL); cz <= Math.floorDiv(ctx.originZ() + 15, CELL); cz++) {
                for (int cx = Math.floorDiv(ctx.originX(), CELL); cx <= Math.floorDiv(ctx.originX() + 15, CELL); cx++) {
                    Kind kind = kind(seed, floor, cx, cz);
                    long h = Hash.of(seed, floor, cx, cz, 0x11FE);
                    if (kind == Kind.COLONNADE && Hash.chance(Hash.next(h, 1), 0.3)) {
                        net.minecraft.core.BlockPos locker = new net.minecraft.core.BlockPos(cx * CELL + 1, y0 + 1, cz * CELL + 1);
                        if (ctx.canPlace(locker) && ctx.getBlock(locker).isAir()) {
                            ctx.chest(locker, net.minecraft.core.Direction.SOUTH, net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_SUPPLY);
                        }
                        for (int u = 2; u <= 5; u++) {
                            ctx.furniture(new net.minecraft.core.BlockPos(cx * CELL + u, y0 + 1, cz * CELL + 1), OneirgeoBlocks.WAITING_CHAIR,
                                    net.minecraft.core.Direction.SOUTH);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return 0;
    }
}
