/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.Frog;
import dev.idhammai.core.impl.PlayerManager;
import dev.idhammai.mod.commands.Command;
import dev.idhammai.mod.gui.windows.WindowsScreen;
import dev.idhammai.mod.gui.windows.impl.ItemSelectWindow;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Items;

public class CleanerCommand
extends Command {
    public CleanerCommand() {
        super("cleaner", "[\"\"/name/reset/clear/list] | [add/remove] [name]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            PlayerManager.screenToOpen = new WindowsScreen(new ItemSelectWindow(Frog.CLEANER));
            return;
        }
        switch (parameters[0]) {
            case "reset": {
                Frog.CLEANER.clear();
                Frog.CLEANER.add(Items.NETHERITE_SWORD.getTranslationKey());
                Frog.CLEANER.add(Items.NETHERITE_PICKAXE.getTranslationKey());
                Frog.CLEANER.add(Items.NETHERITE_HELMET.getTranslationKey());
                Frog.CLEANER.add(Items.NETHERITE_CHESTPLATE.getTranslationKey());
                Frog.CLEANER.add(Items.NETHERITE_LEGGINGS.getTranslationKey());
                Frog.CLEANER.add(Items.NETHERITE_BOOTS.getTranslationKey());
                Frog.CLEANER.add(Items.OBSIDIAN.getTranslationKey());
                Frog.CLEANER.add(Items.ENDER_CHEST.getTranslationKey());
                Frog.CLEANER.add(Items.ENDER_PEARL.getTranslationKey());
                Frog.CLEANER.add(Items.ENCHANTED_GOLDEN_APPLE.getTranslationKey());
                Frog.CLEANER.add(Items.EXPERIENCE_BOTTLE.getTranslationKey());
                Frog.CLEANER.add(Items.COBWEB.getTranslationKey());
                Frog.CLEANER.add(Items.POTION.getTranslationKey());
                Frog.CLEANER.add(Items.SPLASH_POTION.getTranslationKey());
                Frog.CLEANER.add(Items.TOTEM_OF_UNDYING.getTranslationKey());
                Frog.CLEANER.add(Items.END_CRYSTAL.getTranslationKey());
                Frog.CLEANER.add(Items.ELYTRA.getTranslationKey());
                Frog.CLEANER.add(Items.FLINT_AND_STEEL.getTranslationKey());
                Frog.CLEANER.add(Items.PISTON.getTranslationKey());
                Frog.CLEANER.add(Items.STICKY_PISTON.getTranslationKey());
                Frog.CLEANER.add(Items.REDSTONE_BLOCK.getTranslationKey());
                Frog.CLEANER.add(Items.GLOWSTONE.getTranslationKey());
                Frog.CLEANER.add(Items.RESPAWN_ANCHOR.getTranslationKey());
                Frog.CLEANER.add(Items.ANVIL.getTranslationKey());
                this.sendChatMessage("\u00a7fItems list got reset");
                return;
            }
            case "clear": {
                Frog.CLEANER.getList().clear();
                this.sendChatMessage("\u00a7fItems list got clear");
                return;
            }
            case "list": {
                if (Frog.CLEANER.getList().isEmpty()) {
                    this.sendChatMessage("\u00a7fItems list is empty");
                    return;
                }
                for (String name : Frog.CLEANER.getList()) {
                    this.sendChatMessage("\u00a7a" + name);
                }
                return;
            }
            case "add": {
                if (parameters.length == 2) {
                    Frog.CLEANER.add(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Frog.CLEANER.inList(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
            case "remove": {
                if (parameters.length == 2) {
                    Frog.CLEANER.remove(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Frog.CLEANER.inList(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7f" + parameters[0] + (Frog.CLEANER.inList(parameters[0]) ? " \u00a7ais in whitelist" : " \u00a7cisn't in whitelist"));
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
                if (!input.equalsIgnoreCase(Frog.getPrefix() + "cleaner") && !x.toLowerCase().startsWith(input)) continue;
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

