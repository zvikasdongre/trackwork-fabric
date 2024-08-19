package net.zvikasdongre.trackwork.items;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.zvikasdongre.trackwork.TrackworkSounds;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlock;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlockEntity;
import net.zvikasdongre.trackwork.forces.PhysicsTrackController;
import net.zvikasdongre.trackwork.forces.SimpleWheelController;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class TrackToolkit extends Item {
    public enum TOOL implements StringIdentifiable {
        STIFFNESS,
        OFFSET;

        private static final TOOL[] vals = values();

        public static TOOL from(int i) {
            return vals[i];
        }

        public static int next(int i) {
            return (i + 1) % vals.length;
        }

        @Override
        public @NotNull String asString() {
            return Lang.asId(name());
        }
    }

    public TrackToolkit(Settings properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.canModifyBlocks())
            return ActionResult.PASS;

        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();

        NbtCompound nbt = context.getStack().getOrCreateNbt();
        if (nbt.contains("Tool")) {
            TOOL type = TOOL.from(nbt.getInt("Tool"));

            switch (type) {
                case OFFSET -> {
                    BlockEntity be = level.getBlockEntity(pos);

                    player.playSound(TrackworkSounds.POWER_TOOL, 1.0f, player.getRandom().nextFloat() + .5f);
                    if (be instanceof SuspensionTrackBlockEntity se) {
                        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, context.getBlockPos());
                        if (ship == null) return ActionResult.FAIL;
                        se.setHorizontalOffset(VectorConversionsMCKt.toJOML(context.getHitPos().subtract(Vec3d.ofCenter(context.getBlockPos()))));

                        return ActionResult.SUCCESS;
                    } else if (be instanceof WheelBlockEntity wbe) {
                        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, context.getBlockPos());
                        if (ship == null) return ActionResult.FAIL;
                        wbe.setHorizontalOffset(VectorConversionsMCKt.toJOML(context.getHitPos().subtract(Vec3d.ofCenter(context.getBlockPos()))));

                        return ActionResult.SUCCESS;
                    }
                }
                default -> {
                    Block hitBlock = level.getBlockState(pos).getBlock();

                    player.playSound(TrackworkSounds.SPRING_TOOL, 1.0f, 0.8f + 0.4f * player.getRandom().nextFloat());

                    boolean isSneaking = player.isSneaking();
                    if (hitBlock instanceof TrackBaseBlock<?>) {
                        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, context.getBlockPos());
                        if (ship == null) return ActionResult.FAIL;
                        if (!level.isClient) {
                            PhysicsTrackController controller = PhysicsTrackController.getOrCreate((ServerShip) ship);
                            float result = controller.setDamperCoefficient(isSneaking ? -1f : 1f);

                            MutableText chatMessage = Lang.text("Adjusted suspension stiffness to ")
                                    .add(Components.literal(String.format("%.2fx", result))).component();

                            player.sendMessage(chatMessage, true);
                        }
                        return ActionResult.SUCCESS;

                    } else if (hitBlock instanceof WheelBlock) {
                        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, context.getBlockPos());
                        if (ship == null) return ActionResult.FAIL;
                        if (!level.isClient) {
                            SimpleWheelController controller = SimpleWheelController.getOrCreate((ServerShip) ship);
                            float result = controller.setDamperCoefficient(isSneaking ? -1f : 1f);

                            MutableText chatMessage = Lang.text("Adjusted suspension stiffness to ")
                                    .add(Components.literal(String.format("%.2fx", result))).component();

                            player.sendMessage(chatMessage, true);
                        }
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        return this.use(level, player, context.getHand()).getResult();
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!player.isSneaking()) {
            if (!level.isClient) nextMode(stack);
            player.getItemCooldownManager().set(this, 2);
        }

        return TypedActionResult.pass(stack);
    }

    private void nextMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!nbt.contains("Tool")) {
            nbt.putInt("Tool", 0);
        } else {
            nbt.putInt("Tool", TOOL.next(nbt.getInt("Tool")));
        }
        stack.setNbt(nbt);
    }
}
