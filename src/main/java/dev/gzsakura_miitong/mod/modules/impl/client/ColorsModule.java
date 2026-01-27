/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.client;

import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;

public class ColorsModule
extends Module {
    public static ColorsModule INSTANCE;
    public final EnumSetting<ColorMode> colorMode = this.add(new EnumSetting<ColorMode>("ColorMode", ColorMode.Custom));
    public final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 4.0, 1.0, 10.0, 0.1, () -> this.colorMode.getValue() == ColorMode.Rainbow));
    public final SliderSetting saturation = this.add(new SliderSetting("Saturation", 130.0, 1.0, 255.0, () -> this.colorMode.getValue() == ColorMode.Rainbow));
    public final SliderSetting rainbowDelay = this.add(new SliderSetting("Delay", 350, 0, 1000, () -> this.colorMode.getValue() == ColorMode.Rainbow));

    public ColorsModule() {
        super("Colors", Module.Category.Client);
        this.setChinese("\u989c\u8272");
        INSTANCE = this;
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }

    public Color getColor() {
        return this.getColor(0.0);
    }

    public Color getColor(double delay) {
        if (this.colorMode.getValue() == ColorMode.Rainbow) {
            double rainbowState = Math.ceil(((double)System.currentTimeMillis() * this.rainbowSpeed.getValue() + delay * this.rainbowDelay.getValue()) / 20.0);
            return Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0f, 1.0f);
        }
        return new Color(255, 0, 0);
    }

    public enum ColorMode {
        Custom,
        Rainbow
    }
}

