/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 */
package dev.gzsakura_miitong.api.utils.player;

import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.asm.accessors.IVec3d;
import dev.gzsakura_miitong.mod.modules.impl.movement.HoleSnap;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class MovementUtil
implements Wrapper {
    public static boolean isMoving() {
        return (double)MovementUtil.mc.player.input.movementForward != 0.0 || (double)MovementUtil.mc.player.input.movementSideways != 0.0 || HoleSnap.INSTANCE.isOn();
    }

    public static boolean isStatic() {
        return MovementUtil.mc.player.getVelocity().getX() == 0.0 && MovementUtil.mc.player.isOnGround() && MovementUtil.mc.player.getVelocity().getZ() == 0.0;
    }

    public static boolean isJumping() {
        return MovementUtil.mc.player.input.jumping;
    }

    public static double getDistance2D() {
        double xDist = MovementUtil.mc.player.getX() - MovementUtil.mc.player.prevX;
        double zDist = MovementUtil.mc.player.getZ() - MovementUtil.mc.player.prevZ;
        return Math.sqrt(xDist * xDist + zDist * zDist);
    }

    public static double getJumpSpeed() {
        double defaultSpeed = 0.0;
        if (MovementUtil.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            int amplifier = ((StatusEffectInstance)MovementUtil.mc.player.getActiveStatusEffects().get(StatusEffects.JUMP_BOOST)).getAmplifier();
            defaultSpeed += (double)(amplifier + 1) * 0.1;
        }
        return defaultSpeed;
    }

    public static double[] directionSpeed(double speed) {
        float forward = MovementUtil.mc.player.input.movementForward;
        float side = MovementUtil.mc.player.input.movementSideways;
        return MovementUtil.directionSpeed(speed, forward, side);
    }

    private static double[] directionSpeed(double speed, float forward, float side) {
        float yaw = MovementUtil.mc.player.prevYaw + (MovementUtil.mc.player.getYaw() - MovementUtil.mc.player.prevYaw) * mc.getRenderTickCounter().getTickDelta(true);
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double)forward * speed * cos + (double)side * speed * sin;
        double posZ = (double)forward * speed * sin - (double)side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static double getMotionX() {
        return MovementUtil.mc.player.getVelocity().x;
    }

    public static void setMotionX(double x) {
        ((IVec3d)MovementUtil.mc.player.getVelocity()).setX(x);
    }

    public static double getMotionY() {
        return MovementUtil.mc.player.getVelocity().y;
    }

    public static void setMotionY(double y) {
        ((IVec3d)MovementUtil.mc.player.getVelocity()).setY(y);
    }

    public static double getMotionZ() {
        return MovementUtil.mc.player.getVelocity().z;
    }

    public static void setMotionZ(double z) {
        ((IVec3d)MovementUtil.mc.player.getVelocity()).setZ(z);
    }

    public static double getSpeed(boolean slowness) {
        double defaultSpeed = 0.2873;
        return MovementUtil.getSpeed(slowness, defaultSpeed);
    }

    public static double getSpeed(boolean slowness, double defaultSpeed) {
        int amplifier;
        if (MovementUtil.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            amplifier = ((StatusEffectInstance)MovementUtil.mc.player.getActiveStatusEffects().get(StatusEffects.SPEED)).getAmplifier();
            defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        if (slowness && MovementUtil.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            amplifier = ((StatusEffectInstance)MovementUtil.mc.player.getActiveStatusEffects().get(StatusEffects.SLOWNESS)).getAmplifier();
            defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        if (MovementUtil.mc.player.isSneaking()) {
            defaultSpeed /= 5.0;
        }
        return defaultSpeed;
    }
}

