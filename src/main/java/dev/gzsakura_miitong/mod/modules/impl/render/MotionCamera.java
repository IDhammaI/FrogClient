/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.ClientTickEvent;
import dev.gzsakura_miitong.api.utils.math.AnimateUtil;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;

public class MotionCamera
extends Module {
    public static MotionCamera INSTANCE;
    public final BooleanSetting noFirstPerson = this.add(new BooleanSetting("NoFirstPerson", true));
    public final SliderSetting firstPersonSpeed = this.add(new SliderSetting("FirstPersonSpeed", 0.6, 0.0, 1.0, 0.01));
    public final SliderSetting speed = this.add(new SliderSetting("Speed", 0.3, 0.0, 1.0, 0.01));
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;

    public MotionCamera() {
        super("MotionCamera", Module.Category.Render);
        INSTANCE = this;
        this.setChinese("\u8fd0\u52a8\u76f8\u673a");
    }

    public boolean on() {
        return this.isOn() && (!this.noFirstPerson.getValue() || !MotionCamera.mc.options.getPerspective().isFirstPerson());
    }

    @Override
    public void onEnable() {
        if (MotionCamera.nullCheck()) {
            return;
        }
        this.fakeX = MotionCamera.mc.player.getX();
        this.fakeY = MotionCamera.mc.player.getY() + (double)MotionCamera.mc.player.getEyeHeight(MotionCamera.mc.player.getPose());
        this.fakeZ = MotionCamera.mc.player.getZ();
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
    }

    @EventListener
    public void onUpdate(ClientTickEvent event) {
        if (event.isPre() || MotionCamera.nullCheck()) {
            return;
        }
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        double speed = MotionCamera.mc.options.getPerspective().isFirstPerson() ? this.firstPersonSpeed.getValue() : this.speed.getValue();
        this.fakeX = AnimateUtil.animate(this.fakeX, MotionCamera.mc.player.getX(), speed);
        this.fakeY = AnimateUtil.animate(this.fakeY, MotionCamera.mc.player.getY() + (double)MotionCamera.mc.player.getEyeHeight(MotionCamera.mc.player.getPose()), speed);
        this.fakeZ = AnimateUtil.animate(this.fakeZ, MotionCamera.mc.player.getZ(), speed);
    }

    public double getFakeX() {
        return MathUtil.interpolate(this.prevFakeX, this.fakeX, (double)mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFakeY() {
        return MathUtil.interpolate(this.prevFakeY, this.fakeY, (double)mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFakeZ() {
        return MathUtil.interpolate(this.prevFakeZ, this.fakeZ, (double)mc.getRenderTickCounter().getTickDelta(true));
    }
}

