package com.eltavine.oneirgeo.mixin;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Signs written during world generation have no level to notify, so their text is set directly. */
@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
    @Accessor("frontText")
    void oneirgeo$setFrontText(SignText text);

    @Accessor("isWaxed")
    void oneirgeo$setWaxed(boolean waxed);
}
