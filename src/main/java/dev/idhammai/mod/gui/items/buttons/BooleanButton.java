/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.idhammai.mod.gui.items.buttons;

import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.gui.clickgui.ClickGuiScreen;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

public class BooleanButton
extends Button {
    private final BooleanSetting setting;
    private final Animation toggleAnimation = new Animation();

    public BooleanButton(BooleanSetting setting) {
        super(setting.getName());
        this.setting = setting;
        double initial = setting.getValue() ? 1.0 : 0.0;
        this.toggleAnimation.from = initial;
        this.toggleAnimation.to = initial;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        double toggleProgress = this.toggleAnimation.get(this.getState() ? 1.0 : 0.0, 160L, Easing.CubicInOut);
        double baseDelay = this.getColorDelay();
        int accentA = hovered ? ClickGui.getInstance().hoverAlpha.getValueInt() : ClickGui.getInstance().alpha.getValueInt();
        Color unpressedFill = new Color(hovered ? hoverColor : defaultColor, true);
        float w = (float)this.width + 7.0f;
        float h = (float)this.height - 0.5f;
        if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            if (toggleProgress >= 0.999) {
                Render2DUtil.drawLutRect(context.getMatrices(), this.x, this.y, w, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), accentA);
            } else {
                Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + w, this.y + h, unpressedFill.getRGB());
                int overlayA = (int)Math.round((double)accentA * toggleProgress);
                if (overlayA > 0) {
                    Render2DUtil.drawLutRect(context.getMatrices(), this.x, this.y, w, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), overlayA);
                }
            }
        } else {
            Color accent = ClickGui.getInstance().getColor(baseDelay);
            Color pressedFill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), accentA);
            Color baseFill = ColorUtil.fadeColor(unpressedFill, pressedFill, toggleProgress);
            Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + w, this.y + (float)this.height - 0.5f, baseFill.getRGB());
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (hovered && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.drawString("Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(this.getName(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        }
        if (this.setting.hasParent()) {
            this.drawString(this.setting.isOpen() ? "-" : "+", (double)(this.x + (float)this.width - 1.0f), (double)textY, ClickGui.getInstance().gear.getValue());
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            if (this.setting.hasParent()) {
                boolean resetChildren = false;
                for (dev.idhammai.mod.gui.items.Component component : ClickGuiScreen.getInstance().getComponents()) {
                    for (ModuleButton moduleButton : component.getItems()) {
                        if (!moduleButton.getModule().getSettings().contains(this.setting)) continue;
                        moduleButton.resetChildSettingsToDefault(this.setting);
                        resetChildren = true;
                        break;
                    }
                    if (!resetChildren) continue;
                    break;
                }
            }
            this.setting.setValue(this.setting.getDefaultValue());
            BooleanButton.sound();
            return;
        }
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

