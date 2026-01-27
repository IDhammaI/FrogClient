/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.util.Formatting
 */
package dev.gzsakura_miitong.mod.gui.items.buttons;

import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.mod.gui.ClickGuiScreen;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

public class EnumButton
extends Button {
    public final EnumSetting<?> setting;
    private boolean open;

    public EnumButton(EnumSetting<?> setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        Color color = ClickGui.getInstance().getColor(this.getColorDelay());
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width + 7.0f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()) : (!this.isHovering(mouseX, mouseY) ? defaultColor : hoverColor));
        this.drawString(this.setting.getName() + " " + String.valueOf(Formatting.GRAY) + (((Enum)this.setting.getValue()).name().equalsIgnoreCase("ABC") ? "ABC" : ((Enum)this.setting.getValue()).name()), (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), this.getState() ? enableTextColor : defaultTextColor);
        if (this.open) {
            int y = (int)this.y;
            for (Enum e : (Enum[])((Enum)this.setting.getValue()).getDeclaringClass().getEnumConstants()) {
                String s = e.name();
                this.drawString(s, (double)((float)this.width / 2.0f - (float)this.getWidth(s) / 2.0f + 2.0f + this.x), (double)((float)y + (float)this.height - 3.0f - (float)ClickGuiScreen.getInstance().getTextOffset()), ((Enum)this.setting.getValue()).name().equals(s) ? enableTextColor : defaultTextColor);
                y += 11;
            }
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
            this.open = !this.open;
            EnumButton.sound();
        } else if (mouseButton == 0 && this.open) {
            int y = (int)this.y;
            for (Enum o : (Enum[])((Enum)this.setting.getValue()).getDeclaringClass().getEnumConstants()) {
                if ((float)mouseX > this.x && (float)mouseX < this.x + (float)this.width && mouseY >= y + this.height + 1 && mouseY < y + this.height + 11 + 1) {
                    this.setting.setEnumValue(String.valueOf(o));
                    EnumButton.sound();
                }
                y += 11;
            }
        }
    }

    @Override
    public int getHeight() {
        return super.getHeight() + (this.open ? 11 * ((Enum[])((Enum)this.setting.getValue()).getDeclaringClass().getEnumConstants()).length : 0);
    }

    @Override
    public void toggle() {
        this.setting.increaseEnum();
    }

    @Override
    public boolean getState() {
        return true;
    }
}

