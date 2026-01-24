/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.entity.Entity;

public class EntitySpawnedEvent {
    private static final EntitySpawnedEvent INSTANCE = new EntitySpawnedEvent();
    private Entity entity;

    private EntitySpawnedEvent() {
    }

    public static EntitySpawnedEvent get(Entity player) {
        EntitySpawnedEvent.INSTANCE.entity = player;
        return INSTANCE;
    }

    public Entity getEntity() {
        return this.entity;
    }
}

