package edn.stratodonut.trackwork.tracks.forces;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mojang.datafixers.util.Pair;
import edn.stratodonut.trackwork.TrackworkMod;
import edn.stratodonut.trackwork.tracks.data.PhysEntityTrackData;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public final class PhysEntityTrackController implements ShipPhysicsListener {
    @JsonIgnore
    public static final double RPM_TO_RADS = 0.10471975512;
    @JsonIgnore
    public static final Vector3dc UP = new Vector3d(0, 1, 0);
    public final HashMap<Integer, PhysEntityTrackData> trackData = new HashMap<>();
    @JsonIgnore
    private final ConcurrentLinkedQueue<Pair<Integer, PhysEntityTrackData.CreateData>> createdTrackData = new ConcurrentLinkedQueue<>();
    @JsonIgnore
    private final ConcurrentHashMap<Integer, PhysEntityTrackData.UpdateData> trackUpdateData = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Integer> removedTracks = new ConcurrentLinkedQueue<>();
    private int nextBearingID = 0;

    public PhysEntityTrackController() {
    }

    public static PhysEntityTrackController getOrCreate(LoadedServerShip ship) {
        if (ship.getAttachment(PhysEntityTrackController.class) == null) {
            ship.setAttachment(PhysEntityTrackController.class, new PhysEntityTrackController());
        }

        return ship.getAttachment(PhysEntityTrackController.class);
    }


    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        while (!this.createdTrackData.isEmpty()) {
            Pair<Integer, PhysEntityTrackData.CreateData> createData = this.createdTrackData.remove();
            if (createData.getFirst() != null && createData.getSecond() != null) this.trackData.put(createData.getFirst(), PhysEntityTrackData.from(createData.getSecond()));
            else TrackworkMod.warn("Tried to create a PE track of ID {} with no data!", createData.getFirst());
        }

        this.trackUpdateData.forEach((id, data) -> {
            PhysEntityTrackData old = this.trackData.get(id);
            if (old != null) {
                this.trackData.put(id, old.updateWith(data));
            }
        });
        this.trackUpdateData.clear();

        // Idk why, but sometimes removing a block can send an update in the same tick(?), so this is last.
        while (!removedTracks.isEmpty()) {
            Integer removeId = this.removedTracks.remove();
            PhysEntityTrackData track = this.trackData.remove(removeId);
            ((VsiPhysLevel) physLevel).removeJoint(track.axleId);
        }

        if (this.trackData.isEmpty()) return;

        Vector3d netLinearForce = new Vector3d(0);

        double coefficientOfPower = Math.min(1.0d, 4d / this.trackData.size());
        this.trackData.forEach((id, data) -> {
            PhysShip wheel = physLevel.getShipById(data.shiptraptionID);
            Pair<Vector3dc, Vector3dc> forces = this.computeForce(data, ((PhysShipImpl) physShip), (PhysShipImpl)wheel, coefficientOfPower);
            if (forces != null) {
                netLinearForce.add(forces.getFirst());
                if (forces.getSecond().isFinite()) wheel.applyInvariantTorque(forces.getSecond());
            }
        });
        if (netLinearForce.isFinite()) physShip.applyInvariantForce(netLinearForce);
    }

    private Pair<@NotNull Vector3dc, @NotNull Vector3dc> computeForce(PhysEntityTrackData data, PhysShip ship, PhysShip wheel, double coefficientOfPower) {
        if (wheel != null) {
            double m = ship.getMass();
            ShipTransform shipTransform = ship.getTransform();
            Vector3dc trackPos = shipTransform.getShipToWorld().transformPosition(data.trackPos, new Vector3d());
            Vector3dc springVec = wheel.getTransform().getPositionInWorld().sub(trackPos, new Vector3d());
            double springDist = Math.clamp(0.0, 1.5, 1.5 - springVec.length());
            assert data.constraint != null;
            Vector3dc springForce =  data.constraint.getPose0().getPos().mul(m * 8.0 * springDist, new Vector3d());
            double distDelta = Math.clamp(-5, 5, (springDist - data.previousSpringDist));
            double damperForce = (distDelta / 20) * m * 3000.0;
            springForce = springForce.add(data.constraint.getPose0().getPos().mul(damperForce, new Vector3d()), new Vector3d());
            data.previousSpringDist = springDist;

            Vector3dc wheelAxis = shipTransform.getShipToWorldRotation().transform(data.wheelAxis, new Vector3d());
            double wheelSpeed = wheel.getKinematics().getAngularVelocity().dot(wheelAxis);
            double slip = Math.clamp(-3, 3, -data.trackRPM - wheelSpeed);
            Vector3dc driveTorque = wheelAxis.mul(-slip * m * 0.4 * coefficientOfPower, new Vector3d());
            return new Pair<>(new Vector3d(0), driveTorque);
        }
        return null;
    }

    public final int addTrackBlock(PhysEntityTrackData.CreateData data) {
        this.createdTrackData.add(new Pair<>(++nextBearingID, data));
        return this.nextBearingID;
    }

    public final void updateTrackBlock(Integer id, PhysEntityTrackData.UpdateData data) {
        this.trackUpdateData.put(id, data);
    }

    public final void removeTrackBlock(ServerLevel level, int id) {
        PhysEntityTrackData data = this.trackData.get(id);
//        if (data != null) {
//            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(data.springId);
//            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(data.axleId);
//        }
        this.removedTracks.add(id);
    }

    public final void resetController() {
        for (int i = 0; i < nextBearingID ; i++) {
            this.removedTracks.add(i);
        }
        this.nextBearingID = 0;
    }

    public static <T> boolean areQueuesEqual(Queue<T> left, Queue<T> right) {
        return Arrays.equals(left.toArray(), right.toArray());
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof PhysEntityTrackController otherController)) {
            return false;
        } else {
            return Objects.equals(this.trackData, otherController.trackData) &&
                    Objects.equals(this.trackUpdateData, otherController.trackUpdateData) &&
                    areQueuesEqual(this.createdTrackData, otherController.createdTrackData) &&
                    areQueuesEqual(this.removedTracks, otherController.removedTracks) &&
                    this.nextBearingID == otherController.nextBearingID;
        }
    }
}
