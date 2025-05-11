package net.zvikasdongre.trackwork.blocks.suspension;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.zvikasdongre.trackwork.TrackworkDamageTypes;

public class TrackworkDamageSources {
    public static DamageSource runOver(World level) {
        return source(TrackworkDamageTypes.RUN_OVER, level);
    }

    private static DamageSource source(RegistryKey<DamageType> key, WorldAccess level) {
        Registry<DamageType> registry = level.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        return new DamageSource(registry.entryOf(key));
    }

}
