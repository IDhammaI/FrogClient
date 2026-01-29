/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.Block
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.BookScreen
 *  net.minecraft.client.gui.screen.ingame.BookScreen$Contents
 *  net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.inventory.Inventory
 *  net.minecraft.inventory.SimpleInventory
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.screen.ShulkerBoxScreenHandler
 *  net.minecraft.util.DyeColor
 *  net.minecraft.util.Identifier
 */
package dev.idhammai.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.mod.modules.impl.misc.ShulkerViewer;
import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class PeekScreen
extends ShulkerBoxScreen {
    private final Identifier TEXTURE = Identifier.of((String)"textures/gui/container/shulker_box.png");
    private final ItemStack[] contents;
    private final ItemStack storageBlock;

    public PeekScreen(ItemStack storageBlock, ItemStack[] contents) {
        super(new ShulkerBoxScreenHandler(0, Wrapper.mc.player.getInventory(), (Inventory)new SimpleInventory(contents)), Wrapper.mc.player.getInventory(), storageBlock.getName());
        this.contents = contents;
        this.storageBlock = storageBlock;
    }

    public static Color getShulkerColor(ItemStack shulkerItem) {
        BlockItem blockItem;
        Block block;
        Item item = shulkerItem.getItem();
        if (item instanceof BlockItem && (block = (blockItem = (BlockItem)item).getBlock()) instanceof ShulkerBoxBlock) {
            ShulkerBoxBlock shulkerBlock = (ShulkerBoxBlock)block;
            DyeColor dye = shulkerBlock.getColor();
            if (dye == null) {
                return Color.WHITE;
            }
            int color = dye.getEntityColor();
            return new Color(color);
        }
        return Color.WHITE;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 && this.focusedSlot != null && !this.focusedSlot.getStack().isEmpty() && Wrapper.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            ItemStack itemStack = this.focusedSlot.getStack();
            if (ShulkerViewer.hasItems(itemStack) || itemStack.getItem() == Items.ENDER_CHEST) {
                return ShulkerViewer.openContainer(this.focusedSlot.getStack(), this.contents, false);
            }
            if (itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) != null || itemStack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT) != null) {
                this.close();
                Wrapper.mc.setScreen((Screen)new BookScreen(BookScreen.Contents.create((ItemStack)itemStack)));
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || Wrapper.mc.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return false;
    }

    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        Color color = PeekScreen.getShulkerColor(this.storageBlock);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(this.TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }
}

