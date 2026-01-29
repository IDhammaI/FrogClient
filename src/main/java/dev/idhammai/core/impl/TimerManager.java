/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.core.impl;

import dev.idhammai.mod.modules.impl.player.TimerModule;

public class TimerManager {
    public float timer = 1.0f;
    public float lastTimer;

    public void set(float factor) {
        if (factor < 0.1f) {
            factor = 0.1f;
        }
        this.timer = factor;
    }

    public void reset() {
        this.lastTimer = this.timer = this.getDefault();
    }

    public void tryReset() {
        if (this.lastTimer != this.getDefault()) {
            this.reset();
        }
    }

    public float get() {
        return this.timer;
    }

    public float getDefault() {
        return TimerModule.INSTANCE.isOn() ? (TimerModule.INSTANCE.boostKey.isPressed() ? TimerModule.INSTANCE.boost.getValueFloat() : TimerModule.INSTANCE.multiplier.getValueFloat()) : 1.0f;
    }
}

