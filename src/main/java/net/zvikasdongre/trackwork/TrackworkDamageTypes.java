package net.zvikasdongre.trackwork;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class TrackworkDamageTypes {
    public static final RegistryKey<DamageType> RUN_OVER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Trackwork.MOD_ID, "run_over"));
}
