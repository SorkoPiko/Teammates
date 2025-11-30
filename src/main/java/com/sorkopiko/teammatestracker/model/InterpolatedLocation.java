package com.sorkopiko.teammatestracker.model;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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
                //? if >= 1.21.5 {
                MathHelper.lerp(tickDelta, player.lastX, player.getX()),
                MathHelper.lerp(tickDelta, player.lastY, player.getY()),
                MathHelper.lerp(tickDelta, player.lastZ, player.getZ())
                //?} else {
                /*MathHelper.lerp(tickDelta, player.prevX, player.getX()),
                MathHelper.lerp(tickDelta, player.prevY, player.getY()),
                MathHelper.lerp(tickDelta, player.prevZ, player.getZ())
                *///?}
        );
    }

    public InterpolatedLocation withOffset(double xOffset, double yOffset, double zOffset) {
        return new InterpolatedLocation(x() + xOffset, y() + yOffset, z() + zOffset);
    }

    public InterpolatedLocation relativeToCamera(Vec3d camera) {
        return this.withOffset(-camera.x, -camera.y, -camera.z);
    }
}
