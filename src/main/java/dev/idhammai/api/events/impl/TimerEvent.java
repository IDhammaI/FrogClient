/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.events.Event;

public class TimerEvent
extends Event {
    private static final TimerEvent instance = new TimerEvent();
    private float timer;
    private boolean modified;

    private TimerEvent() {
    }

    public static TimerEvent getEvent() {
        TimerEvent.instance.timer = 1.0f;
        TimerEvent.instance.modified = false;
        instance.setCancelled(false);
        return instance;
    }

    public float get() {
        return this.timer;
    }

    public void set(float timer) {
        this.modified = true;
        this.timer = timer;
    }

    public boolean isModified() {
        return this.modified;
    }
}

