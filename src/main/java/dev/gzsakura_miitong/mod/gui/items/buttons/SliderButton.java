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
package dev.gzsakura_miitong.mod.gui.items.buttons;

import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.mod.gui.ClickGuiScreen;
import dev.gzsakura_miitong.mod.gui.items.Component;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
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

    public SliderButton(SliderSetting setting) {
        super(setting.getName());
        this.setting = setting;
        this.min = setting.getMin();
        this.max = setting.getMax();
        this.difference = this.max - this.min;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.dragSetting(mouseX, mouseY);
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float)this.width + 7.0f, this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? defaultColor : hoverColor);
        Color color = ClickGui.getInstance().color.getValue();
        Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.setting.getValue() <= this.min ? this.x : (float)((double)this.x + (double)((float)this.width + 7.0f) * this.partialMultiplier()), this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB());
        if (this.isListening) {
            this.drawString(this.currentString + StringButton.getIdleSign(), (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), this.getState() ? enableTextColor : defaultTextColor);
        } else {
            this.drawString(this.getName() + " " + String.valueOf(Formatting.GRAY) + this.setting.getValueFloat() + this.setting.getSuffix(), (double)(this.x + 2.3f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), enableTextColor);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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
        double result = Math.min(this.setting.getMin() + this.difference * percent, this.max);
        this.setting.setValue(result);
    }

    private double part() {
        return this.setting.getValue() - this.min;
    }

    private double partialMultiplier() {
        return Math.min(this.part() / this.difference, 1.0);
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}

