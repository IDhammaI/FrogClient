/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.ConnectScreen
 *  net.minecraft.client.network.ServerAddress
 *  net.minecraft.client.network.ServerInfo
 *  net.minecraft.network.listener.ClientCommonPacketListener
 *  net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
 *  net.minecraft.text.Text
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.mod.commands.Command;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

public class RejoinCommand
extends Command {
    public RejoinCommand() {
        super("rejoin", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        mc.executeTask(() -> {
            if (RejoinCommand.mc.world != null && mc.getCurrentServerEntry() != null) {
                ServerInfo lastestServerEntry = mc.getCurrentServerEntry();
                new DisconnectS2CPacket(Text.of((String)"Self kick")).apply((ClientCommonPacketListener)mc.getNetworkHandler());
                ConnectScreen.connect((Screen)new TitleScreen(), (MinecraftClient)mc, (ServerAddress)ServerAddress.parse((String)lastestServerEntry.address), (ServerInfo)lastestServerEntry, (boolean)false, null);
            }
        });
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

