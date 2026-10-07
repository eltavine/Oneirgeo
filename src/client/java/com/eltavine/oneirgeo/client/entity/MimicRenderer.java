package com.eltavine.oneirgeo.client.entity;

import com.eltavine.oneirgeo.entity.MimicEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;

/** Drawn as the barrel it pretends to be, its lid open once it has woken. */
public class MimicRenderer extends EntityRenderer<MimicEntity, MimicRenderer.State> {
    public static class State extends EntityRenderState {
        final MovingBlockRenderState block = new MovingBlockRenderState();
    }

    public MimicRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.45F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MimicEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        state.block.randomSeedPos = entity.blockPosition();
        state.block.blockPos = pos;
        state.block.blockState = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP).setValue(BarrelBlock.OPEN, entity.isAwake());
        if (entity.level() instanceof ClientLevel level) {
            state.block.biome = level.getBiome(pos);
            state.block.cardinalLighting = level.cardinalLighting();
            state.block.lightEngine = level.getLightEngine();
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        collector.submitMovingBlock(poseStack, state.block, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
