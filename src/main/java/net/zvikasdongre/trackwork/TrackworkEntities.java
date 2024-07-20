package net.zvikasdongre.trackwork;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zvikasdongre.trackwork.entities.TrackBeltEntity;
import net.zvikasdongre.trackwork.entities.WheelEntity;
import org.valkyrienskies.mod.client.EmptyRenderer;

public class TrackworkEntities {
    public static final EntityEntry<WheelEntity> WHEEL =
            Trackwork.REGISTRATE.entity("wheel_entity", WheelEntity::new, SpawnGroup.MISC)
                    .properties(b -> b.trackRangeBlocks(10)
                            .trackedUpdateRate(1)
                            .dimensions(new EntityDimensions(.3f, .3f, false))
                            .fireImmune()
                    )
                    .renderer(() -> EmptyRenderer::new)
                    .register();

    public static final EntityEntry<TrackBeltEntity> BELT =
            Trackwork.REGISTRATE.entity("track_belt_entity", TrackBeltEntity::new, SpawnGroup.MISC)
                    .properties(b -> b.trackRangeBlocks(10)
                            .trackedUpdateRate(1)
                            .dimensions(new EntityDimensions(1, 1, false))
                            .fireImmune()
                    )
                    .renderer(() -> EmptyRenderer::new)
                    .register();

    public static void initialize() {}
}
