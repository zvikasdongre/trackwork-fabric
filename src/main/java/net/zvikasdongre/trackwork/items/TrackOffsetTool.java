package net.zvikasdongre.trackwork.items;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class TrackOffsetTool extends Item {
    public TrackOffsetTool(Settings properties) {
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
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof SuspensionTrackBlockEntity se) {
            Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, context.getBlockPos());
            if (ship == null) return ActionResult.FAIL;
            se.setHorizontalOffset(VectorConversionsMCKt.toJOML(context.getHitPos().subtract(Vec3d.ofCenter(context.getBlockPos()))));

            return ActionResult.SUCCESS;
        }

        return this.use(level, player, context.getHand()).getResult();
    }
}
