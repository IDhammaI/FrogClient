/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.client;

import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;

public class ColorsModule
extends Module {
    public static ColorsModule INSTANCE;
    public final ColorSetting clientColor = this.add(new ColorSetting("Color", new Color(255, 0, 0)).allowClientColor(false));

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
}

