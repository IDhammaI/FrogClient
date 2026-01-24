/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 */
package dev.gzsakura_miitong.mod.modules.impl.player;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.asm.accessors.IPlayerMoveC2SPacket;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.exploit.BowBomb;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall
extends Module {
    private final EnumSetting<NoFallMode> mode = this.add(new EnumSetting<NoFallMode>("Mode", NoFallMode.Packet));
    private final SliderSetting getDistance = this.add(new SliderSetting("Distance", 3.0, 0.0, 8.0, 0.1));

    public NoFall() {
        super("NoFall", "Prevents fall damage.", Module.Category.Player);
        this.setChinese("\u6ca1\u6709\u6454\u843d\u4f24\u5bb3");
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (NoFall.nullCheck()) {
            return;
        }
        if (this.mode.is(NoFallMode.Grim) && this.checkFalling()) {
            mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(NoFall.mc.player.getX(), NoFall.mc.player.getY() + 1.0E-9, NoFall.mc.player.getZ(), NoFall.mc.player.getYaw(), NoFall.mc.player.getPitch(), false));
            NoFall.mc.player.onLanding();
        }
    }

    private boolean checkFalling() {
        return NoFall.mc.player.fallDistance > (float)NoFall.mc.player.getSafeFallDistance() && !NoFall.mc.player.isOnGround() && !NoFall.mc.player.isFallFlying();
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (NoFall.nullCheck()) {
            return;
        }
        for (ItemStack is : NoFall.mc.player.getArmorItems()) {
            if (is.getItem() != Items.ELYTRA) continue;
            return;
        }
        if (!this.mode.is(NoFallMode.Packet)) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket)packet;
            if (NoFall.mc.player.fallDistance >= (float)this.getDistance.getValue() && !BowBomb.send) {
                ((IPlayerMoveC2SPacket)packet2).setOnGround(true);
            }
        }
    }

    public static enum NoFallMode {
        Packet,
        Grim;

    }
}

