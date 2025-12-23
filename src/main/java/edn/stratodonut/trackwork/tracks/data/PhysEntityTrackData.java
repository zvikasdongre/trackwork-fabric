package edn.stratodonut.trackwork.tracks.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joml.Vector3dc;
import org.valkyrienskies.core.internal.joints.VSRevoluteJoint;

import javax.annotation.Nonnull;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public class PhysEntityTrackData {
    public final Vector3dc trackPos;
    public final Vector3dc wheelAxis;
    public final long shiptraptionID;
    public final double springConstant;
    public final double damperConstant;
    @JsonIgnore
    public final VSRevoluteJoint constraint;
    public volatile Integer axleId;
    public final double trackRPM;
    public float trackSU;
    public double previousSpringDist;

    // For Jackson
    private PhysEntityTrackData() {
        this.trackPos = null;
        this.wheelAxis = null;
        this.springConstant = 0;
        this.damperConstant = 0;
        this.constraint = null;
        this.shiptraptionID = -1L;
        this.axleId = null;
        this.trackRPM = 0;
        this.previousSpringDist = 0;
    }

    private PhysEntityTrackData(Vector3dc trackPos, Vector3dc wheelAxis, long shiptraptionID, double springConstant, double damperConstant, VSRevoluteJoint constraint, int axleId, double trackRPM, double springDist) {
        this.trackPos = trackPos;
        this.wheelAxis = wheelAxis;
        this.springConstant = springConstant;
        this.damperConstant = damperConstant;
        this.constraint = constraint;
        this.shiptraptionID = shiptraptionID;
        this.axleId = axleId;
        this.trackRPM = trackRPM;
        this.previousSpringDist = springDist;
    }

    public final PhysEntityTrackData updateWith(@Nonnull UpdateData update) {
        return new PhysEntityTrackData(this.trackPos, this.wheelAxis, update.shiptraptionID, update.springConstant, update.damperConstant, this.constraint, axleId, update.trackRPM, this.previousSpringDist);
    }

    public static PhysEntityTrackData from(@Nonnull CreateData data) {
        return new PhysEntityTrackData(data.trackPos, data.wheelAxis, data.shiptraptionID, data.springConstant, data.damperConstant, data.constraint, -1, data.trackRPM, 0);
    }

    public record UpdateData(double springConstant, double damperConstant, double trackRPM, long shiptraptionID) {
    }

    public record CreateData(Vector3dc trackPos, Vector3dc wheelAxis, long shiptraptionID, double springConstant, double damperConstant, VSRevoluteJoint constraint, double trackRPM) {
    }
}
