package dev.idhammai.mod.modules;

import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;

public abstract class HudModule extends Module {
    public final EnumSetting<PosMode> posMode;
    public final EnumSetting<Corner> corner;
    protected final SliderSetting x;
    protected final SliderSetting y;
    private int lastHudX;
    private int lastHudY;
    private int lastHudW;
    private int lastHudH;

    public HudModule(String name, String chinese, int defaultX, int defaultY) {
        this(name, "", chinese, defaultX, defaultY, PosMode.Pixel, Corner.LeftTop);
    }

    public HudModule(String name, String description, String chinese, int defaultX, int defaultY) {
        this(name, description, chinese, defaultX, defaultY, PosMode.Pixel, Corner.LeftTop);
    }

    protected HudModule(String name, String description, String chinese, int defaultX, int defaultY, PosMode defaultPosMode, Corner defaultCorner) {
        super(name, description, Category.Client);
        this.setChinese(chinese);
        this.posMode = this.add(new EnumSetting<PosMode>("PosMode", defaultPosMode));
        this.corner = this.add(new EnumSetting<Corner>("Corner", defaultCorner, () -> this.posMode.is(PosMode.Corner)));
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

    public final int getHudRenderX(int elementW) {
        if (this.posMode.is(PosMode.Pixel) || HudModule.mc.getWindow() == null) {
            return this.getHudX();
        }
        int sw = HudModule.mc.getWindow().getScaledWidth();
        int w = Math.max(0, elementW);
        int margin = this.getHudX();
        int x = this.corner.getValue().isRight() ? (sw - w - margin) : margin;
        return clampInt(x, 0, Math.max(0, sw - w));
    }

    public final int getHudRenderY(int elementH) {
        if (this.posMode.is(PosMode.Pixel) || HudModule.mc.getWindow() == null) {
            return this.getHudY();
        }
        int sh = HudModule.mc.getWindow().getScaledHeight();
        int h = Math.max(0, elementH);
        int margin = this.getHudY();
        int y = this.corner.getValue().isBottom() ? (sh - h - margin) : margin;
        return clampInt(y, 0, Math.max(0, sh - h));
    }

    public final void setHudPosFromBounds(int boundsX, int boundsY) {
        if (this.posMode.is(PosMode.Pixel) || HudModule.mc.getWindow() == null) {
            this.setHudPos(boundsX, boundsY);
            return;
        }
        int sw = HudModule.mc.getWindow().getScaledWidth();
        int sh = HudModule.mc.getWindow().getScaledHeight();
        int w = Math.max(0, this.getHudBoundW());
        int h = Math.max(0, this.getHudBoundH());
        Corner c = this.corner.getValue();
        int nx = c.isRight() ? (sw - w - boundsX) : boundsX;
        int ny = c.isBottom() ? (sh - h - boundsY) : boundsY;
        this.setHudPos(nx, ny);
    }

    private static int clampInt(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        return Math.min(v, max);
    }

    public enum PosMode {
        Pixel,
        Corner
    }

    public enum Corner {
        LeftTop(false, false),
        RightTop(true, false),
        LeftBottom(false, true),
        RightBottom(true, true);

        private final boolean right;
        private final boolean bottom;

        Corner(boolean right, boolean bottom) {
            this.right = right;
            this.bottom = bottom;
        }

        public boolean isRight() {
            return this.right;
        }

        public boolean isBottom() {
            return this.bottom;
        }
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

    public final int getHudBoundX() {
        return this.lastHudX;
    }

    public final int getHudBoundY() {
        return this.lastHudY;
    }

    public final int getHudBoundW() {
        return this.lastHudW;
    }

    public final int getHudBoundH() {
        return this.lastHudH;
    }

    public final boolean isHudHit(int mouseX, int mouseY) {
        if (this.lastHudW <= 0 || this.lastHudH <= 0) {
            return false;
        }
        return mouseX >= this.lastHudX && mouseX <= this.lastHudX + this.lastHudW && mouseY >= this.lastHudY && mouseY <= this.lastHudY + this.lastHudH;
    }

    public final boolean isHudOverlapping(int x1, int y1, int x2, int y2) {
        if (this.lastHudW <= 0 || this.lastHudH <= 0) {
            return false;
        }
        int rx1 = Math.min(x1, x2);
        int ry1 = Math.min(y1, y2);
        int rx2 = Math.max(x1, x2);
        int ry2 = Math.max(y1, y2);
        int bx1 = this.lastHudX;
        int by1 = this.lastHudY;
        int bx2 = this.lastHudX + this.lastHudW;
        int by2 = this.lastHudY + this.lastHudH;
        return rx1 < bx2 && rx2 > bx1 && ry1 < by2 && ry2 > by1;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) {
            return min;
        }
        return Math.min(v, max);
    }
}