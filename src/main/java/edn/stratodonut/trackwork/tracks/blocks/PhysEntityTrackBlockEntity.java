package edn.stratodonut.trackwork.tracks.blocks;

import edn.stratodonut.trackwork.*;
import edn.stratodonut.trackwork.sounds.TrackSoundScapes;
import edn.stratodonut.trackwork.tracks.ITrackPointProvider;
import edn.stratodonut.trackwork.tracks.TrackBeltEntity;
import edn.stratodonut.trackwork.tracks.data.PhysEntityTrackData;
import edn.stratodonut.trackwork.tracks.forces.PhysEntityTrackController;
import edn.stratodonut.trackwork.wheel.WheelEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.internal.joints.*;
import org.valkyrienskies.core.internal.physics.PhysicsEntityData;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static edn.stratodonut.trackwork.tracks.blocks.TrackBaseBlock.*;
import static net.minecraft.ChatFormatting.GRAY;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toMinecraft;

public class PhysEntityTrackBlockEntity extends TrackBaseBlockEntity implements ITrackPointProvider {
    private float wheelRadius;
    protected final Supplier<Ship> ship;
    @Deprecated(forRemoval = true)
    private Integer trackID;
    private UUID wheelID;
    @NotNull
    private WeakReference<WheelEntity> wheel;
    public boolean assembled;
    public boolean assembleNextTick = true;
    MutableComponent chatMessage = MutableComponent.create(ComponentContents.EMPTY);

