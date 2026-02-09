/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.text.Text
 */
package dev.idhammai.mod.gui.clickgui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.Frog;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.AnimateUtil;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.mod.gui.clickgui.pages.ClickGuiAiAssistantPage;
import dev.idhammai.mod.gui.clickgui.pages.ClickGuiConfigPage;
import dev.idhammai.mod.gui.clickgui.pages.ClickGuiHudPage;
import dev.idhammai.mod.gui.clickgui.pages.ClickGuiModulePage;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.gui.items.Item;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.core.impl.CommandManager;
import dev.idhammai.mod.gui.windows.WindowBase;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class ClickGuiScreen
extends Screen {
    private static ClickGuiScreen INSTANCE = new ClickGuiScreen();
    private final ArrayList<Component> components = new ArrayList();
    private float mouseMoveOffsetX;
    private float mouseMoveOffsetY;
    private float walkShakeOffsetX;
    private float walkShakeOffsetY;
    private float walkShakeTime;
    private boolean layoutCorrected = false;
    private int lastLayoutScreenW = -1;
    private int lastLayoutScreenH = -1;
    private final Random snowRandom = new Random();
    private final ArrayList<Snowflake> snowflakes = new ArrayList();
    private final ArrayList<TopTab> topTabs = new ArrayList();
    private Page page = Page.Module;
    private final Animation pageSlide = new Animation();
    private final ClickGuiModulePage modulePage = new ClickGuiModulePage(this);
    private final ClickGuiConfigPage configPage = new ClickGuiConfigPage(this);
    private final ClickGuiHudPage hudPage = new ClickGuiHudPage(this);
    private final ClickGuiAiAssistantPage aiAssistantPage = new ClickGuiAiAssistantPage(this);
    private ClickGuiFrame lastFrame;
    private float topTabAnimX;
    private float topTabAnimW;
    private boolean topTabAnimInit;
    private boolean confirmOpen;
    private String confirmTitle;
    private String confirmMessage;
    private Runnable confirmYesAction;

    public ClickGuiScreen() {
        super((Text)Text.literal((String)"Frog"));
        this.setInstance();
        this.load();
    }

    public static ClickGuiScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGuiScreen();
        }
        return INSTANCE;
    }

    public Page getPage() {
        return this.page;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        this.topTabs.clear();
        this.topTabs.add(new TopTab(Page.Module, "Module", "模块"));
        this.topTabs.add(new TopTab(Page.Config, "Config", "配置"));
        this.topTabs.add(new TopTab(Page.Hud, "HUD", "HUD"));
        this.topTabs.add(new TopTab(Page.AiAssistant, "AI Assistant", "AI助手"));
        this.modulePage.load();
        this.hudPage.init();
    }

    private void renderHudModules(DrawContext context, float delta) {
        for (Module module : Frog.MODULE.getModules()) {
            if (!(module instanceof HudModule) || !module.isOn()) continue;
            try {
                module.onRender2D(context, delta);
            }
            catch (Exception e) {
                e.printStackTrace();
                if (ClientSetting.INSTANCE == null || !ClientSetting.INSTANCE.debug.getValue()) continue;
                CommandManager.sendMessage("\u00a74An error has occurred (" + module.getName() + " [onRender2D]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float keyCodec = (float)ClickGui.getInstance().alphaValue;
        float scale = 0.92f + 0.08f * keyCodec;
        float slideY = (1.0f - keyCodec) * 20.0f;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)keyCodec);
        Item.context = context;
        this.renderBackground(context, mouseX, mouseY, delta);
        if (this.page == Page.Hud) {
            this.renderHudModules(context, delta);
        }
        ClickGui clickGui = ClickGui.getInstance();
        if (clickGui != null && clickGui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            clickGui.updateSpectrumLut(context.getScaledWindowHeight());
        }
        if (Wrapper.mc != null && Wrapper.mc.getWindow() != null) {
            int sw = Wrapper.mc.getWindow().getScaledWidth();
            int sh = Wrapper.mc.getWindow().getScaledHeight();
            if (sw != this.lastLayoutScreenW || sh != this.lastLayoutScreenH) {
                this.layoutCorrected = false;
                this.lastLayoutScreenW = sw;
                this.lastLayoutScreenH = sh;
                this.hudPage.resetHudLayout();
            }
        }
        if (!this.layoutCorrected && Wrapper.mc != null && Wrapper.mc.getWindow() != null) {
            int categoryWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().categoryWidth.getValueInt() : 101;
            int moduleButtonWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().moduleButtonWidth.getValueInt() : 93;
            int layoutWidth = Math.max(categoryWidth, moduleButtonWidth);
            int spacing = layoutWidth + 1;
            int count = this.components.size();
            if (count > 0) {
                int screenWidth = Wrapper.mc.getWindow().getScaledWidth();
                int screenHeight = Wrapper.mc.getWindow().getScaledHeight();
                int totalWidth = count * layoutWidth + (count - 1);
                int startX = Math.round(((float)screenWidth - (float)totalWidth) / 2.0f);
                int startY = Math.round((float)screenHeight / 6.0f);
                int offsetX = Math.round(((float)layoutWidth - (float)moduleButtonWidth) / 2.0f);
                int x = startX - spacing;
                for (Component component : this.components) {
                    x += spacing;
                    component.setX(x + offsetX);
                    component.setY(startY);
                }
            }
            this.layoutCorrected = true;
        }
        boolean dragging = false;
        for (Component c : this.components) {
            if (!c.drag) continue;
            dragging = true;
            break;
        }
        float targetOffsetX = 0.0f;
        float targetOffsetY = 0.0f;
        if (ClickGui.getInstance().mouseMove.getValue() && !dragging) {
            float strength = ClickGui.getInstance().mouseMoveStrength.getValueFloat() * (float)ClickGui.getInstance().alphaValue;
            float cx = (float)context.getScaledWindowWidth() / 2.0f;
            float cy = (float)context.getScaledWindowHeight() / 2.0f;
            float nx = cx <= 0.0f ? 0.0f : ((float)mouseX - cx) / cx;
            float ny = cy <= 0.0f ? 0.0f : ((float)mouseY - cy) / cy;
            nx = Math.max(-1.0f, Math.min(1.0f, nx));
            ny = Math.max(-1.0f, Math.min(1.0f, ny));
            targetOffsetX = nx * strength;
            targetOffsetY = ny * strength;
        }
        float smooth = ClickGui.getInstance().mouseMoveSmooth.getValueFloat();
        if (smooth <= 0.0f) {
            this.mouseMoveOffsetX = targetOffsetX;
            this.mouseMoveOffsetY = targetOffsetY;
        } else {
            float a = AnimateUtil.deltaTime() * smooth;
            if (a < 0.0f) {
                a = 0.0f;
            }
            if (a > 0.35f) {
                a = 0.35f;
            }
            this.mouseMoveOffsetX += (targetOffsetX - this.mouseMoveOffsetX) * a;
            this.mouseMoveOffsetY += (targetOffsetY - this.mouseMoveOffsetY) * a;
        }

        float targetWalkX = 0.0f;
        float targetWalkY = 0.0f;
        float maxWalk = 0.0f;
        if (ClickGui.getInstance().walkShake.getValue() && !dragging && Wrapper.mc.player != null) {
            Vec3d v = Wrapper.mc.player.getVelocity();
            float horizontalSpeed = (float)Math.sqrt(v.x * v.x + v.z * v.z);
            float moving = horizontalSpeed > 0.003f ? Math.min(1.0f, horizontalSpeed * 18.0f) : 0.0f;
            float dt = AnimateUtil.deltaTime();
            if (moving > 0.0f) {
                float speed = ClickGui.getInstance().walkShakeSpeed.getValueFloat();
                this.walkShakeTime += dt * speed * (0.4f + 0.6f * moving);
            }
            float strength = ClickGui.getInstance().walkShakeStrength.getValueFloat() * moving * (float)ClickGui.getInstance().alphaValue;
            maxWalk = ClickGui.getInstance().walkShakeMax.getValueFloat() * (float)ClickGui.getInstance().alphaValue;
            targetWalkX = (float)Math.sin((double)this.walkShakeTime) * strength;
            targetWalkY = (float)Math.cos((double)(this.walkShakeTime * 2.0f)) * strength * 0.35f;
            targetWalkX = Math.max(-maxWalk, Math.min(maxWalk, targetWalkX));
            targetWalkY = Math.max(-maxWalk, Math.min(maxWalk, targetWalkY));
        }

        float walkSmooth = ClickGui.getInstance().walkShakeSmooth.getValueFloat();
        if (walkSmooth <= 0.0f) {
            this.walkShakeOffsetX = targetWalkX;
            this.walkShakeOffsetY = targetWalkY;
        } else {
            float a = AnimateUtil.deltaTime() * walkSmooth;
            if (a < 0.0f) {
                a = 0.0f;
            }
            if (a > 0.35f) {
                a = 0.35f;
            }
            this.walkShakeOffsetX += (targetWalkX - this.walkShakeOffsetX) * a;
            this.walkShakeOffsetY += (targetWalkY - this.walkShakeOffsetY) * a;
        }
        if (maxWalk > 0.0f) {
            this.walkShakeOffsetX = Math.max(-maxWalk, Math.min(maxWalk, this.walkShakeOffsetX));
            this.walkShakeOffsetY = Math.max(-maxWalk, Math.min(maxWalk, this.walkShakeOffsetY));
        }

        float totalOffsetX = this.mouseMoveOffsetX + this.walkShakeOffsetX;
        float totalOffsetY = this.mouseMoveOffsetY + this.walkShakeOffsetY;
        this.components.forEach(c -> c.setMouseMoveOffset(totalOffsetX, totalOffsetY));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Component c : this.components) {
            minX = Math.min(minX, c.getX());
            minY = Math.min(minY, c.getY());
            maxX = Math.max(maxX, c.getX() + c.getWidth());
            maxY = Math.max(maxY, c.getY() + c.getHeight());
        }
        int margin = 16;
        int panelX = Math.max(8, minX - margin);
        int panelY = Math.max(6, minY - margin);
        int panelW = Math.min(context.getScaledWindowWidth() - panelX - 8, maxX - minX + margin * 2);
        int panelH = Math.min(context.getScaledWindowHeight() - panelY - 6, maxY - minY + margin * 2 + 24);
        boolean focused = mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH;
        int alpha = focused ? (int)Math.round(242.25) : (int)Math.round(226.95000000000002);
        if (ClickGui.getInstance().blur.getValue() && this.page != Page.Hud) {
            float blurRadius = 1.0f + (ClickGui.getInstance().radius.getValueFloat() - 1.0f) * (float)ClickGui.getInstance().alphaValue;
            Frog.BLUR.applyBlur(blurRadius, 0.0f, 0.0f, (float)context.getScaledWindowWidth(), (float)context.getScaledWindowHeight(), (float)ClickGui.getInstance().blurType.getValue().ordinal());
        }
        this.renderSnow(context);
        this.renderTopTabs(context, mouseX, mouseY);
        context.getMatrices().push();
        context.getMatrices().translate((float)panelX + (float)panelW / 2.0f, (float)panelY + (float)panelH / 2.0f + slideY, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-((float)panelX + (float)panelW / 2.0f), -((float)panelY + (float)panelH / 2.0f), 0.0f);
        // Render2DUtil.drawRoundedRect(context.getMatrices(), panelX, panelY, panelW, panelH, r, new Color(255, 255, 255, alpha));
        int strokeA = Math.max(0, Math.min(255, (int)Math.round((double)alpha * 0.22)));
        // Render2DUtil.drawRoundedStroke(context.getMatrices(), panelX, panelY, panelW, panelH, r, new Color(220, 224, 230, strokeA), 48);
        context.getMatrices().pop();
        int screenW = context.getScaledWindowWidth();
        int categoryWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().categoryWidth.getValueInt() : 101;
        int moduleButtonWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().moduleButtonWidth.getValueInt() : 93;
        int layoutWidth = Math.max(categoryWidth, moduleButtonWidth);
        int count = this.components.size();
        int totalWidth = count > 0 ? count * layoutWidth + (count - 1) : screenW;
        int pageW = Math.max(screenW, totalWidth + 32);
        float pageX = (float)this.pageSlide.get(-((double)this.page.ordinal() * (double)pageW), 260L, Easing.SineOut);
        float pageOffsetX = scale == 0.0f ? pageX : pageX / scale;
        this.components.forEach(c -> c.setPageOffsetX(pageOffsetX));
        ClickGuiFrame frame = new ClickGuiFrame(scale, slideY, pageOffsetX, pageW, panelX, panelY, panelW, panelH, totalOffsetX, totalOffsetY, context.getScaledWindowWidth(), context.getScaledWindowHeight());
        this.lastFrame = frame;
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, slideY, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        this.modulePage.render(context, mouseX, mouseY, delta);
        this.configPage.render(context, mouseX, mouseY, delta, frame);
        this.hudPage.render(context, mouseX, mouseY, delta, frame);
        this.aiAssistantPage.render(context, mouseX, mouseY, delta, frame);
        context.getMatrices().pop();
        ClickGui gui = ClickGui.getInstance();
        if (gui != null && gui.tips.getValue()) {
            context.getMatrices().push();
            context.getMatrices().translate(pageX, 0.0f, 0.0f);
            boolean customFont = FontManager.isCustomFontEnabled();
            boolean shadow = FontManager.isShadowEnabled();
            float lineHeight = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
            float marginBottom = 6.0f;
            int lines = 5;
            float baseY = (float)context.getScaledWindowHeight() - marginBottom - lineHeight * (float)lines;
            int tipX = 6;
            int tipY = Math.round(baseY);
            boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
            String tip1 = chinese ? "左键拖动列 右键展开/折叠" : "LMB drag, RMB expand/collapse";
            String tip2 = chinese ? "滚轮是上下移动 SHIFT+滚轮是左右移动" : "Scroll up/down, SHIFT+scroll left/right";
            String tip3 = chinese ? "SHIFT+左键 快捷键按钮 切换功能(按住/松开)触发" : "SHIFT+LMB: toggle hold/release";
            String tip4 = chinese ? "SHIFT+左键 功能按钮 重置设置" : "SHIFT+LMB: reset this setting";
            String tip5 = chinese ? "文本设置 右键编辑" : "RMB on String setting: edit";
            boolean spectrumTips = gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum;
            double delay1 = spectrumTips ? (double)tipY * 0.25 : (double)tipY / 10.0;
            Color c1 = gui.getActiveColor(delay1);
            int color1 = ColorUtil.injectAlpha(c1, alpha).getRGB();
            TextUtil.drawString(context, tip1, tipX, tipY, color1, customFont, shadow);
            int tipY2 = (int)((float)tipY + lineHeight);
            double delay2 = spectrumTips ? (double)tipY2 * 0.25 : (double)tipY2 / 10.0;
            Color c2 = gui.getActiveColor(delay2);
            int color2 = ColorUtil.injectAlpha(c2, alpha).getRGB();
            TextUtil.drawString(context, tip2, tipX, tipY2, color2, customFont, shadow);
            int tipY3 = (int)((float)tipY + lineHeight * 2.0f);
            double delay3 = spectrumTips ? (double)tipY3 * 0.25 : (double)tipY3 / 10.0;
            Color c3 = gui.getActiveColor(delay3);
            int color3 = ColorUtil.injectAlpha(c3, alpha).getRGB();
            TextUtil.drawString(context, tip3, tipX, tipY3, color3, customFont, shadow);
            int tipY4 = (int)((float)tipY + lineHeight * 3.0f);
            double delay4 = spectrumTips ? (double)tipY4 * 0.25 : (double)tipY4 / 10.0;
            Color c4 = gui.getActiveColor(delay4);
            int color4 = ColorUtil.injectAlpha(c4, alpha).getRGB();
            TextUtil.drawString(context, tip4, tipX, tipY4, color4, customFont, shadow);
            int tipY5 = (int)((float)tipY + lineHeight * 4.0f);
            double delay5 = spectrumTips ? (double)tipY5 * 0.25 : (double)tipY5 / 10.0;
            Color c5 = gui.getActiveColor(delay5);
            int color5 = ColorUtil.injectAlpha(c5, alpha).getRGB();
            TextUtil.drawString(context, tip5, tipX, tipY5, color5, customFont, shadow);
            context.getMatrices().pop();
        }
        if (this.confirmOpen) {
            this.renderConfirmDialog(context, mouseX, mouseY);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (this.confirmOpen) {
            return this.handleConfirmClick((int)mouseX, (int)mouseY, clickedButton);
        }
        if (clickedButton == 0 && this.handleTopTabClick((int)mouseX, (int)mouseY)) {
            return true;
        }
        if (this.page == Page.Module) {
            this.modulePage.mouseClicked((int)mouseX, (int)mouseY, clickedButton);
            return super.mouseClicked(mouseX, mouseY, clickedButton);
        }
        ClickGuiFrame frame = this.lastFrame;
        if (frame == null) {
            return super.mouseClicked(mouseX, mouseY, clickedButton);
        }
        if (this.page == Page.Config && this.configPage.mouseClicked((int)mouseX, (int)mouseY, clickedButton, frame)) {
            return true;
        }
        if (this.page == Page.Hud && this.hudPage.mouseClicked((int)mouseX, (int)mouseY, clickedButton, frame)) {
            return true;
        }
        if (this.page == Page.AiAssistant && this.aiAssistantPage.mouseClicked((int)mouseX, (int)mouseY, clickedButton, frame)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int releaseButton) {
        if (this.page == Page.Module) {
            this.modulePage.mouseReleased((int)mouseX, (int)mouseY, releaseButton);
        } else if (this.page == Page.Hud) {
            ClickGuiFrame frame = this.lastFrame;
            int mX = frame != null ? (int)frame.unitMouseX((int)mouseX) : (int)mouseX;
            int mY = frame != null ? (int)frame.unitMouseY((int)mouseY) : (int)mouseY;
            this.hudPage.mouseReleased(mX, mY, releaseButton);
        }
        return super.mouseReleased(mouseX, mouseY, releaseButton);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.page == Page.Module) {
            this.modulePage.mouseScrolled(verticalAmount);
        } else if (this.page == Page.Config) {
            if (Wrapper.mc != null && Wrapper.mc.getWindow() != null) {
                this.configPage.mouseScrolled(verticalAmount, Wrapper.mc.getWindow().getScaledHeight());
            }
        } else if (this.page == Page.Hud) {
            this.hudPage.mouseScrolled(verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirmOpen) {
            if (keyCode == 256) {
                this.closeConfirm();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                this.confirmYes();
                return true;
            }
            return true;
        }
        if (this.page == Page.Module) {
            this.modulePage.keyPressed(keyCode);
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.page == Page.Hud) {
            this.hudPage.keyPressed(keyCode);
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.page == Page.Config && this.configPage.keyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.page == Page.Module) {
            this.modulePage.charTyped(chr, modifiers);
            return super.charTyped(chr, modifiers);
        }
        if (this.page == Page.Hud) {
            this.hudPage.charTyped(chr, modifiers);
            return super.charTyped(chr, modifiers);
        }
        if (this.page == Page.Config && this.configPage.charTyped(chr)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    public void openConfirm(String title, String message, Runnable yesAction) {
        this.confirmOpen = true;
        this.confirmTitle = title;
        this.confirmMessage = message;
        this.confirmYesAction = yesAction;
        this.configPage.stopNameListening();
    }

    private void closeConfirm() {
        this.confirmOpen = false;
        this.confirmTitle = null;
        this.confirmMessage = null;
        this.confirmYesAction = null;
    }

    private void confirmYes() {
        if (this.confirmYesAction != null) {
            try {
                this.confirmYesAction.run();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.closeConfirm();
    }

    private boolean handleConfirmClick(int mouseX, int mouseY, int clickedButton) {
        if (clickedButton != 0) {
            this.closeConfirm();
            return true;
        }
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            this.closeConfirm();
            return true;
        }
        int sw = Item.context != null ? Item.context.getScaledWindowWidth() : (Wrapper.mc != null && Wrapper.mc.getWindow() != null ? Wrapper.mc.getWindow().getScaledWidth() : 0);
        int sh = Item.context != null ? Item.context.getScaledWindowHeight() : (Wrapper.mc != null && Wrapper.mc.getWindow() != null ? Wrapper.mc.getWindow().getScaledHeight() : 0);
        if (sw <= 0 || sh <= 0) {
            this.closeConfirm();
            return true;
        }
        boolean customFont = FontManager.isCustomFontEnabled();
        float lineH = (float)(customFont ? (int)FontManager.ui.getFontHeight() : TextUtil.getHeight());
        float pad = 10.0f;
        float boxW = Math.min(340.0f, (float)sw - 40.0f);
        float btnH = lineH + 6.0f;
        float boxH = pad + lineH + 6.0f + lineH + 12.0f + btnH + pad;
        float x = ((float)sw - boxW) / 2.0f;
        float y = ((float)sh - boxH) / 2.0f;
        float gap = 8.0f;
        float btnW = (boxW - pad * 2.0f - gap) / 2.0f;
        float btnY = y + boxH - pad - btnH;
        float yesX = x + pad;
        float noX = yesX + btnW + gap;
        boolean inYes = (float)mouseX >= yesX && (float)mouseX <= yesX + btnW && (float)mouseY >= btnY && (float)mouseY <= btnY + btnH;
        boolean inNo = (float)mouseX >= noX && (float)mouseX <= noX + btnW && (float)mouseY >= btnY && (float)mouseY <= btnY + btnH;
        if (inYes) {
            this.confirmYes();
            return true;
        }
        if (inNo) {
            this.closeConfirm();
            return true;
        }
        this.closeConfirm();
        return true;
    }

    private void renderConfirmDialog(DrawContext context, int mouseX, int mouseY) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return;
        }
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        Render2DUtil.rect(context.getMatrices(), 0.0f, 0.0f, (float)sw, (float)sh, new Color(0, 0, 0, 140).getRGB());
        float lineH = (float)(customFont ? (int)FontManager.ui.getFontHeight() : TextUtil.getHeight());
        float pad = 10.0f;
        float boxW = Math.min(340.0f, (float)sw - 40.0f);
        float btnH = lineH + 6.0f;
        float boxH = pad + lineH + 6.0f + lineH + 12.0f + btnH + pad;
        float x = ((float)sw - boxW) / 2.0f;
        float y = ((float)sh - boxH) / 2.0f;
        Render2DUtil.rect(context.getMatrices(), x, y, x + boxW, y + boxH, gui.defaultColor.getValue().getRGB());
        String title = this.confirmTitle == null || this.confirmTitle.isEmpty() ? (chinese ? "确认" : "Confirm") : this.confirmTitle;
        String msg = this.confirmMessage == null ? "" : this.confirmMessage;
        float titleY = y + pad;
        float msgY = titleY + lineH + 6.0f;
        float titleX = x + (boxW - (float)this.getTextWidth(title)) / 2.0f;
        float msgX = x + (boxW - (float)this.getTextWidth(msg)) / 2.0f;
        TextUtil.drawString(context, title, (double)titleX, (double)titleY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        TextUtil.drawString(context, msg, (double)msgX, (double)msgY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float gap = 8.0f;
        float btnW = (boxW - pad * 2.0f - gap) / 2.0f;
        float btnY = y + boxH - pad - btnH;
        float yesX = x + pad;
        float noX = yesX + btnW + gap;
        boolean hYes = (float)mouseX >= yesX && (float)mouseX <= yesX + btnW && (float)mouseY >= btnY && (float)mouseY <= btnY + btnH;
        boolean hNo = (float)mouseX >= noX && (float)mouseX <= noX + btnW && (float)mouseY >= btnY && (float)mouseY <= btnY + btnH;
        int activeAlpha = hYes ? gui.hoverAlpha.getValueInt() : gui.alpha.getValueInt();
        if (gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            Render2DUtil.drawLutRect(context.getMatrices(), yesX, btnY, btnW, btnH, gui.getSpectrumLutId(), gui.getSpectrumLutHeight(), activeAlpha);
        } else {
            Color ac = gui.getActiveColor((double)btnY * 0.25);
            Render2DUtil.rect(context.getMatrices(), yesX, btnY, yesX + btnW, btnY + btnH, ColorUtil.injectAlpha(ac, activeAlpha).getRGB());
        }
        int bgNo = hNo ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), noX, btnY, noX + btnW, btnY + btnH, bgNo);
        String yes = chinese ? "确认" : "Yes";
        String no = chinese ? "取消" : "No";
        float yesTx = yesX + (btnW - (float)this.getTextWidth(yes)) / 2.0f;
        float noTx = noX + (btnW - (float)this.getTextWidth(no)) / 2.0f;
        float btnTy = this.getCenteredTextY(btnY, btnH);
        TextUtil.drawString(context, yes, (double)yesTx, (double)btnTy, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        TextUtil.drawString(context, no, (double)noTx, (double)btnTy, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
    }

    public boolean shouldPause() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return this.components;
    }

    public int getTextOffset() {
        return -ClickGui.getInstance().textOffset.getValueInt() - 6;
    }

    private void setPage(Page page) {
        this.setPage(page, true);
    }

    private void setPage(Page page, boolean resetHud) {
        if (page == null) {
            return;
        }
        this.page = page;
        for (Component c : this.components) {
            c.drag = false;
        }
        this.configPage.stopNameListening();
        if (page == Page.Config) {
            this.configPage.onOpen();
        }
        if (page == Page.Hud && resetHud) {
            this.hudPage.resetHudLayout();
        }
    }

    public void openHudWindow(WindowBase window) {
        this.hudPage.openHudWindow(window);
        this.setPage(Page.Hud, false);
    }

    public int getFontHeight() {
        if (FontManager.isCustomFontEnabled()) {
            return (int)FontManager.ui.getFontHeight();
        }
        return 9;
    }

    public int getTextWidth(String s) {
        if (FontManager.isCustomFontEnabled()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return Wrapper.mc != null ? Wrapper.mc.textRenderer.getWidth(s) : 0;
    }

    public float getCenteredTextY(float baseY, float boxHeight) {
        return baseY + (boxHeight - (float)this.getFontHeight()) / 2.0f + (float)ClickGui.getInstance().textOffset.getValueInt();
    }

    private void updateTopTabsLayout(int screenWidth, boolean chinese) {
        int gap = 0;
        int padX = 8;
        int y = 6;
        int h = this.getFontHeight() + 6;
        int total = 0;
        for (int i = 0; i < this.topTabs.size(); ++i) {
            TopTab tab = this.topTabs.get(i);
            String label = tab.getLabel(chinese);
            int w = this.getTextWidth(label) + padX * 2;
            tab.w = w;
            tab.h = h;
            tab.y = y;
            total += w;
            if (i != this.topTabs.size() - 1) {
                total += gap;
            }
        }
        int x = Math.round(((float)screenWidth - (float)total) / 2.0f);
        for (int i = 0; i < this.topTabs.size(); ++i) {
            TopTab tab = this.topTabs.get(i);
            tab.x = x;
            x += tab.w + gap;
        }
    }

    private void renderTopTabs(DrawContext context, int mouseX, int mouseY) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return;
        }
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return;
        }
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        this.updateTopTabsLayout(Wrapper.mc.getWindow().getScaledWidth(), chinese);
        int padX = 8;
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();

        TopTab activeTab = null;
        for (TopTab tab : this.topTabs) {
            if (this.page != tab.page) continue;
            activeTab = tab;
            break;
        }
        if (activeTab == null) {
            return;
        }

        float dt = AnimateUtil.deltaTime();
        if (dt <= 0.0f) {
            dt = 0.016f;
        }
        float a = dt * 18.0f;
        if (a < 0.0f) {
            a = 0.0f;
        }
        if (a > 0.35f) {
            a = 0.35f;
        }
        float targetX = (float)activeTab.x;
        float targetW = (float)activeTab.w;
        if (!this.topTabAnimInit) {
            this.topTabAnimX = targetX;
            this.topTabAnimW = targetW;
            this.topTabAnimInit = true;
        } else {
            this.topTabAnimX += (targetX - this.topTabAnimX) * a;
            this.topTabAnimW += (targetW - this.topTabAnimW) * a;
        }

        for (TopTab tab : this.topTabs) {
            boolean hovered = mouseX >= tab.x && mouseX <= tab.x + tab.w && mouseY >= tab.y && mouseY <= tab.y + tab.h;
            int base = gui.defaultColor.getValue().getRGB();
            int hov = gui.hoverColor.getValue().getRGB();
            Render2DUtil.rect(context.getMatrices(), (float)tab.x, (float)tab.y, (float)(tab.x + tab.w), (float)tab.y + (float)tab.h - 0.5f, hovered ? hov : base);
        }

        boolean hoveredActive = mouseX >= activeTab.x && mouseX <= activeTab.x + activeTab.w && mouseY >= activeTab.y && mouseY <= activeTab.y + activeTab.h;
        int activeAlpha = hoveredActive ? gui.hoverAlpha.getValueInt() : gui.alpha.getValueInt();
        if (gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            Render2DUtil.drawLutRect(context.getMatrices(), this.topTabAnimX, (float)activeTab.y, this.topTabAnimW, (float)activeTab.h - 0.5f, gui.getSpectrumLutId(), gui.getSpectrumLutHeight(), activeAlpha);
        } else {
            Color c = gui.getActiveColor((double)activeTab.y * 0.25);
            Render2DUtil.rect(context.getMatrices(), this.topTabAnimX, (float)activeTab.y, this.topTabAnimX + this.topTabAnimW, (float)activeTab.y + (float)activeTab.h - 0.5f, ColorUtil.injectAlpha(c, activeAlpha).getRGB());
        }

        for (TopTab tab : this.topTabs) {
            boolean hovered = mouseX >= tab.x && mouseX <= tab.x + tab.w && mouseY >= tab.y && mouseY <= tab.y + tab.h;
            boolean active = this.page == tab.page;
            int textColor = active || hovered ? gui.enableTextColor.getValue().getRGB() : gui.defaultTextColor.getValue().getRGB();
            float textY = this.getCenteredTextY((float)tab.y, (float)tab.h - 0.5f);
            TextUtil.drawString(context, tab.getLabel(chinese), (double)(tab.x + padX), (double)textY, textColor, customFont, shadow);
        }
    }

    private boolean handleTopTabClick(int mouseX, int mouseY) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return false;
        }
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        this.updateTopTabsLayout(Wrapper.mc.getWindow().getScaledWidth(), chinese);
        for (TopTab tab : this.topTabs) {
            if (mouseX >= tab.x && mouseX <= tab.x + tab.w && mouseY >= tab.y && mouseY <= tab.y + tab.h) {
                this.setPage(tab.page);
                return true;
            }
        }
        return false;
    }

    private void renderSnow(DrawContext context) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null || !gui.snow.getValue()) {
            if (!this.snowflakes.isEmpty()) {
                this.snowflakes.clear();
            }
            return;
        }
        float fade = (float)gui.alphaValue;
        if (fade <= 0.01f) {
            return;
        }
        int w = context.getScaledWindowWidth();
        int h = context.getScaledWindowHeight();
        int target = Math.max(0, gui.snowAmount.getValueInt());
        while (this.snowflakes.size() < target) {
            this.snowflakes.add(this.spawnSnowflake(w, h, true));
        }
        while (this.snowflakes.size() > target) {
            this.snowflakes.remove(this.snowflakes.size() - 1);
        }
        float dt = AnimateUtil.deltaTime();
        if (dt <= 0.0f) {
            dt = 0.016f;
        }
        float baseSpeed = gui.snowSpeed.getValueFloat();
        float baseSize = gui.snowSize.getValueFloat();
        float wind = gui.snowWind.getValueFloat();
        int a = (int)Math.round((double)gui.snowAlpha.getValueInt() * (double)fade);
        a = Math.max(0, Math.min(255, a));
        Color c = new Color(255, 255, 255, a);
        for (int i = 0; i < this.snowflakes.size(); ++i) {
            Snowflake f = this.snowflakes.get(i);
            f.y += (baseSpeed * f.speedMul) * dt;
            f.x += wind * dt + (float)Math.sin((double)(f.y * 0.02f + f.phase)) * f.drift * dt;
            float size = Math.max(0.3f, baseSize * f.sizeMul);
            if (f.y > (float)h + size + 2.0f || f.x < -10.0f || f.x > (float)w + 10.0f) {
                this.snowflakes.set(i, this.spawnSnowflake(w, h, false));
                continue;
            }
            if (gui.snowShape.getValue() == ClickGui.SnowShape.Circle) {
                Render2DUtil.drawCircle(context.getMatrices(), f.x, f.y, size, c, 16);
            } else {
                this.drawSnowflake(context.getMatrices(), f.x, f.y, size, c.getRGB(), f.phase + f.y * 0.01f);
            }
        }
    }

    private void drawSnowflake(MatrixStack matrices, float x, float y, float r, int color, float rotation) {
        float branchLen = r * 0.45f;
        float branchOffset = 0.65f;
        for (int i = 0; i < 3; ++i) {
            double a = (double)rotation + (double)i * 1.0471975511965976;
            float dx = (float)Math.cos(a) * r;
            float dy = (float)Math.sin(a) * r;
            Render2DUtil.drawLine(matrices, x - dx, y - dy, x + dx, y + dy, color);

            float fx1 = x + dx * branchOffset;
            float fy1 = y + dy * branchOffset;
            Render2DUtil.drawLine(matrices, fx1, fy1, fx1 + (float)Math.cos(a + 0.5235987755982988) * branchLen, fy1 + (float)Math.sin(a + 0.5235987755982988) * branchLen, color);
            Render2DUtil.drawLine(matrices, fx1, fy1, fx1 + (float)Math.cos(a - 0.5235987755982988) * branchLen, fy1 + (float)Math.sin(a - 0.5235987755982988) * branchLen, color);

            double a2 = a + Math.PI;
            float fx2 = x - dx * branchOffset;
            float fy2 = y - dy * branchOffset;
            Render2DUtil.drawLine(matrices, fx2, fy2, fx2 + (float)Math.cos(a2 + 0.5235987755982988) * branchLen, fy2 + (float)Math.sin(a2 + 0.5235987755982988) * branchLen, color);
            Render2DUtil.drawLine(matrices, fx2, fy2, fx2 + (float)Math.cos(a2 - 0.5235987755982988) * branchLen, fy2 + (float)Math.sin(a2 - 0.5235987755982988) * branchLen, color);
        }
    }

    private Snowflake spawnSnowflake(int w, int h, boolean randomY) {
        float x = (float)this.snowRandom.nextInt(Math.max(1, w));
        float y = randomY ? (float)this.snowRandom.nextInt(Math.max(1, h)) : -5.0f - this.snowRandom.nextFloat() * 20.0f;
        float phase = this.snowRandom.nextFloat() * 6.2831855f;
        float drift = 10.0f + this.snowRandom.nextFloat() * 40.0f;
        float speedMul = 0.55f + this.snowRandom.nextFloat() * 1.05f;
        float sizeMul = 0.6f + this.snowRandom.nextFloat() * 1.2f;
        return new Snowflake(x, y, phase, drift, speedMul, sizeMul);
    }

    public static enum Page {
        Module,
        Config,
        Hud,
        AiAssistant;

    }

    private static final class TopTab {
        private final Page page;
        private final String labelEn;
        private final String labelZh;
        private int x;
        private int y;
        private int w;
        private int h;

        private TopTab(Page page, String labelEn, String labelZh) {
            this.page = page;
            this.labelEn = labelEn;
            this.labelZh = labelZh;
        }

        private String getLabel(boolean chinese) {
            if (chinese) {
                return this.labelZh == null ? this.labelEn : this.labelZh;
            }
            return this.labelEn;
        }
    }

    private static final class Snowflake {
        private float x;
        private float y;
        private final float phase;
        private final float drift;
        private final float speedMul;
        private final float sizeMul;

        private Snowflake(float x, float y, float phase, float drift, float speedMul, float sizeMul) {
            this.x = x;
            this.y = y;
            this.phase = phase;
            this.drift = drift;
            this.speedMul = speedMul;
            this.sizeMul = sizeMul;
        }
    }
}

