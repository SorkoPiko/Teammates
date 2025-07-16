package com.sorkopiko.teammatestracker.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sorkopiko.teammatestracker.client.TeammatesTrackerClient;
import com.sorkopiko.teammatestracker.config.TeammatesConfig;
import com.sorkopiko.teammatestracker.model.InterpolatedLocation;
import com.sorkopiko.teammatestracker.model.Teammate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.awt.*;
import java.util.Map;
import java.util.UUID;
//? if >= 1.21.2 {
/*import net.minecraft.client.gl.ShaderProgramKeys;
*///?}

public class TeammateRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final float ARROW_SIZE = 0.3f;
    private static final int TEXT_OFFSET = 20;
    private static final float ARROW_OFFSET_Y = 2.15f;

    public static void render(MatrixStack matrices, Camera camera, Map<UUID, Teammate> teammates, double tickDelta) {
        if (client.player == null || client.world == null || !TeammatesConfig.HANDLER.instance().enabled) return;

        Vec3d cameraPos = camera.getPos();
        String currentWorld = client.world.getRegistryKey().getValue().getPath();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        //? if >= 1.21.2 {
        /*Fog originalFog = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(new Fog(
                Float.MAX_VALUE,
                Float.MAX_VALUE,
                FogShape.SPHERE,
                0.f,
                0.f,
                0.f,
                0.f
        ));
        *///?} else {
        float originalFogStart = RenderSystem.getShaderFogStart();
        float originalFogEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
        //?}

        long currentTime = System.currentTimeMillis();

        matrices.push();

        teammates.values().stream()
                .filter(teammate -> !teammate.getPlayerId().equals(client.player.getUuid()) && currentWorld.equals(teammate.getWorldName()))
                .sorted((a, b) -> {
                    double distanceA = calculateDistance(getLocation(a, currentTime, tickDelta), cameraPos);
                    double distanceB = calculateDistance(getLocation(b, currentTime, tickDelta), cameraPos);
                    return Double.compare(distanceB, distanceA);
                })
                .forEach(teammate -> {
                    InterpolatedLocation location = getLocation(teammate, currentTime, tickDelta)
                            .withOffset(0, ARROW_OFFSET_Y, 0)
                            .relativeToCamera(camera);

                    double distance = calculateDistance(location, cameraPos);
                    renderTeammateMarker(matrices, location, teammate, distance);
                });

        matrices.pop();

        //? if >= 1.21.2 {
        /*RenderSystem.setShaderFog(originalFog);
        *///?} else {
        RenderSystem.setShaderFogStart(originalFogStart);
        RenderSystem.setShaderFogEnd(originalFogEnd);
        //?}
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderTeammateMarker(MatrixStack matrices, InterpolatedLocation location, Teammate teammate, double distance) {
        if (client.textRenderer == null) return;

        matrices.push();
        matrices.translate(location.x(), location.y(), location.z());

        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        matrices.multiply(dispatcher.getRotation());
        float scale = (float) getScale(distance, TeammatesConfig.HANDLER.instance().markerScale * 0.25f);
        matrices.scale(scale, scale, scale);
        int backgroundColor = (int)(MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24 | 0x666666;

        Text displayName = teammate.getDisplayName();
        if (displayName != null) {
            renderText(matrices, displayName, 0, -TEXT_OFFSET-10, 0xFFFFFF, backgroundColor, true, distance);
        }

        String distanceText = String.format("%.1fm", distance);
        renderArrow(matrices, teammate.getMarkerColor());
        renderText(matrices, Text.literal(distanceText), 0, -TEXT_OFFSET, 0xFFFFFF, backgroundColor, true, distance);

        matrices.pop();
    }

    private static void renderArrow(MatrixStack matrices, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        float halfSize = ARROW_SIZE / 2.0f;

        buffer.vertex(matrix, -halfSize, halfSize, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, -halfSize, 0).color(r, g, b, a);
        buffer.vertex(matrix, halfSize, halfSize, 0).color(r, g, b, a);

        //? if >= 1.21.2 {
        /*RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        *///?} else {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         //?}
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void renderText(MatrixStack matrices, Text text, float x, float y, int color, int backgroundColor, boolean centered, double distance) {
        matrices.push();
        float scale = 0.015f;
        matrices.scale(scale, -scale, scale);

        TextRenderer textRenderer = client.textRenderer;

        if (centered) x += (-textRenderer.getWidth(text) / 2.0f);

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        textRenderer.draw(text, x, y, color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, backgroundColor, 15728880);
        immediate.draw();

        matrices.pop();
    }

    private static double getScale(double distance, float scaleFactor) {
        if (distance < 10.f) return scaleFactor;
//        return scaleFactor * (distance / 10.f);
        return scaleFactor * (distance - 9.f);
    }

    private static double calculateDistance(InterpolatedLocation location, Vec3d cameraPos) {
        double dx = location.x() - cameraPos.x;
        double dy = location.y() - cameraPos.y;
        double dz = location.z() - cameraPos.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static PlayerEntity getLoadedPlayer(UUID playerId) {
        if (client.world == null) return null;

        return client.world.getPlayers().stream()
                .filter(player -> player.getUuid().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    private static InterpolatedLocation getLocation(Teammate teammate, long currentTime, double tickDelta) {
        PlayerEntity player = getLoadedPlayer(teammate.getPlayerId());
        if (player != null) {
            return InterpolatedLocation.fromPlayer(player, tickDelta);
        } else {
            return InterpolatedLocation.fromTeammate(teammate, currentTime);
        }
    }
}