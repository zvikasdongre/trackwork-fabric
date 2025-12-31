package edn.stratodonut.trackwork.tracks.network;

import com.simibubi.create.foundation.networking.BlockEntityDataPacket;
import edn.stratodonut.trackwork.tracks.blocks.OleoWheelBlockEntity;
import edn.stratodonut.trackwork.tracks.blocks.WheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server to client
 */
public final class OleoWheelPacket extends BlockEntityDataPacket<OleoWheelBlockEntity> {
    public final float wheelTravel;
    public final float steeringValue;
    public final float horizontalOffset;

    public OleoWheelPacket(FriendlyByteBuf buffer) {
        super(buffer);
        this.wheelTravel = buffer.readFloat();
        this.steeringValue = buffer.readFloat();
        this.horizontalOffset = buffer.readFloat();
    }

    public OleoWheelPacket(BlockPos pos, float wheelTravel, float steeringValue, float horizontalOffset) {
        super(pos);
        this.wheelTravel = wheelTravel;
        this.steeringValue = steeringValue;
        this.horizontalOffset = horizontalOffset;
    }

    @Override
    protected void writeData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.wheelTravel);
        buffer.writeFloat(this.steeringValue);
        buffer.writeFloat(this.horizontalOffset);
    }

    @Override
    protected void handlePacket(OleoWheelBlockEntity blockEntity) {
        blockEntity.handlePacket(this.wheelTravel, this.steeringValue, this.horizontalOffset);
    }
}
