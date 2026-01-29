/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.LightmapTextureManager
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.dimension.DimensionType
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.idhammai.asm.mixins;

import dev.idhammai.mod.modules.impl.render.Ambience;
import dev.idhammai.mod.modules.impl.render.NoRender;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LightmapTextureManager.class})
public class MixinLightmapTextureManager {
    @Final
    @Shadow
    private NativeImageBackedTexture texture;
    @Final
    @Shadow
    private NativeImage image;
    @Shadow
    private boolean dirty;
    @Shadow
    private float flickerIntensity;
    @Final
    @Shadow
    private GameRenderer renderer;
    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private static void clamp(Vector3f vec) {
        vec.set(MathHelper.clamp((float)vec.x, (float)0.0f, (float)1.0f), MathHelper.clamp((float)vec.y, (float)0.0f, (float)1.0f), MathHelper.clamp((float)vec.z, (float)0.0f, (float)1.0f));
    }

    @Shadow
    public static float getBrightness(DimensionType type, int lightLevel) {
        float f = (float)lightLevel / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        return MathHelper.lerp((float)type.ambientLight(), (float)g, (float)1.0f);
    }

    @Redirect(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal=0, remap=false), require=0)
    private boolean nightVisionHook(ClientPlayerEntity instance, RegistryEntry<StatusEffect> registryEntry) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.fullBright.getValue()) {
            return true;
        }
        return instance.hasStatusEffect(registryEntry);
    }

    @Inject(method={"update"}, at={@At(value="HEAD")}, cancellable=true)
    public void updateHook(float delta, CallbackInfo ci) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.worldColor.booleanValue) {
            ci.cancel();
            if (this.dirty) {
                this.dirty = false;
                this.client.getProfiler().push("lightTex");
                ClientWorld clientWorld = this.client.world;
                if (clientWorld != null) {
                    float f = clientWorld.getSkyBrightness(1.0f);
                    float g = clientWorld.getLightningTicksLeft() > 0 ? 1.0f : f * 0.95f + 0.05f;
                    float h = ((Double)this.client.options.getDarknessEffectScale().getValue()).floatValue();
                    float i = this.getDarknessFactor(delta) * h;
                    float j = this.getDarkness((LivingEntity)this.client.player, i, delta) * h;
                    Vector3f vector3f = new Vector3f(f, f, 1.0f).lerp((Vector3fc)new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);
                    float m = this.flickerIntensity + 1.5f;
                    Vector3f vector3f2 = new Vector3f();
                    for (int n = 0; n < 16; ++n) {
                        for (int o = 0; o < 16; ++o) {
                            float p = MixinLightmapTextureManager.getBrightness(clientWorld.getDimension(), n) * g;
                            float q = MixinLightmapTextureManager.getBrightness(clientWorld.getDimension(), o) * m;
                            float s = q * ((q * 0.6f + 0.4f) * 0.6f + 0.4f);
                            float t = q * (q * q * 0.6f + 0.4f);
                            vector3f2.set(q, s, t);
                            boolean bl = clientWorld.getDimensionEffects().shouldBrightenLighting();
                            if (bl) {
                                vector3f2.lerp((Vector3fc)new Vector3f(0.99f, 1.12f, 1.0f), 0.25f);
                                MixinLightmapTextureManager.clamp(vector3f2);
                            } else {
                                Vector3f vector3f3 = new Vector3f((Vector3fc)vector3f).mul(p);
                                vector3f2.add((Vector3fc)vector3f3);
                                vector3f2.lerp((Vector3fc)new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                                if (this.renderer.getSkyDarkness(delta) > 0.0f) {
                                    float u = this.renderer.getSkyDarkness(delta);
                                    Vector3f vector3f4 = new Vector3f((Vector3fc)vector3f2).mul(0.7f, 0.6f, 0.6f);
                                    vector3f2.lerp((Vector3fc)vector3f4, u);
                                }
                            }
                            float v = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
                            if (v < 1.0f) {
                                this.image.setColor(o, n, new Color(Ambience.INSTANCE.worldColor.getValue().getBlue(), Ambience.INSTANCE.worldColor.getValue().getGreen(), Ambience.INSTANCE.worldColor.getValue().getRed(), Ambience.INSTANCE.worldColor.getValue().getAlpha()).getRGB());
                                continue;
                            }
                            if (!bl) {
                                if (j > 0.0f) {
                                    vector3f2.add(-j, -j, -j);
                                }
                                MixinLightmapTextureManager.clamp(vector3f2);
                            }
                            v = ((Double)this.client.options.getGamma().getValue()).floatValue();
                            Vector3f vector3f5 = new Vector3f(this.easeOutQuart(vector3f2.x), this.easeOutQuart(vector3f2.y), this.easeOutQuart(vector3f2.z));
                            vector3f2.lerp((Vector3fc)vector3f5, Math.max(0.0f, v - i));
                            vector3f2.lerp((Vector3fc)new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                            MixinLightmapTextureManager.clamp(vector3f2);
                            vector3f2.mul(255.0f);
                            int x = (int)vector3f2.x();
                            int y = (int)vector3f2.y();
                            int z = (int)vector3f2.z();
                            this.image.setColor(o, n, 0xFF000000 | z << 16 | y << 8 | x);
                        }
                    }
                    this.texture.upload();
                    this.client.getProfiler().pop();
                }
            }
        }
    }

    @Inject(method={"getDarknessFactor(F)F"}, at={@At(value="HEAD")}, cancellable=true)
    private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.darkness.getValue()) {
            info.setReturnValue(0.0f);
        }
    }

    @Shadow
    private float easeOutQuart(float x) {
        float f = 1.0f - x;
        return 1.0f - f * f * f * f;
    }

    @Shadow
    private float getDarknessFactor(float delta) {
        StatusEffectInstance statusEffectInstance = this.client.player.getStatusEffect(StatusEffects.DARKNESS);
        return statusEffectInstance != null ? statusEffectInstance.getFadeFactor((LivingEntity)this.client.player, delta) : 0.0f;
    }

    @Shadow
    private float getDarkness(LivingEntity entity, float factor, float delta) {
        float f = 0.45f * factor;
        return Math.max(0.0f, MathHelper.cos((float)(((float)entity.age - delta) * (float)Math.PI * 0.025f)) * f);
    }
}

