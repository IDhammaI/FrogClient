package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.Frog;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.StringSetting;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;

public class WaterMarkHudModule extends HudModule {
    public static WaterMarkHudModule INSTANCE;
    public final StringSetting title = this.add(new StringSetting("Title", "%hackname% %version%"));

    public WaterMarkHudModule() {
        super("WaterMark", "水印", 1, 1);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        String text = this.title.getValue().replaceAll("%version%", Frog.VERSION).replaceAll("%hackname%", Frog.NAME);
        int w = HudSetting.useFont() ? (int)Math.ceil(FontManager.ui.getWidth(text)) : WaterMarkHudModule.mc.textRenderer.getWidth(text);
        int h;
        if (HudSetting.useFont()) {
            h = (int)Math.ceil(FontManager.ui.getFontHeight());
        } else {
            Objects.requireNonNull(WaterMarkHudModule.mc.textRenderer);
            h = 9;
        }

        int x = this.getHudRenderX(w);
        int y = this.getHudRenderY(h);

        int color = this.getHudColor(0.0);
        TextUtil.drawString(context, text, x, y, color, HudSetting.useFont(), HudSetting.useShadow());

        this.setHudBounds(x, y, Math.max(1, w), Math.max(1, h));
    }

    private int getHudColor(double delay) {
        ClickGui gui = ClickGui.getInstance();
        return gui == null ? -1 : gui.getColor(delay).getRGB();
    }
}
