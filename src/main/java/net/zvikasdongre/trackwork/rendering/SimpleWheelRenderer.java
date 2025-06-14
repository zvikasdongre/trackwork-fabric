package net.zvikasdongre.trackwork.rendering;

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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlock;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlockEntity;
import net.zvikasdongre.trackwork.client.TrackworkPartialModels;

import java.util.function.BiConsumer;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class SimpleWheelRenderer extends KineticBlockEntityRenderer<WheelBlockEntity> {

    public SimpleWheelRenderer(Context context) {
        super(context);
    }

    public static void ribTransform(SuperByteBuffer rib, float yRot, float wheelTravel, MatrixStack ms, VertexConsumerProvider buffer, int light,
                                    Vec3d offset, boolean flip) {
        rib.centre()
                .rotateY(-yRot)
                .translate(offset.x, -8 / 16f - offset.y, 1.5 + offset.z)
                .rotateX(-Math.toDegrees(Math.atan((wheelTravel + 0.3f) / 1.1)))
                .translate(0, 8 / 16f + offset.y, -1.375)
                .unCentre();
        rib.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    public static float getAngleForBE(WheelBlockEntity be, final BlockPos pos, Direction.Axis axis, float partialTick) {
        if (be.isFreespin) return be.getFreeWheelAngle(partialTick);
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return (time * be.getWheelSpeed() * 3f / 10 + offset) % 360;
    }

    @Override
    protected void renderSafe(WheelBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
                              int light, int overlay) {
//        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

//        if (Backend.canUseInstancing(be.getLevel())) return;

        BlockState state = be.getCachedState();
        Direction.Axis rotationAxis = getRotationAxisOf(be);
        BlockPos visualPos = be.getPos();
        float angleForBE = SimpleWheelRenderer.getAngleForBE(be, visualPos, rotationAxis, partialTicks);
//
        Direction trackDir = state.get(HORIZONTAL_FACING);

        float axisMult = -1;
        if (trackDir.getAxis() == Direction.Axis.X) {
            axisMult = 1;
        }

        float horizontalOffset = be.getPointHorizontalOffset();
        float axialOffset = be.getPointAxialOffset();

        boolean springFlip = trackDir.getDirection() == Direction.AxisDirection.NEGATIVE;
        if (!springFlip)
            angleForBE *= -1;
        else {
            horizontalOffset *= -1;
            axialOffset *= -1;
        }

        float yRot = state.get(HORIZONTAL_FACING).asRotation();
//        float yRot = (trackAxis == Direction.Axis.X) ? 0 : 90;
        float wheelTravel = be.getWheelTravel(partialTicks) - be.getWheelRadius();
        double wheelTuck = Math.sqrt(2.25 - Math.min(1, wheelTravel * wheelTravel)) - 1.5 - axialOffset;

        ribTransform(CachedBufferer.partial(TrackworkPartialModels.SIMPLE_WHEEL_RIB, state), yRot, wheelTravel, ms, buffer, light,
                new Vec3d(horizontalOffset * -axisMult, 0, axialOffset), springFlip);
        ribTransform(CachedBufferer.partial(TrackworkPartialModels.SIMPLE_WHEEL_RIB_UPPER, state), yRot, wheelTravel, ms, buffer, light,
                new Vec3d(horizontalOffset * -axisMult, -9 / 16f, axialOffset), false);

//        19.5
        final BiConsumer<SuperByteBuffer, Float> springTransform = (spring, angle) ->
                spring
                        .translate(0, 14 / 16f, 6 / 16f)
                        .translate(0, -7 / 16f, 12 / 16f)
                        .rotateX(angle)
                        .translate(0, -7 / 16f, -12 / 16f);

        float springBaseLength = 4 / 16f;
        float springEndPointLength = 12 / 16f;
        double diagonalLength = Math.sqrt(springEndPointLength * springEndPointLength + springBaseLength * springBaseLength);
        double diagonalRibOffset = -Math.atan(springBaseLength);
        double diagonalSpringOffset = (Math.PI / 2 - Math.abs(diagonalRibOffset));

        double ribAngle = Math.atan((wheelTravel + 0.3f) / 1.1);
        double scaleLength = Math.sqrt(springEndPointLength * springEndPointLength + diagonalLength * diagonalLength
                - 2 * springEndPointLength * diagonalLength * Math.cos(Math.PI / 2 + ribAngle - diagonalRibOffset));

        if (state.contains(WheelBlock.VISUAL_VARIANT)
                && state.get(WheelBlock.VISUAL_VARIANT) != WheelBlock.VisualVariant.NO_SPRING) {
            float springAngle = (float) (Math.toDegrees(Math.asin((springEndPointLength * Math.sin(Math.PI / 2 + ribAngle
                    - diagonalRibOffset)) / scaleLength) + diagonalSpringOffset - Math.PI / 2));

            SuperByteBuffer springBase = CachedBufferer.partial(TrackworkPartialModels.SIMPLE_WHEEL_SPRING_BASE, state);
            springBase.centre().rotateY(-yRot);
            springTransform.accept(springBase, 0f);
            springBase.translate((springFlip ? 12 / 16f : 0) + horizontalOffset * -axisMult, 0, axialOffset)
                    .unCentre();
            springBase.renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

//        float springScale = (0.6f * wheelTravel + 1f) / (13.5f/16);
            SuperByteBuffer springCoil = CachedBufferer.partial(TrackworkPartialModels.SIMPLE_WHEEL_SPRING_COIL, state);
            springCoil.centre().rotateY(-yRot);
            springTransform.accept(springCoil, springAngle);
            springCoil
                    .translate((springFlip ? 12 / 16f : 0) + horizontalOffset * -axisMult, 7 / 16f, axialOffset)
                    .scale(1, (float) scaleLength / (17f / 16), 1)
                    .translate(0, -7 / 16f, 0)
                    .unCentre();
            springCoil.renderInto(ms, buffer.getBuffer(RenderLayer.getCutout()));
        }

        {
            SuperByteBuffer wheels = be.getWheelRadius() > 0.8f ?
                    CachedBufferer.partial(TrackworkPartialModels.SIMPLE_WHEEL, state) :
                    CachedBufferer.partial(TrackworkPartialModels.MED_SIMPLE_WHEEL, state);
            wheels.centre()
                    .rotateY(-yRot + be.getSteeringValue() * 30)
//                    .translate(0, be.getWheelRadius() , 0)
                    .translate(horizontalOffset * -axisMult, -wheelTravel - 0.5, -wheelTuck)
                    .rotateZ(-angleForBE)
//                    .translate(0, 8 / 16f, 0)
                    .unCentre();

            wheels.light(light)
                    .renderInto(ms, buffer.getBuffer(RenderLayer.getCutout()));
        }
    }

    @Override
    protected BlockState getRenderedBlockState(WheelBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    @Override
    public int getRenderDistance() {
        return TrackworkConfigs.trackRenderDist.get();
    }

}
