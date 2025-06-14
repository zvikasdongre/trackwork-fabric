package net.zvikasdongre.trackwork.client;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.sounds.TrackSoundScapes;

public class ClientResourceReloadListener implements SynchronousResourceReloader, IdentifiableResourceReloadListener {
    @Override
    public void reload(ResourceManager manager) {
        TrackSoundScapes.invalidateAll();
    }

    @Override
    public Identifier getFabricId() {
        return Trackwork.getResource("trackwork_resource_reload_listener");
    }
}
