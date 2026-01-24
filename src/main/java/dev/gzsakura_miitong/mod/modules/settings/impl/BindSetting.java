/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 */
package dev.gzsakura_miitong.mod.modules.settings.impl;

import dev.gzsakura_miitong.mod.modules.settings.Setting;
import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;
import org.lwjgl.glfw.GLFW;

public class BindSetting
extends Setting {
    private final int defaultValue;
    public boolean holding = false;
    private int value;
    private boolean pressed = false;
    private boolean holdEnable = false;

    public BindSetting(String name, int value) {
        super(name);
        this.defaultValue = value;
        this.value = value;
    }

    public BindSetting(String name, int value, BooleanSupplier visibilityIn) {
        super(name, visibilityIn);
        this.defaultValue = value;
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getKeyString() {
        Object kn;
        if (this.value == -1) {
            return "None";
        }
        if (this.value < -1) {
            return "Mouse" + (Math.abs(this.value) - 1);
        }
        Object object = kn = this.value > 0 ? GLFW.glfwGetKeyName((int)this.value, (int)GLFW.glfwGetKeyScancode((int)this.value)) : "None";
        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    int keyCodec;
                    if (!declaredField.getName().startsWith("GLFW_KEY_") || (keyCodec = ((Integer)declaredField.get(null)).intValue()) != this.value) continue;
                    String nb = declaredField.getName().substring("GLFW_KEY_".length());
                    kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                }
            }
            catch (Exception ignored) {
                kn = "None";
            }
        }
        if (kn == null) {
            return "Unknown " + this.value;
        }
        return ((String)kn).toUpperCase();
    }

    public boolean isPressed() {
        return this.pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isHoldEnable() {
        return this.holdEnable;
    }

    public void setHoldEnable(boolean holdEnable) {
        this.holdEnable = holdEnable;
    }

    public int getDefaultValue() {
        return this.defaultValue;
    }
}

