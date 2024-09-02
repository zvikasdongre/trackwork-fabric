package net.zvikasdongre.trackwork.blocks.suspension;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.zvikasdongre.trackwork.TrackworkBlockEntityTypes;
import net.zvikasdongre.trackwork.blocks.TrackBaseBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;

public class SuspensionTrackBlock extends TrackBaseBlock<SuspensionTrackBlockEntity> {
//    public static DamageSource damageSourceTrack = new DamageSource("trackwork.track");

    public static final Property<TrackVariant> WHEEL_VARIANT = EnumProperty.of("variant", TrackVariant.class);

    

    public SuspensionTrackBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(PART, TrackBaseBlock.TrackPart.NONE).with(WHEEL_VARIANT, SuspensionTrackBlock.TrackVariant.WHEEL));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(WHEEL_VARIANT));
    }

    @NotNull
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);

        if (AllItems.WRENCH.isIn(heldItem)) {
            if (state.contains(WHEEL_VARIANT)) {
                TrackVariant old = state.get(WHEEL_VARIANT);
                switch (old) {
                    case WHEEL -> world.setBlockState(pos, state.with(WHEEL_VARIANT, TrackVariant.BLANK));
                    default -> world.setBlockState(pos, state.with(WHEEL_VARIANT, TrackVariant.WHEEL));
                }
                ;
                return ActionResult.SUCCESS;
            }
        };
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    public Class<SuspensionTrackBlockEntity> getBlockEntityClass() {
        return SuspensionTrackBlockEntity.class;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getOutlineShape(
            BlockState state,
            BlockView view,
            BlockPos pos,
            ShapeContext context
    ) {
        return VoxelShapes.fullCube();
    }

    public BlockEntityType<? extends SuspensionTrackBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.SUSPENSION_TRACK.get();
    }

    public static enum TrackVariant implements StringIdentifiable {
        WHEEL,
        WHEEL_ROLLER,
        ROLLER,
        BLANK;

        @NotNull
        public String asString() {
            return Lang.asId(this.name());
        }
    }
}
