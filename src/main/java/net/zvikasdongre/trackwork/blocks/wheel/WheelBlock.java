package net.zvikasdongre.trackwork.blocks.wheel;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class WheelBlock extends HorizontalKineticBlock implements IBE<WheelBlockEntity> {

    public static final Property<VisualVariant> VISUAL_VARIANT = EnumProperty.of("variant", VisualVariant.class);

    @Nonnull
    private final Supplier<BlockEntityType<WheelBlockEntity>> wbet;

    public WheelBlock(Settings properties) {
        super(properties);
        this.wbet = TrackworkBlockEntityTypes.SIMPLE_WHEEL;
    }

    public WheelBlock(Settings properties, Supplier<BlockEntityType<WheelBlockEntity>> wbet) {
        super(properties);
        this.wbet = wbet;
    }

    public static boolean isValid(Direction facing) {
        return !facing.getAxis().isVertical();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(VISUAL_VARIANT));
    }

    @Override
    public @NotNull ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);

        if (AllItems.WRENCH.isIn(heldItem)) {
            if (state.contains(VISUAL_VARIANT)) {
                VisualVariant old = state.get(VISUAL_VARIANT);
                switch (old) {
                    case DEFAULT -> world.setBlockState(pos, state.with(VISUAL_VARIANT, VisualVariant.NO_SPRING));
                    default -> world.setBlockState(pos, state.with(VISUAL_VARIANT, VisualVariant.DEFAULT));
                }
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isSneaking()) || preferred == null) {
            return getDefaultState().with(HORIZONTAL_FACING, context.getHorizontalPlayerFacing());
        }
        return getDefaultState().with(HORIZONTAL_FACING, preferred);
    }

    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.BLOCK;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(HORIZONTAL_FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<WheelBlockEntity> getBlockEntityClass() {
        return WheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<WheelBlockEntity> getBlockEntityType() {
        return wbet.get();
    }

    public enum VisualVariant implements StringIdentifiable {
        DEFAULT,
        NO_SPRING;

        @Override
        public @NotNull String asString() {
            return Lang.asId(name());
        }
    }
}
