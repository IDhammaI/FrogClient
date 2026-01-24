/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgram
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.EndCrystalEntityRenderer
 *  net.minecraft.client.render.entity.EnderDragonEntityRenderer
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.client.render.entity.EntityRendererFactory$Context
 *  net.minecraft.client.render.item.ItemRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.RotationAxis
 *  org.joml.Quaternionf
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.api.utils.render.ModelPlayer;
import dev.gzsakura_miitong.mod.modules.impl.render.Chams;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EndCrystalEntityRenderer.class})
public abstract class MixinEndCrystalEntityRenderer
extends EntityRenderer<EndCrystalEntity> {
    @Mutable
    @Final
    @Shadow
    private static RenderLayer END_CRYSTAL;
    @Shadow
    @Final
    private static Identifier TEXTURE;
    @Unique
    private static final Identifier BLANK;
    @Unique
    private static final RenderLayer END_CRYSTAL_BLANK;
    @Unique
    private static final RenderLayer END_CRYSTAL_CUSTOM;
    @Final
    @Shadow
    private static float SINE_45_DEGREES;
    @Final
    @Shadow
    private ModelPart core;
    @Final
    @Shadow
    private ModelPart frame;
    @Final
    @Shadow
    private ModelPart bottom;

    protected MixinEndCrystalEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Unique
    private float yOffset(int age, float tickDelta, Chams module) {
        float f = ((float)age + tickDelta) * module.floatValue.getValueFloat();
        float g = MathHelper.sin((float)(f * 0.2f)) / 2.0f + 0.5f;
        g = (g * g + g) * 0.4f * module.bounceHeight.getValueFloat();
        return g - 1.4f + module.floatOffset.getValueFloat();
    }

    @Inject(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void render(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams module = Chams.INSTANCE;
        if (module.customCrystal()) {
            ci.cancel();
            int age = module.spinSync.getValue() ? module.age : endCrystalEntity.endCrystalAge;
            float h = this.yOffset(age, g, module);
            float j = ((float)age + g) * 3.0f * module.spinValue.getValueFloat();
            matrixStack.push();
            if (module.custom.getValue()) {
                ShaderProgram s = RenderSystem.getShader();
                if (module.depth.getValue()) {
                    RenderSystem.enableDepthTest();
                }
                RenderSystem.enableBlend();
                if (module.chamsTexture.getValue()) {
                    RenderSystem.setShaderTexture((int)0, (Identifier)TEXTURE);
                    RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
                } else {
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                }
                matrixStack.push();
                matrixStack.scale(2.0f * module.scale.getValueFloat(), 2.0f * module.scale.getValueFloat(), 2.0f * module.scale.getValueFloat());
                matrixStack.translate(0.0f, -0.5f, 0.0f);
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
                matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
                matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
                if (module.outerFrame.booleanValue) {
                    ModelPlayer.render(matrixStack, this.frame, module.fill, module.line, 1.0, module.chamsTexture.getValue());
                }
                matrixStack.scale(0.875f, 0.875f, 0.875f);
                matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
                if (module.innerFrame.booleanValue) {
                    ModelPlayer.render(matrixStack, this.frame, module.fill, module.line, 1.0, module.chamsTexture.getValue());
                }
                matrixStack.scale(0.875f, 0.875f, 0.875f);
                matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
                if (module.core.booleanValue) {
                    ModelPlayer.render(matrixStack, this.core, module.fill, module.line, 1.0, module.chamsTexture.getValue());
                }
                matrixStack.pop();
                RenderSystem.setShader(() -> s);
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
            }
            VertexConsumer vertexConsumer = ItemRenderer.getItemGlintConsumer(vertexConsumerProvider, (RenderLayer)(module.texture.getValue() ? END_CRYSTAL_CUSTOM : END_CRYSTAL_BLANK), false, module.glint.getValue());
            matrixStack.push();
            matrixStack.scale(2.0f * module.scale.getValueFloat(), 2.0f * module.scale.getValueFloat(), 2.0f * module.scale.getValueFloat());
            matrixStack.translate(0.0f, -0.5f, 0.0f);
            int k = OverlayTexture.DEFAULT_UV;
            if (endCrystalEntity.shouldShowBottom()) {
                this.bottom.render(matrixStack, vertexConsumer, i, k);
            }
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
            matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            if (module.outerFrame.booleanValue) {
                this.frame.render(matrixStack, vertexConsumer, i, k, module.outerFrame.getValue().getRGB());
            }
            matrixStack.scale(0.875f, 0.875f, 0.875f);
            matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            if (module.innerFrame.booleanValue) {
                this.frame.render(matrixStack, vertexConsumer, i, k, module.innerFrame.getValue().getRGB());
            }
            matrixStack.scale(0.875f, 0.875f, 0.875f);
            matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            if (module.core.booleanValue) {
                this.core.render(matrixStack, vertexConsumer, i, k, module.core.getValue().getRGB());
            }
            matrixStack.pop();
            matrixStack.pop();
            BlockPos blockPos = endCrystalEntity.getBeamTarget();
            if (blockPos != null) {
                float m = (float)blockPos.getX() + 0.5f;
                float n = (float)blockPos.getY() + 0.5f;
                float o = (float)blockPos.getZ() + 0.5f;
                float p = (float)((double)m - endCrystalEntity.getX());
                float q = (float)((double)n - endCrystalEntity.getY());
                float r = (float)((double)o - endCrystalEntity.getZ());
                matrixStack.translate(p, q, r);
                EnderDragonEntityRenderer.renderCrystalBeam((float)(-p), (float)(-q + h), (float)(-r), (float)g, (int)endCrystalEntity.endCrystalAge, (MatrixStack)matrixStack, (VertexConsumerProvider)vertexConsumerProvider, (int)i);
            }
            super.render(endCrystalEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    static {
        BLANK = Identifier.of((String)"textures/blank.png");
        END_CRYSTAL_BLANK = RenderLayer.getEntityTranslucent((Identifier)BLANK);
        END_CRYSTAL_CUSTOM = RenderLayer.getEntityTranslucent((Identifier)TEXTURE);
    }
}

