package com.sorkopiko.teammatestracker.client;

import com.google.protobuf.*;
import com.sorkopiko.teammatestracker.render.TeammateRenderer;
import lunarclient.apollo.team.v1.Schema.*;
import com.sorkopiko.teammatestracker.config.TeammatesConfig;
import com.sorkopiko.teammatestracker.model.Teammate;
import com.sorkopiko.teammatestracker.packet.ApolloPayload;
import com.sorkopiko.teammatestracker.packet.UpdateTeamMembersPayload;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Entrypoint
public class TeammatesTrackerClient implements ClientModInitializer {
    public static final String MOD_ID = "teammates-tracker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private final Map<UUID, Teammate> teammates = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Teammates Tracker initialized!");

        PayloadTypeRegistry.playS2C().register(ApolloPayload.ID, ApolloPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ApolloPayload.ID, (payload, context) -> {
            try {
                Any message = Any.parseFrom(payload.data());
                LOGGER.debug("Received Apollo message of type {}", message.getTypeUrl());

                safeUnpack(message, UpdateTeamMembersMessage.class).ifPresent(updateMsg -> {
                    UpdateTeamMembersPayload updatePayload = UpdateTeamMembersPayload.fromProto(updateMsg);
                    handleUpdateTeamMembers(updatePayload, context);
                    LOGGER.debug("Received update team members payload");
                });

                safeUnpack(message, ResetTeamMembersMessage.class).ifPresent(resetMsg -> {
                    teammates.clear();
                    LOGGER.debug("Received reset team members payload");
                });

            } catch (Exception e) {
                LOGGER.warn("Failed to process Apollo message: {}", e.getMessage());
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register((context) -> TeammateRenderer.render(context.matrixStack(), context.camera(), teammates, context.tickCounter().getTickDelta(true)));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> teammates.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> teammates.clear());

        TeammatesConfig.HANDLER.load();
    }

    private void handleUpdateTeamMembers(UpdateTeamMembersPayload payload, ClientPlayNetworking.Context context) {
        ClientWorld world = context.client().world;
        long currentTime = System.currentTimeMillis();
        RegistryWrapper.WrapperLookup registries = world != null ? world.getRegistryManager() : DynamicRegistryManager.EMPTY;

        Set<UUID> currentTeammateIds = payload.members().stream()
                .map(UpdateTeamMembersPayload.TeamMemberPayload::playerId)
                .collect(Collectors.toSet());

        teammates.keySet().removeIf(playerId -> !currentTeammateIds.contains(playerId));
        teammates.values().removeIf(teammate -> teammate.getLastUpdate() < currentTime - 30000);

        payload.members().forEach(teammember -> {
            Teammate teammate = teammates.computeIfAbsent(teammember.playerId(), id -> new Teammate(id, currentTime));

            teammate.update(
                    teammember.x(),
                    teammember.y(),
                    teammember.z(),
                    teammember.worldName(),
                    Text.Serialization.fromJson(teammember.displayName(), registries),
                    teammember.markerColor(),
                    currentTime
            );
        });
    }

    private static <T extends GeneratedMessage> Optional<T> safeUnpack(Any message, Class<T> clazz) {
        try {
            return message.is(clazz) ? Optional.of(message.unpack(clazz)) : Optional.empty();
        } catch (InvalidProtocolBufferException e) {
            LOGGER.warn("Failed to unpack message of type {}: {}", message.getTypeUrl(), e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Failed to check message type for {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }
}