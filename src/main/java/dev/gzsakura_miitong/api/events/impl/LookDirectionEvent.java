/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.entity.Entity;

public class LookDirectionEvent
extends Event {
    private static final LookDirectionEvent instance = new LookDirectionEvent();
    private Entity entity;
    private double cursorDeltaX;
    private double cursorDeltaY;

    private LookDirectionEvent() {
    }

    public static LookDirectionEvent get(Entity entity, double cursorDeltaX, double cursorDeltaY) {
        LookDirectionEvent.instance.entity = entity;
        LookDirectionEvent.instance.cursorDeltaX = cursorDeltaX;
        LookDirectionEvent.instance.cursorDeltaY = cursorDeltaY;
        instance.setCancelled(false);
        return instance;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public double getCursorDeltaX() {
        return this.cursorDeltaX;
    }

    public double getCursorDeltaY() {
        return this.cursorDeltaY;
    }
}

