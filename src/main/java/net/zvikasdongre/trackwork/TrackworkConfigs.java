package net.zvikasdongre.trackwork;
import net.minecraftforge.common.ForgeConfigSpec;

public class TrackworkConfigs {
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue enableStress = SERVER_BUILDER
            .comment("Enable track Kinetic Stress")
            .define("enableTrackStress", false);

    public static final ForgeConfigSpec.DoubleValue stressMult = SERVER_BUILDER
            .comment("Stress multiplier, units SU/(ton x RPM)")
            .defineInRange("stressMultiplier", 1/50f, 0.0f, 1);
    public static final ForgeConfigSpec.IntValue maxRPM = SERVER_BUILDER
            .comment("Maximum Track RPM, 1 RPM ~ 0.104 m/s")
            .defineInRange("maxTrackRPM", 256, 1,1024);
    public static final ForgeConfigSpec.BooleanValue enableTrackThrow = SERVER_BUILDER
            .comment("Enable entire tracks being thrown off by explosions")
            .define("enableTrackThrow", false);

    public static final ForgeConfigSpec.IntValue trackRenderDist = CLIENT_BUILDER
            .comment("Track render distance")
            .defineInRange("trackRenderDist", 256, 64,1024);

    static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();
    static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

}

