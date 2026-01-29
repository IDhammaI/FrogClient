/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 */
package dev.idhammai.mod.modules.impl.player;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.RotationEvent;
import dev.idhammai.api.events.impl.TickEvent;
import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.exploit.Blink;
import dev.idhammai.mod.modules.impl.movement.ElytraFly;
import dev.idhammai.mod.modules.impl.movement.Velocity;
import dev.idhammai.mod.modules.settings.enums.SwingSide;
import dev.idhammai.mod.modules.settings.enums.Timing;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoPearl
extends Module {
    public static AutoPearl INSTANCE;
    public static boolean throwing;
    public final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All));
    public final EnumSetting<SwingSide> interactSwing = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.All));
    private final BooleanSetting rotation = this.add(new BooleanSetting("Rotation", true));
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, this.yawStep::isOpen));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, this.yawStep::isOpen));
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, this.yawStep::isOpen));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 100, 0, 100, this.yawStep::isOpen));
    private final BooleanSetting sync = this.add(new BooleanSetting("Sync", true, this.yawStep::isOpen));

    public AutoPearl() {
        super("AutoPearl", Module.Category.Player);
        this.setChinese("\u6254\u73cd\u73e0");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (AutoPearl.nullCheck()) {
            this.disable();
            return;
        }
        if (AutoPearl.INSTANCE.inventory.getValue()) {
            if (InventoryUtil.findItemInventorySlotFromZero(Items.ENDER_PEARL) == -1) {
                this.disable();
                return;
            }
        } else if (InventoryUtil.findItem(Items.ENDER_PEARL) == -1) {
            this.disable();
            return;
        }
        if (this.shouldYawStep()) {
            return;
        }
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        this.throwPearl(AutoPearl.mc.player.getYaw(), AutoPearl.mc.player.getPitch());
        this.disable();
    }

    @EventListener
    public void onUpdate(TickEvent event) {
        if (AutoPearl.nullCheck()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (!this.shouldYawStep()) {
            this.throwPearl(AutoPearl.mc.player.getYaw(), AutoPearl.mc.player.getPitch());
            this.disable();
        } else if (Frog.ROTATION.inFov(AutoPearl.mc.player.getYaw(), AutoPearl.mc.player.getPitch(), this.fov.getValueFloat())) {
            if (this.sync.getValue()) {
                this.throwPearl(AutoPearl.mc.player.getYaw(), AutoPearl.mc.player.getPitch());
            } else {
                int pearl;
                throwing = true;
                if (AutoPearl.mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
                    AutoPearl.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Frog.ROTATION.getLastYaw(), Frog.ROTATION.getLastPitch()));
                    EntityUtil.swingHand(Hand.MAIN_HAND, this.interactSwing.getValue());
                } else if (this.inventory.getValue() && (pearl = InventoryUtil.findItemInventorySlotFromZero(Items.ENDER_PEARL)) != -1) {
                    InventoryUtil.inventorySwap(pearl, AutoPearl.mc.player.getInventory().selectedSlot);
                    AutoPearl.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Frog.ROTATION.getLastYaw(), Frog.ROTATION.getLastPitch()));
                    EntityUtil.swingHand(Hand.MAIN_HAND, this.interactSwing.getValue());
                    InventoryUtil.inventorySwap(pearl, AutoPearl.mc.player.getInventory().selectedSlot);
                    EntityUtil.syncInventory();
                } else {
                    pearl = InventoryUtil.findItem(Items.ENDER_PEARL);
                    if (pearl != -1) {
                        int old = AutoPearl.mc.player.getInventory().selectedSlot;
                        InventoryUtil.switchToSlot(pearl);
                        AutoPearl.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Frog.ROTATION.getLastYaw(), Frog.ROTATION.getLastPitch()));
                        EntityUtil.swingHand(Hand.MAIN_HAND, this.interactSwing.getValue());
                        InventoryUtil.switchToSlot(old);
                    }
                }
                throwing = false;
            }
            this.disable();
        }
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.shouldYawStep()) {
            event.setRotation(AutoPearl.mc.player.getYaw(), AutoPearl.mc.player.getPitch(), this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (AutoPearl.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    public void throwPearl(float yaw, float pitch) {
        int pearl;
        throwing = true;
        if (AutoPearl.mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            if (this.rotation.getValue()) {
                Frog.ROTATION.snapAt(yaw, pitch);
            }
            AutoPearl.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
            EntityUtil.swingHand(Hand.MAIN_HAND, this.interactSwing.getValue());
            if (this.rotation.getValue()) {
                Frog.ROTATION.snapBack();
            }
        } else if (this.inventory.getValue() && (pearl = InventoryUtil.findItemInventorySlotFromZero(Items.ENDER_PEARL)) != -1) {
            InventoryUtil.inventorySwap(pearl, AutoPearl.mc.player.getInventory().selectedSlot);
            if (this.rotation.getValue()) {
                Frog.ROTATION.snapAt(yaw, pitch);
            }
            AutoPearl.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
            EntityUtil.swingHand(Hand.MAIN_HAND, this.interactSwing.getValue());
            InventoryUtil.inventorySwap(pearl, AutoPearl.mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
            if (this.rotation.getValue()) {
                Frog.ROTATION.snapBack();
            }
        } else {
            pearl = InventoryUtil.findItem(Items.ENDER_PEARL);
            if (pearl != -1) {
                int old = AutoPearl.mc.player.getInventory().selectedSlot;
                InventoryUtil.switchToSlot(pearl);
                if (this.rotation.getValue()) {
                    Frog.ROTATION.snapAt(yaw, pitch);
                }
                AutoPearl.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
                EntityUtil.swingHand(Hand.MAIN_HAND, this.interactSwing.getValue());
                InventoryUtil.switchToSlot(old);
                if (this.rotation.getValue()) {
                    Frog.ROTATION.snapBack();
                }
            }
        }
        throwing = false;
    }

    static {
        throwing = false;
    }
}

