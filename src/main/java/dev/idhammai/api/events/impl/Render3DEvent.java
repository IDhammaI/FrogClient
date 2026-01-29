/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.Box
 */
package dev.idhammai.api.events.impl;

import dev.idhammai.api.utils.render.Render3DUtil;
import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();
    public MatrixStack matrixStack;
    public float tickDelta;

    public static Render3DEvent get(MatrixStack matrixStack, float tickDelta) {
        Render3DEvent.INSTANCE.matrixStack = matrixStack;
        Render3DEvent.INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }

    public void drawBox(Box box, Color color) {
        Render3DUtil.drawBox(this.matrixStack, box, color);
    }

    public void drawFill(Box box, Color color) {
        Render3DUtil.drawFill(this.matrixStack, box, color);
    }
}

