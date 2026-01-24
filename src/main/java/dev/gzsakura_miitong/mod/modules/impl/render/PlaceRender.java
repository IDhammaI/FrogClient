/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 */
package dev.gzsakura_miitong.mod.modules.impl.render;

import dev.gzsakura_miitong.api.utils.Wrapper;
import dev.gzsakura_miitong.api.utils.math.Easing;
import dev.gzsakura_miitong.api.utils.math.FadeUtils;
import dev.gzsakura_miitong.api.utils.math.Timer;
import dev.gzsakura_miitong.api.utils.render.ColorUtil;
import dev.gzsakura_miitong.api.utils.render.Render3DUtil;
import dev.gzsakura_miitong.mod.modules.Module;
import dev.gzsakura_miitong.mod.modules.settings.impl.BooleanSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.ColorSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.EnumSetting;
import dev.gzsakura_miitong.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PlaceRender
extends Module {
    public static final HashMap<BlockPos, PlacePos> renderMap = new HashMap();
    public static PlaceRender INSTANCE;
    public final SliderSetting fadeTime = this.add(new SliderSetting("FadeTime", 500, 0, 3000));
    public final SliderSetting timeout = this.add(new SliderSetting("TimeOut", 500, 0, 3000));
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting tryPlaceBox = this.add(new ColorSetting("TryPlaceBox", new Color(178, 178, 178, 255)).injectBoolean(true));
    private final ColorSetting tryPlaceFill = this.add(new ColorSetting("TryPlaceFill", new Color(255, 119, 119, 157)).injectBoolean(true));
    private final BooleanSetting noFail = this.add(new BooleanSetting("NoFail", false));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut));
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.All));

    public PlaceRender() {
        super("PlaceRender", Module.Category.Render);
        this.setChinese("\u653e\u7f6e\u663e\u793a");
        this.enable();
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        renderMap.values().removeIf(v -> v.draw(matrixStack));
    }

    public void create(BlockPos pos) {
        renderMap.put(pos, new PlacePos(pos));
    }

    private static enum Mode {
        Fade,
        Shrink,
        All;

    }

    public class PlacePos {
        public final FadeUtils fade;
        public final BlockPos pos;
        public final Timer timer;
        public boolean isAir;

        public PlacePos(BlockPos placePos) {
            this.fade = new FadeUtils((long)PlaceRender.this.fadeTime.getValue());
            this.pos = placePos;
            this.timer = new Timer();
            this.isAir = true;
        }

        public boolean draw(MatrixStack matrixStack) {
            double quads;
            if (this.isAir) {
                if (PlaceRender.this.noFail.getValue() || !Wrapper.mc.world.isAir(this.pos)) {
                    this.isAir = false;
                } else {
                    if (!this.timer.passedMs(PlaceRender.this.timeout.getValue())) {
                        this.fade.reset();
                        Box aBox = new Box(this.pos);
                        if (PlaceRender.this.tryPlaceFill.booleanValue) {
                            Render3DUtil.drawFill(matrixStack, aBox, PlaceRender.this.tryPlaceFill.getValue());
                        }
                        if (PlaceRender.this.tryPlaceBox.booleanValue) {
                            Render3DUtil.drawBox(matrixStack, aBox, PlaceRender.this.tryPlaceBox.getValue());
                        }
                    }
                    return false;
                }
            }
            if ((quads = this.fade.ease(PlaceRender.this.ease.getValue())) == 1.0) {
                return true;
            }
            double alpha = PlaceRender.this.mode.getValue() == Mode.Fade || PlaceRender.this.mode.getValue() == Mode.All ? 1.0 - quads : 1.0;
            double size = PlaceRender.this.mode.getValue() == Mode.Shrink || PlaceRender.this.mode.getValue() == Mode.All ? quads : 0.0;
            Box aBox = new Box(this.pos).expand(-size * 0.5, -size * 0.5, -size * 0.5);
            if (PlaceRender.this.fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, aBox, ColorUtil.injectAlpha(PlaceRender.this.fill.getValue(), (int)((double)PlaceRender.this.fill.getValue().getAlpha() * alpha)));
            }
            if (PlaceRender.this.box.booleanValue) {
                Render3DUtil.drawBox(matrixStack, aBox, ColorUtil.injectAlpha(PlaceRender.this.box.getValue(), (int)((double)PlaceRender.this.box.getValue().getAlpha() * alpha)));
            }
            return false;
        }
    }
}

