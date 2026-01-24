/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.commands.impl;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.core.impl.ConfigManager;
import dev.gzsakura_miitong.mod.commands.Command;
import java.util.List;

public class ReloadCommand
extends Command {
    public ReloadCommand() {
        super("reload", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        this.sendChatMessage("\u00a7fReloading..");
        Vitality.CONFIG = new ConfigManager();
        Vitality.CONFIG.load();
        Vitality.CLEANER.read();
        Vitality.XRAY.read();
        Vitality.TRADE.read();
        Vitality.FRIEND.read();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

