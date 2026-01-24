/*
 * Decompiled with CFR 0.152.
 */
package satin.api.managed.uniform;

import satin.api.managed.uniform.SamplerUniform;
import satin.api.managed.uniform.Uniform1f;
import satin.api.managed.uniform.Uniform1i;
import satin.api.managed.uniform.Uniform2f;
import satin.api.managed.uniform.Uniform2i;
import satin.api.managed.uniform.Uniform3f;
import satin.api.managed.uniform.Uniform3i;
import satin.api.managed.uniform.Uniform4f;
import satin.api.managed.uniform.Uniform4i;
import satin.api.managed.uniform.UniformMat4;

public interface UniformFinder {
    public Uniform1i findUniform1i(String var1);

    public Uniform2i findUniform2i(String var1);

    public Uniform3i findUniform3i(String var1);

    public Uniform4i findUniform4i(String var1);

    public Uniform1f findUniform1f(String var1);

    public Uniform2f findUniform2f(String var1);

    public Uniform3f findUniform3f(String var1);

    public Uniform4f findUniform4f(String var1);

    public UniformMat4 findUniformMat4(String var1);

    public SamplerUniform findSampler(String var1);
}

