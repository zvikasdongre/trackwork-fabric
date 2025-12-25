package edn.stratodonut.trackwork.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import edn.stratodonut.trackwork.blocks.HornBlock;
import edn.stratodonut.trackwork.blocks.HornBlockEntity;
import edn.stratodonut.trackwork.client.TrackworkPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HornBlockEntityRenderer<T extends HornBlockEntity> extends SafeBlockEntityRenderer<T> {

    public HornBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction dir = state.getValue(HornBlock.FACING);

        boolean powered = be.getPowered();

        Vec3 shakeVector = powered ? shake() : Vec3.ZERO;

        SuperByteBuffer pipe = CachedBuffers.partial(TrackworkPartialModels.HORN_PIPE, state);
        pipe.center()
                .translate(shakeVector.x, shakeVector.y, shakeVector.z)
                .rotateYDegrees(180 - dir.toYRot())

                .translate(0, -4/16f, 4/16f)
                .scale(powered ? 0.75f : 1, powered ? 0.75f : 1, powered ? 1.5f : 1)
                .translate(0, 4/16f, -4/16f)
                .uncenter();

        pipe.light(light).renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
    }

    public Vec3 shake() {
        long currentTime = System.currentTimeMillis();
        // Magic numbers
        return new Vec3(Math.sin(currentTime/2.), Math.cos(currentTime/2. + 7_000), Math.sin(currentTime/2. + 33_000))
                .scale(0.5/16f);
    }
}
