/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.impl.misc;

import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;

public class ExtraTab
extends Module {
    public static ExtraTab INSTANCE;
    public final SliderSetting size = this.add(new SliderSetting("Size", 200.0, 80.0, 1000.0, 1.0));
    public final SliderSetting columns = this.add(new SliderSetting("Columns", 20.0, 1.0, 100.0, 1.0));

    public ExtraTab() {
        super("ExtraTab", Module.Category.Misc);
        this.setChinese("\u5217\u8868\u65e0\u4e0a\u9650");
        INSTANCE = this;
    }
}

