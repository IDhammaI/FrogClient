/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.Particle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package dev.idhammai.asm.mixins;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={Particle.class})
public abstract class MixinParticle {
    @Shadow
    protected double velocityX;
    @Shadow
    protected double velocityY;
    @Shadow
    protected double velocityZ;

    @Shadow
    public abstract void setColor(float var1, float var2, float var3);

    @Shadow
    protected void setAlpha(float alpha) {
    }
}

