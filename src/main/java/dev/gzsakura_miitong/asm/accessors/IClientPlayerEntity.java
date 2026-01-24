/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientPlayerEntity.class})
public interface IClientPlayerEntity {
    @Accessor(value="ticksSinceLastPositionPacketSent")
    public void setTicksSinceLastPositionPacketSent(int var1);

    @Accessor(value="lastYaw")
    public float getLastYaw();

    @Accessor(value="lastYaw")
    public void setLastYaw(float var1);

    @Accessor(value="lastPitch")
    public float getLastPitch();

    @Accessor(value="lastPitch")
    public void setLastPitch(float var1);
}

