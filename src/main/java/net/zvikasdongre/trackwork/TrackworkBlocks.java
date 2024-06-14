package net.zvikasdongre.trackwork;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveGenerator;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class TrackworkBlocks {
    public static final BlockEntry<SuspensionTrackBlock> SUSPENSION_TRACK =
        Trackwork.REGISTRATE.block("suspension_track", SuspensionTrackBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.BROWN))
            .properties(AbstractBlock.Settings::nonOpaque)
            .transform(BlockStressDefaults.setNoImpact())
            .transform(axeOrPickaxe())
            .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
            .addLayer(() -> RenderLayer::getCutoutMipped)
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<SprocketBlock> SPROCKET_TRACK =
            Trackwork.REGISTRATE.block("sprocket_track", SprocketBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.BROWN))
                    .properties(AbstractBlock.Settings::nonOpaque)
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(axeOrPickaxe())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p)
                    )
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .addLayer(() -> RenderLayer::getCutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static <T extends Block> T register(T block, String name, boolean shouldRegisterItem) {
        Identifier id = new Identifier(Trackwork.MOD_ID, name);

        if(shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {}
}