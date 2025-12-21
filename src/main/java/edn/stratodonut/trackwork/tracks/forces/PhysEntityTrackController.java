package edn.stratodonut.trackwork.tracks.forces;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mojang.datafixers.util.Pair;
import edn.stratodonut.trackwork.TrackworkMod;
import edn.stratodonut.trackwork.tracks.data.PhysEntityTrackData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

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

    @JsonIgnore
    @Deprecated(forRemoval = true)
    public final HashMap<Integer, PhysEntityTrackData> trackData = new HashMap<>();

    public final HashMap<Long, PhysEntityTrackData> trackData2 = new HashMap<>();
    @JsonIgnore
    private final ConcurrentLinkedQueue<Pair<Long, PhysEntityTrackData.CreateData>> createdTrackData = new ConcurrentLinkedQueue<>();
    @JsonIgnore
    private final ConcurrentHashMap<Long, PhysEntityTrackData.UpdateData> trackUpdateData = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Long> removedTracks = new ConcurrentLinkedQueue<>();
    private final HashMap<Long, Integer> posToJointId = new HashMap<>();

    @JsonIgnore
    @Deprecated(forRemoval = true)
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
            Pair<Long, PhysEntityTrackData.CreateData> createData = this.createdTrackData.remove();
            if (createData.getFirst() != null && createData.getSecond() != null) this.trackData2.put(createData.getFirst(), PhysEntityTrackData.from(createData.getSecond()));
            else TrackworkMod.warn("Tried to create a PE track of ID {} with no data!", createData.getFirst());
        }

        this.trackUpdateData.forEach((id, data) -> {
            PhysEntityTrackData old = this.trackData2.get(id);
            if (old != null) {
                this.trackData2.put(id, old.updateWith(data));
            }
        });
        this.trackUpdateData.clear();

//         Idk why, but sometimes removing a block can send an update in the same tick(?), so this is last.
        while (!removedTracks.isEmpty()) {
            Long removeId = this.removedTracks.remove();
            this.trackData2.remove(removeId);
        }

        if (this.trackData2.isEmpty()) return;

        double coefficientOfPower = Math.min(1.0d, 4d / this.trackData2.size());
        this.trackData2.forEach((id, data) -> {
            PhysShip wheel = physLevel.getShipById(data.shiptraptionID);
            Pair<Vector3dc, Vector3dc> forces = this.computeForce(data, ((PhysShipImpl) physShip), (PhysShipImpl)wheel, coefficientOfPower);
            if (forces != null) {
                if (forces.getSecond().isFinite()) wheel.applyWorldTorque(forces.getSecond());
            }
        });
    }

    private Pair<@NotNull Vector3dc, @NotNull Vector3dc> computeForce(PhysEntityTrackData data, PhysShip ship, PhysShip wheel, double coefficientOfPower) {
        if (wheel != null) {
            double m = ship.getMass();
            ShipTransform shipTransform = ship.getTransform();
            Vector3dc wheelAxis = shipTransform.getShipToWorldRotation().transform(data.wheelAxis, new Vector3d());
            double wheelSpeed = wheel.getKinematics().getAngularVelocity().dot(wheelAxis);
            double slip = Math.clamp(-3, 3, -data.trackRPM - wheelSpeed);
            Vector3dc driveTorque = wheelAxis.mul(-slip * m * 0.4 * coefficientOfPower, new Vector3d());
            return new Pair<>(new Vector3d(0), driveTorque);
        }
        return null;
    }

    public void addTrackBlock(BlockPos pos, PhysEntityTrackData.CreateData data, int axleId) {
        this.createdTrackData.add(new Pair<>(pos.asLong(), data));
        posToJointId.put(pos.asLong(), axleId);
    }

    public void updateTrackBlock(BlockPos pos, PhysEntityTrackData.UpdateData data) {
        this.trackUpdateData.put(pos.asLong(), data);
    }

    public void removeTrackBlock(ServerLevel level, BlockPos pos) {
        this.removedTracks.add(pos.asLong());
        posToJointId.computeIfPresent(
                pos.asLong(),
                (k, id) -> {
                    ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level)).removeJoint(id);
                    return null;
                }
        );
    }

    @Deprecated
    public void resetController() {
        // Do nothing
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
            return Objects.equals(this.trackData2, otherController.trackData2) &&
                    Objects.equals(this.trackUpdateData, otherController.trackUpdateData) &&
                    areQueuesEqual(this.createdTrackData, otherController.createdTrackData) &&
                    areQueuesEqual(this.removedTracks, otherController.removedTracks);
        }
    }
}
