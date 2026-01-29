/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.world.World
 */
package dev.idhammai.api.utils.entity;

import com.mojang.authlib.GameProfile;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.PredictUtil;
import dev.idhammai.asm.accessors.ILivingEntity;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class CopyPlayerEntity
extends PlayerEntity {
    private final boolean onGround;

    public CopyPlayerEntity(PlayerEntity player) {
        this(player, false, 0.0, 0, 0, false, false, false, false);
    }

    public CopyPlayerEntity(PlayerEntity player, boolean effect) {
        super((World)Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(player.getGameProfile().getId(), player.getGameProfile().getName()));
        this.copyPositionAndRotation((Entity)player);
        this.prevX = player.prevX;
        this.prevZ = player.prevZ;
        this.prevY = player.prevY;
        this.bodyYaw = player.bodyYaw;
        this.headYaw = player.headYaw;
        this.handSwingProgress = player.handSwingProgress;
        this.handSwingTicks = player.handSwingTicks;
        this.limbAnimator.setSpeed(player.limbAnimator.getSpeed());
        this.limbAnimator.pos = player.limbAnimator.getPos();
        ((ILivingEntity)((Object)this)).setLeaningPitch(((ILivingEntity)player).getLeaningPitch());
        ((ILivingEntity)((Object)this)).setLastLeaningPitch(((ILivingEntity)player).getLeaningPitch());
        this.touchingWater = player.isTouchingWater();
        this.setSneaking(player.isSneaking());
        this.setPose(player.getPose());
        this.setFlag(7, player.isFallFlying());
        this.onGround = player.isOnGround();
        this.setOnGround(this.onGround);
        this.setVelocity(player.getVelocity());
        this.getInventory().clone(player.getInventory());
        if (effect) {
            for (StatusEffectInstance se : new ArrayList<StatusEffectInstance>(player.getStatusEffects())) {
                this.addStatusEffect(se);
            }
        }
        this.setAbsorptionAmountUnclamped(player.getAbsorptionAmount());
        this.setHealth(player.getHealth());
        this.setBoundingBox(player.getBoundingBox());
    }

    public CopyPlayerEntity(PlayerEntity player, boolean effect, double maxMotionY, int ticks, int simulation, boolean step, boolean doubleStep, boolean jump, boolean inBlockPause) {
        this(player, effect);
        if (ticks > 0) {
            this.setPosition(PredictUtil.getPos(player, maxMotionY, ticks, simulation, step, doubleStep, jump, inBlockPause));
        }
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean isSpectator() {
        return false;
    }

    public boolean isCreative() {
        return false;
    }
}

