/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.events.Event;
import net.minecraft.block.entity.BlockEntity;

public class RenderBlockEntityEvent
extends Event {
    private static final RenderBlockEntityEvent INSTANCE = new RenderBlockEntityEvent();
    public BlockEntity blockEntity;

    public static RenderBlockEntityEvent get(BlockEntity blockEntity) {
        INSTANCE.setCancelled(false);
        RenderBlockEntityEvent.INSTANCE.blockEntity = blockEntity;
        return INSTANCE;
    }
}

