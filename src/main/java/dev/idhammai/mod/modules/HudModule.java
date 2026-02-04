package dev.idhammai.mod.modules;

import dev.idhammai.mod.modules.settings.impl.SliderSetting;

public abstract class HudModule extends Module {
    protected final SliderSetting x;
    protected final SliderSetting y;
    private int lastHudX;
    private int lastHudY;
    private int lastHudW;
    private int lastHudH;

    public HudModule(String name, String chinese, int defaultX, int defaultY) {
        this(name, "", chinese, defaultX, defaultY);
    }

    public HudModule(String name, String description, String chinese, int defaultX, int defaultY) {
        super(name, description, Category.Client);
        this.setChinese(chinese);
        this.x = this.add(new SliderSetting("X", defaultX, 0, 1500));
        this.y = this.add(new SliderSetting("Y", defaultY, 0, 1000));
    }

    public final int getHudX() {
        return this.x.getValueInt();
    }

    public final int getHudY() {
        return this.y.getValueInt();
    }

    public final void setHudX(int x) {
        double nx = clamp((double)x, this.x.getMin(), this.x.getMax());
        this.x.setValue(nx);
    }

    public final void setHudY(int y) {
        double ny = clamp((double)y, this.y.getMin(), this.y.getMax());
        this.y.setValue(ny);
    }

    public final void setHudPos(int x, int y) {
        this.setHudX(x);
        this.setHudY(y);
    }

    protected final void setHudBounds(int x, int y, int w, int h) {
        this.lastHudX = x;
        this.lastHudY = y;
        this.lastHudW = w;
        this.lastHudH = h;
    }

    protected final void clearHudBounds() {
        this.lastHudW = 0;
        this.lastHudH = 0;
    }

    public final boolean isHudHit(int mouseX, int mouseY) {
        if (this.lastHudW <= 0 || this.lastHudH <= 0) {
            return false;
        }
        return mouseX >= this.lastHudX && mouseX <= this.lastHudX + this.lastHudW && mouseY >= this.lastHudY && mouseY <= this.lastHudY + this.lastHudH;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) {
            return min;
        }
        return Math.min(v, max);
    }
}