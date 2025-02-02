package edn.stratodonut.trackwork.client;

import edn.stratodonut.trackwork.sounds.TrackSoundScapes;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        TrackSoundScapes.invalidateAll();
    }
}
