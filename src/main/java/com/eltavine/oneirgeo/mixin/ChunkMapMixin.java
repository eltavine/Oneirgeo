package com.eltavine.oneirgeo.mixin;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A chunk here is 254 sections, so chunks waiting to unload are expensive to keep. Vanilla lets up
 * to 2000 of them per dimension wait for a tick with time to spare, which a busy server never has:
 * they then stay in memory indefinitely. Here at most 256 may wait, and a few dozen are unloaded on
 * every tick regardless.
 * <p>
 * Saving also copies the whole chunk before it is written; when hundreds unload at once the copies
 * would outgrow the heap long before the disk catches up, so the server waits while too many
 * writes are in flight.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    private static final int ONEIRGEO$QUEUED_UNLOADS = 256;
    private static final int ONEIRGEO$UNLOADS_PER_TICK = 64;
    private static final int ONEIRGEO$MAX_WRITES = 64;
    private static final long ONEIRGEO$MAX_WAIT = 5_000_000_000L;

    @Shadow
    @Final
    private AtomicInteger activeChunkWrites;

    @Shadow
    @Final
    private Queue<Runnable> unloadQueue;

    @ModifyConstant(method = "processUnloads", constant = @Constant(intValue = 2000))
    private int oneirgeo$fewerQueuedUnloads(int original) {
        return ONEIRGEO$QUEUED_UNLOADS;
    }

    @ModifyVariable(method = "processUnloads", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private int oneirgeo$steadyUnloads(int minimal) {
        return Math.max(minimal, Math.min(this.unloadQueue.size(), ONEIRGEO$UNLOADS_PER_TICK));
    }

    @Inject(method = "save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/storage/SerializableChunkData;copyOf(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/world/level/chunk/storage/SerializableChunkData;"))
    private void oneirgeo$waitForWrites(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        long deadline = System.nanoTime() + ONEIRGEO$MAX_WAIT;
        while (this.activeChunkWrites.get() >= ONEIRGEO$MAX_WRITES && System.nanoTime() < deadline) {
            LockSupport.parkNanos(500_000L);
        }
    }
}
