/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.commands.impl;

import dev.gzsakura_miitong.Alien;
import dev.gzsakura_miitong.mod.commands.Command;
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
                Alien.FRIEND.friendList.clear();
                this.sendChatMessage("\u00a7fFriends list got reset");
                return;
            }
            case "list": {
                if (Alien.FRIEND.friendList.isEmpty()) {
                    this.sendChatMessage("\u00a7fFriends list is empty");
                    return;
                }
                StringBuilder friends = new StringBuilder();
                int time = 0;
                boolean first = true;
                boolean start = true;
                for (String name : Alien.FRIEND.friendList) {
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
                    Alien.FRIEND.add(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Alien.FRIEND.isFriend(parameters[1]) ? " \u00a7ahas been friended" : " \u00a7chas been unfriended"));
                    return;
                }
                this.sendUsage();
                return;
            }
            case "remove": 
            case "del": {
                if (parameters.length == 2) {
                    Alien.FRIEND.remove(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Alien.FRIEND.isFriend(parameters[1]) ? " \u00a7ahas been friended" : " \u00a7chas been unfriended"));
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7f" + parameters[0] + (Alien.FRIEND.isFriend(parameters[0]) ? " \u00a7ais friended" : " \u00a7cisn't friended"));
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
                if (!input.equalsIgnoreCase(Alien.getPrefix() + "friend") && !x.toLowerCase().startsWith(input)) continue;
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

