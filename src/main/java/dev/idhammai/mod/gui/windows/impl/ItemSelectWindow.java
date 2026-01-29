/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.Block
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.resource.language.I18n
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.item.Item
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.StringHelper
 */
package dev.idhammai.mod.gui.windows.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.core.Manager;
import dev.idhammai.core.impl.CleanerManager;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.core.impl.TradeManager;
import dev.idhammai.core.impl.XrayManager;
import dev.idhammai.mod.gui.items.buttons.StringButton;
import dev.idhammai.mod.gui.windows.WindowBase;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.StringHelper;

public class ItemSelectWindow
extends WindowBase {
    private final Manager manager;
    private final ArrayList<ItemPlate> itemPlates = new ArrayList();
    private final ArrayList<ItemPlate> allItems = new ArrayList();
    private boolean allTab = true;
    private boolean listening = false;
    private String search = "Search";

    public ItemSelectWindow(Manager manager) {
        this((float)Wrapper.mc.getWindow().getScaledWidth() / 2.0f - 100.0f, (float)Wrapper.mc.getWindow().getScaledHeight() / 2.0f - 150.0f, 200.0f, 300.0f, manager);
    }

    public ItemSelectWindow(float x, float y, float width, float height, Manager manager) {
        super(x, y, width, height, "Items", null);
        this.manager = manager;
        this.refreshItemPlates();
        int id1 = 0;
        for (Block block : Registries.BLOCK) {
            this.allItems.add(new ItemPlate(id1, id1 * 20, block.asItem(), block.getTranslationKey()));
            ++id1;
        }
        for (Item item : Registries.ITEM) {
            this.allItems.add(new ItemPlate(id1, id1 * 20, item, item.getTranslationKey()));
            ++id1;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        super.render(context, mouseX, mouseY);
        boolean hover1 = Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 90.0f, this.getY() + 3.0f, 70.0, 10.0);
        Render2DUtil.drawRect(context.getMatrices(), this.getX() + this.getWidth() - 90.0f, this.getY() + 3.0f, 70.0f, 10.0f, hover1 ? new Color(-981236861, true) : new Color(-984131753, true));
        FontManager.small.drawString(context.getMatrices(), this.search, (double)(this.getX() + this.getWidth() - 86.0f), (double)(this.getY() + 7.0f), new Color(0xD5D5D5).getRGB());
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        int tabColor1 = this.allTab ? new Color(0xD5D5D5).getRGB() : Color.GRAY.getRGB();
        int tabColor2 = this.allTab ? Color.GRAY.getRGB() : new Color(0xBDBDBD).getRGB();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(this.getX() + 1.5f, this.getY() + 29.0f, 0.0f).color(Color.DARK_GRAY.getRGB());
        bufferBuilder.vertex(this.getX() + 8.0f, this.getY() + 29.0f, 0.0f).color(tabColor1);
        bufferBuilder.vertex(this.getX() + 8.0f, this.getY() + 19.0f, 0.0f).color(tabColor1);
        bufferBuilder.vertex(this.getX() + 48.0f, this.getY() + 19.0f, 0.0f).color(tabColor1);
        bufferBuilder.vertex(this.getX() + 54.0f, this.getY() + 29.0f, 0.0f).color(tabColor1);
        bufferBuilder.vertex(this.getX() + 52.0f, this.getY() + 25.0f, 0.0f).color(tabColor2);
        bufferBuilder.vertex(this.getX() + 52.0f, this.getY() + 19.0f, 0.0f).color(tabColor2);
        bufferBuilder.vertex(this.getX() + 92.0f, this.getY() + 19.0f, 0.0f).color(tabColor2);
        bufferBuilder.vertex(this.getX() + 100.0f, this.getY() + 29.0f, 0.0f).color(Color.GRAY.getRGB());
        bufferBuilder.vertex(this.getX() + this.getWidth() - 1.0f, this.getY() + 29.0f, 0.0f).color(Color.DARK_GRAY.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        FontManager.small.drawString(context.getMatrices(), "All", (double)(this.getX() + 25.0f), (double)(this.getY() + 25.0f), tabColor1);
        FontManager.small.drawString(context.getMatrices(), "Selected", (double)(this.getX() + 60.0f), (double)(this.getY() + 25.0f), tabColor2);
        if (!this.allTab && this.itemPlates.isEmpty()) {
            FontManager.ui.drawCenteredString(context.getMatrices(), "It's empty here yet", (double)(this.getX() + this.getWidth() / 2.0f), (double)(this.getY() + this.getHeight() / 2.0f), new Color(0xBDBDBD).getRGB());
        }
        context.enableScissor((int)this.getX(), (int)(this.getY() + 30.0f), (int)(this.getX() + this.getWidth()), (int)(this.getY() + this.getHeight() - 1.0f));
        for (ItemPlate itemPlate : this.allTab ? this.allItems : this.itemPlates) {
            if (itemPlate.offset + this.getY() + 25.0f + this.getScrollOffset() > this.getY() + this.getHeight() || itemPlate.offset + this.getScrollOffset() + this.getY() + 10.0f < this.getY()) continue;
            context.getMatrices().push();
            context.getMatrices().translate(this.getX() + 6.0f, itemPlate.offset + this.getY() + 32.0f + this.getScrollOffset(), 0.0f);
            context.drawItem(itemPlate.item().getDefaultStack(), 0, 0);
            context.getMatrices().pop();
            FontManager.ui.drawString(context.getMatrices(), I18n.translate((String)itemPlate.key(), (Object[])new Object[0]), (double)(this.getX() + 26.0f), (double)(itemPlate.offset + this.getY() + 38.0f + this.getScrollOffset()), new Color(0xBDBDBD).getRGB());
            boolean hover2 = Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 20.0f, itemPlate.offset + this.getY() + 35.0f + this.getScrollOffset(), 11.0, 11.0);
            Render2DUtil.drawRect(context.getMatrices(), this.getX() + this.getWidth() - 20.0f, itemPlate.offset + this.getY() + 35.0f + this.getScrollOffset(), 11.0f, 11.0f, hover2 ? new Color(-981828998, true) : new Color(-984131753, true));
            boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key, itemPlate.key));
            if (this.allTab && !selected) {
                FontManager.ui.drawString(context.getMatrices(), "+", (double)(this.getX() + this.getWidth() - 17.0f), (double)(itemPlate.offset + this.getY() + 37.0f + this.getScrollOffset()), -1);
                continue;
            }
            FontManager.ui.drawString(context.getMatrices(), "-", (double)(this.getX() + this.getWidth()) - 16.5, (double)(itemPlate.offset + this.getY()) + 37.5 + (double)this.getScrollOffset(), -1);
        }
        this.setMaxElementsHeight((this.allTab ? this.allItems : this.itemPlates).size() * 20);
        context.disableScissor();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (Render2DUtil.isHovered(mouseX, mouseY, this.getX() + 8.0f, this.getY() + 19.0f, 52.0, 19.0)) {
            this.allTab = true;
            this.resetScroll();
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.getX() + 54.0f, this.getY() + 19.0f, 70.0, 19.0)) {
            this.allTab = false;
            this.resetScroll();
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 90.0f, this.getY() + 3.0f, 70.0, 10.0)) {
            this.listening = true;
            this.search = "";
        }
        ArrayList<ItemPlate> copy = Lists.newArrayList(this.allTab ? this.allItems : this.itemPlates);
        for (ItemPlate itemPlate : copy) {
            XrayManager m;
            CleanerManager m2;
            TradeManager m3;
            Manager manager;
            if ((float)((int)(itemPlate.offset + this.getY() + 50.0f)) + this.getScrollOffset() > this.getY() + this.getHeight()) continue;
            String name = itemPlate.key().replace("item.minecraft.", "").replace("block.minecraft.", "");
            if (!Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 20.0f, itemPlate.offset + this.getY() + 35.0f + this.getScrollOffset(), 10.0, 10.0)) continue;
            boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key(), itemPlate.key));
            if (this.allTab && !selected) {
                manager = this.manager;
                if (manager instanceof TradeManager) {
                    m3 = (TradeManager)manager;
                    if (m3.inWhitelist(name)) continue;
                    m3.add(name);
                    this.refreshItemPlates();
                    continue;
                }
                manager = this.manager;
                if (manager instanceof CleanerManager) {
                    m2 = (CleanerManager)manager;
                    if (m2.inList(name)) continue;
                    m2.add(name);
                    this.refreshItemPlates();
                    continue;
                }
                manager = this.manager;
                if (!(manager instanceof XrayManager) || (m = (XrayManager)manager).inWhitelist(name)) continue;
                m.add(name);
                this.refreshItemPlates();
                continue;
            }
            manager = this.manager;
            if (manager instanceof TradeManager) {
                m3 = (TradeManager)manager;
                m3.remove(name);
            } else {
                manager = this.manager;
                if (manager instanceof CleanerManager) {
                    m2 = (CleanerManager)manager;
                    m2.remove(name);
                } else {
                    manager = this.manager;
                    if (manager instanceof XrayManager) {
                        m = (XrayManager)manager;
                        m.remove(name);
                    }
                }
            }
            this.refreshItemPlates();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 70 && (InputUtil.isKeyPressed((long)Wrapper.mc.getWindow().getHandle(), (int)341) || InputUtil.isKeyPressed((long)Wrapper.mc.getWindow().getHandle(), (int)345))) {
            this.listening = !this.listening;
            return;
        }
        if (this.listening) {
            switch (keyCode) {
                case 256: {
                    this.listening = false;
                    this.search = "Search";
                    this.refreshAllItems();
                    break;
                }
                case 259: {
                    this.search = StringButton.removeLastChar(this.search);
                    this.refreshAllItems();
                    if (!Objects.equals(this.search, "")) break;
                    this.listening = false;
                    this.search = "Search";
                    break;
                }
                case 32: {
                    this.search = this.search + " ";
                }
            }
        }
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar((char)key) && this.listening) {
            this.search = this.search + key;
            this.refreshAllItems();
        }
    }

    private void refreshItemPlates() {
        this.itemPlates.clear();
        int id = 0;
        for (Item item : Registries.ITEM) {
            XrayManager m;
            Manager manager = this.manager;
            if (manager instanceof TradeManager) {
                TradeManager m2 = (TradeManager)manager;
                if (!m2.inWhitelist(item.getTranslationKey())) continue;
                this.itemPlates.add(new ItemPlate(id, id * 20, item.asItem(), item.getTranslationKey()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (manager instanceof CleanerManager) {
                CleanerManager m3 = (CleanerManager)manager;
                if (!m3.inList(item.getTranslationKey())) continue;
                this.itemPlates.add(new ItemPlate(id, id * 20, item.asItem(), item.getTranslationKey()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (!(manager instanceof XrayManager) || !(m = (XrayManager)manager).inWhitelist(item.getTranslationKey())) continue;
            this.itemPlates.add(new ItemPlate(id, id * 20, item.asItem(), item.getTranslationKey()));
            ++id;
        }
    }

    private void refreshAllItems() {
        this.allItems.clear();
        this.resetScroll();
        int id1 = 0;
        for (Item item : Registries.ITEM) {
            if (!this.search.equals("Search") && !this.search.isEmpty() && !item.getTranslationKey().contains(this.search) && !item.getName().getString().toLowerCase().contains(this.search.toLowerCase())) continue;
            this.allItems.add(new ItemPlate(id1, id1 * 20, item, item.getTranslationKey()));
            ++id1;
        }
    }

    private record ItemPlate(float id, float offset, Item item, String key) {
    }
}

