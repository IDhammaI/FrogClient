/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Hand
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.util.Hand;

public class InteractItemEvent
extends Event {
    private static final InteractItemEvent INSTANCE = new InteractItemEvent();
    public Hand hand;

    private InteractItemEvent() {
    }

    public static InteractItemEvent getPre(Hand hand) {
        InteractItemEvent.INSTANCE.hand = hand;
        InteractItemEvent.INSTANCE.stage = Event.Stage.Pre;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public static InteractItemEvent getPost(Hand hand) {
        InteractItemEvent.INSTANCE.hand = hand;
        InteractItemEvent.INSTANCE.stage = Event.Stage.Post;
        return INSTANCE;
    }
}

