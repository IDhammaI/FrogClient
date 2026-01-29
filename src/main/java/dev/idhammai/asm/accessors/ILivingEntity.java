/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.idhammai.asm.accessors;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LivingEntity.class})
public interface ILivingEntity {
    @Accessor(value="lastAttackedTicks")
    public int getLastAttackedTicks();

    @Accessor(value="jumpingCooldown")
    public int getLastJumpCooldown();

    @Accessor(value="jumpingCooldown")
    public void setLastJumpCooldown(int var1);

    @Accessor(value="leaningPitch")
    public float getLeaningPitch();

    @Accessor(value="leaningPitch")
    public void setLeaningPitch(float var1);

    @Accessor(value="lastLeaningPitch")
    public void setLastLeaningPitch(float var1);
}

