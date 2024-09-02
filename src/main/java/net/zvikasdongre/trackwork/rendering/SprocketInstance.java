package net.zvikasdongre.trackwork.rendering;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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

      gantryCogs = getTransformMaterial()
              .getModel(TrackworkPartialModels.COGS, blockState)
              .createInstance();

      axis = blockState.get(SuspensionTrackBlock.AXIS);
//        alongFirst = blockState.get(SuspensionTrackBlock.AXIS_ALONG_FIRST_COORDINATE);
      rotationAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);

      if (axis == Direction.Axis.X)
         rotationMult = -1;
      else {
         rotationMult = 1;
      }

      visualPos = blockEntity.getPos();

      animateCogs(getCogAngle());
   }

   @Override
   public void beginFrame() {
      float cogAngle = this.getCogAngle();
      if (MathHelper.approximatelyEquals(cogAngle, lastAngle)) return;
      this.animateCogs(cogAngle);
   }

   private float getCogAngle() {
      return SprocketRenderer.getAngleForBE(blockEntity, visualPos, rotationAxis) * rotationMult;
   }

   private void animateCogs(float cogAngle) {
      gantryCogs.loadIdentity()
              .translate(getInstancePosition())
              .centre()
              .rotateY(axis == Direction.Axis.X ? 0 : 90)
              .rotateX(-cogAngle * rotationMult)
              .translate(0, 9 / 16f, 0)
              .unCentre();
   }

   @Override
   public void updateLight() {
      relight(pos, gantryCogs, rotatingModel);
   }

   @Override
   public void remove() {
      super.remove();
      gantryCogs.delete();
   }
}
