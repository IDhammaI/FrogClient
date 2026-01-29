/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.core.Manager;
import dev.idhammai.core.impl.ConfigManager;
import dev.idhammai.mod.commands.Command;
import java.io.File;
import java.util.List;

public class SaveCommand
extends Command {
    public SaveCommand() {
        super("save", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7fSaving config named " + parameters[0]);
            File folder = new File(SaveCommand.mc.runDirectory.getPath() + File.separator + dev.idhammai.Frog.NAME.toLowerCase() + File.separator + "cfg");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            ConfigManager.options = Manager.getFile("cfg" + File.separator + parameters[0] + ".cfg");
            Frog.save();
            ConfigManager.options = Manager.getFile("options.txt");
        } else {
            this.sendChatMessage("\u00a7fSaving..");
        }
        Frog.save();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}
