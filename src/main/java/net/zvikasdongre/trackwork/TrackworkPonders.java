package net.zvikasdongre.trackwork;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;

public class TrackworkPonders {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(Trackwork.MOD_ID);

    public static final boolean REGISTER_DEBUG_SCENES = false;

    public static void init() {
        HELPER.forComponents(TrackworkBlocks.SPROCKET_TRACK, TrackworkBlocks.SUSPENSION_TRACK,
                        TrackworkBlocks.LARGE_SPROCKET_TRACK, TrackworkBlocks.LARGE_SUSPENSION_TRACK,
                        TrackworkBlocks.MED_SPROCKET_TRACK, TrackworkBlocks.MED_SUSPENSION_TRACK)
                .addStoryBoard("tracks", TrackworkPonderScenes::trackTutorial);

        HELPER.forComponents(TrackworkBlocks.SIMPLE_WHEEL)
                .addStoryBoard("wheels", TrackworkPonderScenes::wheelTutorial);
    }
}
