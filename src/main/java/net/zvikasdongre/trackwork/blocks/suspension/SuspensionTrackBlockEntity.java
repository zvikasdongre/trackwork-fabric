package net.zvikasdongre.trackwork.blocks.suspension;


import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockRenderType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.*;
import net.zvikasdongre.trackwork.Trackwork;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.TrackworkDamageTypes;
import net.zvikasdongre.trackwork.blocks.ITrackPointProvider;

import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlockEntity;
import net.zvikasdongre.trackwork.data.PhysTrackData;
import net.zvikasdongre.trackwork.forces.PhysicsTrackController;
import net.zvikasdongre.trackwork.networking.TrackworkPackets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3ic;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static net.zvikasdongre.trackwork.forces.PhysicsTrackController.UP;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toMinecraft;

public class SuspensionTrackBlockEntity extends TrackBaseBlockEntity implements ITrackPointProvider {
    private float wheelRadius;
    private float suspensionTravel = 1.5f;
    protected final Random random = new Random();
    @NotNull
    protected final Supplier<Ship> ship;
    private Integer trackID;
    public boolean assembled;
    public boolean assembleNextTick = true;
    private float wheelTravel;
    private float prevWheelTravel;
    private double suspensionScale = 1.0;
    private float horizontalOffset;

    public SuspensionTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.assembled = false;
        this.wheelRadius = 0.5f;
        this.suspensionTravel = 1.5f;
        this.ship = () -> VSGameUtilsKt.getShipObjectManagingPos(this.world, pos);
        setLazyTickRate(40);
    }

    public static SuspensionTrackBlockEntity large(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        SuspensionTrackBlockEntity be = new SuspensionTrackBlockEntity(type, pos, state);
        be.wheelRadius = 1.0f;
        be.suspensionTravel = 2.0f;
        return be;
    }

    public static SuspensionTrackBlockEntity med(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        SuspensionTrackBlockEntity be = new SuspensionTrackBlockEntity(type, pos, state);
        be.wheelRadius = 0.75f;
        be.suspensionTravel = 1.5f;
        return be;
    }

    public void onLoad() {
        super.onLoad();

//        if (this.getCachedState().getBlock() )
    }

    public void remove() {
        super.remove();

        if (this.world != null && !this.world.isClient && this.assembled) {
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
                controller.removeTrackBlock(this.trackID);
            }
        }
    }

    private void assemble() {
        if (!TrackBaseBlock.isValidAxis(this.getCachedState().get(AXIS))) return;
        if (this.world != null && !this.world.isClient) {
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null && Math.abs(1.0 - ship.getTransform().getShipToWorldScaling().length()) > 0.01) {
                this.assembled = true;
                PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
                PhysTrackData.PhysTrackCreateData data = new PhysTrackData.PhysTrackCreateData(toJOML(Vec3d.ofCenter(this.getPos())));
                this.trackID = controller.addTrackBlock(data);
                this.sendData();
                if (this.trackID != null) return;
            }
        }
    }

    public void disassemble() {

    }

    @Override
    public void tick() {
        super.tick();

        if (this.ship.get() != null && this.assembleNextTick && !this.assembled && this.world != null) {
            this.assemble();
            this.assembleNextTick = false;
            return;
        }

        // Ground particles
        if (this.world.isClient && this.ship.get() != null && Math.abs(this.getSpeed()) > 64) {
            Vector3d pos = toJOML(Vec3d.ofBottomCenter(this.getPos()));
            Vector3d ground = VSGameUtilsKt.getWorldCoordinates(this.world, this.getPos(), pos.sub(UP.mul(this.wheelTravel * 1.2, new Vector3d())));
            BlockPos blockpos = BlockPos.ofFloored(toMinecraft(ground));
            BlockState blockstate = this.world.getBlockState(blockpos);
            // Is this safe without calling BlockState::addRunningEffects?
            if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                Vector3dc speed = this.ship.get().getShipTransform().getShipToWorldRotation().transform(getActionVec3d(this.getCachedState().get(AXIS), this.getSpeed()));
                world.addParticle(new BlockStateParticleEffect(
                                ParticleTypes.BLOCK, blockstate).setSourcePos(blockpos),
                        pos.x + (this.random.nextDouble() - 0.5D),
                        pos.y + 0.25D,
                        pos.z + (this.random.nextDouble() - 0.5D) * this.wheelRadius,
                        speed.x() * -1.0D, 10.5D, speed.z() * -1.0D
                );
            }
        }

        // TODO: degrass + de-snowlayer

        if (this.world.isClient) return;
        if (this.assembled) {
            Vec3d start = Vec3d.ofCenter(this.getPos());
            Direction.Axis axis = this.getCachedState().get(AXIS);
            double restOffset = this.wheelRadius - 0.5f;
            float trackRPM = this.getSpeed();
            double susScaled = this.suspensionTravel * this.suspensionScale;
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                Vec3d worldSpaceNormal = toMinecraft(ship.getTransform().getShipToWorldRotation().transform(VectorConversionsMCKt.toJOMLD(getActionNormal(axis)), new Vector3d()).mul(susScaled + 0.5));
                Vec3d worldSpaceStart = toMinecraft(ship.getShipToWorld().transformPosition(toJOML(start.add(0, -restOffset, 0))));
                Vector3dc worldSpaceForward = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1), new Vector3d());
                Vec3d worldSpaceFutureOffset = toMinecraft(
                        worldSpaceForward.mul(0.1 * ship.getVelocity().dot(worldSpaceForward), new Vector3d())
                );
                Vec3d worldSpaceHorizontalOffset = toMinecraft(
                        worldSpaceForward.mul(this.getPointHorizontalOffset(), new Vector3d())
                );

                Vector3dc forceVec;
                ClipResult clipResult = clipAndResolve(ship, axis, worldSpaceStart.add(worldSpaceFutureOffset).add(worldSpaceHorizontalOffset), worldSpaceNormal);

                forceVec = clipResult.trackTangent.mul(this.wheelRadius / 0.5, new Vector3d());
                if (forceVec.lengthSquared() == 0) {
                    BlockState b = this.world.getBlockState(BlockPos.ofFloored(worldSpaceStart));
                    if (b.getFluidState().isIn(FluidTags.WATER)) {
                        forceVec = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1)).mul(this.wheelRadius / 0.5).mul(0.2);
                    }
                }

                double suspensionTravel = clipResult.suspensionLength.lengthSquared() == 0 ? susScaled : clipResult.suspensionLength.length() - 0.5;
                Vector3dc suspensionForce = toJOML(worldSpaceNormal.multiply( (susScaled - suspensionTravel))).negate();

                PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
                if (this.trackID == null) {return;}
                PhysTrackData.PhysTrackUpdateData data = new PhysTrackData.PhysTrackUpdateData(
                        toJOML(worldSpaceStart),
                        forceVec,
                        toJOML(worldSpaceNormal),
                        suspensionForce,
                        clipResult.groundShipId,
                        clipResult.suspensionLength.lengthSquared() != 0,
                        trackRPM
                );
                this.suspensionScale = controller.updateTrackBlock(this.trackID, data);
                this.prevWheelTravel = this.wheelTravel;
                this.wheelTravel = (float) (suspensionTravel + restOffset);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(this.getPos());
                buf.writeFloat(this.wheelTravel);

                for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, this.getPos())) {
                    ServerPlayNetworking.send(player, TrackworkPackets.SUSPENSION_PACKET_ID, buf);
                }

                // Entity Damage
                // TODO: Players don't get pushed, why?
                List<LivingEntity> hits = this.world.getEntitiesByClass(LivingEntity.class, new Box(this.getPos()).expand(0, -1, 0).contract(0.5), LivingEntity::isAlive);
                Vec3d worldPos = toMinecraft(ship.getShipToWorld().transformPosition(toJOML(Vec3d.ofCenter(this.getPos()))));
                DamageSource damageSource = new DamageSource(
                        world.getRegistryManager()
                                .get(RegistryKeys.DAMAGE_TYPE)
                                .entryOf(TrackworkDamageTypes.RUN_OVER));
                for (LivingEntity e : hits) {
//                    if (e instanceof ItemEntity)
//                        continue;
//                    if (e instanceof AbstractContraptionEntity)
//                        continue;
                    this.push(e, worldPos);
//                    What is this?
//                    if (e instanceof ServerPlayer p) {
//                        ((MSGPLIDuck) p.connection).tallyho$setAboveGroundTickCount(0);
//                    }
                    Vec3d relPos = e.getPos().subtract(worldPos);
                    float speed = Math.abs(this.getSpeed());
                    if (speed > 1) e.damage(damageSource, (speed / 16f) * AllConfigs.server().kinetics.crushingDamage.get());
                }
            }
        }
    }

    @NotNull
    private SuspensionTrackBlockEntity.ClipResult clipAndResolve(ServerShip ship, Axis axis, Vec3d start, Vec3d dir) {
        BlockHitResult bResult = this.world.raycast(new RaycastContext(start, start.add(dir), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, null));
        if (bResult.getType() != HitResult.Type.BLOCK) {
            return new ClipResult(new Vector3d(0), Vec3d.ZERO, null);
        }
        Ship hitShip = VSGameUtilsKt.getShipObjectManagingPos(this.world, bResult.getBlockPos());
        Long hitShipId = null;
        if (hitShip != null) {
             if (hitShip.equals(ship)) return new ClipResult(new Vector3d(0), Vec3d.ZERO, null);
            hitShipId = hitShip.getId();
        }

        Vec3d worldSpacehitExact = bResult.getPos();
        Vec3d forceNormal = start.subtract(worldSpacehitExact);
        Vec3d worldSpaceAxis = toMinecraft(ship.getTransform().getShipToWorldRotation().transform(getAxisAsVec(axis)));
        return new ClipResult(
                toJOML(worldSpaceAxis.crossProduct(forceNormal)).normalize(),
                forceNormal,
                hitShipId
        );
    }

    public void setHorizontalOffset(Vector3dc offset) {
        Direction.Axis axis = this.getCachedState().get(AXIS);
        double factor = offset.dot(getActionVec3d(axis, 1));
        this.horizontalOffset = Math.clamp(-0.5f, 0.5f, Math.round(factor * 8.0f) / 8.0f);
        this.markDirty();
    }

    @Override
    public float getPointDownwardOffset(float partialTicks) {
        return this.getWheelTravel(partialTicks);
    }

    @Override
    public float getPointHorizontalOffset() {
        return this.horizontalOffset;
    }

    @Override
    public boolean isBeltLarge() {
        return this.wheelRadius > 0.75;
    }

    @Override
    public Vec3d getTrackPointSlope(float partialTicks) {
        return new Vec3d(0,
                MathHelper.lerp(partialTicks, this.nextPointVerticalOffset.getFirst(), this.nextPointVerticalOffset.getSecond()) - this.getWheelTravel(partialTicks),
                this.nextPointHorizontalOffset - this.horizontalOffset
        );
    }
    @NotNull 
    @Override
    public ITrackPointProvider.PointType getTrackPointType() {
//        if (this.getCachedState().hasProperty(WHEEL_VARIANT) &&
//                this.getCachedState().get(WHEEL_VARIANT) == SuspensionTrackBlock.TrackVariant.BLANK) return PointType.BLANK;
        return PointType.GROUND;
    }

    @Override
    public float getWheelRadius() {
        return this.wheelRadius;
    }

    public float getSpeed() {
        if (!assembled) return 0;
        return Math.clamp(-TrackworkConfigs.maxRPM.get(), TrackworkConfigs.maxRPM.get(), super.getSpeed());
    }

    public static void push(Entity entity, Vec3d worldPos) {
        if (!entity.noClip) {
            double d0 = entity.getX() - worldPos.x;
            double d1 = entity.getZ() - worldPos.z;
            double d2 = MathHelper.absMax(d0, d1);
            if (d2 >= (double)0.01F) {
                d2 = java.lang.Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;
                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= (double)0.2F;
                d1 *= (double)0.2F;

                if (!entity.hasPassengers()) {
                    entity.addVelocity(d0, 0.0D, d1);
                }
            }
        }
    }

    @Override
    public void write(NbtCompound compound, boolean clientPacket) {
        compound.putBoolean("Assembled", this.assembled);
        if (this.trackID != null) compound.putInt("trackBlockID", this.trackID);
        compound.putFloat("WheelTravel", this.wheelTravel);
        compound.putFloat("horizontalOffset", this.horizontalOffset);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(NbtCompound compound, boolean clientPacket) {
        this.assembled = compound.getBoolean("Assembled");
        if (this.trackID == null && compound.contains("trackBlockID")) this.trackID = compound.getInt("trackBlockID");
        this.wheelTravel = compound.getFloat("WheelTravel");
        if (compound.contains("horizontalOffset")) this.horizontalOffset = compound.getFloat("horizontalOffset");
        this.prevWheelTravel = this.wheelTravel;
        super.read(compound, clientPacket);
    }

    public float getWheelTravel(float partialTicks) {
        return MathHelper.lerp(partialTicks, prevWheelTravel, wheelTravel);
    }

    public void setWheelTravel(float wheelTravel) {
        this.prevWheelTravel = this.wheelTravel;
        this.wheelTravel = wheelTravel;
    }

    public static record ClipResult(Vector3dc trackTangent, Vec3d suspensionLength, @Nullable Long groundShipId) {
    }
}
