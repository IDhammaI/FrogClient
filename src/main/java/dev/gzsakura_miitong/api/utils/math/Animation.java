/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.utils.math;

public class Animation {
    private final FadeUtils fadeUtils = new FadeUtils(0L);
    public double from = 0.0;
    public double to = 0.0;

    public double get(double target, long length, Easing ease) {
        if (target != this.to) {
            this.from += (this.to - this.from) * this.fadeUtils.ease(ease);
            this.to = target;
            this.fadeUtils.reset();
        }
        this.fadeUtils.setLength(length);
        return this.from + (this.to - this.from) * this.fadeUtils.ease(ease);
    }
}

