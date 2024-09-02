package net.zvikasdongre.trackwork;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveGenerator;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.zvikasdongre.trackwork.blocks.TrackAdjusterBlock;
import net.zvikasdongre.trackwork.blocks.sproket.SprocketBlock;
import net.zvikasdongre.trackwork.blocks.sproket.variants.LargeSprocketBlock;
import net.zvikasdongre.trackwork.blocks.sproket.variants.MedSprocketBlock;
import net.zvikasdongre.trackwork.blocks.suspension.variants.LargeSuspensionTrackBlock;
import net.zvikasdongre.trackwork.blocks.suspension.variants.MedSuspensionTrackBlock;
import net.zvikasdongre.trackwork.blocks.suspension.SuspensionTrackBlock;
import net.zvikasdongre.trackwork.blocks.wheel.WheelBlock;
import org.jetbrains.annotations.NotNull;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static net.zvikasdongre.trackwork.Trackwork.REGISTRATE;

public class TrackworkBlocks {
    public static final BlockEntry<SuspensionTrackBlock> SUSPENSION_TRACK =
            REGISTRATE.block("suspension_track", SuspensionTrackBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(12.0f).sounds(BlockSoundGroup.METAL))
                    .properties(AbstractBlock.Settings::nonOpaque)
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
                    .addLayer(() -> RenderLayer::getCutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<LargeSuspensionTrackBlock> LARGE_SUSPENSION_TRACK =
            REGISTRATE.block("large_suspension_track", LargeSuspensionTrackBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(12.0f).sounds(BlockSoundGroup.METAL))
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
                    .addLayer(() -> RenderLayer::getCutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<MedSuspensionTrackBlock> MED_SUSPENSION_TRACK =
            REGISTRATE.block("med_suspension_track", MedSuspensionTrackBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(12.0f).sounds(BlockSoundGroup.METAL))
                    .properties(AbstractBlock.Settings::nonOpaque)
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
                    .addLayer(() -> RenderLayer::getCutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<SprocketBlock> SPROCKET_TRACK =
            REGISTRATE.block("sprocket_track", SprocketBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(14.0f).sounds(BlockSoundGroup.METAL))
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

    public static final BlockEntry<LargeSprocketBlock> LARGE_SPROCKET_TRACK =
            REGISTRATE.block("large_sprocket_track", LargeSprocketBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(14.0f).sounds(BlockSoundGroup.METAL))
                    .properties(AbstractBlock.Settings::nonOpaque)
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .addLayer(() -> RenderLayer::getCutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<MedSprocketBlock> MED_SPROCKET_TRACK =
            REGISTRATE.block("med_sprocket_track", MedSprocketBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(14.0f).sounds(BlockSoundGroup.METAL))
                    .properties(AbstractBlock.Settings::nonOpaque)
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .addLayer(() -> RenderLayer::getCutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<TrackAdjusterBlock> TRACK_LEVEL_CONTROLLER =
            REGISTRATE.block("track_level_controller", TrackAdjusterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.BROWN))
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.axisBlockProvider(true))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<WheelBlock> SIMPLE_WHEEL =
            REGISTRATE.block("simple_wheel", WheelBlock::new)
                    .initialProperties(() -> Blocks.RAIL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(7.0f).sounds(BlockSoundGroup.METAL))
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(pickaxeOnly())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
//                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<? extends RotatedPillarKineticBlock> SIMPLE_WHEEL_PART =
            REGISTRATE.block("simple_wheel_part", (properties) -> new RotatedPillarKineticBlock(properties) {
                        @Override
                        public Direction.Axis getRotationAxis(BlockState state) {
                            return null;
                        }

                        @Override
                        public @NotNull VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
                            return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
                        }
                    })
                    .initialProperties(() -> Blocks.WHITE_WOOL)
                    .properties(p -> p.mapColor(MapColor.BROWN).noCollision().strength(2.0f, 7.0f).sounds(BlockSoundGroup.WOOL))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static void initialize() {
    }
}