package com.sorkopiko.teammatestracker.model;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.UUID;

public class Teammate {
    private final UUID playerId;
    private double x, y, z;
    private double lastX, lastY, lastZ;
    private String worldName;
    private Text displayName;
    private Color markerColor;

    private long lastUpdate;
    private long lastLastUpdate;

    public Teammate(UUID playerId, long lastUpdate) {
        this.playerId = playerId;
        this.lastUpdate = lastUpdate;
    }

    public void update(double x, double y, double z, String worldName, Text displayName, Color markerColor, long lastUpdate) {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.displayName = displayName;
        this.markerColor = markerColor;
        this.lastLastUpdate = this.lastUpdate;
        this.lastUpdate = lastUpdate;
    }

    public UUID getPlayerId() { return playerId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getWorldName() { return worldName; }
    public Color getMarkerColor() { return markerColor; }
    public Text getDisplayName() { return displayName; }
    public long getLastUpdate() { return lastUpdate; }

    /**
     * Returns interpolated X position based on time delta
     * @param currentTime Current time in milliseconds
     * @return Interpolated X coordinate
     */
    public double getInterpolatedX(long currentTime) {
        return interpolatePosition(lastX, x, currentTime);
    }

    /**
     * Returns interpolated Y position based on time delta
     * @param currentTime Current time in milliseconds
     * @return Interpolated Y coordinate
     */
    public double getInterpolatedY(long currentTime) {
        return interpolatePosition(lastY, y, currentTime);
    }

    /**
     * Returns interpolated Z position based on time delta
     * @param currentTime Current time in milliseconds
     * @return Interpolated Z coordinate
     */
    public double getInterpolatedZ(long currentTime) {
        return interpolatePosition(lastZ, z, currentTime);
    }

    /**
     * Helper method to interpolate between two positions
     * @param lastPos Previous position
     * @param currentPos Current position
     * @param currentTime Current time in milliseconds
     * @return Interpolated position
     */
    private double interpolatePosition(double lastPos, double currentPos, long currentTime) {
        if (lastLastUpdate == 0 || lastUpdate == lastLastUpdate) {
            return currentPos;
        }

        long timeDelta = lastUpdate - lastLastUpdate;
        long timeSinceLastUpdate = currentTime - lastUpdate;

        double tickDelta = (double) timeSinceLastUpdate / timeDelta;

        return MathHelper.lerp(tickDelta, lastPos, currentPos);
    }

    public double distanceTo(Teammate other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}