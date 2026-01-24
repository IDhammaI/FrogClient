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
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class BooleanButton
extends Button {
    private final BooleanSetting setting;

    public BooleanButton(BooleanSetting setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        Color color = ClickGui.getInstance().color.getValue();
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width + 7.0f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()) : (!this.isHovering(mouseX, mouseY) ? defaultColor : hoverColor));
        this.drawString(this.getName(), (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), this.getState() ? enableTextColor : defaultTextColor);
        if (this.setting.hasParent()) {
            this.drawString(this.setting.isOpen() ? "-" : "+", (double)(this.x + (float)this.width - 1.0f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), ClickGui.getInstance().gear.getValue());
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            BooleanButton.sound();
            this.setting.setOpen(!this.setting.isOpen());
        }
    }

    @Override
    public void toggle() {
        this.setting.setValue(!this.setting.getValue());
    }

    @Override
    public boolean getState() {
        return this.setting.getValue();
    }
}

