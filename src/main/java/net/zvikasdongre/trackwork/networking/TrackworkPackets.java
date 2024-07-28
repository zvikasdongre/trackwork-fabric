package net.zvikasdongre.trackwork.networking;

import net.minecraft.util.Identifier;
import net.zvikasdongre.trackwork.Trackwork;

public class TrackworkPackets {
    public static final Identifier SUSPENSION_PACKET_ID = new Identifier(Trackwork.MOD_ID, "suspension_track");
    public static final Identifier WHEEL_PACKET_ID = new Identifier(Trackwork.MOD_ID, "simple_wheel");
    public static final Identifier THROW_TRACK_PACKET_ID = new Identifier(Trackwork.MOD_ID, "throw_track");

    public static void registerS2CPackets() {

    }

    public static void registerC2SPackets() {}
}
