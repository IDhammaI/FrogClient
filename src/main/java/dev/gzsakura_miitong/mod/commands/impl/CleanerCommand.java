/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 */
package dev.gzsakura_miitong.mod.commands.impl;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.core.impl.PlayerManager;
import dev.gzsakura_miitong.mod.commands.Command;
import dev.gzsakura_miitong.mod.gui.windows.WindowsScreen;
import dev.gzsakura_miitong.mod.gui.windows.impl.ItemSelectWindow;
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
            PlayerManager.screenToOpen = new WindowsScreen(new ItemSelectWindow(Vitality.CLEANER));
            return;
        }
        switch (parameters[0]) {
            case "reset": {
                Vitality.CLEANER.clear();
                Vitality.CLEANER.add(Items.NETHERITE_SWORD.getTranslationKey());
                Vitality.CLEANER.add(Items.NETHERITE_PICKAXE.getTranslationKey());
                Vitality.CLEANER.add(Items.NETHERITE_HELMET.getTranslationKey());
                Vitality.CLEANER.add(Items.NETHERITE_CHESTPLATE.getTranslationKey());
                Vitality.CLEANER.add(Items.NETHERITE_LEGGINGS.getTranslationKey());
                Vitality.CLEANER.add(Items.NETHERITE_BOOTS.getTranslationKey());
                Vitality.CLEANER.add(Items.OBSIDIAN.getTranslationKey());
                Vitality.CLEANER.add(Items.ENDER_CHEST.getTranslationKey());
                Vitality.CLEANER.add(Items.ENDER_PEARL.getTranslationKey());
                Vitality.CLEANER.add(Items.ENCHANTED_GOLDEN_APPLE.getTranslationKey());
                Vitality.CLEANER.add(Items.EXPERIENCE_BOTTLE.getTranslationKey());
                Vitality.CLEANER.add(Items.COBWEB.getTranslationKey());
                Vitality.CLEANER.add(Items.POTION.getTranslationKey());
                Vitality.CLEANER.add(Items.SPLASH_POTION.getTranslationKey());
                Vitality.CLEANER.add(Items.TOTEM_OF_UNDYING.getTranslationKey());
                Vitality.CLEANER.add(Items.END_CRYSTAL.getTranslationKey());
                Vitality.CLEANER.add(Items.ELYTRA.getTranslationKey());
                Vitality.CLEANER.add(Items.FLINT_AND_STEEL.getTranslationKey());
                Vitality.CLEANER.add(Items.PISTON.getTranslationKey());
                Vitality.CLEANER.add(Items.STICKY_PISTON.getTranslationKey());
                Vitality.CLEANER.add(Items.REDSTONE_BLOCK.getTranslationKey());
                Vitality.CLEANER.add(Items.GLOWSTONE.getTranslationKey());
                Vitality.CLEANER.add(Items.RESPAWN_ANCHOR.getTranslationKey());
                Vitality.CLEANER.add(Items.ANVIL.getTranslationKey());
                this.sendChatMessage("\u00a7fItems list got reset");
                return;
            }
            case "clear": {
                Vitality.CLEANER.getList().clear();
                this.sendChatMessage("\u00a7fItems list got clear");
                return;
            }
            case "list": {
                if (Vitality.CLEANER.getList().isEmpty()) {
                    this.sendChatMessage("\u00a7fItems list is empty");
                    return;
                }
                for (String name : Vitality.CLEANER.getList()) {
                    this.sendChatMessage("\u00a7a" + name);
                }
                return;
            }
            case "add": {
                if (parameters.length == 2) {
                    Vitality.CLEANER.add(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Vitality.CLEANER.inList(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
            case "remove": {
                if (parameters.length == 2) {
                    Vitality.CLEANER.remove(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Vitality.CLEANER.inList(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7f" + parameters[0] + (Vitality.CLEANER.inList(parameters[0]) ? " \u00a7ais in whitelist" : " \u00a7cisn't in whitelist"));
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
                if (!input.equalsIgnoreCase(Vitality.getPrefix() + "cleaner") && !x.toLowerCase().startsWith(input)) continue;
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