    public PhysEntityTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.assembled = false;
        this.wheelRadius = 0.5f;
        this.ship = () -> VSGameUtilsKt.getShipObjectManagingPos(this.level, pos);
        this.wheel = new WeakReference<>(null);
    }

    public static PhysEntityTrackBlockEntity large(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        PhysEntityTrackBlockEntity be = new PhysEntityTrackBlockEntity(type, pos, state);
        be.wheelRadius = 1.0f;
        return be;
    }

    public static PhysEntityTrackBlockEntity med(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        PhysEntityTrackBlockEntity be = new PhysEntityTrackBlockEntity(type, pos, state);
        be.wheelRadius = 0.75f;
        return be;
    }

    @Override
    public void destroy() {
        super.destroy();

        if (this.level != null && !this.level.isClientSide && this.assembled) {
            LoadedServerShip ship = (LoadedServerShip) this.ship.get();
            if (ship != null) {
                PhysEntityTrackController controller = PhysEntityTrackController.getOrCreate(ship);
                controller.removeTrackBlock((ServerLevel) this.level, this.getBlockPos());
                Objects.requireNonNull(this.wheel.get()).kill();
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!this.level.isClientSide && this.assembled) {
            Entity e = ((ServerLevel) this.level).getEntity(this.wheelID);
            LoadedServerShip ship = (LoadedServerShip) this.ship.get();
            if (ship != null) {
                if (e instanceof WheelEntity wheel) {
                    this.asyncConstrainWheel(ship, wheel.getShipId(), toJOML(Vec3.atCenterOf(this.getBlockPos())));
                    this.wheel = new WeakReference<>(wheel);
                } else {
                    this.assemble();
                    return;
                }
            }

            this.assembled = false;
            this.assembleNextTick = true;
        }
    }

    @Deprecated
    public boolean summonBelt() {
        if (!this.level.isClientSide) {
            TrackBeltEntity e = TrackBeltEntity.create(this.level, this.getBlockPos());
            e.setPos(Vec3.atLowerCornerOf(this.getBlockPos()));
            this.level.addFreshEntity(e);
        }

        return true;
    }

    private void assemble() {
        if (this.level != null && !this.level.isClientSide) {
            if (!isValidAxis(this.getBlockState().getValue(AXIS))) return;
            ServerLevel slevel = (ServerLevel) this.level;
            LoadedServerShip ship = (LoadedServerShip) this.ship.get();
            if (ship != null) {
                PhysEntityTrackController controller = PhysEntityTrackController.getOrCreate(ship);
                if (this.assembled) {
                    controller.removeTrackBlock((ServerLevel) this.level, this.getBlockPos());
                }
                this.assembled = true;
                Vector3dc trackLocalPos = toJOML(Vec3.atCenterOf(this.getBlockPos()));

                WheelEntity wheel = TrackEntityTypes.WHEEL.create(slevel);
                long wheelId = VSGameUtilsKt.getShipObjectWorld(slevel).allocateShipId(VSGameUtilsKt.getDimensionId(slevel));
                double wheelRadius = this.wheelRadius;
//                Vector3dc wheelOffset = ship.getTransform().getShipToWorldRotation().transform(UP.negate(new Vector3d()));
                Vector3dc wheelGlobalPos = ship.getTransform().getShipToWorld().transformPosition(trackLocalPos, new Vector3d());

                ShipTransform transform = ShipTransformImpl.Companion.create(wheelGlobalPos, new Vector3d());
                PhysicsEntityData wheelData = WheelEntity.DataBuilder.createBasicData(wheelId, transform, wheelRadius, 1000);
                wheel.setPhysicsEntityData(wheelData);
                wheel.setPos(VectorConversionsMCKt.toMinecraft(wheelGlobalPos));
                slevel.addFreshEntity(wheel);

                ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level)).disableCollisionBetween(wheelId, ship.getId());

                this.asyncConstrainWheel(ship, wheelId, trackLocalPos);
                this.wheelID = wheel.getUUID();
                this.wheel = new WeakReference<>(wheel);
                this.sendData();
            }
        }
    }

    /***
     * Constraint creation and Attachment is queued onto the physics backend, hence async
     */
    private void asyncConstrainWheel(LoadedServerShip ship, long wheelId, Vector3dc trackLocalPos) {
        ServerLevel slevel = (ServerLevel) this.level;
        double attachCompliance = 1e-8;
        double attachMaxForce = 1e50;
        double hingeMaxForce = 1e25;
        Vector3dc axis = TrackworkUtil.getAxisAsVec(this.getBlockState().getValue(AXIS));

        // According to Triode's comment in org.valkyrienskies.mod.common.block.TestHingeBlock
        // Hinge constraints will attempt to align the X-axes of both bodies, so to align the Z axis we
        // apply this rotation to the X-axis
        Quaterniond hingeOrientation = new Quaterniond();
        if (this.getBlockState().getValue(AXIS) == Direction.Axis.Z) {
            hingeOrientation = hingeOrientation.rotateLocalY(Math.PI/2);
        }

        VSRevoluteJoint axle = new VSRevoluteJoint(
                ship.getId(),
                new VSJointPose(trackLocalPos, hingeOrientation),
                wheelId,
                new VSJointPose(
                        new Vector3d(0.5, 0.5, 0.5),
                        new Quaterniond()),
                new VSJointMaxForceTorque((float) attachMaxForce, (float) hingeMaxForce),
                attachCompliance,
                null,
                null,
                null,
                null,
                true
        );

        PhysEntityTrackData.CreateData trackData = new PhysEntityTrackData.CreateData(
                trackLocalPos,
                axis,
                wheelId,
                0,
                0,
                axle,
                this.getSpeed()
        );

        ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(slevel)).addJoint(
                axle,
                0,
                (id) -> PhysEntityTrackController.getOrCreate(ship).addTrackBlock(this.getBlockPos(), trackData, id)
        );
    }

    @Override
    public void tick() {
        super.tick();

        // Compatibility with new system
        if (this.trackID != null) {
            this.assembled = false;
            this.trackID = null;
        }

        if (this.ship.get() != null && this.assembleNextTick && !this.assembled && this.level != null) {
            this.assemble();
            this.assembleNextTick = false;
            return;
        }

        if (this.level == null) {
            return;
        }
        if (this.assembled && !this.level.isClientSide) {
            LoadedServerShip ship = (LoadedServerShip) this.ship.get();
            if (ship != null) {
                WheelEntity wheel = this.wheel.get();
                if (wheel == null || !wheel.isAlive() || wheel.isRemoved()) {
                    this.assemble();
                    wheel = this.wheel.get();
                } else {
                    double distance = ship.getShipToWorld().transformPosition(toJOML(Vec3.atCenterOf(this.getBlockPos())))
                            .distance(toJOML(wheel.position()));
                    if (distance > 8f) {
                        this.assemble();
                        wheel = this.wheel.get();
                    }
                }
                if (wheel == null) {
                    TrackworkMod.warn("Wheel is NULL after assembly! At {}", this.getBlockPos().toString());
                    return;
                }
                wheel.keepAlive();

                PhysEntityTrackController controller = PhysEntityTrackController.getOrCreate(ship);
                PhysEntityTrackData.UpdateData data = new PhysEntityTrackData.UpdateData(
                        0,
                        0,
                        this.getSpeed()
                );
                controller.updateTrackBlock(this.getBlockPos(), data);

                // Entity pushing, no damage here
                List<LivingEntity> hits = this.level.getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos())
                        .deflate(0.25)
                );
                Vec3 worldPos = toMinecraft(ship.getShipToWorld().transformPosition(toJOML(Vec3.atCenterOf(this.getBlockPos()))));
                for (LivingEntity e : hits) {
                    SuspensionTrackBlockEntity.push(e, worldPos);
                    if (e instanceof ServerPlayer p) p.connection.send(new ClientboundSetEntityMotionPacket(p));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void tickAudio() {
        float spd = Math.abs(getSpeed());
        float pitch = Mth.clamp((spd / 256f) + .45f, .85f, 1f);
        if (spd < 8)
            return;
        TrackSoundScapes.play(TrackAmbientGroups.TRACK_SPROCKET_AMBIENT, worldPosition, pitch);
    }

//    @Override
//    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
//        if (!TrackworkConfigs.server().enableStress.get()) return false;
//        Ship ship = this.ship.get();
//        if (!this.assembled || ship == null) return false;
//
//        addStressImpactStats(tooltip, calculateStressApplied(this.shipMass));
////        addMassStats(tooltip, this.shipMass);
//
//        return true;
//    }

    public void addMassStats(List<Component> tooltip, float mass) {
        Component.literal("Total Mass")
                .withStyle(GRAY);

        Component.literal(String.valueOf(mass))
                .append(" kg")
                .withStyle(ChatFormatting.WHITE);
//                .space()
//                .add(Lang.translate("gui.goggles.at_current_speed")
//                        .style(ChatFormatting.DARK_GRAY))
    }

    @Override
    public float getPointDownwardOffset(float partialTicks) {
        return (float) (this.wheelRadius - 0.5);
    }

    @Override
    public float getPointHorizontalOffset() {
        return 0.0f;
    }

    public boolean isBeltLarge() {
        return this.wheelRadius > 0.75;
    }

    @Override
    public Vec3 getTrackPointSlope(float partialTicks) {
        return new Vec3(0,
                Mth.lerp(partialTicks, this.nextPointVerticalOffset.getFirst(), this.nextPointVerticalOffset.getSecond()) - this.getPointDownwardOffset(partialTicks),
                this.nextPointHorizontalOffset
        );
    }

    @Override
    public @NotNull PointType getTrackPointType() {
        return PointType.WRAP;
    }

    @Override
    public float getWheelRadius() {
        return this.wheelRadius;
    }

    @Override
    public float getSpeed() {
        if (!assembled) return 0;
        float maxRpm = TrackworkConfigs.server().maxRPM.get();
        return Math.clamp(-maxRpm, maxRpm, super.getSpeed());
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Assembled", this.assembled);
        if (this.trackID != null) compound.putInt("trackBlockID", this.trackID);
        if (this.wheelID != null) compound.putUUID("wheelID", this.wheelID);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        this.assembled = compound.getBoolean("Assembled");
        if (this.trackID == null && compound.contains("trackBlockID")) this.trackID = compound.getInt("trackBlockID");
        if (this.wheelID == null && compound.hasUUID("wheelID")) this.wheelID = compound.getUUID("wheelID");
        super.read(compound, clientPacket);
    }

    @Override
    public float calculateStressApplied() {
        if (this.level.isClientSide || !TrackworkConfigs.server().enableStress.get() ||
                !this.assembled || this.getBlockState().getValue(PART) != TrackPart.start) return super.calculateStressApplied();

        Ship ship = this.ship.get();
        if (ship == null) return super.calculateStressApplied();
        double mass = ((ServerShip) ship).getInertiaData().getMass();
        float impact = this.calculateStressApplied((float) mass);
        this.lastStressApplied = impact;
        return impact;
    }

    public float calculateStressApplied(float mass) {
        double impact = (mass / 1000) * TrackworkConfigs.server().stressMult.get() * (2.0f * this.wheelRadius) * 8;
        if (impact < 0) {
            impact = 0;
        }
        return (float) impact;
    }
}
