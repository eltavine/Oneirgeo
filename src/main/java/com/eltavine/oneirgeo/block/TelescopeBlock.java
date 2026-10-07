package com.eltavine.oneirgeo.block;

import com.eltavine.oneirgeo.story.TimeCapsule;
import com.eltavine.oneirgeo.survival.Lucidity;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The observatory telescope. In the End one star is always brighter than the rest, and it hangs over
 * the observatory where the tin box is buried.
 */
public class TelescopeBlock extends FurnitureBlock {
    private static final String[] DIRECTIONS = {"north", "north_east", "east", "south_east", "south", "south_west", "west", "north_west"};

    public TelescopeBlock(Properties properties) {
        super(properties, box(4, 0, 4, 12, 15, 12), null);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(reading(serverPlayer.level(), pos).copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            Lucidity.add(serverPlayer, 0.02F);
        }
        return InteractionResult.SUCCESS;
    }

    /** What the telescope at {@code pos} shows: in the End, the star over the buried tin box. */
    public static Component reading(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        {
            Optional<BlockPos> box = level.dimension() == Level.END ? TimeCapsule.box(level.getServer()) : Optional.empty();
            Component text;
            if (box.isPresent()) {
                int dx = box.get().getX() - pos.getX();
                int dz = box.get().getZ() - pos.getZ();
                int distance = (int) Math.round(Math.sqrt((double) dx * dx + (double) dz * dz));
                if (distance < 24) {
                    text = Component.translatable("oneirgeo.telescope.here");
                } else {
                    double angle = Math.toDegrees(Math.atan2(dx, -dz));
                    int sector = Math.floorMod((int) Math.round(angle / 45.0), 8);
                    text = Component.translatable("oneirgeo.telescope.star", Component.translatable("oneirgeo.direction." + DIRECTIONS[sector]),
                            (distance + 25) / 50 * 50);
                }
            } else {
                text = Component.translatable("oneirgeo.telescope.nothing");
            }
            return text;
        }
    }
}
