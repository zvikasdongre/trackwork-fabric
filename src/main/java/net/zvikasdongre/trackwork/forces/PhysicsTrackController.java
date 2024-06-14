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
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.data.PhysTrackData;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.physics_api.PoseVel;

@JsonAutoDetect(
   fieldVisibility = Visibility.ANY
)
public class PhysicsTrackController implements ShipForcesInducer {
   @JsonIgnore
   public static final double RPM_TO_RADS = 0.10471975512;
   @JsonIgnore
   public static final double MAXIMUM_SLIP = 3.0;
   public static final Vector3dc UP = new Vector3d(0.0, 1.0, 0.0);
   private final HashMap<Integer, PhysTrackData> trackData = new HashMap<>();
   @JsonIgnore
   private final ConcurrentLinkedQueue<Pair<Integer, PhysTrackData.PhysTrackCreateData>> createdTrackData = new ConcurrentLinkedQueue<>();
   @JsonIgnore
   private final ConcurrentHashMap<Integer, PhysTrackData.PhysTrackUpdateData> trackUpdateData = new ConcurrentHashMap<>();
   private final ConcurrentLinkedQueue<Integer> removedTracks = new ConcurrentLinkedQueue<>();
   private int nextBearingID = 0;
   private volatile Vector3dc suspensionAdjust = new Vector3d(0.0, 1.0, 0.0);
   private volatile float suspensionStiffness = 1.0F;
   private float debugTick = 0.0F;

   public static PhysicsTrackController getOrCreate(ServerShip ship) {
      if (ship.getAttachment(PhysicsTrackController.class) == null) {
         ship.saveAttachment(PhysicsTrackController.class, new PhysicsTrackController());
      }

      return (PhysicsTrackController)ship.getAttachment(PhysicsTrackController.class);
   }

   public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

      while (!this.createdTrackData.isEmpty()) {
         Pair<Integer, PhysTrackData.PhysTrackCreateData> createData = this.createdTrackData.remove();
         this.trackData.put((Integer)createData.getFirst(), PhysTrackData.from((PhysTrackData.PhysTrackCreateData)createData.getSecond()));
      }

      this.trackUpdateData.forEach((id, data) -> {
         PhysTrackData old = this.trackData.get(id);
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
         Vector3d netTorque = new Vector3d(0.0);
         double coefficientOfPower = Math.min(2.0, 14.0 / (double)this.trackData.size());
         this.trackData.forEach((id, data) -> {
            Pair<Vector3dc, Vector3dc> forces = this.computeForce(data, (PhysShipImpl)physShip, coefficientOfPower, lookupPhysShip);
            if (forces != null) {
               netLinearForce.add((Vector3dc)forces.getFirst());
               netTorque.add((Vector3dc)forces.getSecond());
            }
         });
         if (netLinearForce.isFinite()) {
            physShip.applyInvariantForce(netLinearForce);
         }

         if (netTorque.isFinite()) {
            physShip.applyInvariantTorque(netTorque);
         }
      }
   }

   public void applyForces(@NotNull PhysShip physShip) {
   }

