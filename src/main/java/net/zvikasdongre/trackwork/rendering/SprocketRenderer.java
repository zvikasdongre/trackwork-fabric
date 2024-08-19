package net.zvikasdongre.trackwork.rendering;

import com.jozufozu.flywheel.backend.Backend;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.TrackworkPartialModels;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;

public class SprocketRenderer extends KineticBlockEntityRenderer<SprocketBlockEntity> {

    public SprocketRenderer(Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SprocketBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState state = be.getCachedState();
        Axis rotationAxis = getRotationAxisOf(be);
        BlockPos visualPos = be.getPos();
        float angleForBE = getAngleForBE(be, visualPos, rotationAxis);
        Axis trackAxis = (Axis) state.get(TrackBaseBlock.AXIS);
        if (trackAxis == Axis.X) {
            angleForBE *= -1.0F;
        }

        SuperByteBuffer cogs = be.getWheelRadius() < 0.6f ?
                CachedBufferer.partial(TrackworkPartialModels.COGS, state) :
                be.getWheelRadius() > 0.8f ? CachedBufferer.partial(TrackworkPartialModels.LARGE_COGS, state) :
                        CachedBufferer.partial(TrackworkPartialModels.MED_COGS, state);

        cogs.centre()
                .rotateY(trackAxis == Direction.Axis.X ? 0 : 90)
                .rotateX(-angleForBE)
//                .scale(1, be.getWheelRadius() / 0.5f, be.getWheelRadius() / 0.5f)
                .translate(0, 9 / 16f, 0)
                .unCentre();


        cogs.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

        if (be.assembled) {
            TrackBeltRenderer.renderBelt(
                    be,
                    partialTicks,
                    ms,
                    buffer,
                    light,
                    new TrackBeltRenderer.ScalableScroll(be, (float) ((double) be.getSpeed() * ((double) be.getWheelRadius() / 0.5)), trackAxis)
            );
        }
    }

    public static float getAngleForBE(KineticBlockEntity be, BlockPos pos, Direction.Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        float offset = getRotationOffsetForPosition(be, pos, axis);

        return (time * be.getSpeed() * 3.0F / 10.0F + offset) % 360.0F;
    }

    protected BlockState getRenderedBlockState(SprocketBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public int getRenderDistance() {
        return TrackworkConfigs.trackRenderDist.get();
    }
}
