/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Identifier
 */
package dev.gzsakura_miitong.core.impl;

import dev.gzsakura_miitong.api.utils.Wrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class BlurManager
implements Wrapper {
    public static final ManagedShaderEffect BLUR = ShaderEffectManager.getInstance().manage(Identifier.of((String)"shaders/post/blurarea.json"));

    public void applyBlur(float radius, float startX, float startY, float width, float height) {
        float factor = (float)mc.getWindow().getScaleFactor() / 2.0f;
        BLUR.setUniformValue("Radius", radius);
        BLUR.setUniformValue("BlurXY", startX * factor, (float)mc.getWindow().getHeight() / 2.0f - (startY + height) * factor);
        BLUR.setUniformValue("BlurCoord", width * factor, height * factor);
        BLUR.render(mc.getRenderTickCounter().getTickDelta(true));
    }
}

