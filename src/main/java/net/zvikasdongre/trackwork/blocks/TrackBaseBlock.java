package net.zvikasdongre.trackwork.blocks;

import com.simibubi.create.content.contraptions.ITransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.chainDrive.ChainGearshiftBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.content.contraptions.ITransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.blocks.ITrackPointProvider;
import org.jetbrains.annotations.NotNull;
import net.minecraft.state.StateManager.Builder;

public abstract class TrackBaseBlock<BE extends TrackBaseBlockEntity> extends RotatedPillarKineticBlock implements ITransformableBlock, IBE<BE> {
    public static final EnumProperty<TrackPart> PART = EnumProperty.of("part", TrackPart.class);
    public static final BooleanProperty CONNECTED_ALONG_FIRST_COORDINATE = DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

    public TrackBaseBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return (Axis) state.get(AXIS);
    }

    @Override
    public Class<BE> getBlockEntityClass() {
        return null;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public BlockEntityType<? extends BE> getBlockEntityType() {
        return null;
    }

    public static boolean isValidAxis(Axis axis) {
        return !axis.isVertical();
    }

    public boolean shouldCheckWeakPower(BlockState state, WorldView world, BlockPos pos, Direction side) {
        return false;
    }

    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if ((Boolean) TrackworkConfigs.enableTrackThrow.get()) {
            this.withBlockEntityDo(world, pos, be -> be.throwTrack(false));
        }

        super.onDestroyedByExplosion(world, pos, explosion);
    }

    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onBlockAdded(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @NotNull
    public PistonBehavior pistonBehavior(@NotNull BlockState state) {
        return PistonBehavior.BLOCK;
    }

    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(PART);
        builder.add(CONNECTED_ALONG_FIRST_COORDINATE);
        super.appendProperties(builder);
    }

    public static void updateTrackSystem(BlockPos pos) {
    }

    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction.Axis placedAxis = context.getPlayerLookDirection().getAxis();
        Direction.Axis axis = context.getPlayer() != null && context.getPlayer()
                .isSneaking() ? placedAxis : getPreferredAxis(context);
        if (axis == null)
            axis = placedAxis;
        if (axis == Direction.Axis.Y) {
            axis = Direction.Axis.X;
        }

        BlockState state = getDefaultState().with(AXIS, axis);
        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == axis)
                continue;
            BlockPos pos = context.getBlockPos();
            BlockPos offset = pos.offset(facing);
            state = getStateForNeighborUpdate(state, facing, context.getWorld()
                    .getBlockState(offset), context.getWorld(), pos, offset);
        }
        return state;
    }


    @NotNull
    public BlockState getStateForNeighborUpdate(
            BlockState stateIn,
            Direction face,
            @NotNull BlockState neighbour,
            @NotNull WorldAccess worldIn,
            @NotNull BlockPos currentPos,
            @NotNull BlockPos facingPos
    ) {
        TrackPart part = stateIn.get(PART);
        Direction.Axis axis = stateIn.get(AXIS);
        boolean connectionAlongFirst = stateIn.get(CONNECTED_ALONG_FIRST_COORDINATE);
        Direction.Axis connectionAxis =
                connectionAlongFirst ? (axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X) : (axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z);

        Direction.Axis faceAxis = face.getAxis();
        boolean facingAlongFirst = axis == Direction.Axis.X ? faceAxis.isVertical() : faceAxis == Direction.Axis.X;
        boolean positive = face.getDirection() == Direction.AxisDirection.POSITIVE;

        if (axis == faceAxis)
            return stateIn;

        if (!(neighbour.getBlock() instanceof TrackBaseBlock)) {
            if (facingAlongFirst != connectionAlongFirst || part == TrackPart.NONE)
                return stateIn;
            if (part == TrackPart.MIDDLE)
                return stateIn.with(PART, positive ? TrackPart.END : TrackPart.START);
            if ((part == TrackPart.START) == positive)
                return stateIn.with(PART, TrackPart.NONE);
            return stateIn;
        }

        TrackPart otherPart = neighbour.get(PART);
        Direction.Axis otherAxis = neighbour.get(AXIS);
        boolean otherConnection = neighbour.get(CONNECTED_ALONG_FIRST_COORDINATE);
        Direction.Axis otherConnectionAxis =
                otherConnection ? (otherAxis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X) : (otherAxis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z);

        if (neighbour.get(AXIS) == faceAxis)
            return stateIn;
        if (otherPart != TrackPart.NONE && otherConnectionAxis != faceAxis)
            return stateIn;

        if (part == TrackPart.NONE) {
            part = positive ? TrackPart.START : TrackPart.END;
            connectionAlongFirst = axis == Direction.Axis.X ? faceAxis.isVertical() : faceAxis == Direction.Axis.X;
        } else if (connectionAxis != faceAxis) {
            return stateIn;
        }

        if ((part == TrackPart.START) != positive)
            part = TrackPart.MIDDLE;

        return stateIn.with(PART, part)
                .with(CONNECTED_ALONG_FIRST_COORDINATE, connectionAlongFirst);
    }

    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (originalState.get(PART) == TrackPart.NONE)
            return super.getRotatedBlockState(originalState, targetedFace);
        return super.getRotatedBlockState(originalState,
                Direction.get(Direction.AxisDirection.POSITIVE, getConnectionAxis(originalState)));
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    public static boolean areBlocksConnected(BlockState state, BlockState other, Direction facing) {
        TrackPart part = state.get(PART);
        Direction.Axis connectionAxis = getConnectionAxis(state);
        Direction.Axis otherConnectionAxis = getConnectionAxis(other);

        if (otherConnectionAxis != connectionAxis)
            return false;
        if (facing.getAxis() != connectionAxis)
            return false;
        if (facing.getDirection() == Direction.AxisDirection.POSITIVE && (part == TrackPart.MIDDLE || part == TrackPart.START))
            return true;
        if (facing.getDirection() == Direction.AxisDirection.NEGATIVE && (part == TrackPart.MIDDLE || part == TrackPart.END))
            return true;

        return false;
    }

    protected static Axis getConnectionAxis(BlockState state) {
        Direction.Axis axis = state.get(AXIS);
        boolean connectionAlongFirst = state.get(CONNECTED_ALONG_FIRST_COORDINATE);
        return connectionAlongFirst ? (axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X) : (axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z);
    }
    
    public BlockState m_6843_(BlockState state, BlockRotation rot) {
        return this.rotate(state, rot, Direction.Axis.Y);
    }

    protected BlockState rotate(BlockState pState, BlockRotation rot, Axis rotAxis) {
        Direction.Axis connectionAxis = getConnectionAxis(pState);
        Direction direction = Direction.from(connectionAxis, Direction.AxisDirection.POSITIVE);
        Direction normal = Direction.from(pState.get(AXIS), Direction.AxisDirection.POSITIVE);
        for (int i = 0; i < rot.ordinal(); i++) {
            direction = direction.rotateClockwise(rotAxis);
            normal = normal.rotateClockwise(rotAxis);
        }

        if (direction.getDirection() == Direction.AxisDirection.NEGATIVE)
            pState = reversePart(pState);

        Direction.Axis newAxis = normal.getAxis();
        Direction.Axis newConnectingDirection = direction.getAxis();
        boolean alongFirst = newAxis == Direction.Axis.X && newConnectingDirection == Direction.Axis.Y
                || newAxis != Direction.Axis.X && newConnectingDirection == Direction.Axis.X;

        return pState.with(AXIS, newAxis)
                .with(CONNECTED_ALONG_FIRST_COORDINATE, alongFirst);
    }

    @NotNull
    public BlockState mirror(@NotNull BlockState pState, BlockMirror pMirror) {
        Direction.Axis connectionAxis = getConnectionAxis(pState);
        if (pMirror.apply(Direction.from(connectionAxis, Direction.AxisDirection.POSITIVE))
                .getDirection() == Direction.AxisDirection.POSITIVE)
            return pState;
        return reversePart(pState);
    }

    protected BlockState reversePart(BlockState pState) {
        TrackPart part = pState.get(PART);
        if (part == TrackPart.START)
            return pState.with(PART, TrackPart.END);
        if (part == TrackPart.END)
            return pState.with(PART, TrackPart.START);
        return pState;
    }

    public BlockState transform(BlockState state, StructureTransform transform) {
        return this.rotate(this.mirror(state, transform.mirror), transform.rotation, transform.rotationAxis);
    }

    public static enum TrackPart implements StringIdentifiable {
        START,
        MIDDLE,
        END,
        NONE;

        @NotNull
        public String asString() {
            return Lang.asId(this.name());
        }
    }
}
