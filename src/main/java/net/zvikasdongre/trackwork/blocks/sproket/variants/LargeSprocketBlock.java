package net.zvikasdongre.trackwork.blocks.sproket.variants;

import net.minecraft.block.entity.BlockEntityType;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlock;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlockEntity;

public class LargeSprocketBlock extends SprocketBlock {
    public LargeSprocketBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<SprocketBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.LARGE_SPROCKET_TRACK.get();
    }
}
