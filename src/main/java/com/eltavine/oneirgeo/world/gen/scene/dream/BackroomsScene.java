package com.eltavine.oneirgeo.world.gen.scene.dream;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import com.eltavine.oneirgeo.world.gen.scene.rooms.RoomGrid;
import com.eltavine.oneirgeo.world.gen.scene.rooms.RoomStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

/**
 * Yellow wallpaper, wet carpet and the hum of lights, floor upon floor for the whole height of the
 * world. Corridors loop, some rooms let you in and never out, and a few walls hide a door home.
 */
public final class BackroomsScene implements Scene {
    private static final RoomGrid GRID = new RoomGrid(10, 8, 0.42, 0.38, 0.008, 0.006, 0.012, 0.012, 0.014, Integer.MAX_VALUE);
    private static final RoomStyle STYLE = new RoomStyle(OneirgeoBlocks.DAMP_CARPET.defaultBlockState(), OneirgeoBlocks.WALLPAPER.defaultBlockState(),
            Blocks.SMOOTH_SANDSTONE.defaultBlockState(), OneirgeoBlocks.CEILING_TILE.defaultBlockState(),
            OneirgeoBlocks.FLUORESCENT_LIGHT.defaultBlockState(), 0.45, OneirgeoBlocks.EXIT_DOOR.defaultBlockState(), 0.035);

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int floorMin = Math.floorDiv(ctx.minY(), GRID.floorHeight());
        int floorMax = Math.floorDiv(ctx.maxY() + 1, GRID.floorHeight()) - 1;
        for (int floor = floorMin; floor <= floorMax; floor++) {
            GRID.generateFloor(ctx, seed, floor, STYLE);
        }
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                ctx.set(lx, ctx.minY(), lz, bedrock);
                ctx.set(lx, ctx.maxY(), lz, bedrock);
            }
        }
        GRID.registerSpace(ctx, seed, floorMin, floorMax);
    }

    @Override
    public void decorate(DecorationContext ctx) {
        GRID.furnish(ctx, 0.03, 0x3A17, (floor, cellX, cellZ, x0, y0, z0) -> waitingRoom(ctx, x0, y0 + 1, z0));
        GRID.decorate(ctx, (floor, cellX, cellZ, pos, facing) -> {
            if (Hash.chance(Hash.of(ctx.seed(), pos.getX(), pos.getY(), pos.getZ(), 0x313C), 0.2)) {
                ctx.mimic(pos);
            } else {
                ctx.barrel(pos, BuiltInLootTables.SHIPWRECK_SUPPLY);
            }
        });
    }

    /** A row of plastic chairs facing a reception desk with a telephone, a television in the corner, a clock that stopped. */
    private static void waitingRoom(DecorationContext ctx, int x0, int y, int z0) {
        for (int u = 2; u <= 7; u++) {
            ctx.furniture(new BlockPos(x0 + u, y, z0 + 7), OneirgeoBlocks.WAITING_CHAIR, Direction.NORTH);
        }
        for (int u = 2; u <= 4; u++) {
            BlockPos desk = new BlockPos(x0 + u, y, z0 + 2);
            if (ctx.canPlace(desk) && ctx.getBlock(desk).isAir()) {
                ctx.set(desk, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
            }
        }
        ctx.furniture(new BlockPos(x0 + 3, y + 1, z0 + 2), OneirgeoBlocks.TELEPHONE, Direction.SOUTH);
        ctx.furniture(new BlockPos(x0 + 8, y, z0 + 2), OneirgeoBlocks.TELEVISION, Direction.SOUTH);
        BlockPos clock = new BlockPos(x0 + 5, y + 2, z0 + 1);
        if (ctx.canPlace(clock) && !ctx.getBlock(clock.north()).isAir()) {
            ctx.furniture(clock, OneirgeoBlocks.STOPPED_CLOCK, Direction.SOUTH);
        }
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return 0;
    }

    @Override
    public @Nullable String biomeVariant(SceneInfo info, int x, int y, int z) {
        return GRID.isClosedRoom(info.seed(), x, y, z) ? "closed" : null;
    }
}
