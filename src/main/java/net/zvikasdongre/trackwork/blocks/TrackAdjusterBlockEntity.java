package net.zvikasdongre.trackwork.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.zvikasdongre.trackwork.forces.PhysicsTrackController;
import net.zvikasdongre.trackwork.forces.SimpleWheelController;
import org.joml.Vector3f;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class TrackAdjusterBlockEntity extends KineticBlockEntity {
    public TrackAdjusterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (this.world.isClient) return;
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerWorld) this.world, this.getPos());
        if (ship != null) {
            PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
            controller.resetSuspension();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient) return;
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerWorld) this.world, this.getPos());
        if (ship != null) {
            Direction.Axis axis = this.getCachedState().get(RotatedPillarKineticBlock.AXIS);
            Vector3f vec = Direction.get(Direction.AxisDirection.POSITIVE, axis).getUnitVector();
            vec.mul(this.getSpeed() / 20000f);

            PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
            controller.adjustSuspension(vec);

            SimpleWheelController controller2 = SimpleWheelController.getOrCreate(ship);
            controller2.adjustSuspension(vec);
        }
    }
}
