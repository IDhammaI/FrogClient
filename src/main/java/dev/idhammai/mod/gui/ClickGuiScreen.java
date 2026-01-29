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
import dev.idhammai.mod.Mod;
import dev.idhammai.mod.gui.items.Component;
import dev.idhammai.mod.gui.items.Item;
import dev.idhammai.mod.gui.items.buttons.ModuleButton;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
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
        int x = -84;
        for (final Module.Category category : Module.Category.values()) {
            this.components.add(new Component(category.toString(), category, x += 94, 4, true){

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
        float r = 4.0f;
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
        int tipColor = new Color(255, 255, 255, alpha).getRGB();
        boolean customFont = ClickGui.getInstance().font.getValue();
        boolean shadow = ClickGui.getInstance().shadow.getValue();
        int buttonSize = 12;
        int buttonX = 6;
        int buttonY = context.getScaledWindowHeight() - (buttonSize + 6);
        Color baseColor = ClickGui.getInstance().color.getValue();
        Color buttonColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), buttonX, buttonY, buttonSize, buttonSize, buttonColor, new Color(0, 0, 0, Math.min(180, alpha)));
        TextUtil.drawString(context, "?", buttonX + 4, buttonY + 4, tipColor, customFont, shadow);
        boolean hoverTip = mouseX >= buttonX && mouseX <= buttonX + buttonSize && mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        if (hoverTip) {
            int tipX = 6;
            int tipY = context.getScaledWindowHeight() - 54;
            TextUtil.drawString(context, "左键拖动面板 右键展开/折叠", tipX, tipY, tipColor, customFont, shadow);
            TextUtil.drawString(context, "滚轮上下移动面板 SHIFT+滚轮左右移动", tipX, tipY + 12, tipColor, customFont, shadow);
            TextUtil.drawString(context, "SHIFT+单击切换触发方式 按住/松开", tipX, tipY + 24, tipColor, customFont, shadow);
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
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
}

