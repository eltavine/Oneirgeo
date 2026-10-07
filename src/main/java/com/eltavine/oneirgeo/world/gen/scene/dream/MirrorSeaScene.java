package com.eltavine.oneirgeo.world.gen.scene.dream;

import com.eltavine.oneirgeo.block.MirrorPortalBlock;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.Frame;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * A film of still water at noon, as wide as the world. Odd things stand on it (street lamps, an
 * armchair, a stair to nowhere, broken columns, doors, mirrors), and every one of them hangs upside
 * down beneath the surface as well.
 */
public final class MirrorSeaScene implements Scene {
    private static final int CELL = 40;
    private static final BlockState SURFACE = OneirgeoBlocks.MIRROR_SURFACE.defaultBlockState();

    private enum Prop {
        LAMP,
        CHAIR,
        STAIRS,
        COLUMN,
        DOOR,
        MIRROR,
        BEDSIDE,
        TELEVISION,
        TELEPHONE,
        SUITCASE
    }

    private record Placed(Prop prop, int x, int z, Direction facing, long hash) {
    }

    private static Placed placed(long seed, int cx, int cz) {
        long h = Hash.of(seed, cx, cz, 0x313E);
        if (!Hash.chance(h, 0.3)) {
            return null;
        }
        int x = cx * CELL + Hash.range(Hash.next(h, 1), 6, CELL - 6);
        int z = cz * CELL + Hash.range(Hash.next(h, 2), 6, CELL - 6);
        int roll = Hash.range(Hash.next(h, 3), 0, 99);
        Prop prop = roll < 20 ? Prop.LAMP : roll < 30 ? Prop.CHAIR : roll < 40 ? Prop.STAIRS : roll < 52 ? Prop.COLUMN : roll < 60 ? Prop.DOOR
                : roll < 66 ? Prop.MIRROR : roll < 76 ? Prop.BEDSIDE : roll < 84 ? Prop.TELEVISION : roll < 90 ? Prop.TELEPHONE : Prop.SUITCASE;
        return new Placed(prop, x, z, Direction.from2DDataValue(Hash.range(Hash.next(h, 4), 0, 3)), h);
    }

