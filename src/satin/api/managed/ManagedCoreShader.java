/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.ShaderProgram
 */
package satin.api.managed;

import net.minecraft.client.gl.ShaderProgram;
import satin.api.managed.uniform.UniformFinder;

public interface ManagedCoreShader
extends UniformFinder {
    public ShaderProgram getProgram();

    public void release();
}

