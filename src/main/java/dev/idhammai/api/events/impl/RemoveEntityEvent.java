/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 */
package dev.idhammai.api.events.impl;

import net.minecraft.entity.Entity;

public class RemoveEntityEvent {
    public static final RemoveEntityEvent instance = new RemoveEntityEvent();
    private Entity entity;
    private Entity.RemovalReason removalReason;

    private RemoveEntityEvent() {
    }

    public static RemoveEntityEvent get(Entity entity, Entity.RemovalReason removalReason) {
        RemoveEntityEvent.instance.entity = entity;
        RemoveEntityEvent.instance.removalReason = removalReason;
        return instance;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Entity.RemovalReason getRemovalReason() {
        return this.removalReason;
    }
}

