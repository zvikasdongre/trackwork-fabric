package edn.stratodonut.trackwork;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.bodies.properties.BodyKinematics;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;

public class TrackworkUtil {
    public static final Vector3dc ZERO = new Vector3d();

    public static Vector3dc accumulatedVelocity(ShipTransform t, BodyKinematics pose, Vector3dc worldPosition) {
        return pose.getVelocity().add(pose.getAngularVelocity().cross(worldPosition.sub(t.getPositionInWorld(), new Vector3d()), new Vector3d()), new Vector3d());
    }

    public static double roundTowardZero(double val) {
        if (val < 0) {
            return Math.ceil(val);
        }
        return Math.floor(val);
    }

    public static Direction.Axis around(Direction.Axis axis) {
        if (axis.isVertical()) return axis;
        return (axis == Direction.Axis.X) ? Direction.Axis.Z : Direction.Axis.X;
    }

    public static Vec3 getActionNormal(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Vec3(0, -1, 0);
            case Y -> new Vec3(0,0, 0);
            case Z -> new Vec3(0, -1, 0);
        };
    }

    public static Vector3d getAxisAsVec(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Vector3d(1, 0, 0);
            case Y -> new Vector3d(0,1, 0);
            case Z -> new Vector3d(0, 0, 1);
        };
    }
    
    public static Vector3d getForwardVec3d(Direction.Axis axis, float length) {
        return switch (axis) {
            case X -> new Vector3d(0, 0, length);
            case Y -> new Vector3d(0,0, 0);
            case Z -> new Vector3d(length, 0, 0);
        };
    }
}
