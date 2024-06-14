package net.zvikasdongre.trackwork;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlockEntity;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import net.zvikasdongre.trackwork.rendering.SprocketInstance;
import net.zvikasdongre.trackwork.rendering.SprocketRenderer;
import net.zvikasdongre.trackwork.rendering.SuspensionRenderer;

public class TrackworkBlockEntityTypes {
    public static final BlockEntityEntry<SuspensionTrackBlockEntity> SUSPENSION_TRACK = Trackwork.REGISTRATE
            .blockEntity("suspension_track", SuspensionTrackBlockEntity::new)
            .validBlocks(TrackworkBlocks.SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SprocketBlockEntity> SPROCKET_TRACK_TYPE = Trackwork.REGISTRATE
            .blockEntity("sprocket_track", SprocketBlockEntity::new)
            .instance(() -> SprocketInstance::new)
            .validBlocks(TrackworkBlocks.SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();

    public static void initialize() {};
}
