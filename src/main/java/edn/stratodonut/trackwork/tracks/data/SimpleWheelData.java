package edn.stratodonut.trackwork.tracks.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleWheelData {
    public final Vector3dc wheelOriginPosition;
    public final Vector3dc wheelContactPosition;
    public final Vector3dc driveForceVector;
    public final Vector3dc wheelNormal;
    public final boolean isFreespin;
    @Nullable
    public Long groundShipId;
    public final boolean isWheelGrounded;
    public float wheelRPM;

    public float trackSU;
    @PhysTickOnly
    @Nullable
    public Vector3dc lastSuspensionForce;

    // For Jackson serialisation
    private SimpleWheelData() {
        this(null);
    }

    private SimpleWheelData(Vector3dc wheelOriginPosition) {
        this.wheelOriginPosition = wheelOriginPosition;
        this.wheelContactPosition = new Vector3d(0);
        this.driveForceVector = new Vector3d(0);
        this.wheelNormal = new Vector3d(0, -1, 0);
        this.isFreespin = true;
        this.isWheelGrounded = false;
        this.wheelRPM = 0;
    }

    public SimpleWheelData(Vector3dc wheelOriginPosition, Vector3dc wheelContactPosition, Vector3dc driveForceVector,
                           Vector3dc wheelNormal, boolean isFreespin, @Nullable Long groundShipId, boolean isWheelGrounded, float wheelRPM) {
        this.wheelOriginPosition = wheelOriginPosition;
        this.wheelContactPosition = wheelContactPosition;
        this.driveForceVector = driveForceVector;
        this.wheelNormal = wheelNormal;
        this.isFreespin = isFreespin;
        this.groundShipId = groundShipId;
        this.isWheelGrounded = isWheelGrounded;
        this.wheelRPM = wheelRPM;
    }

    public final SimpleWheelData updateWith(SimpleWheelUpdateData update) {
        return new SimpleWheelData(
                this.wheelOriginPosition,
                this.wheelContactPosition,
                this.driveForceVector,
                this.wheelNormal,
                update.isFreespin,
                this.groundShipId,
                this.isWheelGrounded,
                update.trackRPM
        );
    }

    public static SimpleWheelData from(SimpleWheelCreateData data) {
        return new SimpleWheelData(data.trackOriginPosition);
    }

    public record SimpleWheelUpdateData(float steeringValue, float trackRPM, Direction.Axis wheelAxis, float axialOffset, float horizontalOffset,
                                        double susScaled, double wheelRadius, boolean isFreespin) {
    }

    public record ExtraWheelData(float steeringValue, Direction.Axis wheelAxis, float axialOffset, float horizontalOffset,
                                 double susScaled, double wheelRadius) {
        public static ExtraWheelData from(SimpleWheelUpdateData data) {
            return new ExtraWheelData(
                    data.steeringValue,
                    data.wheelAxis,
                    data.axialOffset,
                    data.horizontalOffset,
                    data.susScaled,
                    data.wheelRadius
            );
        }

        public static ExtraWheelData empty() {
            return new ExtraWheelData(0f, Direction.Axis.X, 0f, 0f, 0.0, 0.0);
        }
    }

    public record SimpleWheelCreateData(Vector3dc trackOriginPosition) {}
}
