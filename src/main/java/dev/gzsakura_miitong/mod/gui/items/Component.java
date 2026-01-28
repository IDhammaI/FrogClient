/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.gzsakura_miitong.mod.gui.items;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.utils.math.Animation;
import dev.gzsakura_miitong.api.utils.math.Easing;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.core.impl.FontManager;
import dev.gzsakura_miitong.mod.Mod;
import dev.gzsakura_miitong.mod.gui.ClickGuiScreen;
import dev.gzsakura_miitong.mod.gui.items.buttons.Button;
import dev.gzsakura_miitong.mod.gui.items.buttons.ModuleButton;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

public class Component
extends Mod {
    private final List<ModuleButton> items = new ArrayList<ModuleButton>();
    private final Module.Category category;
    public boolean drag;
    protected DrawContext context;
    private int x;
    private int y;
    private float animX;
    private float animY;
    private float mouseMoveOffsetX;
    private float mouseMoveOffsetY;
    private final Animation xAnimation = new Animation();
    private final Animation yAnimation = new Animation();
    private int x2;
    private int y2;
    private int width;
    private int height;
    private boolean open;
    private boolean hidden = false;

    public Component(String name, Module.Category category, int x, int y, boolean open) {
        super(name);
        this.category = category;
        this.setX(x);
        this.setY(y);
        this.animX = this.x;
        this.animY = this.y;
        if (ClickGui.getInstance() != null) {
            this.setWidth(ClickGui.getInstance().categoryWidth.getValueInt());
            this.setHeight(ClickGui.getInstance().categoryBarHeight.getValueInt() + 5);
        } else {
            this.setWidth(93);
            this.setHeight(18);
        }
        this.open = open;
        this.setupItems();
    }

    public void setupItems() {
    }

    private void drag(int mouseX, int mouseY) {
        if (!this.drag) {
            return;
        }
        this.x = this.x2 + mouseX;
        this.y = this.y2 + mouseY;
        this.animX = this.x;
        this.animY = this.y;
    }

    private void updatePosition() {
        if (!ClickGui.getInstance().scrollAnim.getValue() || this.drag) {
            this.animX = this.x;
            this.animY = this.y;
            this.xAnimation.from = this.x;
            this.xAnimation.to = this.x;
            this.yAnimation.from = this.y;
            this.yAnimation.to = this.y;
            return;
        }
        int length = Math.max(1, ClickGui.getInstance().scrollAnimLength.getValueInt());
        Easing easing = ClickGui.getInstance().scrollAnimEasing.getValue();
        this.animX = (float)this.xAnimation.get((double)this.x, (long)length, easing);
        this.animY = (float)this.yAnimation.get((double)this.y, (long)length, easing);
    }

    protected double getColorDelay() {
        return (double)this.getY() / 10.0;
    }

    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);
        this.updatePosition();
        int x = this.getX();
        int y = this.getY();
        float totalItemHeight = this.open ? this.getTotalItemHeight() - 2.0f : 0.0f;
        Color topColor = ColorUtil.injectAlpha(ClickGui.getInstance().getColor(this.getColorDelay()), ClickGui.getInstance().topAlpha.getValueInt());
        Render2DUtil.drawRect(context.getMatrices(), x, y, this.width, (float)this.height - 5.0f, topColor);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), x, y, this.width, (float)this.height - 5.0f, new Color(0, 0, 0, 0), new Color(ClickGui.getInstance().hoverColor.getValue().getRGB()));
        if (this.open) {
            if (ClickGui.getInstance().backGround.booleanValue) {
                Render2DUtil.drawRect(context.getMatrices(), x, (float)y + (float)this.height - 5.0f, this.width, (float)(y + this.height) + totalItemHeight - ((float)y + (float)this.height - 5.0f), ColorUtil.injectAlpha(ClickGui.getInstance().backGround.getValue(), ClickGui.getInstance().backgroundAlpha.getValueInt()));
                Render2DUtil.drawRectWithOutline(context.getMatrices(), x, (float)y + (float)this.height - 5.0f, this.width, (float)(y + this.height) + totalItemHeight - ((float)y + (float)this.height - 5.0f), new Color(0, 0, 0, 0), new Color(ClickGui.getInstance().hoverColor.getValue().getRGB()));
            }
            if (ClickGui.getInstance().line.getValue()) {
                int lineColor = ColorUtil.injectAlpha(ClickGui.getInstance().getColor(this.getColorDelay()).getRGB(), ClickGui.getInstance().topAlpha.getValueInt());
                Render2DUtil.drawLine(context.getMatrices(), (float)x + 0.2f, (float)(y + this.height) + totalItemHeight, (float)x + 0.2f, (float)y + (float)this.height - 5.0f, lineColor);
                Render2DUtil.drawLine(context.getMatrices(), x + this.width, (float)(y + this.height) + totalItemHeight, x + this.width, (float)y + (float)this.height - 5.0f, lineColor);
                Render2DUtil.drawLine(context.getMatrices(), x, (float)(y + this.height) + totalItemHeight, x + this.width, (float)(y + this.height) + totalItemHeight, lineColor);
            }
        }
        float barHeight = (float)this.height - 5.0f;
        float iconY = (float)y + (barHeight - FontManager.icon.getFontHeight()) / 2.0f;
        FontManager.icon.drawString(context.getMatrices(), this.category.getIcon(), (double)((float)x + 6.0f), (double)iconY, Button.enableTextColor);
        float nameFontHeight = ClickGui.getInstance().font.getValue() ? FontManager.ui.getFontHeight() : 9.0f;
        float nameY = (float)y + (barHeight - nameFontHeight) / 2.0f + (float)ClickGui.getInstance().titleOffset.getValueInt();
        this.drawString(this.getName(), (double)((float)x + 20.0f), (double)nameY, Button.enableTextColor);
        if (this.open) {
            float yOff = (float)(this.getY() + this.getHeight()) - 3.0f;
            for (ModuleButton item : this.getItems()) {
                if (item.isHidden()) continue;
                item.setLocation((float)x + 2.0f, yOff);
                item.setWidth(this.getWidth() - 4);
                if (item.itemHeight > 0.0 || item.subOpen) {
                    int scissorX1 = (int)item.x - 1;
                    int scissorY1 = (int)item.y - 1;
                    int scissorX2 = (int)(item.x + (float)item.getWidth() + 1.0f);
                    int scissorY2 = (int)((double)(yOff + (float)item.getButtonHeight() + 1.5f) + item.itemHeight) + 1;
                    context.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                    context.disableScissor();
                } else {
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                }
                yOff += (float)item.getButtonHeight() + 1.5f + (float)item.itemHeight;
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            if (ClickGui.getInstance().mouseMove.getValue()) {
                this.setX(this.getX());
                this.setY(this.getY());
                this.animX = this.x;
                this.animY = this.y;
                this.xAnimation.from = this.x;
                this.xAnimation.to = this.x;
                this.yAnimation.from = this.y;
                this.yAnimation.to = this.y;
            }
            this.x2 = this.getX() - mouseX;
            this.y2 = this.getY() - mouseY;
            ClickGuiScreen.getInstance().getComponents().forEach(component -> {
                if (component.drag) {
                    component.drag = false;
                }
            });
            this.drag = true;
            return;
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            Item.sound();
            return;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            this.drag = false;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void onKeyPressed(int key) {
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.onKeyPressed(key));
    }

    public void addButton(ModuleButton button) {
        this.items.add(button);
    }

    public int getX() {
        return (int)(this.animX + this.mouseMoveOffsetX);
    }

    public int getTargetX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return (int)(this.animY + this.mouseMoveOffsetY);
    }

    public int getTargetY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOpen() {
        return this.open;
    }

    public final List<ModuleButton> getItems() {
        return this.items;
    }

    public void setMouseMoveOffset(float x, float y) {
        this.mouseMoveOffsetX = x;
        this.mouseMoveOffsetY = y;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - 5;
    }

    private float getTotalItemHeight() {
        float height = 0.0f;
        for (ModuleButton item : this.getItems()) {
            item.update();
            item.itemHeight = item.animation.get(item.subOpen ? (double)item.getItemHeight() : 0.0, 200L, Easing.CubicInOut);
            height += (float)item.getButtonHeight() + 1.5f + (float)item.itemHeight;
        }
        return height;
    }

    protected void drawString(String text, double x, double y, Color color) {
        this.drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        if (ClickGui.getInstance().font.getValue()) {
            FontManager.ui.drawString(this.context.getMatrices(), text, (double)((int)x), (double)((int)y), color, ClickGui.getInstance().shadow.getValue());
        } else {
            this.context.drawText(Component.mc.textRenderer, text, (int)x, (int)y, color, ClickGui.getInstance().shadow.getValue());
        }
    }
}

