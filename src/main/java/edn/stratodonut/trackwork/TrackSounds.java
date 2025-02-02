package edn.stratodonut.trackwork;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TrackSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TrackworkMod.MOD_ID);

    public static final RegistryObject<SoundEvent> SUSPENSION_CREAK = registerSoundEvents("suspension_creak");
//    public static final RegistryObject<SoundEvent> TRACK_CREAK = registerSoundEvents("suspension_creak");
    public static final RegistryObject<SoundEvent> POWER_TOOL = registerSoundEvents("power_wrench");
    public static final RegistryObject<SoundEvent> SPRING_TOOL = registerSoundEvents("spring_tool");

    public static final RegistryObject<SoundEvent> TRACK_AMBIENT_SPROCKET = registerSoundEvents("track_ambient_sprocket");

    public static final RegistryObject<SoundEvent> TRACK_AMBIENT_GROUND_1 = registerSoundEvents("track_ambient_ground_1");
    public static final RegistryObject<SoundEvent> TRACK_AMBIENT_GROUND_2 = registerSoundEvents("track_ambient_ground_2");

    public static final RegistryObject<SoundEvent> WHEEL_ROCKTOSS = registerSoundEvents("wheel_rocktoss");

    public static final RegistryObject<SoundEvent> WHEEL_AMBIENT_GROUND_1 = registerSoundEvents("wheel_ambient_ground_1");
    public static final RegistryObject<SoundEvent> WHEEL_AMBIENT_GROUND_2 = registerSoundEvents("wheel_ambient_ground_2");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TrackworkMod.MOD_ID, name)));
    }

    public static void register(IEventBus bus) { SOUND_EVENTS.register(bus); }
}
