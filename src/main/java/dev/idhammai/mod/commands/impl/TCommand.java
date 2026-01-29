/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.modules.Module;
import java.util.ArrayList;
import java.util.List;

public class TCommand
extends Command {
    public TCommand() {
        super("t", "[module]");
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
            this.sendChatMessage("\u00a7fUnknown module!");
            return;
        }
        module.toggle();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            for (Module x : Frog.MODULE.getModules()) {
                if (!input.equalsIgnoreCase(Frog.getPrefix() + "t") && !x.getName().toLowerCase().startsWith(input)) continue;
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

