package com.sorkopiko.teammatestracker.packet;

import lunarclient.apollo.common.v1.ColorOuterClass;
import lunarclient.apollo.common.v1.LocationOuterClass;
import lunarclient.apollo.common.v1.UuidOuterClass;
import lunarclient.apollo.team.v1.Schema.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record UpdateTeamMembersPayload(List<TeamMemberPayload> members) implements CustomPayload {
    public static final CustomPayload.Id<UpdateTeamMembersPayload> ID = new CustomPayload.Id<>(Identifier.of("lunar", "apollo"));
    public static final PacketCodec<RegistryByteBuf, UpdateTeamMembersPayload> CODEC = PacketCodec.of(
            UpdateTeamMembersPayload::serialize,
            UpdateTeamMembersPayload::deserialize
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void serialize(UpdateTeamMembersPayload payload, RegistryByteBuf buf) {
        try {
            List<TeamMember> teamMembersProto = payload.members.stream()
                    .map(teamMember -> TeamMember.newBuilder()
                            .setPlayerUuid(toProtobuf(teamMember.playerId()))
                            .setAdventureJsonPlayerName(teamMember.displayName())
                            .setLocation(toProtobuf(teamMember.x(), teamMember.y(), teamMember.z(), teamMember.worldName()))
                            .setMarkerColor(toProtobuf(teamMember.markerColor()))
                            .build()
                    )
                    .collect(Collectors.toList());
            UpdateTeamMembersMessage message = UpdateTeamMembersMessage.newBuilder()
                    .addAllMembers(teamMembersProto)
                    .build();
            buf.writeBytes(message.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize UpdateTeamMembersPayload", e);
        }
    }

    public static UpdateTeamMembersPayload deserialize(RegistryByteBuf buf) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);

            UpdateTeamMembersMessage message = UpdateTeamMembersMessage.parseFrom(data);
            return fromProto(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize UpdateTeamMembersPayload", e);
        }
    }

    public static UpdateTeamMembersPayload fromProto(UpdateTeamMembersMessage message) {
        List<TeamMemberPayload> teammates = message.getMembersList().stream()
                .map(member -> new TeamMemberPayload(
                        fromProtobuf(member.getPlayerUuid()),
                        member.getLocation().getX(),
                        member.getLocation().getY(),
                        member.getLocation().getZ(),
                        member.getLocation().getWorld(),
                        member.getAdventureJsonPlayerName(),
                        fromProtobuf(member.getMarkerColor())
                ))
                .collect(Collectors.toList());
        return new UpdateTeamMembersPayload(teammates);
    }

    public record TeamMemberPayload(
            UUID playerId,
            double x,
            double y,
            double z,
            String worldName,
            String displayName,
            Color markerColor
    ) {}

    private static UUID fromProtobuf(UuidOuterClass.Uuid uuid) {
        return new UUID(uuid.getHigh64(), uuid.getLow64());
    }

    private static Color fromProtobuf(ColorOuterClass.Color markerColor) {
        return new Color(markerColor.getColor());
    }

    private static UuidOuterClass.Uuid toProtobuf(UUID uuid) {
        return UuidOuterClass.Uuid.newBuilder()
                .setHigh64(uuid.getMostSignificantBits())
                .setLow64(uuid.getLeastSignificantBits())
                .build();
    }

    private static ColorOuterClass.Color toProtobuf(Color color) {
        return ColorOuterClass.Color.newBuilder()
                .setColor(color.getRGB())
                .build();
    }

    private static LocationOuterClass.Location toProtobuf(double x, double y, double z, String worldName) {
        return LocationOuterClass.Location.newBuilder()
                .setX(x)
                .setY(y)
                .setZ(z)
                .setWorld(worldName)
                .build();
    }
}