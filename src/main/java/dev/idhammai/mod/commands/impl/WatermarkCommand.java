/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.modules.impl.client.HUD;
import java.util.Arrays;
import java.util.List;

public class WatermarkCommand
extends Command {
    public WatermarkCommand() {
        super("watermark", "[text]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        StringBuilder text = new StringBuilder();
        boolean first = true;
        for (String s : Arrays.stream(parameters).toList()) {
            if (first) {
                text.append(s);
                first = false;
                continue;
            }
            text.append(" ").append(s);
        }
        HUD.INSTANCE.waterMarkString.setValue(text.toString());
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

