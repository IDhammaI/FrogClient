/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.GlUniform
 *  net.minecraft.client.gl.PostEffectPass
 *  net.minecraft.client.gl.ShaderProgram
 *  org.joml.Matrix4f
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package satin.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import satin.api.managed.uniform.Uniform1f;
import satin.api.managed.uniform.Uniform1i;
import satin.api.managed.uniform.Uniform2f;
import satin.api.managed.uniform.Uniform2i;
import satin.api.managed.uniform.Uniform3f;
import satin.api.managed.uniform.Uniform3i;
import satin.api.managed.uniform.Uniform4f;
import satin.api.managed.uniform.Uniform4i;
import satin.api.managed.uniform.UniformMat4;
import satin.impl.ManagedUniformBase;

public final class ManagedUniform
extends ManagedUniformBase
implements Uniform1i,
Uniform2i,
Uniform3i,
Uniform4i,
Uniform1f,
Uniform2f,
Uniform3f,
Uniform4f,
UniformMat4 {
    private static final GlUniform[] NO_TARGETS = new GlUniform[0];
    private final int count;
    private GlUniform[] targets = NO_TARGETS;
    private int i0;
    private int i1;
    private int i2;
    private int i3;
    private float f0;
    private float f1;
    private float f2;
    private float f3;
    private boolean firstUpload = true;

    public ManagedUniform(String name, int count) {
        super(name);
        this.count = count;
    }

    @Override
    public boolean findUniformTargets(List<PostEffectPass> shaders) {
        ArrayList<GlUniform> list = new ArrayList<GlUniform>();
        for (PostEffectPass shader : shaders) {
            GlUniform uniform = shader.getProgram().getUniformByName(this.name);
            if (uniform == null) continue;
            if (uniform.getCount() != this.count) {
                throw new IllegalStateException("Mismatched number of values, expected " + this.count + " but JSON definition declares " + uniform.getCount());
            }
            list.add(uniform);
        }
        if (!list.isEmpty()) {
            this.targets = list.toArray(new GlUniform[0]);
            this.syncCurrentValues();
            return true;
        }
        this.targets = NO_TARGETS;
        return false;
    }

    @Override
    public boolean findUniformTarget(ShaderProgram shader) {
        GlUniform uniform = shader.getUniform(this.name);
        if (uniform != null) {
            this.targets = new GlUniform[]{uniform};
            this.syncCurrentValues();
            return true;
        }
        this.targets = NO_TARGETS;
        return false;
    }

    private void syncCurrentValues() {
        if (!this.firstUpload) {
            for (GlUniform target : this.targets) {
                if (target.getIntData() != null) {
                    target.setForDataType(this.i0, this.i1, this.i2, this.i3);
                    continue;
                }
                assert (target.getFloatData() != null);
                target.setForDataType(this.f0, this.f1, this.f2, this.f3);
            }
        }
    }

    @Override
    public void set(int value) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.i0 != value)) {
            for (GlUniform target : targets) {
                target.set(value);
            }
            this.i0 = value;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(int value0, int value1) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.i0 != value0 || this.i1 != value1)) {
            for (GlUniform target : targets) {
                target.set(value0, value1);
            }
            this.i0 = value0;
            this.i1 = value1;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(int value0, int value1, int value2) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.i0 != value0 || this.i1 != value1 || this.i2 != value2)) {
            for (GlUniform target : targets) {
                target.set(value0, value1, value2);
            }
            this.i0 = value0;
            this.i1 = value1;
            this.i2 = value2;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(int value0, int value1, int value2, int value3) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.i0 != value0 || this.i1 != value1 || this.i2 != value2 || this.i3 != value3)) {
            for (GlUniform target : targets) {
                target.set(value0, value1, value2, value3);
            }
            this.i0 = value0;
            this.i1 = value1;
            this.i2 = value2;
            this.i3 = value3;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(float value) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.f0 != value)) {
            for (GlUniform target : targets) {
                target.set(value);
            }
            this.f0 = value;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(float value0, float value1) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.f0 != value0 || this.f1 != value1)) {
            for (GlUniform target : targets) {
                target.set(value0, value1);
            }
            this.f0 = value0;
            this.f1 = value1;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(Vector2f value) {
        this.set(value.x(), value.y());
    }

    @Override
    public void set(float value0, float value1, float value2) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.f0 != value0 || this.f1 != value1 || this.f2 != value2)) {
            for (GlUniform target : targets) {
                target.set(value0, value1, value2);
            }
            this.f0 = value0;
            this.f1 = value1;
            this.f2 = value2;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(Vector3f value) {
        this.set(value.x(), value.y(), value.z());
    }

    @Override
    public void set(float value0, float value1, float value2, float value3) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0 && (this.firstUpload || this.f0 != value0 || this.f1 != value1 || this.f2 != value2 || this.f3 != value3)) {
            for (GlUniform target : targets) {
                target.set(value0, value1, value2, value3);
            }
            this.f0 = value0;
            this.f1 = value1;
            this.f2 = value2;
            this.f3 = value3;
            this.firstUpload = false;
        }
    }

    @Override
    public void set(Vector4f value) {
        this.set(value.x(), value.y(), value.z(), value.w());
    }

    @Override
    public void set(Matrix4f value) {
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0) {
            for (GlUniform target : targets) {
                target.set(value);
            }
        }
    }

    @Override
    public void setFromArray(float[] values) {
        if (this.count != values.length) {
            throw new IllegalArgumentException("Mismatched values size, expected " + this.count + " but got " + values.length);
        }
        GlUniform[] targets = this.targets;
        int nbTargets = targets.length;
        if (nbTargets > 0) {
            for (GlUniform target : targets) {
                target.set(values);
            }
        }
    }
}

