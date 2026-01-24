/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 */
package dev.gzsakura_miitong.mod.commands.impl;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.core.impl.PlayerManager;
import dev.gzsakura_miitong.mod.commands.Command;
import dev.gzsakura_miitong.mod.gui.windows.WindowsScreen;
import dev.gzsakura_miitong.mod.gui.windows.impl.ItemSelectWindow;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Items;

public class TradeCommand
extends Command {
    public TradeCommand() {
        super("trade", "[\"\"/name/reset/clear/list] | [add/remove] [name]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            PlayerManager.screenToOpen = new WindowsScreen(new ItemSelectWindow(Alien.TRADE));
            return;
        }
        switch (parameters[0]) {
            case "reset": {
                Alien.TRADE.clear();
                Alien.TRADE.add(Items.ENCHANTED_BOOK.getTranslationKey());
                Alien.TRADE.add(Items.DIAMOND_BLOCK.getTranslationKey());
                this.sendChatMessage("\u00a7fItems list got reset");
                return;
            }
            case "clear": {
                Alien.TRADE.clear();
                this.sendChatMessage("\u00a7fItems list got clear");
                return;
            }
            case "list": {
                if (Alien.TRADE.getList().isEmpty()) {
                    this.sendChatMessage("\u00a7fItems list is empty");
                    return;
                }
                for (String name : Alien.TRADE.getList()) {
                    this.sendChatMessage("\u00a7a" + name);
                }
                return;
            }
            case "add": {
                if (parameters.length == 2) {
                    Alien.TRADE.add(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Alien.TRADE.inWhitelist(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
            case "remove": {
                if (parameters.length == 2) {
                    Alien.TRADE.remove(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Alien.TRADE.inWhitelist(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7f" + parameters[0] + (Alien.TRADE.inWhitelist(parameters[0]) ? " \u00a7ais in whitelist" : " \u00a7cisn't in whitelist"));
            return;
        }
        this.sendUsage();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            List<String> list = List.of("add", "remove", "list", "reset", "clear");
            for (String x : list) {
                if (!input.equalsIgnoreCase(Alien.getPrefix() + "trade") && !x.toLowerCase().startsWith(input)) continue;
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

