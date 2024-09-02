package net.zvikasdongre.trackwork.blocks.suspension.variants;

import net.minecraft.block.entity.BlockEntityType;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;

public class MedSuspensionTrackBlock extends SuspensionTrackBlock {
    public MedSuspensionTrackBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<SuspensionTrackBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.MED_SUSPENSION_TRACK.get();
    }
}
