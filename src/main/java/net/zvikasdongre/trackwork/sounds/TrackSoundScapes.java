package net.zvikasdongre.trackwork.sounds;

import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

public class TrackSoundScapes {
    static final int UPDATE_INTERVAL = 5;
    private static final Map<AmbientGroup, Map<PitchGroups.Group, Set<BlockPos>>> counter = new IdentityHashMap<>();
    private static final Map<Pair<AmbientGroup, PitchGroups.Group>, TrackSoundScape> activeSounds = new HashMap<>();

    public static void play(AmbientGroup group, BlockPos pos, float pitch) {
        if (!AllConfigs.client().enableAmbientSounds.get())
            return;
        if (!outOfRange(pos))
            addSound(group, pos, pitch);
    }

    public static void tick() {
        activeSounds.values()
                .forEach(TrackSoundScape::tick);

        if (AnimationTickHolder.getTicks() % UPDATE_INTERVAL != 0)
            return;

        boolean disable = !AllConfigs.client().enableAmbientSounds.get();
        for (Iterator<Map.Entry<Pair<AmbientGroup, PitchGroups.Group>, TrackSoundScape>> iterator = activeSounds.entrySet()
                .iterator(); iterator.hasNext(); ) {

            Map.Entry<Pair<AmbientGroup, PitchGroups.Group>, TrackSoundScape> entry = iterator.next();
            Pair<AmbientGroup, PitchGroups.Group> key = entry.getKey();
            TrackSoundScape value = entry.getValue();

            if (disable || getSoundCount(key.getFirst(), key.getSecond()) == 0) {
                value.remove();
                iterator.remove();
            }
        }

        counter.values()
                .forEach(m -> m.values()
                        .forEach(Set::clear));
    }

    private static void addSound(AmbientGroup group, BlockPos pos, float pitch) {
        PitchGroups.Group groupFromPitch = PitchGroups.getGroupFromPitch(pitch);
        Set<BlockPos> set = counter.computeIfAbsent(group, ag -> new IdentityHashMap<>())
                .computeIfAbsent(groupFromPitch, pg -> new HashSet<>());
        set.add(pos);

        Pair<AmbientGroup, PitchGroups.Group> pair = Pair.of(group, groupFromPitch);
        activeSounds.computeIfAbsent(pair, $ -> {
            TrackSoundScape TrackSoundScape = group.instantiate(pitch);
            TrackSoundScape.play();
            return TrackSoundScape;
        });
    }

    public static void invalidateAll() {
        counter.clear();
        activeSounds.forEach(($, sound) -> sound.remove());
        activeSounds.clear();
    }

    protected static boolean outOfRange(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) {
            pos = BlockPos.ofFloored(VSGameUtilsKt.toWorldCoordinates(mc.world, Vec3d.ofCenter(pos)));
        }
        return !getCameraPos().isWithinDistance(pos, getMaxAmbientSourceDistance());
    }

    protected static int getMaxAmbientSourceDistance() {
        return TrackworkConfigs.trackSoundDist.get();
    }

    protected static BlockPos getCameraPos() {
        Entity renderViewEntity = MinecraftClient.getInstance().cameraEntity;
        if (renderViewEntity == null)
            return BlockPos.ORIGIN;
        BlockPos playerLocation = renderViewEntity.getBlockPos();
        return playerLocation;
    }

    public static int getSoundCount(AmbientGroup group, PitchGroups.Group pitchGroup) {
        return getAllLocations(group, pitchGroup).size();
    }

    public static Set<BlockPos> getAllLocations(AmbientGroup group, PitchGroups.Group pitchGroup) {
        return counter.getOrDefault(group, Collections.emptyMap())
                .getOrDefault(pitchGroup, Collections.emptySet());
    }
}
