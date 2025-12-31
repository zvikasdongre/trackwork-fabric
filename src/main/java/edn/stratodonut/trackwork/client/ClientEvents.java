package edn.stratodonut.trackwork.client;

import edn.stratodonut.trackwork.sounds.TrackSoundScapes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.PackType;

import dev.engine_room.flywheel.api.backend.BackendManager;

public class ClientEvents {
    public static final IdentifiableResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

    public static void init() {
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!BackendManager.isBackendOn())
                return;

            ClientLevel world = client.level;
            if (world == null)
                return;

            TrackSoundScapes.tick();
        });

        // Register resource reload listener
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(RESOURCE_RELOAD_LISTENER);
    }

}
