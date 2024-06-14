package net.zvikasdongre.trackwork.entities;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.apigame.physics.PhysicsEntityData;
import org.valkyrienskies.core.apigame.physics.VSWheelCollisionShapeData;
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl;
import org.valkyrienskies.mod.common.entity.VSPhysicsEntity;


public class WheelEntity extends VSPhysicsEntity {
    int timeout = 0;
    public WheelEntity(@NotNull EntityType<? extends VSPhysicsEntity> type, World world) {
        super((EntityType<VSPhysicsEntity>) type, world);
    }

    @NotNull
    public EntityDimensions getDimensions(@NotNull EntityPose pose) {
        return new EntityDimensions(0.01F, 0.01F, false);
    }

    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            this.timeout++;
            if (this.timeout > 60) {
                this.kill();
            }
        }
    }

    public long getShipId() {
        return this.getPhysicsEntityData() == null ? -1L : this.getPhysicsEntityData().getShipId();
    }

    public void keepAlive() {
        this.timeout = 0;
    }

    public static final class DataBuilder {
        private DataBuilder() {
        }

        @NotNull
        public static PhysicsEntityData createBasicData(long shipId, @NotNull ShipTransform transform, double radius, double mass) {
            double inertia = 0.4 * mass * radius * radius;
            ShipInertiaData inertiaData = new ShipInertiaDataImpl(new Vector3d(), mass * radius, new Matrix3d().scale(inertia));
            VSWheelCollisionShapeData collisionShapeData = new VSWheelCollisionShapeData(radius, 0.45, (int)(11.0 * radius));
            return new PhysicsEntityData(shipId, transform, inertiaData, new Vector3d(), new Vector3d(), collisionShapeData, -1, 0.8, 0.6, 0.6, false);
        }
    }
}