package net.zvikasdongre.trackwork.blocks;

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
        return !axis.isHorizontal();
    }

    public boolean shouldCheckWeakPower(BlockState state, WorldView world, BlockPos pos, Direction side) {
        return false;
    }

//    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
//        if ((Boolean)TrackworkConfigs.server().enableTrackThrow.get()) {
//            this.withBlockEntityDo(level, pos, be -> be.throwTrack(false));
//        }
//
//        super.onBlockExploded(state, level, pos, explosion);
//    }

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
        Axis placedAxis = context.getSide().getAxis();
        Axis axis = context.getPlayer() != null && context.getPlayer().isSneaking() ? placedAxis : getPreferredAxis(context);
        if (axis == null) {
            axis = placedAxis;
        }

        if (axis == Axis.Y) {
            axis = Axis.X;
        }

        BlockState state = (BlockState) this.getDefaultState().with(AXIS, axis);

        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() != axis) {
                BlockPos pos = context.getBlockPos();
                BlockPos offset = pos.offset(facing);
                state = this.getStateForNeighborUpdate(state, facing, context.getWorld().getBlockState(offset), context.getWorld(), pos, offset);
            }
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
        TrackPart part = (TrackPart)stateIn.get(PART);
        Axis axis = (Axis)stateIn.get(AXIS);
        boolean connectionAlongFirst = (Boolean)stateIn.get(CONNECTED_ALONG_FIRST_COORDINATE);
        Axis connectionAxis = connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);
        Axis faceAxis = face.getAxis();
        boolean facingAlongFirst = axis == Axis.X ? faceAxis.isHorizontal() : faceAxis == Axis.X;
        boolean positive = face.getDirection() == AxisDirection.POSITIVE;
        if (axis == faceAxis) {
            return stateIn;
        } else if (!(neighbour.getBlock() instanceof TrackBaseBlock)) {
            if (facingAlongFirst != connectionAlongFirst || part == TrackPart.NONE) {
                return stateIn;
            } else if (part == TrackPart.MIDDLE) {
                return (BlockState)stateIn.with(PART, positive ? TrackPart.END : TrackPart.START);
            } else {
                return part == TrackPart.START == positive ? (BlockState)stateIn.with(PART, TrackPart.NONE) : stateIn;
            }
        } else {
            TrackPart otherPart = (TrackPart)neighbour.get(PART);
            Axis otherAxis = (Axis)neighbour.get(AXIS);
            boolean otherConnection = (Boolean)neighbour.get(CONNECTED_ALONG_FIRST_COORDINATE);
            Axis otherConnectionAxis = otherConnection ? (otherAxis == Axis.X ? Axis.Y : Axis.X) : (otherAxis == Axis.Z ? Axis.Y : Axis.Z);
            if (neighbour.get(AXIS) == faceAxis) {
                return stateIn;
            } else if (otherPart != TrackPart.NONE && otherConnectionAxis != faceAxis) {
                return stateIn;
            } else {
                if (part == TrackPart.NONE) {
                    part = positive ? TrackPart.START : TrackPart.END;
                    connectionAlongFirst = axis == Axis.X ? faceAxis.isHorizontal() : faceAxis == Axis.X;
                } else if (connectionAxis != faceAxis) {
                    return stateIn;
                }

                if (part == TrackPart.START != positive) {
                    part = TrackPart.MIDDLE;
                }

                return (BlockState)((BlockState)stateIn.with(PART, part)).with(CONNECTED_ALONG_FIRST_COORDINATE, connectionAlongFirst);
            }
        }
    }

    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.get(PART) == TrackPart.NONE
                ? super.getRotatedBlockState(originalState, targetedFace)
                : super.getRotatedBlockState(originalState, Direction.get(AxisDirection.POSITIVE, getConnectionAxis(originalState)));
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == (Axis) state.get(AXIS);
    }

    public static boolean areBlocksConnected(BlockState state, BlockState other, Direction facing) {
        TrackPart part = (TrackPart)state.get(PART);
        Axis connectionAxis = getConnectionAxis(state);
        Axis otherConnectionAxis = getConnectionAxis(other);
        if (otherConnectionAxis != connectionAxis) {
            return false;
        } else if (facing.getAxis() != connectionAxis) {
            return false;
        } else {
            return facing.getDirection() != AxisDirection.POSITIVE || part != TrackPart.MIDDLE && part != TrackPart.START
                    ? facing.getDirection() == AxisDirection.NEGATIVE && (part == TrackPart.MIDDLE || part == TrackPart.END)
                    : true;
        }
    }

    protected static Axis getConnectionAxis(BlockState state) {
        Axis axis = (Axis)state.get(AXIS);
        boolean connectionAlongFirst = (Boolean)state.get(CONNECTED_ALONG_FIRST_COORDINATE);
        return connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);
    }

    public BlockState m_6843_(BlockState state, BlockRotation rot) {
        return this.rotate(state, rot, Axis.Y);
    }

    protected BlockState rotate(BlockState pState, BlockRotation rot, Axis rotAxis) {
        Axis connectionAxis = getConnectionAxis(pState);
        Direction direction = Direction.from(connectionAxis, AxisDirection.POSITIVE);
        Direction normal = Direction.from((Axis)pState.get(AXIS), AxisDirection.POSITIVE);

        for (int i = 0; i < rot.ordinal(); i++) {
            direction = direction.rotateClockwise(rotAxis);
            normal = normal.rotateClockwise(rotAxis);
        }

        if (direction.getDirection() == AxisDirection.NEGATIVE) {
            pState = this.reversePart(pState);
        }

        Axis newAxis = normal.getAxis();
        Axis newConnectingDirection = direction.getAxis();
        boolean alongFirst = newAxis == Axis.X && newConnectingDirection == Axis.Y || newAxis != Axis.X && newConnectingDirection == Axis.X;
        return (BlockState)((BlockState) pState.with(AXIS, newAxis)).with(CONNECTED_ALONG_FIRST_COORDINATE, alongFirst);
    }

    @NotNull
    public BlockState mirror(@NotNull BlockState pState, BlockMirror pMirror) {
        Axis connectionAxis = getConnectionAxis(pState);
        return pMirror.apply(Direction.from(connectionAxis, AxisDirection.POSITIVE)).getDirection() == AxisDirection.POSITIVE
                ? pState
                : this.reversePart(pState);
    }

    protected BlockState reversePart(BlockState pState) {
        TrackPart part = (TrackPart)pState.get(PART);
        if (part == TrackPart.START) {
            return (BlockState)pState.with(PART, TrackPart.END);
        } else {
            return part == TrackPart.END ? (BlockState)pState.with(PART, TrackPart.START) : pState;
        }
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
