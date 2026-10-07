package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.world.gen.scene.nether.AshPlainsScene;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * New Nether portals in layered dimensions open onto the main walkable layer (the ash plains, or the
 * overworld surface) rather than wherever vanilla's column scan lands among 4064 blocks of layers.
 */
public final class PortalPlacement {
    private static final int OVERWORLD_SURFACE = 65;

    private PortalPlacement() {
    }

    /** Returns null to let vanilla place the portal. */
    public static @Nullable Optional<BlockUtil.FoundRectangle> create(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        if (!(level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator)) {
            return null;
        }
        int hint = level.dimension() == Level.NETHER ? AshPlainsScene.GROUND + 1 : OVERWORLD_SURFACE;
        int surface = SafeSpot.surfaceHint(level, origin.getX(), hint, origin.getZ());
        BlockPos feet = SafeSpot.find(level, origin.getX(), surface, origin.getZ(), 6, 10);
        if (feet == null) {
            feet = new BlockPos(origin.getX(), surface, origin.getZ());
        }

        Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction across = along.getClockWise();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int w = -1; w <= 2; w++) {
            for (int s = -1; s <= 1; s++) {
                for (int h = -1; h <= 3; h++) {
                    pos.setWithOffset(feet, w * along.getStepX() + s * across.getStepX(), h, w * along.getStepZ() + s * across.getStepZ());
                    level.setBlockAndUpdate(pos, h == -1 ? obsidian : Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int w = -1; w < 3; w++) {
            for (int h = -1; h < 4; h++) {
                if (w == -1 || w == 2 || h == -1 || h == 3) {
                    pos.setWithOffset(feet, w * along.getStepX(), h, w * along.getStepZ());
                    level.setBlockAndUpdate(pos, obsidian);
                }
            }
        }
        BlockState portal = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int w = 0; w < 2; w++) {
            for (int h = 0; h < 3; h++) {
                pos.setWithOffset(feet, w * along.getStepX(), h, w * along.getStepZ());
                level.setBlock(pos, portal, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(feet.immutable(), 2, 3));
    }
}
