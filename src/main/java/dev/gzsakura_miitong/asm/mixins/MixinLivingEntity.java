/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.AttributeContainer
 *  net.minecraft.entity.attribute.EntityAttribute
 *  net.minecraft.entity.attribute.EntityAttributeInstance
 *  net.minecraft.entity.attribute.EntityAttributeModifier
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.world.World
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.impl.LerpToEvent;
import dev.gzsakura_miitong.api.events.impl.SprintEvent;
import dev.gzsakura_miitong.mod.modules.impl.movement.ElytraFly;
import dev.gzsakura_miitong.mod.modules.impl.movement.NoSlow;
import dev.gzsakura_miitong.mod.modules.impl.movement.Velocity;
import dev.gzsakura_miitong.mod.modules.impl.player.AntiEffects;
import dev.gzsakura_miitong.mod.modules.impl.render.ViewModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class MixinLivingEntity
extends Entity {
    @Final
    @Shadow
    private static EntityAttributeModifier SPRINTING_SPEED_BOOST;
    @Unique
    private boolean previousElytra = false;
    @Unique
    private long lastLerp = 0L;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    @Nullable
    public EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    @Shadow
    public AttributeContainer getAttributes() {
        return null;
    }

    @Shadow
    public abstract void remove(Entity.RemovalReason var1);

    @Inject(method={"getHandSwingDuration"}, at={@At(value="HEAD")}, cancellable=true)
    private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {
        if (ViewModel.INSTANCE.isOn() && ViewModel.INSTANCE.slowAnimation.getValue()) {
            info.setReturnValue(ViewModel.INSTANCE.slowAnimationVal.getValueInt());
        }
    }

    @Inject(method={"isFallFlying"}, at={@At(value="TAIL")}, cancellable=true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = (Boolean)cir.getReturnValue();
        if (this.previousElytra && !elytra && ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce)) {
            cir.setReturnValue(ElytraFly.recastElytra(MinecraftClient.getInstance().player));
        }
        this.previousElytra = elytra;
    }

    @Redirect(method={"travel"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"), require=0)
    private boolean travelEffectHook(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        if (AntiEffects.INSTANCE.isOn()) {
            if (effect == StatusEffects.SLOW_FALLING && AntiEffects.INSTANCE.slowFalling.getValue()) {
                return false;
            }
            if (effect == StatusEffects.LEVITATION && AntiEffects.INSTANCE.levitation.getValue()) {
                return false;
            }
        }
        return instance.hasStatusEffect(effect);
    }

    @Redirect(method={"applyMovementInput"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isClimbing()Z"), require=0)
    public boolean climbingHook(LivingEntity instance) {
        if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.noClimb.getValue() && LivingEntity.class.cast((Object)this) == MinecraftClient.getInstance().player) {
            return false;
        }
        return instance.isClimbing();
    }

    @Redirect(method={"applyClimbingSpeed"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isClimbing()Z"), require=0)
    public boolean climbingHook2(LivingEntity instance) {
        if (NoSlow.INSTANCE.climb() && LivingEntity.class.cast((Object)this) == MinecraftClient.getInstance().player) {
            return false;
        }
        return instance.isClimbing();
    }

    @Inject(method={"updateTrackedPositionAndAngles"}, at={@At(value="HEAD")})
    private void lerpToHook(double x, double y, double z, float yRot, float xRot, int steps, CallbackInfo ci) {
        Alien.EVENT_BUS.post(LerpToEvent.get((LivingEntity)LivingEntity.class.cast((Object)this), x, y, z, yRot, xRot, this.lastLerp));
        this.lastLerp = System.currentTimeMillis();
    }

    @Inject(method={"setSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
        if (LivingEntity.class.cast((Object)this) == MinecraftClient.getInstance().player) {
            SprintEvent event = SprintEvent.get();
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                sprinting = event.isSprint();
                super.setSprinting(sprinting);
                EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance((RegistryEntry<EntityAttribute>)EntityAttributes.GENERIC_MOVEMENT_SPEED);
                entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.id());
                if (sprinting) {
                    entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
                }
            }
        }
    }
}

