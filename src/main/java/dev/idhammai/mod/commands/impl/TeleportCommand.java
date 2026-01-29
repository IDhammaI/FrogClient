/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.api.utils.path.TPUtils;
import dev.idhammai.mod.commands.Command;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public class TeleportCommand
extends Command {
    public TeleportCommand() {
        super("tp", "[x] [y] [z]");
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public void runCommand(String[] parameters) {
        double z;
        double y;
        double x;
        if (parameters.length != 3) {
            this.sendUsage();
            return;
        }
        if (this.isNumeric(parameters[0])) {
            x = Double.parseDouble(parameters[0]);
        } else {
            if (!parameters[0].startsWith("~")) {
                this.sendUsage();
                return;
            }
            if (this.isNumeric(parameters[0].replace("~", ""))) {
                x = TeleportCommand.mc.player.getX() + Double.parseDouble(parameters[0].replace("~", ""));
            } else {
                if (!parameters[0].replace("~", "").isEmpty()) {
                    this.sendUsage();
                    return;
                }
                x = TeleportCommand.mc.player.getX();
            }
        }
        if (this.isNumeric(parameters[1])) {
            y = Double.parseDouble(parameters[1]);
        } else {
            if (!parameters[1].startsWith("~")) {
                this.sendUsage();
                return;
            }
            if (this.isNumeric(parameters[1].replace("~", ""))) {
                y = TeleportCommand.mc.player.getY() + Double.parseDouble(parameters[1].replace("~", ""));
            } else {
                if (!parameters[1].replace("~", "").isEmpty()) {
                    this.sendUsage();
                    return;
                }
                y = TeleportCommand.mc.player.getY();
            }
        }
        if (this.isNumeric(parameters[2])) {
            z = Double.parseDouble(parameters[2]);
        } else {
            if (!parameters[2].startsWith("~")) {
                this.sendUsage();
                return;
            }
            if (this.isNumeric(parameters[2].replace("~", ""))) {
                z = TeleportCommand.mc.player.getZ() + Double.parseDouble(parameters[2].replace("~", ""));
            } else {
                if (!parameters[2].replace("~", "").isEmpty()) {
                    this.sendUsage();
                    return;
                }
                z = TeleportCommand.mc.player.getZ();
            }
        }
        TPUtils.newTeleport(new Vec3d(x, y, z));
        TeleportCommand.mc.player.setPosition(x, y, z);
        DecimalFormat df = new DecimalFormat("0.0");
        this.sendChatMessage("\u00a7fTeleported to \u00a7e" + df.format(x) + ", " + df.format(y) + ", " + df.format(z));
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return new String[]{"~ "};
    }
}

