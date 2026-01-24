/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3d
 */
package dev.gzsakura_miitong.api.utils.math;

import dev.gzsakura_miitong.api.utils.Wrapper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ExplosionUtil
implements Wrapper {
    public static float anchorDamage(BlockPos pos, LivingEntity target, LivingEntity predict) {
        return DamageUtils.anchorDamage(target, predict, pos.toCenterPos());
    }

    public static float calculateDamage(Vec3d pos, LivingEntity entity, LivingEntity predict, float power) {
        return DamageUtils.explosionDamage(entity, predict, pos, power * 2.0f);
    }
}

