package com.sorkopiko.teammatestracker.model;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public record InterpolatedLocation(double x, double y, double z) {
    public static InterpolatedLocation fromTeammate(Teammate teammate, long currentTime) {
        return new InterpolatedLocation(
                teammate.getInterpolatedX(currentTime),
                teammate.getInterpolatedY(currentTime),
                teammate.getInterpolatedZ(currentTime)
        );
    }

    public static InterpolatedLocation fromPlayer(PlayerEntity player, double tickDelta) {
        return new InterpolatedLocation(
                MathHelper.lerp(tickDelta, player.prevX, player.getX()),
                MathHelper.lerp(tickDelta, player.prevY, player.getY()),
                MathHelper.lerp(tickDelta, player.prevZ, player.getZ())
        );
    }

    public InterpolatedLocation withOffset(double xOffset, double yOffset, double zOffset) {
        return new InterpolatedLocation(x() + xOffset, y() + yOffset, z() + zOffset);
    }

    public InterpolatedLocation relativeToCamera(Camera camera) {
        return this.withOffset(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
    }
}
