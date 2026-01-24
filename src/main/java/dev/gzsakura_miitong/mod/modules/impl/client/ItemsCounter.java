/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.PistonBlock
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package dev.gzsakura_miitong.mod.modules.impl.client;

import dev.gzsakura_miitong.api.utils.player.InventoryUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemsCounter
extends Module {
    public static ItemsCounter INSTANCE;
    private final BooleanSetting hideEmpty = this.add(new BooleanSetting("HideEmpty", true));
    private final BooleanSetting crystal = this.add(new BooleanSetting("Crystal", true));
    private final BooleanSetting xp = this.add(new BooleanSetting("XP", true));
    private final BooleanSetting pearl = this.add(new BooleanSetting("Pearl", true));
    private final BooleanSetting obsidian = this.add(new BooleanSetting("Obsidian", true));
    private final BooleanSetting egApple = this.add(new BooleanSetting("E-GApple", true));
    private final BooleanSetting gApple = this.add(new BooleanSetting("GApple", true));
    private final BooleanSetting totem = this.add(new BooleanSetting("Totem", true));
    private final BooleanSetting web = this.add(new BooleanSetting("Web", true));
    private final BooleanSetting anchor = this.add(new BooleanSetting("Anchor", true));
    private final BooleanSetting glowstone = this.add(new BooleanSetting("Glowstone", true));
    private final BooleanSetting piston = this.add(new BooleanSetting("Piston", true));
    private final BooleanSetting redstone = this.add(new BooleanSetting("RedStone", true));
    private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true));
    private final BooleanSetting firework = this.add(new BooleanSetting("Firework", true));
    private final SliderSetting xOffset = this.add(new SliderSetting("X", 100, 0, 1500));
    private final SliderSetting yOffset = this.add(new SliderSetting("Y", 100, 0, 1000));
    private final SliderSetting offset = this.add(new SliderSetting("Offset", 18, 0, 30));
    private final ItemStack crystalStack = new ItemStack((ItemConvertible)Items.END_CRYSTAL);
    private final ItemStack xpStack = new ItemStack((ItemConvertible)Items.EXPERIENCE_BOTTLE);
    private final ItemStack pearlStack = new ItemStack((ItemConvertible)Items.ENDER_PEARL);
    private final ItemStack obsidianStack = new ItemStack((ItemConvertible)Items.OBSIDIAN);
    private final ItemStack eGappleStack = new ItemStack((ItemConvertible)Items.ENCHANTED_GOLDEN_APPLE);
    private final ItemStack gappleStack = new ItemStack((ItemConvertible)Items.GOLDEN_APPLE);
    private final ItemStack totemStack = new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING);
    private final ItemStack webStack = new ItemStack((ItemConvertible)Items.COBWEB);
    private final ItemStack anchorStack = new ItemStack((ItemConvertible)Items.RESPAWN_ANCHOR);
    private final ItemStack glowstoneStack = new ItemStack((ItemConvertible)Items.GLOWSTONE);
    private final ItemStack pistonStack = new ItemStack((ItemConvertible)Items.PISTON);
    private final ItemStack redstoneStack = new ItemStack((ItemConvertible)Items.REDSTONE_BLOCK);
    private final ItemStack enderChestStack = new ItemStack((ItemConvertible)Items.ENDER_CHEST);
    private final ItemStack fireworkStack = new ItemStack((ItemConvertible)Items.FIREWORK_ROCKET);
    int x;
    int y;
    DrawContext drawContext;

    public ItemsCounter() {
        super("Items", Module.Category.Client);
        this.setChinese("\u7269\u54c1\u6570\u91cf");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        int pistonCount;
        this.drawContext = drawContext;
        this.x = this.xOffset.getValueInt() - this.offset.getValueInt();
        this.y = this.yOffset.getValueInt();
        if (this.crystal.getValue()) {
            this.crystalStack.setCount(this.getItemCount(Items.END_CRYSTAL));
            this.drawItem(this.crystalStack);
        }
        if (this.xp.getValue()) {
            this.xpStack.setCount(this.getItemCount(Items.EXPERIENCE_BOTTLE));
            this.drawItem(this.xpStack);
        }
        if (this.pearl.getValue()) {
            this.pearlStack.setCount(this.getItemCount(Items.ENDER_PEARL));
            this.drawItem(this.pearlStack);
        }
        if (this.obsidian.getValue()) {
            this.obsidianStack.setCount(this.getItemCount(Items.OBSIDIAN));
            this.drawItem(this.obsidianStack);
        }
        if (this.egApple.getValue()) {
            this.eGappleStack.setCount(this.getItemCount(Items.ENCHANTED_GOLDEN_APPLE));
            this.drawItem(this.eGappleStack);
        }
        if (this.gApple.getValue()) {
            this.gappleStack.setCount(this.getItemCount(Items.GOLDEN_APPLE));
            this.drawItem(this.gappleStack);
        }
        if (this.totem.getValue()) {
            this.totemStack.setCount(this.getItemCount(Items.TOTEM_OF_UNDYING));
            this.drawItem(this.totemStack);
        }
        if (this.web.getValue()) {
            this.webStack.setCount(this.getItemCount(Items.COBWEB));
            this.drawItem(this.webStack);
        }
        if (this.anchor.getValue()) {
            this.anchorStack.setCount(this.getItemCount(Items.RESPAWN_ANCHOR));
            this.drawItem(this.anchorStack);
        }
        if (this.glowstone.getValue()) {
            this.glowstoneStack.setCount(this.getItemCount(Items.GLOWSTONE));
            this.drawItem(this.glowstoneStack);
        }
        if (this.piston.getValue() && ((pistonCount = InventoryUtil.getItemCount(PistonBlock.class)) > 0 || !this.hideEmpty.getValue())) {
            this.x += this.offset.getValueInt();
            this.pistonStack.setCount(Math.max(1, pistonCount));
            this.drawItem(this.pistonStack);
        }
        if (this.redstone.getValue()) {
            this.redstoneStack.setCount(this.getItemCount(Items.REDSTONE_BLOCK));
            this.drawItem(this.redstoneStack);
        }
        if (this.enderChest.getValue()) {
            this.enderChestStack.setCount(this.getItemCount(Items.ENDER_CHEST));
            this.drawItem(this.enderChestStack);
        }
        if (this.firework.getValue()) {
            this.fireworkStack.setCount(this.getItemCount(Items.FIREWORK_ROCKET));
            this.drawItem(this.fireworkStack);
        }
    }

    private int getItemCount(Item item) {
        int i = InventoryUtil.getItemCount(item);
        if (this.hideEmpty.getValue() && i == 0) {
            return 0;
        }
        this.x += this.offset.getValueInt();
        return Math.max(i, 1);
    }

    private void drawItem(ItemStack itemStack) {
        this.drawContext.drawItem(itemStack, this.x, this.y);
        this.drawContext.drawItemInSlot(ItemsCounter.mc.textRenderer, itemStack, this.x, this.y);
    }
}

