package com.sorkopiko.teammatestracker.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sorkopiko.teammatestracker.config.TeammatesConfig;
import com.sorkopiko.teammatestracker.model.InterpolatedLocation;
import com.sorkopiko.teammatestracker.model.Teammate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.awt.*;
import java.util.Map;
import java.util.UUID;
//? if >= 1.21.5 {
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
//? if >= 1.21.6 {
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.buffers.Std140Builder;
import net.minecraft.client.gl.MappableRingBuffer;
//?}
//?} else if >= 1.21.2 {
/*import net.minecraft.client.gl.ShaderProgramKeys;
*///?}

//? if >= 1.21.9 {
import net.minecraft.client.render.entity.EntityRenderManager;
//?} else {
/*import net.minecraft.client.render.entity.EntityRenderDispatcher;
*///?}

public class TeammateRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final float ARROW_SIZE = 0.3f;
    private static final int TEXT_OFFSET = 20;
    private static final float ARROW_OFFSET_Y = 2.f;

    //? if >= 1.21.5 {
    private static final RenderPipeline POSITION_COLOR_TRIANGLE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation(Identifier.of("teammates_tracker", "pipeline/position_color_triangle"))
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
                    .withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withCull(false)
                    .build()
    );

    private static final RenderLayer ARROW_RENDER_LAYER = RenderLayer.of(
            "teammates_tracker_arrow",
            RenderLayer.DEFAULT_BUFFER_SIZE,
            false,
            true,
            POSITION_COLOR_TRIANGLE,
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );
    //?}

    //? if >= 1.21.6 {
    private static final MappableRingBuffer fogBuffer = new MappableRingBuffer(
            () -> "Custom Fog Buffer",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            new Std140SizeCalculator()
                    .putFloat()  // start
                    .putFloat()  // end
                    .putInt()    // shape enum
                    .putVec4()   // color (r, g, b, a)
                    .get()
    );
    //?}

    public static void render(MatrixStack matrices, Camera camera, Map<UUID, Teammate> teammates, double tickDelta) {
        if (client.player == null || client.world == null || !TeammatesConfig.HANDLER.instance().enabled) return;

        Vec3d cameraPos = camera.getPos();
        String currentWorld = client.world.getRegistryKey().getValue().getPath();

        //? if <= 1.21.4 {
        /*RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        *///?}

        //? if >= 1.21.6 {
        GpuBufferSlice originalFog = RenderSystem.getShaderFog();

        fogBuffer.rotate();

        try (GpuBuffer.MappedView view = RenderSystem.getDevice()
                .createCommandEncoder()
                .mapBuffer(fogBuffer.getBlocking(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putFloat(Float.MAX_VALUE)
                    .putFloat(Float.MAX_VALUE)
                    .putInt(0)
                    .putVec4(0.f, 0.f, 0.f, 0.f);
        }

        RenderSystem.setShaderFog(fogBuffer.getBlocking().slice());
        //?} else if >= 1.21.2 {
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
        /*float originalFogStart = RenderSystem.getShaderFogStart();
        float originalFogEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
        *///?}

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
                    InterpolatedLocation location = getLocation(teammate, currentTime, tickDelta);

                    double distance = calculateDistance(location, cameraPos);
                    renderTeammateMarker(
                            matrices,
                            location.withOffset(0, ARROW_OFFSET_Y + TeammatesConfig.HANDLER.instance().yOffset, 0)
                                    .relativeToCamera(cameraPos),
                            teammate,
                            distance
                    );
                });

        matrices.pop();

        //? if >= 1.21.2 {
        RenderSystem.setShaderFog(originalFog);
        //?} else {
        /*RenderSystem.setShaderFogStart(originalFogStart);
        RenderSystem.setShaderFogEnd(originalFogEnd);
        *///?}

        //? if <= 1.21.4 {
        /*RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        *///?}
    }

    private static void renderTeammateMarker(MatrixStack matrices, InterpolatedLocation location, Teammate teammate, double distance) {
        if (client.textRenderer == null) return;

        matrices.push();
        matrices.translate(location.x(), location.y(), location.z());

        //? if >= 1.21.9 {
        EntityRenderManager dispatcher = client.getEntityRenderDispatcher();
        matrices.multiply(dispatcher.camera.getRotation());
        //?} else {
        /*EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        matrices.multiply(dispatcher.getRotation());
        *///?}
        float scale = (float) getScale(distance, TeammatesConfig.HANDLER.instance().markerScale);
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

        //? if >= 1.21.5 {
        ARROW_RENDER_LAYER.draw(buffer.end());
        //?} else if >= 1.21.2 {
        /*RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        *///?} else {
        /*RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        *///?}
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
        return scaleFactor * (distance / 10.f);
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