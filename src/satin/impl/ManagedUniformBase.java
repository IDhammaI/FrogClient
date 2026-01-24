/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.PostEffectPass
 *  net.minecraft.client.gl.ShaderProgram
 */
package satin.impl;

import java.util.List;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;

public abstract class ManagedUniformBase {
    protected final String name;

    public ManagedUniformBase(String name) {
        this.name = name;
    }

    public abstract boolean findUniformTargets(List<PostEffectPass> var1);

    public abstract boolean findUniformTarget(ShaderProgram var1);

    public String getName() {
        return this.name;
    }
}

