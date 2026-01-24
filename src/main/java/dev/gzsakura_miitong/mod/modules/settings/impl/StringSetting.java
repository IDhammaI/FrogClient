/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.settings.impl;

import dev.gzsakura_miitong.mod.modules.settings.Setting;
import java.util.function.BooleanSupplier;

public class StringSetting
extends Setting {
    private final String defaultValue;
    private String value;

    public StringSetting(String name, String value) {
        super(name);
        this.value = value;
        this.defaultValue = value;
    }

    public StringSetting(String name, String value, BooleanSupplier visibilityIn) {
        super(name, visibilityIn);
        this.value = value;
        this.defaultValue = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String text) {
        this.value = text;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }
}

