/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.api.events.impl;

public class AmbientOcclusionEvent {
    private static final AmbientOcclusionEvent INSTANCE = new AmbientOcclusionEvent();
    public float lightLevel = -1.0f;

    private AmbientOcclusionEvent() {
    }

    public static AmbientOcclusionEvent get() {
        AmbientOcclusionEvent.INSTANCE.lightLevel = -1.0f;
        return INSTANCE;
    }
}

