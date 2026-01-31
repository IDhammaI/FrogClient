/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.util.Formatting
 */
package dev.idhammai.mod.gui.items.buttons;

import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.BindSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;

public class BindButton
extends Button {
    private final BindSetting setting;
    public boolean isListening;

    public BindButton(BindSetting setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        double baseDelay = this.getColorDelay();
        float w = (float)this.width + 7.0f;
        float h = (float)this.height - 0.5f;
        if (!this.getState() && ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            int a = !hovered ? ClickGui.getInstance().alpha.getValueInt() : ClickGui.getInstance().hoverAlpha.getValueInt();
            Render2DUtil.drawLutRect(context.getMatrices(), this.x, this.y, w, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), a);
        } else {
            Color color = ClickGui.getInstance().getColor(baseDelay);
            Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + w, this.y + (float)this.height - 0.5f, this.getState() ? (!hovered ? defaultColor : hoverColor) : (!hovered ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()));
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (this.isListening) {
            this.drawString("Press keyCodec Key...", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            String str = this.setting.getKeyString();
            if (!this.isListening && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
                if (this.setting.getName().equals("Key")) {
                    if (this.setting.isHoldEnable()) {
                        this.drawString("\u00a77Toggle/\u00a7fHold", (double)(this.x + 2.3f), (double)textY, enableTextColor);
                    } else {
                        this.drawString("\u00a7fToggle\u00a77/Hold", (double)(this.x + 2.3f), (double)textY, enableTextColor);
                    }
                } else {
                    this.drawString("Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
                }
            } else {
                this.drawString(this.setting.getName() + " " + String.valueOf(Formatting.GRAY) + str, (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            if (InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
                if (this.setting.getName().equals("Key")) {
                    this.setting.setHoldEnable(!this.setting.isHoldEnable());
                    BindButton.sound();
                } else {
                    this.setting.setValue(this.setting.getDefaultValue());
                    this.setting.setHoldEnable(false);
                    this.setting.setPressed(false);
                    this.setting.holding = false;
                    BindButton.sound();
                }
            } else {
                this.onMouseClick();
            }
        } else if (this.isListening) {
            this.setting.setValue(-mouseButton - 2);
            this.onMouseClick();
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (this.isListening) {
            this.setting.setValue(key);
            if (this.setting.getKeyString().equalsIgnoreCase("DELETE")) {
                this.setting.setValue(-1);
            }
            this.onMouseClick();
        }
    }

    @Override
    public void toggle() {
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }
}

