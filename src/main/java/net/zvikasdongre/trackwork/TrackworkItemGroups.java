package net.zvikasdongre.trackwork;

import com.simibubi.create.AllBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TrackworkItemGroups {
    public static final ItemGroup TRACKWORK_ITEMGROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(AllBlocks.BELT))
            .displayName(Text.translatable("itemGroup.trackwork"))
            .entries((displayContext, entries) -> {
                entries.add(TrackworkBlocks.SUSPENSION_TRACK.asItem());
                entries.add(TrackworkBlocks.MED_SUSPENSION_TRACK.asItem());
                entries.add(TrackworkBlocks.LARGE_SUSPENSION_TRACK.asItem());
                entries.add(TrackworkBlocks.SPROCKET_TRACK.asItem());
                entries.add(TrackworkBlocks.MED_SPROCKET_TRACK.asItem());
                entries.add(TrackworkBlocks.LARGE_SPROCKET_TRACK.asItem());
                entries.add(TrackworkBlocks.SIMPLE_WHEEL.asItem());
                entries.add(TrackworkBlocks.SIMPLE_WHEEL_PART.asItem());
                entries.add(TrackworkBlocks.TRACK_LEVEL_CONTROLLER.asItem());
                entries.add(TrackworkItems.TRACK_TOOL_KIT.asItem());
                entries.add(TrackworkItems.CONTROL_RESET_STICK.asItem());
            })
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, new Identifier(Trackwork.MOD_ID, "item_group"), TRACKWORK_ITEMGROUP);
    }
}
