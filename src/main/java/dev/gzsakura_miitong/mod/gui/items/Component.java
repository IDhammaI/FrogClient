/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.gzsakura_miitong.mod.gui.items;

import dev.gzsakura_miitong.Alien;
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
    }

    protected double getColorDelay() {
        return (double)this.y / 10.0;
    }

    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);
        float totalItemHeight = this.open ? this.getTotalItemHeight() - 2.0f : 0.0f;
        Color topColor = ColorUtil.injectAlpha(ClickGui.getInstance().getColor(this.getColorDelay()), ClickGui.getInstance().topAlpha.getValueInt());
        Render2DUtil.drawRect(context.getMatrices(), this.x, this.y, this.width, (float)this.height - 5.0f, topColor);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x, this.y, this.width, (float)this.height - 5.0f, new Color(0, 0, 0, 0), new Color(ClickGui.getInstance().hoverColor.getValue().getRGB()));
        if (this.open) {
            if (ClickGui.getInstance().backGround.booleanValue) {
                Render2DUtil.drawRect(context.getMatrices(), this.x, (float)this.y + (float)this.height - 5.0f, this.width, (float)(this.y + this.height) + totalItemHeight - ((float)this.y + (float)this.height - 5.0f), ColorUtil.injectAlpha(ClickGui.getInstance().backGround.getValue(), ClickGui.getInstance().backgroundAlpha.getValueInt()));
                Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x, (float)this.y + (float)this.height - 5.0f, this.width, (float)(this.y + this.height) + totalItemHeight - ((float)this.y + (float)this.height - 5.0f), new Color(0, 0, 0, 0), new Color(ClickGui.getInstance().hoverColor.getValue().getRGB()));
            }
            if (ClickGui.getInstance().line.getValue()) {
                int lineColor = ColorUtil.injectAlpha(ClickGui.getInstance().getColor(this.getColorDelay()).getRGB(), ClickGui.getInstance().topAlpha.getValueInt());
                Render2DUtil.drawLine(context.getMatrices(), (float)this.x + 0.2f, (float)(this.y + this.height) + totalItemHeight, (float)this.x + 0.2f, (float)this.y + (float)this.height - 5.0f, lineColor);
                Render2DUtil.drawLine(context.getMatrices(), this.x + this.width, (float)(this.y + this.height) + totalItemHeight, this.x + this.width, (float)this.y + (float)this.height - 5.0f, lineColor);
                Render2DUtil.drawLine(context.getMatrices(), this.x, (float)(this.y + this.height) + totalItemHeight, this.x + this.width, (float)(this.y + this.height) + totalItemHeight, lineColor);
            }
        }
        float barHeight = (float)this.height - 5.0f;
        float iconY = (float)this.y + (barHeight - FontManager.icon.getFontHeight()) / 2.0f;
        FontManager.icon.drawString(context.getMatrices(), this.category.getIcon(), (double)((float)this.x + 6.0f), (double)iconY, Button.enableTextColor);
        float nameFontHeight = ClickGui.getInstance().font.getValue() ? FontManager.ui.getFontHeight() : 9.0f;
        float nameY = (float)this.y + (barHeight - nameFontHeight) / 2.0f + (float)ClickGui.getInstance().titleOffset.getValueInt();
        this.drawString(this.getName(), (double)((float)this.x + 20.0f), (double)nameY, Button.enableTextColor);
        if (this.open) {
            float y = (float)(this.getY() + this.getHeight()) - 3.0f;
            for (ModuleButton item : this.getItems()) {
                if (item.isHidden()) continue;
                item.setLocation((float)this.x + 2.0f, y);
                item.setWidth(this.getWidth() - 4);
                if (item.itemHeight > 0.0 || item.subOpen) {
                    int scissorX1 = (int)item.x - 1;
                    int scissorY1 = (int)item.y - 1;
                    int scissorX2 = (int)(item.x + (float)item.getWidth() + 1.0f);
                    int scissorY2 = (int)((double)(y + (float)item.getButtonHeight() + 1.5f) + item.itemHeight) + 1;
                    context.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                    context.disableScissor();
                } else {
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                }
                y += (float)item.getButtonHeight() + 1.5f + (float)item.itemHeight;
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
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
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
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

