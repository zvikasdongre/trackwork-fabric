package net.zvikasdongre.trackwork;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TrackworkItemGroups {
    public static final ItemGroup TRACKWORK_ITEMGROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(AllBlocks.BELT))
            .displayName(Text.translatable("itemGroup.trackwork"))
            .entries((displayContext, entries) -> {
                for (RegistryEntry<Block> entry : Trackwork.REGISTRATE.getAll(RegistryKeys.BLOCK)) {
                    if (CreateRegistrate.isInCreativeTab(entry, AllCreativeModeTabs.BASE_CREATIVE_TAB.key()))
                        entries.add(entry.get().asItem());
                }

                for (RegistryEntry<Item> entry : Trackwork.REGISTRATE.getAll(RegistryKeys.ITEM)) {
                    if (CreateRegistrate.isInCreativeTab(entry, AllCreativeModeTabs.BASE_CREATIVE_TAB.key()))
                        entries.add(entry.get());
                }
            })
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, new Identifier(Trackwork.MOD_ID, "item_group"), TRACKWORK_ITEMGROUP);
    }
}
