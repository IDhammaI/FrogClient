/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 *  org.apache.commons.io.IOUtils
 */
package dev.idhammai.core.impl;

import dev.idhammai.core.Manager;
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
import net.minecraft.item.Items;
import org.apache.commons.io.IOUtils;

public class CleanerManager
extends Manager {
    private final ArrayList<String> list = new ArrayList();

    public CleanerManager() {
        this.read();
    }

    public ArrayList<String> getList() {
        return this.list;
    }

    public boolean inList(String name) {
        return this.list.contains(name) || this.list.contains(name.replace("block.minecraft.", "").replace("item.minecraft.", ""));
    }

    public void clear() {
        this.list.clear();
    }

    public void remove(String name) {
        name = name.replace("block.minecraft.", "").replace("item.minecraft.", "");
        this.list.remove(name);
    }

    public void add(String name) {
        if (!this.list.contains(name = name.replace("block.minecraft.", "").replace("item.minecraft.", ""))) {
            this.list.add(name);
        }
    }

    public void read() {
        try {
            File friendFile = CleanerManager.getFile("cleaner.txt");
            if (!friendFile.exists()) {
                this.add(Items.NETHERITE_SWORD.getTranslationKey());
                this.add(Items.NETHERITE_PICKAXE.getTranslationKey());
                this.add(Items.NETHERITE_HELMET.getTranslationKey());
                this.add(Items.NETHERITE_CHESTPLATE.getTranslationKey());
                this.add(Items.NETHERITE_LEGGINGS.getTranslationKey());
                this.add(Items.NETHERITE_BOOTS.getTranslationKey());
                this.add(Items.OBSIDIAN.getTranslationKey());
                this.add(Items.ENDER_CHEST.getTranslationKey());
                this.add(Items.ENDER_PEARL.getTranslationKey());
                this.add(Items.ENCHANTED_GOLDEN_APPLE.getTranslationKey());
                this.add(Items.EXPERIENCE_BOTTLE.getTranslationKey());
                this.add(Items.COBWEB.getTranslationKey());
                this.add(Items.POTION.getTranslationKey());
                this.add(Items.SPLASH_POTION.getTranslationKey());
                this.add(Items.TOTEM_OF_UNDYING.getTranslationKey());
                this.add(Items.END_CRYSTAL.getTranslationKey());
                this.add(Items.ELYTRA.getTranslationKey());
                this.add(Items.FLINT_AND_STEEL.getTranslationKey());
                this.add(Items.PISTON.getTranslationKey());
                this.add(Items.STICKY_PISTON.getTranslationKey());
                this.add(Items.REDSTONE_BLOCK.getTranslationKey());
                this.add(Items.GLOWSTONE.getTranslationKey());
                this.add(Items.RESPAWN_ANCHOR.getTranslationKey());
                this.add(Items.ANVIL.getTranslationKey());
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
            File friendFile = CleanerManager.getFile("cleaner.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : this.list) {
                printwriter.println(str);
            }
            printwriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

