package edn.stratodonut.trackwork.tracks.blocks;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.Iterate;
import edn.stratodonut.trackwork.TrackPackets;
import edn.stratodonut.trackwork.TrackworkUtil;
import edn.stratodonut.trackwork.tracks.ITrackPointProvider;
import edn.stratodonut.trackwork.tracks.blocks.TrackBaseBlock.TrackPart;
import edn.stratodonut.trackwork.tracks.network.ThrowTrackPacket;
import edn.stratodonut.trackwork.tracks.render.TrackBeltRenderer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import javax.annotation.Nullable;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static edn.stratodonut.trackwork.tracks.blocks.SuspensionTrackBlock.WHEEL_VARIANT;
import static edn.stratodonut.trackwork.tracks.blocks.TrackBaseBlock.PART;

public abstract class TrackBaseBlockEntity extends KineticBlockEntity implements ITrackPointProvider {
    private boolean detracked = false;
    public TrackBaseBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        setLazyTickRate(10);
    }

    @Override
    public void tick() {
        super.tick();

        BlockEntity next = this.level.getBlockEntity(this.getBlockPos().relative(TrackBeltRenderer.getAlong(this.getBlockState())));
        if (next instanceof ITrackPointProvider track) {
            this.nextPointVerticalOffset = new Pair<>(track.getPointDownwardOffset(0), track.getPointDownwardOffset(1));
            this.nextPointHorizontalOffset = track.getPointHorizontalOffset();
            this.nextPoint = track.getTrackPointType();
        } else {
            this.nextPoint = ITrackPointProvider.PointType.NONE;
        }
    }

    protected Pair<Float, Float> nextPointVerticalOffset = new Pair<>(0f, 0f);
    protected float nextPointHorizontalOffset = 0.0f;
    private @NotNull ITrackPointProvider.PointType nextPoint = ITrackPointProvider.PointType.NONE;
    @Override
    public @NotNull ITrackPointProvider.PointType getNextPoint() {
        return this.nextPoint;
    }

    public void throwTrack(boolean fixTrack) {
        Level world = this.level;
        if (world.isClientSide || this.detracked == !fixTrack)
            return;

        this.detracked = !fixTrack;
        this.speed = 0.0f;

        BlockPos pos = this.getBlockPos();
        for (boolean forward : Iterate.trueAndFalse) {
            BlockPos currentPos = nextTrackPosition(this.getBlockState(), pos, forward);
            if (currentPos == null)
                continue;
            BlockEntity currentEntity = world.getBlockEntity(currentPos);
            if (currentEntity instanceof TrackBaseBlockEntity te) {
                te.throwTrack(fixTrack);
            }
        }

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(this.getBlockPos());
        buf.writeBoolean(this.detracked);

        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) world, this.getBlockPos())) {
            ServerPlayNetworking.send(player, TrackPackets.THROW_TRACK_PACKET_ID, buf);
        }
    }

    private @Nullable BlockPos nextTrackPosition(BlockState state, BlockPos pos, boolean forward) {
        TrackPart part = state.getValue(PART);
        Direction next = Direction.get(Direction.AxisDirection.POSITIVE, TrackworkUtil.around(state.getValue(AXIS)));

        int offset = forward ? 1 : -1;
        if (part == TrackPart.END && forward || part == TrackPart.START && !forward)
            return null;
        pos = pos.relative(next, offset);
        return pos;
    }

    public boolean isDetracked() {
        return this.detracked;
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Detracked", this.detracked);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        if (compound.contains("Detracked")) this.detracked = compound.getBoolean("Detracked");
        super.read(compound, clientPacket);
    }

    public void handlePacket(boolean detracked) {
        this.detracked = detracked;
        if (this.detracked) this.speed = 0.0f;
    }
}
