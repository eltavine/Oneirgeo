package com.eltavine.oneirgeo.item;

import com.eltavine.oneirgeo.survival.Lucidity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Warm, too sweet, and for a while everything holds still. */
public class LucidTeaItem extends Item {
    public LucidTeaItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            Lucidity.add(player, 0.25F);
        }
        return super.finishUsingItem(itemStack, level, entity);
    }
}
