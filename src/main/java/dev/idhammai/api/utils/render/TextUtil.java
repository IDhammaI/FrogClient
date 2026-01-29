/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.lwjgl.opengl.GL11
 */
package dev.idhammai.api.utils.render;

import dev.idhammai.api.utils.Wrapper;
import dev.idhammai.core.impl.FontManager;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class TextUtil
implements Wrapper {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static float getWidth(String s) {
        return TextUtil.mc.textRenderer.getWidth(s);
    }

    public static float getHeight() {
        Objects.requireNonNull(TextUtil.mc.textRenderer);
        return 9.0f;
    }

    public static void drawStringWithScale(DrawContext drawContext, String text, float x, float y, int color, float scale) {
        MatrixStack matrixStack = drawContext.getMatrices();
        if (scale != 1.0f) {
            matrixStack.push();
            matrixStack.scale(scale, scale, 1.0f);
            if (scale > 1.0f) {
                matrixStack.translate(-x / scale, -y / scale, 0.0f);
            } else {
                matrixStack.translate(x / scale / 2.0f, y / scale / 2.0f, 0.0f);
            }
        }
        TextUtil.drawString(drawContext, text, x, y, color);
        matrixStack.pop();
    }

    public static void drawStringScale(DrawContext drawContext, String text, float x, float y, int color, float scale, boolean shadow) {
        MatrixStack matrixStack = drawContext.getMatrices();
        if (scale != 1.0f) {
            matrixStack.push();
            matrixStack.scale(scale, scale, 1.0f);
            if (scale > 1.0f) {
                matrixStack.translate(-x / scale, -y / scale, 0.0f);
            } else {
                matrixStack.translate(x / scale / 2.0f, y / scale / 2.0f, 0.0f);
            }
        }
        drawContext.drawText(TextUtil.mc.textRenderer, text, (int)x, (int)y, color, shadow);
        matrixStack.pop();
    }

    public static void drawString(DrawContext drawContext, String text, double x, double y, int color) {
        TextUtil.drawString(drawContext, text, x, y, color, false);
    }

    public static void drawString(DrawContext drawContext, String text, double x, double y, int color, boolean customFont) {
        TextUtil.drawString(drawContext, text, x, y, color, customFont, true);
    }

    public static void drawString(DrawContext drawContext, String text, double x, double y, int color, boolean customFont, boolean shadow) {
        if (customFont) {
            FontManager.ui.drawString(drawContext.getMatrices(), text, (double)((int)x), (double)((int)y), color, shadow);
        } else {
            drawContext.drawText(TextUtil.mc.textRenderer, text, (int)x, (int)y, color, shadow);
        }
    }

    public static void drawStringPulse(DrawContext drawContext, String text, double x, double y, Color startColor, Color endColor, double speed, int counter, boolean customFont) {
        char[] stringToCharArray = text.toCharArray();
        int index = 0;
        boolean color = false;
        String s = null;
        for (char c : stringToCharArray) {
            if (c == '\u00a7') {
                color = true;
                continue;
            }
            if (color) {
                s = c == 'r' ? null : "\u00a7" + c;
                color = false;
                continue;
            }
            ++index;
            if (s != null) {
                TextUtil.drawString(drawContext, s + c, x, y, startColor.getRGB(), customFont);
            } else {
                TextUtil.drawString(drawContext, String.valueOf(c), x, y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB(), customFont);
            }
            x += customFont ? (double)FontManager.ui.getWidth(String.valueOf(c)) : (double)TextUtil.mc.textRenderer.getWidth(String.valueOf(c));
        }
    }

    public static void drawStringPulse(DrawContext drawContext, String text, double x, double y, Color startColor, Color endColor, double speed, int counter, boolean customFont, boolean shadow) {
        char[] stringToCharArray = text.toCharArray();
        int index = 0;
        boolean color = false;
        String s = null;
        for (char c : stringToCharArray) {
            if (c == '\u00a7') {
                color = true;
                continue;
            }
            if (color) {
                s = c == 'r' ? null : "\u00a7" + c;
                color = false;
                continue;
            }
            ++index;
            if (s != null) {
                TextUtil.drawString(drawContext, s + c, x, y, startColor.getRGB(), customFont, shadow);
            } else {
                TextUtil.drawString(drawContext, String.valueOf(c), x, y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB(), customFont, shadow);
            }
            x += customFont ? (double)FontManager.ui.getWidth(String.valueOf(c)) : (double)TextUtil.mc.textRenderer.getWidth(String.valueOf(c));
        }
    }

    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        Camera camera = TextUtil.mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv((int)2978, (int[])viewport);
        Vector3f target = new Vector3f();
        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;
        Vector4f transformedCoordinates = new Vector4f((float)deltaX, (float)deltaY, (float)deltaZ, 1.0f).mul((Matrix4fc)lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f((Matrix4fc)lastProjMat);
        Matrix4f matrixModel = new Matrix4f((Matrix4fc)lastModMat);
        matrixProj.mul((Matrix4fc)matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d((double)target.x / mc.getWindow().getScaleFactor(), (double)((float)displayHeight - target.y) / mc.getWindow().getScaleFactor(), (double)target.z);
    }
}

