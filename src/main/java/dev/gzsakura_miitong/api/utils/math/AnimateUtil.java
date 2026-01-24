/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.utils.math;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.utils.Wrapper;

public class AnimateUtil
implements Wrapper {
    public static float deltaTime() {
        return Vitality.FPS.getFps() > 5 ? 1.0f / (float)Vitality.FPS.getFps() : 0.016f;
    }

    public static float fast(float end, float start, float multiple) {
        float clampedDelta = MathUtil.clamp(AnimateUtil.deltaTime() * multiple, 0.0f, 1.0f);
        return (1.0f - clampedDelta) * end + clampedDelta * start;
    }

    public static double animate(double current, double endPoint, double speed) {
        if (speed >= 1.0) {
            return endPoint;
        }
        if (speed == 0.0) {
            return current;
        }
        boolean shouldContinueAnimation = endPoint > current;
        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        if (Math.abs(dif) <= 0.001) {
            return endPoint;
        }
        double factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }
}

