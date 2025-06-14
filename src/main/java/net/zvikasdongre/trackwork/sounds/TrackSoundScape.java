package net.zvikasdongre.trackwork.sounds;

import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;
import java.util.List;

public class TrackSoundScape {
    List<ContinuousSound> continuous;
    List<RepeatingSound> repeating;
    private int sound_volume_arg_max = 15;
    private float pitch;
    private AmbientGroup group;
    private Vec3d meanPos;
    private PitchGroups.Group pitchGroup;

    public TrackSoundScape(float pitch, AmbientGroup group) {
        this.pitchGroup = PitchGroups.getGroupFromPitch(pitch);
        this.pitch = pitch;
        this.group = group;
        continuous = new ArrayList<>();
        repeating = new ArrayList<>();
    }

    public TrackSoundScape continuous(SoundEvent sound, float relativeVolume, float relativePitch) {
        return add(new ContinuousSound(sound, this, pitch * relativePitch, relativeVolume));
    }

    public TrackSoundScape repeating(SoundEvent sound, float relativeVolume, float relativePitch, int delay) {
        return add(new RepeatingSound(sound, this, pitch * relativePitch, relativeVolume, delay));
    }

    public TrackSoundScape withArgMax(int max) {
        this.sound_volume_arg_max = max;
        return this;
    }

    public TrackSoundScape add(ContinuousSound continuousSound) {
        continuous.add(continuousSound);
        return this;
    }

    public TrackSoundScape add(RepeatingSound repeatingSound) {
        repeating.add(repeatingSound);
        return this;
    }

    public void play() {
        continuous.forEach(MinecraftClient.getInstance()
                .getSoundManager()::play);
    }

    public void tick() {
        if (AnimationTickHolder.getTicks() % TrackSoundScapes.UPDATE_INTERVAL == 0)
            meanPos = null;
        repeating.forEach(RepeatingSound::tick);
    }

    public void remove() {
        continuous.forEach(ContinuousSound::remove);
    }

    public Vec3d getMeanPos() {
        return meanPos == null ? meanPos = determineMeanPos() : meanPos;
    }

    private Vec3d determineMeanPos() {
        meanPos = Vec3d.ZERO;
        int amount = 0;
        MinecraftClient mc = MinecraftClient.getInstance();
        for (BlockPos blockPos : TrackSoundScapes.getAllLocations(group, pitchGroup)) {
            if (mc.world != null) {
                blockPos = BlockPos.ofFloored(VSGameUtilsKt.toWorldCoordinates(mc.world, Vec3d.ofCenter(blockPos)));
            }
            meanPos = meanPos.add(VecHelper.getCenterOf(blockPos));
            amount++;
        }
        if (amount == 0)
            return meanPos;
        return meanPos.multiply(1f / amount);
    }

    public float getVolume() {
        Entity renderViewEntity = MinecraftClient.getInstance().cameraEntity;
        float distanceMultiplier = 0;
        if (renderViewEntity != null) {
            double distanceTo = renderViewEntity.getPos()
                    .distanceTo(getMeanPos());
            distanceMultiplier = (float) MathHelper.lerp(distanceTo / TrackSoundScapes.getMaxAmbientSourceDistance(), 2, 0);
        }
        int soundCount = TrackSoundScapes.getSoundCount(group, pitchGroup);
        float max = AllConfigs.client().ambientVolumeCap.getF();
        return MathHelper.clamp(soundCount / (sound_volume_arg_max * 10f), 0.025f, max) * distanceMultiplier;
    }

}
