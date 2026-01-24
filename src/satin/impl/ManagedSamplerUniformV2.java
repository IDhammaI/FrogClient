/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.gl.JsonEffectShaderProgram
 *  net.minecraft.client.texture.AbstractTexture
 */
package satin.impl;

import java.util.function.IntSupplier;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.texture.AbstractTexture;
import satin.api.managed.uniform.SamplerUniformV2;
import satin.impl.ManagedSamplerUniformBase;
import satin.impl.SamplerAccess;

public final class ManagedSamplerUniformV2
extends ManagedSamplerUniformBase
implements SamplerUniformV2 {
    public ManagedSamplerUniformV2(String name) {
        super(name);
    }

    @Override
    public void set(AbstractTexture texture) {
        this.set(() -> ((AbstractTexture)texture).getGlId());
    }

    @Override
    public void set(Framebuffer textureFbo) {
        this.set(() -> ((Framebuffer)textureFbo).getColorAttachment());
    }

    @Override
    public void set(int textureName) {
        this.set(() -> textureName);
    }

    @Override
    protected void set(Object value) {
        this.set((IntSupplier)value);
    }

    @Override
    public void set(IntSupplier value) {
        SamplerAccess[] targets = this.targets;
        if (targets.length > 0 && this.cachedValue != value) {
            for (SamplerAccess target : targets) {
                ((JsonEffectShaderProgram)target).bindSampler(this.name, value);
            }
            this.cachedValue = value;
        }
    }
}

