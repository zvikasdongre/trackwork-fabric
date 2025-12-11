package edn.stratodonut.trackwork;

import com.tterrag.registrate.util.entry.EntityEntry;
import edn.stratodonut.trackwork.tracks.TrackBeltEntity;
import edn.stratodonut.trackwork.tracks.render.TrackBeltEntityRenderer;
import edn.stratodonut.trackwork.wheel.WheelEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.valkyrienskies.mod.client.EmptyRenderer;

import static edn.stratodonut.trackwork.TrackworkMod.REGISTRATE;
import static net.minecraft.world.entity.Mob.createMobAttributes;

public class TrackEntityTypes {

    public static final EntityEntry<WheelEntity> WHEEL =
            REGISTRATE.entity("wheel_entity", WheelEntity::new, MobCategory.MISC)
                    .properties(b -> b.setTrackingRange(10)
                            .setUpdateInterval(1)
                            .sized(.3f, .3f)
                            .fireImmune()
                    )
                    .attributes(() -> createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 1.0)
                            .add(Attributes.MOVEMENT_SPEED, 0.25))
                    .renderer(() -> EmptyRenderer::new)
                    .register();

    public static final EntityEntry<TrackBeltEntity> BELT =
            REGISTRATE.entity("track_belt_entity", TrackBeltEntity::new, MobCategory.MISC)
                    .properties(b -> b.setTrackingRange(10)
                            .setUpdateInterval(1)
                            .sized(1f, 1f)
                            .fireImmune()
                    )
                    .renderer(() -> TrackBeltEntityRenderer::new)
                    .register();

    public static void register() {}
}
