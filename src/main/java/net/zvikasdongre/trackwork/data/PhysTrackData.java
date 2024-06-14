package net.zvikasdongre.trackwork.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

@JsonAutoDetect(
   fieldVisibility = Visibility.ANY
)
public class PhysTrackData {
   public final Vector3dc trackOriginPosition;
   public final Vector3dc trackContactPosition;
   public final Vector3dc trackSpeed;
   public final Vector3dc trackNormal;
   public final Vector3dc suspensionCompression;
   @Nullable
   public Long groundShipId;
   private Vector3dc suspensionCompressionDelta;
   public final boolean istrackGrounded;
   public float trackRPM;
   public float trackSU;

   private PhysTrackData() {
      this(null);
   }

   private PhysTrackData(Vector3dc trackOriginPosition) {
      this.trackOriginPosition = trackOriginPosition;
      this.trackContactPosition = new Vector3d(0.0);
      this.trackSpeed = new Vector3d(0.0);
      this.trackNormal = new Vector3d(0.0, -1.0, 0.0);
      this.suspensionCompression = new Vector3d(0.0);
      this.suspensionCompressionDelta = new Vector3d(0.0);
      this.istrackGrounded = false;
      this.trackRPM = 0.0F;
   }

   public PhysTrackData(
      Vector3dc trackOriginPosition,
      Vector3dc trackContactPosition,
      Vector3dc trackSpeed,
      Vector3dc trackNormal,
      Vector3dc suspensionCompression,
      Vector3dc suspensionCompressionDelta,
      @Nullable Long groundShipId,
      boolean istrackGrounded,
      float trackRPM
   ) {
      this.trackOriginPosition = trackOriginPosition;
      this.trackContactPosition = trackContactPosition;
      this.trackSpeed = trackSpeed;
      this.trackNormal = trackNormal;
      this.suspensionCompression = suspensionCompression;
      this.suspensionCompressionDelta = suspensionCompressionDelta;
      this.groundShipId = groundShipId;
      this.istrackGrounded = istrackGrounded;
      this.trackRPM = trackRPM;
   }

   public final PhysTrackData updateWith(PhysTrackUpdateData update) {
      return new PhysTrackData(
         this.trackOriginPosition,
         update.trackContactPosition,
         update.trackSpeed,
         update.trackNormal,
         update.suspensionCompression,
         update.suspensionCompression.sub(this.suspensionCompression, new Vector3d()).div(20.0, new Vector3d()),
         update.groundShipId,
         update.trackHit,
         update.trackRPM
      );
   }

   public static PhysTrackData from(PhysTrackCreateData data) {
      return new PhysTrackData(data.trackOriginPosition);
   }

   @Nonnull
   public Vector3dc getSuspensionCompressionDelta() {
      return this.suspensionCompressionDelta;
   }

   public void setSuspensionCompressionDelta(Vector3dc suspensionCompressionDelta) {
      if (suspensionCompressionDelta == null) {
         throw new NullPointerException();
      } else {
         this.suspensionCompressionDelta = suspensionCompressionDelta;
      }
   }

   public static record PhysTrackCreateData(Vector3dc trackOriginPosition) {
   }

   public static record PhysTrackUpdateData(
      Vector3dc trackContactPosition,
      Vector3dc trackSpeed,
      Vector3dc trackNormal,
      Vector3dc suspensionCompression,
      Long groundShipId,
      boolean trackHit,
      float trackRPM
   ) {
   }
}
