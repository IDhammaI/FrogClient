/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.Frog;
import dev.idhammai.mod.commands.Command;
import java.util.ArrayList;
import java.util.List;

public class FriendCommand
extends Command {
    public FriendCommand() {
        super("friend", "[name/reset/list] | [add/remove] [name]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        switch (parameters[0]) {
            case "reset": {
                Frog.FRIEND.friendList.clear();
                this.sendChatMessage("\u00a7fFriends list got reset");
                return;
            }
            case "list": {
                if (Frog.FRIEND.friendList.isEmpty()) {
                    this.sendChatMessage("\u00a7fFriends list is empty");
                    return;
                }
                StringBuilder friends = new StringBuilder();
                int time = 0;
                boolean first = true;
                boolean start = true;
                for (String name : Frog.FRIEND.friendList) {
                    if (!first) {
                        friends.append(", ");
                    }
                    friends.append(name);
                    first = false;
                    if (++time <= 3) continue;
                    this.sendChatMessage((start ? "\u00a7eFriends \u00a7a" : "\u00a7a") + String.valueOf(friends));
                    friends = new StringBuilder();
                    start = false;
                    first = true;
                    time = 0;
                }
                if (first) {
                    this.sendChatMessage("\u00a7a" + String.valueOf(friends));
                }
                return;
            }
            case "add": {
                if (parameters.length == 2) {
                    Frog.FRIEND.add(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Frog.FRIEND.isFriend(parameters[1]) ? " \u00a7ahas been friended" : " \u00a7chas been unfriended"));
                    return;
                }
                this.sendUsage();
                return;
            }
            case "remove": 
            case "del": {
                if (parameters.length == 2) {
                    Frog.FRIEND.remove(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Frog.FRIEND.isFriend(parameters[1]) ? " \u00a7ahas been friended" : " \u00a7chas been unfriended"));
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7f" + parameters[0] + (Frog.FRIEND.isFriend(parameters[0]) ? " \u00a7ais friended" : " \u00a7cisn't friended"));
            return;
        }
        this.sendUsage();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            List<String> list = List.of("add", "remove", "list", "reset");
            for (String x : list) {
                if (!input.equalsIgnoreCase(Frog.getPrefix() + "friend") && !x.toLowerCase().startsWith(input)) continue;
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

