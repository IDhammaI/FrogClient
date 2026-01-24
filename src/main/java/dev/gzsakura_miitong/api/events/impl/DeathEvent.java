/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.entity.player.PlayerEntity;

public class DeathEvent {
    private static final DeathEvent INSTANCE = new DeathEvent();
    private PlayerEntity player;

    private DeathEvent() {
    }

    public static DeathEvent get(PlayerEntity player) {
        DeathEvent.INSTANCE.player = player;
        return INSTANCE;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}

