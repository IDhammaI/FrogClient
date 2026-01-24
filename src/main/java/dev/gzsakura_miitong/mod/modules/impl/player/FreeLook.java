/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package dev.gzsakura_miitong.mod.modules.impl.player;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.LookDirectionEvent;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import net.minecraft.client.util.math.MatrixStack;

public class FreeLook
extends Module {
    public static FreeLook INSTANCE;
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;

    public FreeLook() {
        super("FreeLook", Module.Category.Player);
        this.setChinese("\u81ea\u7531\u89c6\u89d2");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (FreeLook.nullCheck()) {
            this.disable();
            return;
        }
        this.fakePitch = FreeLook.mc.player.getPitch();
        this.fakeYaw = FreeLook.mc.player.getYaw();
        this.prevFakePitch = this.fakePitch;
        this.prevFakeYaw = this.fakeYaw;
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event) {
        this.fakeYaw += (float)event.getCursorDeltaX() * 0.15f;
        this.fakePitch += (float)event.getCursorDeltaY() * 0.15f;
        event.cancel();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
    }

    public float getFakeYaw() {
        return MathUtil.interpolate(this.prevFakeYaw, this.fakeYaw, mc.getRenderTickCounter().getTickDelta(true));
    }

    public float getFakePitch() {
        return MathUtil.interpolate(this.prevFakePitch, this.fakePitch, mc.getRenderTickCounter().getTickDelta(true));
    }
}

