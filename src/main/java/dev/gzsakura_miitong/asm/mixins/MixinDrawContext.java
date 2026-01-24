/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.mod.modules.impl.misc.ShulkerViewer;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DrawContext.class})
public class MixinDrawContext {
    @Unique
    private static final ItemStack[] ITEMS = new ItemStack[27];
    @Final
    @Shadow
    private MatrixStack matrices;

    @Inject(method={"drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"}, at={@At(value="TAIL")})
    public void onDrawItem(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        BlockItem blockItem;
        ItemStack[] itemStackArray;
        if (ShulkerViewer.INSTANCE.isOn() && ShulkerViewer.INSTANCE.icon.getValue() && stack.getItem() instanceof BlockItem && (blockItem = (BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            ShulkerViewer.getItemsInContainerItem(stack, ITEMS);
            for (ItemStack itemStack : ITEMS) {
                if (itemStack.isEmpty()) continue;
                this.matrices.push();
                this.matrices.scale(0.5f, 0.5f, 1.0f);
                this.drawItem(itemStack, x * 2 + 20, y * 2 + 20);
                this.matrices.pop();
                return;
            }
        }
    }

    @Shadow
    public void drawItem(ItemStack item, int x, int y) {
    }
}

