package net.zvikasdongre.trackwork.blocks.wheel;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.zvikasdongre.trackwork.TrackworkConfigs;
import net.zvikasdongre.trackwork.TrackworkSounds;
import net.zvikasdongre.trackwork.TrackworkUtil;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import net.zvikasdongre.trackwork.blocks.suspension.TrackworkDamageSources;
import net.zvikasdongre.trackwork.data.SimpleWheelData;
import net.zvikasdongre.trackwork.forces.SimpleWheelController;
import net.zvikasdongre.trackwork.networking.TrackworkPackets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static net.zvikasdongre.trackwork.forces.SimpleWheelController.UP;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toMinecraft;

public class WheelBlockEntity extends KineticBlockEntity {
    protected final Random random = new Random();
    @NotNull
    protected final Supplier<Ship> ship;
    public boolean isFreespin = true;
    public boolean assembled;
    public boolean assembleNextTick = true;
    private float wheelRadius;
    private float suspensionTravel = 1.5f;
    private double suspensionScale = 1.0f;
    private float steeringValue = 0.0f;
    private float linkedSteeringValue = 0.0f;
    private float wheelTravel;
    private float prevWheelTravel;
    private float prevFreeWheelAngle;
    private float horizontalOffset;

    public WheelBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.wheelRadius = 1.0f;
        this.suspensionTravel = 1.5f;
        this.ship = () -> VSGameUtilsKt.getShipObjectManagingPos(this.world, pos);
        this.setLazyTickRate(10);
    }

    public static WheelBlockEntity med(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        WheelBlockEntity be = new WheelBlockEntity(type, pos, state);
        be.wheelRadius = 0.75f;
        be.suspensionTravel = 1.5f;
        return be;
    }

    @Override
    public void remove() {
        super.remove();

        if (this.world != null && !this.world.isClient && this.assembled) {
            ServerShip ship = (ServerShip) this.ship.get();
            if (ship != null) {
                SimpleWheelController controller = SimpleWheelController.getOrCreate(ship);
                controller.removeTrackBlock(this.getPos());
            }
        }
    }

    private void assemble() {
        if (!WheelBlock.isValid(this.getCachedState().get(HORIZONTAL_FACING))) return;
        if (this.world != null && !this.world.isClient) {
            ServerShip ship = (ServerShip) this.ship.get();
            if (ship != null && Math.abs(1.0 - ship.getTransform().getShipToWorldScaling().length()) > 0.01) {
                this.assembled = true;
                SimpleWheelController controller = SimpleWheelController.getOrCreate(ship);
                SimpleWheelData.SimpleWheelCreateData data = new SimpleWheelData.SimpleWheelCreateData(toJOML(Vec3d.ofCenter(this.getPos())));
                controller.addTrackBlock(this.getPos(), data);
                this.sendData();
            }
        }
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
                Vector3dc speed = this.ship.get().getShipTransform().getShipToWorldRotation().transform(TrackworkUtil.getForwardVec3d(this.getCachedState().get(HORIZONTAL_FACING).getAxis(), this.getSpeed()));
                world.addParticle(new BlockStateParticleEffect(
                                ParticleTypes.BLOCK, blockstate).setSourcePos(blockpos),
                        pos.x + (this.random.nextDouble() - 0.5D),
                        pos.y + 0.25D,
                        pos.z + (this.random.nextDouble() - 0.5D) * this.wheelRadius,
                        speed.x() * -1.0D, 10.5D, speed.z() * -1.0D
                );
            }
        }


        // Freespin check
        Direction dir = this.getCachedState().get(HORIZONTAL_FACING);
        BlockPos innerBlock = this.getPos().offset(dir);
        BlockState innerState = this.world.getBlockState(innerBlock);
        if (innerState.getBlock() instanceof KineticBlock ke && ke.hasShaftTowards(world, this.getPos(), innerState, dir.getOpposite())) {
            isFreespin = false;
        } else {
            isFreespin = true;
            if (this.world.isClient) {
                this.prevFreeWheelAngle += this.getWheelSpeed() * 3f / 10;
            }
        }

        if (this.world.isClient) return;
        if (this.assembled) {
            Vec3d start = Vec3d.ofCenter(this.getPos());
            Direction.Axis axis = dir.getAxis();
            double restOffset = this.wheelRadius - 0.5f;
            float trackRPM = this.getSpeed();
            double susScaled = this.suspensionTravel * this.suspensionScale;
            ServerShip ship = (ServerShip) this.ship.get();
            if (ship != null) {
                Vec3d worldSpaceNormal = toMinecraft(ship.getTransform().getShipToWorldRotation().transform((Vector3dc) toJOML(TrackworkUtil.getActionNormal(axis)), new Vector3d()).mul(susScaled + 0.5));
                Vec3d worldSpaceStart = toMinecraft(ship.getShipToWorld().transformPosition(toJOML(start.add(0, -restOffset, 0))));

//                 Steering Control
                int bestSignal = this.world.getReceivedRedstonePower(this.getPos());
                float oldSteeringValue = this.steeringValue;
                this.steeringValue = bestSignal / 15f * ((dir.getDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1));
                float deltaSteeringValue = oldSteeringValue - this.steeringValue;
                this.onLinkedWheel(wbe -> wbe.setLinkedSteeringValue(this.steeringValue));
                Vector3dc worldSpaceForward = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1), new Vector3d());
                float horizontalOffset = this.getPointHorizontalOffset();
                Vec3d worldSpaceFutureOffset = toMinecraft(
                        worldSpaceForward.normalize(Math.clamp(-0.4 - horizontalOffset, 0.4 - horizontalOffset, 0.05 * ship.getVelocity().dot(worldSpaceForward)), new Vector3d())
                );

                Vec3d worldSpaceHorizontalOffset = toMinecraft(
                        ship.getTransform().getShipToWorldRotation().transform(TrackworkUtil.getForwardVec3d(axis, 1), new Vector3d()).mul(horizontalOffset, new Vector3d())
                );

                Vector3dc forceVec;
                ClipResult clipResult = clipAndResolve(ship, axis, worldSpaceStart.add(worldSpaceHorizontalOffset).add(worldSpaceFutureOffset), worldSpaceNormal);

                forceVec = clipResult.trackTangent.mul(this.wheelRadius / 0.5, new Vector3d());
