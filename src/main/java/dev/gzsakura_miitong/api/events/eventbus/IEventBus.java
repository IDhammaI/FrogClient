/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.eventbus;

public interface IEventBus {
    public void registerLambdaFactory(LambdaListener.Factory var1);

    public <T> T post(T var1);

    public <T extends ICancellable> T post(T var1);

    public void subscribe(Object var1);

    public void subscribe(Class<?> var1);

    public void subscribe(IListener var1);

    public void unsubscribe(Object var1);

    public void unsubscribe(Class<?> var1);

    public void unsubscribe(IListener var1);
}

