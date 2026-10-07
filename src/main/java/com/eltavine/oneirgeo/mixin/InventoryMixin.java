package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.item.BelongingItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Belongings cannot be dropped from the hand: the drop key takes the stack out here, in the client's prediction and on the server. */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    @Shadow
    public abstract ItemStack getSelectedItem();

    @Inject(method = "removeFromSelected", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$holdOnToBelongings(boolean all, CallbackInfoReturnable<ItemStack> cir) {
        if (BelongingItem.holdOnTo(this.player, this.getSelectedItem())) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