   private Pair<Vector3dc, Vector3dc> computeForce(
      PhysTrackData data, PhysShipImpl ship, double coefficientOfPower, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip
   ) {
      PoseVel pose = ship.getPoseVel();
      ShipTransform shipTransform = ship.getTransform();
      double m = ship.getInertia().getShipMass();
      Vector3dc trackRelPosShip = data.trackOriginPosition.sub(shipTransform.getPositionInShip(), new Vector3d());
      Vector3dc tForce = data.trackSpeed;
      Vector3dc trackNormal = data.trackNormal.normalize(new Vector3d());
      Vector3dc trackSurface = data.trackSpeed.mul((double)data.trackRPM * 0.10471975512 * 0.5, new Vector3d());
      Vector3dc velocityAtPosition = accumulatedVelocity(shipTransform, pose, data.trackContactPosition);
      if (data.istrackGrounded && data.groundShipId != null) {
         PhysShipImpl ground = (PhysShipImpl)lookupPhysShip.invoke(data.groundShipId);
         Vector3dc groundShipVelocity = accumulatedVelocity(ground.getTransform(), ground.getPoseVel(), data.trackContactPosition);
         velocityAtPosition = velocityAtPosition.sub(groundShipVelocity, new Vector3d());
      }

      if (data.istrackGrounded) {
         double suspensionDelta = velocityAtPosition.dot(trackNormal);
         double tilt = 1.0 + this.tilt(trackRelPosShip);
         tForce = tForce.add(
            data.suspensionCompression.mul(m * 1.0 * coefficientOfPower * (double)this.suspensionStiffness * tilt, new Vector3d()), new Vector3d()
         );
         tForce = tForce.add(
            trackNormal.mul(m * 0.8 * -suspensionDelta * coefficientOfPower * (double)this.suspensionStiffness, new Vector3d()), new Vector3d()
         );
         if (data.trackRPM == 0.0F) {
            tForce = new Vector3d(0.0, tForce.y(), 0.0);
         }
      }

      if (data.istrackGrounded || trackSurface.lengthSquared() > 0.0) {
         Vector3dc surfaceVelocity = velocityAtPosition.sub(trackNormal.mul(velocityAtPosition.dot(trackNormal), new Vector3d()), new Vector3d());
         Vector3dc slipVelocity = trackSurface.sub(surfaceVelocity, new Vector3d());
         tForce = tForce.add(slipVelocity.normalize(Math.min(slipVelocity.length(), 3.0), new Vector3d()).mul(1.0 * m * coefficientOfPower), new Vector3d());
      }

      Vector3dc trackRelPos = shipTransform.getShipToWorldRotation().transform(trackRelPosShip, new Vector3d());
      Vector3dc torque = trackRelPos.cross(tForce, new Vector3d());
      return new Pair(tForce, torque);
   }

   private static Vector3dc accumulatedVelocity(ShipTransform t, PoseVel pose, Vector3dc worldPosition) {
      return pose.getVel().add(pose.getOmega().cross(worldPosition.sub(t.getPositionInWorld(), new Vector3d()), new Vector3d()), new Vector3d());
   }

   public final int addTrackBlock(PhysTrackData.PhysTrackCreateData data) {
      this.createdTrackData.add(new Pair(++this.nextBearingID, data));
      return this.nextBearingID;
   }

   public final double updateTrackBlock(Integer id, PhysTrackData.PhysTrackUpdateData data) {
      this.trackUpdateData.put(id, data);
      return (double)Math.round(this.suspensionAdjust.y() * 16.0) / 16.0 * (double)((9.0F + 1.0F / (this.suspensionStiffness * 2.0F - 1.0F)) / 10.0F);
   }

   public final void removeTrackBlock(int id) {
      this.removedTracks.add(id);
   }

   public final float setDamperCoefficient(float delta) {
      this.suspensionStiffness = Math.clamp(1.0F, 4.0F, this.suspensionStiffness + delta);
      return this.suspensionStiffness;
   }

   public final void adjustSuspension(Vector3f delta) {
      Vector3dc old = this.suspensionAdjust;
      this.suspensionAdjust = new Vector3d(
         Math.clamp(-0.5, 0.5, old.x() + (double)(delta.x() * 5.0F)),
         Math.clamp(0.1, 1.0, old.y() + (double)delta.y()),
         Math.clamp(-0.5, 0.5, old.z() + (double)(delta.z() * 5.0F))
      );
   }

   public final void resetSuspension() {
      double y = this.suspensionAdjust.y();
      this.suspensionAdjust = new Vector3d(0.0, y, 0.0);
   }

   private double tilt(Vector3dc relPos) {
      return Math.signum(relPos.x()) * this.suspensionAdjust.z() + Math.signum(relPos.z()) * this.suspensionAdjust.x();
   }

   public static <T> boolean areQueuesEqual(Queue<T> left, Queue<T> right) {
      return Arrays.equals(left.toArray(), right.toArray());
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof PhysicsTrackController otherController)
            ? false
            : Objects.equals(this.trackData, otherController.trackData)
               && Objects.equals(this.trackUpdateData, otherController.trackUpdateData)
               && areQueuesEqual(this.createdTrackData, otherController.createdTrackData)
               && areQueuesEqual(this.removedTracks, otherController.removedTracks)
               && this.nextBearingID == otherController.nextBearingID;
      }
   }
}
