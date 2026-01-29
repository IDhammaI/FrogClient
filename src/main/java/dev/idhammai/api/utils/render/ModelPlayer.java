/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.model.ModelPart$Cuboid
 *  net.minecraft.client.model.ModelPart$Quad
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.render.entity.EntityRendererFactory$Context
 *  net.minecraft.client.render.entity.model.EntityModelLayers
 *  net.minecraft.client.render.entity.model.PlayerEntityModel
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package dev.idhammai.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.api.utils.math.MathUtil;
import dev.idhammai.asm.accessors.ILivingEntity;
import dev.idhammai.asm.accessors.IModelPart;
import dev.idhammai.asm.accessors.IModelPartCuboid;
import dev.idhammai.asm.accessors.IPlayerEntityModel;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ModelPlayer
extends PlayerEntityModel<PlayerEntity> {
    public final PlayerEntity player;
    private static final Vector4f pos1 = new Vector4f();
    private static final Vector4f pos2 = new Vector4f();
    private static final Vector4f pos3 = new Vector4f();
    private static final Vector4f pos4 = new Vector4f();

    public ModelPlayer(PlayerEntity player) {
        super(new EntityRendererFactory.Context(Wrapper.mc.getEntityRenderDispatcher(), Wrapper.mc.getItemRenderer(), Wrapper.mc.getBlockRenderManager(), Wrapper.mc.getEntityRenderDispatcher().getHeldItemRenderer(), Wrapper.mc.getResourceManager(), Wrapper.mc.getEntityModelLoader(), Wrapper.mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
        this.player = player;
        ((IPlayerEntityModel)this).getLeftPants().visible = false;
        ((IPlayerEntityModel)this).getRightPants().visible = false;
        ((IPlayerEntityModel)this).getLeftSleeve().visible = false;
        ((IPlayerEntityModel)this).getRightSleeve().visible = false;
        ((IPlayerEntityModel)this).getJacket().visible = false;
        this.hat.visible = false;
        this.getHead().scale(new Vector3f(-0.05f, -0.05f, -0.05f));
        this.sneaking = player.isInSneakingPose();
    }

    public void render(MatrixStack matrices, ColorSetting fill, ColorSetting line) {
        this.render(matrices, fill, line, 1.0, 0.0, 1.0, 0.0, false, false);
    }

    public void render(MatrixStack matrices, ColorSetting fill, ColorSetting line, double alpha, double yOffset, double scale, double yaw, boolean noLimb, boolean forceSneaking) {
        if (forceSneaking) {
            this.sneaking = true;
        }
        double x = this.player.getX() - Wrapper.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = this.player.getY() - Wrapper.mc.getEntityRenderDispatcher().camera.getPos().getY() + yOffset;
        double z = this.player.getZ() - Wrapper.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        matrices.push();
        matrices.translate((float)x, (float)y, (float)z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180.0f - this.player.bodyYaw + (float)yaw)));
        this.handSwingProgress = this.player.getHandSwingProgress(1.0f);
        float j = ((ILivingEntity)this.player).getLeaningPitch();
        if (this.player.isFallFlying()) {
            float k = this.player.getPitch();
            float l = (float)this.player.getFallFlyingTicks() + this.player.bodyYaw + (float)yaw;
            float m = MathHelper.clamp((float)(l * l / 100.0f), (float)0.0f, (float)1.0f);
            if (!this.player.isUsingRiptide()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0f - k)));
            }
            Vec3d vec3d = this.player.getRotationVec(1.0f);
            Vec3d vec3d2 = this.player.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float)(Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0f) {
            float k = this.player.getPitch();
            float l = this.player.isTouchingWater() ? -90.0f - k : -90.0f;
            float m = MathHelper.lerp((float)j, (float)0.0f, (float)l);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            if (this.player.isInSwimmingPose()) {
                matrices.translate(0.0f, -1.0f, 0.3f);
            }
        }
        matrices.scale(-1.0f, -1.0f, 1.0f);
        matrices.translate(0.0f, -1.401f, 0.0f);
        matrices.scale((float)scale * 0.93f, (float)scale * 0.93f, (float)scale * 0.93f);
        this.animateModel(this.player, noLimb ? 0.0f : this.player.limbAnimator.getPos(), noLimb ? 0.0f : this.player.limbAnimator.getSpeed(), Wrapper.mc.getRenderTickCounter().getTickDelta(true));
        this.setAngles(this.player, noLimb ? 0.0f : this.player.limbAnimator.getPos(), noLimb ? 0.0f : this.player.limbAnimator.getSpeed(), this.player.age, this.player.headYaw - this.player.bodyYaw, this.player.getPitch());
        this.riding = this.player.hasVehicle();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        this.getHeadParts().forEach(modelPart -> ModelPlayer.render(matrices, modelPart, fill, line, alpha, false));
        this.getBodyParts().forEach(modelPart -> ModelPlayer.render(matrices, modelPart, fill, line, alpha, false));
        matrices.pop();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static void render(MatrixStack matrices, ModelPart part, ColorSetting fill, ColorSetting line, double alpha, boolean texture) {
        if (!part.visible || ((IModelPart)(Object)part).getCuboids().isEmpty() && ((IModelPart)(Object)part).getChildren().isEmpty()) {
            return;
        }
        matrices.push();
        part.rotate(matrices);
        for (ModelPart.Cuboid cuboid : ((IModelPart)(Object)part).getCuboids()) {
            ModelPlayer.render(matrices, cuboid, fill, line, alpha, texture);
        }
        for (ModelPart child : ((IModelPart)(Object)part).getChildren().values()) {
            ModelPlayer.render(matrices, child, fill, line, alpha, texture);
        }
        matrices.pop();
    }

    public static void render(MatrixStack matrices, ModelPart.Cuboid cuboid, ColorSetting fill, ColorSetting line, double alpha, boolean texture) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (ModelPart.Quad quad : ((IModelPartCuboid)(Object)cuboid).getSides()) {
            BufferBuilder buffer;
            float elementCodec;
            float g;
            float r;
            float keyCodec;
            Color color;
            pos1.set(quad.vertices[0].pos.x / 16.0f, quad.vertices[0].pos.y / 16.0f, quad.vertices[0].pos.z / 16.0f, 1.0f);
            pos1.mul((Matrix4fc)matrix);
            pos2.set(quad.vertices[1].pos.x / 16.0f, quad.vertices[1].pos.y / 16.0f, quad.vertices[1].pos.z / 16.0f, 1.0f);
            pos2.mul((Matrix4fc)matrix);
            pos3.set(quad.vertices[2].pos.x / 16.0f, quad.vertices[2].pos.y / 16.0f, quad.vertices[2].pos.z / 16.0f, 1.0f);
            pos3.mul((Matrix4fc)matrix);
            pos4.set(quad.vertices[3].pos.x / 16.0f, quad.vertices[3].pos.y / 16.0f, quad.vertices[3].pos.z / 16.0f, 1.0f);
            pos4.mul((Matrix4fc)matrix);
            if (fill.booleanValue) {
                color = fill.getValue();
                keyCodec = (float)((double)((float)color.getAlpha() / 255.0f) * alpha);
                r = (float)color.getRed() / 255.0f;
                g = (float)color.getGreen() / 255.0f;
                elementCodec = (float)color.getBlue() / 255.0f;
                buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, texture ? VertexFormats.POSITION_TEXTURE_COLOR : VertexFormats.POSITION_COLOR);
                buffer.vertex(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos4.x, ModelPlayer.pos4.y, ModelPlayer.pos4.z).texture(quad.vertices[3].u, quad.vertices[3].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, elementCodec, keyCodec);
                buffer.vertex(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, elementCodec, keyCodec);
                Render3DUtil.endBuilding(buffer);
            }
            if (!line.booleanValue) continue;
            color = line.getValue();
            keyCodec = (float)((double)((float)color.getAlpha() / 255.0f) * alpha);
            r = (float)color.getRed() / 255.0f;
            g = (float)color.getGreen() / 255.0f;
            elementCodec = (float)color.getBlue() / 255.0f;
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, texture ? VertexFormats.POSITION_TEXTURE_COLOR : VertexFormats.POSITION_COLOR);
            buffer.vertex(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos4.x, ModelPlayer.pos4.y, ModelPlayer.pos4.z).texture(quad.vertices[3].u, quad.vertices[3].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos4.x, ModelPlayer.pos4.y, ModelPlayer.pos4.z).texture(quad.vertices[3].u, quad.vertices[3].v).color(r, g, elementCodec, keyCodec);
            buffer.vertex(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, elementCodec, keyCodec);
            Render3DUtil.endBuilding(buffer);
        }
    }
}

