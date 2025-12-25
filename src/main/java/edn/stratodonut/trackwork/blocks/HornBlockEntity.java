package edn.stratodonut.trackwork.blocks;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import edn.stratodonut.trackwork.client.HornSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
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

        boolean powered = playOverrideTicks > 0 || getBlockState().getOptionalValue(HornBlock.POWERED).orElse(false);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.tickSound(note, powered));
    }

    @OnlyIn(Dist.CLIENT)
    protected HornSoundInstance soundInstance;
    @OnlyIn(Dist.CLIENT)
    protected void tickSound(int note, boolean powered) {
        if (!powered) {
            if (soundInstance != null) {
                soundInstance.kill();
                soundInstance = null;
            }
            return;
        }

        float pitch = note * 0.2f;
        Vec3 worldPos = VSGameUtilsKt.toWorldCoordinates(this.level, worldPosition.getCenter());
        if (soundInstance == null || soundInstance.isStopped() || soundInstance.getNote() != note) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(soundInstance = new HornSoundInstance(note, worldPos));
        }

        soundInstance.keepAlive();
        soundInstance.setPitch(pitch);
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickSound() {
        playOverrideTicks = 7;
        this.tickSound(this.note, true);
    }

    public void cycleNote() {
        this.note = ++this.note % PITCH_RANGE;
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
