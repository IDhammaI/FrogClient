/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.client.gui.DrawContext;

public class Render2DEvent {
    private static final Render2DEvent INSTANCE = new Render2DEvent();
    public DrawContext drawContext;
    public float tickDelta;

    public static Render2DEvent get(DrawContext drawContext, float tickDelta) {
        Render2DEvent.INSTANCE.drawContext = drawContext;
        Render2DEvent.INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}

