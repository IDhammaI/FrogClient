/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.modules.impl.render;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.Render3DEvent;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;

public class Zoom
extends Module {
    public static Zoom INSTANCE;
    public static boolean on;
    public final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut));
    final SliderSetting fov = this.add(new SliderSetting("ZoomFov", 60.0, 0.0, 130.0, 1.0));
    final Animation animation = new Animation();
    private final SliderSetting animTime = this.add(new SliderSetting("AnimTime", 300, 0, 1000));
    public double currentFov;

    public Zoom() {
        super("Zoom", Module.Category.Render);
        this.setChinese("\u653e\u5927");
        INSTANCE = this;
        Frog.EVENT_BUS.subscribe(new ZoomAnim());
    }

    @Override
    public void onEnable() {
        if (Zoom.nullCheck()) {
            this.disable();
        }
    }

    static {
        on = false;
    }

    public class ZoomAnim {
        @EventListener
        public void onRender3D(Render3DEvent event) {
            if (Zoom.this.isOn()) {
                Zoom.this.currentFov = Zoom.this.animation.get(Zoom.this.fov.getValue(), Zoom.this.animTime.getValueInt(), Zoom.this.ease.getValue());
                on = true;
            } else if (on) {
                Zoom.this.currentFov = Zoom.this.animation.get(0.0, Zoom.this.animTime.getValueInt(), Zoom.this.ease.getValue());
                if ((int)Zoom.this.currentFov == 0) {
                    on = false;
                }
            }
        }
    }
}
