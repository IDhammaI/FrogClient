/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.entity.Entity;

public class EntitySpawnEvent
extends Event {
    private static final EntitySpawnEvent INSTANCE = new EntitySpawnEvent();
    private Entity entity;

    private EntitySpawnEvent() {
    }

    public static EntitySpawnEvent get(Entity entity) {
        EntitySpawnEvent.INSTANCE.entity = entity;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public Entity getEntity() {
        return this.entity;
    }
}

