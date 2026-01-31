/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.client.util.SelectionManager
 *  net.minecraft.util.Formatting
 */
package dev.idhammai.mod.gui.items.buttons;

import dev.idhammai.api.utils.math.Timer;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.util.Formatting;

public class StringButton
extends Button {
    private static final Timer idleTimer = new Timer();
    private static boolean idle;
    private final StringSetting setting;
    public boolean isListening;
    private String currentString = "";

    public StringButton(StringSetting setting) {
        super(setting.getName());
        this.setting = setting;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    public static String getIdleSign() {
        if (idleTimer.passed(500L)) {
            idle = !idle;
            idleTimer.reset();
        }
        if (idle) {
            return "_";
        }
        return "";
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        double baseDelay = this.getColorDelay();
        float w = (float)this.width + 7.0f;
        float h = (float)this.height - 0.5f;
        if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum && this.getState()) {
            int a = !hovered ? ClickGui.getInstance().alpha.getValueInt() : ClickGui.getInstance().hoverAlpha.getValueInt();
            Render2DUtil.drawLutRect(context.getMatrices(), this.x, this.y, w, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), a);
        } else {
            Color color = ClickGui.getInstance().getColor(baseDelay);
            Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + w, this.y + (float)this.height - 0.5f, this.getState() ? (!hovered ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()) : (!hovered ? defaultColor : hoverColor));
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (this.isListening) {
            this.drawString(this.currentString + StringButton.getIdleSign(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        } else if (this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.drawString("Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(this.setting.getName() + ": " + String.valueOf(Formatting.GRAY) + this.setting.getValue(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.isListening = false;
            this.currentString = "";
            this.setting.setValue(this.setting.getDefaultValue());
            StringButton.sound();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.isListening) {
            this.setString(this.currentString + typedChar);
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (this.isListening) {
            switch (key) {
                case 256: {
                    this.isListening = false;
                    break;
                }
                case 257: 
                case 335: {
                    this.enterString();
                    break;
                }
                case 86: {
                    if (!InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)341)) break;
                    this.setString(this.currentString + SelectionManager.getClipboard((MinecraftClient)mc));
                    break;
                }
                case 259: {
                    this.setString(StringButton.removeLastChar(this.currentString));
                }
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    private void enterString() {
        if (this.currentString.isEmpty()) {
            this.setting.setValue(this.setting.getDefaultValue());
        } else {
            this.setting.setValue(this.currentString);
        }
        this.onMouseClick();
    }

    @Override
    public void toggle() {
        this.setString(this.setting.getValue());
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }

    public void setString(String newString) {
        this.currentString = newString;
    }
}

