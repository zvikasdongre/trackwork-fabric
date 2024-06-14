package net.zvikasdongre.trackwork.blocks;


import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
//import net.zvikasdongre.trackwork_fabric.network.ThrowTrackPacket;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.rendering.TrackBeltRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public abstract class TrackBaseBlockEntity extends KineticBlockEntity implements ITrackPointProvider {
    private boolean detracked = false;
    protected Pair<Float, Float> nextPointVerticalOffset = new Pair(0.0F, 0.0F);
    protected float nextPointHorizontalOffset = 0.0F;
    @NotNull
    private ITrackPointProvider.PointType nextPoint = ITrackPointProvider.PointType.NONE;

    public TrackBaseBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void tick() {
        if (this.world.getBlockEntity(this.getPos().offset(TrackBeltRenderer.getAlong(this.getCachedState()))) instanceof ITrackPointProvider track) {
            this.nextPointVerticalOffset = new Pair(track.getPointDownwardOffset(0.0F), track.getPointDownwardOffset(1.0F));
            this.nextPointHorizontalOffset = track.getPointHorizontalOffset();
            this.nextPoint = track.getTrackPointType();
        } else {
            this.nextPoint = ITrackPointProvider.PointType.NONE;
        }
    }

    @NotNull
    @Override
    public ITrackPointProvider.PointType getNextPoint() {
        return this.nextPoint;
    }

    public void throwTrack(boolean fixTrack) {
//        World world = this.world;
//        if (!world.isClient && this.detracked == fixTrack) {
//            this.detracked = !fixTrack;
//            this.speed = 0.0F;
//            BlockPos pos = this.getPos();
//
//            for (boolean forward : Iterate.trueAndFalse) {
//                BlockPos currentPos = this.nextTrackPosition(this.getCachedState(), pos, forward);
//                if (currentPos != null && world.getBlockEntity(currentPos) instanceof TrackBaseBlockEntity track_base_be) {
//                    track_base_be.throwTrack(fixTrack);
//                }
//            }
//
//            TrackPackets.getChannel().send(this.packetTarget(), new ThrowTrackPacket(this.getPos(), this.detracked));
//        }
    }

    @Nullable
    private BlockPos nextTrackPosition(BlockState state, BlockPos pos, boolean forward) {
        TrackBaseBlock.TrackPart part = state.get(TrackBaseBlock.PART);
        Direction next = Direction.get(Direction.AxisDirection.POSITIVE, around((Axis) state.get(RotatedPillarKineticBlock.AXIS)));
        int offset = forward ? 1 : -1;
        return (part != TrackBaseBlock.TrackPart.END || !forward) && (part != TrackBaseBlock.TrackPart.START || forward) ? pos.offset(next, offset) : null;
    }

    private static Axis around(Axis axis) {
        if (axis.isHorizontal()) {
            return axis;
        } else {
            return axis == Axis.X ? Axis.Z : Axis.X;
        }
    }

    protected static Vec3d getActionNormal(Axis axis) {
        return switch (axis) {
            case X -> new Vec3d(0.0, -1.0, 0.0);
            case Y -> new Vec3d(0.0, 0.0, 0.0);
            case Z -> new Vec3d(0.0, -1.0, 0.0);
            default -> throw new IncompatibleClassChangeError();
        };
    }

    protected static Vector3d getAxisAsVec(Axis axis) {
        return switch (axis) {
            case X -> new Vector3d(1.0, 0.0, 0.0);
            case Y -> new Vector3d(0.0, 1.0, 0.0);
            case Z -> new Vector3d(0.0, 0.0, 1.0);
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public static Vector3d getActionVec3d(Axis axis, float length) {
        return switch (axis) {
            case X -> new Vector3d(0.0, 0.0, (double)length);
            case Y -> new Vector3d(0.0, 0.0, 0.0);
            case Z -> new Vector3d((double)length, 0.0, 0.0);
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public boolean isDetracked() {
        return this.detracked;
    }

    public void write(NbtCompound compound, boolean clientPacket) {
        compound.putBoolean("Detracked", this.detracked);
        super.write(compound, clientPacket);
    }

    protected void read(NbtCompound compound, boolean clientPacket) {
        if (compound.getBoolean("Detracked")) {
            this.detracked = compound.getBoolean("Detracked");
        }

        super.read(compound, clientPacket);
    }

//    public void handlePacket(ThrowTrackPacket packet) {
//        this.detracked = packet.detracked;
//        if (this.detracked) {
//            this.speed = 0.0F;
//        }
//    }
}
