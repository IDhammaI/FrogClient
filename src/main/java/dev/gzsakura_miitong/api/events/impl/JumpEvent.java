/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;

public class JumpEvent
extends Event {
    private static final JumpEvent instance = new JumpEvent();

    private JumpEvent() {
    }

    public static JumpEvent get(Event.Stage stage) {
        JumpEvent.instance.stage = stage;
        return instance;
    }
}

