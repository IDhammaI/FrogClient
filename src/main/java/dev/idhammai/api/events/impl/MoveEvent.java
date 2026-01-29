/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.events.Event;

public class MoveEvent
extends Event {
    private static final MoveEvent instance = new MoveEvent();
    public boolean modify;
    private double x;
    private double y;
    private double z;

    private MoveEvent() {
    }

    public static MoveEvent get(double x, double y, double z) {
        MoveEvent.instance.modify = false;
        MoveEvent.instance.x = x;
        MoveEvent.instance.y = y;
        MoveEvent.instance.z = z;
        instance.setCancelled(false);
        return instance;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.modify = true;
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.modify = true;
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.modify = true;
        this.z = z;
    }
}

