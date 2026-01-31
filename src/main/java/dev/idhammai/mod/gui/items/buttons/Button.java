/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.idhammai.mod.gui.items.buttons;

import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.gui.ClickGuiScreen;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.gui.items.Item;
import dev.idhammai.mod.modules.impl.client.ClickGui;
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
        boolean hovered = this.isHovering(mouseX, mouseY);
        float h = (float)this.height - 0.5f;
        if (this.getState()) {
            int a = hovered ? ClickGui.getInstance().hoverAlpha.getValueInt() : ClickGui.getInstance().alpha.getValueInt();
            double baseDelay = this.getColorDelay();
            if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
                Render2DUtil.drawLutRect(context.getMatrices(), this.x, this.y, (float)this.width, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), a);
            } else {
                Color color = ClickGui.getInstance().getActiveColor(baseDelay);
                Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, ColorUtil.injectAlpha(color, a).getRGB());
            }
        } else {
            Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, !hovered ? defaultColor : hoverColor);
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        this.drawString(this.getName(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
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

