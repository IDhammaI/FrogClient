package dev.idhammai.mod.gui.clickgui.pages;

import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.AnimateUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.ConfigManager;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.gui.clickgui.ClickGuiFrame;
import dev.idhammai.mod.gui.clickgui.ClickGuiScreen;
import dev.idhammai.mod.gui.items.buttons.StringButton;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.impl.client.ClientSetting;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.util.StringHelper;

public final class ClickGuiConfigPage {
    private final ClickGuiScreen host;
    private final ArrayList<String> configNames = new ArrayList<>();
    private float configScroll;
    private String selectedConfigName;
    private String appliedConfigName;
    private String configNameInput = "";
    private boolean configNameListening;
    private String lastSavedConfigName;
    private long lastSavedConfigTime;
    private float configSelectAnimY;
    private boolean configSelectAnimInit;

    public ClickGuiConfigPage(ClickGuiScreen host) {
        this.host = host;
    }

    public void onOpen() {
        this.refreshConfigList();
        this.configScroll = 0.0f;
        this.configNameListening = false;
        this.configSelectAnimInit = false;
        if (this.selectedConfigName != null && !this.configNames.contains(this.selectedConfigName)) {
            this.selectedConfigName = null;
        }
        if (this.selectedConfigName == null && !this.configNames.isEmpty()) {
            this.selectedConfigName = this.configNames.get(0);
        }
    }

    public void stopNameListening() {
        this.configNameListening = false;
    }

