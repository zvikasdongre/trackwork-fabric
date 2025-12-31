package edn.stratodonut.trackwork.tracks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import edn.stratodonut.trackwork.client.TrackworkPartialModels;
import edn.stratodonut.trackwork.tracks.blocks.OleoWheelBlock;
import edn.stratodonut.trackwork.tracks.blocks.OleoWheelBlock.VisualVariant;
import edn.stratodonut.trackwork.tracks.blocks.OleoWheelBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

public class OleoWheelRenderer extends SafeBlockEntityRenderer<OleoWheelBlockEntity> {
    public OleoWheelRenderer(BlockEntityRendererProvider.Context context) {
        // DO NOTHING
    }

    @Override
    protected void renderSafe(OleoWheelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = be.getBlockState();
        float angleForBE = be.getFreeWheelAngle(partialTicks);
        Direction trackDir = state.getValue(OleoWheelBlock.AXLE_FACING);

        float axisMult = (trackDir.getAxis() == Direction.Axis.X) ? 1 : -1;

        float horizontalOffset = be.getPointHorizontalOffset();
        float axialOffset = be.getPointAxialOffset();

        boolean springFlip = trackDir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        if (!springFlip)
            angleForBE *= -1;
        else {
            horizontalOffset *= -1;
            axialOffset *= -1;
        }

        float wheelTravel = be.getWheelTravel(partialTicks) - be.getWheelRadius();

        Vector3fc strutDir = state.getValue(OleoWheelBlock.STRUT_FACING).step().negate();
        Vector3fc axleDir = state.getValue(OleoWheelBlock.AXLE_FACING).step();
        Vector3fc crossDir = strutDir.cross(axleDir, new Vector3f());

        Quaternionf quat = new Quaternionf()
                .setFromNormalized(new Matrix3f(crossDir, strutDir, axleDir)).normalize();

        VisualVariant v = state.getValue(OleoWheelBlock.VISUAL_VARIANT);

        float strutOffset = 0;
        if (v == VisualVariant.single) {
            strutOffset = 6/16f;
        }

        SuperByteBuffer strut_upper = CachedBuffers.partial(TrackworkPartialModels.OLEO_STRUT_UPPER, state);
        strut_upper.center()
                .rotate(quat)
                .translate(horizontalOffset * -axisMult, 0, axialOffset + strutOffset)
                .uncenter();
        strut_upper.light(light)
                .renderInto(ms, bufferSource.getBuffer(RenderType.cutout()));

        SuperByteBuffer strut_lower = CachedBuffers.partial(TrackworkPartialModels.OLEO_STRUT_LOWER, state);
        strut_lower.center()
                .rotate(quat)
                .translate(horizontalOffset * -axisMult, -wheelTravel - 0.5 + 0.5, axialOffset + strutOffset)
                .uncenter();
        strut_lower.light(light)
                .renderInto(ms, bufferSource.getBuffer(RenderType.cutout()));

        SuperByteBuffer wheels = CachedBuffers.partial(
                (v == VisualVariant.single) ? TrackworkPartialModels.OLEO_WHEEL_SINGLE : TrackworkPartialModels.OLEO_WHEEL_TWIN,
                state
        );
        wheels.center()
                .rotate(quat)
                .translate(horizontalOffset * -axisMult, -wheelTravel - 0.5, axialOffset)
                .rotateYDegrees(be.getSteeringValue() * 30)
                .rotateZDegrees(-angleForBE)
                .uncenter();

        wheels.light(light)
                .renderInto(ms, bufferSource.getBuffer(RenderType.cutout()));
    }
}
