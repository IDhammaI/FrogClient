/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.modules.Module;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import net.minecraft.text.Text;

public class EsuCommand
extends Command {
    public EsuCommand() {
        super("esu", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 1) {
            Frog.THREAD.execute(() -> {
                try {
                    String inputLine;
                    URL url = new URL("https://api.xywlapi.cc/qqapi?qq=" + parameters[0]);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    while ((inputLine = in.readLine()) != null) {
                        if (Module.nullCheck()) {
                            return;
                        }
                        EsuCommand.mc.player.sendMessage(Text.of((String)inputLine));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

