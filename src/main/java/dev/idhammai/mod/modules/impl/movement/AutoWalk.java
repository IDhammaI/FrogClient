/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.impl.movement;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.api.utils.path.BaritoneUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;

public class AutoWalk
extends Module {
    public static AutoWalk INSTANCE;
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Forward));
    boolean start = false;

    public AutoWalk() {
        super("AutoWalk", Module.Category.Movement);
        this.setChinese("\u81ea\u52a8\u524d\u8fdb");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.start = false;
    }

    @Override
    public void onLogout() {
        this.disable();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.mode.is(Mode.Forward)) {
            AutoWalk.mc.options.forwardKey.setPressed(true);
        } else if (this.mode.is(Mode.Path)) {
            if (!this.start) {
                BaritoneUtil.forward();
                this.start = true;
            } else if (!BaritoneUtil.isActive()) {
                this.disable();
            }
        }
    }

    @Override
    public void onDisable() {
        BaritoneUtil.cancelEverything();
    }

    public boolean forward() {
        return this.isOn() && this.mode.is(Mode.Forward);
    }

    public static enum Mode {
        Forward,
        Path;

    }
}

