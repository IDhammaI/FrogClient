package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.Frog;
import dev.idhammai.api.events.eventbus.EventListener;
import dev.idhammai.api.events.impl.ClientTickEvent;
import dev.idhammai.api.events.impl.InitEvent;
import dev.idhammai.api.utils.math.Animation;
import dev.idhammai.api.utils.math.Easing;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.Render2DUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.Module;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.EnumSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;

public class ArrayListHudModule extends HudModule {
    public static ArrayListHudModule INSTANCE;

    private final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false));
    private final BooleanSetting listSort = this.add(new BooleanSetting("ListSort", true));
    private final BooleanSetting rightAlign = this.add(new BooleanSetting("RightAlign", true));

    private final SliderSetting xOffset = this.add(new SliderSetting("XOffset", 0.0, 0.0, 50.0, 0.1));
    private final SliderSetting textOffset = this.add(new SliderSetting("TextOffset", 0.0, -10.0, 10.0, 0.1));
    private final SliderSetting interval = this.add(new SliderSetting("Interval", 0.0, 0.0, 15.0, 0.1));
    private final SliderSetting enableLength = this.add(new SliderSetting("EnableLength", 200, 0, 1000));
    private final SliderSetting disableLength = this.add(new SliderSetting("DisableLength", 200, 0, 1000));
    private final SliderSetting fadeLength = this.add(new SliderSetting("FadeLength", 200, 0, 1000));
    private final EnumSetting<Easing> easing = this.add(new EnumSetting<Easing>("Easing", Easing.CircInOut));

    private final BooleanSetting blur = this.add(new BooleanSetting("Blur", false).setParent());
    private final SliderSetting radius = this.add(new SliderSetting("Radius", 10.0, 0.0, 100.0, () -> this.blur.isOpen()));
    private final BooleanSetting backGround = this.add(new BooleanSetting("BackGround", false).setParent());
    private final SliderSetting width = this.add(new SliderSetting("Width", 0.0, 0.0, 15.0, () -> this.backGround.isOpen()));
    private final BooleanSetting rect = this.add(new BooleanSetting("Rect", false));
    private final BooleanSetting glow = this.add(new BooleanSetting("Glow", false));

    private final ArrayList<Entry> entries = new ArrayList<>();

    public ArrayListHudModule() {
        super("ArrayList", "", "模块列表", 2, 2, Corner.RightTop);
        INSTANCE = this;
        Frog.EVENT_BUS.subscribe(new InitHandler());
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        if (Module.nullCheck()) {
            this.clearHudBounds();
            return;
        }

        int fontHeight = this.getFontHeight();

        double maxLineW = 0.0;
        for (Entry e : this.entries) {
            e.prepare(this);
            if (e.renderFade <= 0.01 && e.renderWidth <= 0.01) {
                continue;
            }
            maxLineW = Math.max(maxLineW, e.renderWidth);
        }

        if (maxLineW <= 0.5) {
            this.clearHudBounds();
            return;
        }

        float extraW = this.width.getValueFloat();
        float xPadHalf = extraW / 2.0f;
        float lineH = (float)fontHeight + this.interval.getValueFloat();
        float yPad = this.interval.getValueFloat() / 2.0f;

        double usedH = 0.0;
        for (Entry e : this.entries) {
            if (e.renderFade <= 0.04) {
                continue;
            }
            usedH += ((double)fontHeight + this.interval.getValue()) * e.renderFade;
        }

        int boundsW = Math.max(1, (int)Math.ceil(maxLineW + (double)extraW + (double)xPadHalf));
        int boundsH = Math.max(1, (int)Math.ceil(Math.max(1.0, usedH + (double)fontHeight)));

        int boundsX;
        int boundsY;
        int startX;
        int startY;
        boundsX = this.getHudRenderX(boundsW);
        boundsY = this.getHudRenderY(boundsH);
        startX = (int)Math.floor((double)boundsX + (double)xPadHalf);
        startY = boundsY;

        double counter = 20.0;
        double currentY = startY;

        for (Entry e : this.entries) {
            if (e.renderFade <= 0.04) {
                continue;
            }

            double lineW = e.renderWidth;
            float x = this.rightAlign.getValue() ? (float)(startX + maxLineW - lineW) : (float)startX;

            double fade = e.renderFade;
            Color baseColor = this.getHudColor(counter += fade);
            int baseAlpha = baseColor.getAlpha();
            int c = ColorUtil.injectAlpha(baseColor.getRGB(), (int)((double)baseAlpha * fade));

            float bgX = x - xPadHalf;
            float bgY = (float)currentY - 1.0f - yPad;
            float bgW = (float)(lineW + (double)extraW);

            if (this.blur.getValue()) {
                Frog.BLUR.applyBlur((float)(this.radius.getValue() * fade), bgX, bgY, bgW, lineH);
            }
            if (this.backGround.getValue()) {
                Render2DUtil.drawRect(context.getMatrices(), bgX, bgY, bgW, lineH, ColorUtil.injectAlpha(baseColor.getRGB(), (int)(100.0 * fade)));
            }
            if (this.glow.getValue()) {
                Render2DUtil.drawGlow(context.getMatrices(), bgX, bgY, bgW, lineH, ColorUtil.injectAlpha(baseColor.getRGB(), (int)((double)baseAlpha * fade)));
            }

            TextUtil.drawString(context, e.string, (double)x, currentY + (double)this.textOffset.getValueFloat(), c, HudSetting.useFont(), HudSetting.useShadow());

            if (this.rect.getValue()) {
                Render2DUtil.drawRect(context.getMatrices(), bgX + bgW, bgY, 1.0f, lineH, ColorUtil.injectAlpha(baseColor.getRGB(), (int)((double)baseAlpha * fade)));
            }

            currentY += ((double)fontHeight + this.interval.getValue()) * fade;
        }

        this.setHudBounds(boundsX, boundsY, boundsW, boundsH);
    }

    @EventListener(priority = -999)
    public void onUpdate(ClientTickEvent event) {
        if (Module.nullCheck()) {
            return;
        }
        if (event.isPost()) {
            for (Entry e : this.entries) {
                e.onUpdate(this);
            }
            if (this.listSort.getValue()) {
                this.entries.sort(Comparator.comparingInt(e -> e.string == null ? 0 : -this.getWidth(e.string)));
            }
        }
    }

    private int getWidth(String s) {
        if (s == null) {
            return 0;
        }
        if (HudSetting.useFont()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return ArrayListHudModule.mc.textRenderer.getWidth(s);
    }

    private int getFontHeight() {
        if (HudSetting.useFont()) {
            return (int)FontManager.ui.getFontHeight();
        }
        Objects.requireNonNull(ArrayListHudModule.mc.textRenderer);
        return 9;
    }

    private Color getHudColor(double counter) {
        ClickGui gui = ClickGui.getInstance();
        return gui == null ? Color.WHITE : gui.getColor(counter);
    }

    private class InitHandler {
        @EventListener
        public void onInit(InitEvent event) {
            for (Module module : Frog.MODULE.getModules()) {
                ArrayListHudModule.this.entries.add(new Entry(module));
            }
            Frog.EVENT_BUS.unsubscribe(this);
        }
    }

    private static final class Entry {
        private final Module module;
        private String string = "";
        private boolean isOn;
        private double currentX = 0.0;
        private final Animation animation = new Animation();
        private final Animation fadeAnimation = new Animation();

        private double renderWidth;
        private double renderFade;

        private Entry(Module module) {
            this.module = module;
        }

        private void onUpdate(ArrayListHudModule parent) {
            this.isOn = this.module.isOn() && this.module.drawn.getValue();
            if (this.isOn) {
                String s = this.module.getArrayName();
                if (s == null) {
                    s = "";
                }
                this.string = parent.lowerCase.getValue() ? s.toLowerCase() : s;
            }
        }

        private void prepare(ArrayListHudModule parent) {
            if (this.currentX <= 0.0 && !this.isOn) {
                this.renderWidth = 0.0;
                this.renderFade = 0.0;
                return;
            }
            String text = this.string == null ? "" : this.string;
            double target = (double)(parent.getWidth(text) + 1);
            this.currentX = this.animation.get(this.isOn ? target : 0.0, this.isOn ? (long)parent.enableLength.getValueInt() : (long)parent.disableLength.getValueInt(), parent.easing.getValue());
            this.renderFade = this.fadeAnimation.get(this.isOn ? 1.0 : 0.0, parent.fadeLength.getValueInt(), parent.easing.getValue());
            this.renderWidth = this.currentX + (double)parent.xOffset.getValueFloat();
        }
    }
}
