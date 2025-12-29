package edn.stratodonut.trackwork.client;

import edn.stratodonut.trackwork.TrackSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;

public class HornSoundInstance extends AbstractTickableSoundInstance {
    private boolean playing;
    private int ticksLeft;
    private int note;

    private @Nonnull BlockPos anchorPos;
    private @Nullable Ship ship;

    public HornSoundInstance(int note, BlockPos pos, @Nullable Ship ship) {
        super(TrackSounds.HORN.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.note = note;
        looping = true;
        playing = true;
        volume = 0.5f;
        delay = 0;
        this.keepAlive();
        this.anchorPos = pos;
        Vector3dc worldPos = toJOML(pos.getCenter());
        if (ship != null) {
            worldPos = ship.getShipToWorld().transformPosition(toJOML(anchorPos.getCenter()));
        }
        x = worldPos.x();
        y = worldPos.y();
        z = worldPos.z();
        this.ship = ship;
    }

    @Override
    public void tick() {
        if (ship != null) {
            Vector3dc worldPos = ship.getShipToWorld().transformPosition(toJOML(anchorPos.getCenter()));
            x = worldPos.x();
            y = worldPos.y();
            z = worldPos.z();
        }

        if (playing) {
            volume = Math.min(1, volume + .5f);
            this.ticksLeft--;
            if (ticksLeft == 0) {
                this.kill();
            }
            return;
        }
        volume = Math.max(0, volume - .5f);
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
