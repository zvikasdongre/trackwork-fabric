package edn.stratodonut.trackwork;

import com.tterrag.registrate.fabric.RegistryObject;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class TrackSounds {
    private TrackSounds() {
    }

    public static final SoundEvent SUSPENSION_CREAK = registerSound("suspension_creak");
    public static final SoundEvent POWER_TOOL = registerSound("power_wrench");
    public static final SoundEvent SPRING_TOOL = registerSound("spring_tool");

    public static final SoundEvent TRACK_AMBIENT_SPROCKET = registerSound("track_ambient_sprocket");

    public static final SoundEvent TRACK_AMBIENT_GROUND_1 = registerSound("track_ambient_ground_1");
    public static final SoundEvent TRACK_AMBIENT_GROUND_2 = registerSound("track_ambient_ground_2");
    public static final SoundEvent TRACK_GROUND_SLIP = registerSound("track_ground_slip");

    public static final SoundEvent WHEEL_ROCKTOSS = registerSound("wheel_rocktoss");

    public static final SoundEvent WHEEL_AMBIENT_GROUND_1 = registerSound("wheel_ambient_ground_1");
    public static final SoundEvent WHEEL_AMBIENT_GROUND_2 = registerSound("wheel_ambient_ground_2");
    public static final SoundEvent WHEEL_GROUND_SLIP = registerSound("wheel_ground_slip");

    private static SoundEvent registerSound(String id) {
        ResourceLocation identifier = new ResourceLocation(TrackworkMod.MOD_ID, id);
        ;
        return Registry.register(BuiltInRegistries.SOUND_EVENT,
                identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void register() { }
}
