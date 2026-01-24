/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.Framebuffer
 */
package satin.api.managed;

import net.minecraft.client.gl.Framebuffer;

public interface ManagedFramebuffer {
    public Framebuffer getFramebuffer();

    public void beginWrite(boolean var1);

    public void draw();

    public void draw(int var1, int var2, boolean var3);

    public void clear();

    public void clear(boolean var1);
}

