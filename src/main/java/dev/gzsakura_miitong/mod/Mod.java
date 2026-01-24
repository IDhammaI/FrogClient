/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.mod;

import dev.gzsakura_miitong.api.utils.Wrapper;

public class Mod
implements Wrapper {
    private final String name;

    public Mod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

