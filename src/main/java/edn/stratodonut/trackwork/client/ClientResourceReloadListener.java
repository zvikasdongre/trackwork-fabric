package edn.stratodonut.trackwork.client;

import edn.stratodonut.trackwork.TrackworkMod;
import edn.stratodonut.trackwork.sounds.TrackSoundScapes;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        TrackSoundScapes.invalidateAll();
    }

    @Override
    public ResourceLocation getFabricId() {
        return TrackworkMod.getResource("trackwork_resource_reload_listener");
    }
}