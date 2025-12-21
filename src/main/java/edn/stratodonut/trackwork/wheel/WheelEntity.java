package edn.stratodonut.trackwork.wheel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.core.internal.physics.PhysicsEntityData;
import org.valkyrienskies.core.internal.physics.PhysicsEntityServer;
import org.valkyrienskies.core.internal.physics.VSSphereCollisionShapeData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import javax.annotation.Nullable;

import static org.valkyrienskies.mod.common.ValkyrienSkiesMod.getVsCore;

public class WheelEntity {
    public static @Nullable PhysicsEntityServer getInLevel(ServerLevel level, long id) {
        if (!aliveInLevel(level, id)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectWorld(level)
                .retrieveLoadedPhysicsEntities().get(id);
    }

    public static boolean aliveInLevel(ServerLevel level, long id) {
        return VSGameUtilsKt.getShipObjectWorld(level)
                .retrieveLoadedPhysicsEntities().containsKey(id);
    }

    public static void createInLevel(ServerLevel level, PhysicsEntityData data) {
        // Try adding the rigid body of this entity from the world
        VSGameUtilsKt.getShipObjectWorld(level).createPhysicsEntity(data, ((DimensionIdProvider) level).getDimensionId());
    }

    public static void removeInLevel(ServerLevel level, long id) {
        // Try removing the rigid body of this entity from the world
        VSGameUtilsKt.getShipObjectWorld(level).deletePhysicsEntity(id);
    }

    public static boolean moveTo(ServerLevel level, long id, Vector3dc pos) {
        if (!aliveInLevel(level, id)) {
            return false;
        }
        PhysicsEntityServer serverData = VSGameUtilsKt.getShipObjectWorld(level)
                .retrieveLoadedPhysicsEntities().get(id);

        ShipTeleportData teleportData = getVsCore().newShipTeleportData(
                pos,
                new Quaterniond(),
                new Vector3d(),
                new Vector3d(),
                null,
                null,
                null
        );
        VSGameUtilsKt.getShipObjectWorld(level).teleportPhysicsEntity(serverData, teleportData);
        return true;
    }

    public static final class DataBuilder {
        private DataBuilder() {
        }

        @NotNull
        public static PhysicsEntityData createBasicData(long shipId, @NotNull ShipTransform transform, double radius, double mass) {
            double inertia = 0.4 * mass * radius * radius;
            ShipInertiaData inertiaData = getVsCore().newShipInertiaData(new Vector3d(), mass * radius, new Matrix3d().scale(inertia));
            VSSphereCollisionShapeData collisionShapeData = new VSSphereCollisionShapeData(radius);
            // TODO: Current crashes physics, reimplement when safe
//            VSWheelCollisionShapeData collisionShapeData = new VSWheelCollisionShapeData(radius, 0.45, (int)(11 * radius));
            return new PhysicsEntityData(
                    shipId,
                    transform,
                    inertiaData,
                    new Vector3d(),
                    new Vector3d(),
                    collisionShapeData,
                    -1,
                    0.8,
                    0.6,
                    0.6,
                    false

            );
        }
    }
}
