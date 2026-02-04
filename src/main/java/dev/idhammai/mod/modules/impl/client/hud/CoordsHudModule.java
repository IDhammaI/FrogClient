package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

public class CoordsHudModule extends HudModule {
    public static CoordsHudModule INSTANCE;
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false));

    public CoordsHudModule() {
        super("Coords", "","坐标", 2, 2, PosMode.Corner, Corner.LeftBottom);
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

        int w = this.font.getValue() ? (int)Math.ceil(FontManager.ui.getWidth(text)) : CoordsHudModule.mc.textRenderer.getWidth(text);
        int h;
        if (this.font.getValue()) {
            h = (int)Math.ceil(FontManager.ui.getFontHeight());
        } else {
            Objects.requireNonNull(CoordsHudModule.mc.textRenderer);
            h = 9;
        }

        int x = this.getHudRenderX(w);
        int y = this.getHudRenderY(h);

        int color = this.getHudColor(0.0);
        TextUtil.drawString(context, text, x, y, color, this.font.getValue(), this.shadow.getValue());

        this.setHudBounds(x, y, Math.max(1, w), Math.max(1, h));
    }

    private int getHudColor(double delay) {
        ClickGui gui = ClickGui.getInstance();
        return gui == null ? -1 : gui.getColor(delay).getRGB();
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