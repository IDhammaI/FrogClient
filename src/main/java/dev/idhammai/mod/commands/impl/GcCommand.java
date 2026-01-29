/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.mod.commands.Command;
import java.util.List;

public class GcCommand
extends Command {
    public GcCommand() {
        super("gc", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        System.gc();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

