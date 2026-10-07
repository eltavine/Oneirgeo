package com.eltavine.oneirgeo.client.survival;

import com.eltavine.oneirgeo.network.ElevatorPayload;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;

/** Turns a jump or a sneak on an elevator block into an {@link ElevatorPayload}. */
public final class ElevatorInput {
    private static boolean wasJumping;
    private static boolean wasSneaking;

    private ElevatorInput() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null) {
                return;
            }
            Input keys = player.input.keyPresses;
            boolean onElevator = player.onGround() && player.level().getBlockState(player.blockPosition().below()).is(OneirgeoBlocks.ELEVATOR);
            if (onElevator && keys.jump() && !wasJumping) {
                ClientPlayNetworking.send(new ElevatorPayload(true));
            } else if (onElevator && keys.shift() && !wasSneaking) {
                ClientPlayNetworking.send(new ElevatorPayload(false));
            }
            wasJumping = keys.jump();
            wasSneaking = keys.shift();
        });
    }
}
