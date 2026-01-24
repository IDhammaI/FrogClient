/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.SoundInstance
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.client.sound.SoundInstance;

public class PlaySoundEvent
extends Event {
    private static final PlaySoundEvent INSTANCE = new PlaySoundEvent();
    public SoundInstance sound;

    public static PlaySoundEvent get(SoundInstance sound) {
        INSTANCE.setCancelled(false);
        PlaySoundEvent.INSTANCE.sound = sound;
        return INSTANCE;
    }
}

