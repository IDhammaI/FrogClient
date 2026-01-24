/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={FireworkRocketEntity.class})
public interface IFireworkRocketEntity {
    @Accessor(value="shooter")
    public LivingEntity getShooter();

    @Invoker(value="wasShotByEntity")
    public boolean hookWasShotByEntity();

    @Invoker(value="explodeAndRemove")
    public void hookExplodeAndRemove();
}

