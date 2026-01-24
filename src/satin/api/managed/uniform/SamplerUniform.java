/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.texture.AbstractTexture
 */
package satin.api.managed.uniform;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.AbstractTexture;

public interface SamplerUniform {
    public void set(AbstractTexture var1);

    public void set(Framebuffer var1);

    public void set(int var1);
}

