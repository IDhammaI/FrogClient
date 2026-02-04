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
    private boolean selecting;
    private int selectStartX;
    private int selectStartY;
    private int selectEndX;
    private int selectEndY;
    private final ArrayList<HudModule> selectedHud = new ArrayList<>();
    private boolean groupDragging;
    private int groupDragStartMouseX;
    private int groupDragStartMouseY;
    private final ArrayList<GroupDragEntry> groupDragEntries = new ArrayList<>();
    private boolean layoutMenuOpen;
    private int layoutMenuX;
    private int layoutMenuY;
    private final Animation layoutButtonAnim = new Animation();
    private final Animation layoutMenuAnim = new Animation();

    private static final class GroupDragEntry {
        private final HudModule module;
        private final int startX;
        private final int startY;

        private GroupDragEntry(HudModule module, int startX, int startY) {
            this.module = module;
            this.startX = startX;
            this.startY = startY;
        }
    }

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
            this.groupDragging = false;
            this.groupDragEntries.clear();
        }
        this.hudButtons.forEach(b -> b.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void keyPressed(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.layoutMenuOpen) {
                this.closeLayoutMenu();
                return;
            }
            if (!this.selectedHud.isEmpty()) {
                this.clearSelection();
                return;
            }
        }
        if (!this.selectedHud.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_G) {
                this.applyGridLayout();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_H) {
                this.applyHorizontalLayout();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_V) {
                this.applyVerticalLayout();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_L) {
                this.applyAlignLeft();
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_T) {
                this.applyAlignTop();
                return;
            }
        }
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
        this.updateSelection(mouseX, mouseY);
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
        if (gui.backgroundStyle.getValue() != ClickGui.BackgroundStyle.Transparent) {
            Render2DUtil.drawRectWithOutline(context.getMatrices(), headerX, headerY, headerW, headerHf, new Color(0, 0, 0, 0), new Color(gui.hoverColor.getValue().getRGB()));
        }
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
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 900.0f);
            this.renderSelectionOverlay(context, frame, mouseX, mouseY);
            this.renderLayoutMenu(context, frame, mouseX, mouseY);
            context.getMatrices().pop();
            return;
        }
        if (gui.backGround.booleanValue) {
            float bgH = ((viewTop + viewH) - yTop) * openProgress;
            Render2DUtil.drawRect(context.getMatrices(), x, yTop, (float)width, bgH, ColorUtil.injectAlpha(gui.backGround.getValue(), gui.backgroundAlpha.getValueInt()));
            if (gui.backgroundStyle.getValue() != ClickGui.BackgroundStyle.Transparent) {
                Render2DUtil.drawRectWithOutline(context.getMatrices(), x, yTop, (float)width, bgH, new Color(0, 0, 0, 0), new Color(gui.hoverColor.getValue().getRGB()));
            }
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
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 900.0f);
        this.renderSelectionOverlay(context, frame, mouseX, mouseY);
        this.renderLayoutMenu(context, frame, mouseX, mouseY);
        context.getMatrices().pop();
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
        if (this.isLayoutMenuVisible() && this.handleLayoutMenuClick(mouseX, mouseY, mouseButton, frame)) {
            return true;
        }
        if (!this.selectedHud.isEmpty() && mouseButton == 0 && this.isHoveringLayoutButton(mx, my, frame)) {
            this.openLayoutMenu(frame);
            return true;
        }
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
            if (!inHeader && mouseButton == 1 && !this.selectedHud.isEmpty()) {
                this.openLayoutMenu(frame);
                return true;
            }
            if (!inHeader && mouseButton == 0) {
                if (this.tryBeginDragHudElement(mouseX, mouseY)) {
                    return true;
                }
                this.beginSelection(mouseX, mouseY);
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
        modules.sort(Comparator.comparingInt(ClickGuiHudPage::getHudListGroup)
                .thenComparingInt(ClickGuiHudPage::getHudListPriority)
                .thenComparing(Mod::getName));
        for (Module m : modules) {
            this.hudButtons.add(new ModuleButton(m));
        }
    }

    private static int getHudListGroup(Mod m) {
        String name = m.getName();
        if (name.equals("ArrayList") || name.equals("Coords") || name.equals("Info") || name.equals("WaterMark") || name.equals("Armor")) {
            return 0;
        }
        if (name.startsWith("Items")) {
            return 2;
        }
        return 1;
    }

    private static int getHudListPriority(Mod m) {
        return switch (m.getName()) {
            case "ArrayList" -> 0;
            case "Coords" -> 1;
            case "Info" -> 2;
            case "WaterMark" -> 3;
            case "Armor" -> 4;
            case "TextRadar" -> 5;
            default -> 999;
        };
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
            if (!this.selectedHud.isEmpty() && this.selectedHud.contains(hm)) {
                this.beginGroupDrag(mouseX, mouseY);
                return true;
            }
            this.groupDragging = false;
            this.groupDragEntries.clear();
            this.elementDragging = hm;
            this.elementDragDx = mouseX - hm.getHudBoundX();
            this.elementDragDy = mouseY - hm.getHudBoundY();
            return true;
        }
        return false;
    }

    private void dragHudElements(int mouseX, int mouseY) {
        if (!this.groupDragging && this.elementDragging == null) {
            return;
        }
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            this.elementDragging = null;
            this.groupDragging = false;
            this.groupDragEntries.clear();
            return;
        }
        long handle = Wrapper.mc.getWindow().getHandle();
        if (GLFW.glfwGetMouseButton(handle, 0) != 1) {
            this.elementDragging = null;
            this.groupDragging = false;
            this.groupDragEntries.clear();
            return;
        }
        if (this.groupDragging) {
            int dx = mouseX - this.groupDragStartMouseX;
            int dy = mouseY - this.groupDragStartMouseY;
            for (GroupDragEntry e : this.groupDragEntries) {
                e.module.setHudPosFromBounds(e.startX + dx, e.startY + dy);
            }
            return;
        }
        int nx = mouseX - this.elementDragDx;
        int ny = mouseY - this.elementDragDy;
        this.elementDragging.setHudPosFromBounds(nx, ny);
    }

    private void beginSelection(int mouseX, int mouseY) {
        this.closeLayoutMenu();
        this.selecting = true;
        this.selectStartX = mouseX;
        this.selectStartY = mouseY;
        this.selectEndX = mouseX;
        this.selectEndY = mouseY;
        this.selectedHud.clear();
        this.groupDragging = false;
        this.groupDragEntries.clear();
        this.elementDragging = null;
        this.hudDragging = false;
    }

    private void updateSelection(int mouseX, int mouseY) {
        if (!this.selecting) {
            return;
        }
        this.selectEndX = mouseX;
        this.selectEndY = mouseY;
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            this.finishSelection();
            return;
        }
        long handle = Wrapper.mc.getWindow().getHandle();
        if (GLFW.glfwGetMouseButton(handle, 0) != 1) {
            this.finishSelection();
        }
    }

    private void beginGroupDrag(int mouseX, int mouseY) {
        this.closeLayoutMenu();
        this.groupDragging = true;
        this.groupDragStartMouseX = mouseX;
        this.groupDragStartMouseY = mouseY;
        this.groupDragEntries.clear();
        for (HudModule hm : this.selectedHud) {
            if (!hm.isOn()) {
                continue;
            }
            this.groupDragEntries.add(new GroupDragEntry(hm, hm.getHudBoundX(), hm.getHudBoundY()));
        }
        this.elementDragging = null;
        this.hudDragging = false;
    }

    private void finishSelection() {
        if (!this.selecting) {
            return;
        }
        this.selecting = false;
        int x1 = this.selectStartX;
        int y1 = this.selectStartY;
        int x2 = this.selectEndX;
        int y2 = this.selectEndY;
        this.selectedHud.clear();
        for (Module m : Frog.MODULE.getModules()) {
            if (!(m instanceof HudModule)) {
                continue;
            }
            HudModule hm = (HudModule)m;
            if (!hm.isOn()) {
                continue;
            }
            if (!hm.isHudOverlapping(x1, y1, x2, y2)) {
                continue;
            }
            this.selectedHud.add(hm);
        }
    }

    private void clearSelection() {
        this.selecting = false;
        this.selectedHud.clear();
        this.closeLayoutMenu();
    }

    private void renderSelectionOverlay(DrawContext context, ClickGuiFrame frame, int mouseX, int mouseY) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return;
        }
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        if (this.selecting) {
            int x1 = this.selectStartX;
            int y1 = this.selectStartY;
            int x2 = this.selectEndX;
            int y2 = this.selectEndY;
            float rx = Math.min(frame.unitMouseX(x1), frame.unitMouseX(x2));
            float ry = Math.min(frame.unitMouseY(y1), frame.unitMouseY(y2));
            float rw = Math.abs(frame.unitMouseX(x2) - frame.unitMouseX(x1));
            float rh = Math.abs(frame.unitMouseY(y2) - frame.unitMouseY(y1));
            Color fill = ColorUtil.injectAlpha(gui.hoverColor.getValue(), 36);
            Color outline = ColorUtil.injectAlpha(gui.hoverColor.getValue(), 170);
            Render2DUtil.drawRectWithOutline(context.getMatrices(), rx, ry, rw, rh, fill, outline);
        }

        double btnP = this.layoutButtonAnim.get(this.selectedHud.isEmpty() ? 0.0 : 1.0, 160L, Easing.CubicInOut);
        float btnProgress = (float)btnP;
        if (btnProgress < 0.01f && this.selectedHud.isEmpty()) {
            return;
        }

        Color outline = ColorUtil.injectAlpha(gui.hoverColor.getValue(), (int)Math.round(210.0 * (double)btnProgress));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            int bw = hm.getHudBoundW();
            int bh = hm.getHudBoundH();
            if (bw <= 0 || bh <= 0) {
                continue;
            }
            int bx = hm.getHudBoundX();
            int by = hm.getHudBoundY();
            minX = Math.min(minX, bx);
            minY = Math.min(minY, by);
            float ux = frame.unitMouseX(bx);
            float uy = frame.unitMouseY(by);
            float uw = frame.unitMouseX(bx + bw) - frame.unitMouseX(bx);
            float uh = frame.unitMouseY(by + bh) - frame.unitMouseY(by);
            Render2DUtil.drawRectWithOutline(context.getMatrices(), ux, uy, uw, uh, new Color(0, 0, 0, 0), outline);
        }

        if (minX == Integer.MAX_VALUE) {
            return;
        }

        String label = chinese ? "布局" : "Layout";
        float tw = customFont ? (float)FontManager.ui.getWidth(label) : TextUtil.getWidth(label);
        float th = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
        float pad = 4.0f;
        float bw = tw + pad * 2.0f;
        float bh = th + 4.0f;
        float bx = frame.unitMouseX(minX);
        float by = frame.unitMouseY(minY) - bh - 2.0f;
        float maxH = frame.scale == 0.0f ? (float)frame.screenH : ((float)frame.screenH - frame.slideY) / frame.scale;
        if (by < 1.0f) {
            by = 1.0f;
        }
        if (by + bh > maxH - 1.0f) {
            by = maxH - bh - 1.0f;
        }

        float btnScale = 0.85f + 0.15f * btnProgress;
        float cx = bx + bw / 2.0f;
        float cy = by + bh / 2.0f;
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0.0f);
        context.getMatrices().scale(btnScale, btnScale, 1.0f);
        context.getMatrices().translate(-cx, -cy, 0.0f);

        int bgA = Math.max(0, Math.min(255, (int)Math.round(240.0 * (double)btnProgress)));
        int outA = Math.max(0, Math.min(255, (int)Math.round(200.0 * (double)btnProgress)));
        Color bg = ColorUtil.injectAlpha(gui.defaultColor.getValue(), bgA);
        Color out = ColorUtil.injectAlpha(gui.hoverColor.getValue(), outA);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), bx, by, bw, bh, bg, out);
        int textA = Math.max(0, Math.min(255, (int)Math.round((double)gui.enableTextColor.getValue().getAlpha() * (double)btnProgress)));
        int textColor = ColorUtil.injectAlpha(gui.enableTextColor.getValue(), textA).getRGB();
        TextUtil.drawString(context, label, (double)(bx + pad), (double)(by + 2.0f), textColor, customFont, shadow);
        context.getMatrices().pop();
    }

    private void openLayoutMenu(ClickGuiFrame frame) {
        this.layoutMenuOpen = true;
        if (this.selectedHud.isEmpty()) {
            this.layoutMenuX = 0;
            this.layoutMenuY = 0;
            return;
        }
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            int bw = hm.getHudBoundW();
            int bh = hm.getHudBoundH();
            if (bw <= 0 || bh <= 0) {
                continue;
            }
            minX = Math.min(minX, hm.getHudBoundX());
            minY = Math.min(minY, hm.getHudBoundY());
        }
        if (minX == Integer.MAX_VALUE) {
            this.layoutMenuX = 0;
            this.layoutMenuY = 0;
            return;
        }
        String label = chinese ? "布局" : "Layout";
        float tw = customFont ? (float)FontManager.ui.getWidth(label) : TextUtil.getWidth(label);
        float th = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
        float pad = 4.0f;
        float btnW = tw + pad * 2.0f;
        float btnH = th + 4.0f;
        float btnX = frame.unitMouseX(minX);
        float btnY = frame.unitMouseY(minY) - btnH - 2.0f;
        float maxH = frame.scale == 0.0f ? (float)frame.screenH : ((float)frame.screenH - frame.slideY) / frame.scale;
        if (btnY < 1.0f) {
            btnY = 1.0f;
        }
        if (btnY + btnH > maxH - 1.0f) {
            btnY = maxH - btnH - 1.0f;
        }
        float anchorX = btnX + btnW;
        float anchorY = btnY;
        this.layoutMenuX = Math.round(frame.scale == 0.0f ? anchorX : anchorX * frame.scale);
        this.layoutMenuY = Math.round(frame.scale == 0.0f ? anchorY : anchorY * frame.scale + frame.slideY);
    }

    private void closeLayoutMenu() {
        this.layoutMenuOpen = false;
    }

    private boolean isHoveringLayoutButton(float mx, float my, ClickGuiFrame frame) {
        if (this.selectedHud.isEmpty()) {
            return false;
        }
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            int bw = hm.getHudBoundW();
            int bh = hm.getHudBoundH();
            if (bw <= 0 || bh <= 0) {
                continue;
            }
            minX = Math.min(minX, hm.getHudBoundX());
            minY = Math.min(minY, hm.getHudBoundY());
        }
        if (minX == Integer.MAX_VALUE) {
            return false;
        }
        String label = chinese ? "布局" : "Layout";
        float tw = customFont ? (float)FontManager.ui.getWidth(label) : TextUtil.getWidth(label);
        float th = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
        float pad = 4.0f;
        float bw = tw + pad * 2.0f;
        float bh = th + 4.0f;
        float bx = frame.unitMouseX(minX);
        float by = frame.unitMouseY(minY) - bh - 2.0f;
        float maxH = frame.scale == 0.0f ? (float)frame.screenH : ((float)frame.screenH - frame.slideY) / frame.scale;
        if (by < 1.0f) {
            by = 1.0f;
        }
        if (by + bh > maxH - 1.0f) {
            by = maxH - bh - 1.0f;
        }
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    private String[] getLayoutMenuLabels(boolean chinese) {
        if (chinese) {
            return new String[]{"网格 (G)", "水平 (H)", "垂直 (V)"};
        }
        return new String[]{"Grid (G)", "Horizontal (H)", "Vertical (V)"};
    }

    private boolean isLayoutMenuVisible() {
        if (!this.layoutMenuOpen && this.selectedHud.isEmpty()) {
            return false;
        }
        double p = this.layoutMenuAnim.get(this.layoutMenuOpen ? 1.0 : 0.0, 160L, Easing.CubicInOut);
        return p > 0.01;
    }

    private boolean handleLayoutMenuClick(int mouseX, int mouseY, int mouseButton, ClickGuiFrame frame) {
        double p = this.layoutMenuAnim.get(this.layoutMenuOpen ? 1.0 : 0.0, 160L, Easing.CubicInOut);
        if (p <= 0.01 || this.selectedHud.isEmpty()) {
            return false;
        }
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            this.closeLayoutMenu();
            return false;
        }
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        String[] labels = this.getLayoutMenuLabels(chinese);
        float mx = frame.unitMouseX(mouseX);
        float my = frame.unitMouseY(mouseY);
        float x = frame.unitMouseX(this.layoutMenuX);
        float y = frame.unitMouseY(this.layoutMenuY);
        float textH = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
        float rowH = textH + 6.0f;
        float pad = 6.0f;
        float maxW = 0.0f;
        for (String s : labels) {
            float w = customFont ? (float)FontManager.ui.getWidth(s) : TextUtil.getWidth(s);
            if (w > maxW) {
                maxW = w;
            }
        }
        float menuW = maxW + pad * 2.0f;
        float menuH = rowH * (float)labels.length + pad * 2.0f;
        float progress = (float)p;
        float menuScale = 0.85f + 0.15f * progress;
        float scaledW = menuW * menuScale;
        float scaledH = menuH * menuScale;
        boolean inside = mx >= x && mx <= x + scaledW && my >= y && my <= y + scaledH;
        if (!inside) {
            if (mouseButton == 0) {
                this.closeLayoutMenu();
                return false;
            }
            this.closeLayoutMenu();
            return true;
        }
        if (mouseButton != 0) {
            return true;
        }
        float u = (mx - x) / Math.max(0.0001f, menuScale);
        float v = (my - y) / Math.max(0.0001f, menuScale);
        int idx = (int)Math.floor((double)((v - pad) / rowH));
        if (idx < 0 || idx >= labels.length) {
            return true;
        }
        if (idx == 0) {
            this.applyGridLayout();
        } else if (idx == 1) {
            this.applyHorizontalLayout();
        } else if (idx == 2) {
            this.applyVerticalLayout();
        }
        this.closeLayoutMenu();
        return true;
    }

    private void renderLayoutMenu(DrawContext context, ClickGuiFrame frame, int mouseX, int mouseY) {
        double p = this.layoutMenuAnim.get(this.layoutMenuOpen ? 1.0 : 0.0, 160L, Easing.CubicInOut);
        float progress = (float)p;
        if (progress <= 0.01f || this.selectedHud.isEmpty()) {
            return;
        }
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            this.closeLayoutMenu();
            return;
        }
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        String[] labels = this.getLayoutMenuLabels(chinese);
        float x = frame.unitMouseX(this.layoutMenuX);
        float y = frame.unitMouseY(this.layoutMenuY);
        float textH = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
        float rowH = textH + 6.0f;
        float pad = 6.0f;
        float maxW = 0.0f;
        for (String s : labels) {
            float w = customFont ? (float)FontManager.ui.getWidth(s) : TextUtil.getWidth(s);
            if (w > maxW) {
                maxW = w;
            }
        }
        float menuW = maxW + pad * 2.0f;
        float menuH = rowH * (float)labels.length + pad * 2.0f;
        float maxWUnit = frame.scale == 0.0f ? (float)frame.screenW : (float)frame.screenW / frame.scale;
        float maxHUnit = frame.scale == 0.0f ? (float)frame.screenH : ((float)frame.screenH - frame.slideY) / frame.scale;
        if (x + menuW > maxWUnit - 2.0f) {
            x = maxWUnit - menuW - 2.0f;
        }
        if (y + menuH > maxHUnit - 2.0f) {
            y = maxHUnit - menuH - 2.0f;
        }
        if (x < 2.0f) {
            x = 2.0f;
        }
        if (y < 2.0f) {
            y = 2.0f;
        }
        float cx = x + menuW / 2.0f;
        float cy = y + menuH / 2.0f;
        float menuScale = 0.85f + 0.15f * progress;
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0.0f);
        context.getMatrices().scale(menuScale, menuScale, 1.0f);
        context.getMatrices().translate(-cx, -cy, 0.0f);

        int bgA = Math.max(0, Math.min(255, (int)Math.round(245.0 * (double)progress)));
        int outA = Math.max(0, Math.min(255, (int)Math.round(210.0 * (double)progress)));
        Color bg = ColorUtil.injectAlpha(gui.defaultColor.getValue(), bgA);
        Color out = ColorUtil.injectAlpha(gui.hoverColor.getValue(), outA);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), x, y, menuW, menuH, bg, out);

        float mx = frame.unitMouseX(mouseX);
        float my = frame.unitMouseY(mouseY);
        float localMx = x + (mx - x) / Math.max(0.0001f, menuScale);
        float localMy = y + (my - y) / Math.max(0.0001f, menuScale);
        int textA = Math.max(0, Math.min(255, (int)Math.round((double)gui.enableTextColor.getValue().getAlpha() * (double)progress)));
        int textColor = ColorUtil.injectAlpha(gui.enableTextColor.getValue(), textA).getRGB();
        for (int i = 0; i < labels.length; ++i) {
            float ry = y + pad + (float)i * rowH;
            boolean hovered = localMx >= x && localMx <= x + menuW && localMy >= ry && localMy <= ry + rowH;
            if (hovered) {
                Render2DUtil.drawRect(context.getMatrices(), x + 1.0f, ry, menuW - 2.0f, rowH, ColorUtil.injectAlpha(gui.hoverColor.getValue(), Math.max(0, Math.min(255, (int)Math.round(160.0 * (double)progress)))));
            }
            TextUtil.drawString(context, labels[i], (double)(x + pad), (double)(ry + 2.0f), textColor, customFont, shadow);
        }
        context.getMatrices().pop();
    }

    private void applyGridLayout() {
        if (this.selectedHud.isEmpty()) {
            return;
        }
        this.selectedHud.sort(Comparator.comparingInt(HudModule::getHudBoundY).thenComparingInt(HudModule::getHudBoundX));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxW = 0;
        int maxH = 0;
        for (HudModule hm : this.selectedHud) {
            minX = Math.min(minX, hm.getHudBoundX());
            minY = Math.min(minY, hm.getHudBoundY());
            maxW = Math.max(maxW, Math.max(1, hm.getHudBoundW()));
            maxH = Math.max(maxH, Math.max(1, hm.getHudBoundH()));
        }
        int pad = 2;
        int cellW = maxW + pad;
        int cellH = maxH + pad;
        int n = this.selectedHud.size();
        int cols = (int)Math.ceil(Math.sqrt((double)n));
        cols = Math.max(1, cols);
        for (int i = 0; i < n; ++i) {
            int col = i % cols;
            int row = i / cols;
            int x = minX + col * cellW;
            int y = minY + row * cellH;
            this.selectedHud.get(i).setHudPosFromBounds(x, y);
        }
    }

    private void applyHorizontalLayout() {
        if (this.selectedHud.isEmpty()) {
            return;
        }
        this.selectedHud.sort(Comparator.comparingInt(HudModule::getHudBoundX));
        int minY = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            minY = Math.min(minY, hm.getHudBoundY());
        }
        int pad = 2;
        int x = this.selectedHud.get(0).getHudBoundX();
        for (HudModule hm : this.selectedHud) {
            hm.setHudPosFromBounds(x, minY);
            int w = Math.max(1, hm.getHudBoundW());
            x += w + pad;
        }
    }

    private void applyVerticalLayout() {
        if (this.selectedHud.isEmpty()) {
            return;
        }
        this.selectedHud.sort(Comparator.comparingInt(HudModule::getHudBoundY));
        int minX = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            minX = Math.min(minX, hm.getHudBoundX());
        }
        int pad = 2;
        int y = this.selectedHud.get(0).getHudBoundY();
        for (HudModule hm : this.selectedHud) {
            hm.setHudPosFromBounds(minX, y);
            int h = Math.max(1, hm.getHudBoundH());
            y += h + pad;
        }
    }

    private void applyAlignLeft() {
        if (this.selectedHud.isEmpty()) {
            return;
        }
        int minX = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            minX = Math.min(minX, hm.getHudBoundX());
        }
        for (HudModule hm : this.selectedHud) {
            hm.setHudPosFromBounds(minX, hm.getHudBoundY());
        }
    }

    private void applyAlignTop() {
        if (this.selectedHud.isEmpty()) {
            return;
        }
        int minY = Integer.MAX_VALUE;
        for (HudModule hm : this.selectedHud) {
            minY = Math.min(minY, hm.getHudBoundY());
        }
        for (HudModule hm : this.selectedHud) {
            hm.setHudPosFromBounds(hm.getHudBoundX(), minY);
        }
    }
}
