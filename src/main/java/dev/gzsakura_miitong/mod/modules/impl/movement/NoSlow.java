/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package dev.gzsakura_miitong.mod.modules.impl.movement;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.InteractItemEvent;
import dev.gzsakura_miitong.api.events.impl.KeyboardInputEvent;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.api.utils.player.MovementUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class NoSlow
extends Module {
    public static NoSlow INSTANCE;
    final Queue<ClickSlotC2SPacket> storedClicks = new LinkedList<ClickSlotC2SPacket>();
    final AtomicBoolean pause = new AtomicBoolean();
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Vanilla));
    private final BooleanSetting soulSand = this.add(new BooleanSetting("SoulSand", true));
    private final BooleanSetting sneak = this.add(new BooleanSetting("Sneak", false));
    private final BooleanSetting climb = this.add(new BooleanSetting("Climb", false));
    private final BooleanSetting gui = this.add(new BooleanSetting("Gui", true));
    private final BooleanSetting allowSneak = this.add(new BooleanSetting("AllowSneak", false, this.gui::getValue));
    private final EnumSetting<Bypass> clickBypass = this.add(new EnumSetting<Bypass>("GuiMoveBypass", Bypass.None));
    boolean using = false;
    int delay = 0;

    public NoSlow() {
        super("NoSlow", Module.Category.Movement);
        this.setChinese("\u65e0\u51cf\u901f");
        INSTANCE = this;
    }

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.using = NoSlow.mc.player.isUsingItem();
        --this.delay;
        if (this.using) {
            this.delay = 2;
        }
        if (this.using && !NoSlow.mc.player.isRiding() && !NoSlow.mc.player.isFallFlying()) {
            switch (this.mode.getValue().ordinal()) {
                case 1: {
                    mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot));
                    break;
                }
                case 2: {
                    if (NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND) {
                        NoSlow.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Vitality.ROTATION.getLastYaw(), Vitality.ROTATION.getLastPitch()));
                        break;
                    }
                    NoSlow.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, Vitality.ROTATION.getLastYaw(), Vitality.ROTATION.getLastPitch()));
                    break;
                }
                case 3: {
                    NoSlow.mc.interactionManager.clickSlot(NoSlow.mc.player.currentScreenHandler.syncId, 1, 0, SlotActionType.PICKUP, (PlayerEntity)NoSlow.mc.player);
                    if (NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND) {
                        NoSlow.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Vitality.ROTATION.getLastYaw(), Vitality.ROTATION.getLastPitch()));
                        break;
                    }
                    NoSlow.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, Vitality.ROTATION.getLastYaw(), Vitality.ROTATION.getLastPitch()));
                }
            }
        }
        if (this.gui.getValue() && !(NoSlow.mc.currentScreen instanceof ChatScreen)) {
            for (KeyBinding k : new KeyBinding[]{NoSlow.mc.options.backKey, NoSlow.mc.options.leftKey, NoSlow.mc.options.rightKey}) {
                k.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)k.getBoundKeyTranslationKey()).getCode()));
            }
            NoSlow.mc.options.jumpKey.setPressed(ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce) && ElytraFly.INSTANCE.autoJump.getValue() || InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()));
            NoSlow.mc.options.forwardKey.setPressed(AutoWalk.INSTANCE.forward() || InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
            NoSlow.mc.options.sprintKey.setPressed(Sprint.INSTANCE.isOn() && !Sprint.INSTANCE.inWater() || InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.sprintKey.getBoundKeyTranslationKey()).getCode()));
            if (this.allowSneak.getValue()) {
                NoSlow.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }

    @EventListener(priority=100)
    public void keyboard(KeyboardInputEvent event) {
        if (this.sneak.getValue()) {
            event.cancel();
        }
        if (this.gui.getValue() && !(NoSlow.mc.currentScreen instanceof ChatScreen)) {
            for (KeyBinding k : new KeyBinding[]{NoSlow.mc.options.backKey, NoSlow.mc.options.leftKey, NoSlow.mc.options.rightKey}) {
                k.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)k.getBoundKeyTranslationKey()).getCode()));
            }
            NoSlow.mc.options.jumpKey.setPressed(ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce) && ElytraFly.INSTANCE.autoJump.getValue() || InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()));
            NoSlow.mc.options.forwardKey.setPressed(AutoWalk.INSTANCE.forward() || InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
            NoSlow.mc.options.sprintKey.setPressed(Sprint.INSTANCE.isOn() && !Sprint.INSTANCE.inWater() || InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.sprintKey.getBoundKeyTranslationKey()).getCode()));
            if (this.allowSneak.getValue()) {
                NoSlow.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)InputUtil.fromTranslationKey((String)NoSlow.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
            NoSlow.mc.player.input.pressingForward = NoSlow.mc.options.forwardKey.isPressed();
            NoSlow.mc.player.input.pressingBack = NoSlow.mc.options.backKey.isPressed();
            NoSlow.mc.player.input.pressingLeft = NoSlow.mc.options.leftKey.isPressed();
            NoSlow.mc.player.input.pressingRight = NoSlow.mc.options.rightKey.isPressed();
            NoSlow.mc.player.input.movementForward = NoSlow.getMovementMultiplier(NoSlow.mc.player.input.pressingForward, NoSlow.mc.player.input.pressingBack);
            NoSlow.mc.player.input.movementSideways = NoSlow.getMovementMultiplier(NoSlow.mc.player.input.pressingLeft, NoSlow.mc.player.input.pressingRight);
            NoSlow.mc.player.input.jumping = NoSlow.mc.options.jumpKey.isPressed();
            NoSlow.mc.player.input.sneaking = NoSlow.mc.options.sneakKey.isPressed();
        }
    }

    @EventListener
    public void onUse(InteractItemEvent event) {
        if (event.isPre()) {
            if (this.delay > 0) {
                NoSlow.mc.itemUseCooldown = 0;
                event.cancel();
            } else if (this.mode.is(Mode.GrimPacket) && NoSlow.mc.player != null && NoSlow.mc.player.getStackInHand(event.hand).getItem().getComponents().contains(DataComponentTypes.FOOD)) {
                NoSlow.mc.interactionManager.clickSlot(NoSlow.mc.player.currentScreenHandler.syncId, 1, 0, SlotActionType.PICKUP, (PlayerEntity)NoSlow.mc.player);
            }
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send e) {
        PlayerInteractItemC2SPacket packet;
        Packet<?> packet2;
        if (NoSlow.nullCheck()) {
            return;
        }
        if (this.mode.is(Mode.Drop) && (packet2 = e.getPacket()) instanceof PlayerInteractItemC2SPacket && (packet = (PlayerInteractItemC2SPacket)packet2).getHand() == Hand.MAIN_HAND && NoSlow.mc.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD)) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            return;
        }
        if (!MovementUtil.isMoving() || this.pause.get()) {
            return;
        }
        packet2 = e.getPacket();
        if (packet2 instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket click = (ClickSlotC2SPacket)packet2;
            switch (this.clickBypass.getValue().ordinal()) {
                case 3: {
                    if (click.getActionType() == SlotActionType.PICKUP || click.getActionType() == SlotActionType.PICKUP_ALL) break;
                    mc.getNetworkHandler().sendPacket((Packet)new CloseHandledScreenC2SPacket(0));
                    break;
                }
                case 1: {
                    if (!NoSlow.mc.player.isOnGround() || NoSlow.mc.world.getBlockCollisions((Entity)NoSlow.mc.player, NoSlow.mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) break;
                    if (NoSlow.mc.player.isSprinting()) {
                        mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)NoSlow.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    }
                    mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(NoSlow.mc.player.getX(), NoSlow.mc.player.getY() + 0.0656, NoSlow.mc.player.getZ(), false));
                    break;
                }
                case 2: {
                    if (!NoSlow.mc.player.isOnGround() || NoSlow.mc.world.getBlockCollisions((Entity)NoSlow.mc.player, NoSlow.mc.player.getBoundingBox().offset(0.0, 2.71875E-7, 0.0)).iterator().hasNext()) break;
                    if (NoSlow.mc.player.isSprinting()) {
                        mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)NoSlow.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    }
                    mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(NoSlow.mc.player.getX(), NoSlow.mc.player.getY() + 2.71875E-7, NoSlow.mc.player.getZ(), false));
                    break;
                }
                case 4: {
                    this.storedClicks.add(click);
                    e.cancel();
                }
            }
        }
        if (e.getPacket() instanceof CloseHandledScreenC2SPacket && this.clickBypass.is(Bypass.Delay)) {
            this.pause.set(true);
            while (!this.storedClicks.isEmpty()) {
                mc.getNetworkHandler().sendPacket((Packet)this.storedClicks.poll());
            }
            this.pause.set(false);
        }
    }

    @EventListener
    public void onPacketSendPost(PacketEvent.Sent e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket && NoSlow.mc.player.isSprinting() && this.clickBypass.is(Bypass.NCP)) {
            mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)NoSlow.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }

    public boolean noSlow() {
        return this.isOn() && this.mode.getValue() != Mode.None && (this.mode.getValue() != Mode.Drop && this.mode.getValue() != Mode.GrimPacket || this.using);
    }

    public boolean soulSand() {
        return this.isOn() && this.soulSand.getValue();
    }

    public boolean climb() {
        return this.isOn() && this.climb.getValue();
    }

    public static enum Mode {
        Vanilla,
        NCP,
        Grim,
        GrimPacket,
        Drop,
        None;

    }

    private static enum Bypass {
        None,
        NCP,
        NCP2,
        Grim,
        Delay;

    }
}

