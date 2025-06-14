package net.zvikasdongre.trackwork.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceType;
import net.zvikasdongre.trackwork.sounds.TrackSoundScapes;

import static com.jozufozu.flywheel.backend.Backend.isGameActive;

public class TrackworkClientEvents {
    public static final IdentifiableResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

    public static void init() {
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isGameActive())
                return;

            ClientWorld world = client.world;
            if (world == null)
                return;

            TrackSoundScapes.tick();
        });

        // Register resource reload listener
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(RESOURCE_RELOAD_LISTENER);
    }
}