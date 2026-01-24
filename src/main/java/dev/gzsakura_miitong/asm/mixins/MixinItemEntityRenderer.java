/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.client.render.entity.EntityRendererFactory$Context
 *  net.minecraft.client.render.entity.ItemEntityRenderer
 *  net.minecraft.client.render.item.ItemRenderer
 *  net.minecraft.client.render.model.BakedModel
 *  net.minecraft.client.render.model.json.ModelTransformationMode
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.random.Random
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.mod.modules.impl.render.NoRender;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemEntityRenderer.class})
public abstract class MixinItemEntityRenderer
extends EntityRenderer<ItemEntity> {
    @Final
    @Shadow
    private ItemRenderer itemRenderer;
    @Final
    @Shadow
    private Random random;

    protected MixinItemEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Unique
    private static int getRenderedAmount(ItemStack stackSize) {
        int count = stackSize.getCount();
        if (count <= 1) {
            return 1;
        }
        if (count <= 16) {
            return 2;
        }
        if (count <= 32) {
            return 3;
        }
        return count <= 48 ? 4 : 5;
    }

    @Inject(method={"render*"}, at={@At(value="HEAD")}, cancellable=true)
    public void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        float t;
        float s;
        if (!NoRender.INSTANCE.isOn() || !NoRender.INSTANCE.fastItem.getValue()) {
            return;
        }
        matrixStack.push();
        ItemStack itemStack = itemEntity.getStack();
        long j = itemStack.isEmpty() ? 187L : (long)(Item.getRawId((Item)itemStack.getItem()) + itemStack.getDamage());
        this.random.setSeed(j);
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.getWorld(), null, itemEntity.getId());
        boolean hasDepth = bakedModel.hasDepth();
        this.shadowRadius = NoRender.INSTANCE.castShadow.getValue() ? 0.15f : 0.0f;
        float l = MathHelper.sin((float)(((float)itemEntity.getItemAge() + g) / 10.0f + itemEntity.uniqueOffset)) * 0.1f + 0.1f;
        float m = bakedModel.getTransformation().getTransformation((ModelTransformationMode)ModelTransformationMode.GROUND).scale.y();
        matrixStack.translate(0.0f, l + 0.25f * m, 0.0f);
        matrixStack.multiply(this.dispatcher.getRotation());
        float o = bakedModel.getTransformation().ground.scale.x();
        float p = bakedModel.getTransformation().ground.scale.y();
        float q = bakedModel.getTransformation().ground.scale.z();
        int renderedAmount = MixinItemEntityRenderer.getRenderedAmount(itemStack);
        if (!hasDepth) {
            float r = -0.0f * (float)(renderedAmount - 1) * 0.5f * o;
            s = -0.0f * (float)(renderedAmount - 1) * 0.5f * p;
            t = -0.09375f * (float)(renderedAmount - 1) * 0.5f * q;
            matrixStack.translate(r, s, t);
        }
        for (int u = 0; u < renderedAmount; ++u) {
            matrixStack.push();
            if (u > 0) {
                if (hasDepth) {
                    s = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    t = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float v = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    matrixStack.translate(s, t, v);
                } else {
                    s = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    t = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrixStack.translate(s, t, 0.0f);
                }
            }
            this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
            if (hasDepth) continue;
            matrixStack.translate(0.0f * o, 0.0f * p, 0.0425f * q);
        }
        matrixStack.pop();
        super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
        ci.cancel();
    }
}

