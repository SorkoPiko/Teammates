//? if >= 1.21.9 {
package com.sorkopiko.teammatestracker.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sorkopiko.teammatestracker.client.TeammatesTrackerClient;
import com.sorkopiko.teammatestracker.render.TeammateRenderer;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class) @MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public class WorldRendererMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;renderLateDebug(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/util/math/Vec3d;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/client/render/Frustum;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterLateDebug(
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f positionMatrix,
            Matrix4f matrix4f,
            Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci,
            @Local FrameGraphBuilder frameGraphBuilder
    ) {
        FramePass framePass = frameGraphBuilder.createPass("teammate_render");
        DefaultFramebufferSet fbs = ((WorldRendererAccessor) this).getFramebufferSet();
        fbs.mainFramebuffer = framePass.transfer(fbs.mainFramebuffer);
        framePass.setRenderer(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            MatrixStack matrices = new MatrixStack();
            matrices.multiplyPositionMatrix(positionMatrix);
            TeammateRenderer.render(
                    matrices,
                    camera,
                    TeammatesTrackerClient.getTeammates(),
                    tickCounter.getTickProgress(true)
            );
        });
    }
}
//?}