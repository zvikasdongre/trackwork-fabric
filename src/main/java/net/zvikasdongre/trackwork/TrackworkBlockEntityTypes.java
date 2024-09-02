package net.zvikasdongre.trackwork;

import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.zvikasdongre.trackwork.blocks.TrackAdjusterBlockEntity;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlockEntity;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlockEntity;
import net.zvikasdongre.trackwork.rendering.SimpleWheelRenderer;
import net.zvikasdongre.trackwork.rendering.SprocketInstance;
import net.zvikasdongre.trackwork.rendering.SprocketRenderer;
import net.zvikasdongre.trackwork.rendering.SuspensionRenderer;

import static net.zvikasdongre.trackwork.Trackwork.REGISTRATE;

public class TrackworkBlockEntityTypes {
    public static final BlockEntityEntry<SuspensionTrackBlockEntity> SUSPENSION_TRACK = REGISTRATE
            .blockEntity("suspension_track", SuspensionTrackBlockEntity::new)
            .validBlocks(TrackworkBlocks.SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SuspensionTrackBlockEntity> LARGE_SUSPENSION_TRACK = REGISTRATE
            .blockEntity("large_suspension_track", SuspensionTrackBlockEntity::large)
//            .instance(() -> SuspensionInstance::new, false)
            .validBlocks(TrackworkBlocks.LARGE_SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SuspensionTrackBlockEntity> MED_SUSPENSION_TRACK = REGISTRATE
            .blockEntity("med_suspension_track", SuspensionTrackBlockEntity::med)
//            .instance(() -> SuspensionInstance::new, false)
            .validBlocks(TrackworkBlocks.MED_SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SprocketBlockEntity> SPROCKET_TRACK = REGISTRATE
            .blockEntity("sprocket_track", SprocketBlockEntity::new)
            .instance(() -> ShaftInstance::new)
            .validBlocks(TrackworkBlocks.SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();
    public static final BlockEntityEntry<SprocketBlockEntity> LARGE_SPROCKET_TRACK = REGISTRATE
            .blockEntity("large_sprocket_track", SprocketBlockEntity::large)
            .instance(() -> ShaftInstance::new)
            .validBlocks(TrackworkBlocks.LARGE_SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();
    public static final BlockEntityEntry<SprocketBlockEntity> MED_SPROCKET_TRACK = REGISTRATE
            .blockEntity("med_sprocket_track", SprocketBlockEntity::med)
            .instance(() -> ShaftInstance::new)
            .validBlocks(TrackworkBlocks.MED_SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();

    public static final BlockEntityEntry<TrackAdjusterBlockEntity> TRACK_LEVEL_CONTROLLER = REGISTRATE
            .blockEntity("track_level_controller", TrackAdjusterBlockEntity::new)
            .instance(() -> ShaftInstance::new)
            .validBlocks(TrackworkBlocks.TRACK_LEVEL_CONTROLLER)
            .renderer(() -> ShaftRenderer::new)
            .register();


    public static final BlockEntityEntry<WheelBlockEntity> SIMPLE_WHEEL = REGISTRATE
            .blockEntity("simple_wheel", WheelBlockEntity::new)
//            .instance(() -> PhysEntityTrackInstance::new, false)
            .validBlocks(TrackworkBlocks.SIMPLE_WHEEL)
            .renderer(() -> SimpleWheelRenderer::new)
            .register();

    public static void initialize() {};
}
