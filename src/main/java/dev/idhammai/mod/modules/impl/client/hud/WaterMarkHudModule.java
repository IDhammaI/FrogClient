package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.Frog;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;

public class WaterMarkHudModule extends HudModule {
    public static WaterMarkHudModule INSTANCE;
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
    private final ColorSetting pulse = this.add(new ColorSetting("Pulse", new Color(79, 0, 0)).injectBoolean(true));
    private final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0, 0.0, 5.0, 0.1));
    private final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50));
    public final StringSetting title = this.add(new StringSetting("Title", "%hackname% %version%"));

    public WaterMarkHudModule() {
        super("WaterMark", "水印", 1, 1);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        String text = this.title.getValue().replaceAll("%version%", Frog.VERSION).replaceAll("%hackname%", Frog.NAME);
        int x = this.getHudX();
        int y = this.getHudY();

        if (this.pulse.booleanValue) {
            TextUtil.drawStringPulse(context, text, x, y, this.color.getValue(), this.pulse.getValue(), this.pulseSpeed.getValue(), this.pulseCounter.getValueInt(), this.font.getValue(), this.shadow.getValue());
        } else {
            TextUtil.drawString(context, text, x, y, this.color.getValue().getRGB(), this.font.getValue(), this.shadow.getValue());
        }

        int w = this.font.getValue() ? (int)Math.ceil(FontManager.ui.getWidth(text)) : WaterMarkHudModule.mc.textRenderer.getWidth(text);
        int h;
        if (this.font.getValue()) {
            h = (int)Math.ceil(FontManager.ui.getFontHeight());
        } else {
            Objects.requireNonNull(WaterMarkHudModule.mc.textRenderer);
            h = 9;
        }
        this.setHudBounds(x, y, Math.max(1, w), Math.max(1, h));
    }
}