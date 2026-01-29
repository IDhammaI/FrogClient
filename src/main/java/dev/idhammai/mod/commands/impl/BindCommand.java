/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.modules.Module;
import java.util.ArrayList;
import java.util.List;

public class BindCommand
extends Command {
    public BindCommand() {
        super("bind", "[module] [key]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        String moduleName = parameters[0];
        Module module = Frog.MODULE.getModuleByName(moduleName);
        if (module == null) {
            this.sendChatMessage("\u00a74Unknown module!");
            return;
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7fPlease specify keyCodec \u00a7bkey\u00a7f.");
            return;
        }
        String rkey = parameters[1];
        if (rkey == null) {
            this.sendChatMessage("\u00a74Unknown Error");
            return;
        }
        if (module.setBind(rkey.toUpperCase())) {
            this.sendChatMessage("\u00a7fBind for \u00a7r" + module.getName() + "\u00a7f set to \u00a7r" + rkey.toUpperCase());
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            for (Module x : Frog.MODULE.getModules()) {
                if (!input.equalsIgnoreCase(Frog.getPrefix() + "bind") && !x.getName().toLowerCase().startsWith(input)) continue;
                correct.add(x.getName());
            }
            int numCmds = correct.size();
            String[] commands = new String[numCmds];
            int i = 0;
            for (String x : correct) {
                commands[i++] = x;
            }
            return commands;
        }
        return null;
    }
}
