/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod.modules.impl.client;

import dev.gzsakura_miitong.mod.modules.Module;
import java.io.PrintWriter;

public class IRC
extends Module {
    public static PrintWriter printWriter;
    private static final String SERVER_HOST = "47.121.113.160";
    private static final int SERVER_PORT = 6667;
    public static volatile boolean connect;

    public IRC() {
        super("LowInputLatency", Module.Category.Client);
    }
}

