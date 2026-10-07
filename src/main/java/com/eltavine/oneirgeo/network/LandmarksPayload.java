package com.eltavine.oneirgeo.network;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.world.gen.landmark.Landmark;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: the megastructures within a few kilometres, for far silhouettes (clients have no seed). */
public record LandmarksPayload(List<Landmark> landmarks) implements CustomPacketPayload {
    public static final Type<LandmarksPayload> TYPE = new Type<>(Oneirgeo.id("landmarks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LandmarksPayload> CODEC = Landmark.STREAM_CODEC
            .apply(ByteBufCodecs.list(1024))
            .map(LandmarksPayload::new, LandmarksPayload::landmarks)
            .cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
