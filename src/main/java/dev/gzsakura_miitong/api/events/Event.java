/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events;

public class Event {
    public Stage stage;
    private boolean cancel = false;

    public Event() {
        this(Stage.Pre);
    }

    public Event(Stage stage) {
        this.stage = stage;
    }

    public void cancel() {
        this.setCancelled(true);
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isPost() {
        return this.stage == Stage.Post;
    }

    public boolean isPre() {
        return this.stage == Stage.Pre;
    }

    public static enum Stage {
        Pre,
        Post;

    }
}

