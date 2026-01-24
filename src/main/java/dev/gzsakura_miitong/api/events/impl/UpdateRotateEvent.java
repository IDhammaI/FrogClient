/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.events.impl;

public class UpdateRotateEvent {
    private static final UpdateRotateEvent instance = new UpdateRotateEvent();
    private float yaw;
    private float pitch;
    private boolean modified;

    private UpdateRotateEvent() {
    }

    public static UpdateRotateEvent get(float yaw, float pitch) {
        UpdateRotateEvent.instance.yaw = yaw;
        UpdateRotateEvent.instance.pitch = pitch;
        UpdateRotateEvent.instance.modified = false;
        return instance;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.modified = true;
        this.setYawWithoutSync(yaw);
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.modified = true;
        this.setPitchWithoutSync(pitch);
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setRotation(float yaw, float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);
    }

    public void setYawWithoutSync(float yaw) {
        this.yaw = yaw;
    }

    public void setPitchWithoutSync(float pitch) {
        this.pitch = pitch;
    }
}

