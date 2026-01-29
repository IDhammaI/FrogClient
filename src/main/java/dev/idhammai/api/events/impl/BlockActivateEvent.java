/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 */
package dev.idhammai.api.events.impl;

import net.minecraft.block.BlockState;

public class BlockActivateEvent {
    private static final BlockActivateEvent INSTANCE = new BlockActivateEvent();
    public BlockState blockState;

    private BlockActivateEvent() {
    }

    public static BlockActivateEvent get(BlockState blockState) {
        BlockActivateEvent.INSTANCE.blockState = blockState;
        return INSTANCE;
    }
}

