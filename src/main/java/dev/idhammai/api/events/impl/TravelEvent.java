/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.events.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TravelEvent
extends Event {
    private static final TravelEvent INSTANCE = new TravelEvent();
    private PlayerEntity entity;

    private TravelEvent() {
    }

    public static TravelEvent get(Event.Stage stage, PlayerEntity entity) {
        TravelEvent.INSTANCE.entity = entity;
        TravelEvent.INSTANCE.stage = stage;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public PlayerEntity getEntity() {
        return this.entity;
    }
}

