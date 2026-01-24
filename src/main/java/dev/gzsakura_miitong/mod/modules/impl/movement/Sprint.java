/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.MathHelper
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.SprintEvent;
import dev.gzsakura_miitong.api.events.impl.TickEvent;
import dev.gzsakura_miitong.api.events.impl.TickMovementEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateRotateEvent;
import dev.gzsakura_miitong.api.utils.path.BaritoneUtil;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.client.AntiCheat;
import dev.gzsakura_miitong.mod.modules.impl.player.Freecam;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;

public class Sprint
extends Module {
    public static Sprint INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Legit));
    public final BooleanSetting inWaterPause = this.add(new BooleanSetting("InWaterPause", true));
    public final BooleanSetting inWebPause = this.add(new BooleanSetting("InWebPause", true));
    public final BooleanSetting sneakingPause = this.add(new BooleanSetting("SneakingPause", false));
    public final BooleanSetting blindnessPause = this.add(new BooleanSetting("BlindnessPause", false));
    public final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", false));
    public final BooleanSetting lagPause = this.add(new BooleanSetting("LagPause", true));
    boolean pause = false;

    public Sprint() {
        super("Sprint", "Permanently keeps player in sprinting mode.", Module.Category.Movement);
        this.setChinese("\u5f3a\u5236\u75be\u8dd1");
        INSTANCE = this;
    }

    public static float getSprintYaw(float yaw) {
        if (Sprint.mc.options.forwardKey.isPressed() && !Sprint.mc.options.backKey.isPressed()) {
            if (Sprint.mc.options.leftKey.isPressed() && !Sprint.mc.options.rightKey.isPressed()) {
                yaw -= 45.0f;
            } else if (Sprint.mc.options.rightKey.isPressed() && !Sprint.mc.options.leftKey.isPressed()) {
                yaw += 45.0f;
            }
        } else if (Sprint.mc.options.backKey.isPressed() && !Sprint.mc.options.forwardKey.isPressed()) {
            yaw += 180.0f;
            if (Sprint.mc.options.leftKey.isPressed() && !Sprint.mc.options.rightKey.isPressed()) {
                yaw += 45.0f;
            } else if (Sprint.mc.options.rightKey.isPressed() && !Sprint.mc.options.leftKey.isPressed()) {
                yaw -= 45.0f;
            }
        } else if (Sprint.mc.options.leftKey.isPressed() && !Sprint.mc.options.rightKey.isPressed()) {
            yaw -= 90.0f;
        } else if (Sprint.mc.options.rightKey.isPressed() && !Sprint.mc.options.leftKey.isPressed()) {
            yaw += 90.0f;
        }
        return MathHelper.wrapDegrees((float)yaw);
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (this.lagPause.getValue() && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.pause = true;
        }
    }

    public boolean inWater() {
        return this.inWaterPause.getValue() && Sprint.mc.player.isInFluid();
    }

    @EventListener
    public void onMove(TickMovementEvent event) {
        if (BaritoneUtil.isPathing()) {
            return;
        }
        if (this.inWater()) {
            return;
        }
        if (this.mode.getValue() == Mode.PressKey) {
            Sprint.mc.options.sprintKey.setPressed(true);
        } else {
            Sprint.mc.player.setSprinting(this.shouldSprint());
        }
    }

    @EventListener
    public void tick(TickEvent event) {
        if (event.isPost()) {
            this.pause = false;
        }
    }

    @EventListener
    public void sprint(SprintEvent event) {
        if (BaritoneUtil.isPathing() || this.mode.is(Mode.PressKey)) {
            return;
        }
        if (this.inWater()) {
            return;
        }
        event.cancel();
        event.setSprint(this.shouldSprint());
    }

    private boolean shouldSprint() {
        if (!(Sprint.mc.player.getHungerManager().getFoodLevel() <= 6 && !Sprint.mc.player.isCreative() || !MovementUtil.isMoving() || this.pause || Sprint.mc.player.isSneaking() && this.sneakingPause.getValue() || Alien.PLAYER.isInWeb((PlayerEntity)Sprint.mc.player) && this.inWebPause.getValue() || Sprint.mc.player.isUsingItem() && this.usingPause.getValue() || Sprint.mc.player.isRiding() || Sprint.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && this.blindnessPause.getValue())) {
            switch (this.mode.getValue().ordinal()) {
                case 1: {
                    if (AntiCheat.INSTANCE.movementSync()) {
                        return Sprint.mc.player.input.movementForward > 0.0f;
                    }
                    return HoleSnap.INSTANCE.isOn() || Sprint.mc.options.forwardKey.isPressed() && MathHelper.angleBetween((float)Sprint.mc.player.getYaw(), (float)Alien.ROTATION.rotationYaw) < 40.0f;
                }
                case 3: {
                    if (AntiCheat.INSTANCE.movementSync()) {
                        return Sprint.mc.player.input.movementForward > 0.0f;
                    }
                    return HoleSnap.INSTANCE.isOn() || MathHelper.angleBetween((float)Sprint.getSprintYaw(Sprint.mc.player.getYaw()), (float)Alien.ROTATION.rotationYaw) < 40.0f;
                }
                case 2: {
                    return true;
                }
            }
        }
        return false;
    }

    @EventListener(priority=-100)
    public void rotate(UpdateRotateEvent event) {
        if (BaritoneUtil.isPathing()) {
            return;
        }
        if (!((Sprint.mc.player.getHungerManager().getFoodLevel() <= 6 && !Sprint.mc.player.isCreative() || !MovementUtil.isMoving() || Freecam.INSTANCE.isOn() || Sprint.mc.player.isFallFlying() || Alien.PLAYER.isInWeb((PlayerEntity)Sprint.mc.player) && this.inWebPause.getValue() || Sprint.mc.player.isSneaking() && this.sneakingPause.getValue() || Sprint.mc.player.isRiding() || Sprint.mc.player.isUsingItem() && this.usingPause.getValue() || Sprint.mc.player.isInFluid() || !Freecam.INSTANCE.isOff() || Sprint.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) && this.blindnessPause.getValue() || !this.mode.is(Mode.Rotation) || event.isModified())) {
            event.setYaw(Sprint.getSprintYaw(Sprint.mc.player.getYaw()));
        }
    }

    public static enum Mode {
        PressKey,
        Legit,
        Rage,
        Rotation;

    }
}

