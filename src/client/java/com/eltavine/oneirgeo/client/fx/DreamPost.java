package com.eltavine.oneirgeo.client.fx;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.mixin.client.PostChainAccessor;
import com.eltavine.oneirgeo.mixin.client.PostPassAccessor;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

/**
 * Drives the {@code oneirgeo:dream} post chain. Its uniform block is static in JSON, so every frame
 * the passes are pointed at a ring buffer holding this frame's strengths, and given back their own
 * buffer afterwards so that resource reloads still close what they own.
 */
public final class DreamPost {
    public static final Identifier ID = Oneirgeo.id("dream");
    private static final String BLOCK = "DreamConfig";
    private static final int SIZE = 5 * 16;
    private static final long START = System.nanoTime();
    private static MappableRingBuffer ring;
    private static final Map<PostPass, GpuBuffer> ORIGINALS = new IdentityHashMap<>();

    private DreamPost() {
    }

    public static boolean wanted() {
        return OneirgeoConfig.get().effects && (EffectDirector.active() || Camcorder.active());
    }

    /** Called right before the game renderer runs its post chains. */
    public static void beforeApply() {
        if (!wanted()) {
            return;
        }
        PostChain chain = Minecraft.getInstance().getShaderManager().getPostChain(ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return;
        }
        if (ring == null) {
            ring = new MappableRingBuffer(() -> "Oneirgeo dream config", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, SIZE);
        }
        GpuBuffer buffer = ring.currentBuffer();
        try (GpuBufferSlice.MappedView view = buffer.map(false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(view.data());
            builder.putVec4(EffectDirector.strength(0), EffectDirector.strength(1), EffectDirector.strength(2), EffectDirector.strength(3));
            builder.putVec4(EffectDirector.strength(4), EffectDirector.strength(5), EffectDirector.strength(6), EffectDirector.strength(7));
            builder.putVec4(EffectDirector.tint(0), EffectDirector.tint(1), EffectDirector.tint(2), EffectDirector.tint(3));
            float seconds = (float) (((System.nanoTime() - START) / 1.0E9) % 3600.0);
            builder.putVec4(seconds, EffectDirector.warp(), OneirgeoConfig.get().safeMode ? 0.0F : 1.0F, 0.0F);
            builder.putVec4(Camcorder.strength(), 0.0F, 0.0F, 0.0F);
        }
        List<PostPass> passes = ((PostChainAccessor) chain).oneirgeo$passes();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).oneirgeo$customUniforms();
            GpuBuffer own = uniforms.get(BLOCK);
            if (own != null) {
                ORIGINALS.put(pass, own);
                uniforms.put(BLOCK, buffer);
            }
        }
    }

    /** Called right after the post chains ran. */
    public static void afterApply() {
        if (ORIGINALS.isEmpty()) {
            return;
        }
        ORIGINALS.forEach((pass, own) -> ((PostPassAccessor) pass).oneirgeo$customUniforms().put(BLOCK, own));
        ORIGINALS.clear();
        ring.rotate();
    }
}
