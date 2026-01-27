/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.util.Identifier
 */
package dev.gzsakura_miitong.mod.gui.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.api.utils.math.AnimateUtil;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.core.impl.FontManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
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

    protected void render(DrawContext context, int mouseX, int mouseY) {
        this.prevScrollOffset = AnimateUtil.fast(this.prevScrollOffset, this.scrollOffset, 12.0f);
        Color color2 = new Color(-983868581, true);
        RenderSystem.enableBlend();
        Render2DUtil.drawRect(context.getMatrices(), this.x, this.y, this.width + 10.0f, this.height, -1072689136);
        Render2DUtil.drawRect(context.getMatrices(), this.x + 0.5f, this.y, this.width + 9.0f, 16.0f, new Color(0x5F000000, true));
        Color accent = ClickGui.getInstance().getColor(((double)this.x + (double)this.y) / 20.0);
        Render2DUtil.horizontalGradient(context.getMatrices(), this.x + 2.0f, this.y + 16.0f, this.x + 2.0f + this.width / 2.0f - 2.0f, this.y + 16.5f, ColorUtil.injectAlpha(accent, 0), accent);
        Render2DUtil.horizontalGradient(context.getMatrices(), this.x + 2.0f + this.width / 2.0f - 2.0f, this.y + 16.0f, this.x + 2.0f + this.width - 4.0f, this.y + 16.5f, accent, ColorUtil.injectAlpha(accent, 0));
        FontManager.ui.drawString(context.getMatrices(), this.name, (double)(this.x + 4.0f), (double)(this.y + 5.5f), -1);
        boolean hover1 = Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width - 4.0f, this.y + 3.0f, 10.0, 10.0);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x + this.width - 4.0f, this.y + 3.0f, 10.0f, 10.0f, hover1 ? new Color(-982026377, true) : new Color(-984131753, true), color2);
        float ratio = (this.getHeight() - 35.0f) / this.maxElementsHeight;
        boolean hover2 = Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width, this.y + 19.0f, 6.0, this.getHeight() - 34.0f);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x + this.width, this.y + 19.0f, 6.0f, this.getHeight() - 34.0f, hover2 ? new Color(1595085587, true) : new Color(0x5F000000, true), color2);
        Render2DUtil.drawRect(context.getMatrices(), this.x + this.width, Math.max(this.y + 19.0f - this.scrollOffset * ratio, this.y + 19.0f), 6.0f, Math.min((this.getHeight() - 34.0f) * ratio, this.getHeight() - 34.0f), new Color(-1590611663, true));
        Render2DUtil.drawLine(context.getMatrices(), this.x + this.width - 2.0f, this.y + 5.0f, this.x + this.width + 4.0f, this.y + 11.0f, -1);
        Render2DUtil.drawLine(context.getMatrices(), this.x + this.width - 2.0f, this.y + 11.0f, this.x + this.width + 4.0f, this.y + 5.0f, -1);
        RenderSystem.disableBlend();
        if (this.scrolling) {
            float diff = ((float)mouseY - this.y - 19.0f) / (this.getHeight() - 34.0f);
            this.scrollOffset = -(diff * this.maxElementsHeight);
            this.scrollOffset = MathUtil.clamp(this.scrollOffset, -this.maxElementsHeight + (this.getHeight() - 40.0f), 0.0f);
        }
        this.hoveringWindow = Render2DUtil.isHovered(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        Render2DUtil.drawLine(context.getMatrices(), this.getX() + this.getWidth(), this.getY() + this.getHeight() - 3.0f, this.getX() + this.getWidth() + 7.0f, this.getY() + this.getHeight() - 10.0f, color2.getRGB());
        Render2DUtil.drawLine(context.getMatrices(), this.getX() + this.getWidth() + 5.0f, this.getY() + this.getHeight() - 3.0f, this.getX() + this.getWidth() + 7.0f, this.getY() + this.getHeight() - 5.0f, color2.getRGB());
    }

    protected void mouseClicked(double mouseX, double mouseY, int button) {
        if (Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width - 4.0f, this.y + 3.0f, 10.0, 10.0)) {
            this.setVisible(false);
            return;
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, 10.0)) {
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
        if (Render2DUtil.isHovered(mouseX, mouseY, this.x + this.width, this.y + 19.0f, 6.0, this.getHeight() - 34.0f)) {
            WindowsScreen.lastClickedWindow = this;
            this.dragX = (int)(mouseX - (double)this.getWidth());
            this.dragY = (int)(mouseY - (double)this.getHeight());
            this.scrolling = true;
        }
    }

    protected void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    protected void charTyped(char key, int keyCode) {
    }

    protected void mouseScrolled(int i) {
        if (this.hoveringWindow) {
            this.scrollOffset += (float)(i * 2);
            this.scrollOffset = MathUtil.clamp(this.scrollOffset, -this.maxElementsHeight + (this.getHeight() - 40.0f), 0.0f);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        this.scaling = false;
        this.scrolling = false;
        WindowsScreen.draggingWindow = null;
    }

    protected float getX() {
        return this.x;
    }

    protected void setX(float x) {
        this.x = x;
    }

    protected float getY() {
        return this.y;
    }

    protected void setY(float y) {
        this.y = y;
    }

    protected float getWidth() {
        return this.width;
    }

    protected void setWidth(float width) {
        this.width = width;
    }

    protected float getHeight() {
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

