/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.eventbus;

public interface IListener {
    public void call(Object var1);

    public Class<?> getTarget();

    public int getPriority();

    public boolean isStatic();
}

