package net.zvikasdongre.trackwork;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class TrackworkSounds {

    private TrackworkSounds() {}
    
    public static final SoundEvent SUSPENSION_CREAK = registerSound("suspension_creak");
    public static final SoundEvent POWER_TOOL = registerSound("power_wrench");
    public static final SoundEvent SPRING_TOOL = registerSound("spring_tool");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = new Identifier(Trackwork.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void initialize() {
    }
}