/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.SpriteProvider
 *  net.minecraft.client.particle.TotemParticle
 *  net.minecraft.client.world.ClientWorld
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.impl.TotemParticleEvent;

import java.awt.Color;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TotemParticle.class})
public abstract class MixinTotemParticle
extends MixinParticle {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void hookInit(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci) {
        TotemParticleEvent event = TotemParticleEvent.get(velocityX, velocityY, velocityZ);
        Alien.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            this.velocityX = event.velocityX;
            this.velocityY = event.velocityY;
            this.velocityZ = event.velocityZ;
            Color color = event.color;
            if (color != null) {
                this.setColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
                this.setAlpha((float)color.getAlpha() / 255.0f);
            }
        }
    }
}

