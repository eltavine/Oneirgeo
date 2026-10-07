package com.eltavine.oneirgeo.network;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.space.SeamVolume;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: "I just crossed this seam", sent before the movement packet of the same tick. */
public record SeamCrossPayload(SeamVolume seam) implements CustomPacketPayload {
    public static final Type<SeamCrossPayload> TYPE = new Type<>(Oneirgeo.id("seam_cross"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SeamCrossPayload> CODEC = SeamVolume.STREAM_CODEC
            .map(SeamCrossPayload::new, SeamCrossPayload::seam).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
