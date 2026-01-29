/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.core.impl.ConfigManager;
import dev.idhammai.mod.commands.Command;
import java.util.List;

public class ReloadCommand
extends Command {
    public ReloadCommand() {
        super("reload", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        this.sendChatMessage("\u00a7fReloading..");
        Frog.CONFIG = new ConfigManager();
        Frog.CONFIG.load();
        Frog.CLEANER.read();
        Frog.XRAY.read();
        Frog.TRADE.read();
        Frog.FRIEND.read();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}
