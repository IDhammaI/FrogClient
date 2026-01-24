/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Drawable
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.ScreenRect
 *  net.minecraft.client.gui.screen.DownloadingTerrainScreen
 *  net.minecraft.client.gui.screen.ProgressScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.util.math.MatrixStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.gzsakura_miitong.asm.mixins;

import dev.gzsakura_miitong.Vitality;
import dev.gzsakura_miitong.api.utils.render.Render2DUtil;
import dev.gzsakura_miitong.core.impl.ShaderManager;
import dev.gzsakura_miitong.mod.modules.impl.client.ClickGui;
import dev.gzsakura_miitong.mod.modules.impl.client.ClientSetting;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;

@Mixin(value={Screen.class})
public class MixinScreen {
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    protected MinecraftClient client;

    @Inject(method={"renderBackground"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderInGameBackgroundHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        if (this.client.world == null) {
            boolean isLoading;
            boolean isMainMenu = (Object)this instanceof TitleScreen;
            boolean bl = isLoading = (Object)this instanceof ProgressScreen || (Object)this instanceof DownloadingTerrainScreen;
            if (isMainMenu || isLoading) {
                float x0;
                float i;
                float w = this.width;
                float h = this.height;
                MatrixStack m = context.getMatrices();
                Render2DUtil.verticalGradient(m, 0.0f, 0.0f, w, h, new Color(40, 10, 60, 255), new Color(200, 20, 150, 255));
                long now = System.currentTimeMillis();
                float phase = (float)(now % 6000L) / 6000.0f;
                float angle = 0.523599f;
                float dx = (float)Math.tan(angle) * h;
                float base = -h;
                float spacing = 36.0f;
                float shift = phase * spacing * 4.0f;
                Color c1 = new Color(220, 60, 170, 52);
                Color c2 = new Color(160, 40, 130, 36);
                for (i = base; i < w; i += spacing) {
                    x0 = i + shift;
                    Render2DUtil.drawLine(m, x0, 0.0f, x0 + dx, h, c1.getRGB());
                }
                for (i = base + spacing / 2.0f; i < w; i += spacing) {
                    x0 = i + shift * 0.8f;
                    Render2DUtil.drawLine(m, x0, 0.0f, x0 + dx, h, c2.getRGB());
                }
                float hSpacing = 64.0f;
                Color c3 = new Color(255, 255, 255, 18);
                for (float y = 0.0f; y <= h; y += hSpacing) {
                    Render2DUtil.drawLine(m, 0.0f, y, w, y, c3.getRGB());
                }
                if (!Vitality.SHADER.fullNullCheck()) {
                    ManagedShaderEffect gradient = Vitality.SHADER.getShader(ShaderManager.Shader.Gradient);
                    gradient.setUniformValue("alpha2", 0.2f);
                    gradient.setUniformValue("rgb", 0.78f, 0.05f, 0.59f);
                    gradient.setUniformValue("rgb1", 0.56f, 0.06f, 0.68f);
                    gradient.setUniformValue("rgb2", 0.93f, 0.12f, 0.63f);
                    gradient.setUniformValue("rgb3", 0.64f, 0.0f, 0.64f);
                    gradient.setUniformValue("step", 180.0f);
                    gradient.setUniformValue("radius", 2.0f);
                    gradient.setUniformValue("quality", 1.0f);
                    gradient.setUniformValue("divider", 150.0f);
                    gradient.setUniformValue("maxSample", 10.0f);
                    gradient.setUniformValue("resolution", w, h);
                    float t = (float)(now % 100000L) / 1000.0f;
                    gradient.setUniformValue("time", t * 300.0f);
                    gradient.render(this.client.getRenderTickCounter().getTickDelta(true));
                }
                return;
            }
            this.renderPanoramaBackground(context, delta);
        }
        if (ClientSetting.INSTANCE.darkening.getValue() && !((Object)this instanceof TitleScreen)) {
            this.renderDarkening(context);
        }
        if (this.client.world != null && ClickGui.getInstance().tint.booleanValue) {
            context.fillGradient(0, 0, this.width, this.height, ClickGui.getInstance().tint.getValue().getRGB(), ClickGui.getInstance().endColor.getValue().getRGB());
        }
    }

    @Shadow
    protected void renderPanoramaBackground(DrawContext context, float delta) {
    }

    @Shadow
    protected void renderDarkening(DrawContext context) {
    }

    @Shadow
    public void close() {
    }

    @Shadow
    public ScreenRect getNavigationFocus() {
        return null;
    }

    @Shadow
    protected <T extends Element & Drawable> T addDrawableChild(T drawableElement) {
        return null;
    }
}

