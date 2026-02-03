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
package dev.idhammai.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.Frog;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.AnimateUtil;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.Manager;
import dev.idhammai.core.impl.ConfigManager;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.mod.Mod;
import dev.idhammai.mod.gui.items.buttons.StringButton;
import dev.idhammai.mod.modules.impl.client.HUD;
import dev.idhammai.mod.modules.settings.Setting;
import dev.idhammai.mod.modules.settings.impl.BindSetting;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.gui.items.Item;
import dev.idhammai.mod.gui.items.buttons.ModuleButton;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import dev.idhammai.mod.modules.impl.client.Fonts;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.util.StringHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
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
    private float topTabAnimX;
    private float topTabAnimW;
    private boolean topTabAnimInit;
    private final ArrayList<String> configNames = new ArrayList();
    private float configScroll;
    private String selectedConfigName;
    private String appliedConfigName;
    private String configNameInput = "";
    private boolean configNameListening;
    private String lastSavedConfigName;
    private long lastSavedConfigTime;

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

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        this.topTabs.clear();
        this.topTabs.add(new TopTab(Page.Module, "Module"));
        this.topTabs.add(new TopTab(Page.Config, "Config"));
        this.topTabs.add(new TopTab(Page.Hud, "HUD"));
        this.topTabs.add(new TopTab(Page.AiAssistant, "AI Assistant"));
        int categoryWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().categoryWidth.getValueInt() : 101;
        int moduleButtonWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().moduleButtonWidth.getValueInt() : 93;
        int layoutWidth = Math.max(categoryWidth, moduleButtonWidth);
        int spacing = layoutWidth + 1;
        int count = Module.Category.values().length;
        int startX = 10;
        int startY = 4;
        if (Wrapper.mc != null && Wrapper.mc.getWindow() != null) {
            int screenWidth = Wrapper.mc.getWindow().getScaledWidth();
            int screenHeight = Wrapper.mc.getWindow().getScaledHeight();
            int totalWidth = count * layoutWidth + (count - 1);
            startX = Math.round(((float)screenWidth - (float)totalWidth) / 2.0f);
            startY = Math.round((float)screenHeight / 6.0f);
            this.layoutCorrected = true;
        }
        int offsetX = Math.round(((float)layoutWidth - (float)moduleButtonWidth) / 2.0f);
        int x = startX - spacing;
        for (final Module.Category category : Module.Category.values()) {
            x += spacing;
            this.components.add(new Component(category.toString(), category, x + offsetX, startY, true){

                @Override
                public void setupItems() {
                    for (Module module : Frog.MODULE.getModules()) {
                        if (!module.getCategory().equals((Object)category)) continue;
                        this.addButton(new ModuleButton(module));
                    }
                }
            });
        }
        this.components.forEach(components -> components.getItems().sort(Comparator.comparing(Mod::getName)));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float keyCodec = (float)ClickGui.getInstance().alphaValue;
        float scale = 0.92f + 0.08f * keyCodec;
        float slideY = (1.0f - keyCodec) * 20.0f;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)keyCodec);
        Item.context = context;
        this.renderBackground(context, mouseX, mouseY, delta);
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
        if (ClickGui.getInstance().blur.getValue()) {
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
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, slideY, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        this.components.forEach(components -> components.drawScreen(context, mouseX, mouseY, delta));
        this.renderConfigPage(context, mouseX, mouseY, delta, scale, slideY, pageOffsetX, pageW, panelX, panelY, panelW, panelH);
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
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (clickedButton == 0 && this.handleTopTabClick((int)mouseX, (int)mouseY)) {
            return true;
        }
        if (this.page == Page.Module) {
            this.components.forEach(components -> components.mouseClicked((int)mouseX, (int)mouseY, clickedButton));
            return super.mouseClicked(mouseX, mouseY, clickedButton);
        }
        if (this.page == Page.Config && this.handleConfigClick((int)mouseX, (int)mouseY, clickedButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int releaseButton) {
        if (this.page == Page.Module) {
            this.components.forEach(components -> components.mouseReleased((int)mouseX, (int)mouseY, releaseButton));
        }
        return super.mouseReleased(mouseX, mouseY, releaseButton);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.page == Page.Module) {
            if (InputUtil.isKeyPressed((long)Wrapper.mc.getWindow().getHandle(), (int)340)) {
                if (verticalAmount < 0.0) {
                    this.components.forEach(component -> component.setX(component.getTargetX() - 15));
                } else if (verticalAmount > 0.0) {
                    this.components.forEach(component -> component.setX(component.getTargetX() + 15));
                }
            } else if (verticalAmount < 0.0) {
                this.components.forEach(component -> component.setY(component.getTargetY() - 15));
            } else if (verticalAmount > 0.0) {
                this.components.forEach(component -> component.setY(component.getTargetY() + 15));
            }
        } else if (this.page == Page.Config) {
            this.refreshConfigList();
            int rowH = this.getFontHeight() + 6;
            float viewH = (float)(Wrapper.mc.getWindow().getScaledHeight() - 44);
            float totalH = (float)this.configNames.size() * (float)rowH;
            float max = Math.max(0.0f, totalH - viewH);
            float next = this.configScroll + (float)(-verticalAmount) * 18.0f;
            if (next < 0.0f) {
                next = 0.0f;
            }
            if (next > max) {
                next = max;
            }
            this.configScroll = next;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.page == Page.Module) {
            this.components.forEach(component -> component.onKeyPressed(keyCode));
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.page == Page.Config && this.configNameListening) {
            switch (keyCode) {
                case 256: {
                    this.configNameListening = false;
                    return true;
                }
                case 257:
                case 335: {
                    this.configNameListening = false;
                    return true;
                }
                case 86: {
                    if (Wrapper.mc != null && Wrapper.mc.getWindow() != null && InputUtil.isKeyPressed((long)Wrapper.mc.getWindow().getHandle(), (int)341)) {
                        this.configNameInput = this.configNameInput + SelectionManager.getClipboard(Wrapper.mc);
                        if (this.configNameInput.length() > 64) {
                            this.configNameInput = this.configNameInput.substring(0, 64);
                        }
                        return true;
                    }
                    break;
                }
                case 259: {
                    this.configNameInput = StringButton.removeLastChar(this.configNameInput);
                    return true;
                }
                case 32: {
                    this.configNameInput = this.configNameInput + " ";
                    if (this.configNameInput.length() > 64) {
                        this.configNameInput = this.configNameInput.substring(0, 64);
                    }
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.page == Page.Module) {
            this.components.forEach(component -> component.onKeyTyped(chr, modifiers));
            return super.charTyped(chr, modifiers);
        }
        if (this.page == Page.Config && this.configNameListening && StringHelper.isValidChar(chr)) {
            this.configNameInput = this.configNameInput + chr;
            if (this.configNameInput.length() > 64) {
                this.configNameInput = this.configNameInput.substring(0, 64);
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
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
        if (page == null) {
            return;
        }
        this.page = page;
        for (Component c : this.components) {
            c.drag = false;
        }
        if (page == Page.Config) {
            this.refreshConfigList();
            this.configScroll = 0.0f;
            this.configNameListening = false;
            if (this.selectedConfigName != null && !this.configNames.contains(this.selectedConfigName)) {
                this.selectedConfigName = null;
            }
            if (this.selectedConfigName == null && !this.configNames.isEmpty()) {
                this.selectedConfigName = this.configNames.get(0);
            }
        }
    }

    private void refreshConfigList() {
        this.configNames.clear();
        this.configNames.addAll(ConfigManager.listCfgNames());
    }

    private String sanitizeConfigName(String name) {
        return ConfigManager.sanitizeCfgName(name);
    }

    private String createDefaultConfig(String nameInput) {
        String name = ConfigManager.createDefaultCfg(nameInput);
        this.refreshConfigList();
        return name;
    }

    private String backupConfig(String fromName, String toNameInput) {
        String name = ConfigManager.backupCfg(fromName, toNameInput);
        this.refreshConfigList();
        return name;
    }

    private void deleteConfig(String name) {
        ConfigManager.deleteCfg(name);
        this.refreshConfigList();
    }

    private String saveConfig(String name) {
        String n = ConfigManager.saveCfg(name);
        this.refreshConfigList();
        return n;
    }

    private void loadConfig(String name) {
        if (ConfigManager.loadCfg(name)) {
            this.appliedConfigName = name;
        }
    }

    private void renderConfigPage(DrawContext context, int mouseX, int mouseY, float delta, float scale, float slideY, float pageOffsetX, int pageW, int panelX, int panelY, int panelW, int panelH) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return;
        }
        this.refreshConfigList();
        if (this.selectedConfigName != null && !this.configNames.contains(this.selectedConfigName)) {
            this.selectedConfigName = null;
        }
        if (this.selectedConfigName == null && !this.configNames.isEmpty()) {
            this.selectedConfigName = this.configNames.get(0);
        }
        float mx = scale == 0.0f ? (float)mouseX : (float)mouseX / scale;
        float my = scale == 0.0f ? (float)mouseY : ((float)mouseY - slideY) / scale;
        float pageUnitW = scale == 0.0f ? (float)pageW : (float)pageW / scale;
        float baseX = pageOffsetX + (float)Page.Config.ordinal() * pageUnitW;
        float x = baseX + (float)panelX + 10.0f;
        float w = (float)(panelW - 20);
        float titleY = (float)panelY + 10.0f;
        float listY = (float)panelY + 28.0f;
        float gap = 8.0f;
        float listW = w * 0.62f;
        float rightX = x + listW + gap;
        float rightW = w - listW - gap;
        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
        String title = chinese ? "配置列表" : "Configs";
        String hint = chinese ? "选择配置后点击 Apply 才会应用" : "Select a config, then click Apply";
        String none = chinese ? "无" : "None";
        TextUtil.drawString(context, title, (double)(x + 2.0f), (double)titleY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        int rowH = this.getFontHeight() + 6;
        float clipTop = listY - (float)rowH;
        float clipBottom = (float)context.getScaledWindowHeight() - 20.0f;
        for (int i = 0; i < this.configNames.size(); ++i) {
            String name = this.configNames.get(i);
            float ry = listY + (float)i * (float)rowH - this.configScroll;
            float rh = (float)rowH - 0.5f;
            if (ry + rh < clipTop || ry > clipBottom) continue;
            boolean hovered = mx >= x && mx <= x + listW && my >= ry && my <= ry + rh;
            boolean selected = this.selectedConfigName != null && name.equalsIgnoreCase(this.selectedConfigName);
            int bg;
            if (selected) {
                Color ac = gui.getActiveColor((double)ry * 0.25);
                bg = ColorUtil.injectAlpha(ac, gui.alpha.getValueInt()).getRGB();
            } else {
                bg = hovered ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB();
            }
            Render2DUtil.rect(context.getMatrices(), x, ry, x + listW, ry + rh, bg);
            int tc = hovered || selected ? gui.enableTextColor.getValue().getRGB() : gui.defaultTextColor.getValue().getRGB();
            float textY = this.getCenteredTextY(ry, rh);
            TextUtil.drawString(context, name, (double)(x + 6.0f), (double)textY, tc, customFont, shadow);
        }
        String selLabel = chinese ? "当前选择: " : "Selected: ";
        String appLabel = chinese ? "已应用: " : "Applied: ";
        String selectedName = this.selectedConfigName == null ? none : this.selectedConfigName;
        String appliedName = this.appliedConfigName == null ? none : this.appliedConfigName;
        TextUtil.drawString(context, selLabel + selectedName, (double)rightX, (double)titleY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float infoY2 = (float)titleY + (float)this.getFontHeight() + 4.0f;
        TextUtil.drawString(context, appLabel + appliedName, (double)rightX, (double)infoY2, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float nameLabelY = infoY2 + (float)this.getFontHeight() + 8.0f;
        String nameLabel = chinese ? "名称" : "Name";
        TextUtil.drawString(context, nameLabel, (double)rightX, (double)nameLabelY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float boxY = nameLabelY + (float)this.getFontHeight() + 4.0f;
        float boxH = (float)rowH - 0.5f;
        boolean hoverBox = mx >= rightX && mx <= rightX + rightW && my >= boxY && my <= boxY + boxH;
        int boxBg = hoverBox || this.configNameListening ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, boxY, rightX + rightW, boxY + boxH, boxBg);
        String placeholder = chinese ? "输入配置名" : "Type config name";
        String show = this.configNameInput == null || this.configNameInput.isEmpty() ? placeholder : this.configNameInput;
        if (this.configNameListening) {
            show = show + StringButton.getIdleSign();
        }
        float boxTextY = this.getCenteredTextY(boxY, boxH);
        TextUtil.drawString(context, show, (double)(rightX + 6.0f), (double)boxTextY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY = boxY + boxH + 8.0f;
        float btnH = (float)rowH - 0.5f;
        String bCreate = chinese ? "创建默认" : "Create Default";
        String bBackup = chinese ? "备份" : "Backup";
        String bDelete = chinese ? "删除" : "Delete";
        String bSave = chinese ? "保存" : "Save";
        String bApply = chinese ? "Apply 应用" : "Apply";
        boolean canCreate = !this.sanitizeConfigName(this.configNameInput).isEmpty();
        boolean canSelect = this.selectedConfigName != null && !this.selectedConfigName.isEmpty();
        boolean canSave = canSelect || canCreate;
        boolean canApply = canSelect;
        boolean canBackup = canSelect;
        boolean canDelete = canSelect;
        boolean hCreate = mx >= rightX && mx <= rightX + rightW && my >= btnY && my <= btnY + btnH;
        int bgCreate = canCreate ? (hCreate ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY, rightX + rightW, btnY + btnH, bgCreate);
        TextUtil.drawString(context, bCreate, (double)(rightX + 6.0f), (double)this.getCenteredTextY(btnY, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY2 = btnY + btnH + 4.0f;
        boolean hBackup = mx >= rightX && mx <= rightX + rightW && my >= btnY2 && my <= btnY2 + btnH;
        int bgBackup = canBackup ? (hBackup ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY2, rightX + rightW, btnY2 + btnH, bgBackup);
        TextUtil.drawString(context, bBackup, (double)(rightX + 6.0f), (double)this.getCenteredTextY(btnY2, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY3 = btnY2 + btnH + 4.0f;
        boolean hDelete = mx >= rightX && mx <= rightX + rightW && my >= btnY3 && my <= btnY3 + btnH;
        int bgDelete = canDelete ? (hDelete ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY3, rightX + rightW, btnY3 + btnH, bgDelete);
        TextUtil.drawString(context, bDelete, (double)(rightX + 6.0f), (double)this.getCenteredTextY(btnY3, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY4 = btnY3 + btnH + 4.0f;
        boolean hSave = mx >= rightX && mx <= rightX + rightW && my >= btnY4 && my <= btnY4 + btnH;
        int bgSave = canSave ? (hSave ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY4, rightX + rightW, btnY4 + btnH, bgSave);
        TextUtil.drawString(context, bSave, (double)(rightX + 6.0f), (double)this.getCenteredTextY(btnY4, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY5 = btnY4 + btnH + 4.0f;
        boolean hApply = mx >= rightX && mx <= rightX + rightW && my >= btnY5 && my <= btnY5 + btnH;
        int bgApply = canApply ? (hApply ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY5, rightX + rightW, btnY5 + btnH, bgApply);
        TextUtil.drawString(context, bApply, (double)(rightX + 6.0f), (double)this.getCenteredTextY(btnY5, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float hintY = btnY5 + btnH + 8.0f;
        TextUtil.drawString(context, hint, (double)(rightX + 2.0f), (double)hintY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        if (this.lastSavedConfigName != null && !this.lastSavedConfigName.isEmpty()) {
            long now = System.currentTimeMillis();
            if (now - this.lastSavedConfigTime <= 2500L) {
                String savedMsg = chinese ? "已保存: cfg/" + this.lastSavedConfigName + ".cfg" : "Saved: cfg/" + this.lastSavedConfigName + ".cfg";
                float savedY = hintY + (float)this.getFontHeight() + 4.0f;
                TextUtil.drawString(context, savedMsg, (double)(rightX + 2.0f), (double)savedY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
            }
        }
    }

    private boolean handleConfigClick(int mouseX, int mouseY, int mouseButton) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return false;
        }
        if (mouseButton != 0) {
            this.configNameListening = false;
            return false;
        }
        float keyCodec = (float)ClickGui.getInstance().alphaValue;
        float scale = 0.92f + 0.08f * keyCodec;
        float slideY = (1.0f - keyCodec) * 20.0f;
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
        int panelW = Math.min(Wrapper.mc.getWindow().getScaledWidth() - panelX - 8, maxX - minX + margin * 2);
        int panelH = Math.min(Wrapper.mc.getWindow().getScaledHeight() - panelY - 6, maxY - minY + margin * 2 + 24);
        int screenW = Wrapper.mc.getWindow().getScaledWidth();
        int categoryWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().categoryWidth.getValueInt() : 101;
        int moduleButtonWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().moduleButtonWidth.getValueInt() : 93;
        int layoutWidth = Math.max(categoryWidth, moduleButtonWidth);
        int count = this.components.size();
        int totalWidth = count > 0 ? count * layoutWidth + (count - 1) : screenW;
        int pageW = Math.max(screenW, totalWidth + 32);
        float pageX = (float)this.pageSlide.get(-((double)this.page.ordinal() * (double)pageW), 260L, Easing.SineOut);
        float pageOffsetX = scale == 0.0f ? pageX : pageX / scale;
        float mx = scale == 0.0f ? (float)mouseX : (float)mouseX / scale;
        float my = scale == 0.0f ? (float)mouseY : ((float)mouseY - slideY) / scale;
        float pageUnitW = scale == 0.0f ? (float)pageW : (float)pageW / scale;
        float baseX = pageOffsetX + (float)Page.Config.ordinal() * pageUnitW;
        float x = baseX + (float)panelX + 10.0f;
        float w = (float)(panelW - 20);
        float listY = (float)panelY + 28.0f;
        float gap = 8.0f;
        float listW = w * 0.62f;
        float rightX = x + listW + gap;
        float rightW = w - listW - gap;
        int rowH = this.getFontHeight() + 6;
        this.refreshConfigList();
        for (int i = 0; i < this.configNames.size(); ++i) {
            String name = this.configNames.get(i);
            float ry = listY + (float)i * (float)rowH - this.configScroll;
            float rh = (float)rowH - 0.5f;
            if (mx >= x && mx <= x + listW && my >= ry && my <= ry + rh) {
                this.selectedConfigName = name;
                this.configNameListening = false;
                return true;
            }
        }
        float nameLabelY = (float)panelY + 10.0f + (float)this.getFontHeight() + 4.0f + (float)this.getFontHeight() + 8.0f;
        float boxY = nameLabelY + (float)this.getFontHeight() + 4.0f;
        float boxH = (float)rowH - 0.5f;
        if (mx >= rightX && mx <= rightX + rightW && my >= boxY && my <= boxY + boxH) {
            this.configNameListening = true;
            return true;
        }
        this.configNameListening = false;
        float btnY = boxY + boxH + 8.0f;
        float btnH = (float)rowH - 0.5f;
        float btnY2 = btnY + btnH + 4.0f;
        float btnY3 = btnY2 + btnH + 4.0f;
        float btnY4 = btnY3 + btnH + 4.0f;
        float btnY5 = btnY4 + btnH + 4.0f;
        boolean canCreate = !this.sanitizeConfigName(this.configNameInput).isEmpty();
        boolean canSelect = this.selectedConfigName != null && !this.selectedConfigName.isEmpty();
        boolean canSave = canSelect || canCreate;
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY && my <= btnY + btnH && canCreate) {
            String created = this.createDefaultConfig(this.configNameInput);
            if (created != null) {
                this.selectedConfigName = created;
            }
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY2 && my <= btnY2 + btnH && canSelect) {
            String backup = this.backupConfig(this.selectedConfigName, this.configNameInput);
            if (backup != null) {
                this.selectedConfigName = backup;
            }
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY3 && my <= btnY3 + btnH && canSelect) {
            String deleting = this.selectedConfigName;
            this.deleteConfig(deleting);
            this.selectedConfigName = this.configNames.isEmpty() ? null : this.configNames.get(0);
            if (deleting != null && deleting.equalsIgnoreCase(this.appliedConfigName)) {
                this.appliedConfigName = null;
            }
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY4 && my <= btnY4 + btnH && canSave) {
            String inputName = this.sanitizeConfigName(this.configNameInput);
            String toSave = inputName.isEmpty() ? this.selectedConfigName : inputName;
            String saved = this.saveConfig(toSave);
            if (saved != null) {
                this.selectedConfigName = saved;
                this.lastSavedConfigName = saved;
                this.lastSavedConfigTime = System.currentTimeMillis();
            }
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY5 && my <= btnY5 + btnH && canSelect) {
            this.loadConfig(this.selectedConfigName);
            return true;
        }
        return false;
    }

    private int getFontHeight() {
        if (FontManager.isCustomFontEnabled()) {
            return (int)FontManager.ui.getFontHeight();
        }
        return 9;
    }

    private int getTextWidth(String s) {
        if (FontManager.isCustomFontEnabled()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return Wrapper.mc != null ? Wrapper.mc.textRenderer.getWidth(s) : 0;
    }

    private float getCenteredTextY(float baseY, float boxHeight) {
        return baseY + (boxHeight - (float)this.getFontHeight()) / 2.0f + (float)ClickGui.getInstance().textOffset.getValueInt();
    }

    private void updateTopTabsLayout(int screenWidth) {
        int gap = 0;
        int padX = 8;
        int y = 6;
        int h = this.getFontHeight() + 6;
        int total = 0;
        for (int i = 0; i < this.topTabs.size(); ++i) {
            TopTab tab = this.topTabs.get(i);
            int w = this.getTextWidth(tab.label) + padX * 2;
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
        this.updateTopTabsLayout(Wrapper.mc.getWindow().getScaledWidth());
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
            TextUtil.drawString(context, tab.label, (double)(tab.x + padX), (double)textY, textColor, customFont, shadow);
        }
    }

    private boolean handleTopTabClick(int mouseX, int mouseY) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return false;
        }
        this.updateTopTabsLayout(Wrapper.mc.getWindow().getScaledWidth());
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

    private static enum Page {
        Module,
        Config,
        Hud,
        AiAssistant;

    }

    private static final class TopTab {
        private final Page page;
        private final String label;
        private int x;
        private int y;
        private int w;
        private int h;

        private TopTab(Page page, String label) {
            this.page = page;
            this.label = label;
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

