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
package dev.gzsakura_miitong.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.api.utils.render.TextUtil;
import dev.gzsakura_miitong.mod.Mod;
import dev.gzsakura_miitong.mod.gui.items.Component;
import dev.gzsakura_miitong.mod.gui.items.Item;
import dev.gzsakura_miitong.mod.gui.items.buttons.ModuleButton;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class ClickGuiScreen
extends Screen {
    private static ClickGuiScreen INSTANCE = new ClickGuiScreen();
    private final ArrayList<Component> components = new ArrayList();

    public ClickGuiScreen() {
        super((Text)Text.literal((String)"Vitality"));
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
                    for (Module module : Vitality.MODULE.getModules()) {
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
        Vitality.BLUR.applyBlur(30.0f, panelX, panelY, panelW, panelH);
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
                this.components.forEach(component -> component.setX(component.getX() - 15));
            } else if (verticalAmount > 0.0) {
                this.components.forEach(component -> component.setX(component.getX() + 15));
            }
        } else if (verticalAmount < 0.0) {
            this.components.forEach(component -> component.setY(component.getY() - 15));
        } else if (verticalAmount > 0.0) {
            this.components.forEach(component -> component.setY(component.getY() + 15));
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

