package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.ColorSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

public class CoordsHudModule extends HudModule {
    public static CoordsHudModule INSTANCE;
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
    private final ColorSetting pulse = this.add(new ColorSetting("Pulse", new Color(79, 0, 0)).injectBoolean(true));
    private final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0, 0.0, 5.0, 0.1));
    private final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50));

    public CoordsHudModule() {
        super("Coords", "坐标", 2, 2);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        if (CoordsHudModule.mc.player == null || CoordsHudModule.mc.world == null) {
            this.clearHudBounds();
            return;
        }

        String text = this.getCoordsString();
        if (this.lowerCase.getValue()) {
            text = text.toLowerCase();
        }

        int x = this.getHudX();
        int y = this.getHudY();

        if (this.pulse.booleanValue) {
            TextUtil.drawStringPulse(context, text, x, y, this.color.getValue(), this.pulse.getValue(), this.pulseSpeed.getValue(), this.pulseCounter.getValueInt(), this.font.getValue(), this.shadow.getValue());
        } else {
            TextUtil.drawString(context, text, x, y, this.color.getValue().getRGB(), this.font.getValue(), this.shadow.getValue());
        }

        int w = this.font.getValue() ? (int)Math.ceil(FontManager.ui.getWidth(text)) : CoordsHudModule.mc.textRenderer.getWidth(text);
        int h;
        if (this.font.getValue()) {
            h = (int)Math.ceil(FontManager.ui.getFontHeight());
        } else {
            Objects.requireNonNull(CoordsHudModule.mc.textRenderer);
            h = 9;
        }
        this.setHudBounds(x, y, Math.max(1, w), Math.max(1, h));
    }

    private String getCoordsString() {
        boolean inNether = CoordsHudModule.mc.world.getRegistryKey().equals(World.NETHER);
        int posX = CoordsHudModule.mc.player.getBlockX();
        int posY = CoordsHudModule.mc.player.getBlockY();
        int posZ = CoordsHudModule.mc.player.getBlockZ();
        float factor = !inNether ? 0.125f : 8.0f;
        int anotherWorldX = (int)(CoordsHudModule.mc.player.getX() * (double)factor);
        int anotherWorldZ = (int)(CoordsHudModule.mc.player.getZ() * (double)factor);
        return "XYZ \u00a7f" + (inNether ? posX + ", " + posY + ", " + posZ + " \u00a77[\u00a7f" + anotherWorldX + ", " + anotherWorldZ + "\u00a77]\u00a7f" : posX + ", " + posY + ", " + posZ + "\u00a77 [\u00a7f" + anotherWorldX + ", " + anotherWorldZ + "\u00a77]");
    }
}