/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FireworkShooterRotationEvent
extends Event {
    private static final FireworkShooterRotationEvent instance = new FireworkShooterRotationEvent();
    public LivingEntity shooter;
    public float pitch;
    public float yaw;

    private FireworkShooterRotationEvent() {
    }

    public static FireworkShooterRotationEvent get(LivingEntity shooter, float yaw, float pitch) {
        FireworkShooterRotationEvent.instance.shooter = shooter;
        FireworkShooterRotationEvent.instance.yaw = yaw;
        FireworkShooterRotationEvent.instance.pitch = pitch;
        instance.setCancelled(false);
        return instance;
    }

    public final Vec3d getRotationVector() {
        float f = this.pitch * ((float)Math.PI / 180);
        float g = -this.yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos((float)g);
        float i = MathHelper.sin((float)g);
        float j = MathHelper.cos((float)f);
        float k = MathHelper.sin((float)f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }
}

