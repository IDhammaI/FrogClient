/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface EventListener {
    public int priority() default 0;
}

