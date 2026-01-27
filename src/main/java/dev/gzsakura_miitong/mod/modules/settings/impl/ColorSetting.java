/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.settings.impl;

import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.mod.modules.impl.client.ColorsModule;
import dev.gzsakura_miitong.mod.modules.settings.Setting;
import java.awt.Color;
import java.util.function.BooleanSupplier;

public class ColorSetting
extends Setting {
    public static final Timer timer = new Timer();
    public static final float effectSpeed = 4.0f;
    private final Color defaultValue;
    public boolean sync = false;
    public boolean injectBoolean = false;
    public boolean booleanValue = false;
    private Color value;
    private boolean defaultSync = false;
    private boolean defaultBooleanValue = false;
    private boolean allowClientColor = true;

    public ColorSetting(String name) {
        this(name, new Color(255, 255, 255));
        this.defaultSync = true;
    }

    public ColorSetting(String name, BooleanSupplier visibilityIn) {
        super(name, visibilityIn);
        this.defaultValue = this.value = new Color(255, 255, 255);
        this.defaultSync = true;
    }

    public ColorSetting(String name, Color defaultValue) {
        super(name);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColorSetting(String name, Color defaultValue, BooleanSupplier visibilityIn) {
        super(name, visibilityIn);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColorSetting(String name, int defaultValue) {
        this(name, new Color(defaultValue, true));
    }

    public ColorSetting(String name, int defaultValue, BooleanSupplier visibilityIn) {
        this(name, new Color(defaultValue, true), visibilityIn);
    }

    public Color getValue() {
        if (this.sync) {
            if (this.allowClientColor) {
                ColorsModule colors = ColorsModule.INSTANCE;
                if (colors != null && colors.colorMode.getValue() == ColorsModule.ColorMode.Rainbow) {
                    double rainbowState = Math.ceil(((double)System.currentTimeMillis() * colors.rainbowSpeed.getValue() + (double)colors.rainbowDelay.getValue()) / 20.0);
                    Color hsb = Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), colors.saturation.getValueFloat() / 255.0f, 1.0f);
                    this.setValue(new Color(hsb.getRed(), hsb.getGreen(), hsb.getBlue(), this.value.getAlpha()));
                }
            } else {
                float[] HSB = Color.RGBtoHSB(this.value.getRed(), this.value.getGreen(), this.value.getBlue(), null);
                Color preColor = Color.getHSBColor((float)timer.getMs() * 0.36f * 4.0f / 20.0f % 361.0f / 360.0f, HSB[1], HSB[2]);
                this.setValue(new Color(preColor.getRed(), preColor.getGreen(), preColor.getBlue(), this.value.getAlpha()));
            }
        }
        return this.value;
    }

    public void setValue(Color value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = new Color(value, true);
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public ColorSetting injectBoolean(boolean value) {
        this.injectBoolean = true;
        this.defaultBooleanValue = value;
        this.booleanValue = value;
        return this;
    }

    public ColorSetting allowClientColor(boolean value) {
        this.allowClientColor = value;
        return this;
    }

    public Color getDefaultValue() {
        return this.defaultValue;
    }

    public boolean getDefaultBooleanValue() {
        return this.defaultBooleanValue;
    }

    public boolean getDefaultSync() {
        return this.defaultSync;
    }
}