//                if (forceVec.lengthSquared() == 0) {
//                    BlockState b = this.world.getBlockState(BlockPos.ofFloored(worldSpaceStart));
//                    if (b.getFluidState().isIn(FluidTags.WATER)) {
//                        forceVec = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1)).mul(this.wheelRadius / 0.5).mul(0.2);
//                    }
//                }

                double suspensionTravel = clipResult.suspensionLength.lengthSquared() == 0 ? susScaled : clipResult.suspensionLength.length() - 0.5;
                Vector3dc suspensionForce = toJOML(worldSpaceNormal.multiply((susScaled - suspensionTravel))).negate();

                SimpleWheelController controller = SimpleWheelController.getOrCreate(ship);
                SimpleWheelData.SimpleWheelUpdateData data = new SimpleWheelData.SimpleWheelUpdateData(
                        toJOML(worldSpaceStart.add(worldSpaceHorizontalOffset)),
                        forceVec,
                        toJOML(worldSpaceNormal),
                        suspensionForce,
                        isFreespin,
                        clipResult.groundShipId,
                        clipResult.suspensionLength.lengthSquared() != 0,
                        trackRPM
                );
                this.suspensionScale = controller.updateTrackBlock(this.getPos(), data);
                float newWheelTravel = (float) (suspensionTravel + restOffset);
                float delta = newWheelTravel - wheelTravel;
                if (delta < -0.667) {
                    this.world.playSound(null, this.getPos(), TrackworkSounds.SUSPENSION_CREAK, SoundCategory.BLOCKS, Math.max(1.0f, Math.abs(delta * (this.getWheelSpeed() / 256))), 0.8F + 0.4F * this.random.nextFloat());
                }

                this.prevWheelTravel = this.wheelTravel;
                this.wheelTravel = newWheelTravel;

                if (Math.abs(delta) > 0.01f || Math.abs(deltaSteeringValue) > 0.05f) {
                    this.syncToClient();
                }

                // Entity Damage
                List<LivingEntity> hits = this.world.getEntitiesByClass(LivingEntity.class, new Box(this.getPos()).expand(0, -1, 0).contract(0.5), LivingEntity::isAlive);
                Vec3d worldPos = toMinecraft(ship.getShipToWorld().transformPosition(toJOML(Vec3d.ofCenter(this.getPos()))));
                for (LivingEntity e : hits) {

//                    if (e instanceof ItemEntity)
//                        continue;
//                    if (e instanceof AbstractContraptionEntity)
//                        continue;
                    SuspensionTrackBlockEntity.push(e, worldPos);
//                    What is this??
//                    if (e instanceof ServerPlayerEntity p) {
//                        ((MSGPLIDuck) p.connection).tallyho$setAboveGroundTickCount(0);
//                    }
                    Vec3d relPos = e.getPos().subtract(worldPos);
                    float speed = Math.abs(this.getSpeed());
                    if (speed > 1)
                        e.damage(TrackworkDamageSources.runOver(this.world), (speed / 16f) * AllConfigs.server().kinetics.crushingDamage.get());
                }

            }
        }
    }

    // TODO: Terrain dynamics
    // Ground pressure?
    private @NotNull ClipResult clipAndResolve(ServerShip ship, Direction.Axis axis, Vec3d start, Vec3d dir) {
        BlockHitResult bResult = this.world.raycast(new RaycastContext(start, start.add(dir), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, null));
        if (bResult.isInsideBlock()) {
            // TODO: what to do if the wheel is inside?
        }
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
        Vec3d worldSpaceAxis = toMinecraft(ship.getTransform().getShipToWorldRotation().transform(
                TrackworkUtil.getAxisAsVec(axis).rotateAxis(this.getSteeringValue() * Math.toRadians(30), 0, 1, 0)
        ));
        return new ClipResult(
                toJOML(worldSpaceAxis.crossProduct(forceNormal)).normalize(),
                forceNormal,
                hitShipId
        );
    }

    protected void onLinkedWheel(Consumer<WheelBlockEntity> action) {
        Direction dir = this.getCachedState().get(HORIZONTAL_FACING);
        for (int i = 1; i <= TrackworkConfigs.wheelPairDist.get() + 1; i++) {
            BlockPos bpos = this.getPos().offset(dir, i);
            BlockEntity be = this.world.getBlockEntity(bpos);
            if (be instanceof WheelBlockEntity wbe) {
                action.accept(wbe);
                break;
            }
        }
    }

    /*
        This includes steering!
     */
    public Vector3d getActionVec3d(Direction.Axis axis, float length) {
        return TrackworkUtil.getForwardVec3d(axis, length)
                .rotateAxis(this.getSteeringValue() * Math.toRadians(30), 0, 1, 0);
    }

    public float getFreeWheelAngle(float partialTick) {
        return (this.prevFreeWheelAngle + this.getWheelSpeed() * partialTick * 3f / 10) % 360;
    }

    public float getWheelSpeed() {
        if (this.isFreespin) {
            Ship s = this.ship.get();
            if (s != null) {
                Vector3d vel = s.getVelocity().add(s.getOmega().cross(s.getShipToWorld().transformPosition(
                        toJOML(Vec3d.ofBottomCenter(this.getPos()))).sub(
                        s.getTransform().getPositionInWorld()), new Vector3d()), new Vector3d()
                );
                Direction.Axis axis = this.getCachedState().get(HORIZONTAL_FACING).getAxis();
                int sign = axis == Direction.Axis.X ? 1 : -1;
                return sign * (float) TrackworkUtil.roundTowardZero(vel.dot(s.getShipToWorld()
                        .transformDirection(this.getActionVec3d(axis, 1))) * 9.3f * 1 / wheelRadius);
            }
        }
        return this.getSpeed();
    }

    @Override
    public void write(NbtCompound compound, boolean clientPacket) {
        compound.putBoolean("Assembled", this.assembled);
        compound.putFloat("WheelTravel", this.wheelTravel);
        compound.putFloat("HorizontalOffset", this.horizontalOffset);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(NbtCompound compound, boolean clientPacket) {
        this.assembled = compound.getBoolean("Assembled");
        this.wheelTravel = compound.getFloat("WheelTravel");
        this.horizontalOffset = compound.getFloat("HorizontalOffset");
        this.prevWheelTravel = this.wheelTravel;
        super.read(compound, clientPacket);
    }

    public float getWheelRadius() {
        return this.wheelRadius;
    }

    public float getWheelTravel(float partialTicks) {
        return MathHelper.lerp(partialTicks, prevWheelTravel, wheelTravel);
    }

    public float getSteeringValue() {
        return Math.abs(linkedSteeringValue) > Math.abs(steeringValue) ? linkedSteeringValue : steeringValue;
    }

    /**
     * For ponder usage only!
     */
    public void setSteeringValue(float value) {
        this.steeringValue = value;
    }

    public void setHorizontalOffset(Vector3dc offset) {
        Direction.Axis axis = this.getCachedState().get(HORIZONTAL_FACING).getAxis();
        double factor = offset.dot(getActionVec3d(axis, 1));
        this.horizontalOffset = Math.clamp(-0.4f, 0.4f, Math.round(factor * 8.0f) / 8.0f);
        this.onLinkedWheel(wbe -> {
            wbe.horizontalOffset = this.horizontalOffset;
            wbe.syncToClient();
        });
        this.syncToClient();
    }

    public float getPointHorizontalOffset() {
        return this.horizontalOffset;
    }

    @Override
    public float calculateStressApplied() {
        if (this.world.isClient || !TrackworkConfigs.enableStress.get() || !this.assembled)
            return super.calculateStressApplied();
        Ship ship = this.ship.get();
        if (ship == null) return super.calculateStressApplied();
        double mass = ((ServerShip) ship).getInertiaData().getMass();
        float impact = this.calculateStressApplied((float) mass);
        this.lastStressApplied = impact;
        return impact;
    }

    public float calculateStressApplied(float mass) {
        double impact = (mass / 1000) * TrackworkConfigs.stressMult.get() * (2.0f * this.wheelRadius);
        if (impact < 0) {
            impact = 0;
        }
        return (float) impact;
    }

    public void handlePacket(float wheelTravel, float steeringValue, float horizontalOffset) {
        this.prevWheelTravel = this.wheelTravel;
        this.wheelTravel = wheelTravel;
        this.steeringValue = steeringValue;
        this.horizontalOffset = horizontalOffset;
    }

    public void setLinkedSteeringValue(float v) {
        float old = this.getSteeringValue();
        this.linkedSteeringValue = v;
        float delta = this.getSteeringValue() - old;
        if (Math.abs(delta) > 0.05f) this.syncToClient();
    }

    private void syncToClient() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(this.getPos());
        buf.writeFloat(this.wheelTravel);
        buf.writeFloat(this.getSteeringValue());
        buf.writeFloat(this.horizontalOffset);

        for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, this.getPos())) {
            ServerPlayNetworking.send(player, TrackworkPackets.WHEEL_PACKET_ID, buf);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.assembled && !this.world.isClient && this.ship.get() != null) this.syncToClient();
    }

    public record ClipResult(Vector3dc trackTangent, Vec3d suspensionLength, @Nullable Long groundShipId) {
        ;
    }
}
