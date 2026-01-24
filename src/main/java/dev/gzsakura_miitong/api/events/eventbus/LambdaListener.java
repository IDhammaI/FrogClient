/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.eventbus;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class LambdaListener
implements IListener {
    private static Method privateLookupInMethod;
    private final Class<?> target;
    private final boolean isStatic;
    private final int priority;
    private Consumer<Object> executor;

    public LambdaListener(Factory factory, Class<?> klass, Object object, Method method) {
        this.target = method.getParameters()[0].getType();
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.priority = method.getAnnotation(EventListener.class).priority();
        try {
            MethodType invokedType;
            MethodHandle methodHandle;
            String name = method.getName();
            MethodHandles.Lookup lookup = factory.create(privateLookupInMethod, klass);
            MethodType methodType = MethodType.methodType(Void.TYPE, method.getParameters()[0].getType());
            if (this.isStatic) {
                methodHandle = lookup.findStatic(klass, name, methodType);
                invokedType = MethodType.methodType(Consumer.class);
            } else {
                methodHandle = lookup.findVirtual(klass, name, methodType);
                invokedType = MethodType.methodType(Consumer.class, klass);
            }
            MethodHandle lambdaFactory = LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(Void.TYPE, Object.class), methodHandle, methodType).getTarget();
            this.executor = (Consumer<Object>) (this.isStatic ? lambdaFactory.invoke() : lambdaFactory.invoke(object));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void call(Object event) {
        this.executor.accept(event);
    }

    @Override
    public Class<?> getTarget() {
        return this.target;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean isStatic() {
        return this.isStatic;
    }

    static {
        try {
            privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static interface Factory {
        public MethodHandles.Lookup create(Method var1, Class<?> var2) throws InvocationTargetException, IllegalAccessException;
    }
}

