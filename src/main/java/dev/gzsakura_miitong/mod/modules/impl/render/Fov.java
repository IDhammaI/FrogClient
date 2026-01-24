/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;

public class Fov
extends Module {
    public static Fov INSTANCE;
    public final SliderSetting fov = this.add(new SliderSetting("Fov", 90.0, 30.0, 170.0, 1.0));
    public final SliderSetting itemFov = this.add(new SliderSetting("ItemFov", 70.0, 30.0, 170.0, 1.0));

    public Fov() {
        super("Fov", Module.Category.Render);
        this.setChinese("\u81ea\u5b9a\u4e49\u89c6\u89d2");
        INSTANCE = this;
    }
}

