package net.zvikasdongre.trackwork.rendering;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.TrackworkPartialModels;
import net.zvikasdongre.trackwork.TrackworkSpriteShifts;
import net.zvikasdongre.trackwork.blocks.ITrackPointProvider;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlockEntity;

public class TrackBeltRenderer {
    public static void renderBelt(TrackBaseBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer,
                                  int light, ScalableScroll scroll) {

//        if (Backend.canUseInstancing(be.getWorld())) return;
        if (be.isDetracked()) return;
        BlockState state = be.getCachedState();
        float yRot = getYRotFromState(state);
        renderLink(be, partialTicks, state, yRot, light, ms, buffer, scroll);
    }

    private static void renderLink(TrackBaseBlockEntity fromTrack, float partialTicks,
                                   BlockState state, float yRot, int light, MatrixStack ms, VertexConsumerProvider buf,
                                   ScalableScroll scroll) {
        boolean isLarge = fromTrack.isBeltLarge();
        float largeScale = isLarge ? 2 : 2*fromTrack.getWheelRadius();
        SuperByteBuffer topLink;
        TrackBaseBlock.TrackPart part = state.get(TrackBaseBlock.PART);
        if (part == TrackBaseBlock.TrackPart.MIDDLE) {
            topLink = getLink(state);
            topLink.centre()
                    .rotateY(yRot)
                    .rotateX(180)
                    .translate(0, (-0.5)*(17/16f)*largeScale, -0.5)
                    .scale(1, largeScale, 1)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(1.0f))
                    .unCentre();
            topLink.light(light).renderInto(ms, buf.getBuffer(RenderLayer.getSolid()));

            SuperByteBuffer flatlink = getLink(state);
            flatlink.centre()
                    .rotateY(yRot)
                    .translate(0, -0.5f, -0.25)
                    .translate(0, -fromTrack.getPointDownwardOffset(partialTicks), fromTrack.getPointHorizontalOffset())
                    .scale(1, largeScale, 0.5f)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(1.0f))
                    .unCentre();
            flatlink.light(light).renderInto(ms, buf.getBuffer(RenderLayer.getSolid()));
        } else if (fromTrack.getTrackPointType() == ITrackPointProvider.PointType.WRAP) {
            float flip = (part == TrackBaseBlock.TrackPart.END) ? -1 : 1;
            topLink = getLink(state);
            topLink.centre()
                    .rotateY(yRot)
                    .rotateX(180)
                    .translate(0, (-0.5)*(17/16f)*largeScale, -0.5)
                    .scale(1, largeScale, 12/16f + (largeScale/16f))
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(flip))
                    .unCentre();
            topLink.light(light).renderInto(ms, buf.getBuffer(RenderLayer.getSolid()));

            SuperByteBuffer wrapLink = CachedBufferer.partial(TrackworkPartialModels.TRACK_WRAP, state);
            wrapLink.centre()
                    .rotateY(yRot)
                    .scale(1, largeScale,largeScale)
                    .translate(0, (0.5) + 1/16f, fromTrack.getWheelRadius() > 0.667 ? 0.5/16f : -1/16f)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(flip))
                    .unCentre();
            wrapLink.light(light).renderInto(ms, buf.getBuffer(RenderLayer.getSolid()));
        }

        if (fromTrack.getNextPoint() != ITrackPointProvider.PointType.NONE) {
            Vec3d offset = fromTrack.getTrackPointSlope(partialTicks);
            float opposite = (float) offset.y;
            float adjacent = 1 + (float) offset.z;
            // Slope Link
            SuperByteBuffer link = getLink(state);
            if (fromTrack.getNextPoint() == fromTrack.getTrackPointType()) {
                // Middle
                float cut_adjacent = 8/16f + (float) offset.z;
                float length = (float) Math.sqrt(opposite*opposite + cut_adjacent*cut_adjacent);
                float angleOffset = (float) (Math.atan2(opposite, cut_adjacent));
                link.centre()
                        .rotateY(yRot)
                        .translate(0, -0.5f, 4 / 16f)
                        .translate(0, -fromTrack.getPointDownwardOffset(partialTicks), fromTrack.getPointHorizontalOffset())
                        .rotateX(angleOffset * 180f / Math.PI)
                        .scale(1, largeScale, length)
                        .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(1.0f)) // passing length causes a lot of unintended scroll
                        .unCentre();
                link.light(light).renderInto(ms, buf.getBuffer(RenderLayer.getSolid()));
            } else {
                // Ends
                float length = (float) Math.sqrt(opposite*opposite + adjacent*adjacent + (isLarge ? 4/16f : 0));
                float flip = (part == TrackBaseBlock.TrackPart.START) ? -1 : 1;
                float angleOffset = (float) (Math.atan2(opposite, adjacent + (isLarge ? 2/16f : 0)));
                link.centre()
                        .rotateY(yRot)
                        .translate(0, -0.5f, (5 / 16f + ((isLarge && fromTrack.getTrackPointType() == ITrackPointProvider.PointType.WRAP) ? 2/16f : 0)) * flip)
                        .translate(0, -fromTrack.getPointDownwardOffset(partialTicks), fromTrack.getPointHorizontalOffset())
                        .rotateX(angleOffset * 180f / Math.PI)
                        .scale(1, largeScale, length)
                        .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(1)) // passing length causes a lot of unintended scroll
                        .unCentre();
                link.light(light).renderInto(ms, buf.getBuffer(RenderLayer.getSolid()));
            }
        }
    }

    public static class ScalableScroll {
        private final float trueSpeed;
        private final float time;
        private final float spriteSize;
        private final float scrollMult;

        public ScalableScroll(KineticBlockEntity be, final float speed, Direction.Axis axis) {
            this.trueSpeed = (axis == Direction.Axis.X) ? speed : -speed;
            this.time = AnimationTickHolder.getRenderTime(be.getWorld()) * 1;

            this.scrollMult = 0.5f;
            SpriteShiftEntry spriteShift = TrackworkSpriteShifts.BELT;
            this.spriteSize = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();
        }

        public float getAtScale(float scale) {
            float speed = this.trueSpeed * scale;

            if (speed != 0) {
                double scroll = speed * this.time / (31.5 * 16);
                scroll = scroll - Math.floor(scroll);
                scroll = scroll * this.spriteSize * this.scrollMult;

                return (float) scroll;
            }
            return 0;
        }
    }

    private static SuperByteBuffer getLink(BlockState state) {
        return CachedBufferer.partial(TrackworkPartialModels.TRACK_LINK, state);
    }

    public static Direction getAlong(BlockState state) {
        return state.get(RotatedPillarKineticBlock.AXIS) == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
    }

    public static float getYRotFromState(BlockState state) {
        Direction.Axis trackAxis = state.get(RotatedPillarKineticBlock.AXIS);
        boolean flip = state.get(TrackBaseBlock.PART) == TrackBaseBlock.TrackPart.END;
        return ((trackAxis == Direction.Axis.X) ? 0 : 90) + (flip ? 180 : 0);
    }
}
