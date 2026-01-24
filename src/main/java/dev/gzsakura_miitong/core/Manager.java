/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 */
package dev.gzsakura_miitong.core;

import java.io.File;
import net.minecraft.client.MinecraftClient;

public class Manager {
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    public static File getFile(String s) {
        File folder = Manager.getFolder();
        return new File(folder, s);
    }

    public static File getFolder() {
        File folder = new File(Manager.mc.runDirectory.getPath() + File.separator + dev.gzsakura_miitong.Vitality.NAME.toLowerCase());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}

