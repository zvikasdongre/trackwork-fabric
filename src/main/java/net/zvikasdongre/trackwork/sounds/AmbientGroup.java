package net.zvikasdongre.trackwork.sounds;

import java.util.function.BiFunction;

public class AmbientGroup {
    private final BiFunction<Float, AmbientGroup, TrackSoundScape> factory;

    public AmbientGroup(BiFunction<Float, AmbientGroup, TrackSoundScape> factory) {
        this.factory = factory;
    }

    public TrackSoundScape instantiate(float pitch) {
        return factory.apply(pitch, this);
    }
}