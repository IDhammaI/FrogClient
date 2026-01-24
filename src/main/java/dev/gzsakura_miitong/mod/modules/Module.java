/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.PendingUpdateManager
 *  net.minecraft.client.network.SequencedPacketCreator
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.client.util.math.MatrixStack
 */
package dev.gzsakura_miitong.mod.modules;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.core.impl.CommandManager;
import dev.gzsakura_miitong.mod.Mod;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.client.BaritoneModule;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import dev.gzsakura_miitong.mod.modules.impl.client.ColorsModule;
import dev.gzsakura_miitong.mod.modules.impl.client.HUD;
import dev.gzsakura_miitong.mod.modules.settings.Setting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BindSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.StringSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Module
extends Mod {
    public final BooleanSetting drawn;
    private final List<Setting> settings = new ArrayList<Setting>();
    private final String description;
    private final Category category;
    private final BindSetting bindSetting;
    protected boolean state;
    private String chinese;

    public Module(String name, Category category) {
        this(name, "", category);
    }

    public Module(String name, String description, Category category) {
        super(name);
        this.category = category;
        this.description = description;
        this.bindSetting = this.add(new BindSetting("Key", this.isGuiModule() ? 344 : -1));
        this.drawn = this.add(new BooleanSetting("Drawn", !this.hideInModuleList()));
    }

    private boolean isGuiModule() {
        return this instanceof ClickGui;
    }

    private boolean hideInModuleList() {
        return this instanceof ColorsModule || this instanceof BaritoneModule || this instanceof AntiCheat || this instanceof ClientSetting || this instanceof HUD;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public String getArrayName() {
        return this.getDisplayName() + this.getArrayInfo();
    }

    public String getArrayInfo() {
        return this.getInfo() == null ? "" : " \u00a77[\u00a7f" + this.getInfo() + "\u00a77]";
    }

    public String getInfo() {
        return null;
    }

    public String getDisplayName() {
        if (ClientSetting.INSTANCE.chinese.getValue() && this.chinese != null) {
            return this.chinese;
        }
        return this.getName();
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public BindSetting getBindSetting() {
        return this.bindSetting;
    }

    public boolean isOn() {
        return this.state;
    }

    public boolean isOff() {
        return !this.isOn();
    }

    public void toggle() {
        if (this.isOn()) {
            this.disable();
        } else {
            this.enable();
        }
    }

    public void enable() {
        if (this.state) {
            return;
        }
        if (!Module.nullCheck() && this.drawn.getValue() && ClientSetting.INSTANCE.toggle.getValue()) {
            int id = ClientSetting.INSTANCE.onlyOne.getValue() ? -1 : this.hashCode();
            switch (ClientSetting.INSTANCE.messageStyle.getValue()) {
                case Mio: {
                    CommandManager.sendMessageId("\u00a72[+] \u00a7f" + this.getDisplayName(), id);
                    break;
                }
                case Debug: {
                    CommandManager.sendMessageId(this.getCategory().name().toLowerCase() + "." + this.getDisplayName().toLowerCase() + ".\u00a7aenable", id);
                    break;
                }
                case Lowercase: {
                    CommandManager.sendMessageId(this.getDisplayName().toLowerCase() + " \u00a7aenabled", id);
                    break;
                }
                case Melon: {
                    CommandManager.sendMessageId("\u00a7b" + this.getDisplayName() + " \u00a7aEnabled.", id);
                    break;
                }
                case Normal: {
                    CommandManager.sendMessageId("\u00a7f" + this.getDisplayName() + " \u00a7aEnabled", id);
                    break;
                }
                case Future: {
                    CommandManager.sendMessageId("\u00a77" + this.getDisplayName() + " toggled \u00a7aon", id);
                    break;
                }
                case Chinese: {
                    CommandManager.sendMessageId(this.getDisplayName() + " \u00a7a\u5f00\u542f", id);
                    break;
                }
                case Moon: {
                    CommandManager.sendChatMessageWidthIdNoSync("\u00a7f[\u00a7b" + ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f] [\u00a73" + this.getDisplayName() + "\u00a7f] \u00a77toggled \u00a7aon", id);
                    break;
                }
                case Earth: {
                    CommandManager.sendChatMessageWidthIdNoSync("\u00a7l" + this.getDisplayName() + " \u00a7aenabled.", id);
                }
            }
        }
        if (Vitality.MODULE != null) {
            Vitality.MODULE.showToggleBanner(this, true);
        }
        this.state = true;
        Vitality.EVENT_BUS.subscribe(this);
        this.onToggle();
        this.onEnable();
    }

    public void disable() {
        if (!this.state) {
            return;
        }
        if (!Module.nullCheck() && this.drawn.getValue() && ClientSetting.INSTANCE.toggle.getValue()) {
            int id = ClientSetting.INSTANCE.onlyOne.getValue() ? -1 : this.hashCode();
            switch (ClientSetting.INSTANCE.messageStyle.getValue()) {
                case Mio: {
                    CommandManager.sendMessageId("\u00a74[-] \u00a7f" + this.getDisplayName(), id);
                    break;
                }
                case Debug: {
                    CommandManager.sendMessageId(this.getCategory().name().toLowerCase() + "." + this.getDisplayName().toLowerCase() + ".\u00a7cdisable", id);
                    break;
                }
                case Lowercase: {
                    CommandManager.sendMessageId(this.getDisplayName().toLowerCase() + " \u00a7cdisabled", id);
                    break;
                }
                case Normal: {
                    CommandManager.sendMessageId("\u00a7f" + this.getDisplayName() + " \u00a7cDisabled", id);
                    break;
                }
                case Melon: {
                    CommandManager.sendMessageId("\u00a7b" + this.getDisplayName() + " \u00a7cDisabled.", id);
                    break;
                }
                case Future: {
                    CommandManager.sendMessageId("\u00a77" + this.getDisplayName() + " toggled \u00a7coff", id);
                    break;
                }
                case Earth: {
                    CommandManager.sendChatMessageWidthIdNoSync("\u00a7l" + this.getDisplayName() + " \u00a7cdisabled.", id);
                    break;
                }
                case Chinese: {
                    CommandManager.sendMessageId(this.getDisplayName() + " \u00a7c\u5173\u95ed", id);
                    break;
                }
                case Moon: {
                    CommandManager.sendChatMessageWidthIdNoSync("\u00a7f[\u00a7b" + ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f] [\u00a73" + this.getDisplayName() + "\u00a7f] \u00a77toggled \u00a7coff", id);
                }
            }
        }
        if (Vitality.MODULE != null) {
            Vitality.MODULE.showToggleBanner(this, false);
        }
        this.state = false;
        Vitality.EVENT_BUS.unsubscribe(this);
        this.onToggle();
        this.onDisable();
    }

    public void sendMessage(String message) {
        CommandManager.sendMessage(message);
    }

    public void setState(boolean state) {
        if (this.state == state) {
            return;
        }
        if (state) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public boolean setBind(String rkey) {
        int key;
        if (rkey.equalsIgnoreCase("none")) {
            this.bindSetting.setValue(-1);
            return true;
        }
        try {
            key = InputUtil.fromTranslationKey((String)("key.keyboard." + rkey.toLowerCase())).getCode();
        }
        catch (NumberFormatException e) {
            if (!Module.nullCheck()) {
                this.sendMessage("\u00a74Bad bind!");
            }
            return false;
        }
        if (rkey.equalsIgnoreCase("none")) {
            key = -1;
        }
        if (key == 0) {
            return false;
        }
        this.bindSetting.setValue(key);
        return true;
    }

    public void onDisable() {
    }

    public void onEnable() {
    }

    public void onToggle() {
    }

    public void onLogin() {
    }

    public void onLogout() {
    }

    public void onRender2D(DrawContext drawContext, float tickDelta) {
    }

    public void onRender3D(MatrixStack matrixStack) {
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public StringSetting add(StringSetting setting) {
        this.addSetting(setting);
        return setting;
    }

    public ColorSetting add(ColorSetting setting) {
        this.addSetting(setting);
        return setting;
    }

    public SliderSetting add(SliderSetting setting) {
        this.addSetting(setting);
        return setting;
    }

    public BooleanSetting add(BooleanSetting setting) {
        this.addSetting(setting);
        return setting;
    }

    public <T extends Enum<T>> EnumSetting<T> add(EnumSetting<T> setting) {
        this.addSetting(setting);
        return setting;
    }

    public BindSetting add(BindSetting setting) {
        this.addSetting(setting);
        return setting;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public static boolean nullCheck() {
        return Module.mc.player == null || Module.mc.player.input == null || Module.mc.world == null;
    }

    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.getNetworkHandler() == null || Module.mc.world == null) {
            return;
        }
        try (PendingUpdateManager pendingUpdateManager = Module.mc.world.getPendingUpdateManager().incrementSequence();){
            int i = pendingUpdateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
        }
    }

    public static enum Category {
        Combat{

            @Override
            public String getIcon() {
                return "1";
            }
        }
        ,
        Misc{

            @Override
            public String getIcon() {
                return "[";
            }
        }
        ,
        Render{

            @Override
            public String getIcon() {
                return "2";
            }
        }
        ,
        Movement{

            @Override
            public String getIcon() {
                return "8";
            }
        }
        ,
        Player{

            @Override
            public String getIcon() {
                return "5";
            }
        }
        ,
        Exploit{

            @Override
            public String getIcon() {
                return "6";
            }
        }
        ,
        Client{

            @Override
            public String getIcon() {
                return "7";
            }
        };


        public abstract String getIcon();
    }
}

