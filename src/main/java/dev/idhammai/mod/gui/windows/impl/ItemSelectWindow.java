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
import dev.idhammai.api.utils.math.AnimateUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.core.Manager;
import dev.idhammai.core.impl.CleanerManager;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.core.impl.HudItemManager;
import dev.idhammai.core.impl.TradeManager;
import dev.idhammai.core.impl.XrayManager;
import dev.idhammai.mod.gui.items.buttons.StringButton;
import dev.idhammai.mod.gui.windows.WindowsScreen;
import dev.idhammai.mod.gui.windows.WindowBase;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
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
    private final StringSetting searchSetting;
    private final StringButton searchButton;
    private boolean allTab = true;
    private float tabY;
    private float tabH;
    private float tabAllX;
    private float tabAllW;
    private float tabSelX;
    private float tabSelW;
    private float listTop;
    private float tabAnimX;
    private float tabAnimW;
    private boolean tabAnimInit;

    public ItemSelectWindow(Manager manager) {
        this((float)Wrapper.mc.getWindow().getScaledWidth() / 2.0f - 100.0f, (float)Wrapper.mc.getWindow().getScaledHeight() / 2.0f - 150.0f, 200.0f, 300.0f, manager);
    }

    public ItemSelectWindow(float x, float y, float width, float height, Manager manager) {
        super(x, y, width, height, getTitleLabel(), null);
        this.manager = manager;
        this.searchSetting = new StringSetting(getSearchLabel(), "").injectTask(this::refreshAllItems);
        this.searchButton = new StringButton(this.searchSetting);
        this.refreshItemPlates();
        float rowH = this.getRowHeight();
        int id1 = 0;
        for (Block block : Registries.BLOCK) {
            this.allItems.add(new ItemPlate(id1, id1 * rowH, block.asItem(), block.getTranslationKey()));
            ++id1;
        }
        for (Item item : Registries.ITEM) {
            this.allItems.add(new ItemPlate(id1, id1 * rowH, item, item.getTranslationKey()));
            ++id1;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        this.layoutTabs();
        super.render(context, mouseX, mouseY);
        this.layoutSearchButton();
        this.searchButton.drawScreen(context, mouseX, mouseY, 0.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        ClickGui gui = ClickGui.getInstance();
        boolean shadow = FontManager.isShadowEnabled();
        boolean hoverAll = Render2DUtil.isHovered(mouseX, mouseY, this.tabAllX, this.tabY, this.tabAllW, this.tabH);
        boolean hoverSel = Render2DUtil.isHovered(mouseX, mouseY, this.tabSelX, this.tabY, this.tabSelW, this.tabH);
        int defaultBg = gui != null ? gui.defaultColor.getValue().getRGB() : new Color(30, 30, 30, 236).getRGB();
        int hoverBg = gui != null ? gui.hoverColor.getValue().getRGB() : new Color(50, 50, 50, 200).getRGB();
        int defaultText = gui != null ? gui.defaultTextColor.getValue().getRGB() : new Color(220, 220, 220).getRGB();
        int enableText = gui != null ? gui.enableTextColor.getValue().getRGB() : -1;
        Render2DUtil.rect(context.getMatrices(), this.tabAllX, this.tabY, this.tabAllX + this.tabAllW, this.tabY + this.tabH, hoverAll ? hoverBg : defaultBg);
        Render2DUtil.rect(context.getMatrices(), this.tabSelX, this.tabY, this.tabSelX + this.tabSelW, this.tabY + this.tabH, hoverSel ? hoverBg : defaultBg);
        float targetX = this.allTab ? this.tabAllX : this.tabSelX;
        float targetW = this.allTab ? this.tabAllW : this.tabSelW;
        float dt = AnimateUtil.deltaTime();
        if (dt <= 0.0f) {
            dt = 0.016f;
        }
        float a = dt * 18.0f;
        if (a < 0.0f) {
            a = 0.0f;
        }
        if (a > 0.35f) {
            a = 0.35f;
        }
        boolean dragging = WindowsScreen.draggingWindow == this;
        if (!this.tabAnimInit || dragging) {
            this.tabAnimX = targetX;
            this.tabAnimW = targetW;
            this.tabAnimInit = true;
        } else {
            this.tabAnimX += (targetX - this.tabAnimX) * a;
            this.tabAnimW += (targetW - this.tabAnimW) * a;
        }
        boolean hoverActive = this.allTab ? hoverAll : hoverSel;
        int activeAlpha = gui != null ? (hoverActive ? gui.hoverAlpha.getValueInt() : gui.alpha.getValueInt()) : 200;
        if (gui != null && gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            Render2DUtil.drawLutRect(context.getMatrices(), this.tabAnimX, this.tabY, this.tabAnimW, this.tabH, gui.getSpectrumLutId(), gui.getSpectrumLutHeight(), activeAlpha);
        } else {
            Color ac = gui != null ? gui.getActiveColor((double)this.tabY * 0.25) : new Color(0, 120, 212);
            Render2DUtil.rect(context.getMatrices(), this.tabAnimX, this.tabY, this.tabAnimX + this.tabAnimW, this.tabY + this.tabH, ColorUtil.injectAlpha(ac, activeAlpha).getRGB());
        }
        int allText = this.allTab || hoverAll ? enableText : defaultText;
        int selText = !this.allTab || hoverSel ? enableText : defaultText;
        float tabTextY = this.tabY + (this.tabH - FontManager.small.getFontHeight()) / 2.0f;
        String allLabel = getAllLabel();
        String selectedLabel = getSelectedLabel();
        FontManager.small.drawString(context.getMatrices(), allLabel, (double)(this.tabAllX + (this.tabAllW - (float)FontManager.small.getWidth(allLabel)) / 2.0f), (double)tabTextY, allText, shadow);
        FontManager.small.drawString(context.getMatrices(), selectedLabel, (double)(this.tabSelX + (this.tabSelW - (float)FontManager.small.getWidth(selectedLabel)) / 2.0f), (double)tabTextY, selText, shadow);
        if (!this.allTab && this.itemPlates.isEmpty()) {
            FontManager.ui.drawCenteredString(context.getMatrices(), getEmptyLabel(), (double)(this.getX() + this.getWidth() / 2.0f), (double)(this.getY() + this.getHeight() / 2.0f), new Color(0xBDBDBD).getRGB());
        }
        float rowH = this.getRowHeight();
        float iconSize = 16.0f;
        float leftPad = rowH * 0.3f;
        float textGap = rowH * 0.3f;
        float buttonSize = Math.max(8.0f, rowH * 0.5f);
        float rightPad = rowH * 0.25f + this.getScrollBarWidth();
        context.enableScissor((int)this.getX(), (int)this.listTop, (int)(this.getX() + this.getWidth()), (int)(this.getY() + this.getHeight() - 1.0f));
        for (ItemPlate itemPlate : this.allTab ? this.allItems : this.itemPlates) {
            float itemY = itemPlate.offset + this.listTop + this.getScrollOffset();
            if (itemY > this.getY() + this.getHeight() || itemY + rowH < this.getY()) continue;
            context.getMatrices().push();
            context.getMatrices().translate(this.getX() + leftPad, itemY + (rowH - iconSize) / 2.0f, 0.0f);
            context.drawItem(itemPlate.item().getDefaultStack(), 0, 0);
            context.getMatrices().pop();
            float textY = itemY + (rowH - FontManager.ui.getFontHeight()) / 2.0f;
            float textX = this.getX() + leftPad + iconSize + textGap;
            FontManager.ui.drawString(context.getMatrices(), I18n.translate((String)itemPlate.key(), (Object[])new Object[0]), (double)textX, (double)textY, new Color(0xBDBDBD).getRGB(), shadow);
            float buttonX = this.getX() + this.getWidth() - rightPad - buttonSize;
            float buttonY = itemY + (rowH - buttonSize) / 2.0f;
            boolean hover2 = Render2DUtil.isHovered(mouseX, mouseY, buttonX, buttonY, buttonSize, buttonSize);
            Render2DUtil.drawRect(context.getMatrices(), buttonX, buttonY, buttonSize, buttonSize, hover2 ? new Color(-981828998, true) : new Color(-984131753, true));
            boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key, itemPlate.key));
            if (this.allTab && !selected) {
                FontManager.ui.drawString(context.getMatrices(), "+", (double)(buttonX + (buttonSize - (float)FontManager.ui.getWidth("+")) / 2.0f), (double)(buttonY + (buttonSize - FontManager.ui.getFontHeight()) / 2.0f), -1, shadow);
                continue;
            }
            FontManager.ui.drawString(context.getMatrices(), "-", (double)(buttonX + (buttonSize - (float)FontManager.ui.getWidth("-")) / 2.0f), (double)(buttonY + (buttonSize - FontManager.ui.getFontHeight()) / 2.0f), -1, shadow);
        }
        this.setMaxElementsHeight((this.allTab ? this.allItems : this.itemPlates).size() * rowH);
        context.disableScissor();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.layoutSearchButton();
        this.layoutTabs();
        if (this.searchButton.isHovering((int)mouseX, (int)mouseY)) {
            this.searchButton.mouseClicked((int)mouseX, (int)mouseY, button);
            return;
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.tabAllX, this.tabY, this.tabAllW, this.tabH)) {
            this.allTab = true;
            this.resetScroll();
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.tabSelX, this.tabY, this.tabSelW, this.tabH)) {
            this.allTab = false;
            this.resetScroll();
        }
        float rowH = this.getRowHeight();
        float buttonSize = Math.max(8.0f, rowH * 0.5f);
        float rightPad = rowH * 0.25f + this.getScrollBarWidth();
        ArrayList<ItemPlate> copy = Lists.newArrayList(this.allTab ? this.allItems : this.itemPlates);
        for (ItemPlate itemPlate : copy) {
            XrayManager m;
            CleanerManager m2;
            HudItemManager m4;
            TradeManager m3;
            Manager manager;
            float itemY = itemPlate.offset + this.listTop + this.getScrollOffset();
            if (itemY + rowH > this.getY() + this.getHeight()) continue;
            String name = itemPlate.key().replace("item.minecraft.", "").replace("block.minecraft.", "");
            float buttonX = this.getX() + this.getWidth() - rightPad - buttonSize;
            float buttonY = itemY + (rowH - buttonSize) / 2.0f;
            if (!Render2DUtil.isHovered(mouseX, mouseY, buttonX, buttonY, buttonSize, buttonSize)) continue;
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
                if (manager instanceof HudItemManager) {
                    m4 = (HudItemManager)manager;
                    if (m4.inList(name)) continue;
                    m4.add(name);
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
                } else if (manager instanceof HudItemManager) {
                    m4 = (HudItemManager)manager;
                    m4.remove(name);
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
            this.searchButton.toggle();
            return;
        }
        this.searchButton.onKeyPressed(keyCode);
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar((char)key)) {
            this.searchButton.onKeyTyped(key, keyCode);
        }
    }

    private void refreshItemPlates() {
        this.itemPlates.clear();
        float rowH = this.getRowHeight();
        int id = 0;
        for (Item item : Registries.ITEM) {
            XrayManager m;
            Manager manager = this.manager;
            if (manager instanceof TradeManager) {
                TradeManager m2 = (TradeManager)manager;
                if (!m2.inWhitelist(item.getTranslationKey())) continue;
                this.itemPlates.add(new ItemPlate(id, id * rowH, item.asItem(), item.getTranslationKey()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (manager instanceof CleanerManager) {
                CleanerManager m3 = (CleanerManager)manager;
                if (!m3.inList(item.getTranslationKey())) continue;
                this.itemPlates.add(new ItemPlate(id, id * rowH, item.asItem(), item.getTranslationKey()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (manager instanceof HudItemManager) {
                HudItemManager m4 = (HudItemManager)manager;
                if (!m4.inList(item.getTranslationKey())) continue;
                this.itemPlates.add(new ItemPlate(id, id * rowH, item.asItem(), item.getTranslationKey()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (!(manager instanceof XrayManager) || !(m = (XrayManager)manager).inWhitelist(item.getTranslationKey())) continue;
            this.itemPlates.add(new ItemPlate(id, id * rowH, item.asItem(), item.getTranslationKey()));
            ++id;
        }
    }

    private void refreshAllItems() {
        this.allItems.clear();
        this.resetScroll();
        float rowH = this.getRowHeight();
        int id1 = 0;
        for (Item item : Registries.ITEM) {
            String search = this.searchSetting.getValue();
            if (!search.isEmpty() && !item.getTranslationKey().contains(search) && !item.getName().getString().toLowerCase().contains(search.toLowerCase())) continue;
            this.allItems.add(new ItemPlate(id1, id1 * rowH, item, item.getTranslationKey()));
            ++id1;
        }
    }

    private float getRowHeight() {
        float iconSize = 16.0f;
        float fontH = FontManager.ui.getFontHeight();
        return Math.max(iconSize, fontH) + Math.max(4.0f, fontH * 0.4f);
    }

    private void layoutSearchButton() {
        ClickGui gui = ClickGui.getInstance();
        float headerH = gui != null ? gui.categoryBarHeight.getValueInt() : 14.0f;
        float rightPadding = headerH;
        float boxW = Math.min(this.getWidth() * 0.4f, this.getWidth() - rightPadding - headerH * 0.5f);
        int buttonW = Math.max((int)(boxW * 0.85f), (int)(headerH * 2.2f));
        int buttonH = Math.max(9, (int)(headerH * 0.7f));
        this.searchButton.setWidth(buttonW);
        this.searchButton.setHeight(buttonH);
        this.searchButton.setLocation(this.getX() + this.getWidth() - rightPadding - boxW, this.getY() + (headerH - (float)buttonH) / 2.0f);
    }

    private void layoutTabs() {
        ClickGui gui = ClickGui.getInstance();
        float headerH = gui != null ? gui.categoryBarHeight.getValueInt() : 14.0f;
        float gapX = 0.0f;
        float gapY = Math.max(2.0f, headerH * 0.25f);
        float fontH = FontManager.small.getFontHeight();
        float padX = fontH;
        float minW = fontH * 2.5f;
        this.tabH = Math.max(7.0f, headerH * 0.8f);
        this.tabY = this.getY() + headerH + gapY;
        String allLabel = getAllLabel();
        String selectedLabel = getSelectedLabel();
        this.tabAllW = Math.max(minW, (float)FontManager.small.getWidth(allLabel) + padX);
        this.tabSelW = Math.max(minW, (float)FontManager.small.getWidth(selectedLabel) + padX);
        float leftPad = headerH * 0.6f;
        this.tabAllX = this.getX() + leftPad;
        this.tabSelX = this.tabAllX + this.tabAllW + gapX;
        this.listTop = this.tabY + this.tabH + gapY;
    }

    @Override
    protected float getScrollTop(float headerH) {
        return this.listTop;
    }

    @Override
    protected float getScrollHeight(float headerH) {
        float bottom = this.getY() + this.getHeight() - 1.0f;
        return Math.max(1.0f, bottom - this.listTop);
    }

    private static boolean isChinese() {
        return ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
    }

    private static String getTitleLabel() {
        return isChinese() ? "\u7269\u54c1" : "Items";
    }

    private static String getSearchLabel() {
        return isChinese() ? "\u641c\u7d22" : "Search";
    }

    private static String getAllLabel() {
        return isChinese() ? "\u5168\u90e8" : "All";
    }

    private static String getSelectedLabel() {
        return isChinese() ? "\u5df2\u9009" : "Selected";
    }

    private static String getEmptyLabel() {
        return isChinese() ? "\u8fd9\u91cc\u8fd8\u6ca1\u6709\u5185\u5bb9" : "It's empty here yet";
    }

    private record ItemPlate(float id, float offset, Item item, String key) {
    }
}
