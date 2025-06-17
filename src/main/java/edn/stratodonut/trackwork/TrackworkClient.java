package edn.stratodonut.trackwork;

import edn.stratodonut.trackwork.client.ClientEvents;
import edn.stratodonut.trackwork.client.TrackworkPartialModels;
import edn.stratodonut.trackwork.client.TrackworkSpriteShifts;
import edn.stratodonut.trackwork.tracks.blocks.SuspensionTrackBlockEntity;
import edn.stratodonut.trackwork.tracks.blocks.TrackBaseBlockEntity;
import edn.stratodonut.trackwork.tracks.blocks.WheelBlockEntity;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.config.ModConfig;

public class TrackworkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrackPonders.register();
        TrackworkPartialModels.init();
        TrackworkSpriteShifts.init();
        ClientEvents.init();
        ForgeConfigRegistry.INSTANCE.register(TrackworkMod.MOD_ID, ModConfig.Type.CLIENT, TrackworkConfigs.CLIENT_SPEC);

        ClientPlayNetworking.registerGlobalReceiver(TrackPackets.SUSPENSION_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            float wheelTravel = buf.readFloat();
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                SuspensionTrackBlockEntity be = (SuspensionTrackBlockEntity) client.level.getBlockEntity(target);
                if (be == null) {
                    return;
                }

                be.handlePacket(wheelTravel);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TrackPackets.WHEEL_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            float wheelTravel = buf.readFloat();
            float steeringValue = buf.readFloat();
            float horizontalOffset = buf.readFloat();
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                WheelBlockEntity be = (WheelBlockEntity) client.level.getBlockEntity(target);
                if (be == null) {
                    return;
                }

                be.handlePacket(wheelTravel, steeringValue, horizontalOffset);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TrackPackets.THROW_TRACK_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            boolean detracked = buf.readBoolean();
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                TrackBaseBlockEntity be = (TrackBaseBlockEntity) client.level.getBlockEntity(target);
                if (be == null) {
                    return;
                }

                be.handlePacket(detracked);
            });
        });
    }
}
