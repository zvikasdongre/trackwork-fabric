package edn.stratodonut.trackwork;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

import static edn.stratodonut.trackwork.TrackworkMod.REGISTRATE;
import static net.minecraft.network.chat.Component.translatable;

public class TrackCreativeTabs {
    public static final AllCreativeModeTabs.TabInfo BASE_CREATIVE_TAB = register("base",
            () -> FabricItemGroup.builder()
                    .title(translatable("itemGroup.trackwork"))
                    .icon(TrackBlocks.SIMPLE_WHEEL_PART::asStack)
                    .displayItems((displayParams, output) -> {
                        for (RegistryEntry<Item> entry : REGISTRATE.getAll(Registries.ITEM)) {
                            if (CreateRegistrate.isInCreativeTab(entry, AllCreativeModeTabs.BASE_CREATIVE_TAB.key()))
                                output.accept(entry.get());
                        }
                    })
                    .build());

    private static AllCreativeModeTabs.TabInfo register(String name, Supplier<CreativeModeTab> supplier) {
        ResourceLocation id = TrackworkMod.getResource(name);
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id);
        CreativeModeTab tab = supplier.get();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
        return new AllCreativeModeTabs.TabInfo(key, tab);
    }

    public static void register() {
    }
}
