/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffects
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.MoveEvent;
import dev.gzsakura_miitong.api.utils.path.BaritoneUtil;
import dev.gzsakura_miitong.api.utils.player.EntityUtil;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import java.util.Objects;
import net.minecraft.entity.effect.StatusEffects;

public class Strafe
extends Module {
    public static Strafe INSTANCE;
    private final BooleanSetting airStop = this.add(new BooleanSetting("AirStop", true));
    private final BooleanSetting slowCheck = this.add(new BooleanSetting("SlowCheck", true));

    public Strafe() {
        super("Strafe", "Modifies sprinting", Module.Category.Movement);
        this.setChinese("\u7075\u6d3b\u79fb\u52a8");
        INSTANCE = this;
    }

    @EventListener
    public void onStrafe(MoveEvent event) {
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (Strafe.mc.player.isSneaking() || Fly.INSTANCE.isOn() || HoleSnap.INSTANCE.isOn() || Speed.INSTANCE.isOn() || Strafe.mc.player.isFallFlying() || EntityUtil.isInsideBlock() || Strafe.mc.player.isInLava() || Strafe.mc.player.isTouchingWater() || Strafe.mc.player.getAbilities().flying) {
            return;
        }
        if (!MovementUtil.isMoving()) {
            if (this.airStop.getValue()) {
                MovementUtil.setMotionX(0.0);
                MovementUtil.setMotionZ(0.0);
            }
            return;
        }
        double[] dir = MovementUtil.directionSpeed(this.getBaseMoveSpeed());
        event.setX(dir[0]);
        event.setZ(dir[1]);
    }

    public double getBaseMoveSpeed() {
        double n = 0.2873;
        if (!(!Strafe.mc.player.hasStatusEffect(StatusEffects.SPEED) || this.slowCheck.getValue() && Strafe.mc.player.hasStatusEffect(StatusEffects.SLOWNESS))) {
            n *= 1.0 + 0.2 * (double)(Objects.requireNonNull(Strafe.mc.player.getStatusEffect(StatusEffects.SPEED)).getAmplifier() + 1);
        }
        return n;
    }
}

