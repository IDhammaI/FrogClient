/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ServerAddress
 *  net.minecraft.client.network.ServerInfo
 */
package dev.idhammai.api.events.impl;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerConnectBeginEvent {
    private static final ServerConnectBeginEvent INSTANCE = new ServerConnectBeginEvent();
    public ServerAddress address;
    public ServerInfo info;

    public static ServerConnectBeginEvent get(ServerAddress address, ServerInfo info) {
        ServerConnectBeginEvent.INSTANCE.address = address;
        ServerConnectBeginEvent.INSTANCE.info = info;
        return INSTANCE;
    }
}

