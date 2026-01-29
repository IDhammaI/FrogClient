/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.modules.Module;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BindsCommand
extends Command {
    public BindsCommand() {
        super("binds", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        ArrayList<String> list = new ArrayList<String>();
        for (Module x : Frog.MODULE.getModules()) {
            if (x.getBindSetting().getValue() == -1) continue;
            list.add("\u00a7f" + x.getDisplayName() + " \u00a77- \u00a7r" + x.getBindSetting().getKeyString());
        }
        Iterator<String> temp = list.iterator();
        int i = 0;
        StringBuilder string = new StringBuilder();
        while (temp.hasNext()) {
            if (i == 0) {
                string = new StringBuilder((String)temp.next());
            } else {
                string.append("\u00a77, ").append((String)temp.next());
            }
            if (++i < 3 && temp.hasNext()) continue;
            this.sendChatMessage(string.toString());
            i = 0;
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