    public void mouseScrolled(double verticalAmount, int screenH) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return;
        }
        this.refreshConfigList();
        int rowH = this.host.getFontHeight() + 6;
        float viewH = (float)(screenH - 44);
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

    public boolean keyPressed(int keyCode) {
        if (!this.configNameListening) {
            return false;
        }
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
                if (Wrapper.mc != null && Wrapper.mc.getWindow() != null && InputUtil.isKeyPressed(Wrapper.mc.getWindow().getHandle(), 341)) {
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
        return false;
    }

    public boolean charTyped(char chr) {
        if (this.configNameListening && StringHelper.isValidChar(chr)) {
            this.configNameInput = this.configNameInput + chr;
            if (this.configNameInput.length() > 64) {
                this.configNameInput = this.configNameInput.substring(0, 64);
            }
            return true;
        }
        return false;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, ClickGuiFrame frame) {
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
        float mx = frame.unitMouseX(mouseX);
        float my = frame.unitMouseY(mouseY);
        float baseX = frame.baseX(ClickGuiScreen.Page.Config);
        float screenUnitW = frame.scale == 0.0f ? (float)frame.screenW : (float)frame.screenW / frame.scale;
        float panelXf = Math.max(8.0f, (screenUnitW - (float)frame.panelW) / 2.0f);
        float x = baseX + panelXf + 10.0f;
        float w = (float)(frame.panelW - 20);
        float titleY = (float)frame.panelY + 10.0f;
        float listY = (float)frame.panelY + 28.0f;
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
        int rowH = this.host.getFontHeight() + 6;
        float rh = (float)rowH - 0.5f;
        float clipTop = listY - (float)rowH;
        float clipBottom = (float)frame.screenH - 20.0f;

        int selectedIndex = -1;
        if (this.selectedConfigName != null && !this.selectedConfigName.isEmpty()) {
            for (int i = 0; i < this.configNames.size(); ++i) {
                String n = this.configNames.get(i);
                if (n != null && n.equalsIgnoreCase(this.selectedConfigName)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        float targetY = 0.0f;
        if (selectedIndex >= 0) {
            targetY = listY + (float)selectedIndex * (float)rowH - this.configScroll;
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
            if (!this.configSelectAnimInit) {
                this.configSelectAnimY = targetY;
                this.configSelectAnimInit = true;
            } else {
                this.configSelectAnimY += (targetY - this.configSelectAnimY) * a;
            }
        }

        for (int i = 0; i < this.configNames.size(); ++i) {
            float ry = listY + (float)i * (float)rowH - this.configScroll;
            if (ry + rh < clipTop || ry > clipBottom) continue;
            boolean hovered = mx >= x && mx <= x + listW && my >= ry && my <= ry + rh;
            int bg = hovered ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB();
            Render2DUtil.rect(context.getMatrices(), x, ry, x + listW, ry + rh, bg);
        }

        if (selectedIndex >= 0) {
            float ay = this.configSelectAnimY;
            boolean hoveredSelected = mx >= x && mx <= x + listW && my >= ay && my <= ay + rh;
            int activeAlpha = hoveredSelected ? gui.hoverAlpha.getValueInt() : gui.alpha.getValueInt();
            if (!(ay + rh < clipTop || ay > clipBottom)) {
                if (gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
                    Render2DUtil.drawLutRect(context.getMatrices(), x, ay, listW, rh, gui.getSpectrumLutId(), gui.getSpectrumLutHeight(), activeAlpha);
                } else {
                    Color ac = gui.getActiveColor((double)ay * 0.25);
                    Render2DUtil.rect(context.getMatrices(), x, ay, x + listW, ay + rh, ColorUtil.injectAlpha(ac, activeAlpha).getRGB());
                }
            }
        }

        for (int i = 0; i < this.configNames.size(); ++i) {
            String name = this.configNames.get(i);
            float ry = listY + (float)i * (float)rowH - this.configScroll;
            if (ry + rh < clipTop || ry > clipBottom) continue;
            boolean hovered = mx >= x && mx <= x + listW && my >= ry && my <= ry + rh;
            boolean selected = this.selectedConfigName != null && name != null && name.equalsIgnoreCase(this.selectedConfigName);
            int tc = hovered || selected ? gui.enableTextColor.getValue().getRGB() : gui.defaultTextColor.getValue().getRGB();
            float textY = this.host.getCenteredTextY(ry, rh);
            TextUtil.drawString(context, name, (double)(x + 6.0f), (double)textY, tc, customFont, shadow);
        }
        String selLabel = chinese ? "当前选择: " : "Selected: ";
        String appLabel = chinese ? "已应用: " : "Applied: ";
        String selectedName = this.selectedConfigName == null ? none : this.selectedConfigName;
        String appliedName = this.appliedConfigName == null ? none : this.appliedConfigName;
        TextUtil.drawString(context, selLabel + selectedName, (double)rightX, (double)titleY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float infoY2 = (float)titleY + (float)this.host.getFontHeight() + 4.0f;
        TextUtil.drawString(context, appLabel + appliedName, (double)rightX, (double)infoY2, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float nameLabelY = infoY2 + (float)this.host.getFontHeight() + 8.0f;
        String nameLabel = chinese ? "名称" : "Name";
        TextUtil.drawString(context, nameLabel, (double)rightX, (double)nameLabelY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        float boxY = nameLabelY + (float)this.host.getFontHeight() + 4.0f;
        float boxH = (float)rowH - 0.5f;
        boolean hoverBox = mx >= rightX && mx <= rightX + rightW && my >= boxY && my <= boxY + boxH;
        int boxBg = hoverBox || this.configNameListening ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, boxY, rightX + rightW, boxY + boxH, boxBg);
        String placeholder = chinese ? "请输入配置名" : "Please type config name";
        String show = this.configNameInput == null || this.configNameInput.isEmpty() ? placeholder : this.configNameInput;
        if (this.configNameListening) {
            show = show + StringButton.getIdleSign();
        }
        float boxTextY = this.host.getCenteredTextY(boxY, boxH);
        TextUtil.drawString(context, show, (double)(rightX + 6.0f), (double)boxTextY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY = boxY + boxH + 8.0f;
        float btnH = (float)rowH - 0.5f;
        String bCreate = chinese ? "创建新配置" : "Create New Config";
        String bBackup = chinese ? "备份" : "Backup";
        String bDelete = chinese ? "删除" : "Delete";
        String bSave = chinese ? "保存" : "Save";
        String bApply = chinese ? "应用" : "Apply";
        String bOpenFolder = chinese ? "打开配置文件夹" : "Open Folder";
        boolean canCreate = !this.sanitizeConfigName(this.configNameInput).isEmpty();
        boolean canSelect = this.selectedConfigName != null && !this.selectedConfigName.isEmpty();
        boolean canSave = canSelect || canCreate;
        boolean canApply = canSelect;
        boolean canBackup = canSelect;
        boolean canDelete = canSelect;
        boolean hCreate = mx >= rightX && mx <= rightX + rightW && my >= btnY && my <= btnY + btnH;
        int bgCreate = canCreate ? (hCreate ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY, rightX + rightW, btnY + btnH, bgCreate);
        TextUtil.drawString(context, bCreate, (double)(rightX + 6.0f), (double)this.host.getCenteredTextY(btnY, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY2 = btnY + btnH + 4.0f;
        boolean hBackup = mx >= rightX && mx <= rightX + rightW && my >= btnY2 && my <= btnY2 + btnH;
        int bgBackup = canBackup ? (hBackup ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY2, rightX + rightW, btnY2 + btnH, bgBackup);
        TextUtil.drawString(context, bBackup, (double)(rightX + 6.0f), (double)this.host.getCenteredTextY(btnY2, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY3 = btnY2 + btnH + 4.0f;
        boolean hDelete = mx >= rightX && mx <= rightX + rightW && my >= btnY3 && my <= btnY3 + btnH;
        int bgDelete = canDelete ? (hDelete ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY3, rightX + rightW, btnY3 + btnH, bgDelete);
        TextUtil.drawString(context, bDelete, (double)(rightX + 6.0f), (double)this.host.getCenteredTextY(btnY3, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY4 = btnY3 + btnH + 4.0f;
        boolean hSave = mx >= rightX && mx <= rightX + rightW && my >= btnY4 && my <= btnY4 + btnH;
        int bgSave = canSave ? (hSave ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY4, rightX + rightW, btnY4 + btnH, bgSave);
        TextUtil.drawString(context, bSave, (double)(rightX + 6.0f), (double)this.host.getCenteredTextY(btnY4, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY5 = btnY4 + btnH + 4.0f;
        boolean hApply = mx >= rightX && mx <= rightX + rightW && my >= btnY5 && my <= btnY5 + btnH;
        int bgApply = canApply ? (hApply ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB()) : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY5, rightX + rightW, btnY5 + btnH, bgApply);
        TextUtil.drawString(context, bApply, (double)(rightX + 6.0f), (double)this.host.getCenteredTextY(btnY5, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float btnY6 = btnY5 + btnH + 4.0f;
        boolean hOpenFolder = mx >= rightX && mx <= rightX + rightW && my >= btnY6 && my <= btnY6 + btnH;
        int bgOpenFolder = hOpenFolder ? gui.hoverColor.getValue().getRGB() : gui.defaultColor.getValue().getRGB();
        Render2DUtil.rect(context.getMatrices(), rightX, btnY6, rightX + rightW, btnY6 + btnH, bgOpenFolder);
        TextUtil.drawString(context, bOpenFolder, (double)(rightX + 6.0f), (double)this.host.getCenteredTextY(btnY6, btnH), gui.enableTextColor.getValue().getRGB(), customFont, shadow);
        float hintY = btnY6 + btnH + 8.0f;
        TextUtil.drawString(context, hint, (double)(rightX + 2.0f), (double)hintY, gui.defaultTextColor.getValue().getRGB(), customFont, shadow);
        if (this.lastSavedConfigName != null && !this.lastSavedConfigName.isEmpty()) {
            long now = System.currentTimeMillis();
            if (now - this.lastSavedConfigTime <= 2500L) {
                String savedMsg = chinese ? "已保存: cfg/" + this.lastSavedConfigName + ".cfg" : "Saved: cfg/" + this.lastSavedConfigName + ".cfg";
                float savedY = hintY + (float)this.host.getFontHeight() + 4.0f;
                TextUtil.drawString(context, savedMsg, (double)(rightX + 2.0f), (double)savedY, gui.enableTextColor.getValue().getRGB(), customFont, shadow);
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, ClickGuiFrame frame) {
        if (Wrapper.mc == null || Wrapper.mc.getWindow() == null) {
            return false;
        }
        if (mouseButton != 0) {
            this.configNameListening = false;
            return false;
        }
        float mx = frame.unitMouseX(mouseX);
        float my = frame.unitMouseY(mouseY);
        float baseX = frame.baseX(ClickGuiScreen.Page.Config);
        float screenUnitW = frame.scale == 0.0f ? (float)frame.screenW : (float)frame.screenW / frame.scale;
        float panelXf = Math.max(8.0f, (screenUnitW - (float)frame.panelW) / 2.0f);
        float x = baseX + panelXf + 10.0f;
        float w = (float)(frame.panelW - 20);
        float listY = (float)frame.panelY + 28.0f;
        float gap = 8.0f;
        float listW = w * 0.62f;
        float rightX = x + listW + gap;
        float rightW = w - listW - gap;
        int rowH = this.host.getFontHeight() + 6;
        float rh = (float)rowH - 0.5f;
        this.refreshConfigList();
        for (int i = 0; i < this.configNames.size(); ++i) {
            String name = this.configNames.get(i);
            float ry = listY + (float)i * (float)rowH - this.configScroll;
            if (mx >= x && mx <= x + listW && my >= ry && my <= ry + rh) {
                this.selectedConfigName = name;
                this.configNameListening = false;
                return true;
            }
        }
        float nameLabelY = (float)frame.panelY + 10.0f + (float)this.host.getFontHeight() + 4.0f + (float)this.host.getFontHeight() + 8.0f;
        float boxY = nameLabelY + (float)this.host.getFontHeight() + 4.0f;
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
        float btnY6 = btnY5 + btnH + 4.0f;
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
            final String deleting = this.selectedConfigName;
            boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
            String title = chinese ? "确认删除" : "Confirm Delete";
            String msg = chinese ? "确定删除配置: " + deleting + " ?" : "Delete config: " + deleting + " ?";
            this.host.openConfirm(title, msg, new Runnable() {
                @Override
                public void run() {
                    ClickGuiConfigPage.this.deleteConfig(deleting);
                    ClickGuiConfigPage.this.selectedConfigName = ClickGuiConfigPage.this.configNames.isEmpty() ? null : ClickGuiConfigPage.this.configNames.get(0);
                    if (deleting != null && deleting.equalsIgnoreCase(ClickGuiConfigPage.this.appliedConfigName)) {
                        ClickGuiConfigPage.this.appliedConfigName = null;
                    }
                }
            });
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY4 && my <= btnY4 + btnH && canSave) {
            String saving = canSelect ? this.selectedConfigName : this.configNameInput;
            String saved = this.saveConfig(saving);
            if (saved != null) {
                this.lastSavedConfigName = saved;
                this.lastSavedConfigTime = System.currentTimeMillis();
                this.selectedConfigName = saved;
            }
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY5 && my <= btnY5 + btnH && canSelect) {
            final String applyName = this.selectedConfigName;
            boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
            String title = chinese ? "确认应用" : "Confirm Apply";
            String msg = chinese ? "应用配置: " + applyName : "Apply config: " + applyName;
            this.host.openConfirm(title, msg, new Runnable() {
                @Override
                public void run() {
                    ClickGuiConfigPage.this.loadConfig(applyName);
                }
            });
            return true;
        }
        if (mx >= rightX && mx <= rightX + rightW && my >= btnY6 && my <= btnY6 + btnH) {
            this.openConfigFolder();
            return true;
        }
        return false;
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

    private void openConfigFolder() {
        try {
            File folder = ConfigManager.getCfgFolder();
            if (folder == null) {
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();
                if (d != null && d.isSupported(Desktop.Action.OPEN)) {
                    d.open(folder);
                    return;
                }
            }
            String os = System.getProperty("os.name");
            String path = folder.getAbsolutePath();
            if (os != null) {
                String lower = os.toLowerCase();
                if (lower.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", path});
                    return;
                }
                if (lower.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", path});
                    return;
                }
            }
            Runtime.getRuntime().exec(new String[]{"xdg-open", path});
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
