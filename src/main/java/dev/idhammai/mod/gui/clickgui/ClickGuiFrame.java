package dev.idhammai.mod.gui.clickgui;

public final class ClickGuiFrame {
    public final float scale;
    public final float slideY;
    public final float pageOffsetX;
    public final int pageW;
    public final int panelX;
    public final int panelY;
    public final int panelW;
    public final int panelH;
    public final float totalOffsetX;
    public final float totalOffsetY;
    public final int screenW;
    public final int screenH;

    public ClickGuiFrame(float scale, float slideY, float pageOffsetX, int pageW, int panelX, int panelY, int panelW, int panelH, float totalOffsetX, float totalOffsetY, int screenW, int screenH) {
        this.scale = scale;
        this.slideY = slideY;
        this.pageOffsetX = pageOffsetX;
        this.pageW = pageW;
        this.panelX = panelX;
        this.panelY = panelY;
        this.panelW = panelW;
        this.panelH = panelH;
        this.totalOffsetX = totalOffsetX;
        this.totalOffsetY = totalOffsetY;
        this.screenW = screenW;
        this.screenH = screenH;
    }

    public float unitMouseX(int mouseX) {
        return this.scale == 0.0f ? (float)mouseX : (float)mouseX / this.scale;
    }

    public float unitMouseY(int mouseY) {
        return this.scale == 0.0f ? (float)mouseY : ((float)mouseY - this.slideY) / this.scale;
    }

    public float pageUnitW() {
        return this.scale == 0.0f ? (float)this.pageW : (float)this.pageW / this.scale;
    }

    public float baseX(ClickGuiScreen.Page page) {
        return this.pageOffsetX + (float)page.ordinal() * this.pageUnitW();
    }
}
