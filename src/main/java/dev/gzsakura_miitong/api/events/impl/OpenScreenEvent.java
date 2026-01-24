/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent
extends Event {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();
    public Screen screen;

    private OpenScreenEvent() {
    }

    public static OpenScreenEvent get(Screen screen) {
        OpenScreenEvent.INSTANCE.screen = screen;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

