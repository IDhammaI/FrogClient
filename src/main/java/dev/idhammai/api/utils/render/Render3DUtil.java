/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.font.TextRenderer$TextLayerType
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.BufferAllocator
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.client.util.math.MatrixStack$Entry
 *  net.minecraft.entity.Entity
 *  net.minecraft.text.StringVisitable
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package dev.idhammai.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.Frog;
import dev.idhammai.api.utils.Wrapper;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Render3DUtil
implements Wrapper {
    public static void endBuilding(BufferBuilder bb) {
        BuiltBuffer builtBuffer = bb.end();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
    }

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = Render3DUtil.mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);
        return matrices;
    }

    public static void drawText3D(String text, Vec3d vec3d, Color color) {
        Render3DUtil.drawText3D(Text.of((String)text), vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 1.0, color.getRGB());
    }

    public static void drawText3D(String text, Vec3d vec3d, int color) {
        Render3DUtil.drawText3D(Text.of((String)text), vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 1.0, color);
    }

    public static void drawText3D(Text text, Vec3d vec3d, double offX, double offY, double scale, Color color) {
        Render3DUtil.drawText3D(text, vec3d.x, vec3d.y, vec3d.z, offX, offY, scale, color.getRGB());
    }

    public static void drawText3D(Text text, double x, double y, double z, double offX, double offY, double scale, int color) {
        RenderSystem.disableDepthTest();
        MatrixStack matrices = Render3DUtil.matrixFrom(x, y, z);
        Camera camera = Render3DUtil.mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        RenderSystem.enableBlend();
        matrices.translate(offX, offY, 0.0);
        matrices.scale(-0.025f * (float)scale, -0.025f * (float)scale, 1.0f);
        int halfWidth = Render3DUtil.mc.textRenderer.getWidth((StringVisitable)text) / 2;
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate((BufferAllocator)new BufferAllocator(1536));
        Render3DUtil.mc.textRenderer.draw(text.getString(), (float)(-halfWidth), 0.0f, -1, true, matrices.peek().getPositionMatrix(), (VertexConsumerProvider)immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        immediate.draw();
        Render3DUtil.mc.textRenderer.draw(text.copy(), (float)(-halfWidth), 0.0f, color, false, matrices.peek().getPositionMatrix(), (VertexConsumerProvider)immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        immediate.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static void drawFill(MatrixStack matrixStack, Box bb, Color fillColor) {
        Render3DUtil.draw3DBox(matrixStack, bb, fillColor, new Color(0, 0, 0, 0), false, true);
    }

    public static void drawBox(MatrixStack matrixStack, Box bb, Color outlineColor) {
        Render3DUtil.draw3DBox(matrixStack, bb, new Color(0, 0, 0, 0), outlineColor, true, false);
    }

    public static void drawBox(MatrixStack matrixStack, Box bb, Color outlineColor, float lineWidth) {
        Render3DUtil.draw3DBox(matrixStack, bb, new Color(0, 0, 0, 0), outlineColor, true, false, lineWidth);
    }

    public static void draw3DBox(MatrixStack matrixStack, Box box, Color fillColor, Color outlineColor) {
        Render3DUtil.draw3DBox(matrixStack, box, fillColor, outlineColor, true, true);
    }

    public static void draw3DBox(MatrixStack matrixStack, Box box, Color fillColor, Color outlineColor, boolean outline, boolean fill) {
        Render3DUtil.draw3DBox(matrixStack, box, fillColor, outlineColor, outline, fill, 1.5f);
    }

    public static void draw3DBox(MatrixStack matrixStack, Box box, Color fillColor, Color outlineColor, boolean outline, boolean fill, float lineWidth) {
        BufferBuilder bufferBuilder;
        float elementCodec;
        float g;
        float r;
        float keyCodec;
        box = box.offset(Render3DUtil.mc.gameRenderer.getCamera().getPos().negate());
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        if (outline) {
            keyCodec = (float)outlineColor.getAlpha() / 255.0f;
            r = (float)outlineColor.getRed() / 255.0f;
            g = (float)outlineColor.getGreen() / 255.0f;
            elementCodec = (float)outlineColor.getBlue() / 255.0f;
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.lineWidth((float)lineWidth);
            bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        }
        if (fill) {
            keyCodec = (float)fillColor.getAlpha() / 255.0f;
            r = (float)fillColor.getRed() / 255.0f;
            g = (float)fillColor.getGreen() / 255.0f;
            elementCodec = (float)fillColor.getBlue() / 255.0f;
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, elementCodec, keyCodec);
            bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, elementCodec, keyCodec);
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFadeFill(MatrixStack stack, Box box, Color c, Color c1) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f posMatrix = stack.peek().getPositionMatrix();
        float minX = (float)(box.minX - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float)(box.minY - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float)(box.minZ - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float)(box.maxX - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float)(box.maxY - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float)(box.maxZ - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawLine(Vec3d start, Vec3d end, Color color) {
        Render3DUtil.drawLine(start.x, start.getY(), start.z, end.getX(), end.getY(), end.getZ(), color, 1.0f);
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        RenderSystem.enableBlend();
        MatrixStack matrices = Render3DUtil.matrixFrom(x1, y1, z1);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth((float)width);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Render3DUtil.vertexLine(matrices, (VertexConsumer)buffer, 0.0, 0.0, 0.0, (float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1), color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.enableCull();
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.disableBlend();
    }

    public static void vertexLine(MatrixStack matrices, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        MatrixStack.Entry entry = matrices.peek();
        Vector3f normalVec = Render3DUtil.getNormal((float)x1, (float)y1, (float)z1, (float)x2, (float)y2, (float)z2);
        buffer.vertex(model, (float)x1, (float)y1, (float)z1).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
        buffer.vertex(model, (float)x2, (float)y2, (float)z2).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt((float)(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal));
        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static void drawTargetEsp(MatrixStack stack, @NotNull Entity target, Color color) {
        int j;
        ArrayList<Vec3d> vecs = new ArrayList<Vec3d>();
        ArrayList<Vec3d> vecs1 = new ArrayList<Vec3d>();
        ArrayList<Vec3d> vecs2 = new ArrayList<Vec3d>();
        double x = target.prevX + (target.getX() - target.prevX) * (double)Render3DUtil.getTickDelta() - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * (double)Render3DUtil.getTickDelta() - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = target.prevZ + (target.getZ() - target.prevZ) * (double)Render3DUtil.getTickDelta() - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double height = target.getHeight();
        for (int i = 0; i <= 361; ++i) {
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));
            Vec3d vec = new Vec3d((double)((float)(u * 0.5)), height, (double)((float)(v * 0.5)));
            vecs.add(vec);
            double v1 = Math.sin(Math.toRadians((i + 120) % 360));
            double u1 = Math.cos(Math.toRadians(i + 120) % 360.0);
            Vec3d vec1 = new Vec3d((double)((float)(u1 * 0.5)), height, (double)((float)(v1 * 0.5)));
            vecs1.add(vec1);
            double v2 = Math.sin(Math.toRadians((i + 240) % 360));
            double u2 = Math.cos(Math.toRadians((i + 240) % 360));
            Vec3d vec2 = new Vec3d((double)((float)(u2 * 0.5)), height, (double)((float)(v2 * 0.5)));
            vecs2.add(vec2);
            height -= (double)0.004f;
        }
        stack.push();
        stack.translate(x, y, z);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = stack.peek().getPositionMatrix();
        for (j = 0; j < vecs.size() - 1; ++j) {
            float alpha = 1.0f - ((float)j + (float)(System.currentTimeMillis() - Frog.initTime) / 5.0f) % 360.0f / 60.0f;
            bufferBuilder.vertex(matrix, (float)((Vec3d)vecs.get((int)j)).x, (float)((Vec3d)vecs.get((int)j)).y, (float)((Vec3d)vecs.get((int)j)).z).color(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
            bufferBuilder.vertex(matrix, (float)((Vec3d)vecs.get((int)(j + 1))).x, (float)((Vec3d)vecs.get((int)(j + 1))).y + 0.1f, (float)((Vec3d)vecs.get((int)(j + 1))).z).color(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
        }
        Render3DUtil.endBuilding(bufferBuilder);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (j = 0; j < vecs1.size() - 1; ++j) {
            float alpha = 1.0f - ((float)j + (float)(System.currentTimeMillis() - Frog.initTime) / 5.0f) % 360.0f / 60.0f;
            bufferBuilder.vertex(matrix, (float)((Vec3d)vecs1.get((int)j)).x, (float)((Vec3d)vecs1.get((int)j)).y, (float)((Vec3d)vecs1.get((int)j)).z).color(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
            bufferBuilder.vertex(matrix, (float)((Vec3d)vecs1.get((int)(j + 1))).x, (float)((Vec3d)vecs1.get((int)(j + 1))).y + 0.1f, (float)((Vec3d)vecs1.get((int)(j + 1))).z).color(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
        }
        Render3DUtil.endBuilding(bufferBuilder);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (j = 0; j < vecs2.size() - 1; ++j) {
            float alpha = 1.0f - ((float)j + (float)(System.currentTimeMillis() - Frog.initTime) / 5.0f) % 360.0f / 60.0f;
            bufferBuilder.vertex(matrix, (float)((Vec3d)vecs2.get((int)j)).x, (float)((Vec3d)vecs2.get((int)j)).y, (float)((Vec3d)vecs2.get((int)j)).z).color(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
            bufferBuilder.vertex(matrix, (float)((Vec3d)vecs2.get((int)(j + 1))).x, (float)((Vec3d)vecs2.get((int)(j + 1))).y + 0.1f, (float)((Vec3d)vecs2.get((int)(j + 1))).z).color(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
        }
        Render3DUtil.endBuilding(bufferBuilder);
        RenderSystem.enableCull();
        stack.translate(-x, -y, -z);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(true);
    }
}

