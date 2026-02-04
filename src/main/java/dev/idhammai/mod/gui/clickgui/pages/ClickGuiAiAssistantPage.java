package dev.idhammai.mod.gui.clickgui.pages;

import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.gui.clickgui.ClickGuiFrame;
import dev.idhammai.mod.gui.clickgui.ClickGuiScreen;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import net.minecraft.client.gui.DrawContext;

public final class ClickGuiAiAssistantPage {
    private final ClickGuiScreen host;

    public ClickGuiAiAssistantPage(ClickGuiScreen host) {
        this.host = host;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, ClickGuiFrame frame) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null) {
            return;
        }
        float baseX = frame.baseX(ClickGuiScreen.Page.AiAssistant);
        float screenUnitW = frame.scale == 0.0f ? (float)frame.screenW : (float)frame.screenW / frame.scale;
        float panelXf = Math.max(8.0f, (screenUnitW - (float)frame.panelW) / 2.0f);
        float x = baseX + panelXf + 10.0f;
        float y = (float)frame.panelY + 10.0f;
        float w = (float)(frame.panelW - 20);
        float h = (float)(frame.panelH - 20);

        boolean customFont = FontManager.isCustomFontEnabled();
        boolean shadow = FontManager.isShadowEnabled();
        boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();

        Render2DUtil.rect(context.getMatrices(), x, y, x + w, y + h, gui.defaultColor.getValue().getRGB());

        String title = chinese ? "AI 助手" : "AI Assistant";
        float titleY = y + 6.0f;
        TextUtil.drawString(context, title, (double)(x + 6.0f), (double)titleY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);

        String line1 = chinese ? "该功能暂未接入。" : "This feature isn't connected yet.";
        String line2 = chinese ? "后续可在这里集成聊天/指令建议等。" : "Later, chat and command suggestions can live here.";
        float textY1 = titleY + (float)this.host.getFontHeight() + 10.0f;
        float textY2 = textY1 + (float)this.host.getFontHeight() + 4.0f;
        int textColor = gui.defaultTextColor.getValue().getRGB();
        TextUtil.drawString(context, line1, (double)(x + 6.0f), (double)textY1, textColor, customFont, shadow);
        TextUtil.drawString(context, line2, (double)(x + 6.0f), (double)textY2, textColor, customFont, shadow);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, ClickGuiFrame frame) {
        return false;
    }
}
