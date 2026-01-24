/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.util.math.BlockPos;

public class BlockBreakingProgressEvent {
    private static final BlockBreakingProgressEvent INSTANCE = new BlockBreakingProgressEvent();
    private BlockPos pos;
    private int breakerId;
    private int progress;

    private BlockBreakingProgressEvent() {
    }

    public static BlockBreakingProgressEvent get(BlockPos pos, int breakerId, int progress) {
        BlockBreakingProgressEvent.INSTANCE.pos = pos;
        BlockBreakingProgressEvent.INSTANCE.breakerId = breakerId;
        BlockBreakingProgressEvent.INSTANCE.progress = progress;
        return INSTANCE;
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    public int getBreakerId() {
        return this.breakerId;
    }

    public int getProgress() {
        return this.progress;
    }
}

