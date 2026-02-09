package dev.idhammai.mod.modules.impl.client.hud;

import dev.idhammai.Frog;
import dev.idhammai.api.utils.player.InventoryUtil;
import dev.idhammai.core.impl.FontManager;
import dev.idhammai.mod.gui.clickgui.ClickGuiScreen;
import dev.idhammai.mod.gui.windows.impl.ItemSelectWindow;
import dev.idhammai.mod.modules.HudModule;
import dev.idhammai.mod.modules.impl.client.ClickGui;
import dev.idhammai.mod.modules.settings.impl.BooleanSetting;
import dev.idhammai.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemCounterHudModule extends HudModule {
    private final ItemStack stack;
    private final IntSupplier countSupplier;
    private final boolean useManager;
    private final BooleanSetting edit;
    private final SliderSetting perRow;
    private final BooleanSetting showZero;

    public ItemCounterHudModule(String name, String chinese, Item item, int defaultX, int defaultY) {
        this(name, chinese, item, defaultX, defaultY, () -> InventoryUtil.getItemCount(item));
    }

    public ItemCounterHudModule(String name, String chinese, Item item, int defaultX, int defaultY, IntSupplier countSupplier) {
        super(name, chinese, defaultX, defaultY);
        this.stack = new ItemStack((ItemConvertible)item);
        this.countSupplier = countSupplier;
        this.useManager = false;
        this.edit = null;
        this.perRow = null;
        this.showZero = null;
    }

    public ItemCounterHudModule(String name, String chinese, int defaultX, int defaultY) {
        super(name, chinese, defaultX, defaultY);
        this.stack = new ItemStack((ItemConvertible)Items.AIR);
        this.countSupplier = null;
        this.useManager = true;
        this.edit = this.add(new BooleanSetting("Edit", false).injectTask(this::openGui));
        this.perRow = this.add(new SliderSetting("PerRow", 8, 1, 20));
        this.showZero = this.add(new BooleanSetting("ShowZero", false));
    }

    private void openGui() {
        this.edit.setValueWithoutTask(false);
        if (!ItemCounterHudModule.nullCheck()) {
            ClickGuiScreen screen = ClickGuiScreen.getInstance();
            screen.openHudWindow(new ItemSelectWindow(Frog.HUD_ITEM));
            ClickGui clickGui = ClickGui.getInstance();
            if (clickGui != null && !clickGui.isOn()) {
                clickGui.enable();
            } else if (mc.currentScreen != screen) {
                mc.setScreen(screen);
            }
        }
    }

    private Item resolveItem(String key) {
        String name = key.replace("block.minecraft.", "").replace("item.minecraft.", "");
        Identifier id;
        try {
            id = name.contains(":") ? Identifier.of(name) : Identifier.of("minecraft", name);
        }
        catch (Exception e) {
            return null;
        }
        Item item = (Item)Registries.ITEM.get(id);
        return item == Items.AIR ? null : item;
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (!this.useManager) {
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
            if (HudSetting.useFont()) {
                String s = String.valueOf(count);
                int tx = px + 16 - (int)Math.ceil(FontManager.ui.getWidth(s));
                int ty = py + 16 - (int)Math.ceil(FontManager.ui.getFontHeight());
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
                FontManager.ui.drawString(drawContext.getMatrices(), s, (double)(tx + 1), (double)(ty + 1), -1, HudSetting.useShadow());
                drawContext.getMatrices().pop();
            } else {
                drawContext.drawItemInSlot(ItemCounterHudModule.mc.textRenderer, this.stack, px, py);
            }
            return;
        }
        List<ItemStack> stacks = new ArrayList();
        List<Integer> counts = new ArrayList();
        for (String key : Frog.HUD_ITEM.getList()) {
            Item item = this.resolveItem(key);
            if (item == null) continue;
            int count = InventoryUtil.getItemCount(item);
            if (count <= 0 && !this.showZero.getValue()) continue;
            ItemStack stack = new ItemStack((ItemConvertible)item);
            stack.setCount(Math.max(1, count));
            stacks.add(stack);
            counts.add(count);
        }
        if (stacks.isEmpty()) {
            this.clearHudBounds();
            return;
        }
        int perRowValue = Math.max(1, this.perRow.getValueInt());
        int rows = (stacks.size() + perRowValue - 1) / perRowValue;
        int columns = Math.min(perRowValue, stacks.size());
        int width = columns * 18;
        int height = rows * 18;
        int px = this.getHudRenderX(width);
        int py = this.getHudRenderY(height);
        this.setHudBounds(px, py, width, height);
        for (int i = 0; i < stacks.size(); ++i) {
            int row = i / perRowValue;
            int col = i % perRowValue;
            int itemX = px + col * 18;
            int itemY = py + row * 18;
            ItemStack stack = (ItemStack)stacks.get(i);
            int count = (Integer)counts.get(i);
            if (HudSetting.useFont()) {
                drawContext.drawItem(stack, itemX, itemY);
                String s = String.valueOf(count);
                int tx = itemX + 16 - (int)Math.ceil(FontManager.ui.getWidth(s));
                int ty = itemY + 16 - (int)Math.ceil(FontManager.ui.getFontHeight());
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
                FontManager.ui.drawString(drawContext.getMatrices(), s, (double)(tx + 1), (double)(ty + 1), -1, HudSetting.useShadow());
                drawContext.getMatrices().pop();
                continue;
            }
            drawContext.drawItemInSlot(ItemCounterHudModule.mc.textRenderer, stack, itemX, itemY);
        }
    }
}
