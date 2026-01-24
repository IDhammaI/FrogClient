/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.particle.ParticleEffect
 *  net.minecraft.particle.ParticleTypes
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.impl.FireworkShooterRotationEvent;
import dev.gzsakura_miitong.api.events.impl.RemoveFireworkEvent;
import dev.gzsakura_miitong.api.utils.Wrapper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FireworkRocketEntity.class})
public class MixinFireworkRocketEntity
implements Wrapper {
    @Shadow
    private int life;

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/projectile/FireworkRocketEntity;updateRotation()V", shift=At.Shift.AFTER)}, cancellable=true)
    private void hookTickPre(CallbackInfo ci) {
        FireworkRocketEntity rocketEntity = (FireworkRocketEntity)FireworkRocketEntity.class.cast(this);
        RemoveFireworkEvent removeFireworkEvent = RemoveFireworkEvent.get(rocketEntity);
        Alien.EVENT_BUS.post(removeFireworkEvent);
        if (removeFireworkEvent.isCancelled()) {
            ci.cancel();
            if (this.life == 0 && !rocketEntity.isSilent()) {
                MixinFireworkRocketEntity.mc.world.playSound(null, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0f, 1.0f);
            }
            ++this.life;
            if (MixinFireworkRocketEntity.mc.world.isClient) {
                MixinFireworkRocketEntity.mc.world.addParticle((ParticleEffect)ParticleTypes.FIREWORK, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), MixinFireworkRocketEntity.mc.world.random.nextGaussian() * 0.05, -rocketEntity.getVelocity().y * 0.5, MixinFireworkRocketEntity.mc.world.random.nextGaussian() * 0.05);
            }
        }
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"), require=0)
    public Vec3d hook(LivingEntity instance) {
        FireworkShooterRotationEvent event = FireworkShooterRotationEvent.get(instance, instance.getYaw(), instance.getPitch());
        Alien.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            return event.getRotationVector();
        }
        return instance.getRotationVector();
    }
}

