package net.zvikasdongre.trackwork.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.zvikasdongre.trackwork.TrackworkEntities;

public class TrackBeltEntity extends Entity {
    private static final TrackedData<BlockPos> PARENT = DataTracker.registerData(TrackBeltEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private boolean wow = false;
    private BlockPos parentPos;
    private int timeout = 0;

    public TrackBeltEntity(EntityType<?> type, World level) {
        super(type, level);
    }

    public static TrackBeltEntity create(World level, BlockPos pos) {
        TrackBeltEntity e = (TrackBeltEntity) TrackworkEntities.BELT.create(level);
        e.parentPos = pos;
        return e;
    }

    public void tick() {
        super.tick();
        if (!this.wow && !this.getWorld().isClient) {
            if (!this.wow) {
                this.dataTracker.set(PARENT, this.parentPos);
                this.wow = true;
            }
            this.timeout++;
            if (this.timeout > 60) {
                this.kill();
            }
        }
    }


    public BlockPos getParentPos() {
        return (BlockPos) this.dataTracker.get(PARENT);
    }

    protected void initDataTracker() {
        this.dataTracker.startTracking(PARENT, null);
    }

    protected void readCustomDataFromNbt(NbtCompound compound) {
        if (compound.getBoolean("ParentPos")) {
            this.parentPos = NbtHelper.toBlockPos(compound.getCompound("ParentPos"));
        }

        this.dataTracker.set(PARENT, this.parentPos);
    }

    protected void writeCustomDataToNbt(NbtCompound compound) {
        compound.put("ParentPos", NbtHelper.fromBlockPos(this.parentPos));
    }

    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

}