package net.zvikasdongre.trackwork.items;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.zvikasdongre.trackwork.Trackwork;

public class TrackToolkitRenderer extends CustomRenderedItemModelRenderer {
    protected static final PartialModel OFFSET_WRENCH = new PartialModel(Trackwork.getResource("item/kit/power_wrench"));
    protected static final PartialModel SOCKET = new PartialModel(Trackwork.getResource("item/kit/socket"));
    protected static final PartialModel STIFFNESS_WRENCH = new PartialModel(Trackwork.getResource("item/kit/stiff_tool"));

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ModelTransformationMode transformType, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (transformType == ModelTransformationMode.GUI) {
            renderer.render(model.getOriginalModel(), light);

        } else if (nbt.contains("Tool")) {
            TrackToolkit.TOOL type = TrackToolkit.TOOL.from(nbt.getInt("Tool"));

            BakedModel toolModel;
            switch (type) {
                case OFFSET -> {
                    float yOffset = 0.5f/16;
                    ms.push();
                    ms.translate(0, -yOffset, 0);
                    ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(AnimationTickHolder.getRenderTime() * 15f));
                    ms.translate(0, yOffset, 0);
                    renderer.render(SOCKET.get(), light);
                    ms.pop();

                    toolModel = OFFSET_WRENCH.get();
                }
                case STIFFNESS -> toolModel = STIFFNESS_WRENCH.get();
                default -> toolModel = model.getOriginalModel();
            }
            renderer.render(toolModel, light);

        } else {
            renderer.render(model.getOriginalModel(), light);
        }
    }
}
