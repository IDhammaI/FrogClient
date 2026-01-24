/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.item.BuiltinModelItemRenderer
 *  net.minecraft.client.render.item.ItemRenderer
 *  net.minecraft.client.render.model.BakedModel
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ItemRenderer.class})
public interface IItemRenderer {
    @Accessor(value="builtinModelItemRenderer")
    public BuiltinModelItemRenderer hookGetBuiltinModelItemRenderer();

    @Invoker(value="renderBakedItemModel")
    public void hookRenderBakedItemModel(BakedModel var1, ItemStack var2, int var3, int var4, MatrixStack var5, VertexConsumer var6);
}

