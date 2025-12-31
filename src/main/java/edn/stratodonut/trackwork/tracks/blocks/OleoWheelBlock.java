package edn.stratodonut.trackwork.tracks.blocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import edn.stratodonut.trackwork.TrackBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

// TODO:
public class OleoWheelBlock extends Block implements IBE<OleoWheelBlockEntity>, TransformableBlock, IWrenchable {
    public static final Property<Direction> AXLE_FACING = EnumProperty.create("axle_facing", Direction.class);
    public static final Property<Direction> STRUT_FACING = EnumProperty.create("strut_facing", Direction.class);

    public static final Property<VisualVariant> VISUAL_VARIANT = EnumProperty.create("variant", VisualVariant.class);

    public enum VisualVariant implements StringRepresentable {
        twin,
        single;

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    @Nonnull
    private final Supplier<BlockEntityType<OleoWheelBlockEntity>> wbet;

    public OleoWheelBlock(Properties properties) {
        super(properties);
        this.wbet = TrackBlockEntityTypes.OLEO_WHEEL;
    }

    public OleoWheelBlock(Properties properties, Supplier<BlockEntityType<OleoWheelBlockEntity>> wbet) {
        super(properties);
        this.wbet = wbet;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder
                .add(AXLE_FACING)
                .add(STRUT_FACING)
                .add(VISUAL_VARIANT)
        );
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        VisualVariant old = state.getValue(VISUAL_VARIANT);
        VisualVariant newVariant = switch (old) {
            case twin -> VisualVariant.single;
            case single -> VisualVariant.twin;
            default -> VisualVariant.twin;
        };
        context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(VISUAL_VARIANT, newVariant));
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Player p = context.getPlayer();
        return defaultBlockState()
                .setValue(AXLE_FACING, p != null && p.isCrouching() ? context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection())
                .setValue(STRUT_FACING, Direction.DOWN);
    }

    protected BlockState rotate(BlockState initialState, StructureTransform transform) {
        Direction initialAxle = initialState.getValue(AXLE_FACING);
        Direction initialStrut = initialState.getValue(STRUT_FACING);
        if (transform.rotationAxis == initialStrut.getAxis()) {
            return initialState.setValue(AXLE_FACING, transform.rotateFacing(initialAxle));
        } else {
            return initialState
                    .setValue(STRUT_FACING, transform.rotateFacing(initialStrut))
                    .setValue(AXLE_FACING, transform.rotateFacing(initialAxle)
                    );
        }
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        return rotate(mirror(state, transform.mirror), transform);
    }

    @Override
    public Class<OleoWheelBlockEntity> getBlockEntityClass() {
        return OleoWheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OleoWheelBlockEntity> getBlockEntityType() {
        return wbet.get();
    }
}
