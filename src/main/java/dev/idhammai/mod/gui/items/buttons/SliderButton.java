/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.client.util.SelectionManager
 *  net.minecraft.util.Formatting
 *  org.lwjgl.glfw.GLFW
 */
package dev.idhammai.mod.gui.items.buttons;

import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.gui.ClickGuiScreen;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class SliderButton
extends Button {
    private final double min;
    private final double max;
    private final double difference;
    public final SliderSetting setting;
    public boolean isListening;
    private String currentString = "";
    private boolean drag = false;
    private final Animation valueAnimation = new Animation();

    public SliderButton(SliderSetting setting) {
        super(setting.getName());
        this.setting = setting;
        this.min = setting.getMin();
        this.max = setting.getMax();
        this.difference = this.max - this.min;
        double initial = this.partialMultiplier();
        this.valueAnimation.from = initial;
        this.valueAnimation.to = initial;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.dragSetting(mouseX, mouseY);
        boolean hovered = this.isHovering(mouseX, mouseY);
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width + 7.0f, this.y + (float)this.height - 0.5f, !hovered ? defaultColor : hoverColor);
        double baseDelay = this.getColorDelay();
        double fillProgress = this.valueAnimation.get(this.partialMultiplier(), 200L, Easing.CubicInOut);
        float filledX = (float)((double)this.x + (double)((float)this.width + 7.0f) * fillProgress);
        float h = (float)this.height - 0.5f;
        int a = !hovered ? ClickGui.getInstance().alpha.getValueInt() : ClickGui.getInstance().hoverAlpha.getValueInt();
        if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            float fillW = Math.max(0.0f, filledX - this.x);
            Render2DUtil.drawLutRect(context.getMatrices(), this.x, this.y, fillW, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), a);
        } else {
            Color color = ClickGui.getInstance().getColor(baseDelay);
            Render2DUtil.rect(context.getMatrices(), this.x, this.y, filledX, this.y + (float)this.height - 0.5f, ColorUtil.injectAlpha(color, a).getRGB());
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (this.isListening) {
            this.drawString(this.currentString + StringButton.getIdleSign(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        } else if (hovered && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.drawString("Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(this.getName() + " " + String.valueOf(Formatting.GRAY) + this.setting.getValueFloat() + this.setting.getSuffix(), (double)(this.x + 2.3f), (double)textY, enableTextColor);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.drag = false;
            this.isListening = false;
            this.currentString = "";
            this.setting.setValue(this.setting.getDefaultValue());
            SliderButton.sound();
            return;
        }
        if (this.isHovering(mouseX, mouseY)) {
            SliderButton.sound();
            if (mouseButton == 0) {
                if (this.isListening) {
                    this.toggle();
                } else {
                    this.setSettingFromX(mouseX);
                    this.drag = true;
                }
            } else if (mouseButton == 1) {
                this.toggle();
            }
        }
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ClickGuiScreen.getInstance().getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() + 8.0f && (float)mouseY >= this.getY() && (float)mouseY <= this.getY() + (float)this.height - 1.0f;
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
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

    private void enterString() {
        if (this.currentString.isEmpty() || !this.isNumeric(this.currentString)) {
            this.setting.setValue(this.setting.getDefaultValue());
        } else {
            this.setting.setValue(Double.parseDouble(this.currentString));
        }
        this.onMouseClick();
    }

    public void setString(String newString) {
        this.currentString = newString;
    }

    private void dragSetting(int mouseX, int mouseY) {
        if (this.drag && this.isHovering(mouseX, mouseY) && GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) == 1) {
            this.setSettingFromX(mouseX);
        } else {
            this.drag = false;
        }
    }

    @Override
    public void toggle() {
        this.setString("" + this.setting.getValueFloat());
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }

    private void setSettingFromX(int mouseX) {
        double percent = (double)((float)mouseX - this.x) / ((double)this.width + 7.4);
        double result = this.setting.getMin() + this.difference * percent;
        result = Math.max(this.min, Math.min(result, this.max));
        this.setting.setValue(result);
    }

    private double part() {
        return this.setting.getValue() - this.min;
    }

    private double partialMultiplier() {
        return Math.max(0.0, Math.min(this.part() / this.difference, 1.0));
    }

    public void resetFillAnimation() {
        this.valueAnimation.from = 0.0;
        this.valueAnimation.to = 0.0;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}

