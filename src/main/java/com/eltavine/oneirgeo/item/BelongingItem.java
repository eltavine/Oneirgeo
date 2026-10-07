package com.eltavine.oneirgeo.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Something of the dreamer's own: the journal, the father's tapes, the keepsakes from the tin box.
 * None of them can be found twice, so they stay with whoever carries them. They cannot be dropped or
 * thrown out of an inventory, nor packed into a bundle or a shulker box to be thrown away with it.
 */
public class BelongingItem extends Item {
    public BelongingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    /**
     * True when a player letting go of the stack keeps it instead, because it is a belonging and they
     * are not in creative; the server tells them so. Called on both sides, so prediction agrees.
     */
    public static boolean holdOnTo(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof BelongingItem) || player.hasInfiniteMaterials()) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(Component.translatable("oneirgeo.belonging.kept").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        return true;
    }
}
