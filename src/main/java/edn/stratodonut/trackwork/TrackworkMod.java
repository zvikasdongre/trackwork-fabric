package edn.stratodonut.trackwork;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;

import edn.stratodonut.trackwork.tracks.forces.OleoWheelController;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.VsCoreApi;
import org.valkyrienskies.core.api.attachment.AttachmentRegistration;
import org.valkyrienskies.core.api.attachment.AttachmentSerializer;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.api_impl.events.VsApiImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.core.api.VsCoreApi;
import org.valkyrienskies.core.api.attachment.AttachmentRegistration;
import org.valkyrienskies.core.api.attachment.AttachmentSerializer;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.api_impl.events.VsApiImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import edn.stratodonut.trackwork.tracks.forces.PhysEntityTrackController;
import edn.stratodonut.trackwork.tracks.forces.PhysicsTrackController;
import edn.stratodonut.trackwork.tracks.forces.SimpleWheelController;

import static net.createmod.catnip.lang.FontHelper.Palette.STANDARD_CREATE;

public class TrackworkMod implements ModInitializer
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "trackwork";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, STANDARD_CREATE));
    }

    @Override
    public void onInitialize() {

        VSGameUtilsKt.getVsCore().registerAttachment(VSGameUtilsKt.getVsCore()
                .newAttachmentRegistrationBuilder(PhysEntityTrackController.class)
                .useLegacySerializer()
                .build()
        );

        VSGameUtilsKt.getVsCore().registerAttachment(VSGameUtilsKt.getVsCore()
                .newAttachmentRegistrationBuilder(PhysicsTrackController.class)
                .useLegacySerializer()
                .build()
        );

        VSGameUtilsKt.getVsCore().registerAttachment(VSGameUtilsKt.getVsCore()
                .newAttachmentRegistrationBuilder(SimpleWheelController.class)
                .useLegacySerializer()
                .build()
        );

        VSGameUtilsKt.getVsCore().registerAttachment(VSGameUtilsKt.getVsCore()
                .newAttachmentRegistrationBuilder(OleoWheelController.class)
                .useLegacySerializer()
                .build()
        );

        VSGameUtilsKt.getVsCore().getShipLoadEvent().on(ship -> {
            PhysEntityTrackController.getOrCreate(ship.getShip());
            PhysicsTrackController.getOrCreate(ship.getShip());
            SimpleWheelController.getOrCreate(ship.getShip());
        });

        TrackBlocks.register();
        TrackworkItems.register();
        TrackCreativeTabs.register();
        TrackBlockEntityTypes.register();
        TrackEntityTypes.register();
        REGISTRATE.register();
        TrackSounds.register();
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, TrackworkConfigs.SERVER_SPEC);

    }

    public static void warn(String format, Object arg) {
        LOGGER.warn(format, arg);
    }

    public static void warn(String format, Object... args) {
        LOGGER.warn(format, args);
    }

    public static ResourceLocation getResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
