/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package dev.gzsakura_miitong.mod.gui.items.buttons;

import dev.gzsakura_miitong.api.utils.math.Animation;
import dev.gzsakura_miitong.api.utils.math.Easing;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.core.impl.FontManager;
import dev.gzsakura_miitong.mod.gui.ClickGuiScreen;
import dev.gzsakura_miitong.mod.gui.items.Item;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.settings.Setting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BindSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.RotationAxis;

public class ModuleButton
extends Button {
    private final Module module;
    private List<Item> items = new ArrayList<Item>();
    public boolean subOpen;
    public double itemHeight;
    public final Animation animation = new Animation();
    private final Animation hoverAnimation = new Animation();

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.initSettings();
    }

    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<Item>();
        for (Setting setting : this.module.getSettings()) {
            Setting s;
            if (setting instanceof BooleanSetting) {
                s = (BooleanSetting)setting;
                newItems.add(new BooleanButton((BooleanSetting)s));
            }
            if (setting instanceof BindSetting) {
                s = (BindSetting)setting;
                newItems.add(new BindButton((BindSetting)s));
            }
            if (setting instanceof StringSetting) {
                s = (StringSetting)setting;
                newItems.add(new StringButton((StringSetting)s));
            }
            if (setting instanceof SliderSetting) {
                s = (SliderSetting)setting;
                newItems.add(new SliderButton((SliderSetting)s));
            }
            if (setting instanceof EnumSetting) {
                s = (EnumSetting)setting;
                newItems.add(new EnumButton((EnumSetting<?>)s));
            }
            if (!(setting instanceof ColorSetting)) continue;
            s = (ColorSetting)setting;
            newItems.add(new PickerButton((ColorSetting)s));
        }
        this.items = newItems;
    }

    @Override
    public void update() {
        for (Item item : this.items) {
            item.update();
        }
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        boolean pressed = this.getState();
        double hoverProgress = this.hoverAnimation.get(hovered ? 1.0 : 0.0, 100L, Easing.CubicInOut);
        Color accent = ClickGui.getInstance().activeColor.getValue();
        Color defaultColor = ClickGui.getInstance().defaultColor.getValue();
        Color hoverColor = ClickGui.getInstance().hoverColor.getValue();
        Color idleFill = new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), defaultColor.getAlpha());
        Color hoverFill = new Color(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(), ClickGui.getInstance().hoverAlpha.getValueInt());
        Color animatedFill = ColorUtil.fadeColor(idleFill, hoverFill, hoverProgress);
        Color baseFill = pressed ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Math.min(230, ClickGui.getInstance().hoverAlpha.getValueInt())) : animatedFill;
        Color outline = pressed ? new Color(255, 255, 255, 200) : ColorUtil.fadeColor(new Color(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(), 60), new Color(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(), 180), hoverProgress);
        Render2DUtil.drawRect(context.getMatrices(), this.x, this.y, this.width, (float)this.height - 0.5f, baseFill);
        Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x, this.y, this.width, (float)this.height - 0.5f, new Color(0, 0, 0, 0), outline);
        if (hoverProgress > 0.01) {
            Color accentOutline = ColorUtil.fadeColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160), hoverProgress);
            Render2DUtil.drawRectWithOutline(context.getMatrices(), this.x - 0.5f, this.y - 0.5f, (float)this.width + 1.0f, (float)this.height + 1.0f, new Color(0, 0, 0, 0), accentOutline);
        }
        this.drawString(this.module.getDisplayName(), (double)(this.x + 2.3f), (double)(this.y - 2.0f - (float)ClickGuiScreen.getInstance().getTextOffset()), this.getState() ? enableTextColor : defaultTextColor);
        if (ClickGui.getInstance().gear.booleanValue) {
            boolean expanded = this.subOpen || this.itemHeight > 0.0;
            switch (ClickGui.getInstance().expandIcon.getValue()) {
                case PlusMinus -> this.drawString(this.subOpen ? "-" : "+", (double)(this.x + (float)this.width - 8.0f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), ClickGui.getInstance().gear.getValue().getRGB());
                case Chevron -> this.drawString(this.subOpen ? "v" : ">", (double)(this.x + (float)this.width - 8.0f), (double)(this.y - 1.7f - (float)ClickGuiScreen.getInstance().getTextOffset()), ClickGui.getInstance().gear.getValue().getRGB());
                case Gear -> {
                    float gearX = this.x + (float)this.width - 10.0f;
                    float gearY = this.y - 2.0f - (float)ClickGuiScreen.getInstance().getTextOffset();
                    int gearColor = ClickGui.getInstance().gear.getValue().getRGB();
                    float gearW = (float)FontManager.icon.getWidth("d");
                    float gearH = (float)FontManager.icon.getFontHeight();
                    int totalItemHeight = this.getItemHeight();
                    float expandProgress = totalItemHeight <= 0 ? 0.0f : (float)Math.min(1.0, Math.max(0.0, this.itemHeight / (double)totalItemHeight));
                    if (expandProgress > 0.001f) {
                        float centerX = gearX + gearW / 2.0f;
                        float centerY = gearY + gearH / 2.0f - 2.5f;
                        float angle = (float)(System.currentTimeMillis() % 2000L) / 2000.0f * 360.0f * expandProgress;
                        context.getMatrices().push();
                        context.getMatrices().translate(centerX, centerY, 0.0f);
                        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
                        FontManager.icon.drawString(context.getMatrices(), "d", (double)(-gearW / 2.0f), (double)(-gearH / 2.0f + 2.5f), gearColor);
                        context.getMatrices().pop();
                    } else {
                        FontManager.icon.drawString(context.getMatrices(), "d", (double)gearX, (double)gearY, gearColor);
                    }
                }
            }
        }
        if (this.subOpen || this.itemHeight > 0.0) {
            if (ClickGui.getInstance().line.getValue()) {
                double itemHeight = this.getItemHeight();
                int line = new Color(220, 224, 230, 160).getRGB();
                Render2DUtil.drawLine(context.getMatrices(), this.x + 0.6f, (float)((double)(this.y + (float)this.height) + itemHeight - 0.5), this.x + 0.6f, this.y + (float)this.height - 0.5f, line);
                Render2DUtil.drawLine(context.getMatrices(), this.x + (float)this.width - 0.6f, (float)((double)(this.y + (float)this.height) + itemHeight - 0.5), this.x + (float)this.width - 0.6f, this.y + (float)this.height - 0.5f, line);
                Render2DUtil.drawLine(context.getMatrices(), this.x + 0.6f, (float)((double)(this.y + (float)this.height) + itemHeight - 0.5), this.x + (float)this.width - 0.6f, (float)((double)(this.y + (float)this.height) + itemHeight - (double)0.7f), line);
            }
            float height = this.height + 2;
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.setHeight(this.height);
                item.setLocation(this.x + 1.0f, this.y + height);
                item.setWidth(this.width - 9);
                item.drawScreen(context, mouseX, mouseY, partialTicks);
                height += (float)(item.getHeight() + 2);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                this.subOpen = !this.subOpen;
                ModuleButton.sound();
            }
            if (this.subOpen) {
                for (Item item : this.items) {
                    if (item.isHidden()) continue;
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onKeyPressed(int key) {
        super.onKeyPressed(key);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyPressed(key);
            }
        }
    }

    public int getButtonHeight() {
        return super.getHeight();
    }

    public int getItemHeight() {
        int height = 3;
        for (Item item : this.items) {
            if (item.isHidden()) continue;
            height += item.getHeight() + 2;
        }
        return height;
    }

    @Override
    public int getHeight() {
        if (this.subOpen) {
            int height = super.getHeight();
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                height += item.getHeight() + 1;
            }
            return height + 2;
        }
        return super.getHeight();
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }

    @Override
    public boolean getState() {
        return this.module.isOn();
    }
}

