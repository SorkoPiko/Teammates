package com.sorkopiko.teammatestracker.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ApolloPayload(byte[] data) implements CustomPayload {
    public static final CustomPayload.Id<ApolloPayload> ID = new CustomPayload.Id<>(Identifier.of("lunar", "apollo"));

    public static final PacketCodec<RegistryByteBuf, ApolloPayload> CODEC = PacketCodec.of(
            ApolloPayload::serialize,
            ApolloPayload::deserialize
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void serialize(ApolloPayload payload, RegistryByteBuf buf) {
        buf.writeByteArray(payload.data);
    }

    public static ApolloPayload deserialize(RegistryByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return new ApolloPayload(data);
    }
}