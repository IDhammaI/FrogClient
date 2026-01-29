/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.text.Text
 */
package dev.idhammai.mod.gui.windows;

import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.mod.gui.ClickGuiScreen;
import dev.idhammai.mod.modules.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class WindowsScreen
extends Screen {
    public static WindowBase lastClickedWindow;
    public static WindowBase draggingWindow;
    private List<WindowBase> windows = new ArrayList<WindowBase>();

    public WindowsScreen(WindowBase ... windows) {
        super(Text.of((String)"CustomWindows"));
        this.windows.clear();
        lastClickedWindow = null;
        this.windows = Arrays.stream(windows).toList();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (Module.nullCheck()) {
            this.renderBackground(context, mouseX, mouseY, delta);
        }
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> {
            if (w != lastClickedWindow) {
                w.render(context, mouseX, mouseY);
            }
        });
        if (lastClickedWindow != null && lastClickedWindow.isVisible()) {
            lastClickedWindow.render(context, mouseX, mouseY);
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.windows.forEach(w -> w.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseClicked(mouseX, mouseY, button));
        int i = Wrapper.mc.getWindow().getScaledWidth() / 2;
        float offset = (float)this.windows.size() * 20.0f / -2.0f - 23.0f;
        if (Render2DUtil.isHovered(mouseX, mouseY, (float)i + offset + 1.0f, Wrapper.mc.getWindow().getScaledHeight() - 23, 15.0, 15.0)) {
            Wrapper.mc.setScreen((Screen)ClickGuiScreen.getInstance());
        }
        offset += 23.0f;
        for (WindowBase w2 : this.windows) {
            if (Render2DUtil.isHovered(mouseX, mouseY, (float)i + offset, Wrapper.mc.getWindow().getScaledHeight() - 24, 17.0, 17.0)) {
                w2.setVisible(!w2.isVisible());
            }
            offset += 20.0f;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char key, int keyCode) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.charTyped(key, keyCode));
        return super.charTyped(key, keyCode);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseScrolled((int)(verticalAmount * 5.0)));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}

