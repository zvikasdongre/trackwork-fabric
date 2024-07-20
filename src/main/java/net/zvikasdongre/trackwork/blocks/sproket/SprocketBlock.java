package net.zvikasdongre.trackwork.blocks.sproket;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;

public class SprocketBlock extends TrackBaseBlock<SprocketBlockEntity> {
    public SprocketBlock(Settings properties) {
        super(properties);
    }

    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onBlockAdded(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    public Class<SprocketBlockEntity> getBlockEntityClass() {
        return SprocketBlockEntity.class;
    }

    public BlockEntityType<? extends SprocketBlockEntity> getBlockEntityType() {
        return (BlockEntityType<? extends SprocketBlockEntity>) TrackworkBlockEntityTypes.SPROCKET_TRACK.get();
    }
}