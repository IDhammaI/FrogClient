/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.DownloadingTerrainScreen
 *  net.minecraft.client.gui.screen.ProgressScreen
 */
package dev.gzsakura_miitong.mod.modules.impl.misc;

import dev.gzsakura_miitong.api.events.eventbus.EventListener;
import dev.gzsakura_miitong.api.events.impl.ClientTickEvent;
import dev.gzsakura_miitong.mod.modules.Module;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ProgressScreen;

public class NoTerrainScreen
extends Module {
    public NoTerrainScreen() {
        super("NoTerrainScreen", Module.Category.Misc);
        this.setChinese("\u6ca1\u6709\u52a0\u8f7d\u754c\u9762");
    }

    @EventListener
    public void onEvent(ClientTickEvent event) {
        if (NoTerrainScreen.nullCheck()) {
            return;
        }
        if (NoTerrainScreen.mc.currentScreen instanceof DownloadingTerrainScreen || NoTerrainScreen.mc.currentScreen instanceof ProgressScreen) {
            NoTerrainScreen.mc.currentScreen = null;
        }
    }
}

