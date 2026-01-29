/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.common.CommonPongC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket
 *  net.minecraft.util.hit.BlockHitResult
 */
package dev.idhammai.mod.modules.impl.misc;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.EntityVelocityUpdateEvent;
import dev.idhammai.api.events.impl.PacketEvent;
import dev.idhammai.core.impl.CommandManager;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.combat.Criticals;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.asm.accessors.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;

public class PacketLogger
extends Module {
    public static PacketLogger INSTANCE;
    private final BooleanSetting moveFull = this.add(new BooleanSetting("MoveFull", true));
    private final BooleanSetting movePos = this.add(new BooleanSetting("MovePosition", true));
    private final BooleanSetting moveLook = this.add(new BooleanSetting("MoveLook", true));
    private final BooleanSetting moveGround = this.add(new BooleanSetting("MoveGround", true));
    private final BooleanSetting vehicleMove = this.add(new BooleanSetting("VehicleMove", true));
    private final BooleanSetting playerAction = this.add(new BooleanSetting("PlayerAction", true));
    private final BooleanSetting updateSlot = this.add(new BooleanSetting("UpdateSlot", true));
    private final BooleanSetting handSwing = this.add(new BooleanSetting("HandSwing", true));
    private final BooleanSetting pong = this.add(new BooleanSetting("Pong", true));
    private final BooleanSetting interactEntity = this.add(new BooleanSetting("InteractEntity", true));
    private final BooleanSetting interactBlock = this.add(new BooleanSetting("InteractBlock", true));
    private final BooleanSetting interactItem = this.add(new BooleanSetting("InteractItem", true));
    private final BooleanSetting closeScreen = this.add(new BooleanSetting("CloseScreen", true));
    private final BooleanSetting command = this.add(new BooleanSetting("ClientCommand", true));
    private final BooleanSetting status = this.add(new BooleanSetting("ClientStatus", true));
    private final BooleanSetting clickSlot = this.add(new BooleanSetting("ClickSlot", true));
    private final BooleanSetting pickInventory = this.add(new BooleanSetting("PickInventory", true));
    private final BooleanSetting teleportConfirm = this.add(new BooleanSetting("TeleportConfirm", true));
    private final BooleanSetting s2cVelocity = this.add(new BooleanSetting("S2cVelocity", true));

    public PacketLogger() {
        super("PacketLogger", Module.Category.Misc);
        this.setChinese("\u6570\u636e\u5305\u8bb0\u5f55");
        INSTANCE = this;
    }

    private void logPacket(String msg, Object ... args) {
        String s = String.format(msg, args);
        CommandManager.sendMessage(s);
    }

    @EventListener(priority=999999)
    public void velocity(EntityVelocityUpdateEvent event) {
        if (this.s2cVelocity.getValue() && event.getEntity() == PacketLogger.mc.player) {
            this.logPacket("S2C Velocity, x: %s, y: %s, z: %s, isExplosion: %s", event.getX(), event.getY(), event.getZ(), event.isExplosion());
        }
    }

    @EventListener(priority=-999999)
    public void onPacketSend(PacketEvent.Send event) {
        Packet<?> rawPacket = event.getPacket();
        if (rawPacket instanceof PlayerMoveC2SPacket.Full) {
            PlayerMoveC2SPacket.Full packet = (PlayerMoveC2SPacket.Full)rawPacket;
            if (this.moveFull.getValue()) {
                StringBuilder builder = new StringBuilder();
                builder.append("PlayerMove Full - ");
                if (packet.changesPosition()) {
                    builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
                }
                if (packet.changesLook()) {
                    builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
                }
                builder.append(" onground: ").append(packet.isOnGround());
                this.logPacket(builder.toString());
            }
        }
        if (rawPacket instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
            PlayerMoveC2SPacket.PositionAndOnGround packet = (PlayerMoveC2SPacket.PositionAndOnGround)rawPacket;
            if (this.movePos.getValue()) {
                StringBuilder builder = new StringBuilder();
                builder.append("PlayerMove PosGround - ");
                if (packet.changesPosition()) {
                    builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
                }
                builder.append(" onground: ").append(packet.isOnGround());
                this.logPacket(builder.toString());
            }
        }
        if (rawPacket instanceof PlayerMoveC2SPacket.LookAndOnGround) {
            PlayerMoveC2SPacket.LookAndOnGround packet = (PlayerMoveC2SPacket.LookAndOnGround)rawPacket;
            if (this.moveLook.getValue()) {
                StringBuilder builder = new StringBuilder();
                builder.append("PlayerMove LookGround - ");
                if (packet.changesLook()) {
                    builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
                }
                builder.append(" onground: ").append(packet.isOnGround());
                this.logPacket(builder.toString());
            }
        }
        if (rawPacket instanceof PlayerMoveC2SPacket.OnGroundOnly) {
            PlayerMoveC2SPacket.OnGroundOnly packet = (PlayerMoveC2SPacket.OnGroundOnly)rawPacket;
            if (this.moveGround.getValue()) {
                this.logPacket("PlayerMove Ground - onground: " + packet.isOnGround());
            }
        }
        if (rawPacket instanceof VehicleMoveC2SPacket) {
            VehicleMoveC2SPacket packet = (VehicleMoveC2SPacket)rawPacket;
            if (this.vehicleMove.getValue()) {
                this.logPacket("VehicleMove - x: %s, y: %s, z: %s, yaw: %s, pitch: %s", packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            }
        }
        if (rawPacket instanceof PlayerActionC2SPacket) {
            PlayerActionC2SPacket packet = (PlayerActionC2SPacket)rawPacket;
            IPlayerActionC2SPacket accessor = (IPlayerActionC2SPacket)packet;
            if (this.playerAction.getValue() && accessor.getDirection() != null) {
                this.logPacket("PlayerAction - action: %s, direction: %s, pos: %s", accessor.getAction().name(), accessor.getDirection().name(), accessor.getPos().toShortString());
            }
        }
        if (rawPacket instanceof UpdateSelectedSlotC2SPacket) {
            UpdateSelectedSlotC2SPacket packet = (UpdateSelectedSlotC2SPacket)rawPacket;
            if (this.updateSlot.getValue()) {
                this.logPacket("UpdateSlot - slot: %d", ((IUpdateSelectedSlotC2SPacket)packet).getSelectedSlot());
            }
        }
        if (rawPacket instanceof HandSwingC2SPacket) {
            HandSwingC2SPacket packet = (HandSwingC2SPacket)rawPacket;
            if (this.handSwing.getValue()) {
                this.logPacket("HandSwing - hand: %s", ((IHandSwingC2SPacket)packet).getHand().name());
            }
        }
        if (rawPacket instanceof CommonPongC2SPacket) {
            CommonPongC2SPacket packet = (CommonPongC2SPacket)rawPacket;
            if (this.pong.getValue()) {
                this.logPacket("Pong - %d", ((ICommonPongC2SPacket)packet).getParameter());
            }
        }
        if (rawPacket instanceof PlayerInteractEntityC2SPacket) {
            PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)rawPacket;
            if (this.interactEntity.getValue()) {
                this.logPacket("InteractEntity - Entity: %s, id: %s", Criticals.getEntity(packet).getName().getString(), Criticals.getEntity(packet).getId());
            }
        }
        if (rawPacket instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket packet = (PlayerInteractBlockC2SPacket)rawPacket;
            IPlayerInteractBlockC2SPacket accessor = (IPlayerInteractBlockC2SPacket)packet;
            if (this.interactBlock.getValue()) {
                BlockHitResult blockHitResult = accessor.getBlockHitResult();
                this.logPacket("InteractBlock - pos: %s, dir: %s, hand: %s", blockHitResult.getBlockPos().toShortString(), blockHitResult.getSide().name(), accessor.getHand().name());
            }
        }
        if (rawPacket instanceof PlayerInteractItemC2SPacket) {
            PlayerInteractItemC2SPacket packet = (PlayerInteractItemC2SPacket)rawPacket;
            if (this.interactItem.getValue()) {
                this.logPacket("InteractItem - hand: %s", ((IPlayerInteractItemC2SPacket)packet).getHand().name());
            }
        }
        if (rawPacket instanceof CloseHandledScreenC2SPacket) {
            CloseHandledScreenC2SPacket packet = (CloseHandledScreenC2SPacket)rawPacket;
            if (this.closeScreen.getValue()) {
                this.logPacket("CloseScreen - id: %s", ((ICloseHandledScreenC2SPacket)packet).getSyncId());
            }
        }
        if (rawPacket instanceof ClientCommandC2SPacket) {
            ClientCommandC2SPacket packet = (ClientCommandC2SPacket)rawPacket;
            if (this.command.getValue()) {
                this.logPacket("ClientCommand - mode: %s", packet.getMode().name());
            }
        }
        if (rawPacket instanceof ClientStatusC2SPacket) {
            ClientStatusC2SPacket packet = (ClientStatusC2SPacket)rawPacket;
            if (this.status.getValue()) {
                this.logPacket("ClientStatus - mode: %s", packet.getMode().name());
            }
        }
        if (rawPacket instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket packet = (ClickSlotC2SPacket)rawPacket;
            if (this.clickSlot.getValue()) {
                this.logPacket("ClickSlot - type: %s, slot: %s, button: %s, id: %s", packet.getActionType().name(), packet.getSlot(), packet.getButton(), packet.getSyncId());
            }
        }
        if (rawPacket instanceof PickFromInventoryC2SPacket) {
            PickFromInventoryC2SPacket packet = (PickFromInventoryC2SPacket)rawPacket;
            if (this.pickInventory.getValue()) {
                this.logPacket("PickInventory - slot: %s", packet.getSlot());
            }
        }
        if (rawPacket instanceof TeleportConfirmC2SPacket) {
            TeleportConfirmC2SPacket packet = (TeleportConfirmC2SPacket)rawPacket;
            if (this.teleportConfirm.getValue()) {
                this.logPacket("TeleportConfirm - id: %s", packet.getTeleportId());
            }
        }
    }
}

