package net.zvikasdongre.trackwork.blocks;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface ITrackPointProvider {
    float getPointDownwardOffset(float var1);

    boolean isBeltLarge();

    float getPointHorizontalOffset();

    Vec3d getTrackPointSlope(float var1);

    @NotNull
    ITrackPointProvider.PointType getTrackPointType();

    @NotNull
    ITrackPointProvider.PointType getNextPoint();

    float getWheelRadius();

    public static enum PointType {
        WRAP,
        GROUND,
        BLANK,
        NONE;
    }
}
