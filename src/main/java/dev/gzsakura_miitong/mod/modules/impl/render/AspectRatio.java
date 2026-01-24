/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;

public class AspectRatio
extends Module {
    public static AspectRatio INSTANCE;
    public final SliderSetting ratio = this.add(new SliderSetting("Ratio", 1.78, 0.0, 5.0, 0.01));

    public AspectRatio() {
        super("AspectRatio", Module.Category.Render);
        this.setChinese("\u5206\u8fa8\u7387");
        INSTANCE = this;
    }
}

