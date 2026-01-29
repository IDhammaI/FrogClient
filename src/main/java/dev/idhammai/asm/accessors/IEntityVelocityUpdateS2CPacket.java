/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.idhammai.asm.accessors;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityVelocityUpdateS2CPacket.class})
public interface IEntityVelocityUpdateS2CPacket {
    @Mutable
    @Accessor(value="velocityX")
    public void setX(int var1);

    @Mutable
    @Accessor(value="velocityY")
    public void setY(int var1);

    @Mutable
    @Accessor(value="velocityZ")
    public void setZ(int var1);

    @Accessor(value="velocityX")
    public int getX();

    @Accessor(value="velocityY")
    public int getY();

    @Accessor(value="velocityZ")
    public int getZ();
}

