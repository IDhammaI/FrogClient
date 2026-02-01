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
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.mod.Mod;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.gui.items.Item;
import dev.idhammai.mod.gui.items.buttons.ModuleButton;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
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
    private final Random snowRandom = new Random();
    private final ArrayList<Snowflake> snowflakes = new ArrayList();

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
        context.getMatrices().push();
        context.getMatrices().translate((float)panelX + (float)panelW / 2.0f, (float)panelY + (float)panelH / 2.0f + slideY, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-((float)panelX + (float)panelW / 2.0f), -((float)panelY + (float)panelH / 2.0f), 0.0f);
        // Render2DUtil.drawRoundedRect(context.getMatrices(), panelX, panelY, panelW, panelH, r, new Color(255, 255, 255, alpha));
        int strokeA = Math.max(0, Math.min(255, (int)Math.round((double)alpha * 0.22)));
        // Render2DUtil.drawRoundedStroke(context.getMatrices(), panelX, panelY, panelW, panelH, r, new Color(220, 224, 230, strokeA), 48);
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, slideY, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        this.components.forEach(components -> components.drawScreen(context, mouseX, mouseY, delta));
        context.getMatrices().pop();
        ClickGui gui = ClickGui.getInstance();
        if (gui != null && gui.tips.getValue()) {
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
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        this.components.forEach(components -> components.mouseClicked((int)mouseX, (int)mouseY, clickedButton));
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int releaseButton) {
        this.components.forEach(components -> components.mouseReleased((int)mouseX, (int)mouseY, releaseButton));
        return super.mouseReleased(mouseX, mouseY, releaseButton);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
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
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.components.forEach(component -> component.onKeyPressed(keyCode));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        this.components.forEach(component -> component.onKeyTyped(chr, modifiers));
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

