/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.sound.PositionedSoundInstance
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.sound.SoundEvents
 */
package dev.gzsakura_miitong.mod.gui.items;

import dev.gzsakura_miitong.core.impl.FontManager;
import dev.gzsakura_miitong.mod.Mod;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

public class Item
extends Mod {
    public static DrawContext context;
    protected float x;
    protected float y;
    protected int width;
    protected int height;
    private boolean hidden;

    public Item(String name) {
        super(name);
    }

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static void sound() {
        if (ClickGui.getInstance().sound.getValue()) {
            mc.getSoundManager().play((SoundInstance)PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, (float)ClickGui.getInstance().soundPitch.getValueFloat()));
        }
    }

    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
    }

    public void update() {
    }

    public void onKeyTyped(char typedChar, int keyCode) {
    }

    public void onKeyPressed(int key) {
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    protected double getColorDelay() {
        return (double)this.y / 10.0;
    }

    protected void drawString(String text, double x, double y, Color color) {
        this.drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        if (ClickGui.getInstance().font.getValue()) {
            FontManager.ui.drawString(context.getMatrices(), text, (double)((int)x), (double)((int)y), color, ClickGui.getInstance().shadow.getValue());
        } else {
            context.drawText(Item.mc.textRenderer, text, (int)x, (int)y, color, ClickGui.getInstance().shadow.getValue());
        }
    }

    protected int getFontHeight() {
        if (ClickGui.getInstance().font.getValue()) {
            return (int)FontManager.ui.getFontHeight();
        }
        Objects.requireNonNull(Item.mc.textRenderer);
        return 9;
    }

    protected float getCenteredTextY(float baseY, float boxHeight) {
        return baseY + (boxHeight - (float)this.getFontHeight()) / 2.0f + (float)ClickGui.getInstance().textOffset.getValueInt();
    }

    protected int getWidth(String s) {
        if (ClickGui.getInstance().font.getValue()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return Item.mc.textRenderer.getWidth(s);
    }
}

