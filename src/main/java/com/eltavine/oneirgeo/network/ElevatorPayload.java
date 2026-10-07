package com.eltavine.oneirgeo.network;

import com.eltavine.oneirgeo.Oneirgeo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the player jumped ({@code up}) or sneaked on an elevator block. */
public record ElevatorPayload(boolean up) implements CustomPacketPayload {
    public static final Type<ElevatorPayload> TYPE = new Type<>(Oneirgeo.id("elevator"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ElevatorPayload> CODEC = ByteBufCodecs.BOOL
            .map(ElevatorPayload::new, ElevatorPayload::up).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
