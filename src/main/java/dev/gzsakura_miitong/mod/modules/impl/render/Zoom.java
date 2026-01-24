/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.Render3DEvent;
import dev.gzsakura_miitong.api.utils.math.Animation;
import dev.gzsakura_miitong.api.utils.math.Easing;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;

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
        Vitality.EVENT_BUS.subscribe(new ZoomAnim());
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

