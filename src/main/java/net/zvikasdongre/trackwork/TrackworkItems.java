package net.zvikasdongre.trackwork;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.zvikasdongre.trackwork.items.ControllerResetStick;
import net.zvikasdongre.trackwork.items.TrackToolkit;
import net.zvikasdongre.trackwork.items.TrackToolkitRenderer;

public class TrackworkItems {
    public static final ItemEntry<TrackToolkit> TRACK_TOOL_KIT =
            Trackwork.REGISTRATE.item("track_tool_kit", TrackToolkit::new)
                    .properties(p -> p.maxCount(1))
                    .transform(CreateRegistrate.customRenderedItem(() -> TrackToolkitRenderer::new))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();

    public static final ItemEntry<ControllerResetStick> CONTROL_RESET_STICK =
            Trackwork.REGISTRATE.item("dev_reset_stick", ControllerResetStick::new)
                    .properties(p -> p.maxCount(1))
                    .register();

    public static void initialize() {}
}
