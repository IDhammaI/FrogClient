/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;

public class ChunkOcclusionEvent
extends Event {
    private static final ChunkOcclusionEvent INSTANCE = new ChunkOcclusionEvent();

    private ChunkOcclusionEvent() {
    }

    public static ChunkOcclusionEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

