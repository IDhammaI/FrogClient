package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.api.utils.player.EntityUtil;
import dev.idhammai.api.utils.render.ColorUtil;
import dev.idhammai.api.utils.render.TextUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorHudModule extends HudModule {
    public static ArmorHudModule INSTANCE;
    private final BooleanSetting durability = this.add(new BooleanSetting("Durability", true));
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final Color minColor = new Color(196, 0, 0);
    private final Color maxColor = new Color(0, 227, 0);

    public ArmorHudModule() {
        super("Armor", "护甲", 0, 70);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        if (ArmorHudModule.mc.player == null) {
            this.clearHudBounds();
            return;
        }

        int slot = 0;
        for (ItemStack armor : ArmorHudModule.mc.player.getInventory().armor) {
            ++slot;
        }
        int w = Math.max(1, 20 * slot);
        int topPad = this.durability.getValue() ? 6 : 0;
        int h = 16 + topPad;

        int boundsX = this.getHudRenderX(w);
        int boundsY = this.getHudRenderY(h);
        int px = boundsX;
        int py = boundsY + topPad;

        slot = 0;
        for (ItemStack armor : ArmorHudModule.mc.player.getInventory().armor) {
            int x = px + slot * 20;
            if (!armor.isEmpty()) {
                context.getMatrices().push();
                int damage = EntityUtil.getDamagePercent(armor);
                context.drawItem(armor, x, py);
                context.drawItemInSlot(ArmorHudModule.mc.textRenderer, armor, x, py);
                if (this.durability.getValue()) {
                    if (this.font.getValue()) {
                        FontManager.small.drawString(context.getMatrices(), damage + "%", (double)(x + 1), (double)((float)py - FontManager.small.getFontHeight() / 2.0f), ColorUtil.fadeColor(this.minColor, this.maxColor, (float)damage / 100.0f).getRGB(), this.shadow.getValue());
                    } else {
                        String s = damage + "%";
                        float fx = x + 2;
                        float fy = py;
                        Objects.requireNonNull(ArmorHudModule.mc.textRenderer);
                        TextUtil.drawStringScale(context, s, fx, fy - 9.0f / 4.0f, ColorUtil.fadeColor(this.minColor, this.maxColor, (float)damage / 100.0f).getRGB(), 0.5f, this.shadow.getValue());
                    }
                }
                context.getMatrices().pop();
            }
            ++slot;
        }

        this.setHudBounds(boundsX, boundsY, w, h);
    }
}