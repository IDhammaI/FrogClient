/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.gl.ShaderProgram
 *  net.minecraft.client.texture.AbstractTexture
 */
package satin.impl;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.texture.AbstractTexture;
import satin.impl.ManagedSamplerUniformBase;
import satin.impl.SamplerAccess;

public final class ManagedSamplerUniformV1
extends ManagedSamplerUniformBase {
    public ManagedSamplerUniformV1(String name) {
        super(name);
    }

    @Override
    public void set(AbstractTexture texture) {
        this.set((Object)texture);
    }

    @Override
    public void set(Framebuffer textureFbo) {
        this.set((Object)textureFbo);
    }

    @Override
    public void set(int textureName) {
        this.set((Object)textureName);
    }

    @Override
    protected void set(Object value) {
        SamplerAccess[] targets = this.targets;
        if (targets.length > 0 && this.cachedValue != value) {
            for (SamplerAccess target : targets) {
                ((ShaderProgram)target).addSampler(this.name, value);
            }
            this.cachedValue = value;
        }
    }
}

