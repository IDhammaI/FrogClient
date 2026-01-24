/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  net.minecraft.item.Items
 *  org.apache.commons.io.IOUtils
 */
package dev.gzsakura_miitong.core.impl;

import dev.gzsakura_miitong.core.Manager;
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

public class TradeManager
extends Manager {
    private final ArrayList<String> list = new ArrayList();

    public TradeManager() {
        this.read();
    }

    public ArrayList<String> getList() {
        return this.list;
    }

    public void clear() {
        this.list.clear();
    }

    public boolean inWhitelist(String name) {
        return this.list.contains(name) || this.list.contains(name.replace("block.minecraft.", "").replace("item.minecraft.", ""));
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
            File friendFile = TradeManager.getFile("trades.txt");
            if (!friendFile.exists()) {
                this.add(Items.ENCHANTED_BOOK.getTranslationKey());
                this.add(Items.DIAMOND_BLOCK.getTranslationKey());
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
            File friendFile = TradeManager.getFile("trades.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : this.list) {
                printwriter.println(str);
            }
            printwriter.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

