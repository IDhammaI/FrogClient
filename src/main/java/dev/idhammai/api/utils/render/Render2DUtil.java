/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  org.joml.Matrix4f
 */
package dev.idhammai.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.idhammai.api.utils.Wrapper;

import java.awt.Color;
import java.util.function.DoubleFunction;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class Render2DUtil
implements Wrapper {
    public static void rect(MatrixStack stack, float x1, float y1, float x2, float y2, int color) {
        Render2DUtil.rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void arrow(MatrixStack matrixStack, float x, float y, Color color) {
        Render2DUtil.drawRectWithOutline(matrixStack, x - 1.0f, y - 1.0f, 2.0f, 2.0f, color, Color.BLACK);
    }

    public static void rectFilled(MatrixStack matrix, float x1, float y1, float x2, float y2, int color) {
        float i;
        float f = (float)(color >> 24 & 0xFF) / 255.0f;
        float g = (float)(color >> 16 & 0xFF) / 255.0f;
        float h = (float)(color >> 8 & 0xFF) / 255.0f;
        float j = (float)(color & 0xFF) / 255.0f;
        if ((double)f <= 0.01) {
            return;
        }
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x1, y2, 0.0f).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x2, y2, 0.0f).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x2, y1, 0.0f).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x1, y1, 0.0f).color(g, h, j, f);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(endColor.getRGB());
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(endColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, int startColor, int endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(endColor);
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(endColor);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void verticalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(endColor.getRGB());
        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(endColor.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void verticalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, int startColor, int endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(endColor);
        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(endColor);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawLine(MatrixStack matrices, float x, float y, float x1, float y1, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(color);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawRectWithOutline(MatrixStack matrices, float x, float y, float width, float height, Color c, Color c2) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0.0f).color(c.getRGB());
        buffer.vertex(matrix, x + width, y + height, 0.0f).color(c.getRGB());
        buffer.vertex(matrix, x + width, y, 0.0f).color(c.getRGB());
        buffer.vertex(matrix, x, y, 0.0f).color(c.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0.0f).color(c2.getRGB());
        buffer.vertex(matrix, x + width, y + height, 0.0f).color(c2.getRGB());
        buffer.vertex(matrix, x + width, y, 0.0f).color(c2.getRGB());
        buffer.vertex(matrix, x, y, 0.0f).color(c2.getRGB());
        buffer.vertex(matrix, x, y + height, 0.0f).color(c2.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0f).color(c);
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0f).color(c);
        bufferBuilder.vertex(matrix, x + width, y, 0.0f).color(c);
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(c);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        Render2DUtil.drawRect(matrices, x, y, width, height, c.getRGB());
    }

    public static void drawRect(DrawContext drawContext, float x, float y, float width, float height, Color c) {
        Render2DUtil.drawRect(drawContext.getMatrices(), x, y, width, height, c);
    }

    public static void drawSegmentedRect(MatrixStack matrices, float x, float y, float width, float height, float segment, DoubleFunction<Integer> colorAtY) {
        if (width <= 0.0f || height <= 0.0f || segment <= 0.0f) {
            return;
        }
        float y1 = y;
        float y2 = y + height;
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);
        for (float yy = top; yy < bottom; yy += segment) {
            float yy2 = Math.min(yy + segment, bottom);
            int c = colorAtY.apply((double)yy);
            Render2DUtil.drawRect(matrices, x, yy, width, yy2 - yy, c);
        }
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static void drawGlow(MatrixStack matrices, float x, float y, float width, float height, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int startColor = ColorUtil.injectAlpha(color, 20);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float centerX = x + halfWidth;
        float centerY = y + halfHeight;
        float x2 = x + width;
        float y2 = y + height;
        bufferBuilder.vertex(matrix, centerX, centerY, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x, centerY, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, centerX, y, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, centerX, centerY, 0.0f).color(color);
        bufferBuilder.vertex(matrix, centerX, y, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x2, y, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x2, centerY, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, centerX, centerY, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x, centerY, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x, y2, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, centerX, y2, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, centerX, centerY, 0.0f).color(color);
        bufferBuilder.vertex(matrix, x2, centerY, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(startColor);
        bufferBuilder.vertex(matrix, centerX, y2, 0.0f).color(startColor);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public static void drawCircle(MatrixStack matrices, float cx, float cy, float r, Color c, int segments) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (int i = 0; i <= segments; ++i) {
            double keyCodec = (double)i * (Math.PI * 2) / (double)segments;
            float x = (float)((double)cx + Math.cos(keyCodec) * (double)r);
            float y = (float)((double)cy + Math.sin(keyCodec) * (double)r);
            bufferBuilder.vertex(matrix, x, y, 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawPill(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        float r = height / 2.0f;
        Render2DUtil.drawRect(matrices, x + r, y, width - 2.0f * r, height, c);
        Render2DUtil.drawCircle(matrices, x + r, y + r, r, c, 64);
        Render2DUtil.drawCircle(matrices, x + width - r, y + r, r, c, 64);
    }

    public static void drawRoundedStroke(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c, int seg) {
        double keyCodec;
        int i;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        float x1 = x + r;
        float y1 = y + r;
        float x2 = x + width - r;
        float y2 = y + height - r;
        for (i = 0; i <= seg; ++i) {
            keyCodec = -1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        for (i = 0; i <= seg; ++i) {
            keyCodec = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        for (i = 0; i <= seg; ++i) {
            keyCodec = 1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        for (i = 0; i <= seg; ++i) {
            keyCodec = Math.PI + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawRainbowRoundedStroke(MatrixStack matrices, float x, float y, float width, float height, float radius, int seg, float speed, int alpha) {
        int argb;
        int rgb;
        int i;
        int argb2;
        int rgb2;
        float hue;
        double keyCodec;
        int i2;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        float x1 = x + r;
        float y1 = y + r;
        float x2 = x + width - r;
        float y2 = y + height - r;
        double t = (double)(System.currentTimeMillis() % (long)(1000.0f / Math.max(0.001f, speed))) / (double)(1000.0f / Math.max(0.001f, speed));
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = -1.5707963267948966 + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.vertex(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).color(argb2);
        }
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = 0.0 + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.vertex(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).color(argb2);
        }
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = 1.5707963267948966 + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.vertex(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).color(argb2);
        }
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = Math.PI + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.vertex(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).color(argb2);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        BufferBuilder inner = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float rIn = Math.max(0.5f, r - 1.2f);
        for (i = 0; i <= seg; ++i) {
            double a2 = -1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            float hue2 = (float)((a2 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue2, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.vertex(matrix, (float)((double)x2 + Math.cos(a2) * (double)rIn), (float)((double)y1 + Math.sin(a2) * (double)rIn), 0.0f).color(argb);
        }
        for (i = 0; i <= seg; ++i) {
            double a3 = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            float hue3 = (float)((a3 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue3, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.vertex(matrix, (float)((double)x2 + Math.cos(a3) * (double)rIn), (float)((double)y2 + Math.sin(a3) * (double)rIn), 0.0f).color(argb);
        }
        for (i = 0; i <= seg; ++i) {
            double a4 = 1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            float hue4 = (float)((a4 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue4, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.vertex(matrix, (float)((double)x1 + Math.cos(a4) * (double)rIn), (float)((double)y2 + Math.sin(a4) * (double)rIn), 0.0f).color(argb);
        }
        for (i = 0; i <= seg; ++i) {
            double a5 = Math.PI + (double)i * 1.5707963267948966 / (double)seg;
            float hue5 = (float)((a5 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue5, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.vertex(matrix, (float)((double)x1 + Math.cos(a5) * (double)rIn), (float)((double)y1 + Math.sin(a5) * (double)rIn), 0.0f).color(argb);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)inner.end());
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c) {
        double keyCodec;
        int i;
        if (radius <= 0.0f) {
            Render2DUtil.drawRect(matrices, x, y, width, height, c);
            return;
        }
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        Render2DUtil.drawRect(matrices, x + r, y, width - 2.0f * r, height, c);
        Render2DUtil.drawRect(matrices, x, y + r, r, height - 2.0f * r, c);
        Render2DUtil.drawRect(matrices, x + width - r, y + r, r, height - 2.0f * r, c);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        int seg = 48;
        float cx = x + r;
        float cy = y + r;
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = Math.PI + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        cx = x + width - r;
        cy = y + r;
        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = 4.71238898038469 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        cx = x + width - r;
        cy = y + height - r;
        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        cx = x + r;
        cy = y + height - r;
        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = 1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawRightRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        if (radius <= 0.0f) {
            Render2DUtil.drawRect(matrices, x, y, width, height, c);
            return;
        }
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        int capR = Math.max(0, Math.min(255, (int)Math.round((double)c.getRed() * 0.85)));
        int capG = Math.max(0, Math.min(255, (int)Math.round((double)c.getGreen() * 0.85)));
        int capB = Math.max(0, Math.min(255, (int)Math.round((double)c.getBlue() * 0.85)));
        Render2DUtil.drawCircle(matrices, x + width - r, y + height / 2.0f, r, new Color(capR, capG, capB, c.getAlpha()), 64);
        Render2DUtil.drawRect(matrices, x, y, width - r, height, c);
        if (height > 2.0f * r) {
            Render2DUtil.drawRect(matrices, x + width - r, y + r, r, height - 2.0f * r, c);
        }
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        int seg = 48;
        float cx = x + width - r;
        float cy = y + r;
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (int i = 0; i <= seg; ++i) {
            double a = 4.71238898038469 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)cx + Math.cos(a) * (double)r), (float)((double)cy + Math.sin(a) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        cx = x + width - r;
        cy = y + height - r;
        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0.0f).color(c.getRGB());
        for (int i = 0; i <= seg; ++i) {
            double a = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.vertex(matrix, (float)((double)cx + Math.cos(a) * (double)r), (float)((double)cy + Math.sin(a) * (double)r), 0.0f).color(c.getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawDropShadow(MatrixStack matrices, float x, float y, float width, float height, float radius) {
    }
}

