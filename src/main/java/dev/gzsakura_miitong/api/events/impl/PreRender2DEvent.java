/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.gzsakura_miitong.api.events.impl;

import net.minecraft.client.gui.DrawContext;

public class PreRender2DEvent {
    private static final PreRender2DEvent INSTANCE = new PreRender2DEvent();
    public DrawContext drawContext;

    public static PreRender2DEvent get(DrawContext drawContext) {
        PreRender2DEvent.INSTANCE.drawContext = drawContext;
        return INSTANCE;
    }
}

