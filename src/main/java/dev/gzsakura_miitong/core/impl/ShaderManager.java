/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.gl.PostEffectProcessor
 *  net.minecraft.util.Identifier
 *  org.jetbrains.annotations.NotNull
 */
package dev.gzsakura_miitong.core.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.api.interfaces.IShaderEffectHook;
import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.mod.modules.impl.render.ShaderModule;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class ShaderManager
implements Wrapper {
    static final Timer timer = new Timer();
    private static final List<RenderTask> tasks = new ArrayList<RenderTask>();
    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect PULSE_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;
    public static ManagedShaderEffect FLOW_OUTLINE;
    public static ManagedShaderEffect RAINBOW_OUTLINE;
    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect PULSE;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;
    public static ManagedShaderEffect FLOW;
    public static ManagedShaderEffect RAINBOW;
    public float time = 0.0f;
    private MyFramebuffer shaderBuffer;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        tasks.forEach(t -> this.applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        if (this.fullNullCheck()) {
            return;
        }
        RenderSystem.assertOnRenderThreadOrInit();
        Framebuffer MCBuffer = MinecraftClient.getInstance().getFramebuffer();
        if (this.shaderBuffer.textureWidth != MCBuffer.textureWidth || this.shaderBuffer.textureHeight != MCBuffer.textureHeight) {
            this.shaderBuffer.resize(MCBuffer.textureWidth, MCBuffer.textureHeight, false);
        }
        GlStateManager._glBindFramebuffer((int)36009, (int)this.shaderBuffer.fbo);
        this.shaderBuffer.beginWrite(true);
        runnable.run();
        this.shaderBuffer.endWrite();
        GlStateManager._glBindFramebuffer((int)36009, (int)MCBuffer.fbo);
        MCBuffer.beginWrite(false);
        ManagedShaderEffect shader = this.getShader(mode);
        PostEffectProcessor effect = shader.getShaderEffect();
        if (effect != null) {
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", this.shaderBuffer);
        } else {
            return;
        }
        Framebuffer outBuffer = effect.getSecondaryTarget("bufOut");
        if (outBuffer == null) return;
        this.setupShader(mode, shader);
        this.shaderBuffer.clear(false);
        MCBuffer.beginWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, (GlStateManager.SrcFactor)GlStateManager.SrcFactor.ZERO, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.disableBlend();
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode.ordinal()) {
            case 3 -> GRADIENT;
            case 2 -> SMOKE;
            case 1 -> PULSE;
            case 4 -> SNOW;
            case 5 -> FLOW;
            case 6 -> RAINBOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode.ordinal()) {
            case 3 -> GRADIENT_OUTLINE;
            case 2 -> SMOKE_OUTLINE;
            case 4 -> SNOW_OUTLINE;
            case 5 -> FLOW_OUTLINE;
            case 6 -> RAINBOW_OUTLINE;
            case 1 -> PULSE_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        ShaderModule module = ShaderModule.INSTANCE;
        Color color = module.fill.getValue();
        this.time = (float)timer.getMs() / 5.0f * module.speed.getValueFloat() * 0.004f;
        if (shader == Shader.Rainbow) {
            effect.setUniformValue("alpha2", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        } else if (shader == Shader.Gradient) {
            effect.setUniformValue("alpha2", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("rgb", (float)module.smoke1.getValue().getRed() / 255.0f, (float)module.smoke1.getValue().getGreen() / 255.0f, (float)module.smoke1.getValue().getBlue() / 255.0f);
            effect.setUniformValue("rgb1", (float)module.smoke2.getValue().getRed() / 255.0f, (float)module.smoke2.getValue().getGreen() / 255.0f, (float)module.smoke2.getValue().getBlue() / 255.0f);
            effect.setUniformValue("rgb2", (float)module.smoke3.getValue().getRed() / 255.0f, (float)module.smoke3.getValue().getGreen() / 255.0f, (float)module.smoke3.getValue().getBlue() / 255.0f);
            effect.setUniformValue("rgb3", (float)module.smoke4.getValue().getRed() / 255.0f, (float)module.smoke4.getValue().getGreen() / 255.0f, (float)module.smoke4.getValue().getBlue() / 255.0f);
            effect.setUniformValue("step", module.step.getValueFloat() * 300.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time * 300.0f);
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha1", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("first", (float)module.smoke1.getValue().getRed() / 255.0f, (float)module.smoke1.getValue().getGreen() / 255.0f, (float)module.smoke1.getValue().getBlue() / 255.0f, (float)module.smoke1.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("second", (float)module.smoke2.getValue().getRed() / 255.0f, (float)module.smoke2.getValue().getGreen() / 255.0f, (float)module.smoke2.getValue().getBlue() / 255.0f);
            effect.setUniformValue("third", (float)module.smoke3.getValue().getRed() / 255.0f, (float)module.smoke3.getValue().getGreen() / 255.0f, (float)module.smoke3.getValue().getBlue() / 255.0f);
            effect.setUniformValue("oct", (int)module.octaves.getValue());
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        } else if (shader == Shader.Solid) {
            effect.setUniformValue("mixFactor", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("minAlpha", module.alpha.getValueFloat() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        } else if (shader == Shader.Pulse) {
            effect.setUniformValue("mixFactor", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("minAlpha", module.alpha.getValueFloat() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
            Color color2 = module.pulse.getValue();
            effect.setUniformValue("color2", (float)color2.getRed() / 255.0f, (float)color2.getGreen() / 255.0f, (float)color2.getBlue() / 255.0f);
            effect.setUniformValue("time", this.time);
            effect.setUniformValue("size", module.pulseSpeed.getValueFloat());
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        } else if (shader == Shader.Snow) {
            effect.setUniformValue("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        } else if (shader == Shader.Flow) {
            effect.setUniformValue("mixFactor", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getRenderTickCounter().getTickDelta(true));
        }
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/snow.json"));
        FLOW = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/flow.json"));
        RAINBOW = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/rainbow.json"));
        PULSE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/pulse.json"));
        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/outline.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        PULSE_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/pulse.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/smoke.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/snow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        FLOW_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/flow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
        RAINBOW_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/rainbow.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).alienClient$addHook("bufIn", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffectHook)effect).alienClient$addHook("bufOut", ShaderManager.mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null || FLOW == null || RAINBOW == null || PULSE == null || PULSE_OUTLINE == null || GRADIENT_OUTLINE == null || SMOKE_OUTLINE == null || DEFAULT_OUTLINE == null || FLOW_OUTLINE == null || RAINBOW_OUTLINE == null || this.shaderBuffer == null) {
            if (mc.getFramebuffer() == null) {
                return true;
            }
            this.shaderBuffer = new MyFramebuffer(ShaderManager.mc.getFramebuffer().textureWidth, ShaderManager.mc.getFramebuffer().textureHeight);
            this.reloadShaders();
            return true;
        }
        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public static enum Shader {
        Solid,
        Pulse,
        Smoke,
        Gradient,
        Snow,
        Flow,
        Rainbow;

    }

    public static class MyFramebuffer
    extends Framebuffer {
        public MyFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            this.resize(width, height, true);
            this.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
    }
}

