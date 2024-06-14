package net.zvikasdongre.trackwork.forces;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import kotlin.jvm.functions.Function1;
import net.minecraft.server.world.ServerWorld;
import net.zvikasdongre.trackwork.data.PhysEntityTrackData;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@JsonAutoDetect(
        fieldVisibility = Visibility.ANY
)
public class PhysicsEntityTrackController implements ShipForcesInducer {
   @JsonIgnore
   public static final double RPM_TO_RADS = 0.10471975512;
   @JsonIgnore
   public static final Vector3dc UP = new Vector3d(0.0, 1.0, 0.0);
   public final HashMap<Integer, PhysEntityTrackData> trackData = new HashMap<>();
   @JsonIgnore
   private final ConcurrentLinkedQueue<Pair<Integer, PhysEntityTrackData.CreateData>> createdTrackData = new ConcurrentLinkedQueue<>();
   @JsonIgnore
   private final ConcurrentHashMap<Integer, PhysEntityTrackData.UpdateData> trackUpdateData = new ConcurrentHashMap<>();
   private final ConcurrentLinkedQueue<Integer> removedTracks = new ConcurrentLinkedQueue<>();
   private int nextBearingID = 0;

   public static PhysicsEntityTrackController getOrCreate(ServerShip ship) {
      if (ship.getAttachment(PhysicsEntityTrackController.class) == null) {
         ship.saveAttachment(PhysicsEntityTrackController.class, new PhysicsEntityTrackController());
      }

      return (PhysicsEntityTrackController)ship.getAttachment(PhysicsEntityTrackController.class);
   }

   public void applyForces(@NotNull PhysShip physShip) {
   }

   public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
      while (!this.createdTrackData.isEmpty()) {
         Pair<Integer, PhysEntityTrackData.CreateData> createData = this.createdTrackData.remove();
         this.trackData.put((Integer)createData.getFirst(), PhysEntityTrackData.from((PhysEntityTrackData.CreateData)createData.getSecond()));
      }

      this.trackUpdateData.forEach((id, data) -> {
         PhysEntityTrackData old = this.trackData.get(id);
         if (old != null) {
            this.trackData.put(id, old.updateWith(data));
         }
      });
      this.trackUpdateData.clear();

      while (!this.removedTracks.isEmpty()) {
         Integer removeId = this.removedTracks.remove();
         this.trackData.remove(removeId);
      }

      if (!this.trackData.isEmpty()) {
         Vector3d netLinearForce = new Vector3d(0.0);
         double coefficientOfPower = Math.min(1.0, 4.0 / (double)this.trackData.size());
         this.trackData.forEach((id, data) -> {
            PhysShip wheel = (PhysShip)lookupPhysShip.invoke(data.shiptraptionID);
            Pair<Vector3dc, Vector3dc> forces = this.computeForce(data, (PhysShipImpl)physShip, (PhysShipImpl)wheel, coefficientOfPower);
            if (forces != null) {
               netLinearForce.add((Vector3dc)forces.getFirst());
               if (((Vector3dc)forces.getSecond()).isFinite()) {
                  wheel.applyInvariantTorque((Vector3dc)forces.getSecond());
               }
            }
         });
         if (netLinearForce.isFinite()) {
            physShip.applyInvariantForce(netLinearForce);
         }
      }
   }

   private Pair<Vector3dc, Vector3dc> computeForce(PhysEntityTrackData data, PhysShipImpl ship, PhysShipImpl wheel, double coefficientOfPower) {
      if (wheel != null) {
         double m = ship.getInertia().getShipMass();
         ShipTransform shipTransform = ship.getTransform();
         Vector3dc wheelAxis = shipTransform.getShipToWorldRotation().transform(data.wheelAxis, new Vector3d());
         double wheelSpeed = wheel.getPoseVel().getOmega().dot(wheelAxis);
         double slip = Math.clamp(-3.0, 3.0, -Math.abs(data.trackRPM) - wheelSpeed);
         Vector3dc driveTorque = wheelAxis.mul(slip * m * 0.4 * coefficientOfPower, new Vector3d());
         return new Pair(new Vector3d(0.0), driveTorque);
      } else {
         return null;
      }
   }

   public final int addTrackBlock(PhysEntityTrackData.CreateData data) {
      this.createdTrackData.add(new Pair(++this.nextBearingID, data));
      return this.nextBearingID;
   }

   public final void updateTrackBlock(Integer id, PhysEntityTrackData.UpdateData data) {
      this.trackUpdateData.put(id, data);
   }

   public final void removeTrackBlock(ServerWorld level, int id) {
      PhysEntityTrackData data = this.trackData.get(id);
      if (data != null) {
         VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(data.springId);
         VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(data.axleId);
      }

      this.removedTracks.add(id);
   }

   public final void resetController() {
      for (int i = 0; i < this.nextBearingID; i++) {
         this.removedTracks.add(i);
      }

      this.nextBearingID = 0;
   }

   public static <T> boolean areQueuesEqual(Queue<T> left, Queue<T> right) {
      return Arrays.equals(left.toArray(), right.toArray());
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof PhysicsEntityTrackController otherController)
                 ? false
                 : Objects.equals(this.trackData, otherController.trackData)
                 && Objects.equals(this.trackUpdateData, otherController.trackUpdateData)
                 && areQueuesEqual(this.createdTrackData, otherController.createdTrackData)
                 && areQueuesEqual(this.removedTracks, otherController.removedTracks)
                 && this.nextBearingID == otherController.nextBearingID;
      }
   }
}