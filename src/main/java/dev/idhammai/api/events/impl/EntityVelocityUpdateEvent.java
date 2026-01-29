/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.events.Event;
import net.minecraft.entity.Entity;

public class EntityVelocityUpdateEvent
extends Event {
    private static final EntityVelocityUpdateEvent INSTANCE = new EntityVelocityUpdateEvent();
    private Entity entity;
    private double x;
    private double y;
    private double z;
    private boolean explosion;

    private EntityVelocityUpdateEvent() {
    }

    public static EntityVelocityUpdateEvent get(Entity entity, double x, double y, double z, boolean explosion) {
        EntityVelocityUpdateEvent.INSTANCE.entity = entity;
        EntityVelocityUpdateEvent.INSTANCE.x = x;
        EntityVelocityUpdateEvent.INSTANCE.y = y;
        EntityVelocityUpdateEvent.INSTANCE.z = z;
        EntityVelocityUpdateEvent.INSTANCE.explosion = explosion;
        INSTANCE.setCancelled(false);
        return INSTANCE;
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isExplosion() {
        return this.explosion;
    }
}

