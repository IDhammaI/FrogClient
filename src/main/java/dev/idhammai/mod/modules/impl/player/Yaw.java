/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.impl.player;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.LookDirectionEvent;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;

public class Yaw
extends Module {
    public static Yaw INSTANCE;
    private final BooleanSetting yawLock = this.add(new BooleanSetting("YawLock", true));
    private final BooleanSetting smart = this.add(new BooleanSetting("Smart", true));
    private final SliderSetting yaw = this.add(new SliderSetting("Yaw", 0.0, -180.0, 180.0, 0.1, () -> !this.smart.getValue()));
    private final BooleanSetting pitchLock = this.add(new BooleanSetting("PitchLock", true));
    private final SliderSetting pitch = this.add(new SliderSetting("Pitch", 0.0, -90.0, 90.0, 0.1));
    private final BooleanSetting lock = this.add(new BooleanSetting("Lock", true));

    public Yaw() {
        super("Yaw", Module.Category.Player);
        this.setChinese("\u89c6\u89d2\u9501\u5b9a");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.yawLock.getValue()) {
            Yaw.mc.player.setYaw(this.smart.getValue() ? (float)Math.round((Yaw.mc.player.getYaw() + 1.0f) / 45.0f) * 45.0f : this.yaw.getValueFloat());
        }
        if (this.pitchLock.getValue()) {
            Yaw.mc.player.setPitch(this.pitch.getValueFloat());
        }
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event) {
        if (this.lock.getValue()) {
            event.cancel();
        }
    }
}

