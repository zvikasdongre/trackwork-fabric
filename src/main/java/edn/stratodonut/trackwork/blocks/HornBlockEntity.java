package edn.stratodonut.trackwork.blocks;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import edn.stratodonut.trackwork.client.HornSoundInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

public class HornBlockEntity extends SmartBlockEntity {
    public static final int PITCH_RANGE = 8;
    protected int note = 0;
    protected int playOverrideTicks = 0;

    public HornBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // DO NOTHING
    }

    @Override
    public void tick() {
        super.tick();
        playOverrideTicks = Math.max(--playOverrideTicks, 0);

        if (!level.isClientSide) return;

        boolean powered = getPowered();
        this.tickSound(note, powered);
    }

    @Environment(EnvType.CLIENT)
    protected HornSoundInstance soundInstance;
    @Environment(EnvType.CLIENT)
    protected void tickSound(int note, boolean powered) {
        if (!powered) {
            if (soundInstance != null) {
                soundInstance.kill();
                soundInstance = null;
            }
            return;
        }

        float pitch = note * 0.2f;
        if (soundInstance == null || soundInstance.isStopped() || soundInstance.getNote() != note) {
            Minecraft.getInstance()
                .getSoundManager()
                .play(soundInstance = new HornSoundInstance(
                        note,
                        this.getBlockPos(),
                        VSGameUtilsKt.getLoadedShipManagingPos(this.level, this.getBlockPos())
                ));
        }

        soundInstance.keepAlive();
        soundInstance.setPitch(pitch);
    }

    @Environment(EnvType.CLIENT)
    protected void tickSound() {
        playOverrideTicks = 7;
        this.tickSound(this.note, true);
    }

    public void cycleNote() {
        this.note = ++this.note % PITCH_RANGE;
    }

    public boolean getPowered() {
        return playOverrideTicks > 0 || getBlockState().getOptionalValue(HornBlock.POWERED).orElse(false);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Note", this.note);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("Note")) {
            this.note = Mth.clamp(tag.getInt("Note"), 0, PITCH_RANGE-1);
        }
    }
}
