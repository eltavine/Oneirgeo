package com.eltavine.oneirgeo.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Something small from the time capsule; looking at it closely tells you one thing. */
public class KeepsakeItem extends Item {
    private final String line;

    public KeepsakeItem(Properties properties, String line) {
        super(properties);
        this.line = line;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(Component.translatable(this.line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
        }
        return InteractionResult.SUCCESS;
    }
}
