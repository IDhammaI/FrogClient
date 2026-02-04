package dev.idhammai.mod.gui.clickgui.pages;

import dev.idhammai.Frog;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.Mod;
import dev.idhammai.mod.gui.clickgui.ClickGuiFrame;
import dev.idhammai.mod.gui.clickgui.ClickGuiScreen;
import dev.idhammai.mod.gui.items.buttons.ModuleButton;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiHudPage {
    private final ClickGuiScreen host;
    private final ArrayList<ModuleButton> hudButtons = new ArrayList<>();
    private float hudScroll;
    private boolean hudOpen = true;
    private final Animation hudOpenAnim = new Animation();
    private boolean hudPosInit;
    private float hudLocalX;
    private float hudLocalY;
    private boolean hudDragging;
    private float hudDragDx;
    private float hudDragDy;
    private HudModule elementDragging;
    private int elementDragDx;
    private int elementDragDy;

    public ClickGuiHudPage(ClickGuiScreen host) {
        this.host = host;
    }

    public void init() {
        this.initHudButtons();
    }

    public void mouseScrolled(double verticalAmount) {
        float next = this.hudScroll + (float)(-verticalAmount) * 18.0f;
        if (next < 0.0f) {
            next = 0.0f;
        }
        this.hudScroll = next;
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            this.hudDragging = false;
            this.elementDragging = null;
        }
        this.hudButtons.forEach(b -> b.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void keyPressed(int keyCode) {
        this.hudButtons.forEach(b -> b.onKeyPressed(keyCode));
    }

    public void charTyped(char chr, int modifiers) {
        this.hudButtons.forEach(b -> b.onKeyTyped(chr, modifiers));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, ClickGuiFrame frame) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return;
        }
        this.dragHudElements(mouseX, mouseY);
        float mx = frame.unitMouseX(mouseX);
        float my = frame.unitMouseY(mouseY);
        float baseX = frame.baseX(ClickGuiScreen.Page.Hud);
        float screenUnitW = frame.scale == 0.0f ? (float)frame.screenW : (float)frame.screenW / frame.scale;
        float panelXf = Math.max(8.0f, (screenUnitW - (float)frame.panelW) / 2.0f);
        float defaultLocalX = panelXf + 10.0f;
        float defaultLocalY = (float)frame.panelY + 10.0f;
        if (!this.hudPosInit) {
            this.hudLocalX = defaultLocalX;
            this.hudLocalY = defaultLocalY;
            this.hudPosInit = true;
        }
        if (this.hudDragging) {
            this.hudLocalX = mx - this.hudDragDx - baseX - frame.totalOffsetX;
            this.hudLocalY = my - this.hudDragDy - frame.totalOffsetY;
        }
        float x = baseX + this.hudLocalX + frame.totalOffsetX;
        float y = this.hudLocalY + frame.totalOffsetY;
        int width = gui.moduleButtonWidth.getValueInt();
        int headerH = gui.categoryBarHeight.getValueInt();
        int height = headerH + 5;
        float headerX = x + ((float)width - (float)gui.categoryWidth.getValueInt()) / 2.0f;
        float headerY = y;
        float headerW = (float)gui.categoryWidth.getValueInt();
        float headerHf = (float)headerH;
        int topAlpha = gui.topAlpha.getValueInt();
        if (gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            Render2DUtil.drawLutRect(context.getMatrices(), headerX, headerY, headerW, headerHf, gui.getSpectrumLutId(), gui.getSpectrumLutHeight(), topAlpha);
        } else {
            Color topColor = ColorUtil.injectAlpha(gui.getColor((double)headerY / 10.0), topAlpha);
            Render2DUtil.drawRect(context.getMatrices(), headerX, headerY, headerW, headerHf, topColor);
        }
        Render2DUtil.drawRectWithOutline(context.getMatrices(), headerX, headerY, headerW, headerHf, new Color(0, 0, 0, 0), new Color(gui.hoverColor.getValue().getRGB()));
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        float iconY = headerY + (headerHf - FontManager.icon.getFontHeight()) / 2.0f;
        FontManager.icon.drawString(context.getMatrices(), Module.Category.Client.getIcon(), (double)(headerX + 6.0f), (double)iconY, dev.idhammai.mod.gui.items.buttons.Button.enableTextColor);
        float nameFontHeight = customFont ? FontManager.ui.getFontHeight() : 9.0f;
        float nameY = headerY + (headerHf - nameFontHeight) / 2.0f + (float)gui.titleOffset.getValueInt();
        String title = chinese ? "HUD" : "HUD";
        TextUtil.drawString(context, title, (double)(headerX + 20.0f), (double)nameY, dev.idhammai.mod.gui.items.buttons.Button.enableTextColor, customFont, shadow);
        double openProgressD = this.hudOpenAnim.get(this.hudOpen ? 1.0 : 0.0, 200L, Easing.CubicInOut);
        float openProgress = (float)openProgressD;
        float yTop = y + (float)height - 5.0f;
        float viewTop = y + (float)height - 3.0f;
        float viewBottom = (float)context.getScaledWindowHeight() - 20.0f + frame.totalOffsetY;
        float maxViewH = Math.max(0.0f, viewBottom - viewTop);
        double totalTarget = 0.0;
        for (ModuleButton b : this.hudButtons) {
            b.update();
            double itemOpen = b.animation.get(b.subOpen ? 1.0 : 0.0, 200L, Easing.CubicInOut);
            b.itemHeight = b.getVisibleItemHeight() * itemOpen;
            totalTarget += (double)b.getButtonHeight() + 1.5 + b.itemHeight;
        }
        float viewH = Math.max(0.0f, Math.min(maxViewH, (float)totalTarget));
        float maxScroll = (float)Math.max(0.0, totalTarget - (double)viewH);
        if (this.hudScroll > maxScroll) {
            this.hudScroll = maxScroll;
        }
        if (this.hudScroll < 0.0f) {
            this.hudScroll = 0.0f;
        }
        float openH = viewH * openProgress;
        if (openH <= 0.5f) {
            return;
        }
        if (gui.backGround.booleanValue) {
            float bgH = ((viewTop + viewH) - yTop) * openProgress;
            Render2DUtil.drawRect(context.getMatrices(), x, yTop, (float)width, bgH, ColorUtil.injectAlpha(gui.backGround.getValue(), gui.backgroundAlpha.getValueInt()));
            Render2DUtil.drawRectWithOutline(context.getMatrices(), x, yTop, (float)width, bgH, new Color(0, 0, 0, 0), new Color(gui.hoverColor.getValue().getRGB()));
        }
        int scX1 = (int)x - 1;
        int scY1 = (int)viewTop - 1;
        int scX2 = (int)(x + (float)width) + 1;
        int scY2 = (int)(viewTop + openH) + 1;
        context.enableScissor(scX1, scY1, scX2, scY2);
        float slide = (1.0f - openProgress) * 6.0f;
        float yOff = viewTop - this.hudScroll + slide;
        int imx = (int)mx;
        int imy = (int)my;
        for (ModuleButton b : this.hudButtons) {
            b.setLocation(x + 2.0f, yOff);
            b.setWidth(width - 4);
            if (b.itemHeight > 0.0 || b.subOpen) {
                int sX1 = (int)b.getX() - 1;
                int sY1 = (int)b.getY() - 1;
                int sX2 = (int)(b.getX() + (float)b.getWidth() + 1.0f);
                int sY2 = (int)((double)(yOff + (float)b.getButtonHeight() + 1.5f) + b.itemHeight) + 1;
                int iX1 = Math.max(scX1, sX1);
                int iY1 = Math.max(scY1, sY1);
                int iX2 = Math.min(scX2, sX2);
                int iY2 = Math.min(scY2, sY2);
                if (iX2 > iX1 && iY2 > iY1) {
                    context.enableScissor(iX1, iY1, iX2, iY2);
                    b.drawScreen(context, imx, imy, delta);
                    context.disableScissor();
                }
            } else {
                b.drawScreen(context, imx, imy, delta);
            }
            yOff += (float)b.getButtonHeight() + 1.5f + (float)b.itemHeight;
        }
        context.disableScissor();
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, ClickGuiFrame frame) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return false;
        }
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return false;
        }
        float mx = frame.unitMouseX(mouseX);
        float my = frame.unitMouseY(mouseY);
        float baseX = frame.baseX(ClickGuiScreen.Page.Hud);
        float screenUnitW = frame.scale == 0.0f ? (float)frame.screenW : (float)frame.screenW / frame.scale;
        float panelXf = Math.max(8.0f, (screenUnitW - (float)frame.panelW) / 2.0f);
        float defaultLocalX = panelXf + 10.0f;
        float defaultLocalY = (float)frame.panelY + 10.0f;
        if (!this.hudPosInit) {
            this.hudLocalX = defaultLocalX;
            this.hudLocalY = defaultLocalY;
            this.hudPosInit = true;
        }
        float x = baseX + this.hudLocalX + frame.totalOffsetX;
        float y = this.hudLocalY + frame.totalOffsetY;
        int width = gui.moduleButtonWidth.getValueInt();
        int headerH = gui.categoryBarHeight.getValueInt();
        int height = headerH + 5;
        float headerX = x + ((float)width - (float)gui.categoryWidth.getValueInt()) / 2.0f;
        float headerY = y;
        float headerW = (float)gui.categoryWidth.getValueInt();
        float headerHf = (float)headerH;
        boolean inHeader = mx >= headerX && mx <= headerX + headerW && my >= headerY && my <= headerY + headerHf;
        if (inHeader && mouseButton == 0) {
            this.host.getComponents().forEach(c -> c.drag = false);
            this.hudDragging = true;
            this.hudDragDx = mx - x;
            this.hudDragDy = my - y;
            return true;
        }
        if (inHeader && mouseButton == 1) {
            this.hudOpen = !this.hudOpen;
            dev.idhammai.mod.gui.items.Item.sound();
            return true;
        }
        double openProgressD = this.hudOpenAnim.get(this.hudOpen ? 1.0 : 0.0, 200L, Easing.CubicInOut);
        float openProgress = (float)openProgressD;
        float viewTop = y + (float)height - 3.0f;
        float viewBottom = (float)Wrapper.mc.getWindow().getScaledHeight() - 20.0f + frame.totalOffsetY;
        float maxViewH = Math.max(0.0f, viewBottom - viewTop);
        double totalTarget = 0.0;
        for (ModuleButton b : this.hudButtons) {
            b.update();
            double itemOpen = b.animation.get(b.subOpen ? 1.0 : 0.0, 200L, Easing.CubicInOut);
            b.itemHeight = b.getVisibleItemHeight() * itemOpen;
            totalTarget += (double)b.getButtonHeight() + 1.5 + b.itemHeight;
        }
        float viewH = Math.max(0.0f, Math.min(maxViewH, (float)totalTarget));
        float openH = viewH * openProgress;
        boolean inColumn = mx >= x && mx <= x + (float)width && my >= y && my <= viewTop + openH;
        if (!inColumn) {
            if (!inHeader && mouseButton == 0 && this.tryBeginDragHudElement(mouseX, mouseY)) {
                return true;
            }
            return inHeader;
        }
        if (openProgress <= 0.01f) {
            return true;
        }
        for (ModuleButton b : this.hudButtons) {
            b.update();
            double itemOpen = b.animation.get(b.subOpen ? 1.0 : 0.0, 200L, Easing.CubicInOut);
            b.itemHeight = b.getVisibleItemHeight() * itemOpen;
        }
        float slide = (1.0f - openProgress) * 6.0f;
        float yOff = viewTop - this.hudScroll + slide;
        int imx = (int)mx;
        int imy = (int)my;
        for (ModuleButton b : this.hudButtons) {
            b.setLocation(x + 2.0f, yOff);
            b.setWidth(width - 4);
            b.mouseClicked(imx, imy, mouseButton);
            yOff += (float)b.getButtonHeight() + 1.5f + (float)b.itemHeight;
            if (yOff > viewTop + viewH + 40.0f) {
                break;
            }
        }
        return true;
    }

    private void initHudButtons() {
        this.hudButtons.clear();
        this.hudScroll = 0.0f;
        ArrayList<Module> modules = new ArrayList<>();
        for (Module m : Frog.MODULE.getModules()) {
            if (!this.isHudComponentModule(m)) continue;
            modules.add(m);
        }
        modules.sort(Comparator.comparing(Mod::getName));
        for (Module m : modules) {
            this.hudButtons.add(new ModuleButton(m));
        }
    }

    private boolean isHudComponentModule(Module module) {
        return module instanceof HudModule;
    }

    private boolean tryBeginDragHudElement(int mouseX, int mouseY) {
        for (ModuleButton b : this.hudButtons) {
            Module m = b.getModule();
            if (!(m instanceof HudModule)) {
                continue;
            }
            HudModule hm = (HudModule)m;
            if (!hm.isOn()) {
                continue;
            }
            if (!hm.isHudHit(mouseX, mouseY)) {
                continue;
            }
            this.elementDragging = hm;
            this.elementDragDx = mouseX - hm.getHudX();
            this.elementDragDy = mouseY - hm.getHudY();
            return true;
        }
        return false;
    }

    private void dragHudElements(int mouseX, int mouseY) {
        if (this.elementDragging == null) {
            return;
        }
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            this.elementDragging = null;
            return;
        }
        long handle = Wrapper.mc.getWindow().getHandle();
        if (GLFW.glfwGetMouseButton(handle, 0) != 1) {
            this.elementDragging = null;
            return;
        }
        int nx = mouseX - this.elementDragDx;
        int ny = mouseY - this.elementDragDy;
        this.elementDragging.setHudPos(nx, ny);
    }
}
