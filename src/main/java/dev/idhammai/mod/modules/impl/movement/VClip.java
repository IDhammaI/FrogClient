/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 */
package dev.idhammai.mod.modules.impl.movement;

import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.UpdateEvent;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class VClip
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Jump));

    public VClip() {
        super("VClip", Module.Category.Movement);
        this.setChinese("\u7eb5\u5411\u7a7f\u5899");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.disable();
        switch (this.mode.getValue().ordinal()) {
            case 1: {
                VClip.mc.player.setPosition(VClip.mc.player.getX(), VClip.mc.player.getY() + 3.0, VClip.mc.player.getZ());
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(VClip.mc.player.getX(), VClip.mc.player.getY(), VClip.mc.player.getZ(), true));
                break;
            }
            case 2: {
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(VClip.mc.player.getX(), VClip.mc.player.getY() + 0.4199999868869781, VClip.mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(VClip.mc.player.getX(), VClip.mc.player.getY() + 0.7531999805212017, VClip.mc.player.getZ(), false));
                VClip.mc.player.setPosition(VClip.mc.player.getX(), VClip.mc.player.getY() + 1.0, VClip.mc.player.getZ());
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(VClip.mc.player.getX(), VClip.mc.player.getY(), VClip.mc.player.getZ(), true));
                break;
            }
            case 0: {
                double posX = VClip.mc.player.getX();
                double posY = Math.round(VClip.mc.player.getY());
                double posZ = VClip.mc.player.getZ();
                boolean onGround = VClip.mc.player.isOnGround();
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(posX, posY, posZ, onGround));
                double halfY = 0.005;
                VClip.mc.player.setPosition(posX, posY -= halfY, posZ);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(posX, posY, posZ, onGround));
                VClip.mc.player.setPosition(posX, posY -= halfY * 300.0, posZ);
                mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(posX, posY, posZ, onGround));
            }
        }
    }

    public static enum Mode {
        Glitch,
        Teleport,
        Jump;

    }
}

