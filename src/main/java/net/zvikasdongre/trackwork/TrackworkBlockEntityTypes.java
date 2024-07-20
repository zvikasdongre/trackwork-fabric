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

    public static final BlockEntityEntry<SuspensionTrackBlockEntity> LARGE_SUSPENSION_TRACK = Trackwork.REGISTRATE
            .blockEntity("large_suspension_track", SuspensionTrackBlockEntity::large)
//            .instance(() -> SuspensionInstance::new, false)
            .validBlocks(TrackworkBlocks.LARGE_SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SuspensionTrackBlockEntity> MED_SUSPENSION_TRACK = Trackwork.REGISTRATE
            .blockEntity("med_suspension_track", SuspensionTrackBlockEntity::med)
//            .instance(() -> SuspensionInstance::new, false)
            .validBlocks(TrackworkBlocks.MED_SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SprocketBlockEntity> SPROCKET_TRACK = Trackwork.REGISTRATE
            .blockEntity("sprocket_track", SprocketBlockEntity::new)
            .instance(() -> SprocketInstance::new)
            .validBlocks(TrackworkBlocks.SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();
    public static final BlockEntityEntry<SprocketBlockEntity> LARGE_SPROCKET_TRACK = Trackwork.REGISTRATE
            .blockEntity("large_sprocket_track", SprocketBlockEntity::large)
            .instance(() -> SprocketInstance::new)
            .validBlocks(TrackworkBlocks.LARGE_SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();
    public static final BlockEntityEntry<SprocketBlockEntity> MED_SPROCKET_TRACK = Trackwork.REGISTRATE
            .blockEntity("med_sprocket_track", SprocketBlockEntity::med)
            .instance(() -> SprocketInstance::new)
            .validBlocks(TrackworkBlocks.MED_SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();

    public static void initialize() {};
}
