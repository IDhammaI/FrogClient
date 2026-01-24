/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 */
package dev.gzsakura_miitong.mod.modules.impl.player;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.PacketEvent;
import dev.gzsakura_miitong.api.events.impl.UpdateEvent;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public class PacketEat
extends Module {
    public static PacketEat INSTANCE;
    private final BooleanSetting deSync = this.add(new BooleanSetting("DeSync", false));
    private final BooleanSetting noRelease = this.add(new BooleanSetting("NoRelease", true));

    public PacketEat() {
        super("PacketEat", Module.Category.Player);
        this.setChinese("\u53d1\u5305\u8fdb\u98df");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.deSync.getValue() && PacketEat.mc.player.isUsingItem() && PacketEat.mc.player.getActiveItem().getItem().getComponents().contains(DataComponentTypes.FOOD)) {
            Module.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(PacketEat.mc.player.getActiveHand(), id, Alien.ROTATION.getLastYaw(), Alien.ROTATION.getLastPitch()));
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        PlayerActionC2SPacket packet;
        Packet<?> packet2;
        if (this.noRelease.getValue() && (packet2 = event.getPacket()) instanceof PlayerActionC2SPacket && (packet = (PlayerActionC2SPacket)packet2).getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && PacketEat.mc.player.getActiveItem().getItem().getComponents().contains(DataComponentTypes.FOOD)) {
            event.cancel();
        }
    }
}

