package net.zvikasdongre.trackwork.rendering;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.TrackworkPartialModels;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlockEntity;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlock;

public class SprocketInstance extends ShaftInstance<SprocketBlockEntity> implements DynamicInstance {
   private final ModelData gantryCogs;
   final Axis axis;
   final Axis rotationAxis;
   final float rotationMult;
   final BlockPos visualPos;
   private float lastAngle = Float.NaN;

   public SprocketInstance(MaterialManager materialManager, SprocketBlockEntity blockEntity) {
      super(materialManager, blockEntity);
      this.gantryCogs = (ModelData)this.getTransformMaterial().getModel(TrackworkPartialModels.COGS, this.blockState).createInstance();
      this.axis = (Axis)this.blockState.get(SuspensionTrackBlock.AXIS);
      this.rotationAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
      if (this.axis == Axis.X) {
         this.rotationMult = -1.0F;
      } else {
         this.rotationMult = 1.0F;
      }

      this.visualPos = blockEntity.getPos();
      this.animateCogs(this.getCogAngle());
   }

   public void beginFrame() {
      float cogAngle = this.getCogAngle();
      if (MathHelper.approximatelyEquals(cogAngle, this.lastAngle)) return;
      this.animateCogs(cogAngle);
   }

   private float getCogAngle() {
      return SprocketRenderer.getAngleForBE(this.blockEntity, this.visualPos, this.rotationAxis) * this.rotationMult;
   }

   private void animateCogs(float cogAngle) {
      gantryCogs.loadIdentity()
              .translate(getInstancePosition())
              .centre()
              .rotateY(this.axis == Axis.X ? 0.0 : 90.0)
              .rotateX(-cogAngle)
              .translate(0.0, 0.5625, 0.0)
              .unCentre();
   }

   @Override
   public void updateLight() {
      relight(pos, gantryCogs, rotatingModel);
   }


   public void remove() {
      super.remove();
      this.gantryCogs.delete();
   }
}
