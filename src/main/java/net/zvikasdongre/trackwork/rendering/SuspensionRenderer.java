package net.zvikasdongre.trackwork.rendering;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.TrackworkPartialModels;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;

public class SuspensionRenderer  extends KineticBlockEntityRenderer<SuspensionTrackBlockEntity> {

        public SuspensionRenderer(Context context) {
            super(context);
        }

        @Override
        protected void renderSafe(SuspensionTrackBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
            BlockState state = be.getCachedState();
            Axis rotationAxis = getRotationAxisOf(be);
            BlockPos visualPos = be.getPos();
            float angleForBE = getAngleForBE(be, visualPos, rotationAxis);
            Axis trackAxis = state.get(TrackBaseBlock.AXIS);
            if (trackAxis == Axis.X) {
                angleForBE *= -1.0F;
            }

            float yRot = trackAxis == Axis.X ? 0.0F : 90.0F;
            if (state.contains(SuspensionTrackBlock.WHEEL_VARIANT) && state.get(SuspensionTrackBlock.WHEEL_VARIANT) != SuspensionTrackBlock.TrackVariant.BLANK) {
                SuperByteBuffer wheels = be.getWheelRadius() < 0.6F
                        ? CachedBufferer.partial(TrackworkPartialModels.SUSPENSION_WHEEL, state)
                        : (be.getWheelRadius() > 0.8F
                        ? CachedBufferer.partial(TrackworkPartialModels.LARGE_SUSPENSION_WHEEL, state)
                        : CachedBufferer.partial(TrackworkPartialModels.MED_SUSPENSION_WHEEL, state));

                wheels.centre()
                        .rotateY(yRot)
                        .translate(0.0, be.getWheelRadius() - 0.5, 0.0)
                        .translate(0.0, -be.getWheelTravel(partialTicks), be.getPointHorizontalOffset())
                        .rotateX(-angleForBE)
                        .translate(0.0, 0.5625, 0.0)
                        .unCentre();

                wheels.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
            }

            if (be.assembled) {
                TrackBeltRenderer.renderBelt(
                        be,
                        partialTicks,
                        ms,
                        buffer,
                        light,
                        new TrackBeltRenderer.ScalableScroll(be, (float)(be.getSpeed() * (be.getWheelRadius() / 0.5)), trackAxis)
                );
            }
        }

        public static float getAngleForBE(KineticBlockEntity be, BlockPos pos, Axis axis) {
            float time = AnimationTickHolder.getRenderTime(be.getWorld());
            float offset = getRotationOffsetForPosition(be, pos, axis);
            return (time * be.getSpeed() * 3.0F / 10.0F + offset) % 360.0F;
        }

        @Override
        protected BlockState getRenderedBlockState(SuspensionTrackBlockEntity be) {
            return shaft(getRotationAxisOf(be));
        }

        @Override
        public int getRenderDistance() {
            return TrackworkConfigs.trackRenderDist.get();
        }
}
