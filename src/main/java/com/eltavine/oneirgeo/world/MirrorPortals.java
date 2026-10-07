package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.block.MirrorPortalBlock;
import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Mirror frames, lit with a shard, open onto the mirror sea. Going in remembers where you stood;
 * coming back returns you there, except from the End, which sends you home instead.
 */
public final class MirrorPortals {
    private static final int MAX_SIDE = 21;
    /** Feet height on the mirror surface, which is a sixteenth of a block thick. */
    public static final double SURFACE_FEET = 0.0625;

    private MirrorPortals() {
    }

    private record Rect(BlockPos corner, int width, int height) {
    }

    /** Fills the frame around {@code inside} with portal; false when it is not a closed frame. */
    public static boolean tryLight(Level level, BlockPos inside) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            Rect rect = find(level, inside, axis);
            if (rect != null) {
                fill(level, rect, axis);
                return true;
            }
        }
        return false;
    }

    private static boolean frame(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(OneirgeoBlocks.MIRROR_FRAME);
    }

    private static boolean empty(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    private static @Nullable Rect find(Level level, BlockPos start, Direction.Axis axis) {
        if (!empty(level, start)) {
            return null;
        }
        Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockPos bottom = start;
        for (int i = 0; i < MAX_SIDE && empty(level, bottom.below()); i++) {
            bottom = bottom.below();
        }
        if (!frame(level, bottom.below())) {
            return null;
        }
        BlockPos corner = bottom;
        for (int i = 0; i < MAX_SIDE && empty(level, corner.relative(along.getOpposite())); i++) {
            corner = corner.relative(along.getOpposite());
        }
        if (!frame(level, corner.relative(along.getOpposite()))) {
            return null;
        }
        int width = 1;
        while (width < MAX_SIDE && empty(level, corner.relative(along, width))) {
            width++;
        }
        int height = 1;
        while (height < MAX_SIDE && empty(level, corner.above(height))) {
            height++;
        }
        if (height < 2) {
            return null;
        }
        for (int a = -1; a <= width; a++) {
            for (int h = -1; h <= height; h++) {
                BlockPos pos = corner.relative(along, a).above(h);
                boolean border = a == -1 || a == width || h == -1 || h == height;
                boolean corners = (a == -1 || a == width) && (h == -1 || h == height);
                if (corners) {
                    continue;
                }
                if (border ? !frame(level, pos) : !empty(level, pos)) {
                    return null;
                }
            }
        }
        return new Rect(corner, width, height);
    }

    private static void fill(Level level, Rect rect, Direction.Axis axis) {
        BlockState portal = OneirgeoBlocks.MIRROR_PORTAL.defaultBlockState().setValue(MirrorPortalBlock.AXIS, axis);
        Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        for (int a = 0; a < rect.width(); a++) {
            for (int h = 0; h < rect.height(); h++) {
                level.setBlock(rect.corner().relative(along, a).above(h), portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos portal) {
        MinecraftServer server = level.getServer();
        TeleportTransition.PostTeleportTransition after = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
        if (level.dimension() == OneirgeoDimensions.MIRROR_SEA) {
            GlobalPos back = entity instanceof ServerPlayer player ? player.getAttached(OneirgeoAttachments.RETURN_POINT) : null;
            if (back == null || back.dimension() == Level.END || OneirgeoDimensions.isDream(back.dimension()) || server.getLevel(back.dimension()) == null) {
                back = entity instanceof ServerPlayer player ? Passages.home(player)
                        : GlobalPos.of(Level.OVERWORLD, server.overworld().getRespawnData().pos());
            }
            ServerLevel target = server.getLevel(back.dimension());
            BlockPos at = back.pos();
            BlockPos feet = SafeSpot.find(target, at.getX(), at.getY(), at.getZ(), 6, 16);
            if (feet == null) {
                feet = SafeSpot.findOrBuild(target, at.getX(), SafeSpot.surfaceHint(target, at.getX(), at.getY(), at.getZ()), at.getZ(),
                        Blocks.SMOOTH_STONE.defaultBlockState());
            }
            return new TeleportTransition(target, Vec3.atBottomCenterOf(feet), Vec3.ZERO, entity.getYRot(), entity.getXRot(), after);
        }
        ServerLevel sea = server.getLevel(OneirgeoDimensions.MIRROR_SEA);
        if (sea == null) {
            return null;
        }
        if (entity instanceof ServerPlayer player) {
            player.setAttached(OneirgeoAttachments.RETURN_POINT, GlobalPos.of(level.dimension(), exitSpot(level, portal, entity)));
        }
        return new TeleportTransition(sea, arrival(sea, portal.getX(), portal.getZ()), Vec3.ZERO, entity.getYRot(), entity.getXRot(), after);
    }

    /** Two blocks out of the portal, on the side the entity entered from. */
    static BlockPos exitSpot(ServerLevel level, BlockPos portal, Entity entity) {
        BlockState state = level.getBlockState(portal);
        Direction.Axis axis = state.hasProperty(MirrorPortalBlock.AXIS) ? state.getValue(MirrorPortalBlock.AXIS) : Direction.Axis.X;
        BlockPos bottom = portal;
        while (level.getBlockState(bottom.below()).is(OneirgeoBlocks.MIRROR_PORTAL)) {
            bottom = bottom.below();
        }
        Direction normal = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        double side = axis == Direction.Axis.X ? entity.getZ() - (portal.getZ() + 0.5) : entity.getX() - (portal.getX() + 0.5);
        return bottom.relative(side >= 0.0 ? normal : normal.getOpposite(), 2);
    }

    /** Arrival point on the mirror sea near (x, z), with a standing mirror nearby to go back through. */
    static Vec3 arrival(ServerLevel sea, int x, int z) {
        if (findPortal(sea, x, z, 12) == null) {
            build(sea, x + 3, z - 1, Direction.Axis.Z);
        }
        return new Vec3(x + 0.5, SURFACE_FEET, z + 0.5);
    }

    private static @Nullable BlockPos findPortal(ServerLevel level, int x, int z, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = 1; y <= 3; y++) {
                    if (level.getBlockState(pos.set(x + dx, y, z + dz)).is(OneirgeoBlocks.MIRROR_PORTAL)) {
                        return pos.immutable();
                    }
                }
            }
        }
        return null;
    }

    /**
     * A lit mirror standing on the sea, two wide and three tall, with its reflection hanging under the
     * surface. {@code (x, z)} is the lower corner of the opening.
     */
    public static void build(ServerLevel level, int x, int z, Direction.Axis axis) {
        Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockState frame = OneirgeoBlocks.MIRROR_FRAME.defaultBlockState();
        BlockState portal = OneirgeoBlocks.MIRROR_PORTAL.defaultBlockState().setValue(MirrorPortalBlock.AXIS, axis);
        BlockPos base = new BlockPos(x, 0, z);
        for (int a = -1; a <= 2; a++) {
            for (int h = 0; h <= 4; h++) {
                boolean border = a == -1 || a == 2 || h == 0 || h == 4;
                BlockPos pos = base.relative(along, a).above(h);
                BlockPos mirrored = new BlockPos(pos.getX(), -1 - pos.getY(), pos.getZ());
                BlockState state = border ? frame : portal;
                level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                level.setBlock(mirrored, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }
}
