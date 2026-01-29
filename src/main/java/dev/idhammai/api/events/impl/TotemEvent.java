/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 */
package dev.idhammai.api.events.impl;

import net.minecraft.entity.player.PlayerEntity;

public class TotemEvent {
    private static final TotemEvent INSTANCE = new TotemEvent();
    private PlayerEntity player;

    private TotemEvent() {
    }

    public static TotemEvent get(PlayerEntity player) {
        TotemEvent.INSTANCE.player = player;
        return INSTANCE;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}

