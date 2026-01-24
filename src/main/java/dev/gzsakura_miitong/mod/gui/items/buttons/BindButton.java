/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.util.Formatting
 */
package dev.gzsakura_miitong.mod.gui.items.buttons;

import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.mod.gui.ClickGuiScreen;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.settings.impl.BindSetting;
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
        Color color = ClickGui.getInstance().color.getValue();
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width + 7.0f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? defaultColor : hoverColor) : (!this.isHovering(mouseX, mouseY) ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()));
        if (this.isListening) {
            this.drawString("Press keyCodec Key...", (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), enableTextColor);
        } else {
            String str = this.setting.getKeyString();
            if (!this.isListening && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340) && this.setting.getName().equals("Key")) {
                if (this.setting.isHoldEnable()) {
                    this.drawString("\u00a77Toggle/\u00a7fHold", (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), enableTextColor);
                } else {
                    this.drawString("\u00a7fToggle\u00a77/Hold", (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), enableTextColor);
                }
            } else {
                this.drawString(this.setting.getName() + " " + String.valueOf(Formatting.GRAY) + str, (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), this.getState() ? enableTextColor : defaultTextColor);
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
            if (InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340) && this.setting.getName().equals("Key")) {
                this.setting.setHoldEnable(!this.setting.isHoldEnable());
                BindButton.sound();
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

