/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  net.minecraft.client.gl.JsonEffectShaderProgram
 *  net.minecraft.client.gl.PostEffectPass
 *  net.minecraft.client.gl.ShaderProgram
 */
package satin.impl;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import satin.api.managed.uniform.SamplerUniform;
import satin.impl.ManagedUniformBase;
import satin.impl.SamplerAccess;

public abstract class ManagedSamplerUniformBase
extends ManagedUniformBase
implements SamplerUniform {
    protected SamplerAccess[] targets = new SamplerAccess[0];
    protected int[] locations = new int[0];
    protected Object cachedValue;

    public ManagedSamplerUniformBase(String name) {
        super(name);
    }

    @Override
    public boolean findUniformTargets(List<PostEffectPass> shaders) {
        ArrayList<SamplerAccess> targets = new ArrayList<SamplerAccess>(shaders.size());
        IntArrayList rawTargets = new IntArrayList(shaders.size());
        for (PostEffectPass shader : shaders) {
            JsonEffectShaderProgram program = shader.getProgram();
            SamplerAccess access = (SamplerAccess)program;
            if (!access.hasSampler(this.name)) continue;
            targets.add(access);
            rawTargets.add(this.getSamplerLoc(access));
        }
        this.targets = targets.toArray(new SamplerAccess[0]);
        this.locations = rawTargets.toArray(new int[0]);
        this.syncCurrentValues();
        return this.targets.length > 0;
    }

    private int getSamplerLoc(SamplerAccess access) {
        return access.getSamplerShaderLocs().get(access.getSamplerNames().indexOf(this.name));
    }

    @Override
    public boolean findUniformTarget(ShaderProgram shader) {
        LogUtils.getLogger().warn(shader.getName());
        return this.findUniformTarget1((SamplerAccess)shader);
    }

    private boolean findUniformTarget1(SamplerAccess access) {
        if (access.hasSampler(this.name)) {
            this.targets = new SamplerAccess[]{access};
            this.locations = new int[]{this.getSamplerLoc(access)};
            this.syncCurrentValues();
            return true;
        }
        return false;
    }

    private void syncCurrentValues() {
        Object value = this.cachedValue;
        if (value != null) {
            this.cachedValue = null;
            this.set(value);
        }
    }

    protected abstract void set(Object var1);
}

