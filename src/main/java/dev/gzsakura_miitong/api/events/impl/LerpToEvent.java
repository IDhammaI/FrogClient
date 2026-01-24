/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.entity.LivingEntity;

public class LerpToEvent {
    private static final LerpToEvent instance = new LerpToEvent();
    private LivingEntity entity;
    private double x;
    private double y;
    private double z;
    private float yRot;
    private float xRot;
    private long lastLerp;

    private LerpToEvent() {
    }

    public static LerpToEvent get(LivingEntity entity, double x, double y, double z, float yRot, float xRot, long lastLerp) {
        LerpToEvent.instance.entity = entity;
        LerpToEvent.instance.x = x;
        LerpToEvent.instance.y = y;
        LerpToEvent.instance.z = z;
        LerpToEvent.instance.yRot = yRot;
        LerpToEvent.instance.xRot = xRot;
        LerpToEvent.instance.lastLerp = lastLerp;
        return instance;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public float getYRot() {
        return this.yRot;
    }

    public float getXRot() {
        return this.xRot;
    }

    public long getLastLerp() {
        return this.lastLerp;
    }
}

