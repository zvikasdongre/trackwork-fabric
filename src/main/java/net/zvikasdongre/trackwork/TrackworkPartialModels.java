package net.zvikasdongre.trackwork;

import com.jozufozu.flywheel.core.PartialModel;

public class TrackworkPartialModels {
//    public static final PartialModel SUSPENSION_WHEEL = block("wheels");
//    public static final PartialModel MED_SUSPENSION_WHEEL = block("med_wheels");
//    public static final PartialModel LARGE_SUSPENSION_WHEEL = block("large_wheels");
    public static final PartialModel COGS = block("cogs");
    public static final PartialModel MED_COGS = block("med_cogs");
    public static final PartialModel LARGE_COGS = block("large_cogs");
    public static final PartialModel TRACK_LINK = block("track_link");
    public static final PartialModel TRACK_WRAP = block("wrapped_link");

    public static final PartialModel SUSPENSION_WHEEL = block("wheels");
    public static final PartialModel MED_SUSPENSION_WHEEL = block("med_wheels");
    public static final PartialModel LARGE_SUSPENSION_WHEEL = block("large_wheels");

    private static PartialModel block(String path) {
        return new PartialModel(Trackwork.getResource("block/" + path));
    }

    public static void init() {
    }
}
