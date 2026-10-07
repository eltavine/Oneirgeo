package com.eltavine.oneirgeo.world.gen.scene.end;

import com.eltavine.oneirgeo.block.MirrorPortalBlock;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Frame;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Islands of end stone drifting under the wrong sky, covered in rows of nameless graves. Colossi
 * mourn over some of them; others carry collapsed observatories. The island under the arrival
 * platform has a door that leads home.
 */
public final class StarCemeteryScene implements Scene {
    private static final BlockState END_STONE = Blocks.END_STONE.defaultBlockState();
    private static final BlockState BRICKS = Blocks.END_STONE_BRICKS.defaultBlockState();
    private static final BlockState GRAVE = OneirgeoBlocks.GRAVE_STONE.defaultBlockState();
    /** West of the platform, where arriving players are looking. */
    private static final int SHRINE_DX = -12;
    private static final int CENTRAL_CLEARANCE = 18;

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        EndIslands.forEachNear(seed, minX, minZ, minX + 15, minZ + 15, island -> terrain(ctx, island));
        EndIslands.forEachNear(seed, minX, minZ, minX + 15, minZ + 15, island -> {
            switch (island.kind()) {
                case OBSERVATORY -> {
                    Observatory.build(ctx, island);
                    Observatory.registerSpace(ctx, island);
                }
                case COLOSSUS -> {
                    graves(ctx, island, Colossus.footprint(island));
                    Colossus.build(ctx, island);
                }
                case GRAVES -> {
                    boolean mirror = hasMirror(island);
                    graves(ctx, island, mirror ? 5 : 0);
                    if (mirror) {
                        mirror(ctx, island);
                    }
                }
                case CENTRAL -> {
                    graves(ctx, island, CENTRAL_CLEARANCE);
                    shrine(ctx, island);
                }
                default -> {
                }
            }
        });
    }

    @Override
    public void decorate(DecorationContext ctx) {
        long seed = ctx.seed();
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        EndIslands.forEachNear(seed, minX, minZ, minX + 15, minZ + 15, island -> {
            if (island.kind() == EndIslands.Kind.OBSERVATORY) {
                Observatory.decorate(ctx, island);
            } else if (island.kind() == EndIslands.Kind.CENTRAL) {
                BlockPos sign = new BlockPos(island.x() + SHRINE_DX + 2, island.top() + 1, island.z() + 2);
                ctx.sign(sign, Blocks.PALE_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 12),
                        List.of(Component.empty(), Component.translatable("oneirgeo.sign.way_back")), DyeColor.LIGHT_GRAY, true);
            }
        });
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return EndIslands.surfaceY(info.seed(), x, z);
    }

    private static void terrain(SceneContext ctx, EndIslands.Island island) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int x = ctx.originX() + lx;
                int z = ctx.originZ() + lz;
                int top = island.topAt(x, z);
                if (top == Integer.MIN_VALUE) {
                    continue;
                }
                ctx.fill(lx, lz, island.bottomAt(x, z), top, END_STONE);
                if (Hash.chance(Hash.of(island.hash(), x >> 2, z >> 2, 0xB21), 0.08)) {
                    ctx.set(lx, top, lz, BRICKS);
                }
            }
        }
    }

    /** Rows of steles facing the island's direction, keeping a radius around the centre clear. */
    private static void graves(SceneContext ctx, EndIslands.Island island, int clearance) {
        int r = (int) (island.radius() * 1.2);
        if (!ctx.intersectsChunk(island.x() - r, island.z() - r, island.x() + r, island.z() + r)) {
            return;
        }
        Frame f = new Frame(island.x(), island.top(), island.z(), island.facing());
        for (int b = -r - Math.floorMod(-r, 5); b <= r; b += 5) {
            for (int a = -r - Math.floorMod(-r, 4); a <= r; a += 4) {
                if (a * a + b * b < clearance * clearance) {
                    continue;
                }
                int x = f.worldX(a, b);
                int z = f.worldZ(a, b);
                if (!ctx.intersectsChunk(x - 1, z - 1, x + 1, z + 1) || island.t(x, z) > 0.8) {
                    continue;
                }
                long h = Hash.of(island.hash(), a, b, 0x6A7E);
                double roll = Hash.unit(h);
                if (roll < 0.2) {
                    continue;
                }
                int y = island.top();
                if (roll < 0.42) {
                    ctx.place(x, y + 1, z, GRAVE);
                    ctx.place(f.worldX(a, b - 1), y + 1, f.worldZ(a, b - 1), GRAVE);
                    continue;
                }
                ctx.place(x, y + 1, z, GRAVE);
                if (Hash.chance(Hash.next(h, 1), 0.45)) {
                    ctx.place(x, y + 2, z, GRAVE);
                }
                if (Hash.chance(Hash.next(h, 2), 0.2)) {
                    int count = Hash.range(Hash.next(h, 3), 1, 3);
                    ctx.place(f.worldX(a, b + 1), y + 1, f.worldZ(a, b + 1),
                            Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true));
                }
            }
        }
    }

    private static boolean hasMirror(EndIslands.Island island) {
        return Hash.chance(Hash.next(island.hash(), 30), 0.2);
    }

    /** A lit mirror standing among the graves: a way out of the End, through the mirror sea. */
    private static void mirror(SceneContext ctx, EndIslands.Island island) {
        Frame f = new Frame(island.x(), island.top(), island.z(), island.facing());
        BlockState frame = OneirgeoBlocks.MIRROR_FRAME.defaultBlockState();
        BlockState portal = OneirgeoBlocks.MIRROR_PORTAL.defaultBlockState().setValue(MirrorPortalBlock.AXIS, f.world(Direction.EAST).getAxis());
        for (int a = -1; a <= 2; a++) {
            for (int h = 1; h <= 5; h++) {
                boolean border = a == -1 || a == 2 || h == 1 || h == 5;
                ctx.place(f.worldX(a, 0), island.top() + h, f.worldZ(a, 0), border ? frame : portal);
            }
        }
    }

    /** A lone door in a frame, a short walk from the arrival platform. */
    private static void shrine(SceneContext ctx, EndIslands.Island island) {
        int x = island.x() + SHRINE_DX;
        int y = island.top();
        int z = island.z();
        if (!ctx.intersectsChunk(x - 2, z - 2, x + 2, z + 2)) {
            return;
        }
        ctx.placeBox(x - 1, y, z - 2, x + 1, y, z + 2, BRICKS);
        ctx.placeBox(x, y + 1, z - 1, x, y + 3, z - 1, BRICKS);
        ctx.placeBox(x, y + 1, z + 1, x, y + 3, z + 1, BRICKS);
        ctx.place(x, y + 3, z, BRICKS);
        BlockState door = OneirgeoBlocks.EXIT_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST);
        ctx.place(x, y + 1, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        ctx.place(x, y + 2, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }
}
