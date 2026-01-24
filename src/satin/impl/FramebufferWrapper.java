/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.gl.PostEffectProcessor
 *  net.minecraft.client.util.Window
 */
package satin.impl;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.Window;
import satin.api.managed.ManagedFramebuffer;

public final class FramebufferWrapper
implements ManagedFramebuffer {
    private final String name;
    private Framebuffer wrapped;

    FramebufferWrapper(String name) {
        this.name = name;
    }

    void findTarget(PostEffectProcessor shaderEffect) {
        if (shaderEffect == null) {
            this.wrapped = null;
        } else {
            this.wrapped = shaderEffect.getSecondaryTarget(this.name);
            if (this.wrapped == null) {
                LogUtils.getLogger().warn("No target framebuffer found with name {} in shader {}", (Object)this.name, (Object)shaderEffect.getName());
            }
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public Framebuffer getFramebuffer() {
        return this.wrapped;
    }

    @Override
    public void beginWrite(boolean updateViewport) {
        if (this.wrapped != null) {
            this.wrapped.beginWrite(updateViewport);
        }
    }

    @Override
    public void draw() {
        Window window = MinecraftClient.getInstance().getWindow();
        this.draw(window.getFramebufferWidth(), window.getFramebufferHeight(), true);
    }

    @Override
    public void draw(int width, int height, boolean disableBlend) {
        if (this.wrapped != null) {
            this.wrapped.draw(width, height, disableBlend);
        }
    }

    @Override
    public void clear() {
        this.clear(MinecraftClient.IS_SYSTEM_MAC);
    }

    @Override
    public void clear(boolean swallowErrors) {
        if (this.wrapped != null) {
            this.wrapped.clear(swallowErrors);
        }
    }
}

