package net.zvikasdongre.trackwork.blocks.wheel;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import org.jetbrains.annotations.NotNull;

public class WheelBlock extends HorizontalKineticBlock implements IBE<WheelBlockEntity> {
    public WheelBlock(Settings properties) {
        super(properties);
    }

    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isSneaking()) || preferred == null) {
            return getDefaultState().with(HORIZONTAL_FACING, context.getHorizontalPlayerFacing());
        }
        return getDefaultState().with(HORIZONTAL_FACING, preferred);
    }

    public static boolean isValid(Direction facing) {
        return !facing.getAxis().isVertical();
    }

    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.BLOCK;
    }


    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(HORIZONTAL_FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<WheelBlockEntity> getBlockEntityClass() {
        return WheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<WheelBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.SIMPLE_WHEEL.get();
    }
}
