package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

public class ItemCounterHudModule extends HudModule {
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true).setParent());
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true, () -> this.font.isOpen()));
    private final ItemStack stack;
    private final IntSupplier countSupplier;

    public ItemCounterHudModule(String name, String chinese, Item item, int defaultX, int defaultY) {
        this(name, chinese, item, defaultX, defaultY, () -> InventoryUtil.getItemCount(item));
    }

    public ItemCounterHudModule(String name, String chinese, Item item, int defaultX, int defaultY, IntSupplier countSupplier) {
        super(name, chinese, defaultX, defaultY);
        this.stack = new ItemStack((ItemConvertible)item);
        this.countSupplier = countSupplier;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        int count = this.countSupplier.getAsInt();
        if (count <= 0) {
            this.clearHudBounds();
            return;
        }
        this.stack.setCount(count);
        int px = this.getHudRenderX(16);
        int py = this.getHudRenderY(16);
        this.setHudBounds(px, py, 16, 16);
        drawContext.drawItem(this.stack, px, py);
        if (this.font.getValue()) {
            String s = String.valueOf(count);
            int tx = px + 16 - (int)Math.ceil(FontManager.ui.getWidth(s));
            int ty = py + 16 - (int)Math.ceil(FontManager.ui.getFontHeight());
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
            FontManager.ui.drawString(drawContext.getMatrices(), s, (double)(tx + 1), (double)(ty + 1), -1, this.shadow.getValue());
            drawContext.getMatrices().pop();
        } else {
            drawContext.drawItemInSlot(ItemCounterHudModule.mc.textRenderer, this.stack, px, py);
        }
    }
}