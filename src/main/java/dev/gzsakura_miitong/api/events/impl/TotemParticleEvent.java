/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import java.awt.Color;

public class TotemParticleEvent
extends Event {
    private static final TotemParticleEvent instance = new TotemParticleEvent();
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public Color color;

    private TotemParticleEvent() {
    }

    public static TotemParticleEvent get(double velocityX, double velocityY, double velocityZ) {
        TotemParticleEvent.instance.velocityX = velocityX;
        TotemParticleEvent.instance.velocityY = velocityY;
        TotemParticleEvent.instance.velocityZ = velocityZ;
        return instance;
    }
}

