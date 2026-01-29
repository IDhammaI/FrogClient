/*
 * Decompiled with CFR 0.152.
 */
package dev.idhammai.mod;

import dev.idhammai.api.utils.Wrapper;

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

