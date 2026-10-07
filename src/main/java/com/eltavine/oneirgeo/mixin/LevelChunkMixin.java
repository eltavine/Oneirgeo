package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.space.SelfHeal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Every block change of a full chunk passes through here; protected structures remember their originals. */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void oneirgeo$rememberOriginal(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        BlockState before = cir.getReturnValue();
        if (before != null && before != state) {
            SelfHeal.blockChanged((LevelChunk) (Object) this, pos, before, state);
        }
    }
}
