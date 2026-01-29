/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.Particle
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.events.Event;
import net.minecraft.client.particle.Particle;

public class ParticleEvent
extends Event {
    private static final ParticleEvent instance = new ParticleEvent();
    public Particle particle;

    public static ParticleEvent get(Particle particle) {
        ParticleEvent.instance.particle = particle;
        instance.setCancelled(false);
        return instance;
    }
}

