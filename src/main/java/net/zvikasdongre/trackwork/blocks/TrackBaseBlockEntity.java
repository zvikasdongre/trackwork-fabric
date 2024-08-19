package net.zvikasdongre.trackwork.blocks;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.Iterate;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.zvikasdongre.trackwork.networking.TrackworkPackets;
import net.zvikasdongre.trackwork.rendering.TrackBeltRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;


import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static net.zvikasdongre.trackwork.blocks.TrackBaseBlock.PART;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock.TrackPart;

public abstract class TrackBaseBlockEntity extends KineticBlockEntity implements ITrackPointProvider {
    private boolean detracked = false;
    protected Pair<Float, Float> nextPointVerticalOffset = new Pair(0.0F, 0.0F);
    protected float nextPointHorizontalOffset = 0.0F;
    @NotNull
    private ITrackPointProvider.PointType nextPoint = ITrackPointProvider.PointType.NONE;

    public TrackBaseBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        BlockEntity next = this.world.getBlockEntity(this.getPos().offset(TrackBeltRenderer.getAlong(this.getCachedState())));
        if (next instanceof ITrackPointProvider track) {
            this.nextPointVerticalOffset = new Pair<>(track.getPointDownwardOffset(0), track.getPointDownwardOffset(1));
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
        World world = this.world;
        if (!world.isClient && this.detracked == fixTrack) {
            this.detracked = !fixTrack;
            this.speed = 0.0F;
            BlockPos pos = this.getPos();

            for (boolean forward : Iterate.trueAndFalse) {
                BlockPos currentPos = this.nextTrackPosition(this.getCachedState(), pos, forward);
                if (currentPos != null && world.getBlockEntity(currentPos) instanceof TrackBaseBlockEntity track_base_be) {
                    track_base_be.throwTrack(fixTrack);
                }
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(this.getPos());
            buf.writeBoolean(this.detracked);

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, this.getPos())) {
                ServerPlayNetworking.send(player, TrackworkPackets.THROW_TRACK_PACKET_ID, buf);
            }
        }
    }

    @Nullable
    private BlockPos nextTrackPosition(BlockState state, BlockPos pos, boolean forward) {
        TrackPart part = state.get(PART);
        Direction next = Direction.get(Direction.AxisDirection.POSITIVE, around(state.get(AXIS)));

        int offset = forward ? 1 : -1;
        if (part == TrackPart.END && forward || part == TrackPart.START && !forward)
            return null;
        pos = pos.offset(next, offset);
        return pos;
    }

    private static Axis around(Axis axis) {
        if (axis.isVertical()) return axis;
        return (axis == Direction.Axis.X) ? Direction.Axis.Z : Direction.Axis.X;
    }

    protected static Vec3i getActionNormal(Axis axis) {
        return switch (axis) {
            case X -> new Vec3i(0, -1, 0);
            case Y -> new Vec3i(0,0, 0);
            case Z -> new Vec3i(0, -1, 0);
        };
    }

    protected static Vector3d getAxisAsVec(Axis axis) {
        return switch (axis) {
            case X -> new Vector3d(1, 0, 0);
            case Y -> new Vector3d(0,1, 0);
            case Z -> new Vector3d(0, 0, 1);
        };
    }

    public static Vector3d getActionVec3d(Axis axis, float length) {
        return switch (axis) {
            case X -> new Vector3d(0, 0, length);
            case Y -> new Vector3d(0,0, 0);
            case Z -> new Vector3d(length, 0, 0);
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
        if (compound.contains("Detracked")) {
            this.detracked = compound.getBoolean("Detracked");
        }

        super.read(compound, clientPacket);
    }

    public void handlePacket(boolean detracked) {
        this.detracked = detracked;
        if (this.detracked) {
            this.speed = 0.0F;
        }
    }
}
