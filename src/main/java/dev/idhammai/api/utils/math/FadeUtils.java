/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.api.utils.math;

public class FadeUtils {
    public long length;
    private long start;

    public FadeUtils(long ms) {
        this.length = ms;
        this.reset();
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }

    public boolean isEnd() {
        return this.getTime() >= this.length;
    }

    protected long getTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public double getFadeOne() {
        return this.isEnd() ? 1.0 : (double)this.getTime() / (double)this.length;
    }

    public double easeOutQuad() {
        return 1.0 - (1.0 - this.getFadeOne()) * (1.0 - this.getFadeOne());
    }

    public double ease(Easing easing) {
        return easing.ease(this.getFadeOne());
    }
}

