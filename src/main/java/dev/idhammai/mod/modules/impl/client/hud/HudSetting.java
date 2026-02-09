package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;

public class HudSetting extends Module {
    public static HudSetting INSTANCE;
    public final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    public final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));

    public HudSetting() {
        super("HUDSetting", Module.Category.Client);
        this.setChinese("HUD设置");
        INSTANCE = this;
    }

    public static boolean useFont() {
        return INSTANCE == null || INSTANCE.font.getValue();
    }

    public static boolean useShadow() {
        return INSTANCE == null || INSTANCE.shadow.getValue();
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
