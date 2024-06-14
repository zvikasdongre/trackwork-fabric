package net.zvikasdongre.trackwork;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zvikasdongre.trackwork.entities.TrackBeltEntity;
import net.zvikasdongre.trackwork.entities.WheelEntity;

public class TrackworkEntities {
    public static final EntityType<WheelEntity> WHEEL = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("wheel_entity", "wheel"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, WheelEntity::new).build()
    );

    public static final EntityType<TrackBeltEntity> BELT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("track_belt_entity", "belt"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, TrackBeltEntity::new).build()
    );

    public static void initialize() {

    }
}
