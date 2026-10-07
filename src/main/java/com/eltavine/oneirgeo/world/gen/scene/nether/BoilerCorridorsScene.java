package com.eltavine.oneirgeo.world.gen.scene.nether;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import com.eltavine.oneirgeo.world.gen.scene.rooms.RoomGrid;
import com.eltavine.oneirgeo.world.gen.scene.rooms.RoomStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

/**
 * Boiler rooms and pipe corridors under the ash, lit red, going down for seven hundred blocks.
 * Corridors here loop far more often than the rooms of the overworld.
 */
public final class BoilerCorridorsScene implements Scene {
    private static final RoomGrid GRID = new RoomGrid(10, 8, 0.08, 0.72, 0.03, 0.0, 0.004, 0.02, 0.012, 0);
    private static final RoomStyle STYLE = new RoomStyle(Blocks.POLISHED_BLACKSTONE.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(),
            OneirgeoBlocks.BOILER_PLATE.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), 0.45,
            OneirgeoBlocks.CLOSED_DOOR.defaultBlockState(), 0.03);
    private static final BlockState FILL = Blocks.BLACKSTONE.defaultBlockState();

    private static int topFloor(int maxY) {
        return Math.floorDiv(maxY + 1, GRID.floorHeight()) - 1;
    }

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int floorMin = Math.floorDiv(ctx.minY(), GRID.floorHeight()) + 1;
        int floorMax = topFloor(ctx.maxY());
        for (int floor = floorMin; floor <= floorMax; floor++) {
            GRID.generateFloor(ctx, seed, floor, STYLE);
        }
        int above = (floorMax + 1) * GRID.floorHeight();
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                ctx.fill(lx, lz, above, ctx.maxY(), FILL);
                ctx.fill(lx, lz, ctx.minY(), floorMin * GRID.floorHeight() - 1, FILL);
                ctx.set(lx, ctx.minY(), lz, Blocks.BEDROCK.defaultBlockState());
            }
        }
        GRID.registerSpace(ctx, seed, floorMin, floorMax);
        vents(ctx, seed, floorMin, floorMax);
        long dimensionSeed = ctx.sampler().seed();
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        int interiorBottom = floorMax * GRID.floorHeight() + 1;
        for (NetherFeatures.Sinkhole hole : NetherFeatures.sinkholesNear(dimensionSeed, ctx.originX(), ctx.originZ(), ctx.originX() + 15, ctx.originZ() + 15)) {
            for (int y = interiorBottom; y <= ctx.maxY(); y++) {
                ctx.place(hole.x(), y, hole.z(), ladder);
                ctx.place(hole.x(), y, hole.z() - 1, FILL);
            }
        }
    }

    /** Under every hole in a ceiling, a steam vent that throws you up to the floor above. */
    private static void vents(SceneContext ctx, long seed, int floorMin, int floorMax) {
        int c0x = Math.floorDiv(ctx.originX(), GRID.cell());
        int c1x = Math.floorDiv(ctx.originX() + 15, GRID.cell());
        int c0z = Math.floorDiv(ctx.originZ(), GRID.cell());
        int c1z = Math.floorDiv(ctx.originZ() + 15, GRID.cell());
        BlockState vent = OneirgeoBlocks.STEAM_VENT.defaultBlockState();
        for (int floor = floorMin; floor <= floorMax; floor++) {
            for (int cx = c0x; cx <= c1x; cx++) {
                for (int cz = c0z; cz <= c1z; cz++) {
                    RoomGrid.Cell cell = GRID.cellAt(seed, floor, cx, cz);
                    if (cell.holeAbove() && !cell.hole() && !cell.inRun()) {
                        ctx.place(cx * GRID.cell() + 6, floor * GRID.floorHeight(), cz * GRID.cell() + 5, vent);
                    }
                }
            }
        }
    }

    @Override
    public void decorate(DecorationContext ctx) {
        GRID.furnish(ctx, 0.035, 0xFE7E, (floor, cellX, cellZ, x0, y0, z0) -> feverWard(ctx, x0, y0 + 1, z0));
        GRID.decorate(ctx, (floor, cellX, cellZ, pos, facing) -> ctx.chest(pos, facing, BuiltInLootTables.NETHER_BRIDGE));
    }

    /** Three beds along the wall, a drip and a monitor at each, and a clock that stopped. */
    private static void feverWard(DecorationContext ctx, int x0, int y, int z0) {
        for (int i = 0; i < 3; i++) {
            int z = z0 + 2 + i * 3;
            ctx.furniture(new BlockPos(x0 + 1, y, z), OneirgeoBlocks.HEART_MONITOR, Direction.EAST);
            ctx.furniture(new BlockPos(x0 + 2, y, z), OneirgeoBlocks.HOSPITAL_BED, Direction.EAST);
            ctx.furniture(new BlockPos(x0 + 2, y, z + 1), OneirgeoBlocks.IV_STAND, Direction.EAST);
        }
        BlockPos clock = new BlockPos(x0 + 6, y + 2, z0 + 1);
        if (ctx.canPlace(clock) && !ctx.getBlock(clock.north()).isAir()) {
            ctx.furniture(clock, OneirgeoBlocks.STOPPED_CLOCK, Direction.SOUTH);
        }
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return topFloor(info.maxY()) * GRID.floorHeight();
    }

    @Override
    public @Nullable String biomeVariant(SceneInfo info, int x, int y, int z) {
        return GRID.isClosedRoom(info.seed(), x, y, z) ? "closed" : null;
    }
}
