package net.zvikasdongre.trackwork.mixin;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {RotationPropagator.class},
   remap = false
)
public abstract class MixinRotationPropagator {
   @Inject(
      method = {"getRotationSpeedModifier(Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;)F"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private static void mixinGetRotationSpeedModifier(KineticBlockEntity from, KineticBlockEntity to, CallbackInfoReturnable<Float> cir) {
      BlockState stateFrom = from.getCachedState();
      BlockState stateTo = to.getCachedState();
      Block fromBlock = stateFrom.getBlock();
      Block toBlock = stateTo.getBlock();
      BlockPos diff = to.getPos().subtract(from.getPos());
      Direction direction = Direction.getFacing((float)diff.getX(), (float)diff.getY(), (float)diff.getZ());
      if (fromBlock instanceof TrackBaseBlock<?> && toBlock instanceof TrackBaseBlock) {
         boolean connected = TrackBaseBlock.areBlocksConnected(stateFrom, stateTo, direction) && clockworkdev2$areTracksConnected(from, to);
         cir.setReturnValue(connected ? 1.0F : 0.0F);
      }
   }

   @Unique
   private static boolean clockworkdev2$areTracksConnected(KineticBlockEntity from, KineticBlockEntity to) {
      if (from instanceof TrackBaseBlockEntity te1 && to instanceof TrackBaseBlockEntity te2) {
         return !te1.isDetracked() && !te2.isDetracked();
      }

      return false;
   }
}
