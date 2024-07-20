package net.zvikasdongre.trackwork.blocks.suspension.variants;

import net.minecraft.block.entity.BlockEntityType;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;

public class LargeSuspensionTrackBlock extends SuspensionTrackBlock {

    public LargeSuspensionTrackBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends SuspensionTrackBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.LARGE_SUSPENSION_TRACK.get();
    }
}
