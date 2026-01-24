/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.damage.DamageSource
 *  net.minecraft.world.World
 *  net.minecraft.world.explosion.Explosion
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.gzsakura_miitong.asm.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Explosion.class})
public interface IExplosion {
    @Mutable
    @Accessor(value="x")
    public void setX(double var1);

    @Mutable
    @Accessor(value="y")
    public void setY(double var1);

    @Mutable
    @Accessor(value="z")
    public void setZ(double var1);

    @Mutable
    @Accessor(value="power")
    public void setPower(float var1);

    @Mutable
    @Accessor(value="entity")
    public void setEntity(Entity var1);

    @Mutable
    @Accessor(value="world")
    public World getWorld();

    @Mutable
    @Accessor(value="world")
    public void setWorld(World var1);

    @Mutable
    @Accessor(value="damageSource")
    public DamageSource getDamageSource();
}

