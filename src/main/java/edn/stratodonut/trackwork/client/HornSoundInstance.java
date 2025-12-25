package edn.stratodonut.trackwork.client;

import edn.stratodonut.trackwork.TrackSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class HornSoundInstance extends AbstractTickableSoundInstance {
    private boolean playing;
    private int ticksLeft;
    private int note;

    public HornSoundInstance(int note, Vec3 pos) {
        super(TrackSounds.HORN.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.note = note;
        looping = true;
        playing = true;
        volume = 0.5f;
        delay = 0;
        this.keepAlive();
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    @Override
    public void tick() {
        if (playing) {
            volume = Math.min(1, volume + .25f);
            this.ticksLeft--;
            if (ticksLeft == 0) {
                this.kill();
            }
            return;
        }
        volume = Math.max(0, volume - .25f);
        if (volume == 0) {
            stop();
        }
    }

    public void kill() {
        this.playing = false;
    }

    public void keepAlive() {
        this.ticksLeft = 2;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getNote() {
        return note;
    }
}
