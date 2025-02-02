package edn.stratodonut.trackwork;

import edn.stratodonut.trackwork.sounds.AmbientGroup;
import edn.stratodonut.trackwork.sounds.TrackSoundScape;

public class TrackAmbientGroups {
    public static AmbientGroup TRACK_GROUND_AMBIENT = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .repeating(TrackSounds.TRACK_AMBIENT_GROUND_1.get(), 1.545f, .75f, 1)
            .repeating(TrackSounds.TRACK_AMBIENT_GROUND_2.get(), 0.425f, .75f, 2)
    );

    public static AmbientGroup TRACK_SPROCKET_AMBIENT = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .continuous(TrackSounds.TRACK_AMBIENT_SPROCKET.get(), 1.5f, 1)
    );

    public static AmbientGroup WHEEL_GROUND_AMBIENT = new AmbientGroup((p, g) -> new TrackSoundScape(p, g)
            .repeating(TrackSounds.WHEEL_AMBIENT_GROUND_1.get(), 1.545f, .75f, 1)
            .repeating(TrackSounds.WHEEL_AMBIENT_GROUND_2.get(), 0.425f, .75f, 2)
    );
}
