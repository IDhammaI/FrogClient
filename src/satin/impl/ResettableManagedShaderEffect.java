/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.PostEffectProcessor
 *  net.minecraft.resource.ResourceFactory
 *  net.minecraft.util.Identifier
 */
package satin.impl;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.gzsakura_miitong.asm.accessors.IPostEffectProcessor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import satin.api.managed.ManagedFramebuffer;
import satin.api.managed.ManagedShaderEffect;
import satin.api.managed.uniform.SamplerUniformV2;
import satin.impl.FramebufferWrapper;
import satin.impl.ManagedSamplerUniformV2;
import satin.impl.ManagedUniformBase;
import satin.impl.ResettableManagedShaderBase;

public final class ResettableManagedShaderEffect
extends ResettableManagedShaderBase<PostEffectProcessor>
implements ManagedShaderEffect {
    private final Consumer<ManagedShaderEffect> initCallback;
    private final Map<String, FramebufferWrapper> managedTargets;
    private final Map<String, ManagedSamplerUniformV2> managedSamplers = new HashMap<String, ManagedSamplerUniformV2>();

    public ResettableManagedShaderEffect(Identifier location, Consumer<ManagedShaderEffect> initCallback) {
        super(location);
        this.initCallback = initCallback;
        this.managedTargets = new HashMap<String, FramebufferWrapper>();
    }

    @Override
    public PostEffectProcessor getShaderEffect() {
        return this.getShaderOrLog();
    }

    @Override
    protected PostEffectProcessor parseShader(ResourceFactory resourceFactory, MinecraftClient mc, Identifier location) throws IOException {
        return new PostEffectProcessor(mc.getTextureManager(), (ResourceFactory)mc.getResourceManager(), mc.getFramebuffer(), location);
    }

    @Override
    public void setup(int windowWidth, int windowHeight) {
        Preconditions.checkNotNull((Object)((PostEffectProcessor)this.shader));
        ((PostEffectProcessor)this.shader).setupDimensions(windowWidth, windowHeight);
        for (ManagedUniformBase uniform : this.getManagedUniforms()) {
            this.setupUniform(uniform, (PostEffectProcessor)this.shader);
        }
        for (FramebufferWrapper buf : this.managedTargets.values()) {
            buf.findTarget((PostEffectProcessor)this.shader);
        }
        this.initCallback.accept(this);
    }

    @Override
    public void render(float tickDelta) {
        PostEffectProcessor sg = this.getShaderEffect();
        if (sg != null) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            sg.render(tickDelta);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.disableBlend();
            RenderSystem.blendFunc((int)770, (int)771);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public ManagedFramebuffer getTarget(String name) {
        return this.managedTargets.computeIfAbsent(name, n -> {
            FramebufferWrapper ret = new FramebufferWrapper((String)n);
            if (this.shader != null) {
                ret.findTarget((PostEffectProcessor)this.shader);
            }
            return ret;
        });
    }

    @Override
    public void setUniformValue(String uniformName, int value) {
        this.findUniform1i(uniformName).set(value);
    }

    @Override
    public void setUniformValue(String uniformName, float value) {
        this.findUniform1f(uniformName).set(value);
    }

    @Override
    public void setUniformValue(String uniformName, float value0, float value1) {
        this.findUniform2f(uniformName).set(value0, value1);
    }

    @Override
    public void setUniformValue(String uniformName, float value0, float value1, float value2) {
        this.findUniform3f(uniformName).set(value0, value1, value2);
    }

    @Override
    public void setUniformValue(String uniformName, float value0, float value1, float value2, float value3) {
        this.findUniform4f(uniformName).set(value0, value1, value2, value3);
    }

    @Override
    public SamplerUniformV2 findSampler(String samplerName) {
        return this.manageUniform(this.managedSamplers, ManagedSamplerUniformV2::new, samplerName, "sampler");
    }

    @Override
    protected boolean setupUniform(ManagedUniformBase uniform, PostEffectProcessor shader) {
        return uniform.findUniformTargets(((IPostEffectProcessor)shader).getPasses());
    }

    @Override
    protected void logInitError(IOException e) {
        LogUtils.getLogger().error("Could not create screen shader {}", (Object)this.getLocation(), (Object)e);
    }

    private PostEffectProcessor getShaderOrLog() {
        if (!this.isInitialized() && !this.isErrored()) {
            this.initializeOrLog((ResourceFactory)MinecraftClient.getInstance().getResourceManager());
        }
        return (PostEffectProcessor)this.shader;
    }
}

