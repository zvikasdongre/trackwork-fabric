package net.zvikasdongre.trackwork;

import com.jozufozu.flywheel.core.PartialModel;

public class TrackworkPartialModels {
    //    public static final PartialModel SUSPENSION_WHEEL = block("wheels");
//    public static final PartialModel MED_SUSPENSION_WHEEL = block("med_wheels");
//    public static final PartialModel LARGE_SUSPENSION_WHEEL = block("large_wheels");
    public static final PartialModel COGS = block("cogs"),
            MED_COGS = block("med_cogs"),
            LARGE_COGS = block("large_cogs"),
            TRACK_LINK = block("track_link"),
            TRACK_WRAP = block("wrapped_link"),
            SUSPENSION_WHEEL = block("wheels"),
            MED_SUSPENSION_WHEEL = block("med_wheels"),
            LARGE_SUSPENSION_WHEEL = block("large_wheels"),
            SIMPLE_WHEEL = block("simple_wheel"),
            MED_SIMPLE_WHEEL = block("med_simple_wheel"),
            SIMPLE_WHEEL_RIB = block("partial/simple_wheel_rib"),
            SIMPLE_WHEEL_RIB_UPPER = block("partial/simple_wheel_rib_upper"),
            SIMPLE_WHEEL_SPRING_BASE = block("partial/simple_wheel_spring_base"),
            SIMPLE_WHEEL_SPRING_COIL = block("partial/simple_wheel_spring_coil");

    private static PartialModel block(String path) {
        return new PartialModel(Trackwork.getResource("block/" + path));
    }

    public static void init() {
    }
}
