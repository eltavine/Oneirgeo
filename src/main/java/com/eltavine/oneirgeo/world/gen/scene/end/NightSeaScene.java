package com.eltavine.oneirgeo.world.gen.scene.end;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Frame;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * A sea of black water a thousand blocks under the cemetery, over a floor that floats on nothing.
 * White houses drift on it, some half drowned, and lighthouses that light no shore.
 */
public final class NightSeaScene implements Scene {
    public static final int SURFACE = -1000;
    private static final int FLOOR_TOP = -1041;
    private static final int FLOOR_BOTTOM = -1080;
    private static final int CELL = 56;
    private static final BlockState FLOOR = Blocks.END_STONE.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState PLASTER = OneirgeoBlocks.NIGHT_PLASTER.defaultBlockState();
    private static final BlockState TRIM = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState WINDOW = Blocks.TINTED_GLASS.defaultBlockState();
    /** Someone left a light on; seen from far across the black water. */
    private static final BlockState LIT_WINDOW = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState LAMP = Blocks.SEA_LANTERN.defaultBlockState();

    private record Building(int x, int z, Direction facing, boolean tower, int halfA, int halfB, int wall, int sink, int drown,
                            boolean exit, boolean barrel) {
    }

    @Override
    public void generate(SceneContext ctx) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                ctx.fill(lx, lz, FLOOR_BOTTOM, FLOOR_TOP, FLOOR);
                ctx.fill(lx, lz, FLOOR_TOP + 1, SURFACE, WATER);
            }
        }
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int cz = Math.floorDiv(minZ - 12, CELL); cz <= Math.floorDiv(minZ + 27, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 12, CELL); cx <= Math.floorDiv(minX + 27, CELL); cx++) {
                Building b = building(ctx.seed(), cx, cz);
                if (b != null && ctx.intersectsChunk(b.x - 8, b.z - 8, b.x + 8, b.z + 8)) {
                    if (b.tower) {
                        tower(ctx, b);
                    } else {
                        house(ctx, b);
                    }
                }
            }
        }
    }

    @Override
    public void decorate(DecorationContext ctx) {
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int cz = Math.floorDiv(minZ - 12, CELL); cz <= Math.floorDiv(minZ + 27, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 12, CELL); cx <= Math.floorDiv(minX + 27, CELL); cx++) {
                Building b = building(ctx.seed(), cx, cz);
                if (b == null || b.tower || !b.barrel) {
                    continue;
                }
                Frame f = new Frame(b.x, SURFACE - b.drown, b.z, b.facing);
                BlockPos pos = new BlockPos(f.worldX(b.halfA - 1, -b.halfB + 1), f.y() + 1, f.worldZ(b.halfA - 1, -b.halfB + 1));
                if (ctx.canPlace(pos)) {
                    ctx.barrel(pos, BuiltInLootTables.SHIPWRECK_SUPPLY);
                }
            }
        }
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return SURFACE;
    }

    private static Building building(long seed, int cx, int cz) {
        long h = Hash.of(seed, cx, cz, 0x5EA);
        boolean beacon = cx == 0 && cz == 0;
        if (!beacon && !Hash.chance(h, 0.65)) {
            return null;
        }
        int x = cx * CELL + Hash.range(Hash.next(h, 1), 10, CELL - 10);
        int z = cz * CELL + Hash.range(Hash.next(h, 2), 10, CELL - 10);
        Direction facing = Direction.from2DDataValue(Hash.range(Hash.next(h, 3), 0, 3));
        boolean tower = beacon || Hash.chance(Hash.next(h, 4), 0.3);
        if (tower) {
            return new Building(x, z, facing, true, Hash.range(Hash.next(h, 5), 3, 4), 0, Hash.range(Hash.next(h, 6), 18, 40), 6, 0, false, false);
        }
        int wall = Hash.range(Hash.next(h, 7), 5, 9);
        int drown = Hash.chance(Hash.next(h, 8), 0.38) ? Hash.range(Hash.next(h, 9), 3, wall - 1) : 0;
        return new Building(x, z, facing, false, Hash.range(Hash.next(h, 5), 3, 6), Hash.range(Hash.next(h, 6), 3, 6), wall,
                Hash.range(Hash.next(h, 10), 3, 8), drown, drown == 0 && Hash.chance(Hash.next(h, 11), 0.18),
                Hash.chance(Hash.next(h, 12), 0.35));
    }

    private static void house(SceneContext ctx, Building b) {
        Frame f = new Frame(b.x, SURFACE - b.drown, b.z, b.facing);
        int floor = f.y();
        int wa = b.halfA;
        int wb = b.halfB;
        for (int a = -wa; a <= wa; a++) {
            for (int bb = -wb; bb <= wb; bb++) {
                int x = f.worldX(a, bb);
                int z = f.worldZ(a, bb);
                if (!ctx.ownsWorld(x, z)) {
                    continue;
                }
                for (int y = floor - b.sink; y <= floor; y++) {
                    ctx.place(x, y, z, PLASTER);
                }
                boolean edgeA = Math.abs(a) == wa;
                boolean edgeB = Math.abs(bb) == wb;
                for (int y = floor + 1; y <= floor + b.wall; y++) {
                    BlockState state;
                    if (edgeA && edgeB) {
                        state = TRIM;
                    } else if (edgeA || edgeB) {
                        int along = edgeA ? bb : a;
                        boolean window = Math.floorMod(along, 3) == 0 && (y == floor + 2 || y == floor + 3);
                        if (window) {
                            state = Hash.chance(Hash.of(b.x * 31L + b.z, a, bb, y), 0.22) ? LIT_WINDOW : WINDOW;
                        } else if (y > floor + 1 && Hash.chance(Hash.of(b.x * 31L + b.z, a, bb, y + 0x401E), 0.1)) {
                            state = y <= SURFACE ? WATER : AIR;
                        } else {
                            state = PLASTER;
                        }
                        if (bb == wb && a == 0 && y <= floor + 2) {
                            state = y <= SURFACE ? WATER : AIR;
                        }
                    } else {
                        state = y <= SURFACE ? WATER : AIR;
                    }
                    ctx.place(x, y, z, state);
                }
                for (int k = 1; k <= wa + 1; k++) {
                    int y = floor + b.wall + k;
                    if (Math.abs(a) == wa - k + 1) {
                        ctx.place(x, y, z, Hash.chance(Hash.of(b.x * 31L + b.z, a, bb, y + 0x2007), 0.18) ? AIR : TRIM);
                    } else if (Math.abs(a) < wa - k + 1) {
                        ctx.place(x, y, z, edgeB ? PLASTER : AIR);
                    }
                }
            }
        }
        if (b.exit) {
            BlockState door = OneirgeoBlocks.EXIT_DOOR.defaultBlockState().setValue(DoorBlock.FACING, f.world(Direction.SOUTH));
            int x = f.worldX(0, -wb);
            int z = f.worldZ(0, -wb);
            ctx.place(x, floor + 1, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            ctx.place(x, floor + 2, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    private static void tower(SceneContext ctx, Building b) {
        Frame f = new Frame(b.x, SURFACE, b.z, b.facing);
        int r = b.halfA;
        int top = SURFACE + b.wall;
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, f.world(Direction.SOUTH));
        for (int a = -r - 1; a <= r + 1; a++) {
            for (int bb = -r - 1; bb <= r + 1; bb++) {
                int x = f.worldX(a, bb);
                int z = f.worldZ(a, bb);
                if (!ctx.ownsWorld(x, z)) {
                    continue;
                }
                double d = Math.sqrt(a * a + bb * bb);
                if (d > r + 0.5) {
                    continue;
                }
                boolean wall = d > r - 0.5;
                for (int y = SURFACE - b.sink; y <= top + 1; y++) {
                    BlockState state;
                    if (y <= SURFACE || y == top + 1) {
                        state = y == top + 1 ? TRIM : PLASTER;
                    } else if (y >= top - 3) {
                        if (y == top - 3) {
                            state = a == 0 && bb == -r + 1 ? ladder : TRIM;
                        } else if (wall) {
                            state = GLASS;
                        } else {
                            state = a == 0 && bb == 0 && y == top - 2 ? LAMP : AIR;
                        }
                    } else if (wall) {
                        boolean door = bb == r && a == 0 && y <= SURFACE + 2;
                        state = door ? AIR : (Math.floorMod(y - SURFACE, 6) == 0 ? TRIM : PLASTER);
                    } else {
                        state = a == 0 && bb == -r + 1 ? ladder : AIR;
                    }
                    ctx.place(x, y, z, state);
                }
            }
        }
    }
}