    @Override
    public void generate(SceneContext ctx) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                ctx.set(lx, 0, lz, SURFACE);
            }
        }
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        ctx.flipZone(new com.eltavine.oneirgeo.space.Box(minX, ctx.minY(), minZ, minX + 16, 0, minZ + 16));
        for (int cz = Math.floorDiv(minZ - 8, CELL); cz <= Math.floorDiv(minZ + 23, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 8, CELL); cx <= Math.floorDiv(minX + 23, CELL); cx++) {
                Placed p = placed(ctx.seed(), cx, cz);
                if (p != null && ctx.intersectsChunk(p.x - 6, p.z - 6, p.x + 6, p.z + 6)) {
                    this.build(ctx, p);
                }
            }
        }
    }

    private void build(SceneContext ctx, Placed p) {
        Frame f = new Frame(p.x, 0, p.z, p.facing);
        switch (p.prop) {
            case LAMP -> {
                put(ctx, f, 0, 0, 0, Blocks.SMOOTH_STONE.defaultBlockState());
                int height = 3 + (int) Math.floorMod(p.hash, 3L);
                for (int y = 1; y <= height; y++) {
                    put(ctx, f, 0, y, 0, Blocks.DEEPSLATE_TILE_WALL.defaultBlockState());
                }
                if (!Hash.chance(Hash.next(p.hash, 10), 0.45)) {
                    put(ctx, f, 0, height + 1, 0, Blocks.LANTERN.defaultBlockState());
                }
            }
            case CHAIR -> {
                BlockState velvet = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
                BlockState wood = Blocks.DARK_OAK_PLANKS.defaultBlockState();
                for (int a = 0; a <= 1; a++) {
                    put(ctx, f, a, 0, 0, wood);
                    put(ctx, f, a, 0, 1, wood);
                    put(ctx, f, a, 1, 0, velvet);
                    put(ctx, f, a, 1, 1, velvet);
                    for (int y = 0; y <= 4; y++) {
                        put(ctx, f, a, y, -1, y == 0 ? wood : velvet);
                    }
                }
                for (int b = -1; b <= 1; b++) {
                    for (int y = 0; y <= 2; y++) {
                        put(ctx, f, -1, y, b, y == 0 ? wood : velvet);
                        put(ctx, f, 2, y, b, y == 0 ? wood : velvet);
                    }
                }
            }
            case STAIRS -> {
                int steps = 5 + (int) Math.floorMod(p.hash >> 8, 6L);
                BlockState step = Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, f.world(Direction.NORTH));
                for (int i = 0; i < steps; i++) {
                    for (int a = 0; a <= 1; a++) {
                        for (int y = 0; y < i; y++) {
                            put(ctx, f, a, y, -i, Blocks.QUARTZ_BLOCK.defaultBlockState());
                        }
                        put(ctx, f, a, i, -i, step);
                    }
                }
            }
            case COLUMN -> {
                int height = 3 + (int) Math.floorMod(p.hash >> 4, 7L);
                boolean broken = Hash.chance(Hash.next(p.hash, 9), 0.85);
                for (int y = 0; y <= height; y++) {
                    BlockState state = y == height && !broken ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState()
                            : Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                    put(ctx, f, 0, y, 0, state);
                }
                if (broken) {
                    put(ctx, f, 2, 0, 1, Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, f.world(Direction.EAST).getAxis()));
                    put(ctx, f, 3, 0, 1, Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, f.world(Direction.EAST).getAxis()));
                }
            }
            case DOOR -> {
                BlockState frame = Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState();
                for (int y = 0; y <= 2; y++) {
                    put(ctx, f, -1, y, 0, frame);
                    put(ctx, f, 1, y, 0, frame);
                }
                for (int a = -1; a <= 1; a++) {
                    put(ctx, f, a, 2, 0, frame);
                }
                BlockState door = OneirgeoBlocks.EXIT_DOOR.defaultBlockState().setValue(DoorBlock.FACING, f.world(Direction.SOUTH));
                put(ctx, f, 0, 0, 0, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
                put(ctx, f, 0, 1, 0, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            }
            case BEDSIDE -> {
                put(ctx, f, 0, 0, 0, furniture(OneirgeoBlocks.HOSPITAL_BED, f.world(Direction.EAST)));
                put(ctx, f, 0, 0, -1, furniture(OneirgeoBlocks.HEART_MONITOR, f.world(Direction.EAST)));
                put(ctx, f, 1, 0, -1, furniture(OneirgeoBlocks.IV_STAND, f.world(Direction.EAST)));
                put(ctx, f, 0, 0, 1, furniture(OneirgeoBlocks.WAITING_CHAIR, f.world(Direction.NORTH)));
            }
            case TELEVISION -> {
                put(ctx, f, 0, 0, 0, furniture(OneirgeoBlocks.TELEVISION, f.world(Direction.SOUTH)));
                put(ctx, f, 0, 0, 3, furniture(OneirgeoBlocks.WAITING_CHAIR, f.world(Direction.NORTH)));
            }
            case TELEPHONE -> {
                put(ctx, f, 0, 0, 0, Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                put(ctx, f, 0, 1, 0, furniture(OneirgeoBlocks.TELEPHONE, f.world(Direction.SOUTH)));
                for (int y = 0; y <= 2; y++) {
                    put(ctx, f, 2, y, 1, Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
                put(ctx, f, 2, 2, 0, furniture(OneirgeoBlocks.STOPPED_CLOCK, f.world(Direction.NORTH)));
            }
            case SUITCASE -> {
            }
            case MIRROR -> {
                Direction.Axis axis = f.world(Direction.EAST).getAxis();
                BlockState frame = OneirgeoBlocks.MIRROR_FRAME.defaultBlockState();
                BlockState portal = OneirgeoBlocks.MIRROR_PORTAL.defaultBlockState().setValue(MirrorPortalBlock.AXIS, axis);
                for (int a = -1; a <= 2; a++) {
                    for (int y = 0; y <= 4; y++) {
                        boolean border = a == -1 || a == 2 || y == 0 || y == 4;
                        put(ctx, f, a, y, 0, border ? frame : portal);
                    }
                }
            }
        }
    }

    private static BlockState furniture(net.minecraft.world.level.block.Block block, Direction facing) {
        return block.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
    }

    /** Where the suitcases are within {@code cells} cells of the origin, for the self-test. */
    public static java.util.List<net.minecraft.core.BlockPos> suitcases(long seed, int cells) {
        java.util.List<net.minecraft.core.BlockPos> out = new java.util.ArrayList<>();
        for (int cx = -cells; cx <= cells; cx++) {
            for (int cz = -cells; cz <= cells; cz++) {
                Placed p = placed(seed, cx, cz);
                if (p != null && p.prop == Prop.SUITCASE) {
                    out.add(new net.minecraft.core.BlockPos(p.x, 0, p.z));
                }
            }
        }
        return out;
    }

    /** A suitcase on the water, with whatever someone packed in a hurry; it has no reflection. */
    @Override
    public void decorate(com.eltavine.oneirgeo.world.gen.scene.DecorationContext ctx) {
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int cz = Math.floorDiv(minZ - 8, CELL); cz <= Math.floorDiv(minZ + 23, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 8, CELL); cx <= Math.floorDiv(minX + 23, CELL); cx++) {
                Placed p = placed(ctx.seed(), cx, cz);
                if (p != null && p.prop == Prop.SUITCASE) {
                    net.minecraft.core.BlockPos at = new net.minecraft.core.BlockPos(p.x, 0, p.z);
                    if (ctx.canPlace(at)) {
                        ctx.chest(at, p.facing, net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_SUPPLY);
                    }
                }
            }
        }
    }

    /** Places a block above the surface and its reflection below it. */
    private static void put(SceneContext ctx, Frame f, int a, int y, int b, BlockState state) {
        int x = f.worldX(a, b);
        int z = f.worldZ(a, b);
        ctx.place(x, y, z, state);
        ctx.place(x, -1 - y, z, reflect(state));
    }

    /** The same block seen upside down; doors keep a valid lower and upper half. */
    static BlockState reflect(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HALF)) {
            state = state.setValue(BlockStateProperties.HALF, state.getValue(BlockStateProperties.HALF) == Half.TOP ? Half.BOTTOM : Half.TOP);
        }
        if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
            state = state.setValue(BlockStateProperties.SLAB_TYPE,
                    state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP);
        }
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            state = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                    state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
        }
        if (state.hasProperty(LanternBlock.HANGING)) {
            state = state.setValue(LanternBlock.HANGING, !state.getValue(LanternBlock.HANGING));
        }
        return state;
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return 0;
    }
}
