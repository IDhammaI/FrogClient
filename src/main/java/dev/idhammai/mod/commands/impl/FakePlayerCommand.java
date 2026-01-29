/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.modules.impl.misc.FakePlayer;
import java.util.ArrayList;
import java.util.List;

public class FakePlayerCommand
extends Command {
    public FakePlayerCommand() {
        super("fakeplayer", "[record/play]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            FakePlayer.INSTANCE.toggle();
            return;
        }
        String string = parameters[0];
        int n = 0;
        switch (string) {
            case "record": {
                FakePlayer.INSTANCE.record.setValue(!FakePlayer.INSTANCE.record.getValue());
                break;
            }
            case "play": {
                FakePlayer.INSTANCE.play.setValue(!FakePlayer.INSTANCE.play.getValue());
                break;
            }
            default: {
                this.sendUsage();
            }
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            List<String> list = List.of("record", "play");
            for (String x : list) {
                if (!input.equalsIgnoreCase(Frog.getPrefix() + "fakeplayer") && !x.toLowerCase().startsWith(input)) continue;
                correct.add(x);
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

