/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 */
package dev.idhammai.api.utils.entity;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerEntityPredict {
    public final PlayerEntity player;
    public final PlayerEntity predict;

    public PlayerEntityPredict(PlayerEntity player, double maxMotionY, int ticks, int simulation, boolean step, boolean doubleStep, boolean jump, boolean inBlockPause) {
        this.player = player;
        this.predict = ticks > 0 ? new CopyPlayerEntity(player, true, maxMotionY, ticks, simulation, step, doubleStep, jump, inBlockPause) : player;
    }
}

