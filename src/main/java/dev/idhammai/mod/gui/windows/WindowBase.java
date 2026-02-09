/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.util.Identifier
 */
package dev.idhammai.mod.gui.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.utils.math.AnimateUtil;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class WindowBase {
    private final String name;
    private final Identifier icon;
    private float x;
    private float y;
    private float width;
    private float height;
    private float dragX;
    private float dragY;
    private float scrollOffset;
    private float prevScrollOffset;
    private float maxElementsHeight;
    private boolean dragging;
    private boolean hoveringWindow;
    private boolean scaling;
    private boolean scrolling;
    private boolean visible = true;

    protected WindowBase(float x, float y, float width, float height, String name, Identifier icon) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
        this.name = name;
        this.icon = icon;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (this.dragging && WindowsScreen.draggingWindow == this) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
        } else if (this.scaling && WindowsScreen.lastClickedWindow == this) {
            float nextWidth = mouseX - this.dragX;
            float nextHeight = mouseY - this.dragY;
            this.width = Math.max(120.0f, nextWidth);
            this.height = Math.max(80.0f, nextHeight);
        }
        this.prevScrollOffset = AnimateUtil.fast(this.prevScrollOffset, this.scrollOffset, 12.0f);
        ClickGui gui = ClickGui.getInstance();
        Color color2 = new Color(-983868581, true);
        RenderSystem.enableBlend();
        float headerH = gui != null ? gui.categoryBarHeight.getValueInt() : 14.0f;
        float headerX = this.x;
        float headerY = this.y;
        float headerW = this.width;
        int topAlpha = gui != null ? gui.topAlpha.getValueInt() : 210;
        if (gui != null && gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            Render2DUtil.drawLutRect(context.getMatrices(), headerX, headerY, headerW, headerH, gui.getSpectrumLutId(), gui.getSpectrumLutHeight(), topAlpha);
        } else {
            Color topColor = gui != null ? ColorUtil.injectAlpha(gui.getColor(((double)headerY) / 10.0), topAlpha) : new Color(0, 120, 212, topAlpha);
            Render2DUtil.drawRect(context.getMatrices(), headerX, headerY, headerW, headerH, topColor);
        }
        if (gui == null || gui.backgroundStyle.getValue() != ClickGui.BackgroundStyle.Transparent) {
            int outline = gui != null ? gui.hoverColor.getValue().getRGB() : new Color(50, 50, 50, 200).getRGB();
            Render2DUtil.drawRectWithOutline(context.getMatrices(), headerX, headerY, headerW, headerH, new Color(0, 0, 0, 0), new Color(outline));
        }
        if (gui == null || gui.backGround.booleanValue) {
            int bgAlpha = gui != null ? gui.backgroundAlpha.getValueInt() : 236;
            Color bg = gui != null ? ColorUtil.injectAlpha(gui.backGround.getValue(), bgAlpha) : new Color(30, 30, 30, bgAlpha);
            Render2DUtil.drawRect(context.getMatrices(), this.x, this.y + headerH, this.width, this.height - headerH, bg);
            int outline = gui != null ? gui.hoverColor.getValue().getRGB() : new Color(50, 50, 50, 200).getRGB();
            Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x, this.y + headerH, this.width, this.height - headerH, new Color(0, 0, 0, 0), new Color(outline));
        }
        float nameFontHeight = FontManager.isCustomFontEnabled() ? FontManager.ui.getFontHeight() : 9.0f;
        float nameY = headerY + (headerH - nameFontHeight) / 2.0f + (gui != null ? (float)gui.titleOffset.getValueInt() : 0.0f);
        FontManager.ui.drawString(context.getMatrices(), this.name, (double)(this.x + 6.0f), (double)nameY, -1);
        float closeY = this.y + (headerH - 10.0f) / 2.0f;
        boolean hover1 = Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width - 14.0f, closeY, 10.0, 10.0);
        int closeBg = gui != null ? (hover1 ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : (hover1 ? new Color(-982026377, true).getRGB() : new Color(-984131753, true).getRGB());
        Render2DUtil.rect(context.getMatrices(), this.x + this.width - 14.0f, closeY, this.x + this.width - 4.0f, closeY + 10.0f, closeBg);
        float scrollTop = this.getScrollTop(headerH);
        float scrollH = this.getScrollHeight(headerH);
        float scrollBarW = this.getScrollBarWidth();
        float trackX = this.x + this.width - scrollBarW;
        float maxH = Math.max(1.0f, this.maxElementsHeight);
        float ratio = scrollH / maxH;
        float thumbH = Math.min(scrollH, scrollH * ratio);
        float thumbY = scrollTop - this.scrollOffset * ratio;
        float maxThumbY = scrollTop + scrollH - thumbH;
        if (thumbY < scrollTop) {
            thumbY = scrollTop;
        }
        if (thumbY > maxThumbY) {
            thumbY = maxThumbY;
        }
        boolean hover2 = Render2DUtil.isHovered(mouseX, mouseY, trackX, scrollTop, scrollBarW, scrollH);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), trackX, scrollTop, scrollBarW, scrollH, hover2 ? new Color(1595085587, true) : new Color(0x5F000000, true), color2);
        Render2DUtil.drawRect(context.getMatrices(), trackX, thumbY, scrollBarW, thumbH, new Color(-1590611663, true));
        float closeLeft = this.x + this.width - 14.0f;
        float closeTop = closeY;
        float closeRight = closeLeft + 10.0f;
        float closeBottom = closeTop + 10.0f;
        String closeText = "x";
        float closeTextX = closeLeft + (10.0f - (float)FontManager.ui.getWidth(closeText)) / 2.0f;
        float closeTextY = closeTop + (10.0f - FontManager.ui.getFontHeight()) / 2.0f;
        FontManager.ui.drawString(context.getMatrices(), closeText, (double)closeTextX, (double)closeTextY, -1);
        RenderSystem.disableBlend();
        if (this.scrolling) {
            float diff = ((float)mouseY - scrollTop) / scrollH;
            float scrollRange = this.getScrollRange(scrollH);
            this.scrollOffset = -(diff * scrollRange);
            this.scrollOffset = MathUtil.clamp(this.scrollOffset, -scrollRange, 0.0f);
        }
        this.hoveringWindow = Render2DUtil.isHovered(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        Render2DUtil.drawLine(context.getMatrices(), this.getX() + this.getWidth(), this.getY() + this.getHeight() - 3.0f, this.getX() + this.getWidth() + 7.0f, this.getY() + this.getHeight() - 10.0f, color2.getRGB());
        Render2DUtil.drawLine(context.getMatrices(), this.getX() + this.getWidth() + 5.0f, this.getY() + this.getHeight() - 3.0f, this.getX() + this.getWidth() + 7.0f, this.getY() + this.getHeight() - 5.0f, color2.getRGB());
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        ClickGui gui = ClickGui.getInstance();
        float headerH = gui != null ? gui.categoryBarHeight.getValueInt() : 14.0f;
        float closeY = this.y + (headerH - 10.0f) / 2.0f;
        if (Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width - 14.0f, closeY, 10.0, 10.0)) {
            this.setVisible(false);
            return;
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, headerH)) {
            if (WindowsScreen.draggingWindow == null) {
                this.dragging = true;
            }
            if (WindowsScreen.draggingWindow == null) {
                WindowsScreen.draggingWindow = this;
            }
            WindowsScreen.lastClickedWindow = this;
            this.dragX = (int)(mouseX - (double)this.getX());
            this.dragY = (int)(mouseY - (double)this.getY());
            return;
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width, this.y + this.height - 10.0f, 10.0, 10.0)) {
            WindowsScreen.lastClickedWindow = this;
            this.dragX = (int)(mouseX - (double)this.getWidth());
            this.dragY = (int)(mouseY - (double)this.getHeight());
            this.scaling = true;
            return;
        }
        float scrollTop = this.getScrollTop(headerH);
        float scrollH = this.getScrollHeight(headerH);
        float scrollBarW = this.getScrollBarWidth();
        float trackX = this.x + this.width - scrollBarW;
        if (Render2DUtil.isHovered(mouseX, mouseY, trackX, scrollTop, scrollBarW, scrollH)) {
            WindowsScreen.lastClickedWindow = this;
            this.dragX = (int)(mouseX - (double)this.getWidth());
            this.dragY = (int)(mouseY - (double)this.getHeight());
            this.scrolling = true;
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    public void charTyped(char key, int keyCode) {
    }

    public void mouseScrolled(int i) {
        if (this.hoveringWindow) {
            this.scrollOffset += (float)(i * 16);
            ClickGui gui = ClickGui.getInstance();
            float headerH = gui != null ? gui.categoryBarHeight.getValueInt() : 14.0f;
            float scrollH = this.getScrollHeight(headerH);
            float scrollRange = this.getScrollRange(scrollH);
            this.scrollOffset = MathUtil.clamp(this.scrollOffset, -scrollRange, 0.0f);
        }
    }

    protected float getScrollTop(float headerH) {
        return this.y + headerH + 3.0f;
    }

    protected float getScrollHeight(float headerH) {
        return Math.max(1.0f, this.getHeight() - headerH - 6.0f);
    }

    protected float getScrollRange(float scrollH) {
        return Math.max(0.0f, this.maxElementsHeight - scrollH);
    }

    protected float getScrollBarWidth() {
        return 6.0f;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        this.scaling = false;
        this.scrolling = false;
        WindowsScreen.draggingWindow = null;
    }

    public float getX() {
        return this.x;
    }

    public void setPosition(float x, float y) {
        this.setX(x);
        this.setY(y);
    }

    protected void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    protected void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return this.width;
    }

    protected void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height;
    }

    protected void setHeight(float height) {
        this.height = height;
    }

    protected float getScrollOffset() {
        return this.prevScrollOffset;
    }

    protected void resetScroll() {
        this.prevScrollOffset = 0.0f;
        this.scrollOffset = 0.0f;
    }

    protected void setMaxElementsHeight(float maxElementsHeight) {
        this.maxElementsHeight = maxElementsHeight;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Identifier getIcon() {
        return this.icon;
    }
}
