package net.zvikasdongre.trackwork.items;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.zvikasdongre.trackwork.forces.PhysicsEntityTrackController;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ControllerResetStick extends Item {
    public ControllerResetStick(Settings properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.canModifyBlocks())
            return super.useOnBlock(context);

        World level = context.getWorld();
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, context.getBlockPos());
        if (ship == null) return ActionResult.FAIL;
        if (!level.isClient) {
            PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate((ServerShip) ship);
            controller.resetController();

            MutableText chatMessage = Lang.text("Fix! ").component();
            player.sendMessage(chatMessage, true);
        }

        return ActionResult.SUCCESS;
    }
}
