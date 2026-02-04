package dev.idhammai.mod.modules.impl.client;

import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.mod.modules.HudModule;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

public class ItemCounterHudModule extends HudModule {
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
        int px = this.getHudX();
        int py = this.getHudY();
        this.setHudBounds(px, py, 16, 16);
        drawContext.drawItem(this.stack, px, py);
        drawContext.drawItemInSlot(ItemCounterHudModule.mc.textRenderer, this.stack, px, py);
    }
}