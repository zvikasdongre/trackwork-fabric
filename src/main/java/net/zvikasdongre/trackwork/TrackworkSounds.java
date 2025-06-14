package net.zvikasdongre.trackwork;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class TrackworkSounds {

    private TrackworkSounds() {
    }

    public static final SoundEvent SUSPENSION_CREAK = registerSound("suspension_creak");
    public static final SoundEvent POWER_TOOL = registerSound("power_wrench");
    public static final SoundEvent SPRING_TOOL = registerSound("spring_tool");

    public static final SoundEvent TRACK_AMBIENT_SPROCKET = registerSound("track_ambient_sprocket");

    public static final SoundEvent TRACK_AMBIENT_GROUND_1 = registerSound("track_ambient_ground_1");
    public static final SoundEvent TRACK_AMBIENT_GROUND_2 = registerSound("track_ambient_ground_2");
    public static final SoundEvent TRACK_GROUND_SLIP = registerSound("track_ground_slip");

    public static final SoundEvent WHEEL_ROCKTOSS = registerSound("wheel_rocktoss");

    public static final SoundEvent WHEEL_AMBIENT_GROUND_1 = registerSound("wheel_ambient_ground_1");
    public static final SoundEvent WHEEL_AMBIENT_GROUND_2 = registerSound("wheel_ambient_ground_2");
    public static final SoundEvent WHEEL_GROUND_SLIP = registerSound("wheel_ground_slip");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = new Identifier(Trackwork.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void initialize() {
    }
}