/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package dev.gzsakura_miitong.mod.modules.impl.player;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.KeyboardInputEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateRotateEvent;
import dev.gzsakura_miitong.api.utils.math.MathUtil;
import dev.gzsakura_miitong.api.utils.path.BaritoneUtil;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.core.impl.RotationManager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;

public class Freecam
extends Module {
    public static Freecam INSTANCE;
    private final SliderSetting speed = this.add(new SliderSetting("HSpeed", 1.0, 0.0, 3.0));
    private final SliderSetting hspeed = this.add(new SliderSetting("VSpeed", 0.42, 0.0, 3.0));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private float playerYaw;
    private float playerPitch;

    public Freecam() {
        super("Freecam", Module.Category.Player);
        this.setChinese("\u81ea\u7531\u76f8\u673a");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (Freecam.nullCheck()) {
            this.disable();
            return;
        }
        this.playerYaw = this.getYaw();
        this.playerPitch = this.getPitch();
        this.fakePitch = this.getPitch();
        this.fakeYaw = this.getYaw();
        this.prevFakePitch = this.fakePitch;
        this.prevFakeYaw = this.fakeYaw;
        this.fakeX = Freecam.mc.player.getX();
        this.fakeY = Freecam.mc.player.getY() + (double)Freecam.mc.player.getEyeHeight(Freecam.mc.player.getPose());
        this.fakeZ = Freecam.mc.player.getZ();
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.rotate.getValue() && Freecam.mc.crosshairTarget != null && Freecam.mc.crosshairTarget.getPos() != null) {
            float[] angle = RotationManager.getRotation(Freecam.mc.crosshairTarget.getPos());
            this.playerYaw = angle[0];
            this.playerPitch = angle[1];
        }
        if (BaritoneUtil.isPathing()) {
            double[] motion = MovementUtil.directionSpeed(this.speed.getValue());
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            this.fakeX += motion[0];
            this.fakeZ += motion[1];
            if (Freecam.mc.options.jumpKey.isPressed()) {
                this.fakeY += this.hspeed.getValue();
            }
            if (Freecam.mc.options.sneakKey.isPressed()) {
                this.fakeY -= this.hspeed.getValue();
            }
        }
    }

    @EventListener(priority=200)
    public void onRotate(UpdateRotateEvent event) {
        if (BaritoneUtil.isPathing()) {
            return;
        }
        if (event.isModified()) {
            return;
        }
        event.setYawWithoutSync(this.playerYaw);
        event.setPitchWithoutSync(this.playerPitch);
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
        this.fakeYaw = this.getYaw();
        this.fakePitch = this.getPitch();
    }

    private float getYaw() {
        return Freecam.mc.player.getYaw();
    }

    private float getPitch() {
        return Freecam.mc.player.getPitch();
    }

    @EventListener
    public void onKeyboardInput(KeyboardInputEvent event) {
        if (Freecam.mc.player == null) {
            return;
        }
        double[] motion = MovementUtil.directionSpeed(this.speed.getValue());
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        this.fakeX += motion[0];
        this.fakeZ += motion[1];
        if (Freecam.mc.options.jumpKey.isPressed()) {
            this.fakeY += this.hspeed.getValue();
        }
        if (Freecam.mc.options.sneakKey.isPressed()) {
            this.fakeY -= this.hspeed.getValue();
        }
        Freecam.mc.player.input.movementForward = 0.0f;
        Freecam.mc.player.input.movementSideways = 0.0f;
        Freecam.mc.player.input.jumping = false;
        Freecam.mc.player.input.sneaking = false;
    }

    public float getFakeYaw() {
        return MathUtil.interpolate(this.prevFakeYaw, this.fakeYaw, mc.getRenderTickCounter().getTickDelta(true));
    }

    public float getFakePitch() {
        return MathUtil.interpolate(this.prevFakePitch, this.fakePitch, mc.getRenderTickCounter().getTickDelta(true));
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

