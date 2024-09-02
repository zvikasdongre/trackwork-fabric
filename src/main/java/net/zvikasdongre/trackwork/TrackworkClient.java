package net.zvikasdongre.trackwork;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.netty.channel.ChannelHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.config.ModConfig;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlockEntity;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlockEntity;
import net.zvikasdongre.trackwork.networking.TrackworkPackets;

public class TrackworkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrackworkPartialModels.init();
        TrackworkSpriteShifts.init();
        TrackworkPonders.init();
        ForgeConfigRegistry.INSTANCE.register(Trackwork.MOD_ID, ModConfig.Type.CLIENT, TrackworkConfigs.CLIENT_SPEC);


        ClientPlayNetworking.registerGlobalReceiver(TrackworkPackets.SUSPENSION_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            float wheelTravel = buf.readFloat();
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                SuspensionTrackBlockEntity be = (SuspensionTrackBlockEntity) client.world.getBlockEntity(target);
                if (be == null) {
                    return;
                }

                be.setWheelTravel(wheelTravel);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TrackworkPackets.WHEEL_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            float wheelTravel = buf.readFloat();
            float steeringValue = buf.readFloat();
            float horizontalOffset = buf.readFloat();
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                WheelBlockEntity be = (WheelBlockEntity) client.world.getBlockEntity(target);
                if (be == null) {
                    return;
                }

                be.handlePacket(wheelTravel, steeringValue, horizontalOffset);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TrackworkPackets.THROW_TRACK_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            boolean detracked = buf.readBoolean();
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                TrackBaseBlockEntity be = (TrackBaseBlockEntity) client.world.getBlockEntity(target);
                if (be == null) {
                    return;
                }

                be.handlePacket(detracked);
            });
        });
    }
}