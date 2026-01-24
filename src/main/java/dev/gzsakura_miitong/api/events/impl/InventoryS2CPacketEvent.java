/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.InventoryS2CPacket
 */
package dev.gzsakura_miitong.api.events.impl;

import dev.gzsakura_miitong.api.events.Event;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

public class InventoryS2CPacketEvent
extends Event {
    private static final InventoryS2CPacketEvent INSTANCE = new InventoryS2CPacketEvent();
    public InventoryS2CPacket packet;

    private InventoryS2CPacketEvent() {
    }

    public static InventoryS2CPacketEvent get(InventoryS2CPacket hand) {
        InventoryS2CPacketEvent.INSTANCE.packet = hand;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

