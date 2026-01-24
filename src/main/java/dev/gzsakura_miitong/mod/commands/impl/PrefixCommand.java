/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.commands.impl;

import dev.gzsakura_miitong.mod.commands.Command;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import java.util.List;

public class PrefixCommand
extends Command {
    public PrefixCommand() {
        super("prefix", "[prefix]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        if (parameters[0].startsWith("/")) {
            this.sendChatMessage("\u00a7fPlease specify keyCodec valid \u00a7bprefix.");
            return;
        }
        ClientSetting.INSTANCE.prefix.setValue(parameters[0]);
        this.sendChatMessage("\u00a7bPrefix \u00a7fset to \u00a7e" + parameters[0]);
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

