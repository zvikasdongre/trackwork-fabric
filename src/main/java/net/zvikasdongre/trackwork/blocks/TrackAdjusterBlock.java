package net.zvikasdongre.trackwork.blocks;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;

public class TrackAdjusterBlock extends RotatedPillarKineticBlock implements IBE<TrackAdjusterBlockEntity> {
    public TrackAdjusterBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    @Override
    public Class<TrackAdjusterBlockEntity> getBlockEntityClass() {
        return TrackAdjusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrackAdjusterBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.TRACK_LEVEL_CONTROLLER.get();
    }
}
