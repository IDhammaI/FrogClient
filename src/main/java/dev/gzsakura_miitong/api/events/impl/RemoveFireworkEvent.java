/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.entity.projectile.FireworkRocketEntity;

public class RemoveFireworkEvent
extends Event {
    public static final RemoveFireworkEvent instance = new RemoveFireworkEvent();
    private FireworkRocketEntity entity;

    private RemoveFireworkEvent() {
    }

    public static RemoveFireworkEvent get(FireworkRocketEntity entity) {
        RemoveFireworkEvent.instance.entity = entity;
        instance.setCancelled(false);
        return instance;
    }

    public FireworkRocketEntity getRocketEntity() {
        return this.entity;
    }
}

