/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec3d
 */
package dev.idhammai.mod.commands.impl;

import dev.idhammai.core.impl.RotationManager;
import dev.idhammai.mod.commands.Command;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public class AimCommand
extends Command {
    public AimCommand() {
        super("aim", "[x] [y] [z]");
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
                x = AimCommand.mc.player.getX() + Double.parseDouble(parameters[0].replace("~", ""));
            } else {
                if (!parameters[0].replace("~", "").isEmpty()) {
                    this.sendUsage();
                    return;
                }
                x = AimCommand.mc.player.getX();
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
                y = AimCommand.mc.player.getY() + Double.parseDouble(parameters[1].replace("~", ""));
            } else {
                if (!parameters[1].replace("~", "").isEmpty()) {
                    this.sendUsage();
                    return;
                }
                y = AimCommand.mc.player.getY();
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
                z = AimCommand.mc.player.getZ() + Double.parseDouble(parameters[2].replace("~", ""));
            } else {
                if (!parameters[2].replace("~", "").isEmpty()) {
                    this.sendUsage();
                    return;
                }
                z = AimCommand.mc.player.getZ();
            }
        }
        float[] angle = RotationManager.getRotation(new Vec3d(x, y, z));
        AimCommand.mc.player.setYaw(angle[0]);
        AimCommand.mc.player.setPitch(angle[1]);
        DecimalFormat df = new DecimalFormat("0.0");
        this.sendChatMessage("\u00a7fAim to \u00a7eX:" + df.format(x) + " Y:" + df.format(y) + " Z:" + df.format(z));
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return new String[]{"~ "};
    }
}

