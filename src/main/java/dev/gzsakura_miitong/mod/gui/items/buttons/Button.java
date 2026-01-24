/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.gzsakura_miitong.mod.gui.items.buttons;

import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.mod.gui.ClickGuiScreen;
import dev.gzsakura_miitong.mod.gui.items.Component;
import dev.gzsakura_miitong.mod.gui.items.Item;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class Button
extends Item {
    private boolean state;
    public static int hoverColor = -2007673515;
    public static int defaultColor = 0x11555555;
    public static int defaultTextColor = -5592406;
    public static int enableTextColor = -1;

    public Button(String name) {
        super(name);
        this.setHeight(15);
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        Color color = ClickGui.getInstance().activeColor.getValue();
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()) : (!this.isHovering(mouseX, mouseY) ? defaultColor : hoverColor));
        this.drawString(this.getName(), (double)(this.x + 2.3f), (double)(this.y - 2.0f - (float)ClickGuiScreen.getInstance().getTextOffset()), this.getState() ? enableTextColor : defaultTextColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        Button.sound();
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return this.height - 1;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ClickGuiScreen.getInstance().getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() && (float)mouseY >= this.getY() && (float)mouseY <= this.getY() + (float)this.height - 1.0f;
    }
}

