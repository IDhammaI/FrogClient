/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.eventbus;

public interface ICancellable {
    default public void cancel() {
        this.setCancelled(true);
    }

    public boolean isCancelled();

    public void setCancelled(boolean var1);
}

