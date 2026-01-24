/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  org.apache.commons.io.IOUtils
 */
package dev.gzsakura_miitong.core.impl;

import dev.gzsakura_miitong.core.Manager;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.impl.render.Xray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import org.apache.commons.io.IOUtils;

public class XrayManager
extends Manager {
    private final ArrayList<String> list = new ArrayList();

    public XrayManager() {
        this.read();
    }

    public ArrayList<String> getList() {
        return this.list;
    }

    public boolean inWhitelist(String name) {
        return this.list.contains(name) || this.list.contains(name.replace("block.minecraft.", "").replace("item.minecraft.", ""));
    }

    public void clear() {
        this.list.clear();
    }

    public void remove(String name) {
        if (this.list.remove(name = name.replace("block.minecraft.", "").replace("item.minecraft.", "")) && !Module.nullCheck() && Xray.INSTANCE.isOn()) {
            XrayManager.mc.worldRenderer.reload();
        }
    }

    public void add(String name) {
        if (!this.list.contains(name = name.replace("block.minecraft.", "").replace("item.minecraft.", ""))) {
            this.list.add(name);
            if (!Module.nullCheck() && Xray.INSTANCE.isOn()) {
                XrayManager.mc.worldRenderer.reload();
            }
        }
    }

    public void read() {
        try {
            File friendFile = XrayManager.getFile("xrays.txt");
            if (!friendFile.exists()) {
                this.add(Blocks.DIAMOND_ORE.getTranslationKey());
                this.add(Blocks.DEEPSLATE_DIAMOND_ORE.getTranslationKey());
                this.add(Blocks.GOLD_ORE.getTranslationKey());
                this.add(Blocks.NETHER_GOLD_ORE.getTranslationKey());
                this.add(Blocks.IRON_ORE.getTranslationKey());
                this.add(Blocks.DEEPSLATE_IRON_ORE.getTranslationKey());
                this.add(Blocks.REDSTONE_ORE.getTranslationKey());
                this.add(Blocks.EMERALD_ORE.getTranslationKey());
                this.add(Blocks.DEEPSLATE_EMERALD_ORE.getTranslationKey());
                this.add(Blocks.DEEPSLATE_REDSTONE_ORE.getTranslationKey());
                this.add(Blocks.COAL_ORE.getTranslationKey());
                this.add(Blocks.DEEPSLATE_COAL_ORE.getTranslationKey());
                this.add(Blocks.ANCIENT_DEBRIS.getTranslationKey());
                this.add(Blocks.NETHER_QUARTZ_ORE.getTranslationKey());
                this.add(Blocks.LAPIS_ORE.getTranslationKey());
                this.add(Blocks.DEEPSLATE_LAPIS_ORE.getTranslationKey());
                return;
            }
            List<String> list = IOUtils.readLines((InputStream)new FileInputStream(friendFile), (Charset)StandardCharsets.UTF_8);
            for (String s : list) {
                this.add(s);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            File friendFile = XrayManager.getFile("xrays.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : this.list) {
                printwriter.println(str);
            }
            printwriter.close();
        }
        catch (Exception exception) {
            System.out.println("[Vitality] Failed to save xrays");
        }
    }
}

