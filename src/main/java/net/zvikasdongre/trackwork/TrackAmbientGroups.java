package net.zvikasdongre.trackwork;

import net.zvikasdongre.trackwork.sounds.AmbientGroup;
import net.zvikasdongre.trackwork.sounds.TrackSoundScape;

public class TrackAmbientGroups {
    public static AmbientGroup TRACK_GROUND_AMBIENT = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .repeating(TrackworkSounds.TRACK_AMBIENT_GROUND_1, 1.545f, .75f, 1)
            .repeating(TrackworkSounds.TRACK_AMBIENT_GROUND_2, 0.425f, .75f, 2)
            .withArgMax(32)
    );

    public static AmbientGroup TRACK_SPROCKET_AMBIENT = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .continuous(TrackworkSounds.TRACK_AMBIENT_SPROCKET, 1.5f, 1)
    );

    public static AmbientGroup TRACK_GROUND_SLIP = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .continuous(TrackworkSounds.TRACK_GROUND_SLIP, 1f, 1)
    );

    public static AmbientGroup WHEEL_GROUND_AMBIENT = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .repeating(TrackworkSounds.WHEEL_AMBIENT_GROUND_1, 0.72f, .75f, 1)
            .repeating(TrackworkSounds.WHEEL_AMBIENT_GROUND_2, 0.215f, .75f, 2)
    );

    public static AmbientGroup WHEEL_GROUND_SLIP = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .continuous(TrackworkSounds.WHEEL_GROUND_SLIP, 1f, 1)
    );
}