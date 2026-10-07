package com.eltavine.oneirgeo.world.gen.scene;

import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.entity.MimicEntity;
import com.eltavine.oneirgeo.entity.OneirgeoEntities;
import com.eltavine.oneirgeo.mixin.SignBlockEntityAccessor;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootTable;

/** Features-step view: may place block entities, but only inside the chunk being decorated. */
public final class DecorationContext {
    private final WorldGenLevel level;
    private final ChunkAccess chunk;
    private final SceneInfo info;
    private final LayoutSampler sampler;
    private final boolean[] owned;
    private final RandomSource random;

    public DecorationContext(WorldGenLevel level, ChunkAccess chunk, SceneInfo info, LayoutSampler sampler, boolean[] owned) {
        this.level = level;
        this.chunk = chunk;
        this.info = info;
        this.sampler = sampler;
        this.owned = owned;
        this.random = RandomSource.create(Hash.of(info.seed(), chunk.getPos().x(), chunk.getPos().z(), 0xDEC0));
    }

    public WorldGenLevel level() {
        return this.level;
    }

    public SceneInfo info() {
        return this.info;
    }

    public long seed() {
        return this.info.seed();
    }

    public LayoutSampler sampler() {
        return this.sampler;
    }

    public RandomSource random() {
        return this.random;
    }

    public int originX() {
        return this.chunk.getPos().getMinBlockX();
    }

    public int originZ() {
        return this.chunk.getPos().getMinBlockZ();
    }

    public boolean ownsWorld(int x, int z) {
        int lx = x - this.originX();
        int lz = z - this.originZ();
        return lx >= 0 && lx < 16 && lz >= 0 && lz < 16 && this.owned[lx | lz << 4];
    }

    public boolean canPlace(BlockPos pos) {
        int lx = pos.getX() - this.originX();
        int lz = pos.getZ() - this.originZ();
        return lx >= 0 && lx < 16 && lz >= 0 && lz < 16 && pos.getY() >= this.info.minY() && pos.getY() <= this.info.maxY();
    }

    /** See {@link SceneContext#anchorOwned}. */
    public boolean anchorOwned(int x, int z) {
        return this.sampler.region(this.info.layerIndex(), x, z).entryIndex() == this.info.region().entryIndex();
    }

    public BlockState getBlock(BlockPos pos) {
        return this.level.getBlockState(pos);
    }

    public boolean set(BlockPos pos, BlockState state) {
        if (!this.canPlace(pos)) {
            return false;
        }
        return this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    public void chest(BlockPos pos, Direction facing, ResourceKey<LootTable> lootTable) {
        if (this.set(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing))) {
            RandomizableContainer.setBlockEntityLootTable(this.level, this.random, pos, lootTable);
        }
    }

    /** Puts down a piece of furniture facing {@code facing}, if the spot is free. */
    public void furniture(BlockPos pos, Block block, Direction facing) {
        if (this.canPlace(pos) && this.getBlock(pos).isAir()) {
            this.set(pos, block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing));
        }
    }

    /** A chest holding exactly these things, in this order. */
    public void chestWith(BlockPos pos, Direction facing, List<ItemStack> items) {
        if (this.set(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing))
                && this.level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
            for (int i = 0; i < items.size() && i < container.getContainerSize(); i++) {
                container.setItem(i * 2 + 2 < container.getContainerSize() ? i * 2 + 2 : i, items.get(i));
            }
        }
    }

    /** A mimic waiting where a barrel would be. */
    public void mimic(BlockPos pos) {
        if (!this.canPlace(pos)) {
            return;
        }
        MimicEntity mimic = OneirgeoEntities.MIMIC.create(this.level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (mimic != null) {
            mimic.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            mimic.setPersistenceRequired();
            this.level.addFreshEntityWithPassengers(mimic);
        }
    }

    public void barrel(BlockPos pos, ResourceKey<LootTable> lootTable) {
        if (this.set(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP))) {
            RandomizableContainer.setBlockEntityLootTable(this.level, this.random, pos, lootTable);
        }
    }

    /** Places a bed whose foot is at {@code foot} and whose head points towards {@code facing}. */
    public void bed(BlockPos foot, Direction facing, DyeColor color) {
        BlockPos head = foot.relative(facing);
        if (!this.canPlace(foot) || !this.canPlace(head)) {
            return;
        }
        Block bed = Blocks.BED.pick(color);
        BlockState footState = bed.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
        this.level.setBlock(foot, footState, Block.UPDATE_CLIENTS);
        this.level.setBlock(head, footState.setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_CLIENTS);
    }

    public void sign(BlockPos pos, BlockState signState, List<Component> lines, DyeColor color, boolean glowing) {
        if (!this.set(pos, signState)) {
            return;
        }
        if (this.level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            List<Component> messages = new ArrayList<>(lines);
            while (messages.size() < 4) {
                messages.add(Component.empty());
            }
            List<Component> four = messages.subList(0, 4);
            SignBlockEntityAccessor access = (SignBlockEntityAccessor) sign;
            access.oneirgeo$setFrontText(new SignText(four, four, color, glowing));
            access.oneirgeo$setWaxed(true);
            sign.setChanged();
        }
    }
}
