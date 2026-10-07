package com.eltavine.oneirgeo.network;

import com.eltavine.oneirgeo.Oneirgeo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: play one of the father's recordings on the camcorder screen. {@code tape} names
 * the lines {@code oneirgeo.tape.<tape>.<n>} and their date {@code oneirgeo.tape.<tape>.date}.
 */
public record RecordingPayload(String tape) implements CustomPacketPayload {
    public static final Type<RecordingPayload> TYPE = new Type<>(Oneirgeo.id("recording"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecordingPayload> CODEC = ByteBufCodecs.STRING_UTF8
            .map(RecordingPayload::new, RecordingPayload::tape).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
