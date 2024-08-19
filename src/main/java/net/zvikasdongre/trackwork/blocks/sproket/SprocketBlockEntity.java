package net.zvikasdongre.trackwork.blocks.sproket;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.Lang;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.TrackworkEntities;
import net.zvikasdongre.trackwork.blocks.ITrackPointProvider;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlockEntity;
import net.zvikasdongre.trackwork.data.PhysEntityTrackData;
import net.zvikasdongre.trackwork.entities.TrackBeltEntity;
import net.zvikasdongre.trackwork.entities.WheelEntity;
import net.zvikasdongre.trackwork.forces.PhysicsEntityTrackController;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.apigame.constraints.*;
import org.valkyrienskies.core.apigame.physics.PhysicsEntityData;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class SprocketBlockEntity extends TrackBaseBlockEntity implements ITrackPointProvider {
    private float wheelRadius;
    protected final Supplier<Ship> ship;
    private Integer trackID;
    private UUID wheelID;
    @NotNull
    private WeakReference<WheelEntity> wheel;
    public boolean assembled;
    public boolean assembleNextTick = true;

    public SprocketBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.assembled = false;
        this.wheelRadius = 0.5F;
        this.ship = () -> VSGameUtilsKt.getShipObjectManagingPos(this.getWorld(), pos);
        this.wheel = new WeakReference<>(null);
        this.setLazyTickRate(40);
    }

    public static SprocketBlockEntity large(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        SprocketBlockEntity be = new SprocketBlockEntity(type, pos, state);
        be.wheelRadius = 1.0F;
        return be;
    }

    public static SprocketBlockEntity med(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        SprocketBlockEntity be = new SprocketBlockEntity(type, pos, state);
        be.wheelRadius = 0.75F;
        return be;
    }

    public void destroy() {
        super.destroy();
        if (this.getWorld() != null && !this.getWorld().isClient() && this.assembled) {
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate(ship);
                controller.removeTrackBlock((ServerWorld)this.getWorld(), this.trackID);
                Objects.requireNonNull(this.wheel.get()).kill();
            }
        }
    }

    public void onLoad() {
        super.onLoad();
        if (!this.getWorld().isClient() && this.assembled) {
            Entity e = ((ServerWorld)this.getWorld()).getEntity(this.wheelID);
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                if (!(e instanceof WheelEntity wheel)) {
                    this.assemble();
                    return;
                }

                if (this.constrainWheel(ship, wheel.getShipId(), VectorConversionsMCKt.toJOML(Vec3d.of(this.getPos()))) != null) {
                    return;
                }

                this.wheel = new WeakReference<>(wheel);
            }

            this.assembled = false;
            this.assembleNextTick = true;
        }
    }

    @Deprecated
    public boolean summonBelt() {
        if (!this.getWorld().isClient()) {
        TrackBeltEntity e = TrackBeltEntity.create(this.getWorld(), this.getPos());
            e.setPosition(Vec3d.of(this.getPos()));
            this.getWorld().spawnEntity(e);
        }

        return true;
    }

    private void assemble() {
        if (this.getWorld() != null && !this.getWorld().isClient()) {
            if (!TrackBaseBlock.isValidAxis((Axis)this.getCachedState().get(RotatedPillarKineticBlock.AXIS))) {
                return;
            }

            ServerWorld slevel = (ServerWorld)this.getWorld();
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate(ship);
                if (this.assembled && this.trackID != null) {
                    controller.removeTrackBlock((ServerWorld)this.getWorld(), this.trackID);
                }

                this.assembled = true;
                Vector3dc trackLocalPos = VectorConversionsMCKt.toJOML(Vec3d.ofCenter(this.getPos()));
                WheelEntity wheel = (WheelEntity) TrackworkEntities.WHEEL.create(slevel);
                long wheelId = VSGameUtilsKt.getShipObjectWorld(slevel).allocateShipId(VSGameUtilsKt.getDimensionId(slevel));
                double wheelRadius = (double)this.wheelRadius;
                Vector3dc wheelGlobalPos = ship.getTransform().getShipToWorld().transformPosition(trackLocalPos, new Vector3d());
                ShipTransform transform = ShipTransformImpl.Companion.create(wheelGlobalPos, new Vector3d());
                PhysicsEntityData wheelData = WheelEntity.DataBuilder.createBasicData(wheelId, transform, wheelRadius, 1000.0);
                wheel.setPhysicsEntityData(wheelData);
                wheel.setPosition(VectorConversionsMCKt.toMinecraft(wheelGlobalPos));
                slevel.spawnEntity(wheel);
                PhysEntityTrackData.CreateData createData = this.constrainWheel(ship, wheelId, trackLocalPos);
                this.trackID = controller.addTrackBlock(createData);
                this.wheelID = wheel.getUuid();
                this.wheel = new WeakReference<>(wheel);
                this.sendData();
            }
        }
    }

    private PhysEntityTrackData.CreateData constrainWheel(ServerShip ship, long wheelId, Vector3dc trackLocalPos) {
        ServerWorld slevel = (ServerWorld) this.world;
        double attachCompliance = 1e-8;
        double attachMaxForce = 1e150;
        double hingeMaxForce = 1e75;
        Vector3dc axis = getAxisAsVec(this.getCachedState().get(AXIS));
//                VSSlideConstraint slider = new VSSlideConstraint(
//                        ship.getId(),
//                        wheelId,
//                        attachCompliance,
//                        trackLocalPos,
//                        new Vector3d(0, 0, 0),
//                        attachMaxForce,
//                        UP,
//                        SUSPENSION_TRAVEL
//                );
        VSAttachmentConstraint slider = new VSAttachmentConstraint(
                ship.getId(),
                wheelId,
                attachCompliance,
                trackLocalPos,
                new Vector3d(0, 0, 0),
                attachMaxForce,
                0.0
        );
        VSHingeOrientationConstraint axle = new VSHingeOrientationConstraint(
                ship.getId(),
                wheelId,
                attachCompliance,
                new Quaterniond().fromAxisAngleDeg(axis, 0),
                new Quaterniond().fromAxisAngleDeg(new Vector3d(0, 0, 1), 0),
                hingeMaxForce
        );


        Integer sliderId = VSGameUtilsKt.getShipObjectWorld(slevel).createNewConstraint(slider);
        Integer axleId = VSGameUtilsKt.getShipObjectWorld(slevel).createNewConstraint(axle);
        if (sliderId == null || axleId == null) return null;

        PhysEntityTrackData.CreateData trackData = new PhysEntityTrackData.CreateData(
                trackLocalPos,
                axis,
                wheelId,
                0,
                0,
                new VSConstraintAndId(sliderId, slider),
                new VSConstraintAndId(axleId, axle),
                this.getSpeed()
        );
        return trackData;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ship.get() != null && this.assembleNextTick && !this.assembled && this.getWorld() != null) {
            this.assemble();
            this.assembleNextTick = false;
        } else if (this.getWorld() == null) {
            Trackwork.LOGGER.warn("Level is null????");
            return;
        } else {
            if (this.assembled && !this.getWorld().isClient()) {
                ServerShip ship = (ServerShip)this.ship.get();
                if (ship != null) {
                    WheelEntity wheel = this.wheel.get();
                    if (wheel == null || !wheel.isAlive() || wheel.isRemoved()) {
                        this.assemble();
                        wheel = this.wheel.get();
                    }

                    wheel.keepAlive();
                    PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate(ship);
                    PhysEntityTrackData.UpdateData data = new PhysEntityTrackData.UpdateData(0.0, 0.0, (double)this.getSpeed());
                    controller.updateTrackBlock(this.trackID, data);
                }
            }
        }
    }

    public void addMassStats(List<Text> tooltip, float mass) {
        Lang.text("Total Mass").style(Formatting.GRAY).forGoggles(tooltip);
        Lang.number((double)mass).text(" kg").style(Formatting.WHITE).forGoggles(tooltip, 1);
    }

    @Override
    public float getPointDownwardOffset(float partialTicks) {
        return (float)((double)this.wheelRadius - 0.5);
    }

    @Override
    public float getPointHorizontalOffset() {
        return 0.0F;
    }

    @Override
    public boolean isBeltLarge() {
        return (double)this.wheelRadius > 0.75;
    }

    @Override
    public Vec3d getTrackPointSlope(float partialTicks) {
        return new Vec3d(
                0.0,
                (double)(
                        MathHelper.lerp(partialTicks, this.nextPointVerticalOffset.getFirst(), this.nextPointVerticalOffset.getSecond())
                                - this.getPointDownwardOffset(partialTicks)
                ),
                (double)this.nextPointHorizontalOffset
        );
    }

    @NotNull
    @Override
    public ITrackPointProvider.PointType getTrackPointType() {
        return ITrackPointProvider.PointType.WRAP;
    }

    @Override
    public float getWheelRadius() {
        return this.wheelRadius;
    }

    public float getSpeed() {
        return Math.clamp(-TrackworkConfigs.maxRPM.get(), TrackworkConfigs.maxRPM.get(), super.getSpeed());
    }

    @Override
    public void write(NbtCompound compound, boolean clientPacket) {
        compound.putBoolean("Assembled", this.assembled);
        if (this.trackID != null) {
            compound.putInt("trackBlockID", this.trackID);
        }

        if (this.wheelID != null) {
            compound.putUuid("wheelID", this.wheelID);
        }

        super.write(compound, clientPacket);
    }

    @Override
    protected void read(NbtCompound compound, boolean clientPacket) {
        this.assembled = compound.getBoolean("Assembled");
        if (this.trackID == null && compound.contains("trackBlockID")) {
            this.trackID = compound.getInt("trackBlockID");
        }

        if (this.wheelID == null && compound.contains("wheelID")) {
            this.wheelID = compound.getUuid("wheelID");
        }

        super.read(compound, clientPacket);
    }

    public float calculateStressApplied() {
        if (this.world.isClient || !TrackworkConfigs.enableStress.get() ||
                !this.assembled || this.getCachedState().get(TrackBaseBlock.PART) != TrackBaseBlock.TrackPart.START) return super.calculateStressApplied();

        Ship ship = this.ship.get();
        if (ship == null) return super.calculateStressApplied();
        double mass = ((ServerShip) ship).getInertiaData().getMass();
        float impact = this.calculateStressApplied((float) mass);
        this.lastStressApplied = impact;
        return impact;
    }

    public float calculateStressApplied(float mass) {
        double impact = (mass / 1000) * TrackworkConfigs.stressMult.get() * (2.0f * this.wheelRadius);
        if (impact < 0.0F) {
            impact = 0.0F;
        }

        return (float) impact;
    }
}
