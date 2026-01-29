/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.item.ItemRenderer
 *  net.minecraft.client.render.model.BakedModel
 *  net.minecraft.client.render.model.json.ModelTransformationMode
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.api.utils.render.SimpleItemModel;
import dev.idhammai.mod.modules.impl.render.NoRender;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemRenderer.class})
public class MixinItemRenderer {
    @Unique
    private final SimpleItemModel flattenedModel = new SimpleItemModel();
    @Unique
    private ModelTransformationMode renderMode;

    @Inject(method={"renderItem*"}, at={@At(value="HEAD")})
    private void getRenderType(ItemStack itemStack, ModelTransformationMode transformationMode, boolean leftHand, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        this.renderMode = transformationMode;
    }

    @ModifyVariable(method={"renderBakedItemModel"}, at=@At(value="HEAD"), index=1, argsOnly=true, require=0)
    private BakedModel replaceItemModelClass(BakedModel model, BakedModel arg, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fastItem.getValue() && !NoRender.INSTANCE.renderSidesOfItems.getValue() && !stack.isEmpty() && !model.hasDepth() && this.renderMode == ModelTransformationMode.GROUND) {
            this.flattenedModel.setItem(model);
            return this.flattenedModel;
        }
        return model;
    }
}

