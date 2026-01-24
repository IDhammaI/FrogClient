/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.settings;

import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final BooleanSupplier visibility;
    private final String name;

    public Setting(String name) {
        this.name = name;
        this.visibility = null;
    }

    public Setting(String name, BooleanSupplier visibilityIn) {
        this.name = name;
        this.visibility = visibilityIn;
    }

    public boolean isVisible() {
        return this.visibility == null || this.visibility.getAsBoolean();
    }

    public String getName() {
        return this.name;
    }
}

