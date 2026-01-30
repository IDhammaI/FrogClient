/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.util.Formatting
 */
package dev.idhammai.mod.gui.items.buttons;

import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.gui.ClickGuiScreen;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;

public class EnumButton
extends Button {
    public final EnumSetting<?> setting;
    private boolean open;

    public EnumButton(EnumSetting<?> setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        double baseDelay = this.getColorDelay();
        float w = (float)this.width + 7.0f;
        float h = (float)this.height - 0.5f;
        if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum && this.getState()) {
            int a = hovered ? ClickGui.getInstance().hoverAlpha.getValueInt() : ClickGui.getInstance().alpha.getValueInt();
            Render2DUtil.drawSegmentedRect(context.getMatrices(), this.x, this.y, w, h, 2.0f, yy -> ColorUtil.injectAlpha(ClickGui.getInstance().getColor((double)yy * 0.25), a).getRGB());
        } else {
            Color color = ClickGui.getInstance().getColor(baseDelay);
            Render2DUtil.rect(context.getMatrices(), this.x, this.y, this.x + w, this.y + (float)this.height - 0.5f, this.getState() ? (!hovered ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()) : (!hovered ? defaultColor : hoverColor));
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            this.drawString(this.setting.getName().equalsIgnoreCase("Page") ? "Reset Page" : "Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(this.setting.getName() + " " + String.valueOf(Formatting.GRAY) + (((Enum)this.setting.getValue()).name().equalsIgnoreCase("ABC") ? "ABC" : ((Enum)this.setting.getValue()).name()), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        }
        if (this.open) {
            int y = (int)this.y;
            for (Enum e : (Enum[])((Enum)this.setting.getValue()).getDeclaringClass().getEnumConstants()) {
                String s = e.name();
                float optionY = this.getCenteredTextY((float)y + (float)this.height + 1.0f, 11.0f);
                this.drawString(s, (double)((float)this.width / 2.0f - (float)this.getWidth(s) / 2.0f + 2.0f + this.x), (double)optionY, ((Enum)this.setting.getValue()).name().equals(s) ? enableTextColor : defaultTextColor);
                y += 11;
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)340)) {
            if (this.setting.getName().equalsIgnoreCase("Page")) {
                boolean resetPage = false;
                for (dev.idhammai.mod.gui.items.Component component : ClickGuiScreen.getInstance().getComponents()) {
                    for (ModuleButton moduleButton : component.getItems()) {
                        if (!moduleButton.getModule().getSettings().contains(this.setting)) continue;
                        moduleButton.resetPageSettingsToDefault(this.setting);
                        resetPage = true;
                        break;
                    }
                    if (!resetPage) continue;
                    break;
                }
                EnumButton.sound();
                return;
            }
            Enum defaultValue = (Enum)this.setting.getDefaultValue();
            this.setting.setEnumValue(defaultValue.name());
            EnumButton.sound();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            EnumButton.sound();
        } else if (mouseButton == 0 && this.open) {
            int y = (int)this.y;
            for (Enum o : (Enum[])((Enum)this.setting.getValue()).getDeclaringClass().getEnumConstants()) {
                if ((float)mouseX > this.x && (float)mouseX < this.x + (float)this.width && mouseY >= y + this.height + 1 && mouseY < y + this.height + 11 + 1) {
                    this.setting.setEnumValue(String.valueOf(o));
                    EnumButton.sound();
                }
                y += 11;
            }
        }
    }

    @Override
    public int getHeight() {
        return super.getHeight() + (this.open ? 11 * ((Enum[])((Enum)this.setting.getValue()).getDeclaringClass().getEnumConstants()).length : 0);
    }

    @Override
    public void toggle() {
        this.setting.increaseEnum();
    }

    @Override
    public boolean getState() {
        return true;
    }
}

