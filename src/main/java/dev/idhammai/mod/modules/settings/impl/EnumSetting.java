/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.settings.impl;

import dev.idhammai.mod.modules.settings.EnumConverter;
import dev.idhammai.mod.modules.settings.Setting;
import java.util.function.BooleanSupplier;

public class EnumSetting<T extends Enum<T>>
extends Setting {
    private final T defaultValue;
    private T value;
    private Runnable task = null;
    private boolean injectTask = false;

    public EnumSetting(String name, T defaultValue) {
        super(name);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public EnumSetting(String name, T defaultValue, BooleanSupplier visibilityIn) {
        super(name, visibilityIn);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public void loadSetting(String enumString) {
        EnumConverter converter = new EnumConverter();
        if (enumString == null) {
            this.value = this.defaultValue;
            return;
        }
        Enum<?> value = converter.get((Enum<?>)this.defaultValue, enumString);
        this.value = value != null ? (T) value : this.defaultValue;
    }

    public void increaseEnum() {
        this.value = (T) EnumConverter.increaseEnum(this.value);
        if (this.injectTask) {
            this.task.run();
        }
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setEnumValue(String value) {
        for (Enum e : (Enum[])((Enum)this.value).getDeclaringClass().getEnumConstants()) {
            if (!e.name().equalsIgnoreCase(value)) continue;
            this.value = (T) e;
            if (!this.injectTask) continue;
            this.task.run();
        }
    }

    public EnumSetting<T> injectTask(Runnable task) {
        this.task = task;
        this.injectTask = true;
        return this;
    }

    public boolean is(T mode) {
        return this.getValue() == mode;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }
}

