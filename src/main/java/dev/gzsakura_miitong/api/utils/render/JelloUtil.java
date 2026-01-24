/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 */
package dev.gzsakura_miitong.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gzsakura_miitong.api.utils.Wrapper;

import java.awt.Color;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class JelloUtil
implements Wrapper {
    private static float prevCircleStep;
    private static float circleStep;

    public static void drawJello(MatrixStack matrix, Entity target, Color color) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * mc.getRenderTickCounter().getTickDelta(true);
        double prevSinAnim = JelloUtil.absSinAnimation(cs - (double)0.45f);
        double sinAnim = JelloUtil.absSinAnimation(cs);
        double x = target.prevX + (target.getX() - target.prevX) * (double)mc.getRenderTickCounter().getTickDelta(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * (double)mc.getRenderTickCounter().getTickDelta(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * (double)target.getHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * (double)mc.getRenderTickCounter().getTickDelta(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY + (target.getY() - target.prevY) * (double)mc.getRenderTickCounter().getTickDelta(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * (double)target.getHeight();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        for (int i = 0; i <= 30; ++i) {
            float cos = (float)(x + Math.cos((double)i * 6.28 / 30.0) * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5);
            float sin = (float)(z + Math.sin((double)i * 6.28 / 30.0) * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float)nextY, sin).color(color.getRGB());
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float)y, sin).color(ColorUtil.injectAlpha(color, 0).getRGB());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrix.pop();
    }

    public static void updateJello() {
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }

    private static double absSinAnimation(double input) {
        return Math.abs(1.0 + Math.sin(input)) / 2.0;
    }
}

